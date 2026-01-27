/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processinstance.history;

import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.event.SortEvent;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import io.jmix.core.DataLoadContext;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiEventPublisher;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.filter.VariableFilter;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.entity.variable.HistoricVariableInstanceData;
import io.flowset.control.service.variable.VariableLoadContext;
import io.flowset.control.service.variable.VariableService;
import io.flowset.control.view.processinstance.event.HistoryVariableCountUpdateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.List;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@FragmentDescriptor("history-variables-tab-fragment.xml")
public class HistoryVariablesTabFragment extends Fragment<VerticalLayout> implements HasRefresh {
    @Autowired
    protected VariableService variableService;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected UiEventPublisher uiEventPublisher;
    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected ViewNavigators viewNavigators;

    @ViewComponent
    protected CollectionLoader<HistoricVariableInstanceData> historicVariableInstancesDl;

    @ViewComponent
    protected DataGrid<HistoricVariableInstanceData> historicVariableInstancesGrid;

    @ViewComponent
    protected InstanceContainer<ProcessInstanceData> processInstanceDataDc;

    protected VariableFilter filter;

    protected boolean initialized;

    public void refreshIfRequired() {
        if (!initialized) {
            setVariablesDefaultSort();
            this.filter = metadata.create(VariableFilter.class);
            filter.setProcessInstanceId(processInstanceDataDc.getItem().getId());

            historicVariableInstancesDl.load();
            this.initialized = true;
        }
    }

    @Supply(to = "historicVariableInstancesGrid.value", subject = "renderer")
    protected Renderer<HistoricVariableInstanceData> historicVariableInstancesGridValueRenderer() {
        return new TextRenderer<>(this::getVariableValueColumnText);
    }

    @Install(to = "historicVariableInstancesGrid.value", subject = "tooltipGenerator")
    protected String historicVariableInstancesGridTooltipGenerator(final HistoricVariableInstanceData variableInstanceData) {
        return getVariableValueColumnText(variableInstanceData);
    }


    @Subscribe("historicVariableInstancesGrid.view")
    public void onViewAction(final ActionPerformedEvent event) {
        HistoricVariableInstanceData variableInstance = historicVariableInstancesGrid.getSingleSelectedItem();
        if (variableInstance == null) {
            return;
        }
        dialogWindows.detail(getCurrentView(), HistoricVariableInstanceData.class)
                .editEntity(variableInstance)
                .build()
                .open();
    }

    @Install(to = "historicVariableInstancesDl", target = Target.DATA_LOADER)
    protected List<HistoricVariableInstanceData> historicVariableInstancesDlLoadDelegate(final LoadContext<HistoricVariableInstanceData> loadContext) {
        LoadContext.Query query = loadContext.getQuery();
        VariableLoadContext context = new VariableLoadContext().setFilter(filter);

        if (query != null) {
            context.setFirstResult(query.getFirstResult())
                    .setMaxResults(query.getMaxResults())
                    .setSort(query.getSort());
        }

        return variableService.findHistoricVariables(context);
    }

    @Install(to = "historicVariableInstancesPagination", subject = "totalCountDelegate")
    protected Integer historicVariableInstancesPaginationTotalCountDelegate(final DataLoadContext dataLoadContext) {
        long historicVariableInstancesCount = variableService.getHistoricVariablesCount(filter);
        uiEventPublisher.publishEventForCurrentUI(new HistoryVariableCountUpdateEvent(this, historicVariableInstancesCount));
        return (int) historicVariableInstancesCount;
    }

    @Subscribe("historicVariableInstancesGrid")
    public void onHistoricVariableInstancesGridGridSort(final SortEvent<DataGrid<HistoricVariableInstanceData>, GridSortOrder<DataGrid<HistoricVariableInstanceData>>> event) {
        historicVariableInstancesDl.load();
    }

    @Nullable
    protected String getVariableValueColumnText(HistoricVariableInstanceData variableInstance) {
        if (variableInstance.getValue() != null) {
            return variableInstance.getValue().toString();
        }
        return null;
    }

    protected void setVariablesDefaultSort() {
        List<GridSortOrder<HistoricVariableInstanceData>> gridSortOrders = Collections.singletonList(new GridSortOrder<>(historicVariableInstancesGrid.getColumnByKey("name"),
                SortDirection.ASCENDING));
        historicVariableInstancesGrid.sort(gridSortOrders);
    }
}
