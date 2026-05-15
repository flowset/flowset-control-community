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
import io.flowset.control.test_support.ui.view.processdefinition.action.ActivateProcessDefinitionDialog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.text;
import static io.flowset.control.test_support.ui.view.processdefinition.ProcessDefinitionListView.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Activate action on Process list view")
public class ProcessDefinitionListViewActivateUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Activate action: process state is updated in data grid after confirmation")
    void givenExistingSuspendedProcessDefinition_whenActivateConfirmed_thenProcessActivated() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/contractApproval.bpmn")
                .suspendByKey("contractApproval", false);

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();

        listView.getRowByProcessKey("contractApproval")
                .getCellByIndex(ACTIONS_COLUMN_INDEX)
                .getCellContent()
                .find(ACTIVATE_BUTTON_BY).click();

        $j(ActivateProcessDefinitionDialog.class).exists()
                .displayed()
                .getActivateBtn().click();

        // then
        listView.getRowByProcessKey("contractApproval")
                .getCellByIndex(STATE_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("Active"));
    }

    @Test
    @DisplayName("Activate action: process state is not changed in data grid after cancellation")
    void givenExistingSuspendedProcessDefinition_whenActivateCancelled_thenProcessSuspended() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/contractApproval.bpmn")
                .suspendByKey("contractApproval", false);

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();

        listView.getRowByProcessKey("contractApproval")
                .getCellByIndex(ACTIONS_COLUMN_INDEX)
                .getCellContent()
                .find(ACTIVATE_BUTTON_BY).click();

        $j(ActivateProcessDefinitionDialog.class)
                .exists()
                .displayed()
                .getCancelBtn().click();

        // then
        listView.getRowByProcessKey("contractApproval")
                .getCellByIndex(STATE_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("Suspended"));
    }
}
