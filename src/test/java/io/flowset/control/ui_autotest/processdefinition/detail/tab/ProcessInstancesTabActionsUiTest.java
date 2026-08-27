/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processdefinition.detail.tab;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessInstancesTabFragment;
import io.flowset.control.test_support.ui.view.processinstance.detail.ProcessInstanceDetailView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessInstancesTabFragment.*;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions on Process instances tab in process detail view")
public class ProcessInstancesTabActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Refresh action availability on process instances tab")
    void givenExistingProcessDefinition_whenOpenDetailView_thenProcessInstancesRefreshActionAvailable() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");
        MainView mainView = loginAsAdmin();

        // when
        ProcessInstancesTabFragment processInstancesTabContent = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval")
                .openProcessInstancesTab();

        // then
        processInstancesTabContent
                .getRefreshButton()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        processInstancesTabContent.openInstancesGridContextMenu()
                .find(text("Refresh"))
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Suspend action availability for active process instance")
    void givenExistingProcessDefinition_whenOpenDetailView_thenProcessInstancesSuspendActionAvailable() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval", 2);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstancesTabFragment processInstancesTabContent = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval")
                .openProcessInstancesTab();

        // then
        processInstancesTabContent
                .getSuspendButton()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        processInstancesTabContent.openInstancesGridContextMenu()
                .find(text("Suspend"))
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        processInstancesTabContent.getProcessInstancesGrid().clickSelectAll();

        processInstancesTabContent
                .getSuspendButton()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        processInstancesTabContent.openInstancesGridContextMenu()
                .find(text("Suspend"))
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("List of available actions on process instances tab")
    void givenExistingProcessInstance_whenOpenDetailView_thenAllProcessInstancesActionsAvailable() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstancesTabFragment processInstancesTabContent = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval")
                .openProcessInstancesTab();

        // then
        processInstancesTabContent.getRefreshButton().shouldBe(VISIBLE);
        processInstancesTabContent.getTerminateButton().shouldBe(VISIBLE);
        processInstancesTabContent.getActivateButton().shouldBe(VISIBLE);
        processInstancesTabContent.getSuspendButton().shouldBe(VISIBLE);

        processInstancesTabContent.openInstancesGridContextMenu()
                .shouldHave(visibleItems("Refresh", "Terminate", "Activate", "Suspend"));
    }

    @Test
    @DisplayName("Process instance ID link in data grid opens Process instance detail view")
    void givenExistingProcessInstance_whenInstanceIdLinkClicked_thenProcessInstanceDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstancesTabFragment processInstancesTab = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval")
                .openProcessInstancesTab();

        processInstancesTab.getRowByInstanceId(instanceId)
                .getCellByIndex(ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY).click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/process-instances/" + instanceId));

        $j(ProcessInstanceDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Double-clicking a process instance row opens Process instance detail view")
    void givenExistingProcessInstance_whenRowDoubleClicked_thenProcessInstanceDetailViewOpened() {
        // given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        String instanceId = camundaSampleDataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstancesTabFragment processInstancesTab = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval")
                .openProcessInstancesTab();

        processInstancesTab.getRowByInstanceId(instanceId)
                .getCellByIndex(BUSINESS_KEY_COLUMN_INDEX)
                .getDelegate()
                .doubleClick();

        // then
        webdriver().shouldHave(urlContaining("/bpm/process-instances/" + instanceId));

        $j(ProcessInstanceDetailView.class)
                .exists()
                .displayed();
    }
}
