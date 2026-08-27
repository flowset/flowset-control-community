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
import io.flowset.control.test_support.ui.view.processinstance.action.BulkTerminateProcessInstanceDialog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.text;
import static io.flowset.control.test_support.ui.TagNames.NOTIFICATION_CARD;
import static io.flowset.control.test_support.ui.condition.ControlCondition.emptyGrid;
import static io.flowset.control.test_support.ui.condition.ControlCondition.visibleBodyRowCount;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Bulk Terminate action on Process instance list view")
public class ProcessInstanceListViewBulkTerminateUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Terminate action is enabled for selected active instances")
    void givenActiveProcessInstance_whenSelectGridRow_thenBulkTerminateActionEnabled() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();

        // then
        listView.getBulkTerminateBtn()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        GridContextMenu gridContextMenu = listView.openInstancesGridContextActions();
        gridContextMenu.find(text("Terminate"))
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        gridContextMenu.close();

        listView.getProcessInstancesGrid().clickSelectAll();

        listView.getBulkTerminateBtn()
                .shouldBe(ENABLED);

        listView.openInstancesGridContextActions()
                .find(text("Terminate"))
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Terminate action is disabled for selected completed process instances")
    void givenCompletedProcessInstance_whenSelectGridRow_thenBulkTerminateActionDisabled() {
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
        listView.getBulkTerminateBtn()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        listView.openInstancesGridContextActions()
                .find(text("Terminate"))
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);
    }

    @Test
    @DisplayName("Terminate action: selected instances are removed from the active grid after confirmation")
    void givenActiveProcessInstance_whenBulkTerminateConfirmed_thenInstancesRemovedFromDataGrid() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();
        listView.getProcessInstancesGrid().clickSelectAll();
        listView.getBulkTerminateBtn().click();

        $j(BulkTerminateProcessInstanceDialog.class)
                .exists()
                .displayed()
                .getOkBtn().click();

        camundaRestTestHelper.waitForBatchExecution(camunda7);
        listView.getRefreshBtn().click();

        // then
        listView.getProcessInstancesGrid()
                .shouldBe(emptyGrid);
    }

    @Test
    @DisplayName("Terminate action: selected instances are not changed after cancellation")
    void givenActiveProcessInstance_whenBulkTerminateCancelled_thenInstancesNotRemovedFromDataGrid() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();
        listView.getProcessInstancesGrid().clickSelectAll();
        listView.getBulkTerminateBtn().click();

        $j(BulkTerminateProcessInstanceDialog.class)
                .exists()
                .displayed()
                .getCancelBtn().click();

        // then
        listView.getProcessInstancesGrid()
                .shouldHave(visibleBodyRowCount(1));
    }

    @Test
    @DisplayName("Terminate action: success notification is shown after confirmation")
    void givenActiveProcessInstance_whenBulkTerminateConfirmed_thenSuccessNotificationShown() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();
        listView.getProcessInstancesGrid().clickSelectAll();
        listView.getBulkTerminateBtn().click();

        $j(BulkTerminateProcessInstanceDialog.class)
                .exists()
                .displayed()
                .getOkBtn().click();

        // then
        BatchNotificationContentFragment notification = $j(BatchNotificationContentFragment.class, NOTIFICATION_CARD)
                .exists()
                .displayed();

        notification.getTitleText()
                .shouldBe(VISIBLE)
                .shouldHave(text("Terminating process instances started"));
        notification.getBatchDescription()
                .shouldBe(VISIBLE)
                .shouldHave(text("Refresh data or view progress in Batch details"));
    }
}
