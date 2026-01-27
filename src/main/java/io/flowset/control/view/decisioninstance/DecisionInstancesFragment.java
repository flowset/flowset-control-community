/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.decisioninstance;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.core.DataLoadContext;
import io.jmix.core.LoadContext;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.component.pagination.SimplePagination;
import io.jmix.flowui.data.pagination.PaginationDataLoader;
import io.jmix.flowui.data.pagination.PaginationDataLoaderImpl;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.BaseCollectionLoader;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.HasLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.sys.BeanUtil;
import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Supply;
import io.jmix.flowui.view.Target;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewComponent;
import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.entity.decisioninstance.HistoricDecisionInstanceShortData;
import io.flowset.control.entity.filter.DecisionInstanceFilter;
import io.flowset.control.service.decisioninstance.DecisionInstanceLoadContext;
import io.flowset.control.service.decisioninstance.DecisionInstanceService;
import io.flowset.control.uicomponent.ContainerDataGridHeaderFilter;
import io.flowset.control.view.decisioninstance.filter.ActivityIdHeaderFilter;
import io.flowset.control.view.decisioninstance.filter.EvaluationTimeHeaderFilter;
import io.flowset.control.view.decisioninstance.filter.ProcessInstanceIdHeaderFilter;
import io.flowset.control.view.decisioninstance.filter.ProcessDefinitionKeyHeaderFilter;
import io.flowset.control.view.util.ComponentHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.function.Function;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@FragmentDescriptor("decision-instances-fragment.xml")
public class DecisionInstancesFragment extends Fragment<VerticalLayout> {

    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected Messages messages;
    @Autowired
    protected ViewNavigators viewNavigators;
    @Autowired
    protected DecisionInstanceService decisionInstanceService;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected ComponentHelper componentHelper;

    @ViewComponent
    protected InstanceContainer<DecisionDefinitionData> decisionDefinitionDc;
    @ViewComponent
    protected CollectionContainer<HistoricDecisionInstanceShortData> decisionInstancesDc;
    @ViewComponent
    protected MessageBundle messageBundle;
    @ViewComponent
    protected VerticalLayout decisionInstanceVBox;
    @ViewComponent
    protected Span currentVersionsInstancesCountSpan;
    @ViewComponent
    protected Span allVersionsInstancesCountSpan;
    @ViewComponent
    protected DataGrid<HistoricDecisionInstanceShortData> decisionInstancesGrid;
    @ViewComponent
    protected SimplePagination decisionInstancesPagination;
    @ViewComponent
    protected CollectionLoader<HistoricDecisionInstanceShortData> decisionInstancesDl;
    @ViewComponent
    protected InstanceContainer<DecisionInstanceFilter> decisionInstanceFilterDc;

    @Subscribe(target = Target.HOST_CONTROLLER)
    public void onHostInit(final View.InitEvent event) {
        addClassNames(LumoUtility.Padding.NONE);

        initFilter();
        initDataGridHeaderRow();
    }

    @Subscribe
    public void onReady(ReadyEvent event) {
        initDecisionInstanceGroupStyles();
        if (decisionInstancesDc instanceof HasLoader container
                && container.getLoader() instanceof BaseCollectionLoader) {
            PaginationDataLoader paginationLoader =
                    applicationContext.getBean(PaginationDataLoaderImpl.class, container.getLoader());
            decisionInstancesPagination.setPaginationLoader(paginationLoader);
        }
    }

    @Subscribe(target = Target.HOST_CONTROLLER)
    public void onHostBeforeShow(View.BeforeShowEvent event) {
        initInstancesCountLabels();
    }

    public void initInstancesCountLabels() {
        DecisionDefinitionData item = decisionDefinitionDc.getItem();
        long currentVersionInstancesCount = decisionInstanceService.getCountByDecisionDefinitionId(
                item.getDecisionDefinitionId());
        long allVersionsInstancesCount = decisionInstanceService.getCountByDecisionDefinitionKey(item.getKey());
        currentVersionsInstancesCountSpan.setText(": " + currentVersionInstancesCount);
        allVersionsInstancesCountSpan.setText(": " + allVersionsInstancesCount);
    }

    @Subscribe(id = "decisionInstanceFilterDc", target = Target.DATA_CONTAINER)
    public void onDecisionInstanceFilterDcItemPropertyChange(
            final InstanceContainer.ItemPropertyChangeEvent<DecisionInstanceFilter> event) {
        decisionInstancesDl.load();
    }

    @Subscribe("decisionInstancesGrid.edit")
    public void onDecisionDefinitionsGridViewDetails(ActionPerformedEvent event) {
        HistoricDecisionInstanceShortData selectedItem = decisionInstancesGrid.getSingleSelectedItem();
        if (selectedItem == null) {
            return;
        }
        viewNavigators.detailView(getCurrentView(), HistoricDecisionInstanceShortData.class)
                .withRouteParameters(new RouteParameters("id", selectedItem.getDecisionInstanceId()))
                .withBackwardNavigation(true)
                .navigate();
    }

    @Subscribe(id = "decisionDefinitionDc", target = Target.DATA_CONTAINER)
    public void onDecisionDefinitionDcItemChange(final InstanceContainer.ItemChangeEvent<DecisionDefinitionData> event) {
        decisionInstancesDl.load();
    }

    @Install(to = "decisionInstancesDl", target = Target.DATA_LOADER)
    protected List<HistoricDecisionInstanceShortData> decisionInstancesLoadDelegate(
            LoadContext<HistoricDecisionInstanceShortData> loadContext) {
        LoadContext.Query query = loadContext.getQuery();
        DecisionInstanceFilter filter = decisionInstanceFilterDc.getItemOrNull();
        if (filter != null) {
            filter.setDecisionDefinitionId(decisionDefinitionDc.getItem().getDecisionDefinitionId());
        }
        DecisionInstanceLoadContext context = new DecisionInstanceLoadContext().setFilter(filter);
        if (query != null) {
            context = context.setFirstResult(query.getFirstResult())
                    .setMaxResults(query.getMaxResults())
                    .setSort(query.getSort());
        }
        return decisionInstanceService.findAllHistoryDecisionInstances(context);
    }

    @Install(to = "decisionInstancesPagination", subject = "totalCountDelegate")
    protected Integer decisionInstancesPaginationTotalCountDelegate(final DataLoadContext dataLoadContext) {
        DecisionDefinitionData decisionDefinition = decisionDefinitionDc.getItem();
        return (int) decisionInstanceService.getCountByDecisionDefinitionId(
                decisionDefinition.getDecisionDefinitionId());
    }

    protected void initDataGridHeaderRow() {
        HeaderRow headerRow = decisionInstancesGrid.getDefaultHeaderRow();

        addColumnFilter(headerRow, "evaluationTime", this::createEvaluationTimeColumnFilter);
        addColumnFilter(headerRow, "processDefinitionKey", this::createProcessDefinitionKeyColumnFilter);
        addColumnFilter(headerRow, "processInstanceId", this::createProcessInstanceIdColumnFilter);
        addColumnFilter(headerRow, "activityId", this::createActivityIdColumnFilter);
    }

    protected EvaluationTimeHeaderFilter createEvaluationTimeColumnFilter(
            DataGridColumn<HistoricDecisionInstanceShortData> column) {
        return new EvaluationTimeHeaderFilter(decisionInstancesGrid, column, decisionInstanceFilterDc);
    }

    protected ProcessDefinitionKeyHeaderFilter createProcessDefinitionKeyColumnFilter(
            DataGridColumn<HistoricDecisionInstanceShortData> column) {
        return new ProcessDefinitionKeyHeaderFilter(decisionInstancesGrid, column, decisionInstanceFilterDc);
    }

    protected ProcessInstanceIdHeaderFilter createProcessInstanceIdColumnFilter(
            DataGridColumn<HistoricDecisionInstanceShortData> column) {
        return new ProcessInstanceIdHeaderFilter(decisionInstancesGrid, column, decisionInstanceFilterDc);
    }

    protected ActivityIdHeaderFilter createActivityIdColumnFilter(
            DataGridColumn<HistoricDecisionInstanceShortData> column) {
        return new ActivityIdHeaderFilter(decisionInstancesGrid, column, decisionInstanceFilterDc);
    }

    @Supply(to = "decisionInstancesGrid.evaluationTime", subject = "renderer")
    protected Renderer<HistoricDecisionInstanceShortData> decisionInstancesGridEvaluationTimeRenderer() {
        return new ComponentRenderer<>(instance -> {
            Component dateSpan = componentHelper.createDateSpan(instance.getEvaluationTime());
            dateSpan.addClassNames(LumoUtility.Overflow.HIDDEN, LumoUtility.TextOverflow.ELLIPSIS);
            return dateSpan;
        });
    }

    protected void initFilter() {
        DecisionInstanceFilter filter = metadata.create(DecisionInstanceFilter.class);
        decisionInstanceFilterDc.setItem(filter);
    }

    protected void initDecisionInstanceGroupStyles() {
        decisionInstanceVBox.addClassNames(LumoUtility.Padding.Top.SMALL, LumoUtility.Padding.Left.XSMALL);
        allVersionsInstancesCountSpan.addClassNames(LumoUtility.FontWeight.BOLD);
        currentVersionsInstancesCountSpan.addClassNames(LumoUtility.FontWeight.BOLD);
    }

    protected <T extends ContainerDataGridHeaderFilter> void addColumnFilter(
            HeaderRow headerRow, String columnName,
            Function<DataGridColumn<HistoricDecisionInstanceShortData>, T> filterProvider) {
        DataGridColumn<HistoricDecisionInstanceShortData> column = decisionInstancesGrid.getColumnByKey(columnName);
        T filterComponent = filterProvider.apply(column);
        BeanUtil.autowireContext(applicationContext, filterComponent);
        HeaderRow.HeaderCell headerCell = headerRow.getCell(column);
        headerCell.setComponent(filterComponent);
    }
}
