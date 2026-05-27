/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processdefinition.list.inlineaction;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processdefinition.ProcessDefinitionListView;
import io.flowset.control.test_support.ui.view.processdefinition.action.SuspendProcessDefinitionDialog;
import io.jmix.masquerade.component.DataGrid;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.text;
import static io.flowset.control.test_support.ui.view.processdefinition.ProcessDefinitionListView.STATE_COLUMN_INDEX;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Suspend action on Process list view")
public class ProcessDefinitionListViewSuspendUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Suspend action: process state is updated in data grid after confirmation")
    void givenExistingActiveProcessDefinition_whenSuspendConfirmed_thenProcessSuspended() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();

        DataGrid.Row processRow = listView.getRowByProcessKey("vacation_approval");
        listView.openOtherActions(processRow)
                .find(text("Suspend")).click();

        $j(SuspendProcessDefinitionDialog.class)
                .exists()
                .displayed()
                .getSuspendBtn().click();

        // then
        listView.getRowByProcessKey("vacation_approval")
                .getCellByIndex(STATE_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("Suspended"));
    }

    @Test
    @DisplayName("Suspend action: process state is not changed in data grid after cancellation")
    void givenExistingActiveProcessDefinition_whenSuspendCancelled_thenProcessActive() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();

        DataGrid.Row processRow = listView.getRowByProcessKey("vacation_approval");
        listView.openOtherActions(processRow)
                .find(text("Suspend")).click();

        $j(SuspendProcessDefinitionDialog.class)
                .exists()
                .displayed()
                .getCancelBtn().click();

        // then
        listView.getRowByProcessKey("vacation_approval")
                .getCellByIndex(STATE_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("Active"));
    }
}
