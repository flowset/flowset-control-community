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
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.JobsTabFragment;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeTabFragment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.text;
import static io.flowset.control.test_support.ui.condition.ControlCondition.allBodyRowsHaveCellText;
import static io.flowset.control.test_support.ui.condition.ControlCondition.anyBodyRowHaveCellText;
import static io.flowset.control.test_support.ui.condition.ControlCondition.visibleBodyRowCount;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.JobsTabFragment.PRIORITY_COLUMN_INDEX;

@WithRunningExternalEngine
@DisplayName("Filtering by activity in the Jobs tab (Runtime tab) in Process instance detail view")
public class JobsTabFilterUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Jobs are filtered by the activity selected before the Jobs tab is opened")
    void givenJobsOnDifferentActivities_whenSelectActivityAndOpenJobsTab_thenJobsFilteredByActivity() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testJobsListLoad.bpmn")
                .startByKey("testJobsListLoad");
        String instanceId = dataManager.getStartedInstances("testJobsListLoad").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);
        detailView.openRuntimeTab()
                .selectRowByActivityId("timerEvent");

        JobsTabFragment jobsTab = detailView.openRuntimeJobsTab();

        // then
        jobsTab.getRuntimeJobsGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(PRIORITY_COLUMN_INDEX, "20"));
    }

    @Test
    @DisplayName("Jobs are filtered by the activity selected after the Jobs tab is opened")
    void givenJobsOnDifferentActivities_whenOpenJobsTabAndSelectActivity_thenJobsFilteredByActivity() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testJobsListLoad.bpmn")
                .startByKey("testJobsListLoad");
        String instanceId = dataManager.getStartedInstances("testJobsListLoad").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);
        JobsTabFragment jobsTab = detailView.openRuntimeJobsTab();

        detailView.openRuntimeTab()
                .selectRowByActivityId("timerEvent");

        // then
        jobsTab.getRuntimeJobsGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(PRIORITY_COLUMN_INDEX, "20"));
    }

    @Test
    @DisplayName("Jobs of all activities are shown when no activity is selected")
    void givenJobsOnDifferentActivities_whenOpenJobsTabWithoutActivitySelection_thenAllJobsShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testJobsListLoad.bpmn")
                .startByKey("testJobsListLoad");
        String instanceId = dataManager.getStartedInstances("testJobsListLoad").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        JobsTabFragment jobsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeJobsTab();

        // then
        jobsTab.getRuntimeJobsGrid()
                .shouldHave(visibleBodyRowCount(2))
                .shouldHave(anyBodyRowHaveCellText(PRIORITY_COLUMN_INDEX, "20"))
                .shouldHave(anyBodyRowHaveCellText(PRIORITY_COLUMN_INDEX, "30"));
    }

    @Test
    @DisplayName("Jobs are filtered by another activity selected in the activity tree")
    void givenJobsOnDifferentActivities_whenSelectAnotherActivity_thenJobsFilteredByAnotherActivity() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testJobsListLoad.bpmn")
                .startByKey("testJobsListLoad");
        String instanceId = dataManager.getStartedInstances("testJobsListLoad").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);
        detailView.openRuntimeTab()
                .selectRowByActivityId("timerEvent");

        JobsTabFragment jobsTab = detailView.openRuntimeJobsTab();

        // then
        jobsTab.getRuntimeJobsGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(PRIORITY_COLUMN_INDEX, "20"));

        detailView.openRuntimeTab()
                .selectRowByActivityId("failedScriptTask");

        jobsTab.getRuntimeJobsGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(PRIORITY_COLUMN_INDEX, "30"));
    }

    @Test
    @DisplayName("Jobs of all activities are shown after the activity selection is cleared")
    void givenJobsOnDifferentActivities_whenDeselectActivity_thenAllJobsShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testJobsListLoad.bpmn")
                .startByKey("testJobsListLoad");
        String instanceId = dataManager.getStartedInstances("testJobsListLoad").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);
        detailView.openRuntimeTab()
                .selectRowByActivityId("timerEvent");

        JobsTabFragment jobsTab = detailView.openRuntimeJobsTab();

        // then
        jobsTab.getRuntimeJobsGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(PRIORITY_COLUMN_INDEX, "20"));

        detailView.openRuntimeTab()
                .deselectRowByActivityId("timerEvent");

        jobsTab.getRuntimeJobsGrid()
                .shouldHave(visibleBodyRowCount(2))
                .shouldHave(anyBodyRowHaveCellText(PRIORITY_COLUMN_INDEX, "20"))
                .shouldHave(anyBodyRowHaveCellText(PRIORITY_COLUMN_INDEX, "30"));
    }

    @Test
    @DisplayName("Jobs tab caption shows the count of jobs of the selected activity")
    void givenJobsOnDifferentActivities_whenSelectActivity_thenJobsTabCaptionShowsFilteredCount() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testJobsListLoad.bpmn")
                .startByKey("testJobsListLoad");
        String instanceId = dataManager.getStartedInstances("testJobsListLoad").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        RuntimeTabFragment runtimeTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeTab();
        runtimeTab.selectRowByActivityId("timerEvent");

        // then
        runtimeTab.getTabsheet()
                .getTabById("jobsTab")
                .shouldHave(text("Jobs (1)"));

        runtimeTab.deselectRowByActivityId("timerEvent");

        runtimeTab.getTabsheet()
                .getTabById("jobsTab")
                .shouldHave(text("Jobs (2)"));
    }
}
