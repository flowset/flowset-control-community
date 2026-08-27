/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.usertask.detail;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.response.RuntimeUserTaskDto;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessDefinitionDetailView;
import io.flowset.control.test_support.ui.view.processinstance.detail.ProcessInstanceDetailView;
import io.flowset.control.test_support.ui.view.usertask.UserTaskDataDetailDialog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Selenide.switchTo;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.numberOfWindows;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Navigation actions on User task detail view (dialog mode)")
public class UserTaskDetailViewNavigationActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Navigation button in the Process instance field opens detail view in new tab")
    void givenActiveUserTask_whenViewProcessInstanceLinkClicked_thenInstanceDetailViewOpenedInNewTab() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        String taskId = dataManager.getUserTasksByKey("vacation_approval").get(0);
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        UserTaskDataDetailDialog detailDialog = mainView.openUserTaskListView()
                .openDetailView(taskId);

        detailDialog.getViewProcessInstanceBtn().click();

        // then
        webdriver().shouldHave(numberOfWindows(2));
        switchTo().window(1);

        webdriver().shouldHave(urlContaining("/bpm/process-instances/" + instanceId));

        $j(ProcessInstanceDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Navigation button in the Process field opens detail view in new tab")
    void givenActiveUserTask_whenViewProcessLinkClicked_thenProcessDetailViewOpenedInNewTab() {
        // given
        CamundaSampleDataManager camundaManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        String taskId = camundaManager.getUserTasksByKey("vacation_approval").get(0);
        String processId = camundaManager.getDeployedProcessVersions("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        UserTaskDataDetailDialog detailDialog = mainView.openUserTaskListView()
                .openDetailView(taskId);

        detailDialog.getViewProcessDefinitionBtn().click();

        // then
        webdriver().shouldHave(numberOfWindows(2));
        switchTo().window(1);

        webdriver().shouldHave(urlContaining("/bpm/process-definitions/" + processId));

        $j(ProcessDefinitionDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Navigation button in the Process field is hidden if process is not set")
    void givenStandaloneUserTask_whenOpenDetailDialog_thenViewProcessActionHidden() {
        // given
        RuntimeUserTaskDto userTask = camundaRestTestHelper.createUserTask(camunda7,
                "User task without process", "admin");

        MainView mainView = loginAsAdmin();

        // when
        UserTaskDataDetailDialog dialog = mainView.openUserTaskListView()
                .openDetailView(userTask.getId());

        // then
        dialog.getViewProcessDefinitionBtn().shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Navigation button in the Process instance field is hidden if process instance is not set")
    void givenStandaloneUserTask_whenOpenDetailDialog_thenViewProcessInstanceActionHidden() {
        // given
        RuntimeUserTaskDto userTask = camundaRestTestHelper.createUserTask(camunda7,
                "User task without process", "admin");

        MainView mainView = loginAsAdmin();

        // when
        UserTaskDataDetailDialog dialog = mainView.openUserTaskListView()
                .openDetailView(userTask.getId());

        // then
        dialog.getViewProcessInstanceBtn().shouldNotBe(VISIBLE);
    }
}
