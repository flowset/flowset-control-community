/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processdefinition;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.core.AccessManager;
import io.jmix.core.DataLoadContext;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiEventPublisher;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.accesscontext.UiEntityContext;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.pagination.SimplePagination;
import io.jmix.flowui.data.pagination.PaginationDataLoader;
import io.jmix.flowui.data.pagination.PaginationDataLoaderImpl;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.BaseCollectionLoader;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.HasLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Supply;
import io.jmix.flowui.view.ViewComponent;
import io.flowset.control.entity.filter.ProcessInstanceFilter;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.entity.processinstance.RuntimeProcessInstanceData;
import io.flowset.control.service.processinstance.ProcessInstanceService;
import io.flowset.control.view.processdefinition.event.ResetActivityEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@FragmentDescriptor("process-instances-fragment.xml")
public class ProcessInstancesFragment extends Fragment<VerticalLayout> {

    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected Messages messages;
    @Autowired
    protected ViewNavigators viewNavigators;
    @Autowired
    protected ProcessInstanceService processInstanceService;
    @Autowired
    protected Fragments fragments;
    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected Metadata metadata;
    @Autowired
    private UiEventPublisher uiEventPublisher;

    @ViewComponent
    protected MessageBundle messageBundle;
    @ViewComponent
    protected InstanceContainer<ProcessDefinitionData> processDefinitionDataDc;
    @ViewComponent
    protected CollectionContainer<RuntimeProcessInstanceData> processInstanceDataDc;
    @ViewComponent
    protected VerticalLayout processInstanceVBox;
    @ViewComponent
    protected DataGrid<RuntimeProcessInstanceData> processInstancesGrid;
    @ViewComponent
    protected SimplePagination processInstancesPagination;
    @ViewComponent
    private Div selectedActivityContainer;
    @ViewComponent
    private InstanceContainer<ProcessInstanceFilter> processInstanceFilterDc;

    @Subscribe
    public void onReady(ReadyEvent event) {
        if (processInstanceDataDc instanceof HasLoader container && container.getLoader() instanceof BaseCollectionLoader) {
            PaginationDataLoader paginationLoader =
                    applicationContext.getBean(PaginationDataLoaderImpl.class, container.getLoader());
            processInstancesPagination.setPaginationLoader(paginationLoader);
        }
    }

    @Install(to = "processInstancesPagination", subject = "totalCountDelegate")
    protected Integer processInstancesPaginationTotalCountDelegate(final DataLoadContext dataLoadContext) {
        return (int) processInstanceService.getRuntimeInstancesCount(processInstanceFilterDc.getItem());
    }

    @Subscribe("processInstancesGrid.edit")
    public void onProcessDefinitionsGridViewDetails(ActionPerformedEvent event) {
        RuntimeProcessInstanceData selectedInstance = processInstancesGrid.getSingleSelectedItem();
        if (selectedInstance == null) {
            return;
        }
        openProcessInstanceDetailView(selectedInstance);
    }

    @Install(to = "processInstancesGrid.id", subject = "tooltipGenerator")
    protected String processInstancesGridIdTooltipGenerator(final RuntimeProcessInstanceData processInstanceData) {
        return processInstanceData.getId();
    }

    @Supply(to = "processInstancesGrid.actions", subject = "renderer")
    protected Renderer<RuntimeProcessInstanceData> processInstancesGridActionsRenderer() {
        return new ComponentRenderer<>(processInstance -> {
            UiEntityContext context = new UiEntityContext(metadata.getClass(processInstance));
            accessManager.applyRegisteredConstraints(context);

            if (!context.isViewPermitted()) {
                return null;
            }

            JmixButton viewButton = uiComponents.create(JmixButton.class);
            viewButton.setIcon(VaadinIcon.EYE.create());
            viewButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            viewButton.setText(messages.getMessage("actions.View"));
            viewButton.addClickListener(event -> openProcessInstanceDetailView(processInstance));
            return viewButton;
        });
    }


    protected void openProcessInstanceDetailView(RuntimeProcessInstanceData selectedInstance) {
        viewNavigators.detailView(getCurrentView(), ProcessInstanceData.class)
                .withRouteParameters(new RouteParameters("id", selectedInstance.getId()))
                .withBackwardNavigation(true)
                .navigate();
    }

    public void clearActivity() {
        selectedActivityContainer.removeAll();
    }

    public void showActivity(String elementId, String elementType, String elementName) {
        selectedActivityContainer.removeAll();

        Span activityBadge = new Span(messageBundle.formatMessage("selectedActivityBadge.text", elementId));
        activityBadge.getElement().getThemeList().add("badge pill primary small");
        activityBadge.setHeight("min-content");

        Tooltip tooltip = Tooltip.forComponent(activityBadge);
        tooltip.setText("%s: %s".formatted(elementType, elementName));

        JmixButton clearBtn = uiComponents.create(JmixButton.class);
        clearBtn.setIcon(VaadinIcon.CLOSE_SMALL.create());
        clearBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        clearBtn.addClassNames(LumoUtility.TextColor.PRIMARY_CONTRAST);
        clearBtn.addClickListener(clickEvent -> {
            clearActivity();
            uiEventPublisher.publishEventForCurrentUI(new ResetActivityEvent(this, elementId));
        });

        activityBadge.add(clearBtn);

        selectedActivityContainer.add(activityBadge);
    }
}
