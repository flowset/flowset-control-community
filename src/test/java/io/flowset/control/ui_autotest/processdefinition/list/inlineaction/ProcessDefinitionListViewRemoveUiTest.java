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
import io.flowset.control.test_support.ui.view.processdefinition.action.DeleteProcessDefinitionDialog;
import io.jmix.masquerade.component.DataGrid;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.text;
import static io.flowset.control.test_support.ui.condition.ControlCondition.*;
import static io.flowset.control.test_support.ui.view.processdefinition.ProcessDefinitionListView.KEY_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.processdefinition.ProcessDefinitionListView.KEY_COLUMN_INDEX;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Remove action on Process list view")
public class ProcessDefinitionListViewRemoveUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Remove action: process is removed from the data grid after confirmation")
    void givenExistingProcessDefinition_whenRemoveConfirmed_thenProcessRemoved() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();

        DataGrid.Row processRow = listView.getRowByProcessKey("vacation_approval");

        listView.openOtherActions(processRow)
                .find(text("Remove")).click();

        $j(DeleteProcessDefinitionDialog.class)
                .exists()
                .displayed()
                .getOkBtn().click();

        // then
        listView.getProcessDefinitionsGrid()
                .shouldBe(emptyGrid);
    }

    @Test
    @DisplayName("Remove action: process is not removed from the data grid after cancellation")
    void givenExistingProcessDefinition_whenRemoveCancelled_thenProcessNotRemoved() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();

        DataGrid.Row processRow = listView.getRowByProcessKey("vacation_approval");
        listView.openOtherActions(processRow)
                .find(text("Remove")).click();

        $j(DeleteProcessDefinitionDialog.class)
                .exists()
                .displayed()
                .getCancelBtn().click();

        // then
        listView.getProcessDefinitionsGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellElementText(KEY_COLUMN_INDEX, KEY_BUTTON_BY, "vacation_approval"));
    }
}
