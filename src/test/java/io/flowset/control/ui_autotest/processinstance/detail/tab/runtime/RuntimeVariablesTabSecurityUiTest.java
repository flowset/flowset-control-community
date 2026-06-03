/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.detail.tab.runtime;

import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.request.StartProcessDto;
import io.flowset.control.test_support.camunda7.dto.request.VariableValueDto;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.flowset.control.test_support.security.role.processinstance.TestProcessInstanceDetailNoLinkedDetailViewRole;
import io.flowset.control.test_support.security.role.variable.TestVariableCreatePermissionRole;
import io.flowset.control.test_support.security.role.variable.TestVariableRemovePermissionRole;
import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeVariablesTabFragment;
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

import static com.codeborne.selenide.Condition.text;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeVariablesTabFragment.NAME_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeVariablesTabFragment.NAME_COLUMN_INDEX;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;

@WithRunningExternalEngine
@DisplayName("Secured actions on Variables tab in Process instance detail view")
@Tag("security")
public class RuntimeVariablesTabSecurityUiTest extends AbstractCamunda7UiTest {

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
    @MethodSource("createVisibilitySource")
    @DisplayName("Create action visibility")
    void givenExistingProcessInstance_whenOpenRuntimeVariablesTab_thenCreateActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", StartProcessDto.builder()
                        .variable("firstVariable", new VariableValueDto("String", "Some value"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").get(0);

        controlTestDataCreator.createUser("test-user-variable-create", "password", roleClass);
        MainView mainView = loginAs("test-user-variable-create", "password");

        // when
        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();

        // then
        GridContextMenu contextMenu = variablesTab.openVariablesGridContextMenu();
        if (expectedVisible) {
            variablesTab.getCreateButton().shouldBe(VISIBLE);
            contextMenu.find(text("Create")).shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            variablesTab.getCreateButton().shouldBe(VISIBLE).shouldNotBe(ENABLED);
            contextMenu.find(text("Create")).shouldBe(VISIBLE).shouldNotBe(ENABLED);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("removeVisibilitySource")
    @DisplayName("Remove action visibility")
    void givenExistingProcessInstance_whenOpenRuntimeVariablesTab_thenRemoveActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", StartProcessDto.builder()
                        .variable("firstVariable", new VariableValueDto("String", "Some value"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").get(0);

        controlTestDataCreator.createUser("test-user-variable-remove", "password", roleClass);
        MainView mainView = loginAs("test-user-variable-remove", "password");

        // when
        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();

        // then
        variablesTab.getRuntimeVariablesGrid().clickSelectAll();

        GridContextMenu contextMenu = variablesTab.openVariablesGridContextMenu();
        if (expectedVisible) {
            variablesTab.getRemoveButton().shouldBe(VISIBLE);
            contextMenu.find(text("Remove")).shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            variablesTab.getRemoveButton().shouldBe(VISIBLE).shouldNotBe(ENABLED);
            contextMenu.find(text("Remove")).shouldBe(VISIBLE).shouldNotBe(ENABLED);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("detailLinkAvailabilitySource")
    @DisplayName("Variable name link availability")
    void givenExistingProcessVariable_whenOpenRuntimeVariablesTab_thenVariableNameLinkAvailabilityMatchesPermissions(
            Class<?> roleClass, boolean expectedEnabled
    ) {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", StartProcessDto.builder()
                        .variable("firstVariable", new VariableValueDto("String", "Some value"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").get(0);

        controlTestDataCreator.createUser("test-user-variable-detail-link", "password", roleClass);
        MainView mainView = loginAs("test-user-variable-detail-link", "password");

        // when
        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();

        // then
        if (expectedEnabled) {
            variablesTab.getRowByVariableName("firstVariable")
                    .getCellByIndex(NAME_COLUMN_INDEX)
                    .getCellContent()
                    .find(NAME_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            variablesTab.getRowByVariableName("firstVariable")
                    .getCellByIndex(NAME_COLUMN_INDEX)
                    .getCellContent()
                    .find(NAME_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    private static Stream<Arguments> createVisibilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without create permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with create permission", TestVariableCreatePermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> removeVisibilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without remove permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with remove permission", TestVariableRemovePermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> detailLinkAvailabilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without variable detail dialog view access",
                                TestProcessInstanceDetailNoLinkedDetailViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("with variable detail dialog view access", TestUiPermissionFullAccessReadRole.class),
                        Named.of("enabled", true)
                )
        );
    }
}
