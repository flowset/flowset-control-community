/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.detail;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processinstance.ProcessInstanceMigrationDialog;
import io.flowset.control.test_support.ui.view.processinstance.action.ActivateProcessInstanceDialog;
import io.flowset.control.test_support.ui.view.processinstance.action.ProcessInstanceTerminateDialog;
import io.flowset.control.test_support.ui.view.processinstance.action.SuspendProcessInstanceDialog;
import io.flowset.control.test_support.ui.view.processinstance.detail.ProcessInstanceDetailView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions on Process instance detail view")
public class ProcessInstanceDetailViewActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Suspend action for active instance opens confirmation dialog")
    void givenExistingActiveProcessInstance_whenOpenDetailViewAndSuspend_thenSuspendDialogOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        // then
        detailView.getGeneralPanel().getSuspendBtn().click();

        SuspendProcessInstanceDialog dialog = $j(SuspendProcessInstanceDialog.class).exists();
        dialog.shouldBe(VISIBLE);
    }

    @Test
    @DisplayName("Suspend action hidden for suspended instance")
    void givenExistingSuspendedProcessInstance_whenOpenDetailView_thenSuspendActionHidden() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .suspendByKey("vacation_approval", true);
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        // then
        detailView.getGeneralPanel()
                .getSuspendBtn()
                .shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Activate action for suspended instance opens confirmation dialog")
    void givenExistingSuspendedProcessInstance_whenOpenDetailViewAndActivate_thenActivateDialogOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .suspendByKey("vacation_approval", true);
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        // then
        detailView.getGeneralPanel().getActivateBtn().click();

        ActivateProcessInstanceDialog dialog = $j(ActivateProcessInstanceDialog.class).exists();
        dialog.shouldBe(VISIBLE);
    }

    @Test
    @DisplayName("Activate action hidden for active instance")
    void givenExistingActiveProcessInstance_whenOpenDetailView_thenActivateActionHidden() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        // then
        detailView.getGeneralPanel()
                .getActivateBtn()
                .shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Terminate action for active instance opens confirmation dialog")
    void givenExistingProcessInstance_whenOpenDetailViewAndTerminate_thenTerminateDialogOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        // then
        detailView.getGeneralPanel().getTerminateBtn().click();

        ProcessInstanceTerminateDialog dialog = $j(ProcessInstanceTerminateDialog.class).exists();
        dialog.shouldBe(VISIBLE);
    }

    @Test
    @DisplayName("Suspend action: runtime actions are updated after confirmation")
    void givenExistingActiveProcessInstance_whenSuspendConfirmed_thenInstanceSuspended() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        detailView.getGeneralPanel().getSuspendBtn().click();

        SuspendProcessInstanceDialog dialog = $j(SuspendProcessInstanceDialog.class).exists();
        dialog.getSuspendBtn().click();

        // then
        detailView.getGeneralPanel().getActivateBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        detailView.getGeneralPanel().getSuspendBtn().shouldNotBe(VISIBLE);
        detailView.getGeneralPanel().getRefreshBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        detailView.getGeneralPanel().getMigrateBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        detailView.getGeneralPanel().getTerminateBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Suspend action: runtime actions are not updated after cancellation")
    void givenExistingActiveProcessInstance_whenSuspendCancelled_thenInstanceStillActive() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        detailView.getGeneralPanel().getSuspendBtn().click();

        SuspendProcessInstanceDialog dialog = $j(SuspendProcessInstanceDialog.class).exists();
        dialog.getCancelBtn().click();

        // then
        detailView.getGeneralPanel().getSuspendBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        detailView.getGeneralPanel().getActivateBtn().shouldNotBe(VISIBLE);
        detailView.getGeneralPanel().getRefreshBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        detailView.getGeneralPanel().getMigrateBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        detailView.getGeneralPanel().getTerminateBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Activate action: runtime actions are updated after confirmation")
    void givenExistingSuspendedProcessInstance_whenActivateConfirmed_thenInstanceActivated() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .suspendByKey("vacation_approval", true);

        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        detailView.getGeneralPanel().getActivateBtn().click();

        ActivateProcessInstanceDialog dialog = $j(ActivateProcessInstanceDialog.class).exists();
        dialog.getActivateBtn().click();

        // then
        detailView.getGeneralPanel().getSuspendBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        detailView.getGeneralPanel().getActivateBtn().shouldNotBe(VISIBLE);
        detailView.getGeneralPanel().getRefreshBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        detailView.getGeneralPanel().getMigrateBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        detailView.getGeneralPanel().getTerminateBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Activate action: runtime actions are not updated after cancellation")
    void givenExistingSuspendedProcessInstance_whenActivateCancelled_thenInstanceStillSuspended() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .suspendByKey("vacation_approval", true);
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        detailView.getGeneralPanel().getActivateBtn().click();

        ActivateProcessInstanceDialog dialog = $j(ActivateProcessInstanceDialog.class).exists();
        dialog.getCancelBtn().click();

        // then
        detailView.getGeneralPanel().getActivateBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        detailView.getGeneralPanel().getSuspendBtn().shouldNotBe(VISIBLE);
        detailView.getGeneralPanel().getRefreshBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        detailView.getGeneralPanel().getMigrateBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        detailView.getGeneralPanel().getTerminateBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Terminate action: runtime actions are hidden after confirmation")
    void givenExistingActiveProcessInstance_whenTerminateConfirmed_thenRuntimeButtonsHidden() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        detailView.getGeneralPanel().getTerminateBtn().click();

        ProcessInstanceTerminateDialog dialog = $j(ProcessInstanceTerminateDialog.class).exists();
        dialog.getTerminateBtn().click();

        // then
        detailView.getGeneralPanel().getRefreshBtn().shouldNotBe(VISIBLE);
        detailView.getGeneralPanel().getSuspendBtn().shouldNotBe(VISIBLE);
        detailView.getGeneralPanel().getActivateBtn().shouldNotBe(VISIBLE);
        detailView.getGeneralPanel().getMigrateBtn().shouldNotBe(VISIBLE);
        detailView.getGeneralPanel().getTerminateBtn().shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Terminate action: runtime actions are not hidden after cancellation")
    void givenExistingActiveProcessInstance_whenTerminateCancelled_thenInstanceStillActive() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        detailView.getGeneralPanel().getTerminateBtn().click();

        ProcessInstanceTerminateDialog dialog = $j(ProcessInstanceTerminateDialog.class).exists();
        dialog.getCancelBtn().click();

        // then
        detailView.getGeneralPanel().getRefreshBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        detailView.getGeneralPanel().getSuspendBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        detailView.getGeneralPanel().getMigrateBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        detailView.getGeneralPanel().getTerminateBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Migrate action for active instance opens Migrate dialog")
    void givenExistingProcessInstance_whenOpenDetailViewAndMigrate_thenMigrateDialogOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        // then
        detailView.getGeneralPanel().getMigrateBtn().click();

        $j(ProcessInstanceMigrationDialog.class).exists()
                .displayed();
    }

    @Test
    @DisplayName("Refresh action for active instance reloads process instance detail view")
    void givenExistingProcessInstance_whenOpenDetailViewAndRefresh_thenDetailViewReloaded() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        camundaRestTestHelper.suspendInstanceById(camunda7, instanceId);

        // then
        detailView.getGeneralPanel()
                .getSuspendBtn()
                .shouldBe(VISIBLE);

        detailView.getGeneralPanel().getRefreshBtn().click();

        ProcessInstanceDetailView reopenedDetailView = $j(ProcessInstanceDetailView.class)
                .exists();

        reopenedDetailView.getGeneralPanel()
                .getSuspendBtn()
                .shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Info toggle action shows details panel")
    void givenExistingProcessInstance_whenOpenDetailViewAndClickInfoToggle_thenDetailsPanelShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        // then
        detailView.getGeneralPanel()
                .shouldBe(VISIBLE)
                .getPropertiesPanel()
                .shouldNotBe(VISIBLE);

        detailView.getGeneralPanel().getInfoBtn().click();

        detailView.getGeneralPanel()
                .getPropertiesPanel()
                .shouldBe(EXIST)
                .shouldBe(VISIBLE);

        detailView.getGeneralPanel().getInfoBtn().click();

        detailView.getGeneralPanel()
                .getPropertiesPanel()
                .shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Actions visibility for completed instance")
    void givenExistingCompletedProcessInstance_whenOpenDetailView_thenRuntimeInstanceActionsHidden() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testCompletedInstance.bpmn")
                .startByKey("testCompletedInstance")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testCompletedInstance").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .switchToAllViewMode()
                .openDetailViewByInstanceId(instanceId);

        // then
        detailView.getGeneralPanel().getRefreshBtn().shouldNotBe(VISIBLE);
        detailView.getGeneralPanel().getSuspendBtn().shouldNotBe(VISIBLE);
        detailView.getGeneralPanel().getActivateBtn().shouldNotBe(VISIBLE);
        detailView.getGeneralPanel().getMigrateBtn().shouldNotBe(VISIBLE);
        detailView.getGeneralPanel().getTerminateBtn().shouldNotBe(VISIBLE);
    }
}
