/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.usertask.detail;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.usertask.UserTaskDataDetailDialog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.exactValue;
import static io.jmix.masquerade.JConditions.VISIBLE;

@WithRunningExternalEngine
@DisplayName("Fields of User task detail view")
public class UserTaskDetailViewUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Detail view shows the data of an active user task")
    void givenActiveUserTask_whenOpenDetailDialog_thenTaskDataShown() {
        // given
        String taskId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUserTaskWithFollowUpDate.bpmn")
                .startByKey("userTaskWithFollowUpDate")
                .getUserTasksByKey("userTaskWithFollowUpDate")
                .getFirst();
        camundaRestTestHelper.setTaskAssignee(camunda7, taskId, "manager");

        MainView mainView = loginAsAdmin();

        // when
        UserTaskDataDetailDialog dialog = mainView.openUserTaskListView()
                .openDetailView(taskId);

        // then
        dialog.getTaskDefinitionKeyField().shouldHave(exactValue("taskWithFollowUpDate"));
        dialog.getNameField().shouldHave(exactValue("Task with follow-up date"));
        dialog.getAssigneeField().shouldHave(exactValue("manager"));
        dialog.getTaskIdField().shouldHave(exactValue(taskId));
        dialog.getFollowUpField().shouldNotHave(exactValue(""));
        dialog.getLastUpdatedField().shouldNotHave(exactValue(""));
        dialog.getEndTimeField().shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Detail view shows the data of a completed user task")
    void givenCompletedUserTask_whenOpenDetailDialog_thenTaskDataShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUserTaskWithFollowUpDate.bpmn")
                .startByKey("userTaskWithFollowUpDate");
        String instanceId = dataManager.getStartedInstances("userTaskWithFollowUpDate").getFirst();
        String taskId = dataManager.getUserTasksByKey("userTaskWithFollowUpDate").getFirst();
        camundaRestTestHelper.completeTaskById(camunda7, taskId);

        MainView mainView = loginAsAdmin();

        // when
        UserTaskDataDetailDialog dialog = mainView.openProcessInstanceListView()
                .switchToAllViewMode()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryUserTasksTab()
                .openDetailView(taskId);

        // then
        dialog.getTaskDefinitionKeyField().shouldHave(exactValue("taskWithFollowUpDate"));
        dialog.getNameField().shouldHave(exactValue("Task with follow-up date"));
        dialog.getAssigneeField().shouldHave(exactValue("admin"));
        dialog.getTaskIdField().shouldHave(exactValue(taskId));
        dialog.getFollowUpField().shouldNotHave(exactValue(""));
        dialog.getEndTimeField().shouldNotHave(exactValue(""));
        dialog.getLastUpdatedField().shouldNotBe(VISIBLE);
    }
}
