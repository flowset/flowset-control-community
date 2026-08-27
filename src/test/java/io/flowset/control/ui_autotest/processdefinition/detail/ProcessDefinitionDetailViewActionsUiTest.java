/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processdefinition.detail;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.deployment.DeploymentDetailView;
import io.flowset.control.test_support.ui.view.processdefinition.StartProcessWithVariableDialog;
import io.flowset.control.test_support.ui.view.processdefinition.action.ActivateProcessDefinitionDialog;
import io.flowset.control.test_support.ui.view.processdefinition.action.DeleteProcessDefinitionDialog;
import io.flowset.control.test_support.ui.view.processdefinition.action.SuspendProcessDefinitionDialog;
import io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessDefinitionDetailView;
import io.flowset.control.test_support.ui.view.processinstance.ProcessInstanceMigrationDialog;
import io.jmix.masquerade.component.Notification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.value;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions in Process detail view")
public class ProcessDefinitionDetailViewActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Start process action for active process opens Start process dialog")
    void givenExistingActiveProcessDefinition_whenOpenDetailViewAndStartProcess_thenStartProcessDialogOpened() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");

        detailView.getGeneralPanel().getStartProcessBtn().click();

        // then
        StartProcessWithVariableDialog dialog = $j(StartProcessWithVariableDialog.class).exists()
                .displayed();
        dialog.getNameField().shouldHave(value("Vacation approval"));
        dialog.getVersionField().shouldHave(value("1"));
    }

    @Test
    @DisplayName("Start process action hidden for suspended process")
    void givenExistingSuspendedProcessDefinition_whenOpenDetailView_thenStartProcessActionHidden() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .suspendByKey("vacation_approval", false);
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");

        // then
        detailView.getGeneralPanel()
                .getStartProcessBtn()
                .shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Suspend action: actions in general panel are updated after confirmation")
    void givenExistingActiveProcessDefinition_whenSuspendConfirmedOnDetailView_thenProcessSuspended() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");
        detailView.getGeneralPanel().getSuspendBtn().click();

        $j(SuspendProcessDefinitionDialog.class)
                .exists()
                .displayed()
                .getSuspendBtn()
                .click();

        // then
        detailView.getGeneralPanel().getSuspendBtn().shouldNotBe(VISIBLE);
        detailView.getGeneralPanel().getActivateBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        detailView.getGeneralPanel().getStartProcessBtn().shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Suspend action: actions in general panel are not updated after cancellation")
    void givenExistingActiveProcessDefinition_whenSuspendCancelledOnDetailView_thenProcessActive() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");
        detailView.getGeneralPanel().getSuspendBtn().click();

        $j(SuspendProcessDefinitionDialog.class)
                .exists()
                .displayed()
                .getCancelBtn()
                .click();

        // then
        detailView.getGeneralPanel().getSuspendBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        detailView.getGeneralPanel().getActivateBtn().shouldNotBe(VISIBLE);
        detailView.getGeneralPanel().getStartProcessBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Suspend action hidden for suspended process")
    void givenExistingSuspendedProcessDefinition_whenOpenDetailView_thenSuspendActionHidden() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .suspendByKey("vacation_approval", false);
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");

        // then
        detailView.getGeneralPanel()
                .getSuspendBtn()
                .shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Activate action: actions in general panel are updated after confirmation")
    void givenExistingSuspendedProcessDefinition_whenActivateConfirmedOnDetailView_thenProcessActivated() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .suspendByKey("vacation_approval", false);
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");
        detailView.getGeneralPanel().getActivateBtn().click();

        $j(ActivateProcessDefinitionDialog.class)
                .exists()
                .displayed()
                .getActivateBtn()
                .click();

        // then
        detailView.getGeneralPanel().getActivateBtn().shouldNotBe(VISIBLE);
        detailView.getGeneralPanel().getSuspendBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        detailView.getGeneralPanel().getStartProcessBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Activate action: actions in general panel are not updated after cancellation")
    void givenExistingSuspendedProcessDefinition_whenActivateCancelledOnDetailView_thenProcessSuspended() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .suspendByKey("vacation_approval", false);
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");
        detailView.getGeneralPanel().getActivateBtn().click();

        $j(ActivateProcessDefinitionDialog.class)
                .exists()
                .displayed()
                .getCancelBtn()
                .click();

        // then
        detailView.getGeneralPanel().getActivateBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        detailView.getGeneralPanel().getSuspendBtn().shouldNotBe(VISIBLE);
        detailView.getGeneralPanel().getStartProcessBtn().shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Activate action hidden for active process")
    void givenExistingActiveProcessDefinition_whenOpenDetailView_thenActivateActionHidden() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");

        // then
        detailView.getGeneralPanel()
                .getActivateBtn()
                .shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Delete action: process detail view is closed after confirmation")
    void givenExistingProcessDefinition_whenDeleteConfirmedOnDetailView_thenDetailViewClosed() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");
        detailView.getGeneralPanel()
                .getDeleteBtn()
                .click();

        $j(DeleteProcessDefinitionDialog.class)
                .exists()
                .displayed()
                .getOkBtn()
                .click();

        // then
        detailView.shouldNotBe(EXIST);
    }

    @Test
    @DisplayName("Delete action: process detail view is still open after cancellation")
    void givenExistingProcessDefinition_whenDeleteCancelledOnDetailView_thenDetailViewStillOpen() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");
        detailView.getGeneralPanel()
                .getDeleteBtn()
                .shouldBe(VISIBLE).click();

        $j(DeleteProcessDefinitionDialog.class)
                .exists()
                .displayed()
                .getCancelBtn()
                .click();

        // then
        detailView.shouldBe(VISIBLE);
    }

    @Test
    @DisplayName("Migrate action opens Migrate dialog")
    void givenExistingProcessDefinition_whenOpenDetailViewAndMigrate_thenMigrateDialogOpened() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");
        detailView.getGeneralPanel().getMigrateBtn().click();

        // then
        ProcessInstanceMigrationDialog dialog = $j(ProcessInstanceMigrationDialog.class).exists()
                .displayed();

        dialog.getSourceDefinitionKeyField().shouldHave(value("vacation_approval"));
        dialog.getSourceDefinitionVersionField().shouldHave(value("1"));
    }

    @Test
    @DisplayName("Info toggle action updates visibility of the process information panel")
    void givenExistingProcessDefinition_whenOpenDetailViewAndClickInfoToggle_thenPropertiesPanelToggled() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");

        // then
        detailView.getGeneralPanel()
                .getPropertiesPanel()
                .shouldBe(VISIBLE);

        detailView.getGeneralPanel().getInfoBtn().click();

        detailView.getGeneralPanel()
                .getPropertiesPanel()
                .shouldNotBe(VISIBLE);

        detailView.getGeneralPanel().getInfoBtn().click();

        detailView.getGeneralPanel()
                .getPropertiesPanel()
                .shouldBe(VISIBLE);
    }

    @Test
    @DisplayName("Refresh action availability")
    void givenExistingProcessDefinition_whenOpenDetailView_thenRefreshActionAvailable() {
        // given
        CamundaSampleDataManager camundaDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaDataManager.deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");

        // then
        detailView.getGeneralPanel()
                .getRefreshBtn()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }


    @Test
    @DisplayName("View deployment action opens Deployment detail view")
    void givenExistingProcessDefinition_whenOpenDetailViewAndViewDeployment_thenDeploymentDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");
        String processId = dataManager.getDeployedProcessVersions("vacation_approval").get(0);
        String deploymentId = camundaRestTestHelper.getProcessById(camunda7, processId).getDeploymentId();

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");
        detailView.getGeneralPanel()
                .getPropertiesPanel()
                .getViewDeploymentBtn()
                .click();

        // then
        DeploymentDetailView deploymentView = $j(DeploymentDetailView.class).exists();
        deploymentView.getDeploymentIdField().shouldHave(value(deploymentId));
    }

    @Test
    @DisplayName("Copy key action shows notification")
    void givenExistingProcessDefinition_whenCopyKeyActionClicked_thenNotificationShown() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");

        detailView.getGeneralPanel()
                .getPropertiesPanel()
                .getCopyKeyButton()
                .click();

        // then
        $j(Notification.class)
                .shouldBe(VISIBLE)
                .shouldHave(notificationTheme(Notification.Theme.SUCCESS))
                .shouldHave(text("Value copied!"));
    }

    @Test
    @DisplayName("Copy id action shows notification")
    void givenExistingProcessDefinition_whenCopyIdActionClicked_thenNotificationShown() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");

        detailView.getGeneralPanel()
                .getPropertiesPanel()
                .getCopyIdButton()
                .click();

        // then
        $j(Notification.class)
                .shouldBe(VISIBLE)
                .shouldHave(notificationTheme(Notification.Theme.SUCCESS))
                .shouldHave(text("Value copied!"));
    }
}
