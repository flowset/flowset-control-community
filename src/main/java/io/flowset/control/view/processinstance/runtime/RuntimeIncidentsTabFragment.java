/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processinstance.runtime;

import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.event.SortEvent;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.action.incident.RetryIncidentGridAction;
import io.jmix.core.DataLoadContext;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiEventPublisher;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.filter.IncidentFilter;
import io.flowset.control.entity.incident.IncidentData;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.service.incident.IncidentLoadContext;
import io.flowset.control.service.incident.IncidentService;
import io.flowset.control.view.processinstance.event.IncidentCountUpdateEvent;
import io.flowset.control.view.processinstance.event.IncidentUpdateEvent;
import io.flowset.control.view.util.ComponentHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@FragmentDescriptor("runtime-incident-tab-fragment.xml")
public class RuntimeIncidentsTabFragment extends Fragment<VerticalLayout> {

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected UiEventPublisher uiEventPublisher;
    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected ComponentHelper componentHelper;
    @Autowired
    protected IncidentService incidentService;

    @ViewComponent
    protected CollectionLoader<IncidentData> runtimeIncidentsDl;
    @ViewComponent
    protected InstanceContainer<ProcessInstanceData> processInstanceDataDc;
    @ViewComponent
    protected DataGrid<IncidentData> runtimeIncidentsGrid;
    @ViewComponent("runtimeIncidentsGrid.retry")
    protected RetryIncidentGridAction retryAction;

    protected IncidentFilter filter;
    protected String selectedActivityId;
    protected boolean initialized = false;

    @SuppressWarnings("LombokSetterMayBeUsed")
    public void setSelectedActivityId(String selectedActivityId) {
        this.selectedActivityId = selectedActivityId;
    }

    public void refreshIfChanged(String selectedActivityId) {
        if (!initialized) {
            this.filter = metadata.create(IncidentFilter.class);
            filter.setProcessInstanceId(processInstanceDataDc.getItem().getId());
            runtimeIncidentsDl.load();
            this.initialized = true;
            return;
        }

        if (!StringUtils.equals(this.selectedActivityId, selectedActivityId)) {
            this.selectedActivityId = selectedActivityId;
            filter.setActivityId(selectedActivityId);
            runtimeIncidentsDl.load();
        }
    }

    @Subscribe
    public void onReady(ReadyEvent event) {
        retryAction.setAfterSaveHandler(this::reloadIncidents);
    }

    @Subscribe("runtimeIncidentsGrid.view")
    public void onViewAction(final ActionPerformedEvent event) {
        IncidentData incidentData = runtimeIncidentsGrid.getSingleSelectedItem();
        if (incidentData == null) {
            return;
        }
        dialogWindows.detail(getCurrentView(), IncidentData.class)
                .editEntity(incidentData)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                        reloadIncidents();
                    }
                })
                .build()
                .open();
    }

    @Install(to = "runtimeIncidentsGrid.timestamp", subject = "partNameGenerator")
    protected String runtimeIncidentsGridTimestampPartNameGenerator(final IncidentData incidentData) {
        return "multiline-text-cell";
    }

    @Supply(to = "runtimeIncidentsGrid.timestamp", subject = "renderer")
    protected Renderer<IncidentData> runtimeIncidentsGridTimestampRenderer() {
        return new ComponentRenderer<>(incidentData -> {
            Span span = componentHelper.createDateSpan(incidentData.getTimestamp());
            span.addClassNames(LumoUtility.Overflow.HIDDEN, LumoUtility.TextOverflow.ELLIPSIS);
            return span;
        });
    }

    @Install(to = "runtimeIncidentsDl", target = Target.DATA_LOADER)
    protected List<IncidentData> runtimeIncidentsDlLoadDelegate(final LoadContext<IncidentData> loadContext) {
        LoadContext.Query query = loadContext.getQuery();
        IncidentLoadContext context = new IncidentLoadContext().setFilter(filter);

        if (query != null) {
            context.setFirstResult(query.getFirstResult())
                    .setMaxResults(query.getMaxResults())
                    .setSort(query.getSort());
        }

        return incidentService.findRuntimeIncidents(context);
    }

    @Install(to = "incidentsPagination", subject = "totalCountDelegate")
    protected Integer incidentsPaginationTotalCountDelegate(final DataLoadContext dataLoadContext) {
        long incidentCount = incidentService.getRuntimeIncidentCount(filter);

        uiEventPublisher.publishEventForCurrentUI(new IncidentCountUpdateEvent(this, incidentCount));
        return (int) incidentCount;
    }

    @Subscribe("runtimeIncidentsGrid")
    public void onRuntimeIncidentsGridGridSort(final SortEvent<DataGrid<IncidentData>, GridSortOrder<DataGrid<IncidentData>>> event) {
        runtimeIncidentsDl.load();
    }

    protected void reloadIncidents() {
        runtimeIncidentsDl.load();
        uiEventPublisher.publishEventForCurrentUI(new IncidentUpdateEvent(this));
    }
}
