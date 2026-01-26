/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.alltasks;


import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.view.AbstractListViewWithDelayedLoad;
import io.jmix.core.DataLoadContext;
import io.jmix.core.LoadContext;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.details.JmixDetails;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.UserTaskData;
import io.flowset.control.entity.filter.ProcessDefinitionFilter;
import io.flowset.control.entity.filter.UserTaskFilter;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.service.processdefinition.ProcessDefinitionLoadContext;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import io.flowset.control.service.usertask.UserTaskLoadContext;
import io.flowset.control.service.usertask.UserTaskService;
import io.flowset.control.view.bulktaskcomplete.BulkTaskCompleteView;
import io.flowset.control.view.taskreassign.TaskReassignView;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Stream;

import static io.flowset.control.view.util.JsUtils.SET_DEFAULT_TIME_SCRIPT;

@Route(value = "bpm/user-tasks", layout = DefaultMainViewParent.class)
@ViewController("bpm_AllTasksView")
@ViewDescriptor("all-tasks-view.xml")
@LookupComponent("tasksDataGrid")
public class AllTasksView extends AbstractListViewWithDelayedLoad<UserTaskData> {

    protected static final String ASSIGNED_OPTION = "assigned";
    protected static final String UNASSIGNED_OPTION = "unassigned";

    @Autowired
    protected Metadata metadata;
    @ViewComponent
    protected MessageBundle messageBundle;
    @Autowired
    protected UserTaskService userTaskService;

    @Autowired
    protected DialogWindows dialogWindows;


    @ViewComponent
    protected CollectionContainer<UserTaskData> tasksDc;
    @ViewComponent
    protected CollectionLoader<UserTaskData> tasksDl;
    @ViewComponent
    protected InstanceContainer<UserTaskFilter> userTaskFilterDc;

    @Autowired
    protected ProcessDefinitionService processDefinitionService;

    @ViewComponent
    protected JmixFormLayout filterFormLayout;
    @ViewComponent
    protected JmixComboBox<ProcessDefinitionData> processDefinitionLookup;
    @ViewComponent
    protected DataGrid<UserTaskData> tasksDataGrid;

    @ViewComponent
    protected FlexLayout filterContainer;
    @ViewComponent
    protected JmixButton filterBtn;

    @ViewComponent
    protected Span appliedFiltersCount;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Messages messages;
    @ViewComponent
    protected JmixDetails assignmentFilters;
    @ViewComponent
    protected JmixDetails creationDateFilters;
    @ViewComponent
    protected JmixDetails generalFilters;
    @ViewComponent
    protected JmixRadioButtonGroup<Object> assignmentTypeGroup;
    @ViewComponent
    protected TypedDateTimePicker<OffsetDateTime> createdBeforeField;
    @ViewComponent
    protected TypedDateTimePicker<OffsetDateTime> createdAfterField;
    @ViewComponent
    protected TypedTextField<String> assigneeField;

    protected Map<String, ProcessDefinitionData> processDefinitionsMap = new HashMap<>();

    @ViewComponent
    protected JmixRadioButtonGroup<UserTaskStateFilterOption> stateTypeGroup;

    @Subscribe
    protected void onInit(InitEvent initEvent) {
        addClassNames(LumoUtility.Padding.Top.SMALL);
        initFilterFormStyles();
        initFilter();

        setDefaultSort();

        createdBeforeField.getElement().executeJs(SET_DEFAULT_TIME_SCRIPT);
        createdAfterField.getElement().executeJs(SET_DEFAULT_TIME_SCRIPT);

        addDetailsSummary(generalFilters, "generalFilterGroup");
        addDetailsSummary(assignmentFilters, "assignmentFilterGroup");
        addDetailsSummary(creationDateFilters, "createDateFilterGroup");
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        Map<Object, String> assignmentTypesMap = ImmutableMap.of(
                ASSIGNED_OPTION, messages.getMessage(UserTaskFilter.class, "UserTaskFilter.assigned"),
                UNASSIGNED_OPTION, messages.getMessage(UserTaskFilter.class, "UserTaskFilter.unassigned")
        );
        ComponentUtils.setItemsMap(assignmentTypeGroup, assignmentTypesMap);

        stateTypeGroup.setItems(UserTaskStateFilterOption.class);
        stateTypeGroup.setValue(UserTaskStateFilterOption.ALL);
    }

    @Install(to = "processDefinitionLookup", subject = "itemsFetchCallback")
    protected Stream<ProcessDefinitionData> processDefinitionLookupItemsFetchCallback(final Query<ProcessDefinitionData, String> query) {
        ProcessDefinitionFilter filter = metadata.create(ProcessDefinitionFilter.class);
        filter.setKeyLike(query.getFilter().orElse(null));
        filter.setLatestVersionOnly(true);
        ProcessDefinitionLoadContext context = new ProcessDefinitionLoadContext().setFilter(filter)
                .setMaxResults(query.getLimit())
                .setFirstResult(query.getOffset());

        return processDefinitionService.findAll(context).stream();
    }

    @Install(to = "processDefinitionLookup", subject = "itemLabelGenerator")
    protected String processDefinitionLookupItemLabelGenerator(final ProcessDefinitionData item) {
        return item.getKey();
    }

    @Subscribe("processDefinitionLookup")
    public void onProcessDefinitionLookupComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixComboBox<ProcessDefinitionData>, ProcessDefinitionData> event) {
        ProcessDefinitionData value = event.getValue();
        String key = value != null ? value.getKey() : null;
        userTaskFilterDc.getItem().setProcessDefinitionKey(key);
        startLoadData();
    }

    @Subscribe(id = "userTaskFilterDc", target = Target.DATA_CONTAINER)
    public void onUserTaskFilterDcItemPropertyChange(final InstanceContainer.ItemPropertyChangeEvent<UserTaskFilter> event) {
        String property = event.getProperty();

        boolean notAssignmentFilter = !property.equals("assigned") && !property.equals("unassigned");
        boolean notStateFilter = !property.equals("active") && !property.equals("suspended");
        if (notAssignmentFilter && notStateFilter) { //not to load because the data loading is implemented in the component listeners
            startLoadData();
        }
    }

    @Install(to = "tasksDataGrid.completeTask", subject = "enabledRule")
    protected boolean tasksDataGridCompleteTaskEnabledRule() {
        Set<UserTaskData> selectedItems = tasksDataGrid.getSelectedItems();
        boolean suspendedTaskSelected = selectedItems.stream().anyMatch(userTaskData -> BooleanUtils.isTrue(userTaskData.getSuspended()));
        return !selectedItems.isEmpty() && !suspendedTaskSelected;
    }

    @Install(to = "tasksDataGrid.reassignTask", subject = "enabledRule")
    protected boolean tasksDataGridReassignTaskEnabledRule() {
        Set<UserTaskData> selectedItems = tasksDataGrid.getSelectedItems();
        boolean suspendedTaskSelected = selectedItems.stream().anyMatch(userTaskData -> BooleanUtils.isTrue(userTaskData.getSuspended()));
        return !selectedItems.isEmpty() && !suspendedTaskSelected;
    }

    @Subscribe("tasksDataGrid.reassignTask")
    protected void onTasksDataGridReassignTaskActionPerformed(ActionPerformedEvent event) {
        dialogWindows.view(this, TaskReassignView.class)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                        startLoadData();
                    }
                })
                .withViewConfigurer(taskReassignView -> taskReassignView.setTaskDataList(tasksDataGrid.getSelectedItems()))
                .build()
                .open();
    }

    @Subscribe("tasksDataGrid.completeTask")
    public void onTasksDataGridCompleteTask(final ActionPerformedEvent event) {
        dialogWindows.view(this, BulkTaskCompleteView.class)
                .withViewConfigurer(bulkTaskCompleteView -> bulkTaskCompleteView.setUserTasks(tasksDataGrid.getSelectedItems()))
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                        startLoadData();
                    }
                })
                .build()
                .open();

    }

    @Subscribe("applyFilter")
    protected void onApplyFilterActionPerformed(ActionPerformedEvent event) {
        startLoadData();
    }

    @Subscribe(id = "tasksDl", target = Target.DATA_LOADER)
    public void onTasksDlPostLoad(final CollectionLoader.PostLoadEvent<UserTaskData> event) {
        updateAppliedFiltersCount();
    }

    @Install(to = "tasksDl", target = Target.DATA_LOADER)
    protected List<UserTaskData> tasksDlLoadDelegate(final LoadContext<UserTaskData> loadContext) {
        LoadContext.Query query = loadContext.getQuery();
        UserTaskLoadContext context = new UserTaskLoadContext().setFilter(userTaskFilterDc.getItem());
        if (query != null) {
            context.setFirstResult(query.getFirstResult())
                    .setMaxResults(query.getMaxResults())
                    .setSort(query.getSort());
        }

        return loadItemsWithStateHandling(() -> {
            List<UserTaskData> runtimeTasks = userTaskService.findRuntimeTasks(context);
            loadProcessDefinitions(runtimeTasks);
            return runtimeTasks;
        });
    }

    @Install(to = "tasksPagination", subject = "totalCountDelegate")
    protected Integer tasksPaginationTotalCountDelegate(final DataLoadContext loadContext) {
        return (int) userTaskService.getRuntimeTasksCount(userTaskFilterDc.getItem());
    }


    @Subscribe(id = "filterBtn", subject = "clickListener")
    public void onFilterBtnClick(final ClickEvent<JmixButton> event) {
        filterContainer.setVisible(!filterContainer.isVisible());
        if (filterContainer.isVisible()) {
            filterBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        } else {
            filterBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
        }
    }

    @Subscribe(id = "clearBtn", subject = "clickListener")
    public void onClearBtnClick(final ClickEvent<JmixButton> event) {
        processDefinitionLookup.setValue(null);
        assignmentTypeGroup.setValue(null);
        assignmentTypeGroup.setEnabled(true);
        stateTypeGroup.setValue(null);

        UserTaskFilter userTaskFilter = metadata.create(UserTaskFilter.class);
        userTaskFilterDc.setItem(userTaskFilter);

        startLoadData();
    }

    @Subscribe("tasksDataGrid.refresh")
    public void onTasksDataGridRefresh(final ActionPerformedEvent event) {
        startLoadData();
    }

    @Install(to = "tasksDataGrid.name", subject = "tooltipGenerator")
    protected String tasksDataGridNameTooltipGenerator(final UserTaskData userTaskData) {
        return userTaskData.getName();
    }

    @Install(to = "tasksDataGrid.taskDefinitionKey", subject = "tooltipGenerator")
    protected String tasksDataGridTaskDefinitionKeyTooltipGenerator(final UserTaskData userTaskData) {
        return userTaskData.getTaskDefinitionKey();
    }

    @Subscribe("assignmentTypeGroup")
    public void onAssignmentTypeGroupComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixRadioButtonGroup<String>, String> event) {
        String value = event.getValue();
        if (value == null) {
            assigneeField.setEnabled(true);
            userTaskFilterDc.getItem().setUnassigned(null);
            userTaskFilterDc.getItem().setAssigned(null);
        } else if (ASSIGNED_OPTION.equals(value)) {
            assigneeField.setEnabled(true);
            userTaskFilterDc.getItem().setUnassigned(null);
            userTaskFilterDc.getItem().setAssigned(true);
        } else if (UNASSIGNED_OPTION.equals(value)) {
            userTaskFilterDc.getItem().setUnassigned(true);
            userTaskFilterDc.getItem().setAssigned(null);
            userTaskFilterDc.getItem().setAssigneeLike(null);
            assigneeField.setEnabled(false);
        }
        startLoadData();
    }

    @Subscribe("stateTypeGroup")
    protected void onStateTypeGroupComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixRadioButtonGroup<UserTaskStateFilterOption>, UserTaskStateFilterOption> event) {
        if (event.isFromClient()) {
            UserTaskStateFilterOption option = event.getValue() == null ? UserTaskStateFilterOption.ALL : event.getValue();

            switch (option) {
                case ALL -> setAllStatesFilter();
                case ACTIVE -> setActiveTasksFilter();
                case SUSPENDED -> setSuspendedTasksFilter();
            }

            startLoadData();
        }
    }

    @Supply(to = "tasksDataGrid.processDefinitionId", subject = "renderer")
    protected Renderer<UserTaskData> tasksDataGridProcessDefinitionIdRenderer() {
        return new TextRenderer<>(this::getFormattedProcess);
    }

    @Install(to = "tasksDataGrid.processDefinitionId", subject = "tooltipGenerator")
    protected String tasksDataGridProcessDefinitionIdTooltipGenerator(final UserTaskData userTaskData) {
        return getFormattedProcess(userTaskData);
    }

    @Override
    protected void loadData() {
        tasksDl.load();
    }

    protected void setSuspendedTasksFilter() {
        userTaskFilterDc.getItem().setSuspended(true);
        userTaskFilterDc.getItem().setActive(null);
    }

    protected void setActiveTasksFilter() {
        userTaskFilterDc.getItem().setSuspended(null);
        userTaskFilterDc.getItem().setActive(true);
    }

    protected void setAllStatesFilter() {
        userTaskFilterDc.getItem().setSuspended(null);
        userTaskFilterDc.getItem().setActive(null);
    }

    protected void loadProcessDefinitions(List<UserTaskData> runtimeTasks) {
        List<String> idsToLoad = runtimeTasks.stream()
                .map(UserTaskData::getProcessDefinitionId)
                .filter(processDefinitionId -> !processDefinitionsMap.containsKey(processDefinitionId))
                .distinct()
                .toList();
        ProcessDefinitionFilter filter = metadata.create(ProcessDefinitionFilter.class);
        filter.setIdIn(idsToLoad);

        List<ProcessDefinitionData> definitions = processDefinitionService.findAll(new ProcessDefinitionLoadContext().setFilter(filter));
        definitions.forEach(processDefinitionData -> processDefinitionsMap.put(processDefinitionData.getProcessDefinitionId(), processDefinitionData));
    }


    protected void addDetailsSummary(JmixDetails details, String messageKey) {
        H5 summary = uiComponents.create(H5.class);
        summary.setText(messageBundle.getMessage(messageKey));
        details.setSummary(summary);
    }

    protected void initFilter() {
        UserTaskFilter userTaskFilter = metadata.create(UserTaskFilter.class);
        userTaskFilterDc.setItem(userTaskFilter);
    }

    protected void initFilterFormStyles() {
        filterFormLayout.getOwnComponents().forEach(component -> component.addClassName(LumoUtility.Padding.Top.XSMALL));
        filterFormLayout.addClassNames(LumoUtility.Flex.GROW);
    }

    protected void updateAppliedFiltersCount() {
        MetaClass metaClass = metadata.getClass(UserTaskFilter.class);
        long filtersCount = metaClass.getOwnProperties()
                .stream()
                .filter(metaProperty -> {
                    Object value = EntityValues.getValue(userTaskFilterDc.getItem(), metaProperty.getName());
                    boolean notEmptyValue;
                    if (value instanceof Collection<?> collection) {
                        notEmptyValue = CollectionUtils.isNotEmpty(collection);
                    } else if (value instanceof String s) {
                        notEmptyValue = StringUtils.isNotEmpty(s);
                    } else {
                        notEmptyValue = value != null;
                    }
                    return notEmptyValue && !metaProperty.getName().equals("id");
                })
                .count();
        if (filtersCount > 0) {
            appliedFiltersCount.setVisible(true);
            appliedFiltersCount.setText(String.valueOf(filtersCount));
        } else {
            appliedFiltersCount.setVisible(false);
        }
    }

    protected String getFormattedProcess(UserTaskData userTaskData) {
        ProcessDefinitionData processDefinitionData = processDefinitionsMap.computeIfAbsent(userTaskData.getProcessDefinitionId(),
                processDefinitionId -> processDefinitionService.getById(processDefinitionId));

        return componentHelper.getProcessLabel(processDefinitionData);
    }

    protected void setDefaultSort() {
        List<GridSortOrder<UserTaskData>> gridSortOrders = Collections.singletonList(new GridSortOrder<>(tasksDataGrid.getColumnByKey("createTime"),
                SortDirection.DESCENDING));
        tasksDataGrid.sort(gridSortOrders);
    }
}