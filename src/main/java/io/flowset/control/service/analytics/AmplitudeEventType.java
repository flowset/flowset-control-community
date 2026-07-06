package io.flowset.control.service.analytics;

import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

public enum AmplitudeEventType {

    // View opening
    CONTROL_OPEN_DECISION_TABLE_LIST_VIEW("control_open_decision_table_list_view"),
    CONTROL_OPEN_DECISION_TABLE_DETAIL_VIEW("control_open_decision_table_detail_view"),
    CONTROL_OPEN_DEPLOYMENT_LIST_VIEW("control_open_deployment_list_view"),
    CONTROL_OPEN_DEPLOYMENT_DETAIL_VIEW("control_open_deployment_detail_view"),
    CONTROL_OPEN_PROCESS_INSTANCE_LIST_VIEW("control_open_process_instance_list_view"),
    CONTROL_OPEN_PROCESS_INSTANCE_DETAIL_VIEW("control_open_process_instance_detail_view"),
    CONTROL_OPEN_PROCESS_DEFINITION_LIST_VIEW("control_open_process_definition_list_view"),
    CONTROL_OPEN_PROCESS_DEFINITION_DETAIL_VIEW("control_open_process_definition_detail_view"),
    CONTROL_OPEN_USER_TASK_LIST_VIEW("control_open_user_task_list_view"),
    CONTROL_OPEN_USER_TASK_DETAIL_VIEW("control_open_user_task_detail_view"),
    CONTROL_OPEN_INCIDENTS_LIST_VIEW("control_open_incidents_list_view"),
    CONTROL_OPEN_INCIDENT_DETAIL_VIEW("control_open_incident_detail_view"),
    CONTROL_OPEN_BPM_ENGINES_LIST_VIEW("control_open_bpm_engines_list_view"),
    CONTROL_OPEN_DASHBOARD_VIEW("control_open_dashboard_view"),
    CONTROL_OPEN_USER_LIST_VIEW("control_open_user_list_view"),
    CONTROL_OPEN_USER_DETAIL_VIEW("control_open_user_detail_view"),
    CONTROL_OPEN_BPM_ENGINE_DIALOG("control_open_bpm_engine_dialog"),
    CONTROL_OPEN_JOB_ACTIVATE("control_open_job_activate"),
    CONTROL_OPEN_JOB_SUSPEND("control_open_job_suspend"),
    CONTROL_OPEN_DECISION_INSTANCE_LIST_VIEW("control_open_decision_instance_list_view"),
    CONTROL_OPEN_DECISION_INSTANCE_DETAIL_VIEW("control_open_decision_instance_detail_view"),
    CONTROL_OPEN_ABOUT_VIEW("control_open_about_view"),

    CONTROL_OPEN_RESOURCES_ROLE_LIST_VIEW("control_open_resources_role_list_view"),
    CONTROL_OPEN_ROW_LEVEL_ROLE_LIST_VIEW("control_open_row_level_role_list_view"),
    CONTROL_OPEN_RESOURCE_ROLE_DETAIL_VIEW("control_open_resource_role_detail_view"),
    CONTROL_OPEN_ROW_LEVEL_ROLE_DETAIL_VIEW("control_open_row_level_role_detail_view"),

    // Actions
    CONTROL_START_PROCESS("control_start_process"),
    CONTROL_DEPLOY_PROCESS("control_deploy_process"),
    CONTROL_SUSPEND_PROCESS("control_suspend_process"),
    CONTROL_ACTIVATE_PROCESS("control_activate_process"),
    CONTROL_REFRESH_PROCESS_LIST("control_refresh_process_list"),
    CONTROL_DELETE_PROCESS("control_delete_process"),
    CONTROL_MIGRATE_PROCESS("control_migrate_process"),
    CONTROL_OPEN_BPM_ENGINE_DETAIL_VIEW("control_open_bpm_engine_detail_view"),

    CONTROL_RETRY_INCIDENT("control_retry_incident"),
    CONTROL_REASSIGN_USER_TASK("control_reassign_user_task"),
    CONTROL_COMPLETE_USER_TASK("control_complete_user_task"),

    CONTROL_ACTIVATE_JOB("control_activate_job"),
    CONTROL_SUSPEND_JOB("control_suspend_job"),
    CONTROL_SELECT_ENGINE("control_select_engine"),
    CONTROL_OPEN_SELECT_ENGINE_POPOVER("control_open_select_engine_popover"),

    CONTROL_TEST_CONNECTION_BPM_ENGINE("control_test_connection_bpm_engine"),
    CONTROL_REFRESH_INCIDENT_LIST("control_refresh_incident_list"),
    CONTROL_TERMINATE_PROCESS_INSTANCE("control_terminate_process_instance"),
    CONTROL_ACTIVATE_PROCESS_INSTANCE("control_activate_process_instance"),
    CONTROL_SUSPEND_PROCESS_INSTANCE("control_suspend_process_instance"),
    CONTROL_REFRESH_PROCESS_INSTANCE_LIST("control_refresh_process_instance_list"),
    CONTROL_CREATE_BPM_ENGINE("control_create_bpm_engine"),

    CONTROL_REMOVE_BPM_ENGINE("control_remove_bpm_engine"),
    CONTROL_REFRESH_USER_TASK_LIST("control_refresh_user_task_list"),

    CONTROL_LOGIN("control_login"),
    CONTROL_UPDATE_TENANT("control_update_tenant"),
    CONTROL_CHANGE_LOCALE("control_change_locale"),
    CONTROL_DISABLE_ANALYTICS("control_disable_analytics"),

    CONTROL_OPEN_CALLED_INSTANCES_VIEW("control_open_called_instances_view"),
    CONTROL_OPEN_CALLED_PROCESS_VIEW("control_open_called_process_view"),
    CONTROL_OPEN_EXTERNAL_DASHBOARD("control_open_external_dashboard"),

    CONTROL_NAVIGATE_TO_DECISION_TABLE("control_navigate_to_decision_table"),
    CONTROL_OPEN_ACTUATOR_METRIC_TAGS_DETAIL_VIEW("control_open_actuator_metric_tags_detail_view"),
    CONTROL_OPEN_ENGINE_METRICS_VALUES_DIALOG("control_open_engine_metrics_values_dialog"),
    CONTROL_OPEN_PROCESS_INSTANCE_PREVIEW_VIEW("control_open_process_instance_preview_view"),
    CONTROL_REFRESH_DECISION_LIST("control_refresh_decision_list"),
    CONTROL_REFRESH_DECISION_INSTANCE_LIST("control_refresh_decision_instance_list"),

    //External links
    CONTROL_OPEN_LINK_STUDIO("control_open_link_studio"),
    CONTROL_OPEN_LINK_WORKSPACE("control_open_link_workspace"),
    CONTROL_OPEN_LINK_TASKLIST("control_open_link_tasklist"),
    CONTROL_OPEN_LINK_WEBSITE("control_open_link_website"),
    CONTROL_OPEN_LINK_DOCUMENTATION("control_open_link_documentation"),
    CONTROL_OPEN_LINK_SOURCE_CODE("control_open_link_source_code"),
    CONTROL_OPEN_LINK_SLACK_SUPPORT("control_open_link_slack_support"),
    CONTROL_OPEN_LINK_LINKEDIN("control_open_link_linkedin"),
    CONTROL_REFRESH_DEPLOYMENT_LIST("control_refresh_deployment_list");

    private final String id;

    AmplitudeEventType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static AmplitudeEventType fromId(String id) {
        AmplitudeEventType[] values = AmplitudeEventType.values();
        for (AmplitudeEventType value : values) {
            if (StringUtils.equalsIgnoreCase(value.id, id)) {
                return value;
            }
        }

        return null;
    }
}
