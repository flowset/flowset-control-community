/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processinstance;

import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.event.SortEvent;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.uicomponent.ContainerDataGridHeaderFilter;
import io.flowset.control.view.AbstractListViewWithDelayedLoad;
import io.jmix.core.DataLoadContext;
import io.jmix.core.LoadContext;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.flowui.*;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.facet.UrlQueryParametersFacet;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.sys.BeanUtil;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.filter.ProcessInstanceFilter;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.facet.urlqueryparameters.ProcessInstanceListQueryParamBinder;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import io.flowset.control.service.processinstance.ProcessInstanceLoadContext;
import io.flowset.control.service.processinstance.ProcessInstanceService;
import io.flowset.control.view.processinstance.filter.*;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Route(value = "bpm/process-instances", layout = DefaultMainViewParent.class)
@ViewController("bpm_ProcessInstance.list")
@ViewDescriptor("process-instance-list-view.xml")
@LookupComponent("processInstancesGrid")
@DialogMode(width = "50em", height = "38.5em")
public class ProcessInstanceListView extends AbstractListViewWithDelayedLoad<ProcessInstanceData> {

    @ViewComponent
    protected MessageBundle messageBundle;
    @Autowired
    protected ViewNavigators viewNavigators;
    @Autowired
    protected Fragments fragments;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Messages messages;

    @Autowired
    protected ProcessInstanceService processInstanceService;
    @Autowired
    protected ProcessDefinitionService processDefinitionService;

    @ViewComponent
    protected CollectionContainer<ProcessInstanceData> processInstancesDc;

    @ViewComponent
    protected DataGrid<ProcessInstanceData> processInstancesGrid;


    @ViewComponent
    protected InstanceContainer<ProcessInstanceFilter> processInstanceFilterDc;
    @ViewComponent
    protected CollectionLoader<ProcessInstanceData> processInstancesDl;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected DialogWindows dialogWindows;
    @ViewComponent
    protected UrlQueryParametersFacet urlQueryParameters;
    @ViewComponent
    protected HorizontalLayout modeButtonsGroup;

    @Subscribe
    public void onInit(InitEvent event) {
        addClassNames(LumoUtility.Padding.Top.SMALL);
        initFilter();
        initDataGridHeaderRow();
        setDefaultSort();
        urlQueryParameters.registerBinder(new ProcessInstanceListQueryParamBinder(modeButtonsGroup, processInstanceFilterDc,
                this::startLoadData, processInstancesGrid));
    }

    protected void setDefaultSort() {
        List<GridSortOrder<ProcessInstanceData>> gridSortOrders = Collections.singletonList(new GridSortOrder<>(processInstancesGrid.getColumnByKey("startTime"), SortDirection.DESCENDING));
        processInstancesGrid.sort(gridSortOrders);
    }

    @Install(to = "processInstancesGrid.bulkActivate", subject = "enabledRule")
    protected boolean processInstancesGridBulkActivateEnabledRule() {
        boolean selectedNotEmpty = !processInstancesGrid.getSelectedItems().isEmpty();
        boolean suspendedInstanceSelected = processInstancesGrid.getSelectedItems().stream().anyMatch(processInstanceData ->
                BooleanUtils.isTrue(processInstanceData.getSuspended()) && BooleanUtils.isNotTrue(processInstanceData.getComplete()));
        boolean notCompletedSelected = processInstancesGrid.getSelectedItems().stream().noneMatch(processInstanceData -> BooleanUtils.isTrue(processInstanceData.getComplete()));

        return selectedNotEmpty && suspendedInstanceSelected && notCompletedSelected;
    }

    @Install(to = "processInstancesGrid.bulkTerminate", subject = "enabledRule")
    protected boolean processInstancesGridBulkTerminateEnabledRule() {
        boolean selectedNotEmpty = !processInstancesGrid.getSelectedItems().isEmpty();
        boolean notCompletedSelected = processInstancesGrid.getSelectedItems().stream().noneMatch(processInstanceData -> BooleanUtils.isTrue(processInstanceData.getFinished()));

        return selectedNotEmpty && notCompletedSelected;
    }

    @Install(to = "processInstancesGrid.bulkSuspend", subject = "enabledRule")
    protected boolean processInstancesGridBulkSuspendEnabledRule() {
        boolean selectedNotEmpty = !processInstancesGrid.getSelectedItems().isEmpty();
        boolean activeInstanceSelected = processInstancesGrid.getSelectedItems().stream().anyMatch(processInstanceData ->
                BooleanUtils.isNotTrue(processInstanceData.getSuspended()) && BooleanUtils.isNotTrue(processInstanceData.getFinished())
        );

        boolean notCompletedSelected = processInstancesGrid.getSelectedItems().stream().noneMatch(processInstanceData -> BooleanUtils.isTrue(processInstanceData.getFinished()));
        return selectedNotEmpty && activeInstanceSelected && notCompletedSelected;
    }


    protected void initDataGridHeaderRow() {
        HeaderRow headerRow = processInstancesGrid.getDefaultHeaderRow();

        addColumnFilter(headerRow, "id", this::createIdColumnFilter);
        addColumnFilter(headerRow, "processDefinitionId", this::createProcessColumnFilter);
        addColumnFilter(headerRow, "businessKey", this::createBusinessKeyColumnFilter);
        addColumnFilter(headerRow, "state", this::createStateColumnFilter);
        addColumnFilter(headerRow, "startTime", this::createStartTimeColumnFilter);
        addColumnFilter(headerRow, "endTime", this::createEndTimeColumnFilter);
    }


    @Subscribe("processInstancesGrid.bulkTerminate")
    public void onProcessInstancesGridBulkTerminate(final ActionPerformedEvent event) {
        dialogWindows.view(this, BulkTerminateProcessInstanceView.class)
                .withViewConfigurer(view -> view.setProcessInstances(processInstancesGrid.getSelectedItems()))
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                        startLoadData();
                    }
                })
                .build()
                .open();
    }


    @Subscribe("processInstancesGrid.bulkActivate")
    public void onProcessInstancesGridBulkActivate(final ActionPerformedEvent event) {
        List<String> instancesIds = processInstancesGrid.getSelectedItems().stream().map(ProcessInstanceData::getInstanceId).toList();

        DialogWindow<BulkActivateProcessInstanceView> dialogWindow = dialogWindows.view(this, BulkActivateProcessInstanceView.class)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                        startLoadData();
                    }
                })
                .build();

        BulkActivateProcessInstanceView bulkActivateProcessInstanceView = dialogWindow.getView();
        bulkActivateProcessInstanceView.setInstancesIds(instancesIds);

        dialogWindow.open();
    }

    @Subscribe("processInstancesGrid.bulkSuspend")
    public void onProcessInstancesGridBulkSuspend(final ActionPerformedEvent event) {
        List<String> instancesIds = processInstancesGrid.getSelectedItems().stream().map(ProcessInstanceData::getInstanceId).toList();

        DialogWindow<BulkSuspendProcessInstanceView> dialogWindow = dialogWindows.view(this, BulkSuspendProcessInstanceView.class)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                        startLoadData();
                    }
                })
                .build();

        BulkSuspendProcessInstanceView bulkSuspendProcessInstanceView = dialogWindow.getView();
        bulkSuspendProcessInstanceView.setInstancesIds(instancesIds);

        dialogWindow.open();
    }


    @Install(to = "processInstancePagination", subject = "totalCountDelegate")
    protected Integer processInstancePaginationTotalCountDelegate(final DataLoadContext dataLoadContext) {
        return (int) processInstanceService.getHistoricInstancesCount(processInstanceFilterDc.getItemOrNull());
    }

    @Install(to = "processInstancesDl", target = Target.DATA_LOADER)
    protected List<ProcessInstanceData> processInstancesDlLoadDelegate(final LoadContext<ProcessInstanceData> loadContext) {
        LoadContext.Query query = loadContext.getQuery();
        ProcessInstanceFilter filter = processInstanceFilterDc.getItemOrNull();

        ProcessInstanceLoadContext context = new ProcessInstanceLoadContext().setFilter(filter)
                .setLoadIncidents(true);

        if (query != null) {
            context.setFirstResult(query.getFirstResult())
                    .setMaxResults(query.getMaxResults())
                    .setSort(query.getSort());
        }

        return loadItemsWithStateHandling(() -> processInstanceService.findAllHistoricInstances(context));
    }

    @Subscribe("processInstancesGrid")
    public void onProcessInstancesGridSort(final SortEvent<DataGrid<ProcessInstanceData>, GridSortOrder<DataGrid<ProcessInstanceData>>> event) {
        startLoadData();
    }

    @Subscribe("processInstancesGrid.view")
    public void onProcessInstancesGridEdit(ActionPerformedEvent event) {
        ProcessInstanceData selectedInstance = processInstancesGrid.getSingleSelectedItem();
        if (selectedInstance == null) {
            return;
        }
        viewNavigators.detailView(this, ProcessInstanceData.class)
                .withBackwardNavigation(true)
                .withRouteParameters(new RouteParameters("id", selectedInstance.getId()))
                .navigate();
    }

    @Supply(to = "processInstancesGrid.processDefinitionId", subject = "renderer")
    protected Renderer<ProcessInstanceData> processInstancesGridProcessRenderer() {
        return new TextRenderer<>(this::getProcessDisplayName);
    }

    @Install(to = "processInstancesGrid.processDefinitionId", subject = "tooltipGenerator")
    protected String processInstancesGridProcessTooltipGenerator(final ProcessInstanceData processInstanceData) {
        return getProcessDisplayName(processInstanceData);
    }

    protected String getProcessDisplayName(ProcessInstanceData item) {
        return item.getProcessDefinitionVersion() == null ? item.getProcessDefinitionId() :
                componentHelper.getProcessLabel(item.getProcessDefinitionKey(), item.getProcessDefinitionVersion());
    }

    @Supply(to = "processInstancesGrid.actions", subject = "renderer")
    protected Renderer<ProcessInstanceData> processInstancesGridActionsRenderer() {
        return new ComponentRenderer<>(processInstance -> {
            ProcessInstanceListItemActionsFragment fragment = fragments.create(this, ProcessInstanceListItemActionsFragment.class);
            fragment.setProcessInstance(processInstance);

            return fragment;
        });
    }

    @Install(to = "processInstancesGrid.startTime", subject = "partNameGenerator")
    protected String processInstancesGridStartTimePartNameGenerator(final ProcessInstanceData processInstanceData) {
        return "multiline-text-cell";
    }

    @Install(to = "processInstancesGrid.endTime", subject = "partNameGenerator")
    protected String processInstancesGridEndTimePartNameGenerator(final ProcessInstanceData processInstanceData) {
        return "multiline-text-cell";
    }

    protected void initFilter() {
        ProcessInstanceFilter processInstanceFilter = metadata.create(ProcessInstanceFilter.class);
        processInstanceFilter.setUnfinished(true);
        processInstanceFilterDc.setItem(processInstanceFilter);
    }

    @SuppressWarnings("JmixIncorrectCreateGuiComponent")
    protected BusinessKeyHeaderFilter createBusinessKeyColumnFilter(DataGridColumn<ProcessInstanceData> businessKeyColumn) {
        return new BusinessKeyHeaderFilter(processInstancesGrid, businessKeyColumn, processInstanceFilterDc);
    }

    @SuppressWarnings("JmixIncorrectCreateGuiComponent")
    protected EndTimeHeaderFilter createEndTimeColumnFilter(DataGridColumn<ProcessInstanceData> endTimeColumn) {
        return new EndTimeHeaderFilter(processInstancesGrid, endTimeColumn, processInstanceFilterDc);
    }

    @SuppressWarnings("JmixIncorrectCreateGuiComponent")
    protected StartTimeHeaderFilter createStartTimeColumnFilter(DataGridColumn<ProcessInstanceData> startTimeColumn) {
        return new StartTimeHeaderFilter(processInstancesGrid, startTimeColumn, processInstanceFilterDc);
    }

    @SuppressWarnings("JmixIncorrectCreateGuiComponent")
    protected ProcessHeaderFilter createProcessColumnFilter(DataGridColumn<ProcessInstanceData> processColumn) {
        return new ProcessHeaderFilter(processInstancesGrid, processColumn, processInstanceFilterDc);
    }

    @SuppressWarnings("JmixIncorrectCreateGuiComponent")
    protected IdHeaderFilter createIdColumnFilter(DataGridColumn<ProcessInstanceData> idColumn) {
        return new IdHeaderFilter(processInstancesGrid, idColumn, processInstanceFilterDc);
    }

    @SuppressWarnings("JmixIncorrectCreateGuiComponent")
    protected ProcessInstanceStateHeaderFilter createStateColumnFilter(DataGridColumn<ProcessInstanceData> stateColumn) {
        return new ProcessInstanceStateHeaderFilter(processInstancesGrid, stateColumn, processInstanceFilterDc);
    }

    protected <T extends ContainerDataGridHeaderFilter<ProcessInstanceFilter, ProcessInstanceData>> void addColumnFilter(HeaderRow headerRow, String columnName, Function<DataGridColumn<ProcessInstanceData>, T> filterProvider) {
        DataGridColumn<ProcessInstanceData> column = processInstancesGrid.getColumnByKey(columnName);
        T filterComponent = filterProvider.apply(column);
        BeanUtil.autowireContext(applicationContext, filterComponent);
        HeaderRow.HeaderCell headerCell = headerRow.getCell(column);
        headerCell.setComponent(filterComponent);
    }

    @Override
    protected void loadData() {
        processInstancesDl.load();
    }

    @Subscribe("processInstancesGrid.refresh")
    public void onProcessInstancesGridRefresh(final ActionPerformedEvent event) {
        startLoadData();
    }
}