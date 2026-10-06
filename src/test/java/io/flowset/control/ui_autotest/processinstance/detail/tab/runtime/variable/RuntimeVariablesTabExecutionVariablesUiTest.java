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
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processinstance.detail.ProcessInstanceDetailView;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeTabFragment;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeVariablesTabFragment;
import io.flowset.control.test_support.ui.view.variable.VariableInstanceDataDetailDialog;
import io.jmix.masquerade.component.TextField;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

import static io.flowset.control.test_support.ui.condition.ControlCondition.bodyRowWithCellTexts;
import static io.flowset.control.test_support.ui.condition.ControlCondition.emptyGrid;
import static io.flowset.control.test_support.ui.condition.ControlCondition.visibleBodyRowCount;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeVariablesTabFragment.NAME_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeVariablesTabFragment.SCOPE_COLUMN_INDEX;
import static io.jmix.masquerade.JConditions.CHECKED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Execution variables checkbox in the Variables tab (Runtime tab)")
public class RuntimeVariablesTabExecutionVariablesUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Execution variables checkbox is hidden when no activity instance is selected")
    void givenNoActivityInstanceSelected_whenOpenRuntimeVariablesTab_thenExecutionVariablesCheckboxHidden() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testJobsListLoad.bpmn")
                .startByKey("testJobsListLoad")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testJobsListLoad").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        RuntimeVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeVariablesTab();

        // then
        variablesTab.getExecutionVariablesCheckbox().shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Execution variables checkbox is hidden when a non-transition activity instance is selected")
    void givenActiveTimerEvent_whenTimerEventSelected_thenExecutionVariablesCheckboxHidden() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testJobsListLoad.bpmn")
                .startByKey("testJobsListLoad")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testJobsListLoad").getFirst();

        MainView mainView = loginAsAdmin();

        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        // when
        detailView.openRuntimeTab()
                .selectRowByActivityId("timerEvent");

        // then
        detailView.openRuntimeVariablesTab()
                .getExecutionVariablesCheckbox()
                .shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Execution variables checkbox is hidden for a transition instance in the process instance execution")
    void givenTransitionInstanceInProcessInstanceExecution_whenTransitionInstanceSelected_thenExecutionVariablesCheckboxHidden() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").getFirst();

        MainView mainView = loginAsAdmin();

        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        // when
        detailView.openRuntimeTab()
                .selectRowByActivityId("throwsExceptionTask");

        // then
        detailView.openRuntimeVariablesTab()
                .getExecutionVariablesCheckbox()
                .shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Execution variables checkbox is visible for a transition instance in a parallel branch")
    void givenTransitionInstanceInParallelBranch_whenTransitionInstanceSelected_thenExecutionVariablesCheckboxVisible() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testJobsListLoad.bpmn")
                .startByKey("testJobsListLoad")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testJobsListLoad").getFirst();
        String failedJobExecutionId = camundaRestTestHelper.getFailedJobs(camunda7, List.of(instanceId))
                .getFirst()
                .getExecutionId();

        camundaRestTestHelper.putLocalExecutionVariable(camunda7, failedJobExecutionId, "cardToken",
                new VariableValueDto("String", "tok_new"));

        MainView mainView = loginAsAdmin();

        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        // when
        detailView.openRuntimeTab()
                .selectRowByActivityId("failedScriptTask");

        // then
        RuntimeVariablesTabFragment variablesTab = detailView.openRuntimeVariablesTab();
        variablesTab.getExecutionVariablesCheckbox()
                .shouldBe(VISIBLE)
                .shouldNotBe(CHECKED);
        variablesTab.getRuntimeVariablesGrid().shouldHave(emptyGrid);
    }

    @Test
    @DisplayName("Execution-local variables are shown when the Execution variables checkbox is checked")
    void givenTransitionInstanceInParallelBranch_whenExecutionVariablesChecked_thenExecutionLocalVariablesShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testJobsListLoad.bpmn")
                .startByKey("testJobsListLoad", StartProcessDto.builder()
                        .variable("orderId", new VariableValueDto("String", "order-42"))
                        .build())
                .waitJobsExecution();

        String instanceId = dataManager.getStartedInstances("testJobsListLoad").getFirst();
        String failedJobExecutionId = camundaRestTestHelper.getFailedJobs(camunda7, List.of(instanceId))
                .getFirst()
                .getExecutionId();
        camundaRestTestHelper.putLocalExecutionVariable(camunda7, failedJobExecutionId, "cardToken",
                new VariableValueDto("String", "tok_new"));

        MainView mainView = loginAsAdmin();

        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);
        detailView.openRuntimeTab()
                .selectRowByActivityId("failedScriptTask");
        RuntimeVariablesTabFragment variablesTab = detailView.openRuntimeVariablesTab();

        // when
        variablesTab.getExecutionVariablesCheckbox().setChecked(true);

        // then
        variablesTab.getRuntimeVariablesGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(bodyRowWithCellTexts(Map.of(
                        NAME_COLUMN_INDEX, "cardToken",
                        SCOPE_COLUMN_INDEX, "Execution (Failed script task)")));
    }

    @Test
    @DisplayName("Execution variables checkbox is hidden after a non-transition activity instance is selected")
    void givenExecutionVariablesChecked_whenTimerEventSelected_thenExecutionVariablesCheckboxHidden() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testJobsListLoad.bpmn")
                .startByKey("testJobsListLoad")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testJobsListLoad").getFirst();

        MainView mainView = loginAsAdmin();

        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);
        RuntimeTabFragment runtimeTab = detailView.openRuntimeTab();
        runtimeTab.selectRowByActivityId("failedScriptTask");

        RuntimeVariablesTabFragment variablesTab = detailView.openRuntimeVariablesTab();
        variablesTab.getExecutionVariablesCheckbox().setChecked(true);

        // when
        runtimeTab.selectRowByActivityId("timerEvent");

        // then
        variablesTab.getExecutionVariablesCheckbox().shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Execution variables checkbox is reset when the transition instance is selected again")
    void givenExecutionVariablesChecked_whenTransitionInstanceSelectedAgain_thenExecutionVariablesCheckboxUnchecked() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testJobsListLoad.bpmn")
                .startByKey("testJobsListLoad")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testJobsListLoad").getFirst();
        String failedJobExecutionId = camundaRestTestHelper.getFailedJobs(camunda7, List.of(instanceId))
                .getFirst()
                .getExecutionId();
        camundaRestTestHelper.putLocalExecutionVariable(camunda7, failedJobExecutionId, "cardToken",
                new VariableValueDto("String", "tok_new"));

        MainView mainView = loginAsAdmin();

        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);
        RuntimeTabFragment runtimeTab = detailView.openRuntimeTab();
        runtimeTab.selectRowByActivityId("failedScriptTask");

        RuntimeVariablesTabFragment variablesTab = detailView.openRuntimeVariablesTab();
        variablesTab.getExecutionVariablesCheckbox().setChecked(true);

        runtimeTab.selectRowByActivityId("timerEvent");

        // when
        runtimeTab.selectRowByActivityId("failedScriptTask");

        // then
        variablesTab.getExecutionVariablesCheckbox()
                .shouldBe(VISIBLE)
                .shouldNotBe(CHECKED);
        variablesTab.getRuntimeVariablesGrid().shouldHave(emptyGrid);
    }

    @Test
    @DisplayName("Execution variables checkbox is checked after a local variable is created for a transition instance")
    void givenTransitionInstanceInParallelBranch_whenLocalVariableCreated_thenExecutionVariablesCheckedAndVariableShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testJobsListLoad.bpmn")
                .startByKey("testJobsListLoad")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testJobsListLoad").getFirst();

        MainView mainView = loginAsAdmin();

        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);
        detailView.openRuntimeTab()
                .selectRowByActivityId("failedScriptTask");

        RuntimeVariablesTabFragment variablesTab = detailView.openRuntimeVariablesTab();
        variablesTab.getCreateButton().click();

        // when
        VariableInstanceDataDetailDialog dialog = $j(VariableInstanceDataDetailDialog.class).exists().displayed();
        dialog.getNameField().setValue("myLocalVariable");
        dialog.getLocalField().setChecked(true);
        dialog.getTypeComboBox()
                .shouldBe(VISIBLE)
                .setValue("String");
        dialog.getValueComponentAs(TextField.class)
                .shouldBe(VISIBLE)
                .setValue("localValue");
        dialog.getSaveBtn().click();

        dialog.shouldNotBe(VISIBLE);

        // then
        variablesTab.getExecutionVariablesCheckbox()
                .shouldBe(VISIBLE)
                .shouldBe(CHECKED);
        variablesTab.getRuntimeVariablesGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(bodyRowWithCellTexts(Map.of(
                        NAME_COLUMN_INDEX, "myLocalVariable",
                        SCOPE_COLUMN_INDEX, "Execution (Failed script task)")));
    }

    @Test
    @DisplayName("Execution variables checkbox is not checked after a non-local variable is created for a transition instance")
    void givenTransitionInstanceInParallelBranch_whenNonLocalVariableCreated_thenExecutionVariablesNotChecked() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testJobsListLoad.bpmn")
                .startByKey("testJobsListLoad")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testJobsListLoad").getFirst();

        MainView mainView = loginAsAdmin();

        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);
        detailView.openRuntimeTab()
                .selectRowByActivityId("failedScriptTask");
        RuntimeVariablesTabFragment variablesTab = detailView.openRuntimeVariablesTab();
        variablesTab.getCreateButton().click();

        // when
        VariableInstanceDataDetailDialog dialog = $j(VariableInstanceDataDetailDialog.class).exists().displayed();
        dialog.getNameField().setValue("myGlobalVariable");
        dialog.getTypeComboBox()
                .shouldBe(VISIBLE)
                .setValue("String");
        dialog.getValueComponentAs(TextField.class)
                .shouldBe(VISIBLE)
                .setValue("globalValue");
        dialog.getSaveBtn().click();

        dialog.shouldNotBe(VISIBLE);

        // then
        variablesTab.getExecutionVariablesCheckbox()
                .shouldBe(VISIBLE)
                .shouldNotBe(CHECKED);
        variablesTab.getRuntimeVariablesGrid().shouldHave(emptyGrid);
    }
}
