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
import io.flowset.control.test_support.ui.view.processdefinition.action.BulkSuspendProcessDefinitionView;
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
@DisplayName("Bulk Suspend action on Process list view")
public class ProcessDefinitionListViewBulkSuspendUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Suspend action enabled if active processes selected in data grid")
    void givenExistingActiveProcessDefinition_whenSelectActiveProcess_thenSuspendActionEnabled() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();

        // then
        listView.getBulkSuspendBtn()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        GridContextMenu gridContextMenu = listView.openProcessGridContextActions();
        gridContextMenu.find(text("Suspend"))
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        gridContextMenu.close();

        listView.getProcessDefinitionsGrid().clickSelectAll();

        listView.getBulkSuspendBtn()
                .shouldBe(ENABLED);

        listView.openProcessGridContextActions()
                .find(text("Suspend"))
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Suspend action disabled when suspended processes are selected")
    void givenExistingSuspendedProcessDefinition_whenOpenProcessList_thenSuspendActionDisabled() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .suspendByKey("vacation_approval", false);

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView()
                .waitUntilDataLoading();
        listView.getProcessDefinitionsGrid().clickSelectAll();

        // then
        listView.getBulkSuspendBtn()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        listView.openProcessGridContextActions()
                .find(text("Suspend"))
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);
    }

    @Test
    @DisplayName("Suspend action: process state is updated in data grid after confirmation")
    void givenMultipleActiveProcessDefinitions_whenBulkSuspendConfirmed_thenAllSelectedProcessesSuspended() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .deploy("test_support/contractApproval.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView()
                .waitUntilDataLoading();
        listView.getProcessDefinitionsGrid().clickSelectAll();
        listView.getBulkSuspendBtn()
                .shouldBe(ENABLED).click();

        $j(BulkSuspendProcessDefinitionView.class)
                .exists()
                .displayed()
                .getSuspendBtn()
                .click();

        // then
        listView.getProcessDefinitionsGrid()
                .shouldHave(visibleBodyRowCount(2))
                .shouldHave(allBodyRowsHaveCellText(STATE_COLUMN_INDEX, "Suspended"));
    }

    @Test
    @DisplayName("Suspend action: process state is not changed in data grid after cancellation")
    void givenMultipleActiveProcessDefinitions_whenBulkSuspendCancelled_thenAllSelectedProcessesActive() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .deploy("test_support/contractApproval.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView()
                .waitUntilDataLoading();
        listView.getProcessDefinitionsGrid().clickSelectAll();
        listView.getBulkSuspendBtn()
                .shouldBe(ENABLED).click();

        $j(BulkSuspendProcessDefinitionView.class)
                .exists()
                .displayed()
                .getCancelBtn()
                .shouldBe(VISIBLE).click();

        // then
        listView.getProcessDefinitionsGrid()
                .shouldHave(visibleBodyRowCount(2))
                .shouldHave(allBodyRowsHaveCellText(STATE_COLUMN_INDEX, "Active"));
    }
}
