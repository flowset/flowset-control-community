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
import io.flowset.control.test_support.ui.view.historicactivityinstancedata.HistoricActivityInstanceDataDetailDialog;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryActivitiesTabFragment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.value;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryActivitiesTabFragment.ACTIVITY_INSTANCE_ID_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryActivitiesTabFragment.ID_BUTTON_BY;
import static io.jmix.masquerade.JConditions.visibleItemsCount;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions in the Activities tab (History tab) in Process instance detail view")
public class HistoryActivitiesActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("No actions available for the Activities data grid")
    void givenExistingHistoricActivity_whenOpenHistoryActivitiesTab_thenNoGridActionsAvailable() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        HistoryActivitiesTabFragment activitiesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryActivitiesTab();

        activitiesTab.selectRowByActivityId("approveVacationTask");

        // then
        activitiesTab.openActivityGridContextMenu()
                .shouldHave(visibleItemsCount(0));
    }

    @Test
    @DisplayName("Activity instance ID link in data grid opens Historic activity instance detail dialog")
    void givenExistingHistoricActivity_whenActivityInstanceIdLinkClicked_thenHistoricActivityDetailDialogOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        HistoryActivitiesTabFragment activitiesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryActivitiesTab();

        activitiesTab.getRowByActivityId("approveVacationTask")
                .getCellByIndex(ACTIVITY_INSTANCE_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY).click();

        // then
        HistoricActivityInstanceDataDetailDialog dialog = $j(HistoricActivityInstanceDataDetailDialog.class).exists()
                .displayed();
        dialog.getActivityIdField().shouldHave(value("approveVacationTask"));
        dialog.getActivityNameField().shouldHave(value("Approve vacation"));
    }

    @Test
    @DisplayName("Double-clicking an activity row opens Historic activity instance detail dialog")
    void givenExistingHistoricActivity_whenRowDoubleClicked_thenHistoricActivityDetailDialogOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        HistoryActivitiesTabFragment activitiesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryActivitiesTab();

        activitiesTab.getRowByActivityId("approveVacationTask")
                .getDelegate()
                .doubleClick();

        // then
        HistoricActivityInstanceDataDetailDialog dialog = $j(HistoricActivityInstanceDataDetailDialog.class).exists()
                .displayed();
        dialog.getActivityIdField().shouldHave(value("approveVacationTask"));
        dialog.getActivityNameField().shouldHave(value("Approve vacation"));
    }
}
