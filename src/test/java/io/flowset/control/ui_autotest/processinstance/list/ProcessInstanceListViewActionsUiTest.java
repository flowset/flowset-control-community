/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.list;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessDefinitionDetailView;
import io.flowset.control.test_support.ui.view.processinstance.ProcessInstanceListView;
import io.flowset.control.test_support.ui.view.processinstance.detail.ProcessInstanceDetailView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static io.flowset.control.test_support.ui.condition.ControlCondition.*;
import static io.flowset.control.test_support.ui.view.processinstance.ProcessInstanceListView.*;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions on process instance list view")
public class ProcessInstanceListViewActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Refresh action availability on process instance list view")
    void givenLoggedInUser_whenOpenProcessInstanceList_thenRefreshActionAvailable() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();

        // then
        listView.getRefreshBtn()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        listView.openInstancesGridContextActions()
                .find(text("Refresh"))
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Refresh action updates data in the data grid")
    void givenLoggedInUser_whenClickRefresh_thenDataUpdated() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();

        // then
        listView.getProcessInstancesGrid()
                .shouldBe(emptyGrid);

        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        listView.getRefreshBtn()
                .shouldBe(VISIBLE).click();

        listView.getProcessInstancesGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellElementText(ID_COLUMN_INDEX, ID_BUTTON_BY, instanceId));
    }

    @Test
    @DisplayName("View action in the Id column opens process instance detail view")
    void givenExistingProcessInstance_whenInstanceIdLinkClicked_thenProcessInstanceDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();

        // then
        listView.getRowByInstanceId(instanceId)
                .getCellByIndex(ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY).click();

        $j(ProcessInstanceDetailView.class).exists()
                .displayed();
    }

    @Test
    @DisplayName("Double-click opens process instance detail view")
    void givenExistingProcessInstance_whenRowDoubleClicked_thenProcessInstanceDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();

        // then
        listView.getRowByInstanceId(instanceId)
                .getCellByIndex(BUSINESS_KEY_COLUMN_INDEX)
                .getCellContent()
                .doubleClick();

        webdriver().shouldHave(urlContaining("/bpm/process-instances/" + instanceId));

        $j(ProcessInstanceDetailView.class).exists()
                .displayed();
    }

    @Test
    @DisplayName("View action in the Process column opens process detail view")
    void givenExistingProcessInstance_whenProcessLinkClicked_thenProcessDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String processId = dataManager.getDeployedProcessVersions("vacation_approval").get(0);
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();

        // then
        listView.getRowByInstanceId(instanceId)
                .getCellByIndex(PROCESS_COLUMN_INDEX)
                .getCellContent()
                .find(PROCESS_BUTTON_BY).click();

        webdriver().shouldHave(urlContaining("/bpm/process-definitions/" + processId));

        $j(ProcessDefinitionDetailView.class).exists()
                .displayed();
    }

    @Test
    @DisplayName("List of available actions on process instance list view")
    void givenExistingProcessInstance_whenOpenProcessInstanceList_thenActionsAreVisible() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();

        // then
        listView.getRefreshBtn().shouldBe(VISIBLE);
        listView.getBulkTerminateBtn().shouldBe(VISIBLE);
        listView.getBulkSuspendBtn().shouldBe(VISIBLE);
        listView.getBulkActivateBtn().shouldBe(VISIBLE);


        listView.openInstancesGridContextActions()
                .shouldHave(visibleItems("Refresh", "Terminate", "Activate", "Suspend"));
    }
}
