/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processdefinition;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.view.AbstractListViewWithDelayedLoad;
import io.jmix.core.DataLoadContext;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.filter.ProcessDefinitionFilter;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.entity.processdefinition.ProcessDefinitionState;
import io.flowset.control.service.processdefinition.ProcessDefinitionLoadContext;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import io.flowset.control.view.newprocessdeployment.NewProcessDeploymentView;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

@Route(value = "bpm/process-definitions", layout = DefaultMainViewParent.class)
@ViewController("bpm_ProcessDefinition.list")
@ViewDescriptor("process-definition-list-view.xml")
@Slf4j
public class ProcessDefinitionListView extends AbstractListViewWithDelayedLoad<ProcessDefinitionData> {

    @ViewComponent
    protected MessageBundle messageBundle;
    @Autowired
    protected ViewNavigators viewNavigators;
    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Fragments fragments;
    @Autowired
    protected Metadata metadata;

    @ViewComponent
    protected CollectionLoader<ProcessDefinitionData> processDefinitionsDl;
    @ViewComponent
    protected InstanceContainer<ProcessDefinitionFilter> processDefinitionFilterDc;

    @Autowired
    protected ProcessDefinitionService processDefinitionService;

    @ViewComponent
    protected JmixFormLayout filterFormLayout;
    @ViewComponent
    protected HorizontalLayout filterPanel;

    @ViewComponent
    protected DataGrid<ProcessDefinitionData> processDefinitionsGrid;

    @Subscribe
    public void onInit(final InitEvent event) {
        addClassNames(LumoUtility.Padding.Top.SMALL);
        initFilterFormStyles();
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        initFilter();
    }

    @Install(to = "processDefinitionsDl", target = Target.DATA_LOADER)
    protected List<ProcessDefinitionData> processDefinitionsDlLoadDelegate(final LoadContext<ProcessDefinitionData> loadContext) {
        LoadContext.Query query = loadContext.getQuery();
        ProcessDefinitionFilter filter = processDefinitionFilterDc.getItemOrNull();

        ProcessDefinitionLoadContext context = new ProcessDefinitionLoadContext().setFilter(filter);
        if (query != null) {
            context.setFirstResult(query.getFirstResult())
                    .setMaxResults(query.getMaxResults())
                    .setSort(query.getSort());
        }

        return loadItemsWithStateHandling(() -> processDefinitionService.findAll(context));
    }

    @Install(to = "processDefinitionsGrid.bulkActivate", subject = "enabledRule")
    protected boolean processDefinitionsGridBulkActivateEnabledRule() {
        Set<ProcessDefinitionData> selectedItems = processDefinitionsGrid.getSelectedItems();
        boolean suspendedDefinitionExists = selectedItems.stream().anyMatch(definition -> BooleanUtils.isTrue(definition.getSuspended()));

        return CollectionUtils.isNotEmpty(selectedItems) && suspendedDefinitionExists;
    }

    @Subscribe("processDefinitionsGrid.bulkActivate")
    public void onProcessDefinitionsGridBulkActivate(final ActionPerformedEvent event) {
        Set<ProcessDefinitionData> selectedItems = processDefinitionsGrid.getSelectedItems();
        dialogWindows.view(this, BulkActivateProcessDefinitionView.class)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                        startLoadData();
                    }
                })
                .withViewConfigurer(view -> view.setProcessDefinitions(selectedItems))
                .build()
                .open();
    }

    @Install(to = "processDefinitionPagination", subject = "totalCountDelegate")
    protected Integer processDefinitionPaginationTotalCountDelegate(final DataLoadContext dataLoadContext) {
        return (int) processDefinitionService.getCount(processDefinitionFilterDc.getItemOrNull());
    }

    @Subscribe(id = "processDefinitionFilterDc", target = Target.DATA_CONTAINER)
    public void onProcessDefinitionFilterDcItemPropertyChange(final InstanceContainer.ItemPropertyChangeEvent<ProcessDefinitionFilter> event) {
        startLoadData();
    }


    @Subscribe(id = "clearBtn", subject = "clickListener")
    public void onClearBtnClick(final ClickEvent<JmixButton> event) {
        ProcessDefinitionFilter filter = processDefinitionFilterDc.getItem();
        filter.setKeyLike(null);
        filter.setNameLike(null);
        filter.setState(null);
        filter.setLatestVersionOnly(true);
    }

    @Supply(to = "stateComboBox", subject = "renderer")
    protected Renderer<ProcessDefinitionState> stateComboBoxRenderer() {
        return new ComponentRenderer<>(processDefinitionState -> {
            if (processDefinitionState == ProcessDefinitionState.ACTIVE) {
                return createStateBadge(false);
            } else if (processDefinitionState == ProcessDefinitionState.SUSPENDED) {
                return createStateBadge(true);
            }
            return null;
        });
    }

    @Subscribe("processDefinitionsGrid.bulkRemove")
    protected void onProcessDefinitionsGridBulkRemove(final ActionPerformedEvent event) {
        Set<ProcessDefinitionData> selectedItems = processDefinitionsGrid.getSelectedItems();
        if (selectedItems.isEmpty()) {
            return;
        }
        dialogWindows.view(this, BulkDeleteProcessDefinitionView.class)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                        processDefinitionsDl.load();
                    }
                })
                .withViewConfigurer(view -> view.setProcessDefinitions(selectedItems))
                .build()
                .open();
    }

    @Subscribe("processDefinitionsGrid.deploy")
    protected void onProcessDefinitionsGridDeploy(final ActionPerformedEvent event) {
        viewNavigators.view(this, NewProcessDeploymentView.class)
                .withBackwardNavigation(true)
                .navigate();
    }

    @Install(to = "processDefinitionsGrid.name", subject = "tooltipGenerator")
    protected String processDefinitionsGridNameTooltipGenerator(final ProcessDefinitionData processDefinitionData) {
        return processDefinitionData.getName();
    }

    @Subscribe("applyFilter")
    public void onApplyFilter(ActionPerformedEvent event) {
        startLoadData();
    }

    @Supply(to = "processDefinitionsGrid.status", subject = "renderer")
    protected Renderer<ProcessDefinitionData> processDefinitionsGridStatusRenderer() {
        return new ComponentRenderer<>(this::createStateBadge);
    }

    @Supply(to = "processDefinitionsGrid.actions", subject = "renderer")
    protected Renderer<ProcessDefinitionData> processDefinitionsGridActionsRenderer() {
        return new ComponentRenderer<>((processDefinitionData) -> {
            ProcessDefinitionListItemActionsFragment actionsFragment = fragments.create(this, ProcessDefinitionListItemActionsFragment.class);
            actionsFragment.setProcessDefinition(processDefinitionData);
            return actionsFragment;
        });
    }

    @Install(to = "processDefinitionsGrid.bulkSuspend", subject = "enabledRule")
    protected boolean processDefinitionsGridBulkSuspendEnabledRule() {
        Set<ProcessDefinitionData> selectedDefinitions = processDefinitionsGrid.getSelectedItems();
        boolean activeDefinitionExists = selectedDefinitions.stream().anyMatch(definition -> BooleanUtils.isNotTrue(definition.getSuspended()));

        return CollectionUtils.isNotEmpty(selectedDefinitions) && activeDefinitionExists;
    }

    @Subscribe("processDefinitionsGrid.bulkSuspend")
    protected void onProcessDefinitionsGridBulkSuspend(final ActionPerformedEvent event) {
        Set<ProcessDefinitionData> selectedItems = processDefinitionsGrid.getSelectedItems();
        dialogWindows.view(this, BulkSuspendProcessDefinitionView.class)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                        processDefinitionsDl.load();
                    }
                })
                .withViewConfigurer(view -> view.setProcessDefinitions(selectedItems))
                .build()
                .open();
    }

    protected void initFilterFormStyles() {
        filterFormLayout.getOwnComponents().forEach(component -> component.addClassName(LumoUtility.Padding.Top.XSMALL));
        filterPanel.addClassNames(LumoUtility.Padding.Top.XSMALL, LumoUtility.Padding.Left.MEDIUM,
                LumoUtility.Padding.Bottom.XSMALL, LumoUtility.Padding.Right.MEDIUM,
                LumoUtility.Border.ALL, LumoUtility.BorderRadius.LARGE, LumoUtility.BorderColor.CONTRAST_20);
    }

    protected void initFilter() {
        ProcessDefinitionFilter filter = metadata.create(ProcessDefinitionFilter.class);
        filter.setLatestVersionOnly(true);
        processDefinitionFilterDc.setItem(filter);
    }

    protected Span createStateBadge(ProcessDefinitionData processDefinitionData) {
        return createStateBadge(BooleanUtils.isTrue(processDefinitionData.getSuspended()));
    }

    protected Span createStateBadge(boolean suspended) {
        Span badge = uiComponents.create(Span.class);
        String themeNames = suspended ? "badge warning pill" : "badge success pill";
        badge.getElement().getThemeList().add(themeNames);

        String messageKey = suspended ? "processDefinitionList.status.suspended" : "processDefinitionList.status.active";
        badge.setText(messageBundle.getMessage(messageKey));
        return badge;
    }

    @Supply(to = "processDefinitionsGrid.key", subject = "renderer")
    protected Renderer<ProcessDefinitionData> processDefinitionsGridKeyRenderer() {
        StreamResource iconResource = new StreamResource("preview.svg",
                () -> getClass().getResourceAsStream("/META-INF/resources/icons/preview.svg"));

        return new ComponentRenderer<>(processDefinitionData -> {
            HorizontalLayout layout = uiComponents.create(HorizontalLayout.class);
            layout.setAlignItems(FlexComponent.Alignment.BASELINE);

            Span key = uiComponents.create(Span.class);
            key.setText(processDefinitionData.getKey());
            key.setWidthFull();
            key.addClassNames(LumoUtility.Overflow.HIDDEN, LumoUtility.TextOverflow.ELLIPSIS);

            JmixButton previewBtn = createDiagramPreviewButton(processDefinitionData, iconResource);

            layout.addAndExpand(key);
            layout.add(previewBtn);

            return layout;
        });
    }

    protected JmixButton createDiagramPreviewButton(ProcessDefinitionData processDefinitionData, StreamResource iconResource) {
        SvgIcon previewIcon = uiComponents.create(SvgIcon.class);
        previewIcon.setSrc(iconResource);

        JmixButton previewBtn = uiComponents.create(JmixButton.class);
        previewBtn.setIcon(previewIcon);
        previewBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        previewBtn.addClickListener(clickEvent -> dialogWindows.view(this, ProcessDefinitionDiagramView.class)
                .withViewConfigurer(view -> view.setProcessDefinition(processDefinitionData))
                .build()
                .open());
        return previewBtn;
    }

    @Override
    protected void loadData() {
        processDefinitionsDl.load();
    }

    @Subscribe("processDefinitionsGrid.refresh")
    public void onProcessDefinitionsGridRefresh(final ActionPerformedEvent event) {
        startLoadData();
    }
}