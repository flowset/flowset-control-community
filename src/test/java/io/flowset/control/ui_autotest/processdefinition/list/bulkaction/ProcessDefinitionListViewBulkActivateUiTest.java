/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processdefinition.list.bulkaction;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processdefinition.ProcessDefinitionListView;
import io.flowset.control.test_support.ui.view.processdefinition.action.BulkActivateProcessDefinitionView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.text;
import static io.flowset.control.test_support.ui.condition.ControlCondition.allBodyRowsHaveCellText;
import static io.flowset.control.test_support.ui.condition.ControlCondition.visibleBodyRowCount;
import static io.flowset.control.test_support.ui.view.processdefinition.ProcessDefinitionListView.STATE_COLUMN_INDEX;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Bulk Activate action on Process list view")
public class ProcessDefinitionListViewBulkActivateUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Activate action enabled if suspended processes selected in data grid")
    void givenExistingSuspendedProcessDefinition_whenSelectSuspendedProcess_thenActivateActionEnabled() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .suspendByKey("vacation_approval", false);

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();

        // then
        listView.getBulkActivateBtn()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        GridContextMenu gridContextMenu = listView.openProcessGridContextActions();
        gridContextMenu.find(text("Activate"))
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        gridContextMenu.close();

        listView.getProcessDefinitionsGrid().clickSelectAll();

        listView.getBulkActivateBtn()
                .shouldBe(ENABLED);

        listView.openProcessGridContextActions()
                .find(text("Activate"))
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Activate action disabled when active processes are selected")
    void givenExistingActiveProcessDefinition_whenSelectActiveProcesses_thenActivateActionDisabled() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView()
                .waitUntilDataLoading();
        listView.getProcessDefinitionsGrid().clickSelectAll();

        // then
        listView.getBulkActivateBtn()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        listView.openProcessGridContextActions()
                .find(text("Activate"))
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);
    }

    @Test
    @DisplayName("Activate action: process state is updated in data grid after confirmation")
    void givenMultipleSuspendedProcessDefinitions_whenBulkActivateConfirmed_thenAllSelectedProcessesActivated() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .deploy("test_support/contractApproval.bpmn")
                .suspendByKey("vacation_approval", false)
                .suspendByKey("contractApproval", false);

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView()
                .waitUntilDataLoading();
        listView.getProcessDefinitionsGrid().clickSelectAll();
        listView.getBulkActivateBtn()
                .shouldBe(ENABLED).click();

        $j(BulkActivateProcessDefinitionView.class)
                .exists()
                .displayed()
                .getActivateBtn()
                .shouldBe(VISIBLE).click();

        // then
        listView.getProcessDefinitionsGrid()
                .shouldHave(visibleBodyRowCount(2))
                .shouldHave(allBodyRowsHaveCellText(STATE_COLUMN_INDEX, "Active"));
    }

    @Test
    @DisplayName("Activate action: process state is not changed in data grid after cancellation")
    void givenMultipleSuspendedProcessDefinitions_whenBulkActivateCancelled_thenAllSelectedProcessesSuspended() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .deploy("test_support/contractApproval.bpmn")
                .suspendByKey("vacation_approval", false)
                .suspendByKey("contractApproval", false);

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView()
                .waitUntilDataLoading();
        listView.getProcessDefinitionsGrid().clickSelectAll();
        listView.getBulkActivateBtn()
                .shouldBe(ENABLED).click();

        $j(BulkActivateProcessDefinitionView.class)
                .exists()
                .displayed()
                .getCancelBtn()
                .shouldBe(VISIBLE).click();

        // then
        listView.getProcessDefinitionsGrid()
                .shouldHave(visibleBodyRowCount(2))
                .shouldHave(allBodyRowsHaveCellText(STATE_COLUMN_INDEX, "Suspended"));
    }
}
