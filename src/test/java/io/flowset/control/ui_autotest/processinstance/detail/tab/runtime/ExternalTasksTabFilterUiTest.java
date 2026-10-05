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
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.ExternalTasksTabFragment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static io.flowset.control.test_support.ui.condition.ControlCondition.allBodyRowsHaveCellText;
import static io.flowset.control.test_support.ui.condition.ControlCondition.anyBodyRowHaveCellText;
import static io.flowset.control.test_support.ui.condition.ControlCondition.visibleBodyRowCount;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.ExternalTasksTabFragment.ACTIVITY_ID_COLUMN_INDEX;

@WithRunningExternalEngine
@DisplayName("Filtering by activity in the External tasks tab (Runtime tab) in Process instance detail view")
public class ExternalTasksTabFilterUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("External tasks are filtered by the activity selected before the External tasks tab is opened")
    void givenParallelExternalTasks_whenSelectActivityAndOpenExternalTasksTab_thenExternalTasksFilteredByActivity() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testParallelExternalTasks.bpmn")
                .startByKey("testParallelExternalTasks");
        String instanceId = dataManager.getStartedInstances("testParallelExternalTasks").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);
        detailView.openRuntimeTab()
                .selectRowByActivityId("firstExternalTask");

        ExternalTasksTabFragment externalTasksTab = detailView.openRuntimeExternalTasksTab();

        // then
        externalTasksTab.getRuntimeExternalTasksGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(ACTIVITY_ID_COLUMN_INDEX, "firstExternalTask"));
    }

    @Test
    @DisplayName("External tasks are filtered by the activity selected after the External tasks tab is opened")
    void givenParallelExternalTasks_whenOpenExternalTasksTabAndSelectActivity_thenExternalTasksFilteredByActivity() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testParallelExternalTasks.bpmn")
                .startByKey("testParallelExternalTasks");
        String instanceId = dataManager.getStartedInstances("testParallelExternalTasks").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);
        ExternalTasksTabFragment externalTasksTab = detailView.openRuntimeExternalTasksTab();

        detailView.openRuntimeTab()
                .selectRowByActivityId("firstExternalTask");

        // then
        externalTasksTab.getRuntimeExternalTasksGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(ACTIVITY_ID_COLUMN_INDEX, "firstExternalTask"));
    }

    @Test
    @DisplayName("External tasks of all activities are shown when no activity is selected")
    void givenParallelExternalTasks_whenOpenExternalTasksTabWithoutActivitySelection_thenAllExternalTasksShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testParallelExternalTasks.bpmn")
                .startByKey("testParallelExternalTasks");
        String instanceId = dataManager.getStartedInstances("testParallelExternalTasks").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        ExternalTasksTabFragment externalTasksTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeExternalTasksTab();

        // then
        externalTasksTab.getRuntimeExternalTasksGrid()
                .shouldHave(visibleBodyRowCount(2))
                .shouldHave(anyBodyRowHaveCellText(ACTIVITY_ID_COLUMN_INDEX, "firstExternalTask"))
                .shouldHave(anyBodyRowHaveCellText(ACTIVITY_ID_COLUMN_INDEX, "secondExternalTask"));
    }

    @Test
    @DisplayName("External tasks are filtered by another activity selected in the activity tree")
    void givenParallelExternalTasks_whenSelectAnotherActivity_thenExternalTasksFilteredByAnotherActivity() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testParallelExternalTasks.bpmn")
                .startByKey("testParallelExternalTasks");
        String instanceId = dataManager.getStartedInstances("testParallelExternalTasks").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);
        detailView.openRuntimeTab()
                .selectRowByActivityId("firstExternalTask");

        ExternalTasksTabFragment externalTasksTab = detailView.openRuntimeExternalTasksTab();

        // then
        externalTasksTab.getRuntimeExternalTasksGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(ACTIVITY_ID_COLUMN_INDEX, "firstExternalTask"));

        detailView.openRuntimeTab()
                .selectRowByActivityId("secondExternalTask");

        externalTasksTab.getRuntimeExternalTasksGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(ACTIVITY_ID_COLUMN_INDEX, "secondExternalTask"));
    }

    @Test
    @DisplayName("External tasks of all activities are shown after the activity selection is cleared")
    void givenParallelExternalTasks_whenDeselectActivity_thenAllExternalTasksShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testParallelExternalTasks.bpmn")
                .startByKey("testParallelExternalTasks");
        String instanceId = dataManager.getStartedInstances("testParallelExternalTasks").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);
        detailView.openRuntimeTab()
                .selectRowByActivityId("firstExternalTask");

        ExternalTasksTabFragment externalTasksTab = detailView.openRuntimeExternalTasksTab();

        // then
        externalTasksTab.getRuntimeExternalTasksGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(ACTIVITY_ID_COLUMN_INDEX, "firstExternalTask"));

        detailView.openRuntimeTab()
                .deselectRowByActivityId("firstExternalTask");

        externalTasksTab.getRuntimeExternalTasksGrid()
                .shouldHave(visibleBodyRowCount(2))
                .shouldHave(anyBodyRowHaveCellText(ACTIVITY_ID_COLUMN_INDEX, "firstExternalTask"))
                .shouldHave(anyBodyRowHaveCellText(ACTIVITY_ID_COLUMN_INDEX, "secondExternalTask"));
    }
}
