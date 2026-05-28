/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.externaltask.detail;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.externaltask.ExternalTaskDataDetailDialog;
import io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessDefinitionDetailView;
import io.flowset.control.test_support.ui.view.processinstance.detail.ProcessInstanceDetailView;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.ExternalTasksTabFragment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Selenide.switchTo;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.numberOfWindows;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.ExternalTasksTabFragment.EXTERNAL_TASK_ID_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.ExternalTasksTabFragment.ID_BUTTON_BY;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions on External task detail view (dialog mode)")
public class ExternalTaskDetailViewActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Navigation button in the Process field opens detail view in new tab")
    void givenExistingExternalTask_whenViewProcessClicked_thenProcessDefinitionDetailOpenedInNewTab() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedExternalTask.bpmn")
                .startByKey("testFailedExternalTask");
        String instanceId = dataManager.getStartedInstances("testFailedExternalTask").get(0);
        String externalTaskId = dataManager.getExternalTasksByKey("testFailedExternalTask").get(0);
        String processId = dataManager.getDeployedProcessVersions("testFailedExternalTask").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ExternalTasksTabFragment externalTasksTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeExternalTasksTab();

        externalTasksTab.getRowByExternalTaskId(externalTaskId)
                .getCellByIndex(EXTERNAL_TASK_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY)
                .click();

        ExternalTaskDataDetailDialog detailDialog = $j(ExternalTaskDataDetailDialog.class)
                .exists()
                .displayed();
        detailDialog.getViewProcessBtn().click();

        // then
        webdriver().shouldHave(numberOfWindows(2));
        switchTo().window(1);

        webdriver().shouldHave(urlContaining("/bpm/process-definitions/" + processId));

        $j(ProcessDefinitionDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Navigation button in the Process instance field opens detail view in new tab")
    void givenExistingExternalTask_whenViewProcessInstanceClicked_thenProcessInstanceDetailOpenedInNewTab() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedExternalTask.bpmn")
                .startByKey("testFailedExternalTask");
        String instanceId = dataManager.getStartedInstances("testFailedExternalTask").get(0);
        String externalTaskId = dataManager.getExternalTasksByKey("testFailedExternalTask").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ExternalTasksTabFragment externalTasksTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeExternalTasksTab();

        externalTasksTab.getRowByExternalTaskId(externalTaskId)
                .getCellByIndex(EXTERNAL_TASK_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY)
                .click();

        ExternalTaskDataDetailDialog detailDialog = $j(ExternalTaskDataDetailDialog.class)
                .exists()
                .displayed();
        detailDialog.getViewProcessInstanceBtn().click();

        // then
        webdriver().shouldHave(numberOfWindows(2));
        switchTo().window(1);

        webdriver().shouldHave(urlContaining("/bpm/process-instances/" + instanceId));

        $j(ProcessInstanceDetailView.class)
                .exists()
                .displayed();
    }
}
