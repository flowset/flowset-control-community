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
import io.flowset.control.test_support.ui.view.processinstance.action.BulkSuspendProcessInstanceDialog;
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
@DisplayName("Bulk Suspend action on Process instance list view")
public class ProcessInstanceListViewBulkSuspendUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Suspend action: instance state is updated in data grid after confirmation")
    void givenActiveProcessInstance_whenBulkSuspendConfirmed_thenAllSelectedInstancesSuspended() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();
        listView.getProcessInstancesGrid().clickSelectAll();
        listView.getBulkSuspendBtn().click();

        $j(BulkSuspendProcessInstanceDialog.class)
                .exists()
                .displayed()
                .getSuspendBtn().click();

        camundaRestTestHelper.waitForBatchExecution(camunda7);
        listView.getRefreshBtn().click();

        // then
        listView.getProcessInstancesGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(STATE_COLUMN_INDEX, "Suspended"));
    }

    @Test
    @DisplayName("Suspend action: instance state is not changed in data grid after cancellation")
    void givenActiveProcessInstance_whenBulkSuspendCancelled_thenAllSelectedInstancesNotSuspended() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();
        listView.getProcessInstancesGrid().clickSelectAll();
        listView.getBulkSuspendBtn().click();

        $j(BulkSuspendProcessInstanceDialog.class)
                .exists()
                .displayed()
                .getCancelBtn().click();

        // then
        listView.getProcessInstancesGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(STATE_COLUMN_INDEX, "Running"));
    }

    @Test
    @DisplayName("Suspend action is enabled for active instances")
    void givenActiveProcessInstance_whenSelectGridRow_thenBulkSuspendActionEnabled() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();

        // then
        listView.getBulkSuspendBtn()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        GridContextMenu gridContextMenu = listView.openInstancesGridContextActions();
        gridContextMenu.find(text("Suspend"))
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        gridContextMenu.close();

        listView.getProcessInstancesGrid().clickSelectAll();

        listView.getBulkSuspendBtn()
                .shouldBe(ENABLED);

        listView.openInstancesGridContextActions()
                .find(text("Suspend"))
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Suspend action is disabled for suspended process instances")
    void givenSuspendedProcessInstance_whenSelectGridRow_thenBulkSuspendActionDisabled() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval", 2)
                .suspendByKey("vacation_approval", true);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();
        listView.waitUntilDataLoading();
        listView.getProcessInstancesGrid().clickSelectAll();

        // then
        listView.getBulkSuspendBtn()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        listView.openInstancesGridContextActions()
                .find(text("Suspend"))
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);
    }

    @Test
    @DisplayName("Suspend action is disabled for finished process instance")
    void givenCompletedProcessInstance_whenSelectGridRow_thenBulkSuspendActionDisabled() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .deploy("test_support/testCompletedInstance.bpmn")
                .startByKey("vacation_approval")
                .startByKey("testCompletedInstance")
                .waitJobsExecution();

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();
        listView.switchToAllViewMode();
        listView.getProcessInstancesGrid().clickSelectAll();

        // then
        listView.getBulkSuspendBtn()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        listView.openInstancesGridContextActions()
                .find(text("Suspend"))
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);
    }

    @Test
    @DisplayName("Suspend action: success notification is shown after confirmation")
    void givenActiveProcessInstance_whenBulkSuspendConfirmed_thenSuccessNotificationShown() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();
        listView.getProcessInstancesGrid().clickSelectAll();
        listView.getBulkSuspendBtn()
                .shouldBe(ENABLED).click();

        $j(BulkSuspendProcessInstanceDialog.class)
                .exists()
                .displayed()
                .getSuspendBtn().click();

        // then
        BatchNotificationContentFragment notification = $j(BatchNotificationContentFragment.class, NOTIFICATION_CARD)
                .exists()
                .displayed();

        notification.getTitleText()
                .shouldBe(VISIBLE)
                .shouldHave(text("Suspending process instances started"));
        notification.getBatchDescription()
                .shouldBe(VISIBLE)
                .shouldHave(text("Refresh data or view progress in Batch details"));
    }
}
