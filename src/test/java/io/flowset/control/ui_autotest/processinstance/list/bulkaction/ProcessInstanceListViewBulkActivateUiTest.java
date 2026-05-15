/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.list.bulkaction;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.batch.notification.BatchNotificationContentFragment;
import io.flowset.control.test_support.ui.view.processinstance.ProcessInstanceListView;
import io.flowset.control.test_support.ui.view.processinstance.action.BulkActivateProcessInstanceDialog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.text;
import static io.flowset.control.test_support.ui.TagNames.NOTIFICATION_CARD;
import static io.flowset.control.test_support.ui.condition.ControlCondition.allBodyRowsHaveCellText;
import static io.flowset.control.test_support.ui.condition.ControlCondition.visibleBodyRowCount;
import static io.flowset.control.test_support.ui.view.processinstance.ProcessInstanceListView.STATE_COLUMN_INDEX;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Bulk Activate action on Process instance list view")
public class ProcessInstanceListViewBulkActivateUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Activate action: instance state is updated in data grid after confirmation")
    void givenSuspendedProcessInstance_whenBulkActivateConfirmed_thenAllSelectedInstancesActivated() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .suspendByKey("vacation_approval", true);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();
        listView.getProcessInstancesGrid().clickSelectAll();
        listView.getBulkActivateBtn().click();

        $j(BulkActivateProcessInstanceDialog.class)
                .exists()
                .displayed()
                .getActivateBtn().click();

        camundaRestTestHelper.waitForBatchExecution(camunda7);
        listView.getRefreshBtn().click();

        // then
        listView.getProcessInstancesGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(STATE_COLUMN_INDEX, "Running"));
    }

    @Test
    @DisplayName("Activate action: instance state is not changed in data grid after cancellation")
    void givenSuspendedProcessInstance_whenBulkActivateCancelled_thenAllSelectedInstancesNotActivated() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .suspendByKey("vacation_approval", true);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();
        listView.getProcessInstancesGrid().clickSelectAll();
        listView.getBulkActivateBtn().click();

        $j(BulkActivateProcessInstanceDialog.class)
                .exists()
                .displayed()
                .getCancelBtn().click();

        // then
        listView.getProcessInstancesGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(STATE_COLUMN_INDEX, "Suspended"));
    }

    @Test
    @DisplayName("Activate action is enabled for suspended instances")
    void givenSuspendedProcessInstance_whenSelectGridRow_thenBulkActivateActionEnabled() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .suspendByKey("vacation_approval", true);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();

        // then
        listView.getBulkActivateBtn()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        GridContextMenu gridContextMenu = listView.openInstancesGridContextActions();
        gridContextMenu.find(text("Activate"))
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        gridContextMenu.close();

        listView.getProcessInstancesGrid().clickSelectAll();

        listView.getBulkActivateBtn()
                .shouldBe(ENABLED);

        listView.openInstancesGridContextActions()
                .find(text("Activate"))
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Activate action is  disabled for active process instances")
    void givenActiveProcessInstance_whenSelectGridRow_thenBulkActivateActionDisabled() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();
        listView.waitUntilDataLoading();
        listView.getProcessInstancesGrid().clickSelectAll();

        // then
        listView.getBulkActivateBtn()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        listView.openInstancesGridContextActions()
                .find(text("Activate"))
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);
    }

    @Test
    @DisplayName("Activate action is disabled for completed process instance")
    void givenCompletedProcessInstance_whenSelectGridRow_thenBulkActivateActionDisabled() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .deploy("test_support/testCompletedInstance.bpmn")
                .startByKey("vacation_approval")
                .startByKey("testCompletedInstance")
                .waitJobsExecution()
                .suspendByKey("vacation_approval", true);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();
        listView.switchToAllViewMode();
        listView.getProcessInstancesGrid().clickSelectAll();

        // then
        listView.getBulkActivateBtn()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        listView.openInstancesGridContextActions()
                .find(text("Activate"))
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);
    }

    @Test
    @DisplayName("Activate action: success notification is shown after confirmation")
    void givenSuspendedProcessInstance_whenBulkActivateConfirmed_thenSuccessNotificationShown() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .suspendByKey("vacation_approval", true);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();
        listView.getProcessInstancesGrid().clickSelectAll();
        listView.getBulkActivateBtn()
                .shouldBe(ENABLED).click();

        $j(BulkActivateProcessInstanceDialog.class)
                .exists()
                .displayed()
                .getActivateBtn().click();

        // then
        BatchNotificationContentFragment notification = $j(BatchNotificationContentFragment.class, NOTIFICATION_CARD)
                .exists()
                .displayed();

        notification.getTitleText()
                .shouldBe(VISIBLE)
                .shouldHave(text("Activating process instances started"));
        notification.getBatchDescription().shouldBe(VISIBLE)
                .shouldHave(text("Refresh data or view progress in Batch details"));
    }
}
