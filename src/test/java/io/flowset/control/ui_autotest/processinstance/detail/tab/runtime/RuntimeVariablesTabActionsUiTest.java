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
import io.flowset.control.test_support.ui.component.JmixDialog;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeVariablesTabFragment;
import io.flowset.control.test_support.ui.view.variable.VariableInstanceDataDetailDialog;
import io.jmix.masquerade.component.TextField;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.text;
import static io.flowset.control.test_support.ui.condition.ControlCondition.*;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeVariablesTabFragment.*;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions in the Variables tab (Runtime tab)")
public class RuntimeVariablesTabActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Create action availability on Variables tab")
    void givenExistingProcessInstance_whenOpenDetailView_thenVariablesCreateActionAvailable() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", StartProcessDto.builder()
                        .variable("firstVariable", new VariableValueDto("String", "Some value"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();

        // then
        variablesTab.getCreateButton()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        variablesTab.openVariablesGridContextMenu()
                .find(text("Create"))
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("View action in Name column opens Variable detail view dialog")
    void givenExistingProcessVariable_whenOpenDetailView_thenViewActionOpensDetailView() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", StartProcessDto.builder()
                        .variable("firstVariable", new VariableValueDto("String", "Some value"))
                        .build());

        String instanceId = dataManager.getStartedInstances("testUpdateVariable").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();

        // then
        variablesTab.getRowByVariableName("firstVariable")
                .getCellByIndex(NAME_COLUMN_INDEX)
                .getCellContent()
                .find(NAME_BUTTON_BY).click();

        $j(VariableInstanceDataDetailDialog.class).exists()
                .displayed()
                .getNameField()
                .shouldHave(value("firstVariable"));
    }

    @Test
    @DisplayName("Double-click on grid row opens Variable detail view dialog")
    void givenExistingProcessVariable_whenOpenDetailView_thenDoubleClickOpensDetailView() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", StartProcessDto.builder()
                        .variable("firstVariable", new VariableValueDto("String", "Some value"))
                        .build());

        String instanceId = dataManager.getStartedInstances("testUpdateVariable").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();

        // then
        variablesTab.getRowByVariableName("firstVariable")
                .getCellByIndex(TYPE_COLUMN_INDEX)
                .getCellContent()
                .doubleClick();

        $j(VariableInstanceDataDetailDialog.class).exists()
                .displayed()
                .getNameField()
                .shouldHave(value("firstVariable"));
    }

    @Test
    @DisplayName("Remove action availability on Variables tab")
    void givenExistingProcessVariable_whenOpenDetailView_thenVariablesRemoveActionAvailable() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", StartProcessDto.builder()
                        .variable("firstVariable", new VariableValueDto("String", "Some value"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();

        // then
        variablesTab.getRemoveButton()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        variablesTab.openVariablesGridContextMenu()
                .find(text("Remove"))
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        variablesTab.getRuntimeVariablesGrid().clickSelectAll();

        variablesTab.getRemoveButton()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        variablesTab.openVariablesGridContextMenu()
                .find(text("Remove"))
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("List of available actions on Variables tab")
    void givenExistingProcessVariable_whenOpenDetailView_thenAllVariablesActionsAvailable() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", StartProcessDto.builder()
                        .variable("firstVariable", new VariableValueDto("String", "Some value"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();

        // then
        variablesTab.getCreateButton().shouldBe(VISIBLE);
        variablesTab.getRemoveButton().shouldBe(VISIBLE);

        variablesTab.openVariablesGridContextMenu()
                .shouldHave(visibleItems("Create", "Remove"));
    }

    @Test
    @DisplayName("Create action on Variables tab opens variable detail dialog")
    void givenExistingProcessInstance_whenOpenVariablesTabAndCreate_thenCreateDialogOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", StartProcessDto.builder()
                        .variable("firstVariable", new VariableValueDto("String", "Some value"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();

        variablesTab.getCreateButton().click();

        // then
        $j(VariableInstanceDataDetailDialog.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Create action: variable is not added to data grid after cancellation")
    void givenExistingProcessInstance_whenCreateDialogCancelled_thenNoVariableAdded() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", StartProcessDto.builder()
                        .variable("firstVariable", new VariableValueDto("String", "Some value"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();

        variablesTab.getCreateButton().click();

        $j(VariableInstanceDataDetailDialog.class)
                .exists()
                .displayed()
                .close();

        // then
        variablesTab.getRuntimeVariablesGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(anyBodyRowHaveCellElementText(NAME_COLUMN_INDEX, NAME_BUTTON_BY, "firstVariable"));
    }

    @Test
    @DisplayName("Create action: variable is added to data grid after confirmation")
    void givenExistingProcessInstance_whenCreateDialogConfirmed_thenVariableAdded() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable");
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();

        variablesTab.getCreateButton().click();

        VariableInstanceDataDetailDialog dialog = $j(VariableInstanceDataDetailDialog.class)
                .exists()
                .displayed();
        dialog.getNameField()
                .shouldBe(VISIBLE)
                .setValue("newVariable");
        dialog.getTypeComboBox()
                .shouldBe(VISIBLE)
                .setValue("String");
        dialog.getValueComponentAs(TextField.class)
                .shouldBe(VISIBLE)
                .setValue("New value");
        dialog.getSaveBtn().click();

        dialog.shouldNotBe(VISIBLE);

        // then
        variablesTab.getRuntimeVariablesGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(anyBodyRowHaveCellElementText(NAME_COLUMN_INDEX, NAME_BUTTON_BY, "newVariable"));
    }

    @Test
    @DisplayName("Remove action: variable is removed from data grid after confirmation")
    void givenExistingProcessVariable_whenRemoveConfirmed_thenVariableRemoved() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", StartProcessDto.builder()
                        .variable("firstVariable", new VariableValueDto("String", "Some value"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();

        variablesTab.selectRowByVariableName("firstVariable");

        variablesTab.getRemoveButton().click();

        JmixDialog dialog = $j(JmixDialog.class, JmixDialog.OVERLAY)
                .exists()
                .displayed();
        dialog.getOkBtn().click();

        dialog.shouldNotBe(VISIBLE);

        // then
        variablesTab.getRuntimeVariablesGrid()
                .shouldBe(emptyGrid);
    }

    @Test
    @DisplayName("Remove action: variable is not removed from data grid after cancellation")
    void givenExistingProcessVariable_whenRemoveCancelled_thenVariableStillPresent() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", StartProcessDto.builder()
                        .variable("firstVariable", new VariableValueDto("String", "Some value"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();

        variablesTab.getRuntimeVariablesGrid().clickSelectAll();

        variablesTab.getRemoveButton().click();

        $j(JmixDialog.class, JmixDialog.OVERLAY)
                .exists()
                .displayed()
                .getCancelBtn().click();

        // then
        variablesTab.getRuntimeVariablesGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(anyBodyRowHaveCellElementText(NAME_COLUMN_INDEX, NAME_BUTTON_BY, "firstVariable"));
    }

    @Test
    @DisplayName("Edit action: variable value is updated in data grid after confirmation")
    void givenExistingProcessVariable_whenNameLinkClickedAndEditConfirmed_thenValueUpdatedInGrid() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", StartProcessDto.builder()
                        .variable("firstVariable", new VariableValueDto("String", "Some value"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();

        variablesTab.getRowByVariableName("firstVariable")
                .getCellByIndex(NAME_COLUMN_INDEX)
                .getCellContent()
                .find(NAME_BUTTON_BY).click();

        VariableInstanceDataDetailDialog dialog = $j(VariableInstanceDataDetailDialog.class)
                .exists()
                .displayed();
        dialog.getValueComponentAs(TextField.class)
                .shouldBe(VISIBLE)
                .setValue("Updated value");
        dialog.getSaveBtn().click();

        dialog.shouldNotBe(VISIBLE);

        // then
        variablesTab.getRowByVariableName("firstVariable")
                .getCellByIndex(VALUE_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("Updated value"));
    }

    @Test
    @DisplayName("Edit action: variable value is not changed in data grid after cancellation")
    void givenExistingProcessVariable_whenNameLinkClickedAndEditCancelled_thenValueNotChangedInGrid() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", StartProcessDto.builder()
                        .variable("firstVariable", new VariableValueDto("String", "Some value"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();

        variablesTab.getRowByVariableName("firstVariable")
                .getCellByIndex(NAME_COLUMN_INDEX)
                .getCellContent()
                .find(NAME_BUTTON_BY).click();

        VariableInstanceDataDetailDialog dialog = $j(VariableInstanceDataDetailDialog.class)
                .exists()
                .displayed();
        dialog.getValueComponentAs(TextField.class)
                .shouldBe(VISIBLE)
                .setValue("Updated value");
        dialog.getCloseBtn().click();

        dialog.shouldNotBe(VISIBLE);

        // then
        variablesTab.getRowByVariableName("firstVariable")
                .getCellByIndex(VALUE_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("Some value"));
    }

    @Test
    @DisplayName("Scope column shows the process instance for a global variable")
    void givenGlobalVariable_whenRuntimeVariablesTabOpened_thenScopeColumnShowsProcessInstanceName() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess", StartProcessDto.builder()
                        .variable("globalVariable", new VariableValueDto("String", "globalValue"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();

        // then
        variablesTab.getRowByVariableName("globalVariable")
                .getCellByIndex(SCOPE_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("Process instance (Test local variable subprocess)"));
    }

    @Test
    @DisplayName("Scope column shows the activity name for a local variable")
    void givenLocalVariableInSubProcess_whenRuntimeVariablesTabOpened_thenScopeColumnShowsActivityName() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess");
        String instanceId = dataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();

        camundaRestTestHelper.putLocalExecutionVariable(camunda7, findSubProcessExecutionId(instanceId),
                "localVariable", new VariableValueDto("String", "localValue"));

        MainView mainView = loginAsAdmin();

        // when
        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();

        // then
        variablesTab.getRowByVariableName("localVariable")
                .getCellByIndex(SCOPE_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("Sub process"));
    }

    @Test
    @DisplayName("Remove action: only the local variable is removed from data grid after confirmation")
    void givenLocalVariableSelected_whenRemoveConfirmed_thenOnlyLocalVariableRemoved() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess", StartProcessDto.builder()
                        .variable("globalVariable", new VariableValueDto("String", "globalValue"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();

        camundaRestTestHelper.putLocalExecutionVariable(camunda7, findSubProcessExecutionId(instanceId),
                "localVariable", new VariableValueDto("String", "localValue"));

        MainView mainView = loginAsAdmin();

        // when
        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();

        variablesTab.selectRowByVariableName("localVariable");
        variablesTab.getRemoveButton().click();

        JmixDialog dialog = $j(JmixDialog.class, JmixDialog.OVERLAY)
                .exists()
                .displayed();
        dialog.getOkBtn().click();

        dialog.shouldNotBe(VISIBLE);

        // then
        variablesTab.getRuntimeVariablesGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(anyBodyRowHaveCellElementText(NAME_COLUMN_INDEX, NAME_BUTTON_BY, "globalVariable"));
    }

    @Test
    @DisplayName("Remove action: variables of different scopes are removed from data grid after confirmation")
    void givenLocalAndGlobalVariablesSelected_whenRemoveConfirmed_thenBothRemoved() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess", StartProcessDto.builder()
                        .variable("globalVariable", new VariableValueDto("String", "globalValue"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();

        camundaRestTestHelper.putLocalExecutionVariable(camunda7, findSubProcessExecutionId(instanceId),
                "localVariable", new VariableValueDto("String", "localValue"));

        MainView mainView = loginAsAdmin();

        // when
        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();

        variablesTab.selectRowByVariableName("globalVariable");
        variablesTab.selectRowByVariableName("localVariable");
        variablesTab.getRemoveButton().click();

        JmixDialog dialog = $j(JmixDialog.class, JmixDialog.OVERLAY)
                .exists()
                .displayed();
        dialog.getOkBtn().click();

        dialog.shouldNotBe(VISIBLE);

        // then
        variablesTab.getRuntimeVariablesGrid()
                .shouldBe(emptyGrid);
    }

    @Test
    @DisplayName("Remove action: local variable is not removed from data grid after cancellation")
    void givenLocalVariableSelected_whenRemoveCancelled_thenVariableRemainsInGrid() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess");
        String instanceId = dataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();

        camundaRestTestHelper.putLocalExecutionVariable(camunda7, findSubProcessExecutionId(instanceId),
                "localVariable", new VariableValueDto("String", "localValue"));

        MainView mainView = loginAsAdmin();

        // when
        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();

        variablesTab.selectRowByVariableName("localVariable");
        variablesTab.getRemoveButton().click();

        JmixDialog dialog = $j(JmixDialog.class, JmixDialog.OVERLAY)
                .exists()
                .displayed();
        dialog.getCancelBtn().click();

        dialog.shouldNotBe(VISIBLE);

        // then
        variablesTab.getRowByVariableName("localVariable")
                .getCellByIndex(SCOPE_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("Sub process"));
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
