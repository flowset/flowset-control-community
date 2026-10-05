/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.detail.tab.runtime;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processinstance.detail.ProcessInstanceDetailView;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeUserTasksTabFragment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static io.flowset.control.test_support.ui.condition.ControlCondition.allBodyRowsHaveCellText;
import static io.flowset.control.test_support.ui.condition.ControlCondition.anyBodyRowHaveCellText;
import static io.flowset.control.test_support.ui.condition.ControlCondition.visibleBodyRowCount;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeUserTasksTabFragment.TASK_DEFINITION_KEY_COLUMN_INDEX;

@WithRunningExternalEngine
@DisplayName("Filtering by activity in the User tasks tab (Runtime tab) in Process instance detail view")
public class RuntimeUserTasksTabFilterUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("User tasks are filtered by the activity selected before the User tasks tab is opened")
    void givenParallelUserTasks_whenSelectActivityAndOpenUserTasksTab_thenUserTasksFilteredByActivity() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testParallelUserTasks.bpmn")
                .startByKey("testParallelUserTasks");
        String instanceId = dataManager.getStartedInstances("testParallelUserTasks").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);
        detailView.openRuntimeTab()
                .selectRowByActivityId("firstApprovalTask");

        RuntimeUserTasksTabFragment userTasksTab = detailView.openRuntimeUserTasksTab();

        // then
        userTasksTab.getRuntimeUserTasksGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(TASK_DEFINITION_KEY_COLUMN_INDEX, "firstApprovalTask"));
    }

    @Test
    @DisplayName("User tasks are filtered by the activity selected after the User tasks tab is opened")
    void givenParallelUserTasks_whenOpenUserTasksTabAndSelectActivity_thenUserTasksFilteredByActivity() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testParallelUserTasks.bpmn")
                .startByKey("testParallelUserTasks");
        String instanceId = dataManager.getStartedInstances("testParallelUserTasks").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);
        RuntimeUserTasksTabFragment userTasksTab = detailView.openRuntimeUserTasksTab();

        detailView.openRuntimeTab()
                .selectRowByActivityId("firstApprovalTask");

        // then
        userTasksTab.getRuntimeUserTasksGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(TASK_DEFINITION_KEY_COLUMN_INDEX, "firstApprovalTask"));
    }

    @Test
    @DisplayName("User tasks of all activities are shown when no activity is selected")
    void givenParallelUserTasks_whenOpenUserTasksTabWithoutActivitySelection_thenAllUserTasksShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testParallelUserTasks.bpmn")
                .startByKey("testParallelUserTasks");
        String instanceId = dataManager.getStartedInstances("testParallelUserTasks").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        RuntimeUserTasksTabFragment userTasksTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeUserTasksTab();

        // then
        userTasksTab.getRuntimeUserTasksGrid()
                .shouldHave(visibleBodyRowCount(2))
                .shouldHave(anyBodyRowHaveCellText(TASK_DEFINITION_KEY_COLUMN_INDEX, "firstApprovalTask"))
                .shouldHave(anyBodyRowHaveCellText(TASK_DEFINITION_KEY_COLUMN_INDEX, "secondApprovalTask"));
    }

    @Test
    @DisplayName("User tasks are filtered by another activity selected in the activity tree")
    void givenParallelUserTasks_whenSelectAnotherActivity_thenUserTasksFilteredByAnotherActivity() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testParallelUserTasks.bpmn")
                .startByKey("testParallelUserTasks");
        String instanceId = dataManager.getStartedInstances("testParallelUserTasks").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);
        detailView.openRuntimeTab()
                .selectRowByActivityId("firstApprovalTask");

        RuntimeUserTasksTabFragment userTasksTab = detailView.openRuntimeUserTasksTab();

        // then
        userTasksTab.getRuntimeUserTasksGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(TASK_DEFINITION_KEY_COLUMN_INDEX, "firstApprovalTask"));

        detailView.openRuntimeTab()
                .selectRowByActivityId("secondApprovalTask");

        userTasksTab.getRuntimeUserTasksGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(TASK_DEFINITION_KEY_COLUMN_INDEX, "secondApprovalTask"));
    }

    @Test
    @DisplayName("User tasks of all activities are shown after the activity selection is cleared")
    void givenParallelUserTasks_whenDeselectActivity_thenAllUserTasksShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testParallelUserTasks.bpmn")
                .startByKey("testParallelUserTasks");
        String instanceId = dataManager.getStartedInstances("testParallelUserTasks").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);
        detailView.openRuntimeTab()
                .selectRowByActivityId("firstApprovalTask");

        RuntimeUserTasksTabFragment userTasksTab = detailView.openRuntimeUserTasksTab();

        // then
        userTasksTab.getRuntimeUserTasksGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(TASK_DEFINITION_KEY_COLUMN_INDEX, "firstApprovalTask"));

        detailView.openRuntimeTab()
                .deselectRowByActivityId("firstApprovalTask");

        userTasksTab.getRuntimeUserTasksGrid()
                .shouldHave(visibleBodyRowCount(2))
                .shouldHave(anyBodyRowHaveCellText(TASK_DEFINITION_KEY_COLUMN_INDEX, "firstApprovalTask"))
                .shouldHave(anyBodyRowHaveCellText(TASK_DEFINITION_KEY_COLUMN_INDEX, "secondApprovalTask"));
    }
}
