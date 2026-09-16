/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.detail.tab.runtime;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processinstance.detail.ProcessInstanceDetailView;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeTabFragment;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeVariablesTabFragment;
import io.flowset.control.test_support.ui.view.variable.VariableInstanceDataDetailDialog;
import io.jmix.masquerade.component.TextField;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.empty;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeVariablesTabFragment.NAME_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeVariablesTabFragment.NAME_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeVariablesTabFragment.SCOPE_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeVariablesTabFragment.VALUE_COLUMN_INDEX;
import static io.jmix.masquerade.JConditions.CHECKED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Create action in the Variables tab (Runtime tab)")
public class RuntimeVariablesTabCreateActionUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Local and Scope fields are hidden when no activity instance is selected")
    void givenNoActivityInstanceSelected_whenCreateDialogOpened_thenLocalCheckboxAndScopeFieldHidden() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess");
        String instanceId = dataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab()
                .getCreateButton()
                .click();

        // then
        VariableInstanceDataDetailDialog dialog = $j(VariableInstanceDataDetailDialog.class).exists().displayed();
        dialog.getLocalField().shouldNotBe(VISIBLE);
        dialog.getScopeField().shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Local field is visible and enabled when sub-process is selected")
    void givenSubProcessActivitySelected_whenCreateDialogOpened_thenLocalCheckboxVisibleAndEnabled() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess");
        String instanceId = dataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();

        MainView mainView = loginAsAdmin();

        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);
        RuntimeTabFragment runtimeTab = detailView.openRuntimeTab();

        // when
        runtimeTab.selectRowByActivityId("subProcess");
        detailView.openRuntimeVariablesTab()
                .getCreateButton()
                .click();

        // then
        $j(VariableInstanceDataDetailDialog.class)
                .exists()
                .displayed()
                .getLocalField()
                .shouldBe(VISIBLE)
                .shouldNotBe(CHECKED);
    }

    @Test
    @DisplayName("Local field is hidden when root activity instance is selected")
    void givenRootActivityInstanceSelected_whenCreateDialogOpened_thenLocalCheckboxHidden() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess");
        String instanceId = dataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();
        String processDefinitionId = dataManager.getDeployedProcessVersions("testLocalVariableSubProcess").getFirst();

        MainView mainView = loginAsAdmin();

        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);
        RuntimeTabFragment runtimeTab = detailView.openRuntimeTab();

        // when
        runtimeTab.selectRowByActivityId(processDefinitionId);
        detailView.openRuntimeVariablesTab()
                .getCreateButton()
                .click();

        // then
        $j(VariableInstanceDataDetailDialog.class)
                .exists()
                .displayed()
                .getLocalField()
                .shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Scope field is hidden when Local field is checked")
    void givenCreateDialogWithLocalFlagAvailable_whenLocalCheckboxChecked_thenScopeFieldHidden() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess");
        String instanceId = dataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();

        MainView mainView = loginAsAdmin();

        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);
        RuntimeTabFragment runtimeTab = detailView.openRuntimeTab();
        runtimeTab.selectRowByActivityId("subProcess");
        detailView.openRuntimeVariablesTab()
                .getCreateButton()
                .click();

        VariableInstanceDataDetailDialog dialog = $j(VariableInstanceDataDetailDialog.class).exists().displayed();

        // when
        dialog.getLocalField().setChecked(true);

        // then
        dialog.getScopeField().shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Scope column has sub-process value for created local variable")
    void givenSubProcessActivitySelected_whenLocalVariableCreated_thenVariableAppearsWithSubProcessScope() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess");
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
        dialog.getNameField().setValue("myLocalVariable");
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
        variablesTab.getRowByVariableName("myLocalVariable")
                .getCellByIndex(SCOPE_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("Sub process"));
    }

    @Test
    @DisplayName("Scope column has process instance value for created global variable")
    void givenNoActivityInstanceSelected_whenNonLocalVariableCreated_thenVariableAppearsWithProcessInstanceScope() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess");
        String instanceId = dataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();

        MainView mainView = loginAsAdmin();

        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();
        variablesTab.getCreateButton().click();

        // when
        VariableInstanceDataDetailDialog dialog = $j(VariableInstanceDataDetailDialog.class).exists().displayed();
        dialog.getNameField().setValue("myGlobalVariable");
        dialog.getTypeComboBox()
                .shouldBe(VISIBLE)
                .setValue("String");
        dialog.getValueComponentAs(TextField.class)
                .shouldBe(VISIBLE)
                .setValue("globalValue");
        dialog.getSaveBtn().click();

        dialog.shouldNotBe(VISIBLE);

        // then
        variablesTab.getRowByVariableName("myGlobalVariable")
                .getCellByIndex(NAME_COLUMN_INDEX)
                .getCellContent()
                .find(NAME_BUTTON_BY)
                .shouldHave(text("myGlobalVariable"));
    }

    @Test
    @DisplayName("Value column is not empty for created Date variable with default value")
    void givenCreateDialogWithDateType_whenSavedWithDefaultValue_thenVariableAppearsWithNonEmptyValue() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess");
        String instanceId = dataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();

        MainView mainView = loginAsAdmin();

        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();
        variablesTab.getCreateButton().click();

        VariableInstanceDataDetailDialog dialog = $j(VariableInstanceDataDetailDialog.class).exists().displayed();
        dialog.getNameField().setValue("myDateVariable");
        dialog.getTypeComboBox()
                .shouldBe(VISIBLE)
                .setValue("Date");

        dialog.getValueComponentElement().shouldBe(visible);

        // when
        dialog.getSaveBtn().click();

        dialog.shouldNotBe(VISIBLE);

        // then
        variablesTab.getRowByVariableName("myDateVariable")
                .getCellByIndex(VALUE_COLUMN_INDEX)
                .getCellContent()
                .shouldNotBe(empty);
    }
}
