/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.batch.detail;

import io.flowset.control.security.UiMinimalRole;
import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.request.StartProcessDto;
import io.flowset.control.test_support.camunda7.dto.request.VariableValueDto;
import io.flowset.control.test_support.camunda7.dto.response.BatchDto;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.security.role.batch.TestBatchDetailAccessRole;
import io.flowset.control.test_support.security.role.batch.TestBatchDetailNoJobDetailViewRole;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.batch.AllBatchListView;
import io.flowset.control.test_support.ui.view.batch.BatchDataDetailView;
import io.flowset.control.test_support.ui.view.batch.BatchStatisticsDataDetailDialog;
import io.flowset.control.test_support.ui.view.batch.BatchStatisticsDataDetailView;
import io.jmix.masquerade.component.DataGrid;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.stream.Stream;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static io.flowset.control.test_support.ui.view.batch.AllBatchListView.ACTIVE_ID_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.batch.AllBatchListView.ID_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.batch.BatchStatisticsDataDetailDialog.JOB_ID_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.batch.BatchStatisticsDataDetailDialog.JOB_ID_COLUMN_INDEX;
import static io.jmix.masquerade.JConditions.EXIST;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Secured actions availability on batch detail view")
@Tag("security")
public class BatchDetailViewActionSecurityUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ControlTestDataCreator controlTestDataCreator;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @AfterEach
    void tearDownUsers() {
        jdbcTemplate.update("delete from SEC_ROLE_ASSIGNMENT where USERNAME like 'test-user%'");
        jdbcTemplate.update(
                "delete from SEC_RESOURCE_POLICY where ROLE_ID in " +
                        "(select ID from SEC_RESOURCE_ROLE where CODE like 'test-%')"
        );
        jdbcTemplate.update("delete from SEC_RESOURCE_ROLE where CODE like 'test-%'");
        jdbcTemplate.update("delete from USER_ where USERNAME like 'test-user%'");
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("jobLinkAvailabilitySource")
    @DisplayName("Job link availability with Entity read access")
    void givenExistingActiveBatch_whenOpenBatchStatisticsDetail_thenJobLinkAvailabilityMatchesPermissions(
            Class<?> roleClass, boolean expectedAvailable
    ) {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedBatch.bpmn")
                .startByKey("testFailedBatch", StartProcessDto.builder()
                                .variable("fail", new VariableValueDto("String", "testValue"))
                                .build(),
                        2);
        List<String> processInstanceIds = dataManager.getStartedInstances("testFailedBatch");
        BatchDto batch = camundaRestTestHelper.deleteInstancesAsync(camunda7, processInstanceIds);

        controlTestDataCreator.createUser("test-user-batch-detail-job", "password", roleClass);

        MainView mainView = loginAs("test-user-batch-detail-job", "password");

        // when
        AllBatchListView listView = mainView.openAllBatchListView();
        listView.getActiveRowByBatchId(batch.getId())
                .getCellByIndex(ACTIVE_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY)
                .click();

        BatchStatisticsDataDetailDialog dialog = $j(BatchStatisticsDataDetailDialog.class)
                .exists()
                .displayed();

        // then
        DataGrid.Row jobRow = dialog.getJobsDataGrid()
                .getRowByIndex(0);
        if (expectedAvailable) {
            jobRow.getCellByIndex(JOB_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(JOB_ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            jobRow.getCellByIndex(JOB_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(JOB_ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    @Test
    @DisplayName("Direct URL navigation is denied without Batch detail view policy")
    void givenUserWithoutBatchDetailViewPolicy_whenOpenDetailViewByUrl_thenNavigationDenied() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedBatch.bpmn")
                .startByKey("testFailedBatch", StartProcessDto.builder()
                                .variable("fail", new VariableValueDto("String", "testValue"))
                                .build(),
                        2);
        List<String> processInstanceIds = dataManager.getStartedInstances("testFailedBatch");
        BatchDto batch = camundaRestTestHelper.deleteInstancesAsync(camunda7, processInstanceIds);

        controlTestDataCreator.createUser(
                "test-user-batch-no-detail-view-policy",
                "password",
                UiMinimalRole.class
        );
        loginAs("test-user-batch-no-detail-view-policy", "password");

        // when
        open("/bpm/batches/" + batch.getId());

        // then
        $j(BatchDataDetailView.class).shouldNotBe(EXIST);
        $("body").shouldHave(text("Could not navigate to 'bpm/batches/" + batch.getId() + "'"));
    }

    @Test
    @DisplayName("Direct URL navigation is denied without Batch statistics detail view policy")
    void givenUserWithoutBatchStatisticsDetailViewPolicy_whenOpenDetailViewByUrl_thenNavigationDenied() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedBatch.bpmn")
                .startByKey("testFailedBatch", StartProcessDto.builder()
                                .variable("fail", new VariableValueDto("String", "testValue"))
                                .build(),
                        2);
        List<String> processInstanceIds = dataManager.getStartedInstances("testFailedBatch");
        BatchDto batch = camundaRestTestHelper.deleteInstancesAsync(camunda7, processInstanceIds);

        controlTestDataCreator.createUser(
                "test-user-batch-statistics-no-detail-view-policy",
                "password",
                UiMinimalRole.class
        );
        loginAs("test-user-batch-statistics-no-detail-view-policy", "password");

        // when
        open("/bpm/batches/" + batch.getId() + "/statistics");

        // then
        $j(BatchStatisticsDataDetailView.class).shouldNotBe(EXIST);
        $("body").shouldHave(text("Could not navigate to 'bpm/batches/" + batch.getId() + "/statistics'"));
    }

    private static Stream<Arguments> jobLinkAvailabilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without job detail view access", TestBatchDetailNoJobDetailViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("with job detail view access", TestBatchDetailAccessRole.class),
                        Named.of("enabled", true)
                )
        );
    }
}
