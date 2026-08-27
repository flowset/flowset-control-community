/*
 * Copyright (c) Haulmont 1016. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processdefinition.detail.tab;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessInstancesTabFragment;
import io.flowset.control.test_support.ui.view.processinstance.action.BulkActivateProcessInstanceDialog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.text;
import static io.flowset.control.test_support.ui.condition.ControlCondition.allBodyRowsHaveCellText;
import static io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessInstancesTabFragment.STATE_COLUMN_INDEX;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Activate action on Process instances tab in process detail view")
public class ProcessInstancesTabBulkActivateUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Activate action: process instance state is updated in grid after confirmation")
    void givenExistingProcessDefinition_whenOpenDetailViewAndActivateInstances_thenStateUpdated() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .suspendByKey("vacation_approval", true);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstancesTabFragment processInstancesTabContent = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval")
                .openProcessInstancesTab();

        processInstancesTabContent.getProcessInstancesGrid().clickSelectAll();

        processInstancesTabContent
                .getActivateButton()
                .shouldBe(ENABLED).click();

        $j(BulkActivateProcessInstanceDialog.class).exists()
                .displayed()
                .getActivateBtn().click();

        camundaRestTestHelper.waitForBatchExecution(camunda7);
        processInstancesTabContent.getRefreshButton().click();

        // then
        processInstancesTabContent.getProcessInstancesGrid()
                .shouldHave(allBodyRowsHaveCellText(STATE_COLUMN_INDEX, "Running"));
    }

    @Test
    @DisplayName("Activate action: process instance state is not updated in grid after cancellation")
    void givenExistingProcessDefinition_whenOpenDetailViewAndNotActivateInstances_thenStateNotUpdated() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .suspendByKey("vacation_approval", true);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstancesTabFragment processInstancesTabContent = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval")
                .openProcessInstancesTab();

        processInstancesTabContent.getProcessInstancesGrid().clickSelectAll();

        processInstancesTabContent
                .getActivateButton()
                .shouldBe(ENABLED).click();

        $j(BulkActivateProcessInstanceDialog.class).exists()
                .displayed()
                .getCancelBtn().click();

        // then
        processInstancesTabContent.getProcessInstancesGrid()
                .shouldHave(allBodyRowsHaveCellText(STATE_COLUMN_INDEX, "Suspended"));
    }

    @Test
    @DisplayName("Activate action availability on process instances tab")
    void givenExistingSuspendedProcessInstances_whenOpenDetailView_thenProcessInstancesActivateActionAvailable() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .suspendByKey("vacation_approval", true);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstancesTabFragment processInstancesTabContent = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval")
                .openProcessInstancesTab();

        // then
        processInstancesTabContent
                .getActivateButton()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        processInstancesTabContent.openInstancesGridContextMenu()
                .find(text("Activate"))
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        processInstancesTabContent.getProcessInstancesGrid().clickSelectAll();

        processInstancesTabContent
                .getActivateButton()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        processInstancesTabContent.openInstancesGridContextMenu()
                .find(text("Activate"))
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Activate action disabled when active process instances are selected")
    void givenExistingActiveProcessInstances_whenOpenDetailView_thenProcessInstancesActivateActionDisabled() {
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
                .getActivateButton()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        processInstancesTabContent.openInstancesGridContextMenu()
                .find(text("Activate"))
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        processInstancesTabContent.getProcessInstancesGrid().clickSelectAll();

        processInstancesTabContent
                .getActivateButton()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        processInstancesTabContent.openInstancesGridContextMenu()
                .find(text("Activate"))
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);
    }
}
