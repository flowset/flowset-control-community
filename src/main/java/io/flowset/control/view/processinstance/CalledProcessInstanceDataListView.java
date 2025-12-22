/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processinstance;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import io.flowset.control.view.util.ComponentHelper;
import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.core.Messages;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.Supply;
import io.jmix.flowui.view.Target;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.flowset.control.entity.filter.ProcessInstanceFilter;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.service.processinstance.ProcessInstanceLoadContext;
import io.flowset.control.service.processinstance.ProcessInstanceService;
import io.flowset.control.view.main.MainView;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@Route(value = "called-process-instances", layout = MainView.class)
@ViewController(id = "bpm_CalledProcessInstanceData.list")
@ViewDescriptor(path = "called-process-instance-data-list-view.xml")
@LookupComponent("processInstancesDataGrid")
@DialogMode(minWidth = "65em", width = "70%")
public class CalledProcessInstanceDataListView extends StandardListView<ProcessInstanceData> {

    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected ProcessInstanceService processInstanceService;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected Messages messages;
    @Autowired
    protected ViewNavigators viewNavigators;
    @Autowired
    protected ComponentHelper componentHelper;

    protected List<String> processInstanceIds;

    public void setProcessInstanceIds(List<String> processInstanceIds) {
        this.processInstanceIds = processInstanceIds;
    }

    @Install(to = "processInstancesDl", target = Target.DATA_LOADER)
    protected List<ProcessInstanceData> processInstancesDlLoadDelegate(LoadContext<ProcessInstanceData> loadContext) {
        if (CollectionUtils.isEmpty(processInstanceIds)) {
            return List.of();
        }
        ProcessInstanceFilter filter = dataManager.create(ProcessInstanceFilter.class);
        filter.setProcessInstanceIds(processInstanceIds);

        ProcessInstanceLoadContext context = new ProcessInstanceLoadContext()
                .setFirstResult(0)
                .setMaxResults(processInstanceIds.size())
                .setFilter(filter);

        return processInstanceService.findAllHistoricInstances(context);
    }

    @Supply(to = "processInstancesDataGrid.processDefinitionId", subject = "renderer")
    protected Renderer<ProcessInstanceData> processInstancesDataGridProcessDefinitionIdRenderer() {
        return new TextRenderer<>(item -> item.getProcessDefinitionVersion() == null ? item.getProcessDefinitionId() :
                componentHelper.getProcessLabel(item.getProcessDefinitionKey(), item.getProcessDefinitionVersion()));
    }


    @Supply(to = "processInstancesDataGrid.actions", subject = "renderer")
    protected Renderer<ProcessInstanceData> processInstancesDataGridActionsRenderer() {
        return new ComponentRenderer<>(item -> {
            JmixButton viewButton = uiComponents.create(JmixButton.class);
            viewButton.setText(messages.getMessage("actions.View"));
            viewButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            viewButton.setIcon(VaadinIcon.EYE.create());
            viewButton.addClickListener(event -> {
                if (UiComponentUtils.isComponentAttachedToDialog(this)) {
                    RouterLink routerLink = new RouterLink(ProcessInstanceDetailView.class,
                            new RouteParameters("id", item.getId()));
                    getUI().ifPresent(ui -> ui.getPage().open(routerLink.getHref()));
                } else {
                    openProcessInstanceDetailView(item);
                }
            });

            return viewButton;
        });
    }

    protected void openProcessInstanceDetailView(ProcessInstanceData item) {
        viewNavigators.detailView(getCurrentView(), ProcessInstanceData.class)
                .withViewClass(ProcessInstanceDetailView.class)
                .withRouteParameters(new RouteParameters("id", item.getId()))
                .withBackwardNavigation(true)
                .navigate();
    }
}
