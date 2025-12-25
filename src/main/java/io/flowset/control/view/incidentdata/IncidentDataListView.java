/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.incidentdata;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.event.SortEvent;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.core.DataLoadContext;
import io.jmix.core.LoadContext;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.flowui.*;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.sys.BeanUtil;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.filter.IncidentFilter;
import io.flowset.control.entity.filter.ProcessDefinitionFilter;
import io.flowset.control.entity.incident.IncidentData;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.service.incident.IncidentLoadContext;
import io.flowset.control.service.incident.IncidentService;
import io.flowset.control.service.processdefinition.ProcessDefinitionLoadContext;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import io.flowset.control.view.incidentdata.filter.*;
import io.flowset.control.view.util.ComponentHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.function.Function;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@Slf4j
@Route(value = "bpm/incidents", layout = DefaultMainViewParent.class)
@ViewController("IncidentData.list")
@ViewDescriptor("incident-data-list-view.xml")
@LookupComponent("incidentsDataGrid")
@DialogMode(width = "50em")
public class IncidentDataListView extends StandardListView<IncidentData> {

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected ComponentHelper componentHelper;
    @Autowired
    protected ViewNavigators viewNavigators;
    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected Messages messages;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected Notifications notifications;
    @Autowired
    private DialogWindows dialogWindows;
    @Autowired
    protected IncidentService incidentService;
    @Autowired
    protected ProcessDefinitionService processDefinitionService;

    @ViewComponent
    protected MessageBundle messageBundle;
    @ViewComponent
    protected InstanceContainer<IncidentFilter> filterDc;
    @ViewComponent
    protected CollectionLoader<IncidentData> incidentsDl;
    @ViewComponent
    protected DataGrid<IncidentData> incidentsDataGrid;

    protected Map<String, ProcessDefinitionData> processDefinitionsMap = new HashMap<>();

    @Subscribe
    public void onInit(final InitEvent event) {
        initFilter();
        initDataGridHeaderRow();
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        setDefaultSort();
    }

    @Install(to = "incidentsDl", target = Target.DATA_LOADER)
    protected List<IncidentData> incidentsDlLoadDelegate(LoadContext<IncidentData> loadContext) {
        LoadContext.Query query = loadContext.getQuery();
        IncidentLoadContext context = new IncidentLoadContext().setFilter(filterDc.getItem());
        if (query != null) {
            context.setFirstResult(query.getFirstResult())
                    .setMaxResults(query.getMaxResults())
                    .setSort(query.getSort());
        }

        List<IncidentData> incidents = incidentService.findRuntimeIncidents(context);
        loadProcessDefinitions(incidents);
        return incidents;
    }

    @Install(to = "pagination", subject = "totalCountDelegate")
    protected Integer paginationTotalCountDelegate(final DataLoadContext dataLoadContext) {
        return (int) incidentService.getRuntimeIncidentCount(filterDc.getItem());
    }

    @Supply(to = "incidentsDataGrid.processDefinitionId", subject = "renderer")
    protected Renderer<IncidentData> incidentsDataGridProcessDefinitionIdRenderer() {
        return new TextRenderer<>(this::getFormattedProcess);
    }

    @Subscribe("incidentsDataGrid")
    public void onIncidentsDataGridSort(final SortEvent<DataGrid<IncidentData>, GridSortOrder<DataGrid<IncidentData>>> event) {
        incidentsDl.load();
    }

    @Supply(to = "incidentsDataGrid.actions", subject = "renderer")
    protected Renderer<IncidentData> incidentsDataGridActionsRenderer() {
        return new ComponentRenderer<>(incidentData -> {
            HorizontalLayout layout = uiComponents.create(HorizontalLayout.class);
            layout.addClassNames(LumoUtility.Padding.Top.XSMALL, LumoUtility.Padding.Bottom.XSMALL);
            layout.setWidth("min-content");

            JmixButton viewBtn = createViewButton(incidentData);
            layout.add(viewBtn);

            if (StringUtils.equals(incidentData.getIncidentId(), incidentData.getCauseIncidentId())) {
                if (incidentData.isJobFailed()) {
                    JmixButton retryJobBtn = createRetryJobButton(incidentData);
                    layout.add(retryJobBtn);
                } else if (incidentData.isExternalTaskFailed()) {
                    JmixButton retryExternalTaskBtn = createRetryExternalTaskButton(incidentData);
                    layout.add(retryExternalTaskBtn);
                }
            }

            return layout;
        });
    }

    @Install(to = "incidentsDataGrid.processInstanceId", subject = "tooltipGenerator")
    protected String incidentsDataGridProcessInstanceIdTooltipGenerator(final IncidentData incidentData) {
        return incidentData.getProcessInstanceId();
    }

    @Subscribe(id = "filterDc", target = Target.DATA_CONTAINER)
    public void onFilterDcItemPropertyChange(final InstanceContainer.ItemPropertyChangeEvent<IncidentFilter> event) {
        incidentsDl.load();
    }

    @Subscribe("incidentsDataGrid.bulkRetry")
    public void onIncidentsDataGridBulkRetry(final ActionPerformedEvent event) {
        Set<IncidentData> selectedItems = incidentsDataGrid.getSelectedItems();
        if (selectedItems.isEmpty()) {
            return;
        }

        DialogWindow<BulkRetryIncidentView> dialogWindow = dialogWindows.view(this, BulkRetryIncidentView.class)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                        incidentsDl.load();
                    }
                })
                .build();

        BulkRetryIncidentView bulkRetryIncidentView = dialogWindow.getView();
        bulkRetryIncidentView.setIncidentDataSet(selectedItems);
        dialogWindow.open();
    }

    @Install(to = "incidentsDataGrid.message", subject = "tooltipGenerator")
    protected String incidentsDataGridMessageTooltipGenerator(final IncidentData incidentData) {
        return incidentData.getMessage();
    }

    @Install(to = "incidentsDataGrid.timestamp", subject = "partNameGenerator")
    protected String incidentsDataGridTimestampPartNameGenerator(final IncidentData incidentData) {
        return "multiline-text-cell";
    }

    @Supply(to = "incidentsDataGrid.timestamp", subject = "renderer")
    protected Renderer<IncidentData> incidentsDataGridTimestampRenderer() {
        return new ComponentRenderer<>(incidentData -> {
            Span span = componentHelper.createDateSpan(incidentData.getTimestamp());
            span.addClassNames(LumoUtility.Overflow.HIDDEN, LumoUtility.TextOverflow.ELLIPSIS);
            return span;
        });
    }

    protected void initFilter() {
        IncidentFilter incidentFilter = metadata.create(IncidentFilter.class);
        filterDc.setItem(incidentFilter);
    }

    protected JmixButton createViewButton(IncidentData incidentData) {
        JmixButton viewBtn = uiComponents.create(JmixButton.class);
        viewBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        viewBtn.setText(messages.getMessage("actions.View"));
        viewBtn.setIcon(VaadinIcon.EYE.create());
        viewBtn.addClickListener(buttonClickEvent -> viewNavigators.view(this, IncidentDataDetailView.class)
                .withRouteParameters(new RouteParameters("id", incidentData.getIncidentId()))
                .withBackwardNavigation(true)
                .navigate());
        return viewBtn;
    }

    protected void loadProcessDefinitions(List<IncidentData> incidents) {
        List<String> idsToLoad = incidents.stream()
                .map(IncidentData::getProcessDefinitionId)
                .filter(processDefinitionId -> !processDefinitionsMap.containsKey(processDefinitionId))
                .distinct()
                .toList();
        ProcessDefinitionFilter filter = metadata.create(ProcessDefinitionFilter.class);
        filter.setIdIn(idsToLoad);

        List<ProcessDefinitionData> definitions = processDefinitionService.findAll(new ProcessDefinitionLoadContext().setFilter(filter));
        definitions.forEach(processDefinitionData -> processDefinitionsMap.put(processDefinitionData.getProcessDefinitionId(), processDefinitionData));
    }

    @Nullable
    protected String getFormattedProcess(IncidentData incidentData) {
        if (StringUtils.isEmpty(incidentData.getProcessDefinitionId())) {
            return null;
        }
        ProcessDefinitionData processDefinitionData = processDefinitionsMap.computeIfAbsent(incidentData.getProcessDefinitionId(),
                processDefinitionId -> processDefinitionService.getById(processDefinitionId));

        if (processDefinitionData == null) {
            log.warn("Process definition with id '{}' not found", incidentData.getProcessDefinitionId());
            return null;
        }
        return componentHelper.getProcessLabel(processDefinitionData);
    }

    protected void initDataGridHeaderRow() {
        HeaderRow headerRow = incidentsDataGrid.getDefaultHeaderRow();

        addColumnFilter(headerRow, "activityId", this::createActivityColumnFilter);
        addColumnFilter(headerRow, "message", this::createMessageColumnFilter);
        addColumnFilter(headerRow, "timestamp", this::createTimestampColumnFilter);
        addColumnFilter(headerRow, "processInstanceId", this::createProcessInstanceColumnFilter);
        addColumnFilter(headerRow, "processDefinitionId", this::createProcessColumnFilter);
        addColumnFilter(headerRow, "type", this::createTypeColumnFilter);
    }

    protected <T extends IncidentHeaderFilter> void addColumnFilter(HeaderRow headerRow, String columnName, Function<DataGridColumn<IncidentData>, T> filterProvider) {
        DataGridColumn<IncidentData> column = incidentsDataGrid.getColumnByKey(columnName);
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

    protected IncidentHeaderFilter createActivityColumnFilter(DataGridColumn<IncidentData> column) {
        return new ActivityHeaderFilter(incidentsDataGrid, column, filterDc);
    }

    protected IncidentHeaderFilter createProcessInstanceColumnFilter(DataGridColumn<IncidentData> column) {
        return new ProcessInstanceIdHeaderFilter(incidentsDataGrid, column, filterDc);
    }

    protected IncidentHeaderFilter createProcessColumnFilter(DataGridColumn<IncidentData> column) {
        return new ProcessHeaderFilter(incidentsDataGrid, column, filterDc);
    }

    protected IncidentHeaderFilter createMessageColumnFilter(DataGridColumn<IncidentData> column) {
        return new MessageHeaderFilter(incidentsDataGrid, column, filterDc);
    }

    protected IncidentHeaderFilter createTypeColumnFilter(DataGridColumn<IncidentData> column) {
        return new IncidentTypeHeaderFilter(incidentsDataGrid, column, filterDc);
    }

    protected IncidentHeaderFilter createTimestampColumnFilter(DataGridColumn<IncidentData> column) {
        return new IncidentTimestampHeaderFilter(incidentsDataGrid, column, filterDc);
    }

    protected JmixButton createRetryJobButton(IncidentData incidentData) {
        JmixButton retryJobBtn = uiComponents.create(JmixButton.class);
        retryJobBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        retryJobBtn.setText(messages.getMessage("actions.Retry"));
        retryJobBtn.setIcon(VaadinIcon.ROTATE_LEFT.create());
        retryJobBtn.addClickListener(buttonClickEvent -> {
            DialogWindow<RetryJobView> dialogWindow = dialogWindows.view(getCurrentView(), RetryJobView.class)
                    .withAfterCloseListener(afterClose -> {
                        if (afterClose.closedWith(StandardOutcome.SAVE)) {
                            incidentsDl.load();
                        }
                    })
                    .build();

            RetryJobView retryJobView = dialogWindow.getView();
            retryJobView.setJobId(incidentData.getConfiguration());

            dialogWindow.open();
        });
        return retryJobBtn;
    }

    protected JmixButton createRetryExternalTaskButton(IncidentData incidentData) {
        JmixButton retryExternalTaskBtn = uiComponents.create(JmixButton.class);
        retryExternalTaskBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        retryExternalTaskBtn.setText(messages.getMessage("actions.Retry"));
        retryExternalTaskBtn.setIcon(VaadinIcon.ROTATE_LEFT.create());
        retryExternalTaskBtn.addClickListener(buttonClickEvent -> {
            DialogWindow<RetryExternalTaskView> dialogWindow = dialogWindows.view(this, RetryExternalTaskView.class)
                    .withAfterCloseListener(closeEvent -> {
                        if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                            incidentsDl.load();
                        }
                    })
                    .build();

            RetryExternalTaskView retryExternalTaskView = dialogWindow.getView();
            retryExternalTaskView.setExternalTaskId(incidentData.getConfiguration());
            dialogWindow.open();
        });
        return retryExternalTaskBtn;
    }

    protected void setDefaultSort() {
        List<GridSortOrder<IncidentData>> gridSortOrders = Collections.singletonList(new GridSortOrder<>(incidentsDataGrid.getColumnByKey("timestamp"),
                SortDirection.DESCENDING));
        incidentsDataGrid.sort(gridSortOrders);
    }
}
