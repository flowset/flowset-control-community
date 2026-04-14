/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.batch;

import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.event.SortEvent;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.entity.batch.BatchData;
import io.flowset.control.entity.batch.BatchStatisticsData;
import io.flowset.control.entity.filter.BatchFilter;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.facet.urlqueryparameters.ActiveBatchListQueryParamBinder;
import io.flowset.control.facet.urlqueryparameters.CompletedBatchListQueryParamBinder;
import io.flowset.control.service.batch.BatchLoadContext;
import io.flowset.control.service.batch.BatchService;
import io.flowset.control.view.AbstractListViewWithDelayedLoad;
import io.flowset.control.view.batch.filter.*;
import io.jmix.core.DataLoadContext;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.facet.Timer;
import io.jmix.flowui.facet.UrlQueryParametersFacet;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Collections;
import java.util.List;

@Route(value = "bpm/batches", layout = DefaultMainViewParent.class)
@ViewController("bpm_AllBatchListView")
@ViewDescriptor("all-batch-list-view.xml")
@LookupComponent("activeBatchesDataGrid")
@DialogMode(minWidth = "65em", width = "80%", resizable = true)
public class AllBatchListView extends AbstractListViewWithDelayedLoad<BatchStatisticsData> {

    public static final int ACTIVE_TAB_IDX = 0;
    public static final int COMPLETED_TAB_IDX = 1;

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected BatchService batchService;
    @ViewComponent
    protected UrlQueryParametersFacet urlQueryParameters;
    @Autowired
    protected ViewNavigators viewNavigators;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected ApplicationContext applicationContext;

    @ViewComponent
    protected CollectionLoader<BatchStatisticsData> activeBatchesDl;
    @ViewComponent
    protected InstanceContainer<BatchFilter> activeBatchFilterDc;
    @ViewComponent
    protected DataGrid<BatchStatisticsData> activeBatchesDataGrid;

    @ViewComponent
    protected CollectionLoader<BatchData> completedBatchesDl;
    @ViewComponent
    protected InstanceContainer<BatchFilter> completedBatchFilterDc;
    @ViewComponent
    protected DataGrid<BatchData> completedBatchesDataGrid;
    @ViewComponent
    protected VerticalLayout completedBatchGridEmptyStateBox;

    @ViewComponent
    protected JmixTabSheet tabsheet;

    @ViewComponent
    protected Timer completedBatchLoadTimer;

    @ViewComponent
    protected HorizontalLayout lookupActions;

    protected boolean completedTabFirstSelection = true;

    @Subscribe
    public void onInit(InitEvent event) {
        addClassNames(LumoUtility.Padding.Top.SMALL);

        tabsheet.getTabAt(ACTIVE_TAB_IDX).addComponentAsFirst(VaadinIcon.HOURGLASS.create());
        tabsheet.getTabAt(COMPLETED_TAB_IDX).addComponentAsFirst(VaadinIcon.CHECK_CIRCLE.create());

        initFilters();
        initDataGridHeaderRows();

        urlQueryParameters.registerBinder(new ActiveBatchListQueryParamBinder(activeBatchesDataGrid, tabsheet, this::startLoadData));
        urlQueryParameters.registerBinder(new CompletedBatchListQueryParamBinder(completedBatchesDataGrid, this::startLoadCompletedBatches));

        componentHelper.addNoDataGridStateComponents(completedBatchGridEmptyStateBox);

        setDefaultSort();
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        if (tabsheet.getSelectedIndex() == COMPLETED_TAB_IDX) { // can be set in the URL query param
            startLoadCompletedBatches();
            completedTabFirstSelection = false;
        }

        if (UiComponentUtils.isComponentAttachedToDialog(this)) {
            lookupActions.setVisible(true);
        }
    }


    @Subscribe("tabsheet")
    public void onTabsheetSelectedChange(final JmixTabSheet.SelectedChangeEvent event) {
        if (event.getSource().getSelectedIndex() == COMPLETED_TAB_IDX) {
            if (completedTabFirstSelection) { // load data only on the first tab selection
                startLoadCompletedBatches();
                completedTabFirstSelection = false;
            }
        }
    }

    protected void setDefaultSort() {
        activeBatchesDataGrid.sort(Collections.singletonList(new GridSortOrder<>(
                activeBatchesDataGrid.getColumnByKey("startTime"), SortDirection.DESCENDING)));

        completedBatchesDataGrid.sort(Collections.singletonList(new GridSortOrder<>(
                completedBatchesDataGrid.getColumnByKey("startTime"), SortDirection.DESCENDING)));
    }

    @Install(to = "activeBatchesDl", target = Target.DATA_LOADER)
    protected List<BatchStatisticsData> batchesDlLoadDelegate(LoadContext<BatchStatisticsData> loadContext) {
        LoadContext.Query query = loadContext.getQuery();
        BatchLoadContext context = new BatchLoadContext().setFilter(activeBatchFilterDc.getItem());

        if (query != null) {
            context.setFirstResult(query.getFirstResult())
                    .setMaxResults(query.getMaxResults())
                    .setSort(query.getSort());
        }

        return loadItemsWithStateHandling(() -> batchService.findAllBatchStatistics(context));
    }


    @Install(to = "batchesPagination", subject = "totalCountDelegate")
    protected Integer batchesPaginationTotalCountDelegate(DataLoadContext dataLoadContext) {
        return (int) batchService.getBatchStatisticsCount(activeBatchFilterDc.getItem());
    }

    @Subscribe("activeBatchesDataGrid.refresh")
    protected void onBatchesDataGridRefresh(ActionPerformedEvent event) {
        startLoadData();
    }

    @Subscribe("completedBatchesDataGrid.refresh")
    protected void onCompletedBatchesDataGridRefresh(ActionPerformedEvent event) {
        startLoadCompletedBatches();
    }

    @Subscribe("activeBatchesDataGrid")
    protected void onBatchesDataGridSort(
            SortEvent<DataGrid<BatchStatisticsData>, GridSortOrder<DataGrid<BatchStatisticsData>>> event) {
        startLoadData();
    }


    @Subscribe("completedBatchLoadTimer")
    public void onCompletedBatchLoadTimerTimerAction(final Timer.TimerActionEvent event) {
        loadCompletedBatches();
    }

    @Install(to = "completedBatchesDl", target = Target.DATA_LOADER)
    protected List<BatchData> completedBatchesDlLoadDelegate(final LoadContext<BatchData> loadContext) {
        BatchLoadContext context = new BatchLoadContext().setFilter(completedBatchFilterDc.getItem());

        LoadContext.Query query = loadContext.getQuery();
        if (query != null) {
            context.setFirstResult(query.getFirstResult())
                    .setMaxResults(query.getMaxResults())
                    .setSort(query.getSort());
        }
        try {
            return batchService.findAllHistoricBatches(context);
        } catch (EngineConnectionFailedException e) {
            setCompletedBatchErrorState(e.getMessage());
            return List.of();
        } finally {
            completedBatchGridEmptyStateBox.removeAll();
        }
    }


    @Install(to = "completedBatchesPagination", subject = "totalCountDelegate")
    protected Integer completedBatchesPaginationTotalCountDelegate(final DataLoadContext dataLoadContext) {
        return (int) batchService.getHistoricBatchCount(completedBatchFilterDc.getItemOrNull());
    }

    @Subscribe(id = "completedBatchesDl", target = Target.DATA_LOADER)
    public void onCompletedBatchesDlPostLoad(final CollectionLoader.PostLoadEvent<BatchData> event) {
        completedBatchGridEmptyStateBox.removeAll();
        List<BatchData> loadedEntities = event.getLoadedEntities();
        if (CollectionUtils.isEmpty(loadedEntities)) {
            setCompletedBatchNoDataState();
        }
    }

    protected void setCompletedBatchErrorState(String message) {
        completedBatchGridEmptyStateBox.removeAll();
        componentHelper.addErrorStateGridStateComponents(completedBatchGridEmptyStateBox, message);
    }

    protected void setCompletedBatchLoadingState() {
        completedBatchGridEmptyStateBox.removeAll();
        componentHelper.addLoadingGridStateComponents(completedBatchGridEmptyStateBox);
    }

    protected void setCompletedBatchNoDataState() {
        completedBatchGridEmptyStateBox.removeAll();
        componentHelper.addNoDataGridStateComponents(completedBatchGridEmptyStateBox);
    }

    protected void startLoadCompletedBatches() {
        completedBatchLoadTimer.stop();

        setCompletedBatchLoadingState();

        completedBatchLoadTimer.start();
    }

    protected void loadCompletedBatches() {
        completedBatchesDl.load();
    }

    @Override
    protected void loadData() {
        activeBatchesDl.load();
    }

    protected void initFilters() {
        BatchFilter activeFilter = metadata.create(BatchFilter.class);
        activeBatchFilterDc.setItem(activeFilter);

        BatchFilter completedFilter = metadata.create(BatchFilter.class);
        completedFilter.setCompleted(true);
        completedBatchFilterDc.setItem(completedFilter);
    }

    protected void initDataGridHeaderRows() {
        componentHelper.addColumnFilter(activeBatchesDataGrid, "id", this::createIdColumnFilter);
        componentHelper.addColumnFilter(activeBatchesDataGrid, "type", this::createTypeColumnFilter);
        componentHelper.addColumnFilter(activeBatchesDataGrid, "startTime", this::createStartTimeColumnFilter);
        componentHelper.addColumnFilter(activeBatchesDataGrid, "state", this::createStateColumnFilter);
        componentHelper.addColumnFilter(activeBatchesDataGrid, "failedJobs", this::createExecutionColumnFilter);

        componentHelper.addColumnFilter(completedBatchesDataGrid, "id", this::createCompletedIdColumnFilter);
        componentHelper.addColumnFilter(completedBatchesDataGrid, "type", this::createCompletedTypeColumnFilter);
    }

    protected BatchHeaderFilter createTypeColumnFilter(DataGridColumn<BatchStatisticsData> column) {
        return new BatchTypeHeaderFilter(activeBatchesDataGrid, column, activeBatchFilterDc);
    }

    protected BatchHeaderFilter createIdColumnFilter(DataGridColumn<BatchStatisticsData> column) {
        return new BatchIdHeaderFilter(activeBatchesDataGrid, column, activeBatchFilterDc);
    }

    protected BatchHeaderFilter createStartTimeColumnFilter(DataGridColumn<BatchStatisticsData> column) {
        return new BatchStartTimeHeaderFilter(activeBatchesDataGrid, column, activeBatchFilterDc);
    }

    protected BatchHeaderFilter createStateColumnFilter(DataGridColumn<BatchStatisticsData> column) {
        return new BatchStateHeaderFilter(activeBatchesDataGrid, column, activeBatchFilterDc);
    }

    protected BatchHeaderFilter createExecutionColumnFilter(DataGridColumn<BatchStatisticsData> column) {
        return new BatchExecutionHeaderFilter(activeBatchesDataGrid, column, activeBatchFilterDc);
    }

    protected CompletedBatchHeaderFilter createCompletedTypeColumnFilter(DataGridColumn<BatchData> column) {
        return new CompletedBatchTypeHeaderFilter(completedBatchesDataGrid, column, completedBatchFilterDc);
    }

    protected CompletedBatchHeaderFilter createCompletedIdColumnFilter(DataGridColumn<BatchData> column) {
        return new CompletedBatchIdHeaderFilter(completedBatchesDataGrid, column, completedBatchFilterDc);
    }
}
