/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.job.detail;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.job.JobDataDetailDialog;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.JobsTabFragment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.JobsTabFragment.ID_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.JobsTabFragment.JOB_ID_COLUMN_INDEX;
import static io.jmix.masquerade.JConditions.valueContains;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Job detail view (dialog mode)")
public class JobDetailViewUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Job detail dialog of failed job shows error message and stack trace")
    void givenFailedJob_whenOpenJobDetailDialog_thenErrorMessageAndStackTraceShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").getFirst();
        String jobId = dataManager.getJobsByKey("testFailedJobIncident").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        JobsTabFragment jobsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeJobsTab();

        jobsTab.getRowByJobId(jobId)
                .getCellByIndex(JOB_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY)
                .click();

        // then
        JobDataDetailDialog detailDialog = $j(JobDataDetailDialog.class)
                .exists()
                .displayed();
        detailDialog.getExceptionMessageField()
                .shouldHave(valueContains("Some service not available"));
        detailDialog.getStackTraceField()
                .shouldHave(valueContains("ScriptEvaluationException: Unable to evaluate script"));
    }
}
