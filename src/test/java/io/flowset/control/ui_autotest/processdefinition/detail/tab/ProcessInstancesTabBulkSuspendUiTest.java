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
import io.flowset.control.test_support.ui.view.processinstance.action.BulkSuspendProcessInstanceDialog;
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
@DisplayName("Suspend action on Process instances tab in process detail view")
public class ProcessInstancesTabBulkSuspendUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Suspend action: process instance state is updated in grid after confirmation")
    void givenExistingProcessDefinition_whenOpenDetailViewAndSuspendInstances_thenStateUpdated() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstancesTabFragment processInstancesTabContent = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval")
                .openProcessInstancesTab();

        processInstancesTabContent.getProcessInstancesGrid().clickSelectAll();

        processInstancesTabContent
                .getSuspendButton()
                .shouldBe(ENABLED).click();

        $j(BulkSuspendProcessInstanceDialog.class).exists()
                .displayed()
                .getSuspendBtn().click();

        camundaRestTestHelper.waitForBatchExecution(camunda7);
        processInstancesTabContent.getRefreshButton().click();

        // then
        processInstancesTabContent.getProcessInstancesGrid()
                .shouldHave(allBodyRowsHaveCellText(STATE_COLUMN_INDEX, "Suspended"));
    }

    @Test
    @DisplayName("Suspend action: process instance state is not updated in grid after cancellation")
    void givenExistingProcessDefinition_whenOpenDetailViewAndNotSuspendInstances_thenStateNotUpdated() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstancesTabFragment processInstancesTabContent = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval")
                .openProcessInstancesTab();

        processInstancesTabContent.getProcessInstancesGrid().clickSelectAll();

        processInstancesTabContent
                .getSuspendButton()
                .shouldBe(ENABLED).click();

        $j(BulkSuspendProcessInstanceDialog.class).exists()
                .displayed()
                .getCancelBtn().click();

        // then
        processInstancesTabContent.getProcessInstancesGrid()
                .shouldHave(allBodyRowsHaveCellText(STATE_COLUMN_INDEX, "Running"));
    }

    @Test
    @DisplayName("Suspend action disabled when suspended process instances are selected")
    void givenExistingSuspendedProcessInstances_whenOpenDetailView_thenProcessInstancesSuspendActionDisabled() {
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
                .shouldNotBe(ENABLED);

        processInstancesTabContent.openInstancesGridContextMenu()
                .find(text("Suspend"))
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);
    }
}
