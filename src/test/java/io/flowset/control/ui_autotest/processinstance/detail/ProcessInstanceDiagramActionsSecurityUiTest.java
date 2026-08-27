/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.detail;

import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.flowset.control.test_support.ui.component.BpmnViewerFragment;
import io.flowset.control.test_support.ui.view.MainView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;

@WithRunningExternalEngine
@DisplayName("Secured actions availability on BPMN diagram in Process instance detail view")
@Tag("security")
public class ProcessInstanceDiagramActionsSecurityUiTest extends AbstractCamunda7UiTest {

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

    @Test
    @DisplayName("Called process instance overlay is available on diagram")
    void givenProcessInstanceWithCalledInstance_whenOpenDetailView_thenCalledProcessInstanceOverlayAvailable() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/terminateinstance/testSkipSubprocess.bpmn")
                .deploy("test_support/terminateinstance/testSkipSubprocessMain.bpmn")
                .startByKey("testSkipSubprocessMain");
        String parentInstanceId = dataManager.getStartedInstances("testSkipSubprocessMain").get(0);

        controlTestDataCreator.createUser("test-user-process-instance-called-overlay", "password",
                TestUiPermissionFullAccessReadRole.class);

        MainView mainView = loginAs("test-user-process-instance-called-overlay", "password");

        // when
        BpmnViewerFragment viewerFragment = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(parentInstanceId)
                .getBpmnViewerFragment();

        // then
        viewerFragment.getCalledProcessInstanceOverlay("subprocessTask")
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }
}
