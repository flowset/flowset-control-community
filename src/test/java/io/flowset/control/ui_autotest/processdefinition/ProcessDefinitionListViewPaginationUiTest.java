/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processdefinition;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
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
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Stream;

import static com.codeborne.selenide.Condition.text;
import static io.flowset.control.test_support.ui.condition.ControlCondition.*;

@WithRunningExternalEngine
@DisplayName("Pagination on Process list view")
public class ProcessDefinitionListViewPaginationUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @ParameterizedTest
    @MethodSource("paginationDataSource")
    @DisplayName("Paginated data is shown on selected page")
    void givenDeployedProcesses_whenPageChange_thenPaginatedDataDisplayed(Integer pageNumber, int expectedRowCount,
                                                                          String expectedRangeText) {
        // given
        camundaRestTestHelper.createDeploymentFromZip(camunda7,
                "test_support/processesForPaginationTest.zip");

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView()
                .waitUntilDataLoading();

        listView.openProcessDataGridPage(pageNumber);

        // then
        listView.getProcessDefinitionsGrid()
                .shouldHave(gridBodyRowCount(expectedRowCount));

        listView.getPagination()
                .shouldHave(text(expectedRangeText));
    }

    public static Stream<Arguments> paginationDataSource() {
        return Stream.of(
                //  page number (starts with 0), expected show rows, expected range text
                Arguments.of(0, 50, "1-50 rows of [?]"),
                Arguments.of(1, 15, "51-65 rows")
        );
    }

    @Test
    @DisplayName("Total count label is hidden when all rows fit on a single page")
    void givenSinglePageOfDeployedProcesses_whenOpenProcessList_thenTotalCountLabelHidden() {
        // given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/vacationApproval.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView()
                .waitUntilDataLoading();

        // then
        listView.getPagination()
                .shouldHave(text("1 row"))
                .shouldHave(totalCountHidden);
    }

    @Test
    @DisplayName("Total count is shown only after clicking the placeholder when multiple pages exist")
    void givenMultiplePagesOfDeployedProcesses_whenClickTotalCountLabel_thenTotalCountUpdated() {
        // given
        camundaRestTestHelper.createDeploymentFromZip(camunda7,
                "test_support/processesForPaginationTest.zip");

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView()
                .waitUntilDataLoading();

        // then
        listView.getPagination()
                .shouldHave(totalCountUnknown);

        listView.getPagination()
                .getTotalCountLabel()
                .getDelegate()
                .click();

        listView.getPagination()
                .shouldHave(totalCount(65));
    }
}
