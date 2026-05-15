/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.detail.tab.runtime;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.request.StartProcessDto;
import io.flowset.control.test_support.camunda7.dto.request.VariableValueDto;
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

    @Test
    @DisplayName("Create action availability on Variables tab")
    void givenExistingProcessInstance_whenOpenDetailView_thenVariablesCreateActionAvailable() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", StartProcessDto.builder()
                        .variable("firstVariable", new VariableValueDto("String", "Some value"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").get(0);

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

        String instanceId = dataManager.getStartedInstances("testUpdateVariable").get(0);

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

        String instanceId = dataManager.getStartedInstances("testUpdateVariable").get(0);

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
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").get(0);

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
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").get(0);

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
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").get(0);

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
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").get(0);

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
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").get(0);

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
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").get(0);

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
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").get(0);

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
}
