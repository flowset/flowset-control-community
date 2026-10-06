/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.detail.tab.runtime.variable;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.request.StartProcessDto;
import io.flowset.control.test_support.camunda7.dto.request.VariableValueDto;
import io.flowset.control.test_support.camunda7.dto.response.ExecutionDto;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeVariablesTabFragment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static com.codeborne.selenide.Condition.text;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeVariablesTabFragment.SCOPE_COLUMN_INDEX;

@WithRunningExternalEngine
@DisplayName("Data grid in the Variables tab (Runtime tab)")
public class RuntimeVariablesTabGridUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Scope column shows the process instance for a global variable")
    void givenGlobalVariable_whenRuntimeVariablesTabOpened_thenScopeColumnShowsProcessInstanceName() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess", StartProcessDto.builder()
                        .variable("globalVariable", new VariableValueDto("String", "globalValue"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();

        // then
        variablesTab.getRowByVariableName("globalVariable")
                .getCellByIndex(SCOPE_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("Process instance (Test local variable subprocess)"));
    }

    @Test
    @DisplayName("Scope column shows the activity name for a local variable")
    void givenLocalVariableInSubProcess_whenRuntimeVariablesTabOpened_thenScopeColumnShowsActivityName() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess");
        String instanceId = dataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();

        camundaRestTestHelper.putLocalExecutionVariable(camunda7, findSubProcessExecutionId(instanceId),
                "localVariable", new VariableValueDto("String", "localValue"));

        MainView mainView = loginAsAdmin();

        // when
        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();

        // then
        variablesTab.getRowByVariableName("localVariable")
                .getCellByIndex(SCOPE_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("Sub process"));
    }

    @Test
    @DisplayName("Scope column shows the activity name of a transition instance for an execution variable")
    void givenVariableOfTransitionInstance_whenRuntimeVariablesTabOpened_thenScopeColumnShowsExecutionWithActivityName() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testJobsListLoad.bpmn")
                .startByKey("testJobsListLoad")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testJobsListLoad").getFirst();
        String failedJobExecutionId = camundaRestTestHelper.getFailedJobs(camunda7, List.of(instanceId))
                .getFirst()
                .getExecutionId();
        camundaRestTestHelper.putLocalExecutionVariable(camunda7, failedJobExecutionId, "executionVariable",
                new VariableValueDto("String", "executionValue"));

        MainView mainView = loginAsAdmin();

        // when
        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();

        // then
        variablesTab.getRowByVariableName("executionVariable")
                .getCellByIndex(SCOPE_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("Execution (Failed script task)"));
    }

    @Test
    @DisplayName("Scope column shows the process key for a global variable of a process without a name")
    void givenGlobalVariableOfProcessWithoutName_whenRuntimeVariablesTabOpened_thenScopeColumnShowsProcessKey() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testScopeColumnUnnamedElements.bpmn")
                .startByKey("testScopeColumnUnnamedElements", StartProcessDto.builder()
                        .variable("globalVariable", new VariableValueDto("String", "globalValue"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testScopeColumnUnnamedElements").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();

        // then
        variablesTab.getRowByVariableName("globalVariable")
                .getCellByIndex(SCOPE_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("Process instance (testScopeColumnUnnamedElements)"));
    }

    @Test
    @DisplayName("Scope column shows the activity type and id for a local variable of an activity without a name")
    void givenLocalVariableOfUnnamedSubProcess_whenRuntimeVariablesTabOpened_thenScopeColumnShowsActivityTypeAndId() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testScopeColumnUnnamedElements.bpmn")
                .startByKey("testScopeColumnUnnamedElements");
        String instanceId = dataManager.getStartedInstances("testScopeColumnUnnamedElements").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();

        // then
        variablesTab.getRowByVariableName("subProcessLocalVariable")
                .getCellByIndex(SCOPE_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("subProcess (unnamedSubProcess)"));
    }

    @Test
    @DisplayName("Scope column shows the activity type and id of an unnamed transition instance for an execution variable")
    void givenVariableOfUnnamedTransitionInstance_whenRuntimeVariablesTabOpened_thenScopeColumnShowsExecutionWithActivityTypeAndId() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testScopeColumnUnnamedElements.bpmn")
                .startByKey("testScopeColumnUnnamedElements")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testScopeColumnUnnamedElements").getFirst();
        String failedJobExecutionId = camundaRestTestHelper.getFailedJobs(camunda7, List.of(instanceId))
                .getFirst()
                .getExecutionId();
        camundaRestTestHelper.putLocalExecutionVariable(camunda7, failedJobExecutionId, "executionVariable",
                new VariableValueDto("String", "executionValue"));

        MainView mainView = loginAsAdmin();

        // when
        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();

        // then
        variablesTab.getRowByVariableName("executionVariable")
                .getCellByIndex(SCOPE_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("Execution (scriptTask: unnamedFailedTask)"));
    }

    String findSubProcessExecutionId(String processInstanceId) {
        return camundaRestTestHelper.findExecutions(camunda7, processInstanceId)
                .stream()
                .map(ExecutionDto::getId)
                .filter(executionId -> !executionId.equals(processInstanceId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No child execution found for " + processInstanceId));
    }
}
