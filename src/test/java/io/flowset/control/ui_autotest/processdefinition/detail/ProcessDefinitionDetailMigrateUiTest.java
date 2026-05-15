/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processdefinition.detail;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.response.ProcessDefinitionDto;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.batch.notification.BatchNotificationContentFragment;
import io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessDefinitionDetailView;
import io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessInstancesTabFragment;
import io.flowset.control.test_support.ui.view.processinstance.ProcessInstanceMigrationDialog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.text;
import static io.flowset.control.test_support.ui.TagNames.NOTIFICATION_CARD;
import static io.flowset.control.test_support.ui.condition.ControlCondition.emptyGrid;
import static io.flowset.control.test_support.ui.condition.ControlCondition.visibleBodyRowCount;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Migrate action on Process definition detail view")
public class ProcessDefinitionDetailMigrateUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Migrate action: process instances tab is updated after confirmation")
    void givenProcessWithRunningInstances_whenMigrateConfirmed_thenProcessInstanceMigrated() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning")
                .deploy("test_support/testVisitPlanningV2.bpmn");
        String sourceDefinitionId = dataManager.getDeployedProcessVersions("visitPlanning").get(0);
        ProcessDefinitionDto sourceDefinition = camundaRestTestHelper.getProcessById(camunda7, sourceDefinitionId);

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByProcess("visitPlanning", sourceDefinition.getVersion().toString());

        ProcessInstancesTabFragment processInstancesTab = detailView.openProcessInstancesTab();
        detailView.getGeneralPanel().getMigrateBtn().click();

        ProcessInstanceMigrationDialog dialog = $j(ProcessInstanceMigrationDialog.class)
                .exists()
                .displayed();
        dialog.getProcessDefinitionVersionComboBox()
                .shouldBe(VISIBLE)
                .setValue("2");
        dialog.getMigrateBtn().click();

        camundaRestTestHelper.waitForBatchExecution(camunda7);
        processInstancesTab.getRefreshButton().click();

        // then
        detailView.getTabs()
                .getTabById("processInstancesTab")
                .getDelegate()
                .shouldHave(text("Process instances (0)"));
        processInstancesTab.getProcessInstancesGrid().shouldBe(emptyGrid);
    }

    @Test
    @DisplayName("Migrate action: process instances tab is not updated after cancellation")
    void givenProcessWithRunningInstances_whenMigrateCancelled_thenProcessInstanceNotMigrated() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning")
                .deploy("test_support/testVisitPlanningV2.bpmn");
        String sourceDefinitionId = dataManager.getDeployedProcessVersions("visitPlanning").get(0);
        ProcessDefinitionDto sourceDefinition = camundaRestTestHelper.getProcessById(camunda7, sourceDefinitionId);

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByProcess("visitPlanning", sourceDefinition.getVersion().toString());
        ProcessInstancesTabFragment processInstancesTab = detailView.openProcessInstancesTab();
        detailView.getGeneralPanel()
                .getMigrateBtn().click();

        ProcessInstanceMigrationDialog dialog = $j(ProcessInstanceMigrationDialog.class)
                .exists()
                .displayed();
        dialog.getProcessDefinitionVersionComboBox()
                .shouldBe(VISIBLE)
                .setValue("2");
        dialog.getCancelBtn().click();

        // then
        detailView.getTabs()
                .getTabById("processInstancesTab")
                .getDelegate()
                .shouldHave(text("Process instances (1)"));
        processInstancesTab.getProcessInstancesGrid().shouldHave(visibleBodyRowCount(1));
    }

    @Test
    @DisplayName("Migrate action: success notification is shown after confirmation")
    void givenProcessWithRunningInstances_whenMigrateConfirmed_thenSuccessNotificationShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning")
                .deploy("test_support/testVisitPlanningV2.bpmn");

        String sourceDefinitionId = dataManager.getDeployedProcessVersions("visitPlanning").get(0);
        ProcessDefinitionDto sourceDefinition = camundaRestTestHelper.getProcessById(camunda7, sourceDefinitionId);

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByProcess("visitPlanning", sourceDefinition.getVersion().toString());

        detailView.getGeneralPanel().getMigrateBtn().click();

        ProcessInstanceMigrationDialog dialog = $j(ProcessInstanceMigrationDialog.class)
                .exists()
                .displayed();
        dialog.getProcessDefinitionVersionComboBox()
                .shouldBe(VISIBLE)
                .setValue("2");
        dialog.getMigrateBtn().click();

        // then
        BatchNotificationContentFragment notification = $j(BatchNotificationContentFragment.class, NOTIFICATION_CARD)
                .exists()
                .displayed();

        notification.getTitleText()
                .shouldBe(VISIBLE)
                .shouldHave(text("Process instances migration started"));
        notification.getBatchDescription()
                .shouldBe(VISIBLE)
                .shouldHave(text("Refresh data or view progress in Batch details"));
    }
}
