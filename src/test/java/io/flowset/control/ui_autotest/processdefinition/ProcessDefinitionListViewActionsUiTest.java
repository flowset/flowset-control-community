/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processdefinition;

import com.codeborne.selenide.SelenideElement;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processdefinition.NewProcessDeploymentView;
import io.flowset.control.test_support.ui.view.processdefinition.ProcessDefinitionDiagramDialog;
import io.flowset.control.test_support.ui.view.processdefinition.ProcessDefinitionListView;
import io.flowset.control.test_support.ui.view.processdefinition.StartProcessWithVariableDialog;
import io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessDefinitionDetailView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.CollectionCondition.texts;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static io.flowset.control.test_support.ui.UiTestSupport.getVisibleDropdownItems;
import static io.flowset.control.test_support.ui.condition.ControlCondition.anyBodyRowHaveCellText;
import static io.flowset.control.test_support.ui.condition.ControlCondition.emptyGrid;
import static io.flowset.control.test_support.ui.view.processdefinition.ProcessDefinitionListView.*;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions (except bulk actions) on Process list view")
public class ProcessDefinitionListViewActionsUiTest extends AbstractCamunda7UiTest {

    @Autowired
    ApplicationContext applicationContext;

    @RunningExternalEngine
    ExternalEngine engine;

    @Test
    @DisplayName("Refresh action availability on process list view")
    void givenLoggedInUser_whenOpenProcessList_thenRefreshActionEnabledRuleApplied() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();

        // then
        listView.getRefreshBtn()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        listView.openProcessGridContextActions()
                .find(text("Refresh"))
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Refresh action reloads data in the data grid")
    void givenNewlyDeployedProcessDefinition_whenClickRefresh_thenGridReloaded() {
        // given
        MainView mainView = loginAsAdmin();
        ProcessDefinitionListView listView = mainView.openProcessListView()
                .waitUntilDataLoading();

        listView.getProcessDefinitionsGrid()
                .shouldBe(emptyGrid);

        applicationContext.getBean(CamundaSampleDataManager.class, engine)
                .deploy("test_support/vacationApproval.bpmn");

        // when
        listView.getRefreshBtn().click();

        // then
        listView.getProcessDefinitionsGrid()
                .shouldHave(anyBodyRowHaveCellText(KEY_COLUMN_INDEX, "vacation_approval"));
    }

    @Test
    @DisplayName("Deploy action opens New process deployment view")
    void givenLoggedInUser_whenOpenProcessListAndClickDeploy_thenNewProcessDeploymentViewOpened() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();
        listView.getDeployBtn().click();

        // then
        $j(NewProcessDeploymentView.class).exists()
                .displayed();
    }

    @Test
    @DisplayName("View action in the Name column in row opens process detail view")
    void givenExistingProcessDefinition_whenClickViewActionInNameColumn_thenProcessDetailViewOpened() {
        // given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, engine)
                .deploy("test_support/vacationApproval.bpmn");

        String processId = sampleDataManager.getDeployedProcessVersions("vacation_approval").get(0);
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();
        listView.getRowByProcessKey("vacation_approval")
                .getCellByIndex(NAME_COLUMN_INDEX)
                .getCellContent()
                .find(NAME_BUTTON_BY).click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/process-definitions/" + processId));

        $j(ProcessDefinitionDetailView.class).exists()
                .displayed();
    }

    @Test
    @DisplayName("View action in the Key column in row opens process detail view")
    void givenExistingProcessDefinition_whenClickViewActionInKeyColumn_thenProcessDetailViewOpened() {
        // given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, engine)
                .deploy("test_support/vacationApproval.bpmn");

        String processId = sampleDataManager.getDeployedProcessVersions("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();
        listView.getRowByProcessKey("vacation_approval")
                .getCellByIndex(KEY_COLUMN_INDEX)
                .getCellContent()
                .find(KEY_BUTTON_BY).click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/process-definitions/" + processId));

        $j(ProcessDefinitionDetailView.class).exists()
                .displayed();
    }

    @Test
    @DisplayName("Double-clicking a row opens the process detail view")
    void givenExistingProcessDefinition_whenDoubleClickRow_thenProcessDetailViewOpened() {
        // given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, engine)
                .deploy("test_support/vacationApproval.bpmn");
        String processId = sampleDataManager.getDeployedProcessVersions("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();
        listView.getProcessDefinitionsGrid()
                .getRowByIndex(0)
                .getDelegate()
                .doubleClick();

        // then
        webdriver().shouldHave(urlContaining("/bpm/process-definitions/" + processId));

        $j(ProcessDefinitionDetailView.class).exists();
    }

    @Test
    @DisplayName("Preview action for process opens diagram dialog")
    void givenExistingProcessDefinition_whenClickPreviewInRow_thenDiagramDialogOpened() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, engine)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();

        // then
        listView.getRowByProcessKey("vacation_approval")
                .getCellByIndex(KEY_COLUMN_INDEX)
                .getCellContent()
                .find(PREVIEW_BUTTON_BY).click();

        ProcessDefinitionDiagramDialog dialog = $j(ProcessDefinitionDiagramDialog.class).exists()
                .displayed();
        dialog.getKeyField().shouldHave(value("vacation_approval"));
        dialog.getVersionField().shouldHave(value("1"));
    }


    @Test
    @DisplayName("Start action for process opens Start process dialog")
    void givenExistingActiveProcessDefinition_whenClickStartInRow_thenStartProcessDialogOpened() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, engine)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();

        // then
        listView.getRowByProcessKey("vacation_approval")
                .getCellByIndex(ACTIONS_COLUMN_INDEX)
                .getCellContent()
                .find(START_PROCESS_BUTTON_BY).click();

        StartProcessWithVariableDialog dialog = $j(StartProcessWithVariableDialog.class).exists()
                .displayed();
        dialog.getNameField().shouldHave(value("Vacation approval"));
        dialog.getVersionField().shouldHave(value("1"));
    }

    @Test
    @DisplayName("Other actions availability for active process on process list view")
    void givenExistingActiveProcessDefinition_whenOpenProcessList_thenOtherDropdownActionsAvailable() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, engine)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();

        // then
        SelenideElement dropdown = listView.getRowByProcessKey("vacation_approval")
                .getCellByIndex(ACTIONS_COLUMN_INDEX)
                .getCellContent()
                .find(OTHER_PROCESS_ACTIONS_BY)
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        getVisibleDropdownItems(dropdown)
                .shouldHave(size(3))
                .shouldHave(texts("Remove", "Migrate", "Suspend"));
    }

    @Test
    @DisplayName("Other actions availability for suspended process on process list view")
    void givenExistingSuspendedProcessDefinition_whenOpenProcessList_thenOtherDropdownActionsAvailable() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, engine)
                .deploy("test_support/vacationApproval.bpmn")
                .suspendByKey("vacation_approval", false);

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();

        // then
        SelenideElement dropdown = listView.getRowByProcessKey("vacation_approval")
                .getCellByIndex(ACTIONS_COLUMN_INDEX)
                .getCellContent()
                .find(OTHER_PROCESS_ACTIONS_BY)
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        getVisibleDropdownItems(dropdown)
                .shouldHave(size(2))
                .shouldHave(texts("Remove", "Migrate"));
    }

    @Test
    @DisplayName("List of available actions on process list view")
    void givenExistingProcessDefinition_whenOpenProcessList_thenAllActionsAvailable() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, engine)
                .deploy("test_support/vacationApproval.bpmn");
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();

        // then
        listView.getRefreshBtn().shouldBe(VISIBLE);
        listView.getDeployBtn().shouldBe(VISIBLE);
        listView.getBulkRemoveBtn().shouldBe(VISIBLE);
        listView.getBulkActivateBtn().shouldBe(VISIBLE);
        listView.getBulkSuspendBtn().shouldBe(VISIBLE);

        listView.openProcessGridContextActions()
                .shouldHave(visibleItems("Refresh", "Deploy", "Remove", "Activate", "Suspend"));
    }
}
