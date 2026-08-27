/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.detail.tab.history;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.request.StartProcessDto;
import io.flowset.control.test_support.camunda7.dto.request.VariableValueDto;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.historicvariableinstancedata.HistoricVariableInstanceDataDetailDialog;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryVariablesTabFragment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.value;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryVariablesTabFragment.NAME_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryVariablesTabFragment.NAME_COLUMN_INDEX;
import static io.jmix.masquerade.JConditions.visibleItemsCount;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions in the Variables tab (History tab) in Process instance detail view")
public class HistoryVariablesTabActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("No actions available for the Variables data grid")
    void givenExistingHistoricVariable_whenOpenHistoryVariablesTab_thenNoGridActionsAvailable() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", StartProcessDto.builder()
                        .variable("firstVariable", new VariableValueDto("String", "Some value"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").get(0);

        MainView mainView = loginAsAdmin();

        // when
        HistoryVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryVariablesTab();

        variablesTab.selectRowByVariableName("firstVariable");

        // then
        variablesTab.openVariablesGridContextMenu()
                .shouldHave(visibleItemsCount(0));
    }

    @Test
    @DisplayName("Variable Name link in data grid opens Historic variable instance detail dialog")
    void givenExistingHistoricVariable_whenVariableNameLinkClicked_thenHistoricVariableDetailDialogOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", StartProcessDto.builder()
                        .variable("firstVariable", new VariableValueDto("String", "Some value"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").get(0);

        MainView mainView = loginAsAdmin();

        // when
        HistoryVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryVariablesTab();

        variablesTab.getRowByVariableName("firstVariable")
                .getCellByIndex(NAME_COLUMN_INDEX)
                .getCellContent()
                .find(NAME_BUTTON_BY).click();

        // then
        HistoricVariableInstanceDataDetailDialog dialog = $j(HistoricVariableInstanceDataDetailDialog.class).exists()
                .displayed();
        dialog.getNameField().shouldHave(value("firstVariable"));
        dialog.getTypeField().shouldHave(value("String"));
    }

    @Test
    @DisplayName("Double-clicking a variable row opens Historic variable instance detail dialog")
    void givenExistingHistoricVariable_whenRowDoubleClicked_thenHistoricVariableDetailDialogOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", StartProcessDto.builder()
                        .variable("firstVariable", new VariableValueDto("String", "Some value"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").get(0);

        MainView mainView = loginAsAdmin();

        // when
        HistoryVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryVariablesTab();

        variablesTab.getRowByVariableName("firstVariable")
                .getDelegate()
                .doubleClick();

        // then
        HistoricVariableInstanceDataDetailDialog dialog = $j(HistoricVariableInstanceDataDetailDialog.class).exists()
                .displayed();
        dialog.getNameField().shouldHave(value("firstVariable"));
        dialog.getTypeField().shouldHave(value("String"));
    }
}
