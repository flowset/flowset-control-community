/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.decisioninstance;

import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.entity.decisioninstance.HistoricDecisionInstanceShortData;
import io.flowset.control.entity.filter.DecisionInstanceFilter;
import io.flowset.control.entity.filter.ProcessDefinitionFilter;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.facet.urlqueryparameters.DecisionInstanceListQueryParamBinder;
import io.flowset.control.service.decisiondefinition.DecisionDefinitionService;
import io.flowset.control.service.decisioninstance.DecisionInstanceLoadContext;
import io.flowset.control.service.decisioninstance.DecisionInstanceService;
import io.flowset.control.service.processdefinition.ProcessDefinitionLoadContext;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import io.flowset.control.uicomponent.ContainerDataGridHeaderFilter;
import io.flowset.control.view.AbstractListViewWithDelayedLoad;
import io.flowset.control.view.decisioninstance.column.DecisionDefinitionColumnFragment;
import io.flowset.control.view.decisioninstance.column.DecisionInstanceProcessColumnFragment;
import io.flowset.control.view.decisioninstance.filter.*;
import io.jmix.core.DataLoadContext;
import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.facet.UrlQueryParametersFacet;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.sys.BeanUtil;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Route(value = "decision-instances", layout = DefaultMainViewParent.class)
@ViewController(id = "DecisionInstanceData.list")
@ViewDescriptor(path = "decision-instance-data-list-view.xml")
@LookupComponent("decisionInstancesDataGrid")
@DialogMode(width = "50em")
public class DecisionInstanceDataListView extends AbstractListViewWithDelayedLoad<HistoricDecisionInstanceShortData> {
    @Autowired
    protected ProcessDefinitionService processDefinitionService;
    @Autowired
    protected DecisionInstanceService decisionInstanceService;
    @Autowired
    protected DecisionDefinitionService decisionDefinitionService;
    @Autowired
    protected Fragments fragments;
    @Autowired
    protected DataManager dataManager;
    @ViewComponent
    protected UrlQueryParametersFacet urlQueryParameters;
    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected UiComponents uiComponents;

    @ViewComponent
    protected CollectionLoader<HistoricDecisionInstanceShortData> decisionInstancesDl;
    @ViewComponent
    protected InstanceContainer<DecisionInstanceFilter> decisionFilterDc;

    @ViewComponent
    protected DataGrid<HistoricDecisionInstanceShortData> decisionInstancesDataGrid;

    protected Map<String, ProcessDefinitionData> processDefinitionsMap = new HashMap<>();
    protected Map<String, DecisionDefinitionData> decisionDefinitionsMap = new HashMap<>();


    @Subscribe
    public void onInit(final InitEvent event) {
        initFilter();
        setDefaultSort();
        initDataGridHeaderRow();
        urlQueryParameters.registerBinder(new DecisionInstanceListQueryParamBinder(decisionInstancesDataGrid, this::startLoadData));
    }

    @Supply(to = "decisionInstancesDataGrid.decisionDefinitionId", subject = "renderer")
    protected Renderer<HistoricDecisionInstanceShortData> decisionInstancesDataGridDecisionDefinitionIdRenderer() {
        return new ComponentRenderer<>(decisionInstance -> {
            DecisionDefinitionColumnFragment decisionDefinitionFragment = fragments.create(this, DecisionDefinitionColumnFragment.class);
            decisionDefinitionFragment.setDecisionDefinitionData(decisionDefinitionsMap.get(decisionInstance.getDecisionDefinitionId()));
            decisionDefinitionFragment.setItem(decisionInstance);
            return decisionDefinitionFragment;
        });
    }


    @Supply(to = "decisionInstancesDataGrid.processDefinitionId", subject = "renderer")
    protected Renderer<HistoricDecisionInstanceShortData> decisionInstancesDataGridProcessDefinitionIdRenderer() {
        return new ComponentRenderer<>(decisionInstance -> {
            DecisionInstanceProcessColumnFragment processColumnFragment = fragments.create(this, DecisionInstanceProcessColumnFragment.class);
            processColumnFragment.setProcessDefinitionData(processDefinitionsMap.get(decisionInstance.getProcessDefinitionId()));
            processColumnFragment.setItem(decisionInstance);
            return processColumnFragment;
        });
    }

    @Subscribe("decisionInstancesDataGrid.refreshAction")
    public void onDecisionInstancesDataGridRefreshAction(final ActionPerformedEvent event) {
        startLoadData();
    }

    @Install(to = "decisionInstancesDl", target = Target.DATA_LOADER)
    protected List<HistoricDecisionInstanceShortData> decisionInstancesDlLoadDelegate(LoadContext<HistoricDecisionInstanceShortData> loadContext) {
        DecisionInstanceLoadContext context = new DecisionInstanceLoadContext().setFilter(decisionFilterDc.getItem());

        LoadContext.Query query = loadContext.getQuery();
        if (query != null) {
            context.setFirstResult(query.getFirstResult())
                    .setMaxResults(query.getMaxResults())
                    .setSort(query.getSort());
        }

        return loadItemsWithStateHandling(() -> {
            List<HistoricDecisionInstanceShortData> decisionInstances = decisionInstanceService.findAllHistoryDecisionInstances(context);
            loadProcessDefinitions(decisionInstances);
            loadDecisionDefinitions(decisionInstances);

            return decisionInstances;
        });
    }

    @Override
    protected void loadData() {
        decisionInstancesDl.load();
    }

    protected void loadDecisionDefinitions(List<HistoricDecisionInstanceShortData> decisionInstances) {
        Set<String> decisionDefinitionIds = decisionInstances.stream()
                .filter(instance -> instance.getDecisionDefinitionId() != null &&
                        !decisionDefinitionsMap.containsKey(instance.getDecisionDefinitionId()))
                .map(HistoricDecisionInstanceShortData::getDecisionDefinitionId)
                .collect(Collectors.toSet());

        List<DecisionDefinitionData> decisionDefinitions = decisionDefinitionService.findAllByIds(decisionDefinitionIds);
        decisionDefinitions.forEach(decisionDefinitionData -> decisionDefinitionsMap.put(decisionDefinitionData.getDecisionDefinitionId(), decisionDefinitionData));
    }

    protected void loadProcessDefinitions(List<HistoricDecisionInstanceShortData> decisionInstances) {
        List<String> processDefinitionIds = decisionInstances.stream()
                .filter(instance -> instance.getProcessDefinitionId() != null &&
                        !decisionDefinitionsMap.containsKey(instance.getProcessDefinitionId()))
                .map(HistoricDecisionInstanceShortData::getProcessDefinitionId)
                .distinct()
                .toList();

        ProcessDefinitionFilter processDefinitionFilter = dataManager.create(ProcessDefinitionFilter.class);
        processDefinitionFilter.setLatestVersionOnly(false);
        processDefinitionFilter.setIdIn(processDefinitionIds);

        List<ProcessDefinitionData> processDefinitions = processDefinitionService.findAll(new ProcessDefinitionLoadContext()
                .setFilter(processDefinitionFilter));

        processDefinitions.forEach(processDefinitionData -> processDefinitionsMap.put(processDefinitionData.getProcessDefinitionId(), processDefinitionData));
    }

    protected void initDataGridHeaderRow() {
        HeaderRow headerRow = decisionInstancesDataGrid.getDefaultHeaderRow();

        addColumnFilter(headerRow, "decisionInstanceId", this::createIdColumnFilter);
        addColumnFilter(headerRow, "evaluationTime", this::createEvaluateTimeColumnFilter);
        addColumnFilter(headerRow, "decisionDefinitionId", this::createDecisionColumnFilter);
        addColumnFilter(headerRow, "processInstanceId", this::createProcessInstanceColumnFilter);
        addColumnFilter(headerRow, "processDefinitionId", this::createProcessColumnFilter);
        addColumnFilter(headerRow, "activityId", this::createActivityColumnFilter);
    }

    protected DecisionInstanceIdHeaderFilter createIdColumnFilter(DataGridColumn<HistoricDecisionInstanceShortData> idColumn) {
        return new DecisionInstanceIdHeaderFilter(decisionInstancesDataGrid, idColumn, decisionFilterDc, this::startLoadData);
    }

    protected EvaluationTimeHeaderFilter createEvaluateTimeColumnFilter(DataGridColumn<HistoricDecisionInstanceShortData> evaluateTimeColumn) {
        return new EvaluationTimeHeaderFilter(decisionInstancesDataGrid, evaluateTimeColumn, decisionFilterDc, this::startLoadData);
    }

    protected DecisionHeaderFilter createDecisionColumnFilter(DataGridColumn<HistoricDecisionInstanceShortData> decisionColumn) {
        return new DecisionHeaderFilter(decisionInstancesDataGrid, decisionColumn, decisionFilterDc, this::startLoadData);
    }

    protected ProcessInstanceIdHeaderFilter createProcessInstanceColumnFilter(DataGridColumn<HistoricDecisionInstanceShortData> processInstanceColumn) {
        return new ProcessInstanceIdHeaderFilter(decisionInstancesDataGrid, processInstanceColumn, decisionFilterDc, this::startLoadData);
    }

    protected ProcessHeaderFilter createProcessColumnFilter(DataGridColumn<HistoricDecisionInstanceShortData> processColumn) {
        return new ProcessHeaderFilter(decisionInstancesDataGrid, processColumn, decisionFilterDc, this::startLoadData);
    }

    protected ActivityIdHeaderFilter createActivityColumnFilter(DataGridColumn<HistoricDecisionInstanceShortData> activityIdColumn) {
        return new ActivityIdHeaderFilter(decisionInstancesDataGrid, activityIdColumn, decisionFilterDc, this::startLoadData);
    }

    protected <T extends ContainerDataGridHeaderFilter<DecisionInstanceFilter, HistoricDecisionInstanceShortData>> void addColumnFilter(HeaderRow headerRow, String columnName,
                                                                                                                                        Function<DataGridColumn<HistoricDecisionInstanceShortData>, T> filterProvider) {
        DataGridColumn<HistoricDecisionInstanceShortData> column = decisionInstancesDataGrid.getColumnByKey(columnName);
        T filterComponent = filterProvider.apply(column);
        BeanUtil.autowireContext(applicationContext, filterComponent);
        HeaderRow.HeaderCell headerCell = headerRow.getCell(column);
        HorizontalLayout layout = uiComponents.create(HorizontalLayout.class);
        layout.setSizeFull();
        layout.addClassNames(LumoUtility.Gap.SMALL);
        headerCell.setComponent(filterComponent);

        Element child = filterComponent.getElement().getChild(0);
        if (child != null && child.getStyle() != null) {
            //set styles for column header text to make a filter button always visible
            child.getStyle().setOverflow(Style.Overflow.HIDDEN);
            child.getStyle().set("text-overflow", "ellipsis");
            child.getStyle().setWhiteSpace(Style.WhiteSpace.PRE_WRAP);
        }
    }

    protected void setDefaultSort() {
        List<GridSortOrder<HistoricDecisionInstanceShortData>> gridSortOrders = Collections.singletonList(new GridSortOrder<>(
                decisionInstancesDataGrid.getColumnByKey("evaluationTime"), SortDirection.DESCENDING));
        decisionInstancesDataGrid.sort(gridSortOrders);
    }

    protected void initFilter() {
        DecisionInstanceFilter decisionInstanceFilter = dataManager.create(DecisionInstanceFilter.class);
        decisionFilterDc.setItem(decisionInstanceFilter);
    }

    @Install(to = "pagination", subject = "totalCountDelegate")
    protected Integer paginationTotalCountDelegate(final DataLoadContext dataLoadContext) {
        return (int) decisionInstanceService.getHistoryDecisionInstancesCount(decisionFilterDc.getItem());
    }
}
