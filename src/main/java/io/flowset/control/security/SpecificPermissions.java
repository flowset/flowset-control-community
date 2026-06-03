/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.security;

/**
 * Holds names of permissions giving access to specific actions.
 */
public interface SpecificPermissions {
    // Decisions
    String DECISION_DEFINITION_DEPLOY = "decisionDefinition.deploy";

    // BPM engines
    String ENGINE_MARK_AS_DEFAULT = "engine.markAsDefault";

    // External tasks
    String EXTERNAL_TASK_RETRY = "externalTask.retry";

    // Incidents
    String INCIDENT_RETRY = "incident.retry";

    // Jobs
    String JOB_ACTIVATE = "job.activate";
    String JOB_RETRY = "job.retry";
    String JOB_SUSPEND = "job.suspend";

    // Process definitions
    String PROCESS_DEFINITION_ACTIVATE = "processDefinition.activate";
    String PROCESS_DEFINITION_DEPLOY = "processDefinition.deploy";
    String PROCESS_DEFINITION_MIGRATE = "processDefinition.migrate";
    String PROCESS_DEFINITION_START = "processDefinition.start";
    String PROCESS_DEFINITION_SUSPEND = "processDefinition.suspend";

    // Process instances
    String PROCESS_INSTANCE_ACTIVATE = "processInstance.activate";
    String PROCESS_INSTANCE_MIGRATE = "processInstance.migrate";
    String PROCESS_INSTANCE_SUSPEND = "processInstance.suspend";
    String PROCESS_INSTANCE_TERMINATE = "processInstance.terminate";

    // User tasks
    String USER_TASK_COMPLETE = "userTask.complete";
    String USER_TASK_REASSIGN = "userTask.reassign";
}
