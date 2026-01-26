package io.flowset.control.view.processinstance.terminatereason;


import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.event.SortEvent;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.core.DataLoadContext;
import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.filter.IncidentFilter;
import io.flowset.control.entity.incident.HistoricIncidentData;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.service.incident.IncidentLoadContext;
import io.flowset.control.service.incident.IncidentService;
import io.flowset.control.view.processinstance.ProcessInstanceDetailView;
import io.flowset.control.view.util.ComponentHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@Route(value = "instance-terminate-reason-view", layout = DefaultMainViewParent.class)
@ViewController(id = "InstanceTerminateReasonView")
@ViewDescriptor(path = "instance-terminate-reason-view.xml")
@DialogMode(width = "65em")
public class InstanceTerminateReasonView extends StandardView {
    @Autowired
    protected IncidentService incidentService;

    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected ComponentHelper componentHelper;

    @ViewComponent
    protected InstanceContainer<ProcessInstanceData> processInstanceDc;
    @ViewComponent
    protected CollectionLoader<HistoricIncidentData> incidentsDl;
    @ViewComponent
    protected DataGrid<HistoricIncidentData> incidentsGrid;

    protected IncidentFilter filter;

    public void setProcessInstance(ProcessInstanceData processInstanceData) {
        processInstanceDc.setItem(processInstanceData);
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        filter = dataManager.create(IncidentFilter.class);
        filter.setProcessInstanceId(processInstanceDc.getItem().getInstanceId());
        incidentsDl.load();
    }


    @Subscribe("incidentsGrid.view")
    public void onViewAction(final ActionPerformedEvent event) {
        HistoricIncidentData incident = incidentsGrid.getSingleSelectedItem();
        if (incident == null) {
            return;
        }
        dialogWindows.detail(getCurrentView(), HistoricIncidentData.class)
                .editEntity(incident)
                .build()
                .open();
    }

    @Install(to = "incidentsGrid.createTime", subject = "partNameGenerator")
    protected String incidentsGridCreateTimePartNameGenerator(final HistoricIncidentData historicIncidentData) {
        return "multiline-text-cell";
    }

    @Supply(to = "incidentsGrid.createTime", subject = "renderer")
    protected Renderer<HistoricIncidentData> incidentsGridCreateTimeRenderer() {
        return new ComponentRenderer<>(incidentData -> {
            Component dateSpan = componentHelper.createDateSpan(incidentData.getCreateTime());
            dateSpan.addClassNames(LumoUtility.Overflow.HIDDEN, LumoUtility.TextOverflow.ELLIPSIS);
            return dateSpan;
        });
    }

    @Install(to = "incidentsGrid.endTime", subject = "partNameGenerator")
    protected String incidentsGridEndTimePartNameGenerator(final HistoricIncidentData historicIncidentData) {
        return "multiline-text-cell";
    }

    @Supply(to = "incidentsGrid.endTime", subject = "renderer")
    protected Renderer<HistoricIncidentData> incidentsGridEndTimeRenderer() {
        return new ComponentRenderer<>(incidentData -> {
            Span dateSpan = componentHelper.createDateSpan(incidentData.getEndTime());
            dateSpan.addClassNames(LumoUtility.Overflow.HIDDEN, LumoUtility.TextOverflow.ELLIPSIS);
            return dateSpan;
        });
    }

    @Install(to = "incidentsDl", target = Target.DATA_LOADER)
    protected List<HistoricIncidentData> incidentsDlLoadDelegate(final LoadContext<HistoricIncidentData> loadContext) {
        LoadContext.Query query = loadContext.getQuery();
        IncidentLoadContext context = new IncidentLoadContext().setFilter(filter);

        if (query != null) {
            context.setFirstResult(query.getFirstResult())
                    .setMaxResults(query.getMaxResults())
                    .setSort(query.getSort());
        }

        return incidentService.findHistoricIncidents(context);
    }

    @Install(to = "incidentsPagination", subject = "totalCountDelegate")
    protected Integer incidentsPaginationTotalCountDelegate(final DataLoadContext dataLoadContext) {
        long incidentsCount = incidentService.getHistoricIncidentCount(filter);
        return (int) incidentsCount;
    }

    @Subscribe("incidentsGrid")
    public void onHistoryTasksGridGridSort(final SortEvent<DataGrid<HistoricIncidentData>, GridSortOrder<DataGrid<HistoricIncidentData>>> event) {
        incidentsDl.load();
    }

    @Subscribe(id = "viewInstanceBtn", subject = "clickListener")
    public void onViewInstanceBtnClick(final ClickEvent<JmixButton> event) {
        RouterLink routerLink = new RouterLink(ProcessInstanceDetailView.class, new RouteParameters("id",
                processInstanceDc.getItem().getInstanceId()));
        getUI().ifPresent(ui -> ui.getPage().open(routerLink.getHref()));
    }
}