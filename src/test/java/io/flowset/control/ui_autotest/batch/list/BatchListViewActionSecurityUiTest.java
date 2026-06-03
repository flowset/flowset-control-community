/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.batch.list;

import io.flowset.control.security.UiMinimalRole;
import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.response.BatchDto;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.security.role.batch.TestBatchListAccessRole;
import io.flowset.control.test_support.security.role.batch.TestBatchListNoDetailViewRole;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.batch.AllBatchListView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static io.flowset.control.test_support.ui.view.batch.AllBatchListView.ACTIVE_ID_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.batch.AllBatchListView.COMPLETED_ID_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.batch.AllBatchListView.ID_BUTTON_BY;
import static io.jmix.masquerade.JConditions.EXIST;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Secured actions availability on batch list view")
@Tag("security")
public class BatchListViewActionSecurityUiTest extends AbstractCamunda7UiTest {

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
    @MethodSource("activeBatchDetailLinkAvailabilitySource")
    @DisplayName("Batch statistics link availability on Active tab")
    void givenExistingActiveBatch_whenOpenAllBatchListView_thenBatchStatisticsLinkAvailabilityMatchesPermissions(
            Class<?> roleClass, boolean expectedAvailable
    ) {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedBatch.bpmn")
                .startByKey("testFailedBatch", 2);
        List<String> processInstanceIds = dataManager.getStartedInstances("testFailedBatch");
        BatchDto batch = camundaRestTestHelper.deleteInstancesAsync(camunda7, processInstanceIds);

        controlTestDataCreator.createUser("test-user-batch-list-active", "password", roleClass);

        MainView mainView = loginAs("test-user-batch-list-active", "password");

        // when
        AllBatchListView listView = mainView.openAllBatchListView()
                .selectActiveTab();

        // then
        if (expectedAvailable) {
            listView.getActiveRowByBatchId(batch.getId())
                    .getCellByIndex(ACTIVE_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            listView.getActiveRowByBatchId(batch.getId())
                    .getCellByIndex(ACTIVE_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("completedBatchDetailLinkAvailabilitySource")
    @DisplayName("Batch data link availability on Completed tab")
    void givenExistingCompletedBatch_whenOpenAllBatchListView_thenBatchDataLinkAvailabilityMatchesPermissions(
            Class<?> roleClass, boolean expectedAvailable
    ) {
        // given
        List<String> activeInstanceIds = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .getStartedInstances("visitPlanning");
        BatchDto batch = camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);
        camundaRestTestHelper.waitForBatchExecution(camunda7);

        controlTestDataCreator.createUser("test-user-batch-list-completed", "password", roleClass);

        MainView mainView = loginAs("test-user-batch-list-completed", "password");

        // when
        AllBatchListView listView = mainView.openAllBatchListView()
                .selectCompletedTab();

        // then
        if (expectedAvailable) {
            listView.getCompletedRowByBatchId(batch.getId())
                    .getCellByIndex(COMPLETED_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            listView.getCompletedRowByBatchId(batch.getId())
                    .getCellByIndex(COMPLETED_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    @Test
    @DisplayName("Direct URL navigation is denied without Batch list view policy")
    void givenUserWithoutBatchListViewPolicy_whenOpenListViewByUrl_thenNavigationDenied() {
        // given
        controlTestDataCreator.createUser(
                "test-user-batch-no-list-view-policy",
                "password",
                UiMinimalRole.class
        );
        loginAs("test-user-batch-no-list-view-policy", "password");

        // when
        open("/bpm/batches");

        // then
        $j(AllBatchListView.class).shouldNotBe(EXIST);
        $("body").shouldHave(text("Could not navigate to 'bpm/batches'"));
    }

    private static Stream<Arguments> activeBatchDetailLinkAvailabilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without batch statistics detail view access", TestBatchListNoDetailViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("with batch statistics detail view access", TestBatchListAccessRole.class),
                        Named.of("enabled", true)
                )
        );
    }

    private static Stream<Arguments> completedBatchDetailLinkAvailabilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without batch data detail view access", TestBatchListNoDetailViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("with batch data detail view access", TestBatchListAccessRole.class),
                        Named.of("enabled", true)
                )
        );
    }
}
