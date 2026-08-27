/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.detail.tab.history;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.historicincidentdata.HistoricIncidentDataDetailDialog;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryIncidentsTabFragment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.value;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryIncidentsTabFragment.ID_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryIncidentsTabFragment.INCIDENT_ID_COLUMN_INDEX;
import static io.jmix.masquerade.JConditions.visibleItemsCount;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions in the Incidents tab (History tab) in Process instance detail view")
public class HistoryIncidentsTabActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("No actions available for the Incidents data grid")
    void givenExistingHistoricIncident_whenOpenHistoryIncidentsTab_thenNoGridActionsAvailable() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        HistoryIncidentsTabFragment incidentsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryIncidentsTab();

        incidentsTab.selectRowByActivityId("throwsExceptionTask");

        // then
        incidentsTab.openIncidentsGridContextMenu()
                .shouldHave(visibleItemsCount(0));
    }

    @Test
    @DisplayName("Incident ID link in data grid opens Historic incident detail dialog")
    void givenExistingHistoricIncident_whenIncidentIdLinkClicked_thenHistoricIncidentDetailDialogOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        HistoryIncidentsTabFragment incidentsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryIncidentsTab();

        incidentsTab.getRowByActivityId("throwsExceptionTask")
                .getCellByIndex(INCIDENT_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY).click();

        // then
        HistoricIncidentDataDetailDialog dialog = $j(HistoricIncidentDataDetailDialog.class).exists()
                .displayed();
        dialog.getActivityIdField().shouldHave(value("throwsExceptionTask"));
        dialog.getProcessInstanceIdField().shouldHave(value(instanceId));
    }

    @Test
    @DisplayName("Double-clicking an incident row opens Historic incident detail dialog")
    void givenExistingHistoricIncident_whenRowDoubleClicked_thenHistoricIncidentDetailDialogOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        HistoryIncidentsTabFragment incidentsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryIncidentsTab();

        incidentsTab.getRowByActivityId("throwsExceptionTask")
                .getDelegate()
                .doubleClick();

        // then
        HistoricIncidentDataDetailDialog dialog = $j(HistoricIncidentDataDetailDialog.class).exists()
                .displayed();
        dialog.getActivityIdField().shouldHave(value("throwsExceptionTask"));
        dialog.getProcessInstanceIdField().shouldHave(value(instanceId));
    }
}
