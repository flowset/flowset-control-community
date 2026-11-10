/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.detail.tab.runtime.variable;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.request.StartProcessDto;
import io.flowset.control.test_support.camunda7.dto.request.VariableValueDto;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processdefinition.StartProcessWithVariableDialog;
import io.flowset.control.test_support.ui.view.usertask.AllTasksListView;
import io.flowset.control.test_support.ui.view.usertask.BulkTaskCompleteDialog;
import io.flowset.control.test_support.ui.view.usertask.TaskCompleteDialog;
import io.flowset.control.test_support.ui.view.variable.VariableInstanceDataDetailDialog;
import io.jmix.masquerade.component.ComboBox;
import io.jmix.masquerade.component.ComboBoxOverlay;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.visible;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeVariablesTabFragment.NAME_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeVariablesTabFragment.NAME_COLUMN_INDEX;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JConditions.value;
import static io.jmix.masquerade.JConditions.visibleItems;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Fields of the variable instance detail dialog that depend on the type")
public class VariableInstanceDetailViewValueFieldUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Type combo box lists every variable type when a variable is created in the Variables tab")
    void givenActiveProcessInstance_whenTypeComboBoxOpenedInVariablesTab_thenAllTypesListed() {
        // given
        String instanceId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess")
                .getStartedInstances("testLocalVariableSubProcess")
                .getFirst();

        MainView mainView = loginAsAdmin();

        mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab()
                .getCreateButton()
                .click();

        VariableInstanceDataDetailDialog dialog = $j(VariableInstanceDataDetailDialog.class).exists().displayed();

        // when
        ComboBoxOverlay<ComboBox> typeOverlay = dialog.getTypeComboBox().clickItemsOverlay();

        // then
        typeOverlay.shouldHave(visibleItems("String", "Null", "Integer", "Short",
                "Long", "Double", "Boolean", "Date",
                "Object", "File", "Bytes"));
    }

    @Test
    @DisplayName("Type combo box lists every variable type when a variable is added for starting a process")
    void givenExistingProcessDefinition_whenTypeComboBoxOpenedInStartProcessDialog_thenAllTypesListed() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn");

        MainView mainView = loginAsAdmin();

        mainView.openProcessListView()
                .openDetailViewByKey("testLocalVariableSubProcess")
                .getGeneralPanel()
                .getStartProcessBtn()
                .click();
        $j(StartProcessWithVariableDialog.class)
                .exists()
                .displayed()
                .getCreateVariableBtn()
                .click();

        VariableInstanceDataDetailDialog dialog = $j(VariableInstanceDataDetailDialog.class).exists().displayed();

        // when
        ComboBoxOverlay<ComboBox> typeOverlay = dialog.getTypeComboBox().clickItemsOverlay();

        // then
        typeOverlay.shouldHave(visibleItems("String", "Null", "Integer",
                "Short", "Long", "Double", "Boolean", "Date",
                "Object", "File", "Bytes"));
    }

    @Test
    @DisplayName("Type combo box lists every variable type when a variable is added for completing a user task")
    void givenActiveUserTask_whenTypeComboBoxOpenedInCompleteTaskDialog_thenAllTypesListed() {
        // given
        String taskId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess")
                .getUserTasksByKey("testLocalVariableSubProcess")
                .getFirst();

        MainView mainView = loginAsAdmin();

        mainView.openUserTaskListView()
                .openDetailView(taskId)
                .getCompleteBtn()
                .click();
        $j(TaskCompleteDialog.class)
                .exists()
                .displayed()
                .getCreateVariableBtn()
                .click();

        VariableInstanceDataDetailDialog dialog = $j(VariableInstanceDataDetailDialog.class).exists().displayed();

        // when
        ComboBoxOverlay<ComboBox> typeOverlay = dialog.getTypeComboBox().clickItemsOverlay();

        // then
        typeOverlay.shouldHave(visibleItems("String", "Null", "Integer",
                "Short", "Long", "Double", "Boolean", "Date",
                "Object", "File", "Bytes"));
    }

    @Test
    @DisplayName("Type combo box lists every variable type when a variable is added for completing user tasks in bulk")
    void givenActiveUserTask_whenTypeComboBoxOpenedInBulkCompleteTaskDialog_thenAllTypesListed() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess");

        MainView mainView = loginAsAdmin();

        AllTasksListView listView = mainView.openUserTaskListView();
        listView.getTasksDataGrid().clickSelectAll();
        listView.getCompleteTaskBtn().click();
        $j(BulkTaskCompleteDialog.class)
                .exists()
                .displayed()
                .getCreateVariableBtn()
                .click();

        VariableInstanceDataDetailDialog dialog = $j(VariableInstanceDataDetailDialog.class).exists().displayed();

        // when
        ComboBoxOverlay<ComboBox> typeOverlay = dialog.getTypeComboBox().clickItemsOverlay();

        // then
        typeOverlay.shouldHave(visibleItems("String", "Null", "Integer",
                "Short", "Long", "Double", "Boolean", "Date",
                "Object", "File", "Bytes"));
    }

    @Test
    @DisplayName("Object type shows object type info and serialization format fields")
    void givenActiveProcessInstance_whenObjectTypeSelected_thenObjectInfoFieldsVisible() {
        // given
        String instanceId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess")
                .getStartedInstances("testLocalVariableSubProcess")
                .getFirst();

        MainView mainView = loginAsAdmin();

        mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab()
                .getCreateButton()
                .click();

        VariableInstanceDataDetailDialog dialog = $j(VariableInstanceDataDetailDialog.class).exists().displayed();

        // when
        dialog.getTypeComboBox()
                .shouldBe(VISIBLE)
                .setValue("Object");

        // then
        dialog.getObjectTypeInfoField().shouldBe(VISIBLE);
        dialog.getSerializationDataFormatField().shouldBe(VISIBLE);
    }

    @Test
    @DisplayName("Object type info and serialization format fields are hidden after the type is changed from Object to String")
    void givenActiveProcessInstance_whenTypeChangedFromObjectToString_thenObjectInfoFieldsHidden() {
        // given
        String instanceId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess")
                .getStartedInstances("testLocalVariableSubProcess")
                .getFirst();

        MainView mainView = loginAsAdmin();

        mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab()
                .getCreateButton()
                .click();

        VariableInstanceDataDetailDialog dialog = $j(VariableInstanceDataDetailDialog.class).exists().displayed();
        dialog.getTypeComboBox()
                .shouldBe(VISIBLE)
                .setValue("Object");
        dialog.getObjectTypeInfoField().shouldBe(VISIBLE);

        // when
        dialog.getTypeComboBox().setValue("String");

        // then
        dialog.getObjectTypeInfoField().shouldNotBe(VISIBLE);
        dialog.getSerializationDataFormatField().shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Value field is hidden after the type is changed from String to Null")
    void givenActiveProcessInstance_whenTypeChangedFromStringToNull_thenValueFieldHidden() {
        // given
        String instanceId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess")
                .getStartedInstances("testLocalVariableSubProcess")
                .getFirst();

        MainView mainView = loginAsAdmin();

        mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab()
                .getCreateButton()
                .click();

        VariableInstanceDataDetailDialog dialog = $j(VariableInstanceDataDetailDialog.class).exists().displayed();
        dialog.getTypeComboBox()
                .shouldBe(VISIBLE)
                .setValue("String");
        dialog.getValueComponentElement().shouldBe(visible);

        // when
        dialog.getTypeComboBox().setValue("Null");

        // then
        dialog.getValueComponentElement().shouldNotBe(visible);
    }

    @Test
    @DisplayName("Value field is shown after the type is changed from Null to String")
    void givenActiveProcessInstance_whenTypeChangedFromNullToString_thenValueFieldVisible() {
        // given
        String instanceId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess")
                .getStartedInstances("testLocalVariableSubProcess")
                .getFirst();

        MainView mainView = loginAsAdmin();

        mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab()
                .getCreateButton()
                .click();

        VariableInstanceDataDetailDialog dialog = $j(VariableInstanceDataDetailDialog.class).exists().displayed();
        dialog.getTypeComboBox()
                .shouldBe(VISIBLE)
                .setValue("Null");
        dialog.getValueComponentElement().shouldNotBe(visible);

        // when
        dialog.getTypeComboBox().setValue("String");

        // then
        dialog.getValueComponentElement().shouldBe(visible);
    }

    @Test
    @DisplayName("Object type info and serialization format fields show the values of an existing Object variable")
    void givenExistingObjectVariable_whenDetailDialogOpened_thenObjectInfoFieldsShowValues() {
        // given
        String instanceId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess", StartProcessDto.builder()
                        .variable("objectVariable", new VariableValueDto("Object", "{\"amount\":10}")
                                .setValueInfo(new VariableValueDto.ValueInfo()
                                        .setObjectTypeName("java.util.HashMap")
                                        .setSerializationDataFormat("application/json")))
                        .build())
                .getStartedInstances("testLocalVariableSubProcess")
                .getFirst();

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab()
                .getRowByVariableName("objectVariable")
                .getCellByIndex(NAME_COLUMN_INDEX)
                .getCellContent()
                .find(NAME_BUTTON_BY).click();

        // then
        VariableInstanceDataDetailDialog dialog = $j(VariableInstanceDataDetailDialog.class).exists().displayed();
        dialog.getObjectTypeInfoField()
                .shouldBe(VISIBLE)
                .shouldHave(value("java.util.HashMap"));
        dialog.getSerializationDataFormatField()
                .shouldBe(VISIBLE)
                .shouldHave(value("application/json"));
    }

    @Test
    @DisplayName("Value field is hidden for an existing Null variable")
    void givenExistingNullVariable_whenDetailDialogOpened_thenValueFieldHidden() {
        // given
        String instanceId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess", StartProcessDto.builder()
                        .variable("nullVariable", new VariableValueDto("Null", null))
                        .build())
                .getStartedInstances("testLocalVariableSubProcess")
                .getFirst();

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab()
                .getRowByVariableName("nullVariable")
                .getCellByIndex(NAME_COLUMN_INDEX)
                .getCellContent()
                .find(NAME_BUTTON_BY).click();

        // then
        VariableInstanceDataDetailDialog dialog = $j(VariableInstanceDataDetailDialog.class).exists().displayed();
        dialog.getNameField().shouldHave(value("nullVariable"));
        dialog.getValueComponentElement().shouldNotBe(visible);
    }
}
