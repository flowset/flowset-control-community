/*
 * Copyright (c) Haulmont 1016. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processdefinition.detail.tab;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessDefinitionDetailView;
import io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessInstancesTabFragment;
import io.flowset.control.test_support.ui.view.processinstance.action.BulkTerminateProcessInstanceDialog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.text;
import static io.flowset.control.test_support.ui.condition.ControlCondition.emptyGrid;
import static io.flowset.control.test_support.ui.condition.ControlCondition.visibleBodyRowCount;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Terminate action on Process instances tab in process detail view")
public class ProcessInstancesTabBulkTerminateUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    private CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Terminate action: process instances tab is updated after confirmation")
    void givenExistingProcessInstances_whenTerminateConfirmedOnDetailView_thenProcessInstancesTabUpdated() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");
        ProcessInstancesTabFragment processInstancesTab = detailView.openProcessInstancesTab();

        processInstancesTab.getProcessInstancesGrid().clickSelectAll();
        processInstancesTab.getTerminateButton().click();

        $j(BulkTerminateProcessInstanceDialog.class)
                .exists()
                .displayed()
                .getOkBtn()
                .click();

        camundaRestTestHelper.waitForBatchExecution(camunda7);
        processInstancesTab.getRefreshButton().click();

        // then
        detailView.getTabs()
                .getTabById("processInstancesTab")
                .getDelegate()
                .shouldHave(text("Process instances (0)"));

        processInstancesTab.getProcessInstancesGrid()
                .shouldBe(emptyGrid);
    }

    @Test
    @DisplayName("Terminate action: instances count statistics are updated after confirmation")
    void givenExistingProcessInstances_whenTerminateConfirmedOnDetailView_thenInstancesCountStatisticsUpdated() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");
        ProcessInstancesTabFragment processInstancesTab = detailView.openProcessInstancesTab();

        processInstancesTab.getProcessInstancesGrid().clickSelectAll();
        processInstancesTab.getTerminateButton().click();

        $j(BulkTerminateProcessInstanceDialog.class)
                .exists()
                .displayed()
                .getOkBtn()
                .click();

        camundaRestTestHelper.waitForBatchExecution(camunda7);
        detailView.getGeneralPanel().getRefreshBtn().click();

        // then
        detailView.getAllVersionsInstancesCountValue()
                .shouldHave(text("0"));

        detailView.getBpmnViewerFragment()
                .getActivityStatisticsOverlay("approveVacationTask")
                .shouldNot(exist);
    }

    @Test
    @DisplayName("Terminate action: process instances tab is unchanged after cancellation")
    void givenExistingProcessInstances_whenTerminateCancelledOnDetailView_thenProcessInstancesTabUnchanged() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");
        ProcessInstancesTabFragment processInstancesTab = detailView.openProcessInstancesTab();

        processInstancesTab.getProcessInstancesGrid().clickSelectAll();
        processInstancesTab.getTerminateButton().click();

        $j(BulkTerminateProcessInstanceDialog.class)
                .exists()
                .displayed()
                .getCancelBtn().click();

        // then
        detailView.getTabs()
                .getTabById("processInstancesTab")
                .getDelegate()
                .shouldHave(text("Process instances (1)"));

        processInstancesTab.getProcessInstancesGrid()
                .shouldHave(visibleBodyRowCount(1));
    }

    @Test
    @DisplayName("Terminate action: instances count statistics is unchanged after cancellation")
    void givenExistingProcessInstances_whenTerminateCancelledOnDetailView_thenInstancesCountStatisticsUnchanged() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");
        ProcessInstancesTabFragment processInstancesTab = detailView.openProcessInstancesTab();

        processInstancesTab.getProcessInstancesGrid().clickSelectAll();
        processInstancesTab.getTerminateButton().click();

        $j(BulkTerminateProcessInstanceDialog.class)
                .exists()
                .displayed()
                .getCancelBtn().click();

        // then
        detailView.getAllVersionsInstancesCountValue()
                .shouldHave(text("1"));

        detailView.getBpmnViewerFragment()
                .getActivityStatisticsOverlay("approveVacationTask")
                .shouldHave(text("1"));
    }
}
