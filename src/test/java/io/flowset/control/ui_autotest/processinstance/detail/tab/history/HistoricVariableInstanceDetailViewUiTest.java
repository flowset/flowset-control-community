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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.exactValue;
import static com.codeborne.selenide.Condition.value;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryVariablesTabFragment.NAME_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryVariablesTabFragment.NAME_COLUMN_INDEX;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Fields of the historic variable instance detail dialog")
public class HistoricVariableInstanceDetailViewUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Detail view shows the historic variable instance data")
    void givenExistingHistoricVariable_whenOpenHistoricVariableDetailDialog_thenVariableDataShown() {
        // given
        String instanceId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", StartProcessDto.builder()
                        .variable("firstVariable", new VariableValueDto("String", "Some value"))
                        .build())
                .getStartedInstances("testUpdateVariable")
                .getFirst();

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryVariablesTab()
                .getRowByVariableName("firstVariable")
                .getCellByIndex(NAME_COLUMN_INDEX)
                .getCellContent()
                .find(NAME_BUTTON_BY)
                .click();

        // then
        HistoricVariableInstanceDataDetailDialog dialog = $j(HistoricVariableInstanceDataDetailDialog.class)
                .exists()
                .displayed();
        dialog.getNameField().shouldHave(exactValue("firstVariable"));
        dialog.getTypeField().shouldHave(exactValue("String"));
        dialog.getActivityInstanceIdField().shouldHave(exactValue(instanceId));
        dialog.getStateField().shouldHave(exactValue("CREATED"));
    }

    @Test
    @DisplayName("Error message is shown for an object variable that cannot be deserialized")
    void givenExistingObjectVariableOfUnknownClass_whenOpenHistoricVariableDetailDialog_thenErrorMessageShown() {
        // given
        String instanceId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", StartProcessDto.builder()
                        .variable("objectVariable", new VariableValueDto("Object", "{\"amount\":10}")
                                .setValueInfo(new VariableValueDto.ValueInfo()
                                        .setObjectTypeName("com.example.UnknownClass")
                                        .setSerializationDataFormat("application/json")))
                        .build())
                .getStartedInstances("testUpdateVariable")
                .getFirst();

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryVariablesTab()
                .getRowByVariableName("objectVariable")
                .getCellByIndex(NAME_COLUMN_INDEX)
                .getCellContent()
                .find(NAME_BUTTON_BY)
                .click();

        // then
        HistoricVariableInstanceDataDetailDialog dialog = $j(HistoricVariableInstanceDataDetailDialog.class)
                .exists()
                .displayed();
        dialog.getErrorMessageField().shouldHave(value("Cannot deserialize object in variable 'objectVariable'"));
    }
}
