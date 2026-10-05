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
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeIncidentsTabFragment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static io.flowset.control.test_support.ui.condition.ControlCondition.allBodyRowsHaveCellText;
import static io.flowset.control.test_support.ui.condition.ControlCondition.anyBodyRowHaveCellText;
import static io.flowset.control.test_support.ui.condition.ControlCondition.visibleBodyRowCount;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeIncidentsTabFragment.ACTIVITY_ID_COLUMN_INDEX;

@WithRunningExternalEngine
@DisplayName("Filtering by activity in the Incidents tab (Runtime tab) in Process instance detail view")
public class RuntimeIncidentsTabFilterUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Incidents are filtered by the activity selected before the Incidents tab is opened")
    void givenIncidentsOnDifferentActivities_whenSelectActivityAndOpenIncidentsTab_thenIncidentsFilteredByActivity() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testMultipleFailedJobIncidents.bpmn")
                .startByKey("testMultipleFailedJobIncidents")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testMultipleFailedJobIncidents").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);
        detailView.openRuntimeTab()
                .selectRowByActivityId("throwOneExceptionTask");

        RuntimeIncidentsTabFragment incidentsTab = detailView.openRuntimeIncidentsTab();

        // then
        incidentsTab.getRuntimeIncidentsGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(ACTIVITY_ID_COLUMN_INDEX, "throwOneExceptionTask"));
    }

    @Test
    @DisplayName("Incidents are filtered by the activity selected after the Incidents tab is opened")
    void givenIncidentsOnDifferentActivities_whenOpenIncidentsTabAndSelectActivity_thenIncidentsFilteredByActivity() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testMultipleFailedJobIncidents.bpmn")
                .startByKey("testMultipleFailedJobIncidents")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testMultipleFailedJobIncidents").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);
        RuntimeIncidentsTabFragment incidentsTab = detailView.openRuntimeIncidentsTab();

        detailView.openRuntimeTab()
                .selectRowByActivityId("throwOneExceptionTask");

        // then
        incidentsTab.getRuntimeIncidentsGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(ACTIVITY_ID_COLUMN_INDEX, "throwOneExceptionTask"));
    }

    @Test
    @DisplayName("Incidents of all activities are shown when no activity is selected")
    void givenIncidentsOnDifferentActivities_whenOpenIncidentsTabWithoutActivitySelection_thenAllIncidentsShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testMultipleFailedJobIncidents.bpmn")
                .startByKey("testMultipleFailedJobIncidents")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testMultipleFailedJobIncidents").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        RuntimeIncidentsTabFragment incidentsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeIncidentsTab();

        // then
        incidentsTab.getRuntimeIncidentsGrid()
                .shouldHave(visibleBodyRowCount(4))
                .shouldHave(anyBodyRowHaveCellText(ACTIVITY_ID_COLUMN_INDEX, "throwOneExceptionTask"))
                .shouldHave(anyBodyRowHaveCellText(ACTIVITY_ID_COLUMN_INDEX, "throwsMultipleExceptionsTask"));
    }

    @Test
    @DisplayName("Incidents are filtered by another activity selected in the activity tree")
    void givenIncidentsOnDifferentActivities_whenSelectAnotherActivity_thenIncidentsFilteredByAnotherActivity() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testMultipleFailedJobIncidents.bpmn")
                .startByKey("testMultipleFailedJobIncidents")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testMultipleFailedJobIncidents").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);
        detailView.openRuntimeTab()
                .selectRowByActivityId("throwOneExceptionTask");

        RuntimeIncidentsTabFragment incidentsTab = detailView.openRuntimeIncidentsTab();

        // then
        incidentsTab.getRuntimeIncidentsGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(ACTIVITY_ID_COLUMN_INDEX, "throwOneExceptionTask"));

        detailView.openRuntimeTab()
                .selectRowByActivityId("throwsMultipleExceptionsTask");

        incidentsTab.getRuntimeIncidentsGrid()
                .shouldHave(visibleBodyRowCount(3))
                .shouldHave(allBodyRowsHaveCellText(ACTIVITY_ID_COLUMN_INDEX, "throwsMultipleExceptionsTask"));
    }

    @Test
    @DisplayName("Incidents of all activities are shown after the activity selection is cleared")
    void givenIncidentsOnDifferentActivities_whenDeselectActivity_thenAllIncidentsShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testMultipleFailedJobIncidents.bpmn")
                .startByKey("testMultipleFailedJobIncidents")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testMultipleFailedJobIncidents").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);
        detailView.openRuntimeTab()
                .selectRowByActivityId("throwOneExceptionTask");

        RuntimeIncidentsTabFragment incidentsTab = detailView.openRuntimeIncidentsTab();

        // then
        incidentsTab.getRuntimeIncidentsGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(ACTIVITY_ID_COLUMN_INDEX, "throwOneExceptionTask"));

        detailView.openRuntimeTab()
                .deselectRowByActivityId("throwOneExceptionTask");

        incidentsTab.getRuntimeIncidentsGrid()
                .shouldHave(visibleBodyRowCount(4))
                .shouldHave(anyBodyRowHaveCellText(ACTIVITY_ID_COLUMN_INDEX, "throwOneExceptionTask"))
                .shouldHave(anyBodyRowHaveCellText(ACTIVITY_ID_COLUMN_INDEX, "throwsMultipleExceptionsTask"));
    }
}
