/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processdefinition;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processdefinition.ProcessDefinitionListView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.stream.Stream;

import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static io.flowset.control.test_support.ui.condition.ControlCondition.*;
import static io.flowset.control.test_support.ui.view.processdefinition.ProcessDefinitionListView.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Filters on Process list view")
public class ProcessDefinitionListViewFilterUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;


    @ParameterizedTest
    @ValueSource(strings = {"Active", "Suspended"})
    @DisplayName("State filter shows only processes with selected state")
    void givenDeployedProcesses_whenSelectStateInDropdown_thenDataGridFiltered(String state) {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .deploy("test_support/contractApproval.bpmn")
                .suspendByKey("contractApproval", false);

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();

        // then
        listView.getProcessDefinitionsGrid()
                .shouldHave(visibleBodyRowCount(2));

        listView.getStateComboBox().setValue(state);

        listView.getProcessDefinitionsGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(STATE_COLUMN_INDEX, state));
    }

    @ParameterizedTest
    @MethodSource("processFilteredByNameSource")
    @DisplayName("Name filter: on filter apply shows only processes in data grid with suitable name")
    void givenDeployedProcesses_whenEnterNameFilterAndApply_thenDataGridFiltered(String nameFilter,
                                                                                 List<String> expectedProcessNames) {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .deploy("test_support/contractApproval.bpmn")
                .deploy("test_support/supportRequest.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();

        // then
        listView.getProcessDefinitionsGrid()
                .shouldHave(visibleBodyRowCount(3));

        listView.getNameField().setValue(nameFilter);

        listView.getApplyFilterBtn().click();

        listView.getProcessDefinitionsGrid()
                .shouldHave(visibleBodyRowCount(expectedProcessNames.size()))
                .shouldHave(allBodyRowsHaveCellElementTextIn(NAME_COLUMN_INDEX, NAME_BUTTON_BY, expectedProcessNames));
    }

    @ParameterizedTest
    @MethodSource("processFilteredByKeySource")
    @DisplayName("Key filter: on filter apply shows only processes in data grid with suitable key")
    void givenDeployedProcesses_whenEnterKeyFilterAndApply_thenDataGridFiltered(String keyFilter,
                                                                                List<String> expectedProcessKeys) {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .deploy("test_support/contractApproval.bpmn")
                .deploy("test_support/supportRequest.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();

        // then
        listView.getProcessDefinitionsGrid()
                .shouldHave(visibleBodyRowCount(3));

        listView.getKeyField().setValue(keyFilter);

        listView.getApplyFilterBtn().click();

        listView.getProcessDefinitionsGrid()
                .shouldHave(visibleBodyRowCount(expectedProcessKeys.size()))
                .shouldHave(allBodyRowsHaveCellElementTextIn(KEY_COLUMN_INDEX, KEY_BUTTON_BY, expectedProcessKeys));
    }

    @Test
    @DisplayName("'Last version' checkbox toggles between latest-only and all-versions")
    void givenDeployedProcessVersions_whenToggleLatestVersionCheckbox_thenDataGridFiltered() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();

        // then
        listView.getProcessDefinitionsGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(KEY_COLUMN_INDEX, "vacation_approval"))
                .shouldHave(allBodyRowsHaveCellText(VERSION_COLUMN_INDEX, "2"));


        listView.getLatestVersionCheckbox().setChecked(false);

        // then
        listView.getProcessDefinitionsGrid()
                .shouldHave(visibleBodyRowCount(2))
                .shouldHave(allBodyRowsHaveCellText(KEY_COLUMN_INDEX, "vacation_approval"))
                .shouldHave(anyBodyRowHaveCellText(VERSION_COLUMN_INDEX, "1"))
                .shouldHave(anyBodyRowHaveCellText(VERSION_COLUMN_INDEX, "2"));
    }

    @Test
    @DisplayName("Name filter applying updates URL with name query parameter")
    void givenDeployedProcesses_whenSetNameFilterAndApply_thenUrlHasNameParam() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .deploy("test_support/contractApproval.bpmn")
                .deploy("test_support/supportRequest.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();
        listView.getNameField().setValue("approval");
        listView.getApplyFilterBtn().click();

        // then
        webdriver().shouldHave(urlContaining("name=approval"));
    }

    @Test
    @DisplayName("Clicking 'Clear' button resets the filter and refreshes the grid")
    void givenFilteredProcessList_whenClearFilter_thenGridRefreshed() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .deploy("test_support/contractApproval.bpmn");

        MainView mainView = loginAsAdmin();

        ProcessDefinitionListView listView = mainView.openProcessListView()
                .waitUntilDataLoading();

        listView.getKeyField().setValue("vacation_approval");
        listView.getApplyFilterBtn().click();

        // when
        listView.getClearBtn().click();

        // then
        listView.getProcessDefinitionsGrid()
                .shouldHave(visibleBodyRowCount(2))
                .shouldHave(anyBodyRowHaveCellText(KEY_COLUMN_INDEX, "vacation_approval"))
                .shouldHave(anyBodyRowHaveCellText(KEY_COLUMN_INDEX, "contractApproval"));
    }

    @Test
    @DisplayName("Opening list view with filter query parameters pre-applies the filter")
    void givenDeployedProcesses_whenOpenWithFilterParamsInUrl_thenFilterPreapplied() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .deploy("test_support/contractApproval.bpmn")
                .deploy("test_support/supportRequest.bpmn")
                .suspendByKey("contractApproval", false);

        loginAsAdmin();

        // when
        open("/bpm/process-definitions?name=approval&state=suspended");

        ProcessDefinitionListView listView = $j(ProcessDefinitionListView.class)
                .exists()
                .displayed();

        // then
        listView.getProcessDefinitionsGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellElementText(KEY_COLUMN_INDEX, KEY_BUTTON_BY, "contractApproval"))
                .shouldHave(allBodyRowsHaveCellText(STATE_COLUMN_INDEX, "Suspended"));
    }

    public static Stream<Arguments> processFilteredByNameSource() {
        return Stream.of(
                // filter value, expected process names
                Arguments.of("approval",
                        List.of("Vacation approval", "Contract Approval")),
                Arguments.of("Approval",
                        List.of("Vacation approval", "Contract Approval")),
                Arguments.of("",
                        List.of("Vacation approval", "Contract Approval", "Support request")),
                Arguments.of("Support request",
                        List.of("Support request")),
                Arguments.of("unknown",
                        List.of())

        );
    }

    public static Stream<Arguments> processFilteredByKeySource() {
        return Stream.of(
                //  filter value, expected process keys
                Arguments.of("approval",
                        List.of("vacation_approval")),
                Arguments.of("Approval",
                        List.of("contractApproval")),
                Arguments.of("",
                        List.of("vacation_approval", "contractApproval", "supportRequest")),
                Arguments.of("supportRequest",
                        List.of("supportRequest")),
                Arguments.of("unknown",
                        List.of())
        );
    }
}
