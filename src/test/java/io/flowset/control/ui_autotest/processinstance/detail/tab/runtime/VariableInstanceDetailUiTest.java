/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.detail.tab.runtime;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.request.StartProcessDto;
import io.flowset.control.test_support.camunda7.dto.request.VariableValueDto;
import io.flowset.control.test_support.camunda7.dto.response.ExecutionDto;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processdefinition.StartProcessWithVariableDialog;
import io.flowset.control.test_support.ui.view.processinstance.detail.ProcessInstanceDetailView;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeTabFragment;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeVariablesTabFragment;
import io.flowset.control.test_support.ui.view.usertask.TaskCompleteDialog;
import io.flowset.control.test_support.ui.view.usertask.UserTaskDataDetailDialog;
import io.flowset.control.test_support.ui.view.variable.VariableInstanceDataDetailDialog;
import io.jmix.masquerade.component.Notification;
import io.jmix.masquerade.component.TextField;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.text;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeVariablesTabFragment.NAME_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeVariablesTabFragment.NAME_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeVariablesTabFragment.SCOPE_COLUMN_INDEX;
import static io.jmix.masquerade.JConditions.CHECKED;
import static io.jmix.masquerade.JConditions.READONLY;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JConditions.notificationMessageContains;
import static io.jmix.masquerade.JConditions.notificationTitle;
import static io.jmix.masquerade.JConditions.valueContains;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Runtime variable instance detail dialog")
public class VariableInstanceDetailUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Create action: validation error shown for a duplicate name in the process instance scope")
    void givenExistingGlobalVariable_whenCreatingVariableWithSameNameInProcessScope_thenValidationErrorShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess", StartProcessDto.builder()
                        .variable("sharedName", new VariableValueDto("String", "globalValue"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();

        MainView mainView = loginAsAdmin();

        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();
        variablesTab.getCreateButton().click();

        // when
        VariableInstanceDataDetailDialog dialog = $j(VariableInstanceDataDetailDialog.class).exists().displayed();
        dialog.getNameField()
                .shouldBe(VISIBLE)
                .setValue("sharedName");
        dialog.getTypeComboBox()
                .shouldBe(VISIBLE)
                .setValue("String");
        dialog.getValueComponentAs(TextField.class)
                .shouldBe(VISIBLE)
                .setValue("anotherValue");
        dialog.getSaveBtn().click();

        // then
        $j(Notification.class)
                .shouldBe(VISIBLE)
                .shouldHave(notificationTitle("Alert"))
                .shouldHave(notificationMessageContains(
                        "A variable with this name already exists in the selected scope"));
        dialog.shouldBe(VISIBLE);
    }

    @Test
    @DisplayName("Create action: local variable with the name of an existing global variable is created")
    void givenExistingGlobalVariable_whenCreatingLocalVariableWithSameName_thenVariableCreated() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess", StartProcessDto.builder()
                        .variable("sharedName", new VariableValueDto("String", "globalValue"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();

        MainView mainView = loginAsAdmin();

        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);
        RuntimeTabFragment runtimeTab = detailView.openRuntimeTab();
        runtimeTab.selectRowByActivityId("subProcess");

        RuntimeVariablesTabFragment variablesTab = detailView.openRuntimeVariablesTab();
        variablesTab.getCreateButton().click();

        // when
        VariableInstanceDataDetailDialog dialog = $j(VariableInstanceDataDetailDialog.class).exists().displayed();
        dialog.getNameField()
                .shouldBe(VISIBLE)
                .setValue("sharedName");
        dialog.getLocalField().setChecked(true);
        dialog.getTypeComboBox()
                .shouldBe(VISIBLE)
                .setValue("String");
        dialog.getValueComponentAs(TextField.class)
                .shouldBe(VISIBLE)
                .setValue("localValue");
        dialog.getSaveBtn().click();

        dialog.shouldNotBe(VISIBLE);

        // then
        variablesTab.getRowByVariableName("sharedName")
                .getCellByIndex(SCOPE_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("Sub process"));
    }

    @Test
    @DisplayName("Create action: validation error shown for a duplicate name in the sub-process scope")
    void givenExistingLocalVariable_whenCreatingLocalVariableWithSameNameInSameScope_thenValidationErrorShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess");
        String instanceId = dataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();

        camundaRestTestHelper.putLocalExecutionVariable(camunda7, findSubProcessExecutionId(instanceId),
                "localVariable", new VariableValueDto("String", "localValue"));

        MainView mainView = loginAsAdmin();

        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);
        RuntimeTabFragment runtimeTab = detailView.openRuntimeTab();
        runtimeTab.selectRowByActivityId("subProcess");

        detailView.openRuntimeVariablesTab()
                .getCreateButton()
                .click();

        // when
        VariableInstanceDataDetailDialog dialog = $j(VariableInstanceDataDetailDialog.class).exists().displayed();
        dialog.getNameField()
                .shouldBe(VISIBLE)
                .setValue("localVariable");
        dialog.getLocalField().setChecked(true);
        dialog.getTypeComboBox()
                .shouldBe(VISIBLE)
                .setValue("String");
        dialog.getValueComponentAs(TextField.class)
                .shouldBe(VISIBLE)
                .setValue("anotherValue");
        dialog.getSaveBtn().click();

        // then
        $j(Notification.class)
                .shouldBe(VISIBLE)
                .shouldHave(notificationTitle("Alert"))
                .shouldHave(notificationMessageContains(
                        "A variable with this name already exists in the selected scope"));
        dialog.shouldBe(VISIBLE);
    }

    @Test
    @DisplayName("Local field is checked and read-only for local variable")
    void givenExistingLocalVariable_whenDetailDialogOpened_thenLocalCheckboxCheckedAndReadOnly() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess");
        String instanceId = dataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();

        camundaRestTestHelper.putLocalExecutionVariable(camunda7, findSubProcessExecutionId(instanceId),
                "localVariable", new VariableValueDto("String", "localValue"));

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab()
                .getRowByVariableName("localVariable")
                .getCellByIndex(NAME_COLUMN_INDEX)
                .getCellContent()
                .find(NAME_BUTTON_BY).click();

        // then
        $j(VariableInstanceDataDetailDialog.class)
                .exists()
                .displayed()
                .getLocalField()
                .shouldBe(VISIBLE)
                .shouldBe(CHECKED)
                .shouldBe(READONLY);
    }

    @Test
    @DisplayName("Local field is unchecked and read-only for global variable")
    void givenExistingGlobalVariable_whenDetailDialogOpened_thenLocalCheckboxUncheckedAndReadOnly() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess", StartProcessDto.builder()
                        .variable("globalVariable", new VariableValueDto("String", "globalValue"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab()
                .getRowByVariableName("globalVariable")
                .getCellByIndex(NAME_COLUMN_INDEX)
                .getCellContent()
                .find(NAME_BUTTON_BY).click();

        // then
        $j(VariableInstanceDataDetailDialog.class)
                .exists()
                .displayed()
                .getLocalField()
                .shouldBe(VISIBLE)
                .shouldNotBe(CHECKED)
                .shouldBe(READONLY);
    }

    @Test
    @DisplayName("Scope field has sub-process execution id value for local variable")
    void givenExistingLocalVariable_whenDetailDialogOpened_thenScopeFieldShowsExecutionIdAndActivityName() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess");
        String instanceId = dataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();
        String subProcessExecutionId = findSubProcessExecutionId(instanceId);

        camundaRestTestHelper.putLocalExecutionVariable(camunda7, subProcessExecutionId, "localVariable",
                new VariableValueDto("String", "localValue"));

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab()
                .getRowByVariableName("localVariable")
                .getCellByIndex(NAME_COLUMN_INDEX)
                .getCellContent()
                .find(NAME_BUTTON_BY).click();

        // then
        $j(VariableInstanceDataDetailDialog.class)
                .exists()
                .displayed()
                .getScopeField()
                .shouldBe(VISIBLE)
                .shouldHave(valueContains("Sub process"));
    }

    @Test
    @DisplayName("Scope is not changed after value is saved for local variable")
    void givenExistingLocalVariable_whenValueEditedAndSaved_thenValueUpdatedAndScopeUnchanged() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess");
        String instanceId = dataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();

        camundaRestTestHelper.putLocalExecutionVariable(camunda7, findSubProcessExecutionId(instanceId),
                "localVariable", new VariableValueDto("String", "localValue"));

        MainView mainView = loginAsAdmin();

        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();

        variablesTab.getRowByVariableName("localVariable")
                .getCellByIndex(NAME_COLUMN_INDEX)
                .getCellContent()
                .find(NAME_BUTTON_BY).click();

        // when
        VariableInstanceDataDetailDialog dialog = $j(VariableInstanceDataDetailDialog.class).exists().displayed();
        dialog.getValueComponentAs(TextField.class)
                .shouldBe(VISIBLE)
                .setValue("updatedLocalValue");
        dialog.getSaveBtn().click();

        dialog.shouldNotBe(VISIBLE);

        // then
        variablesTab.getRowByVariableName("localVariable")
                .getCellByIndex(SCOPE_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("Sub process"));
    }

    @Test
    @DisplayName("Local and Scope fields are hidden when adding a variable for starting a process")
    void givenExistingProcessDefinition_whenCreateVariableInStartProcessDialog_thenLocalAndScopeFieldsHidden() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn");

        MainView mainView = loginAsAdmin();

        mainView.openProcessListView()
                .openDetailViewByKey("testLocalVariableSubProcess")
                .getGeneralPanel()
                .getStartProcessBtn()
                .click();

        // when
        $j(StartProcessWithVariableDialog.class)
                .exists()
                .displayed()
                .getCreateVariableBtn()
                .click();

        // then
        VariableInstanceDataDetailDialog dialog = $j(VariableInstanceDataDetailDialog.class).exists().displayed();
        dialog.getNameField().shouldBe(VISIBLE);
        dialog.getLocalField().shouldNotBe(VISIBLE);
        dialog.getScopeField().shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Local and Scope fields are hidden when adding a variable for completing a user task")
    void givenActiveUserTask_whenCreateVariableInCompleteTaskDialog_thenLocalAndScopeFieldsHidden() {
        // given
        String taskId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess")
                .getUserTasksByKey("testLocalVariableSubProcess")
                .getFirst();

        MainView mainView = loginAsAdmin();

        UserTaskDataDetailDialog detailDialog = mainView.openUserTaskListView()
                .openDetailView(taskId);
        detailDialog.getCompleteBtn().click();

        // when
        $j(TaskCompleteDialog.class)
                .exists()
                .displayed()
                .getCreateVariableBtn()
                .click();

        // then
        VariableInstanceDataDetailDialog dialog = $j(VariableInstanceDataDetailDialog.class).exists().displayed();
        dialog.getNameField().shouldBe(VISIBLE);
        dialog.getLocalField().shouldNotBe(VISIBLE);
        dialog.getScopeField().shouldNotBe(VISIBLE);
    }

    String findSubProcessExecutionId(String processInstanceId) {
        return camundaRestTestHelper.findExecutions(camunda7, processInstanceId)
                .stream()
                .map(ExecutionDto::getId)
                .filter(executionId -> !executionId.equals(processInstanceId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No child execution found for " + processInstanceId));
    }
}
