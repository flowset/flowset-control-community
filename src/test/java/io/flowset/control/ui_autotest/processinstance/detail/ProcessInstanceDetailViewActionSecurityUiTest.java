/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.detail;

import io.flowset.control.security.UiMinimalRole;
import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.flowset.control.test_support.security.role.processinstance.TestProcessInstanceActivatePermissionRole;
import io.flowset.control.test_support.security.role.processinstance.TestProcessInstanceMigratePermissionRole;
import io.flowset.control.test_support.security.role.processinstance.TestProcessInstanceNoDecisionInstanceViewRole;
import io.flowset.control.test_support.security.role.processinstance.TestProcessInstanceNoProcessDefinitionViewRole;
import io.flowset.control.test_support.security.role.processinstance.TestProcessInstanceSuspendPermissionRole;
import io.flowset.control.test_support.security.role.processinstance.TestProcessInstanceTerminatePermissionRole;
import io.flowset.control.test_support.ui.component.BpmnViewerFragment;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processinstance.detail.ProcessInstanceDetailView;
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

import java.util.stream.Stream;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.EXIST;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Secured actions availability on Process instance detail view")
@Tag("security")
public class ProcessInstanceDetailViewActionSecurityUiTest extends AbstractCamunda7UiTest {

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
    @MethodSource("activateVisibilityOnDetailViewSource")
    @DisplayName("Activate action visibility on Process instance detail view")
    void givenExistingSuspendedProcessInstance_whenOpenDetailView_thenActivateActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .suspendByKey("vacation_approval", true);
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        controlTestDataCreator.createUser("test-user-process-instance-activate", "password", roleClass);

        MainView mainView = loginAs("test-user-process-instance-activate", "password");

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        // then
        if (expectedVisible) {
            detailView.getGeneralPanel().getActivateBtn().shouldBe(VISIBLE);
        } else {
            detailView.getGeneralPanel().getActivateBtn().shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("suspendVisibilityOnDetailViewSource")
    @DisplayName("Suspend action visibility on Process instance detail view")
    void givenExistingActiveProcessInstance_whenOpenDetailView_thenSuspendActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        controlTestDataCreator.createUser("test-user-process-instance-suspend", "password", roleClass);

        MainView mainView = loginAs("test-user-process-instance-suspend", "password");

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        // then
        if (expectedVisible) {
            detailView.getGeneralPanel().getSuspendBtn().shouldBe(VISIBLE);
        } else {
            detailView.getGeneralPanel().getSuspendBtn().shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("terminateVisibilityOnDetailViewSource")
    @DisplayName("Terminate action visibility on Process instance detail view")
    void givenExistingActiveProcessInstance_whenOpenDetailView_thenTerminateActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        controlTestDataCreator.createUser("test-user-process-instance-terminate", "password", roleClass);

        MainView mainView = loginAs("test-user-process-instance-terminate", "password");

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        // then
        if (expectedVisible) {
            detailView.getGeneralPanel().getTerminateBtn().shouldBe(VISIBLE);
        } else {
            detailView.getGeneralPanel().getTerminateBtn().shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("migrateVisibilityOnDetailViewSource")
    @DisplayName("Migrate action visibility on Process instance detail view")
    void givenExistingActiveProcessInstance_whenOpenDetailView_thenMigrateActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        controlTestDataCreator.createUser("test-user-process-instance-migrate", "password", roleClass);

        MainView mainView = loginAs("test-user-process-instance-migrate", "password");

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        // then
        if (expectedVisible) {
            detailView.getGeneralPanel().getMigrateBtn().shouldBe(VISIBLE);
        } else {
            detailView.getGeneralPanel().getMigrateBtn().shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("processDefinitionButtonVisibilityOnDetailViewSource")
    @DisplayName("Process definition button visibility on Properties panel")
    void givenExistingProcessInstance_whenOpenDetailView_thenProcessDefinitionButtonVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        controlTestDataCreator.createUser("test-user-process-instance-properties", "password", roleClass);

        MainView mainView = loginAs("test-user-process-instance-properties", "password");

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);
        detailView.getGeneralPanel().getInfoBtn().click();

        // then
        if (expectedVisible) {
            detailView.getGeneralPanel()
                    .getPropertiesPanel()
                    .getOpenProcessDefinitionEditorBtn()
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            detailView.getGeneralPanel()
                    .getPropertiesPanel()
                    .getOpenProcessDefinitionEditorBtn()
                    .shouldNotBe(VISIBLE);
        }
    }

    @Test
    @DisplayName("Root and parent process buttons are available for child process instance")
    void givenExistingChildProcessInstance_whenOpenDetailView_thenParentProcessButtonsAvailable() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/terminateinstance/testSkipSubprocess.bpmn")
                .deploy("test_support/terminateinstance/testSkipSubprocessMain.bpmn")
                .startByKey("testSkipSubprocessMain");
        String childInstanceId = camundaRestTestHelper.getActiveRuntimeInstancesByKey(camunda7, "testSkipSubprocess")
                .get(0);

        controlTestDataCreator.createUser("test-user-process-instance-parent-buttons", "password",
                TestUiPermissionFullAccessReadRole.class);

        MainView mainView = loginAs("test-user-process-instance-parent-buttons", "password");

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(childInstanceId);
        detailView.getGeneralPanel().getInfoBtn().click();

        // then
        detailView.getGeneralPanel()
                .getPropertiesPanel()
                .getOpenRootProcessInstanceEditorBtn()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        detailView.getGeneralPanel()
                .getPropertiesPanel()
                .getOpenSuperProcessInstanceEditorBtn()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Direct URL navigation is denied without Process instance detail view policy")
    void givenUserWithoutProcessInstanceDetailViewPolicy_whenOpenDetailViewByUrl_thenNavigationDenied() {
        // given
        String instanceId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .getStartedInstances("vacation_approval")
                .get(0);
        controlTestDataCreator.createUser(
                "test-user-process-instance-no-detail-view-policy",
                "password",
                UiMinimalRole.class
        );
        loginAs("test-user-process-instance-no-detail-view-policy", "password");

        // when
        open("/bpm/process-instances/" + instanceId);

        // then
        $j(ProcessInstanceDetailView.class).shouldNotBe(EXIST);
        $("body").shouldHave(text("Could not navigate to 'bpm/process-instances/" + instanceId + "'"));
    }

    @Test
    @DisplayName("Diagram toolbar buttons are available on Process instance detail view")
    void givenExistingProcessInstance_whenOpenDetailView_thenDiagramToolbarButtonsAvailable() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        controlTestDataCreator.createUser("test-user-process-instance-diagram-toolbar", "password",
                TestUiPermissionFullAccessReadRole.class);

        MainView mainView = loginAs("test-user-process-instance-diagram-toolbar", "password");

        // when
        BpmnViewerFragment viewerFragment = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .getBpmnViewerFragment();

        // then
        viewerFragment.getZoomInButton().shouldBe(VISIBLE).shouldBe(ENABLED);
        viewerFragment.getZoomOutButton().shouldBe(VISIBLE).shouldBe(ENABLED);
        viewerFragment.getResetZoomButton().shouldBe(VISIBLE).shouldBe(ENABLED);
        viewerFragment.getViewActivityStatisticsButton().shouldNotBe(EXIST).shouldNotBe(VISIBLE);
        viewerFragment.getViewDocumentationButton().shouldNotBe(EXIST).shouldNotBe(VISIBLE);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("decisionInstanceOverlayVisibilityOnDetailViewSource")
    @DisplayName("Decision instance overlay visibility on diagram")
    void givenProcessInstanceWithDecisionInstance_whenOpenDetailView_thenDecisionInstanceOverlayVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision");
        String instanceId = dataManager.getStartedInstances("testProcessWithDecision").get(0);

        controlTestDataCreator.createUser("test-user-process-instance-decision-overlay", "password", roleClass);

        MainView mainView = loginAs("test-user-process-instance-decision-overlay", "password");

        // when
        BpmnViewerFragment viewerFragment = mainView.openProcessInstanceListView()
                .switchToAllViewMode()
                .openDetailViewByInstanceId(instanceId)
                .getBpmnViewerFragment();

        // then
        if (expectedVisible) {
            viewerFragment.getDecisionInstanceOverlay("evaluateDecisionTask")
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            viewerFragment.getDecisionInstanceOverlay("evaluateDecisionTask")
                    .shouldNotBe(VISIBLE);
        }
    }

    private static Stream<Arguments> activateVisibilityOnDetailViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without activate permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with activate permission", TestProcessInstanceActivatePermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> suspendVisibilityOnDetailViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without suspend permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with suspend permission", TestProcessInstanceSuspendPermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> terminateVisibilityOnDetailViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without terminate permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with terminate permission", TestProcessInstanceTerminatePermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> migrateVisibilityOnDetailViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without migrate permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with migrate permission", TestProcessInstanceMigratePermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> processDefinitionButtonVisibilityOnDetailViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without ProcessDefinitionData view permission",
                                TestProcessInstanceNoProcessDefinitionViewRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with ProcessDefinitionData view permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> decisionInstanceOverlayVisibilityOnDetailViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without HistoricDecisionInstanceShortData view permission",
                                TestProcessInstanceNoDecisionInstanceViewRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with HistoricDecisionInstanceShortData view permission",
                                TestUiPermissionFullAccessReadRole.class),
                        Named.of("visible", true)
                )
        );
    }
}
