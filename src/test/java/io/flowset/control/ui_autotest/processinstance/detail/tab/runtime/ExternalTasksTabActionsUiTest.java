/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.detail.tab.runtime;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.externaltask.ExternalTaskDataDetailDialog;
import io.flowset.control.test_support.ui.view.externaltask.RetryExternalTaskDialog;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.ExternalTasksTabFragment;
import io.jmix.masquerade.component.Notification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.value;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.ExternalTasksTabFragment.*;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions in the External tasks tab (Runtime tab) in Process instance detail view")
public class ExternalTasksTabActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Retry action is enabled for failed external task")
    void givenExistingFailedExternalTask_whenOpenExternalTasksTab_thenRetryActionEnabled() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedExternalTask.bpmn")
                .startByKey("testFailedExternalTask")
                .failExternalTasksByKey("testFailedExternalTask");

        String instanceId = dataManager.getStartedInstances("testFailedExternalTask").get(0);
        String externalTaskId = dataManager.getExternalTasksByKey("testFailedExternalTask").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ExternalTasksTabFragment externalTasksTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeExternalTasksTab();

        // then
        externalTasksTab.getRetryButton()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        externalTasksTab.selectRowByExternalTaskId(externalTaskId);

        externalTasksTab.getRetryButton()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Retry action is not enabled for non-failed external task")
    void givenExistingActiveExternalTask_whenOpenExternalTasksTabAndSelectTask_thenRetryActionNotEnabled() {
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

        // then
        externalTasksTab.getRetryButton()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        externalTasksTab.selectRowByExternalTaskId(externalTaskId);

        externalTasksTab.getRetryButton()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);
    }

    @Test
    @DisplayName("Retry action: external task retries are updated in data grid after confirmation")
    void givenExistingFailedExternalTask_whenRetryConfirmed_thenExternalTaskRetriesUpdated() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedExternalTask.bpmn")
                .startByKey("testFailedExternalTask")
                .failExternalTasksByKey("testFailedExternalTask");
        String instanceId = dataManager.getStartedInstances("testFailedExternalTask").get(0);
        String externalTaskId = dataManager.getExternalTasksByKey("testFailedExternalTask").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ExternalTasksTabFragment externalTasksTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeExternalTasksTab();

        externalTasksTab.selectRowByExternalTaskId(externalTaskId);
        externalTasksTab.getRetryButton().click();

        RetryExternalTaskDialog dialog = $j(RetryExternalTaskDialog.class).exists()
                .displayed();
        dialog.getRetriesField().setValue("5");
        dialog.getRetryBtn().click();

        // then
        externalTasksTab.getRowByExternalTaskId(externalTaskId)
                .getCellByIndex(RETRIES_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("5"));
    }

    @Test
    @DisplayName("Retry action: external task retries are not updated in data grid after cancellation")
    void givenExistingFailedExternalTask_whenRetryCancelled_thenExternalTaskRetriesUnchanged() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedExternalTask.bpmn")
                .startByKey("testFailedExternalTask")
                .failExternalTasksByKey("testFailedExternalTask");

        String instanceId = dataManager.getStartedInstances("testFailedExternalTask").get(0);
        String externalTaskId = dataManager.getExternalTasksByKey("testFailedExternalTask").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ExternalTasksTabFragment externalTasksTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeExternalTasksTab();

        externalTasksTab.selectRowByExternalTaskId(externalTaskId);
        externalTasksTab.getRetryButton().click();

        RetryExternalTaskDialog dialog = $j(RetryExternalTaskDialog.class).exists()
                .displayed();
        dialog.getRetriesField().setValue("5");
        dialog.getCancelBtn().click();

        // then
        externalTasksTab.getRowByExternalTaskId(externalTaskId)
                .getCellByIndex(RETRIES_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("0"));
    }

    @Test
    @DisplayName("Double-clicking an external-task row opens External task detail dialog")
    void givenExistingExternalTask_whenRowDoubleClicked_thenExternalTaskDetailDialogOpened() {
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
                .getDelegate()
                .doubleClick();

        // then
        ExternalTaskDataDetailDialog dialog = $j(ExternalTaskDataDetailDialog.class).exists()
                .displayed();

        dialog.getExternalTaskIdField().shouldHave(value(externalTaskId));
    }

    @Test
    @DisplayName("Copy id action shows notification on External task detail dialog")
    void givenExistingExternalTask_whenCopyIdActionClickedOnDialog_thenNotificationShown() {
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
                .getDelegate()
                .doubleClick();

        ExternalTaskDataDetailDialog dialog = $j(ExternalTaskDataDetailDialog.class).exists().displayed();
        dialog.getCopyIdBtn().click();

        // then
        $j(Notification.class)
                .shouldBe(VISIBLE)
                .shouldHave(notificationTheme(Notification.Theme.SUCCESS))
                .shouldHave(text("Value copied!"));
    }

    @Test
    @DisplayName("Copy error action shows notification on External task detail dialog")
    void givenExistingFailedExternalTask_whenCopyErrorActionClickedOnDialog_thenNotificationShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedExternalTask.bpmn")
                .startByKey("testFailedExternalTask")
                .failExternalTasksByKey("failedExternalTask");
        String instanceId = dataManager.getStartedInstances("testFailedExternalTask").get(0);
        String externalTaskId = dataManager.getExternalTasksByKey("testFailedExternalTask").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ExternalTasksTabFragment externalTasksTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeExternalTasksTab();

        externalTasksTab.getRowByExternalTaskId(externalTaskId)
                .getDelegate()
                .doubleClick();

        ExternalTaskDataDetailDialog dialog = $j(ExternalTaskDataDetailDialog.class).exists().displayed();
        dialog.getCopyErrorBtn().click();

        // then
        $j(Notification.class)
                .shouldBe(VISIBLE)
                .shouldHave(notificationTheme(Notification.Theme.SUCCESS))
                .shouldHave(text("Value copied!"));
    }

    @Test
    @DisplayName("Copy error details action shows notification on External task detail dialog")
    void givenExistingFailedExternalTask_whenCopyErrorDetailsActionClickedOnDialog_thenNotificationShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedExternalTask.bpmn")
                .startByKey("testFailedExternalTask")
                .failExternalTasksByKey("failedExternalTask");
        String instanceId = dataManager.getStartedInstances("testFailedExternalTask").get(0);
        String externalTaskId = dataManager.getExternalTasksByKey("testFailedExternalTask").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ExternalTasksTabFragment externalTasksTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeExternalTasksTab();

        externalTasksTab.getRowByExternalTaskId(externalTaskId)
                .getDelegate()
                .doubleClick();

        ExternalTaskDataDetailDialog dialog = $j(ExternalTaskDataDetailDialog.class).exists().displayed();
        dialog.getCopyErrorDetailsBtn().click();

        // then
        $j(Notification.class)
                .shouldBe(VISIBLE)
                .shouldHave(notificationTheme(Notification.Theme.SUCCESS))
                .shouldHave(text("Value copied!"));
    }

    @Test
    @DisplayName("External task ID link in data grid opens External task detail dialog")
    void givenExistingExternalTask_whenExternalTaskIdLinkClicked_thenExternalTaskDetailDialogOpened() {
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
                .find(ID_BUTTON_BY).click();

        // then
        ExternalTaskDataDetailDialog dialog = $j(ExternalTaskDataDetailDialog.class).exists()
                .displayed();

        dialog.getExternalTaskIdField().shouldHave(value(externalTaskId));
    }

    @Test
    @DisplayName("List of available grid actions on External tasks tab")
    void givenExistingProcessInstance_whenOpenDetailView_thenAllIncidentsActionsAvailable() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedExternalTask.bpmn")
                .startByKey("testFailedExternalTask");
        String instanceId = dataManager.getStartedInstances("testFailedExternalTask").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ExternalTasksTabFragment externalTasksTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeExternalTasksTab();

        // then
        externalTasksTab.getRetryButton().shouldBe(VISIBLE);

        externalTasksTab.openTasksGridContextMenu()
                .shouldHave(visibleItems("Retry"));
    }

    @Test
    @DisplayName("Retry action on External task detail dialog: updates retries in data grid after confirmation")
    void givenExistingFailedExternalTask_whenRetryConfirmedOnDetailDialog_thenExternalTaskRetriesUpdatedInDataGrid() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedExternalTask.bpmn")
                .startByKey("testFailedExternalTask")
                .failExternalTasksByKey("testFailedExternalTask");
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
        detailDialog.getRetryBtn().click();

        RetryExternalTaskDialog dialog = $j(RetryExternalTaskDialog.class)
                .exists()
                .displayed();
        dialog.getRetriesField().setValue("5");
        dialog.getRetryBtn().click();

        // then
        externalTasksTab.getRowByExternalTaskId(externalTaskId)
                .getCellByIndex(RETRIES_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("5"));
    }

}
