/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processdefinition;

import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.security.role.processdefinition.TestProcessDefinitionNoDataViewRole;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processdefinition.ProcessDefinitionListView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.stream.Stream;

import static com.codeborne.selenide.Condition.visible;
import static io.flowset.control.test_support.ui.TagNames.GRID_COLUMN_SORTER_BY;
import static io.flowset.control.test_support.ui.condition.ControlCondition.*;
import static io.flowset.control.test_support.ui.view.processdefinition.ProcessDefinitionListView.*;

@WithRunningExternalEngine
@DisplayName("Processes Grid on Process list view")
public class ProcessDefinitionListViewGridUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ControlTestDataCreator controlTestDataCreator;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDownUsers() {
        jdbcTemplate.update("delete from SEC_ROLE_ASSIGNMENT where USERNAME like 'test-user%'");
        jdbcTemplate.update(
                "delete from SEC_RESOURCE_POLICY where ROLE_ID in (select ID from SEC_RESOURCE_ROLE " +
                        "where CODE like 'test-%')"
        );
        jdbcTemplate.update("delete from SEC_RESOURCE_ROLE where CODE like 'test-%'");
        jdbcTemplate.update("delete from USER_ where USERNAME like 'test-user%'");
    }

    @Test
    @DisplayName("The grid displays Name, Key, Version, State columns")
    void givenLoggedInUser_whenOpenProcessList_thenGridDisplaysExpectedColumns() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();

        // then
        listView.getProcessDefinitionsGrid()
                .shouldHave(headerCellTexts(List.of("Name", "Key", "Version", "State")));
    }

    @Test
    @DisplayName("The grid does not displays Name, Key, Version, State columns without process read access")
    void givenUserWithoutProcessDefinitionDataReadAccess_whenOpenProcessList_thenGridHeaderColumnsAreEmpty() {
        // given
        controlTestDataCreator.createUser(
                "test-user-process-grid-no-data-view",
                "password",
                TestProcessDefinitionNoDataViewRole.class
        );

        MainView mainView = loginAs("test-user-process-grid-no-data-view", "password");

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView()
                .waitUntilDataLoading();

        // then
        listView.getProcessDefinitionsGrid()
                .shouldHave(headerCellTexts(List.of()));
    }

    @Test
    @DisplayName("Sort actions available for Name, Key, Version columns")
    void givenLoggedInUser_whenOpenProcessList_thenSortIconsAvailableOnColumns() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();
        listView.waitUntilDataLoading();

        // then
        listView.getProcessDefinitionsGrid()
                .shouldHave(columnSortable(NAME_COLUMN_INDEX))
                .shouldHave(columnSortable(KEY_COLUMN_INDEX))
                .shouldHave(columnSortable(VERSION_COLUMN_INDEX))
                .shouldHave(columnNotSortable(STATE_COLUMN_INDEX));
    }

    @ParameterizedTest
    @MethodSource("sortDataSource")
    @DisplayName("Processes are sorted by column after clicking on column header")
    void givenDeployedProcesses_whenSortByColumn_thenSortedByColumnDataDisplayed(Integer column, List<String> expectedValueOrders) {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .deploy("test_support/contractApproval.bpmn")
                .deploy("test_support/vacationApproval.bpmn");
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView()
                .waitUntilDataLoading();

        listView.getLatestVersionCheckbox().setChecked(false);

        listView.getProcessDefinitionsGrid()
                .getHeaderCellByIndex(0, column)
                .getCellContent()
                .find(GRID_COLUMN_SORTER_BY)
                .shouldBe(visible)
                .click();

        // then
        listView.getProcessDefinitionsGrid()
                .shouldHave(bodyCellTextsExactly(column, expectedValueOrders));
    }

    public static Stream<Arguments> sortDataSource() {
        return Stream.of(
                Arguments.of(NAME_COLUMN_INDEX, List.of("Contract Approval", "Vacation approval", "Vacation approval")),
                Arguments.of(KEY_COLUMN_INDEX, List.of("contractApproval", "vacation_approval", "vacation_approval")),
                Arguments.of(VERSION_COLUMN_INDEX, List.of("1", "1", "2"))
        );
    }
}
