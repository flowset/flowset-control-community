/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.detail;

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
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryActivitiesTabFragment;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryDecisionsTabFragment;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryIncidentsTabFragment;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryTabFragment;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryUserTasksTabFragment;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryVariablesTabFragment;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeTabFragment;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeVariablesTabFragment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Map;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.CollectionCondition.texts;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.refresh;
import static io.flowset.control.test_support.ui.condition.ControlCondition.bodyRowWithCellTexts;
import static io.flowset.control.test_support.ui.condition.ControlCondition.visibleBodyRowCount;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryActivitiesTabFragment.ACTIVITY_NAME_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryDecisionsTabFragment.DECISION_DEFINITION_ID_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryIncidentsTabFragment.MESSAGE_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryUserTasksTabFragment.NAME_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryVariablesTabFragment.VALUE_COLUMN_INDEX;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Tabs of Process instance detail view")
public class ProcessInstanceDetailViewTabsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Runtime tab shows nested tabs in order: Variables, User tasks, Jobs, External tasks, Incidents")
    void givenActiveProcessInstance_whenOpenRuntimeTab_thenRuntimeTabsShownInOrder() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        RuntimeTabFragment runtimeTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeTab();

        // then
        runtimeTab.getTabs()
                .shouldHave(size(5))
                .shouldHave(texts("Variables", "User tasks", "Jobs", "External tasks", "Incidents"));
    }

    @Test
    @DisplayName("History tab shows nested tabs in order: Activities, User tasks, Variables, Incidents, Decisions")
    void givenActiveProcessInstance_whenOpenHistoryTab_thenHistoryTabsShownInOrder() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        HistoryTabFragment historyTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryTab();

        // then
        historyTab.getTabs()
                .shouldHave(size(5))
                .shouldHave(texts("Activities", "User tasks", "Variables", "Incidents", "Decisions"));
    }

    @Test
    @DisplayName("Variables tab caption (Runtime tab) shows the count of variables of the selected activity")
    void givenProcessInstanceVariables_whenSelectActivity_thenRuntimeVariablesTabCaptionShowsFilteredCount() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", StartProcessDto.builder()
                        .variable("firstVariable", new VariableValueDto("String", "first"))
                        .variable("secondVariable", new VariableValueDto("String", "second"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        RuntimeTabFragment runtimeTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeTab();

        // then
        runtimeTab.getTabsheet()
                .getTabById("runtimeVariablesTab")
                .shouldHave(text("Variables (2)"));

        runtimeTab.selectRowByActivityId("userTask");

        runtimeTab.getTabsheet()
                .getTabById("runtimeVariablesTab")
                .shouldHave(text("Variables (0)"));

        runtimeTab.deselectRowByActivityId("userTask");

        runtimeTab.getTabsheet()
                .getTabById("runtimeVariablesTab")
                .shouldHave(text("Variables (2)"));
    }

    @Test
    @DisplayName("Variables are filtered by the activity selected while another tab is open")
    void givenGlobalAndLocalVariables_whenSelectActivityInUserTasksTabAndReturnToVariablesTab_thenVariablesFilteredByActivity() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testScopeColumnUnnamedElements.bpmn")
                .startByKey("testScopeColumnUnnamedElements", StartProcessDto.builder()
                        .variable("globalVariable", new VariableValueDto("String", "globalValue"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testScopeColumnUnnamedElements").getFirst();

        MainView mainView = loginAsAdmin();

        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);
        detailView.openRuntimeVariablesTab()
                .getRuntimeVariablesGrid()
                .shouldHave(visibleBodyRowCount(2));

        detailView.openRuntimeUserTasksTab();
        detailView.openRuntimeTab()
                .selectRowByActivityId("unnamedSubProcess");

        // when
        RuntimeVariablesTabFragment variablesTab = detailView.openRuntimeVariablesTab();

        // then
        variablesTab.getRuntimeVariablesGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(bodyRowWithCellTexts(Map.of(RuntimeVariablesTabFragment.NAME_COLUMN_INDEX, "subProcessLocalVariable")));
    }

    @Test
    @DisplayName("Activities tab caption (History tab) shows the count of historic activity instances")
    void givenActiveProcessInstance_whenOpenHistoryTab_thenActivitiesTabCaptionShowsCount() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        HistoryTabFragment historyTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryTab();

        // then
        historyTab.getTabsheet()
                .getTabById("historicActivityInstancesTab")
                .shouldHave(text("Activities (2)"));
    }

    @Test
    @DisplayName("User tasks tab caption (History tab) shows the count of historic user tasks")
    void givenActiveProcessInstance_whenOpenHistoryTab_thenUserTasksTabCaptionShowsCount() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        HistoryTabFragment historyTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryTab();

        // then
        historyTab.getTabsheet()
                .getTabById("historyTasksTab")
                .shouldHave(text("User tasks (1)"));
    }

    @Test
    @DisplayName("Variables tab caption (History tab) shows the count of historic variables")
    void givenProcessInstanceVariables_whenOpenHistoryTab_thenVariablesTabCaptionShowsCount() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", StartProcessDto.builder()
                        .variable("firstVariable", new VariableValueDto("String", "first"))
                        .variable("secondVariable", new VariableValueDto("String", "second"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        HistoryTabFragment historyTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryTab();

        // then
        historyTab.getTabsheet()
                .getTabById("historyVariablesTab")
                .shouldHave(text("Variables (2)"));
    }

    @Test
    @DisplayName("Incidents tab caption (History tab) shows the count of historic incidents")
    void givenFailedJobIncident_whenOpenHistoryTab_thenIncidentsTabCaptionShowsCount() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        HistoryTabFragment historyTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryTab();

        // then
        historyTab.getTabsheet()
                .getTabById("historyIncidentsTab")
                .shouldHave(text("Incidents (1)"));
    }

    @Test
    @DisplayName("Decisions tab caption (History tab) shows the count of historic decision instances")
    void givenEvaluatedDecision_whenOpenHistoryTab_thenDecisionsTabCaptionShowsCount() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision");
        String instanceId = dataManager.getStartedInstances("testProcessWithDecision").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        HistoryTabFragment historyTab = mainView.openProcessInstanceListView()
                .switchToAllViewMode()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryTab();

        // then
        historyTab.getTabsheet()
                .getTabById("historyDecisionsTab")
                .shouldHave(text("Decisions (1)"));
    }

    @Test
    @DisplayName("Activities tab (History tab) shows activities of completed instance after page reload")
    void givenCompletedProcessInstance_whenOpenHistoryActivitiesTabAfterPageReload_thenActivitiesShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable");
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").getFirst();
        camundaRestTestHelper.completeTaskById(camunda7, dataManager.getUserTasksByKey("testUpdateVariable").getFirst());

        MainView mainView = loginAsAdmin();
        mainView.openProcessInstanceListView()
                .switchToAllViewMode()
                .openDetailViewByInstanceId(instanceId);

        // when
        refresh();

        ProcessInstanceDetailView detailView = $j(ProcessInstanceDetailView.class);
        HistoryTabFragment historyTab = detailView.openHistoryTab();
        HistoryActivitiesTabFragment activitiesTab = detailView.openHistoryActivitiesTab();

        // then
        historyTab.getTabsheet()
                .getTabById("historicActivityInstancesTab")
                .shouldHave(text("Activities (3)"));

        activitiesTab.getRowByActivityId("userTask")
                .getCellByIndex(ACTIVITY_NAME_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("User task"));
    }

    @Test
    @DisplayName("User tasks tab (History tab) shows user tasks of completed instance after page reload")
    void givenCompletedProcessInstance_whenOpenHistoryUserTasksTabAfterPageReload_thenUserTasksShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable");
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").getFirst();
        camundaRestTestHelper.completeTaskById(camunda7, dataManager.getUserTasksByKey("testUpdateVariable").getFirst());

        MainView mainView = loginAsAdmin();
        mainView.openProcessInstanceListView()
                .switchToAllViewMode()
                .openDetailViewByInstanceId(instanceId);

        // when
        refresh();

        ProcessInstanceDetailView detailView = $j(ProcessInstanceDetailView.class);
        HistoryTabFragment historyTab = detailView.openHistoryTab();
        HistoryUserTasksTabFragment userTasksTab = detailView.openHistoryUserTasksTab();

        // then
        historyTab.getTabsheet()
                .getTabById("historyTasksTab")
                .shouldHave(text("User tasks (1)"));

        userTasksTab.getRowByTaskDefinitionKey("userTask")
                .getCellByIndex(NAME_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("User task"));
    }

    @Test
    @DisplayName("Variables tab (History tab) shows variables of completed instance after page reload")
    void givenCompletedProcessInstance_whenOpenHistoryVariablesTabAfterPageReload_thenVariablesShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", StartProcessDto.builder()
                        .variable("firstVariable", new VariableValueDto("String", "first"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").getFirst();
        camundaRestTestHelper.completeTaskById(camunda7, dataManager.getUserTasksByKey("testUpdateVariable").getFirst());

        MainView mainView = loginAsAdmin();
        mainView.openProcessInstanceListView()
                .switchToAllViewMode()
                .openDetailViewByInstanceId(instanceId);

        // when
        refresh();

        ProcessInstanceDetailView detailView = $j(ProcessInstanceDetailView.class);
        HistoryTabFragment historyTab = detailView.openHistoryTab();
        HistoryVariablesTabFragment variablesTab = detailView.openHistoryVariablesTab();

        // then
        historyTab.getTabsheet()
                .getTabById("historyVariablesTab")
                .shouldHave(text("Variables (1)"));

        variablesTab.getRowByVariableName("firstVariable")
                .getCellByIndex(VALUE_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("first"));
    }

    @Test
    @DisplayName("Incidents tab (History tab) shows incidents of completed instance after page reload")
    void givenCompletedProcessInstance_whenOpenHistoryIncidentsTabAfterPageReload_thenIncidentsShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testResolvedFailedJobIncident.bpmn")
                .startByKey("testResolvedFailedJobIncident")
                .waitJobsExecution()
                .retryFailedJobs()
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testResolvedFailedJobIncident").getFirst();

        MainView mainView = loginAsAdmin();
        mainView.openProcessInstanceListView()
                .switchToAllViewMode()
                .openDetailViewByInstanceId(instanceId);

        // when
        refresh();

        ProcessInstanceDetailView detailView = $j(ProcessInstanceDetailView.class);
        HistoryTabFragment historyTab = detailView.openHistoryTab();
        HistoryIncidentsTabFragment incidentsTab = detailView.openHistoryIncidentsTab();

        // then
        historyTab.getTabsheet()
                .getTabById("historyIncidentsTab")
                .shouldHave(text("Incidents (1)"));

        incidentsTab.getRowByActivityId("throwsExceptionConditionallyTask")
                .getCellByIndex(MESSAGE_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("Some service not available"));
    }

    @Test
    @DisplayName("Decisions tab (History tab) shows decision instances of completed instance after page reload")
    void givenCompletedProcessInstance_whenOpenHistoryDecisionsTabAfterPageReload_thenDecisionInstancesShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision");
        String instanceId = dataManager.getStartedInstances("testProcessWithDecision").getFirst();

        MainView mainView = loginAsAdmin();
        mainView.openProcessInstanceListView()
                .switchToAllViewMode()
                .openDetailViewByInstanceId(instanceId);

        // when
        refresh();

        ProcessInstanceDetailView detailView = $j(ProcessInstanceDetailView.class);
        HistoryTabFragment historyTab = detailView.openHistoryTab();
        HistoryDecisionsTabFragment decisionsTab = detailView.openHistoryDecisionsTab();

        // then
        historyTab.getTabsheet()
                .getTabById("historyDecisionsTab")
                .shouldHave(text("Decisions (1)"));

        decisionsTab.getRowByActivityId("evaluateDecisionTask")
                .getCellByIndex(DECISION_DEFINITION_ID_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("decision_testDmn"));
    }
}
