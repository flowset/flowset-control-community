package io.flowset.control.service.analytics;

import io.flowset.control.view.decisioninstance.DecisionInstanceDataListView;
import io.flowset.control.view.decisioninstance.DecisionInstanceDetailView;
import io.flowset.control.view.job.ActivateJobView;
import io.flowset.control.view.job.SuspendJobView;
import io.flowset.control.view.taskcomplete.TaskCompleteView;
import io.jmix.flowui.action.list.ListDataComponentAction;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.event.view.ViewOpenedEvent;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewActions;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.securityflowui.view.resourcerole.ResourceRoleModelDetailView;
import io.jmix.securityflowui.view.resourcerole.ResourceRoleModelListView;
import io.jmix.securityflowui.view.rowlevelrole.RowLevelRoleModelDetailView;
import io.jmix.securityflowui.view.rowlevelrole.RowLevelRoleModelListView;
import io.flowset.control.view.about.AboutProductView;
import io.flowset.control.view.resourcerolemodellist.ControlResourceRoleModelListView;
import io.flowset.control.view.alltasks.AllTasksView;
import io.flowset.control.view.bpmengine.BpmEngineDetailView;
import io.flowset.control.view.bpmengine.BpmEngineListView;
import io.flowset.control.view.bulktaskcomplete.BulkTaskCompleteView;
import io.flowset.control.view.decisiondefinition.DecisionDefinitionDetailView;
import io.flowset.control.view.decisiondefinition.DecisionDefinitionListView;
import io.flowset.control.view.deploymentdata.DeploymentDetailView;
import io.flowset.control.view.deploymentdata.DeploymentListView;
import io.flowset.control.view.engineconnectionsettings.EngineConnectionSettingsView;
import io.flowset.control.view.incidentdata.*;
import io.flowset.control.view.newprocessdeployment.NewProcessDeploymentView;
import io.flowset.control.view.processdefinition.*;
import io.flowset.control.view.processinstance.*;
import io.flowset.control.view.processinstancemigration.ProcessInstanceMigrationView;
import io.flowset.control.view.processinstanceterminate.ProcessInstanceTerminateView;
import io.flowset.control.view.startprocess.StartProcessWithVariableView;
import io.flowset.control.view.taskreassign.TaskReassignView;
import io.flowset.control.view.user.UserDetailView;
import io.flowset.control.view.user.UserListView;
import io.flowset.control.view.usertaskdata.UserTaskDataDetailView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static io.flowset.control.service.analytics.AmplitudeEventType.*;

@Slf4j
@Component("control_ViewAnalyticsListener")
public class ViewAnalyticsListener {
    private final AnalyticsService analyticsService;
    private final Map<Class<? extends View<?>>, ViewEventData> viewAnalyticstDataMap;

    public ViewAnalyticsListener(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;

        this.viewAnalyticstDataMap = new ConcurrentHashMap<>();
        fillViewAnalyticsMap();
    }

    @EventListener
    public void handleViewOpenedEvent(ViewOpenedEvent viewOpenedEvent) {
        View<?> source = viewOpenedEvent.getSource();
        ViewEventData viewEventData = resolveViewEventData(source.getClass());
        if (viewEventData != null) {
            AmplitudeEventType openEventType = viewEventData.getOpenEventType();
            if (openEventType != null) {
                analyticsService.logEvent(openEventType);
            }

            AmplitudeEventType onSaveCloseEvent = viewEventData.getSaveCloseEventType();
            if (onSaveCloseEvent != null) {
                addSaveCloseListener(source, onSaveCloseEvent);
            }

            GridActionEventData gridActionEventData = viewEventData.getGridActionEventData();
            if (gridActionEventData != null) {
                addGridActionListeners(source, gridActionEventData);
            }

            Map<String, AmplitudeEventType> customActionEvents = viewEventData.getCustomActionEvents();
            if (customActionEvents != null) {
                customActionEvents.forEach((actionId, amplitudeEventType) ->
                        addCustomViewActionListeners(source, actionId, amplitudeEventType));
            }
        }
    }

    /**
     * Resolves the analytics config for a view class. First tries an exact match, then falls back
     * to a registered superclass so that community subclasses of standard views are still tracked.
     */
    @org.springframework.lang.Nullable
    protected ViewEventData resolveViewEventData(Class<?> viewClass) {
        ViewEventData exact = this.viewAnalyticstDataMap.get(viewClass);
        if (exact != null) {
            return exact;
        }
        for (Map.Entry<Class<? extends View<?>>, ViewEventData> entry : this.viewAnalyticstDataMap.entrySet()) {
            if (entry.getKey().isAssignableFrom(viewClass)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private void fillViewAnalyticsMap() {
        addProcessDefinitionStatistics();

        addProcessInstanceStatistics();

        addUserTasksStatistics();

        addIncidentsStatistics();

        addDeploymentsStatistics();

        addDmnStatistics();

        addBpmEngineStatistics();

        addUserStatistics();

        addDashboardStatistics();

        addRolesStatistics();

        addJobsStatistics();

        this.viewAnalyticstDataMap.put(AboutProductView.class, new ViewEventData()
                .setOpenEventType(CONTROL_OPEN_ABOUT_VIEW));
    }

    private void addRolesStatistics() {
        this.viewAnalyticstDataMap.put(ResourceRoleModelListView.class, new ViewEventData()
                .setOpenEventType(CONTROL_OPEN_RESOURCES_ROLE_LIST_VIEW));
        // Community uses a custom subclass for the resource role list view.
        this.viewAnalyticstDataMap.put(ControlResourceRoleModelListView.class, new ViewEventData()
                .setOpenEventType(CONTROL_OPEN_RESOURCES_ROLE_LIST_VIEW));
        this.viewAnalyticstDataMap.put(RowLevelRoleModelListView.class, new ViewEventData()
                .setOpenEventType(CONTROL_OPEN_ROW_LEVEL_ROLE_LIST_VIEW));

        this.viewAnalyticstDataMap.put(ResourceRoleModelDetailView.class, new ViewEventData()
                .setOpenEventType(CONTROL_OPEN_RESOURCE_ROLE_DETAIL_VIEW));
        this.viewAnalyticstDataMap.put(RowLevelRoleModelDetailView.class, new ViewEventData()
                .setOpenEventType(CONTROL_OPEN_ROW_LEVEL_ROLE_DETAIL_VIEW));
    }

    private void addBpmEngineStatistics() {
        this.viewAnalyticstDataMap.put(BpmEngineListView.class, new ViewEventData()
                .setOpenEventType(CONTROL_OPEN_BPM_ENGINES_LIST_VIEW)
                .setGridActionEventData(new GridActionEventData("bpmEnginesDataGrid",
                        Map.of("remove", CONTROL_REMOVE_BPM_ENGINE))));

        this.viewAnalyticstDataMap.put(BpmEngineDetailView.class, new ViewEventData()
                .setOpenEventType(CONTROL_OPEN_BPM_ENGINE_DETAIL_VIEW)
                .setSaveCloseEventType(CONTROL_CREATE_BPM_ENGINE)
                .setCustomActionEvents(Map.of("testConnectionAction", CONTROL_TEST_CONNECTION_BPM_ENGINE)));

        this.viewAnalyticstDataMap.put(EngineConnectionSettingsView.class, new ViewEventData()
                .setOpenEventType(CONTROL_OPEN_BPM_ENGINE_DIALOG)
                .setCustomActionEvents(Map.of("testConnectionAction", CONTROL_TEST_CONNECTION_BPM_ENGINE)));
    }


    private void addUserStatistics() {
        this.viewAnalyticstDataMap.put(UserListView.class, new ViewEventData()
                .setOpenEventType(CONTROL_OPEN_USER_LIST_VIEW));
        this.viewAnalyticstDataMap.put(UserDetailView.class, new ViewEventData()
                .setOpenEventType(CONTROL_OPEN_USER_DETAIL_VIEW));
    }

    private void addDashboardStatistics() {
        // Dashboard opening is tracked by DashboardFragment itself, enriched with size metrics.
    }

    private void addDmnStatistics() {
        this.viewAnalyticstDataMap.put(DecisionDefinitionListView.class, new ViewEventData()
                .setOpenEventType(CONTROL_OPEN_DECISION_TABLE_LIST_VIEW)
                .setGridActionEventData(new GridActionEventData("decisionDefinitionsGrid",
                        Map.of("refresh", CONTROL_REFRESH_DECISION_LIST))));

        this.viewAnalyticstDataMap.put(DecisionDefinitionDetailView.class, new ViewEventData()
                .setOpenEventType(CONTROL_OPEN_DECISION_TABLE_DETAIL_VIEW));

        this.viewAnalyticstDataMap.put(DecisionInstanceDataListView.class, new ViewEventData()
                .setOpenEventType(CONTROL_OPEN_DECISION_INSTANCE_LIST_VIEW)
                .setGridActionEventData(new GridActionEventData("decisionInstancesDataGrid",
                        Map.of("refresh", CONTROL_REFRESH_DECISION_INSTANCE_LIST))));

        this.viewAnalyticstDataMap.put(DecisionInstanceDetailView.class, new ViewEventData()
                .setOpenEventType(CONTROL_OPEN_DECISION_INSTANCE_DETAIL_VIEW));
    }

    private void addDeploymentsStatistics() {
        this.viewAnalyticstDataMap.put(DeploymentListView.class, new ViewEventData()
                .setOpenEventType(CONTROL_OPEN_DEPLOYMENT_LIST_VIEW)
                .setGridActionEventData(new GridActionEventData("deploymentsDataGrid",
                        Map.of("refresh", CONTROL_REFRESH_DEPLOYMENT_LIST))));

        this.viewAnalyticstDataMap.put(DeploymentDetailView.class, new ViewEventData()
                .setOpenEventType(CONTROL_OPEN_DEPLOYMENT_DETAIL_VIEW));
    }

    private void addIncidentsStatistics() {
        this.viewAnalyticstDataMap.put(IncidentDataListView.class, new ViewEventData()
                .setOpenEventType(CONTROL_OPEN_INCIDENTS_LIST_VIEW)
                .setGridActionEventData(new GridActionEventData("incidentsDataGrid",
                        Map.of("refresh", CONTROL_REFRESH_INCIDENT_LIST))));

        this.viewAnalyticstDataMap.put(IncidentDataDetailView.class, new ViewEventData()
                .setOpenEventType(CONTROL_OPEN_INCIDENT_DETAIL_VIEW));

        this.viewAnalyticstDataMap.put(RetryExternalTaskView.class, new ViewEventData()
                .setSaveCloseEventType(CONTROL_RETRY_INCIDENT));
        this.viewAnalyticstDataMap.put(RetryJobView.class, new ViewEventData()
                .setSaveCloseEventType(CONTROL_RETRY_INCIDENT));
        this.viewAnalyticstDataMap.put(BulkRetryIncidentView.class, new ViewEventData()
                .setSaveCloseEventType(CONTROL_RETRY_INCIDENT));
    }

    private void addUserTasksStatistics() {
        this.viewAnalyticstDataMap.put(AllTasksView.class, new ViewEventData()
                .setOpenEventType(CONTROL_OPEN_USER_TASK_LIST_VIEW)
                .setGridActionEventData(new GridActionEventData("tasksDataGrid",
                        Map.of("refresh", CONTROL_REFRESH_USER_TASK_LIST))));

        this.viewAnalyticstDataMap.put(UserTaskDataDetailView.class, new ViewEventData()
                .setOpenEventType(CONTROL_OPEN_USER_TASK_DETAIL_VIEW));

        this.viewAnalyticstDataMap.put(TaskReassignView.class, new ViewEventData()
                .setSaveCloseEventType(CONTROL_REASSIGN_USER_TASK));
        this.viewAnalyticstDataMap.put(BulkTaskCompleteView.class, new ViewEventData()
                .setSaveCloseEventType(CONTROL_COMPLETE_USER_TASK));
        this.viewAnalyticstDataMap.put(TaskCompleteView.class, new ViewEventData()
                .setSaveCloseEventType(CONTROL_COMPLETE_USER_TASK));
    }

    private void addProcessInstanceStatistics() {
        this.viewAnalyticstDataMap.put(ProcessInstanceListView.class, new ViewEventData()
                .setOpenEventType(CONTROL_OPEN_PROCESS_INSTANCE_LIST_VIEW)
                .setGridActionEventData(new GridActionEventData("processInstancesGrid", Map.of("refresh",
                        CONTROL_REFRESH_PROCESS_INSTANCE_LIST))));

        this.viewAnalyticstDataMap.put(ProcessInstanceDetailView.class, new ViewEventData()
                .setOpenEventType(CONTROL_OPEN_PROCESS_INSTANCE_DETAIL_VIEW));

        this.viewAnalyticstDataMap.put(ProcessInstanceTerminateView.class, new ViewEventData()
                .setSaveCloseEventType(CONTROL_TERMINATE_PROCESS_INSTANCE));
        this.viewAnalyticstDataMap.put(BulkTerminateProcessInstanceView.class, new ViewEventData()
                .setSaveCloseEventType(CONTROL_TERMINATE_PROCESS_INSTANCE));

        this.viewAnalyticstDataMap.put(ActivateProcessInstanceView.class, new ViewEventData()
                .setSaveCloseEventType(CONTROL_ACTIVATE_PROCESS_INSTANCE));
        this.viewAnalyticstDataMap.put(BulkActivateProcessInstanceView.class, new ViewEventData()
                .setSaveCloseEventType(CONTROL_ACTIVATE_PROCESS_INSTANCE));

        this.viewAnalyticstDataMap.put(SuspendProcessInstanceView.class, new ViewEventData()
                .setSaveCloseEventType(CONTROL_SUSPEND_PROCESS_INSTANCE));
        this.viewAnalyticstDataMap.put(BulkSuspendProcessInstanceView.class, new ViewEventData()
                .setSaveCloseEventType(CONTROL_SUSPEND_PROCESS_INSTANCE));
    }

    private void addProcessDefinitionStatistics() {
        this.viewAnalyticstDataMap.put(ProcessDefinitionListView.class, new ViewEventData()
                .setOpenEventType(CONTROL_OPEN_PROCESS_DEFINITION_LIST_VIEW)
                .setGridActionEventData(new GridActionEventData("processDefinitionsGrid",
                        Map.of("refresh", CONTROL_REFRESH_PROCESS_LIST))));

        this.viewAnalyticstDataMap.put(ProcessDefinitionDetailView.class, new ViewEventData()
                .setOpenEventType(CONTROL_OPEN_PROCESS_DEFINITION_DETAIL_VIEW));

        this.viewAnalyticstDataMap.put(StartProcessWithVariableView.class, new ViewEventData()
                .setSaveCloseEventType(CONTROL_START_PROCESS));
        this.viewAnalyticstDataMap.put(NewProcessDeploymentView.class, new ViewEventData()
                .setSaveCloseEventType(CONTROL_DEPLOY_PROCESS));

        this.viewAnalyticstDataMap.put(SuspendProcessDefinitionView.class, new ViewEventData()
                .setSaveCloseEventType(CONTROL_SUSPEND_PROCESS));
        this.viewAnalyticstDataMap.put(BulkSuspendProcessDefinitionView.class, new ViewEventData()
                .setSaveCloseEventType(CONTROL_SUSPEND_PROCESS));

        this.viewAnalyticstDataMap.put(ActivateProcessDefinitionView.class, new ViewEventData()
                .setSaveCloseEventType(CONTROL_ACTIVATE_PROCESS));
        this.viewAnalyticstDataMap.put(BulkActivateProcessDefinitionView.class, new ViewEventData()
                .setSaveCloseEventType(CONTROL_ACTIVATE_PROCESS));

        this.viewAnalyticstDataMap.put(DeleteProcessDefinitionView.class, new ViewEventData()
                .setSaveCloseEventType(CONTROL_DELETE_PROCESS));
        this.viewAnalyticstDataMap.put(BulkDeleteProcessDefinitionView.class, new ViewEventData()
                .setSaveCloseEventType(CONTROL_DELETE_PROCESS));

        this.viewAnalyticstDataMap.put(ProcessInstanceMigrationView.class, new ViewEventData()
                .setSaveCloseEventType(CONTROL_MIGRATE_PROCESS));
    }

    private void addJobsStatistics() {
        this.viewAnalyticstDataMap.put(ActivateJobView.class, new ViewEventData()
                .setOpenEventType(CONTROL_OPEN_JOB_ACTIVATE)
                .setSaveCloseEventType(CONTROL_ACTIVATE_JOB));

        this.viewAnalyticstDataMap.put(SuspendJobView.class, new ViewEventData()
                .setOpenEventType(CONTROL_OPEN_JOB_SUSPEND)
                .setSaveCloseEventType(CONTROL_SUSPEND_JOB));
    }




    protected void addSaveCloseListener(View<?> view, AmplitudeEventType eventType) {
        ViewControllerUtils.addAfterCloseListener(view, event -> {
            if (event.closedWith(StandardOutcome.SAVE)) {
                analyticsService.logEvent(eventType);
            }
        });
    }

    private void addGridActionListeners(View<?> source, GridActionEventData gridActionEventData) {
        String gridId = gridActionEventData.getGridId();
        Optional<com.vaadin.flow.component.Component> componentOpt = UiComponentUtils.findComponent(source, gridId);
        if (componentOpt.isEmpty()) {
            log.warn("Grid not found by id {} in view {}", gridId, source.getClass());
            return;
        }
        DataGrid<?> dataGrid = (DataGrid<?>) componentOpt.get();
        Map<String, AmplitudeEventType> actionEvents = gridActionEventData.getActionEvents();
        actionEvents.forEach((actionId, amplitudeEventType) -> {
            Action action = dataGrid.getAction(actionId);
            if (action instanceof ListDataComponentAction<?, ?> listAction) {
                listAction.addActionPerformedListener(actionPerformedEvent -> {
                    listAction.execute();
                    analyticsService.logEvent(amplitudeEventType);
                });
            }
        });
    }

    protected void addCustomViewActionListeners(View<?> source, String actionId, AmplitudeEventType amplitudeEventType) {
        ViewActions viewActions = ViewControllerUtils.getViewActions(source);
        Action action = viewActions.getAction(actionId);
        if (action != null) {
            if (action instanceof BaseAction baseAction) {
                baseAction.addActionPerformedListener(actionPerformedEvent -> {
                    baseAction.actionPerform(actionPerformedEvent.getComponent());
                    analyticsService.logEvent(amplitudeEventType);
                });
            }
        } else {
            log.warn("Action {} no found in view {}", actionId, source.getClass());
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    protected static class ViewEventData {
        private AmplitudeEventType openEventType;
        private AmplitudeEventType saveCloseEventType;

        private GridActionEventData gridActionEventData;
        private Map<String, AmplitudeEventType> customActionEvents;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    protected static class GridActionEventData {
        private String gridId;
        private Map<String, AmplitudeEventType> actionEvents;
    }
}
