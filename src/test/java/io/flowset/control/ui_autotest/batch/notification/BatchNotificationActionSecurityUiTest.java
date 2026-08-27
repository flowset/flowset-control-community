/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.batch.notification;

import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.security.role.batch.TestBatchNotificationNoBatchViewRole;
import io.flowset.control.test_support.security.role.batch.TestBatchNotificationViewRole;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.batch.notification.BatchNotificationContentFragment;
import io.flowset.control.test_support.ui.view.processinstance.ProcessInstanceListView;
import io.flowset.control.test_support.ui.view.processinstance.action.BulkTerminateProcessInstanceDialog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.stream.Stream;

import static io.flowset.control.test_support.ui.TagNames.NOTIFICATION_CARD;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Secured actions availability on batch notification")
@Tag("security")
public class BatchNotificationActionSecurityUiTest extends AbstractCamunda7UiTest {

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
                "delete from SEC_RESOURCE_POLICY where ROLE_ID in " +
                        "(select ID from SEC_RESOURCE_ROLE where CODE like 'test-%')"
        );
        jdbcTemplate.update("delete from SEC_RESOURCE_ROLE where CODE like 'test-%'");
        jdbcTemplate.update("delete from USER_ where USERNAME like 'test-user%'");
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("openBatchButtonVisibilitySource")
    @DisplayName("Batch detail button visibility")
    void givenSingleBatchNotification_whenShown_thenOpenBatchButtonVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        controlTestDataCreator.createUser("test-user-batch-notification", "password", roleClass);

        MainView mainView = loginAs("test-user-batch-notification", "password");

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();
        listView.getProcessInstancesGrid().clickSelectAll();
        listView.getBulkTerminateBtn().click();

        $j(BulkTerminateProcessInstanceDialog.class)
                .exists()
                .displayed()
                .getOkBtn()
                .click();

        BatchNotificationContentFragment notification = $j(BatchNotificationContentFragment.class, NOTIFICATION_CARD)
                .exists()
                .displayed();

        // then
        if (expectedVisible) {
            notification.getBatchDescription().shouldBe(VISIBLE);
            notification.getOpenBatchBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            notification.getBatchDescription().shouldNotBe(VISIBLE);
            notification.getOpenBatchBtn().shouldNotBe(VISIBLE);
        }
    }

    private static Stream<Arguments> openBatchButtonVisibilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without BatchData view permission", TestBatchNotificationNoBatchViewRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with BatchData view permission", TestBatchNotificationViewRole.class),
                        Named.of("visible", true)
                )
        );
    }
}
