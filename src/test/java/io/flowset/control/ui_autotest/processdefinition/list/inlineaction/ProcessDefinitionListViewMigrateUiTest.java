/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processdefinition.list.inlineaction;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.response.ProcessDefinitionDto;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.batch.notification.BatchNotificationContentFragment;
import io.flowset.control.test_support.ui.view.processdefinition.ProcessDefinitionListView;
import io.flowset.control.test_support.ui.view.processinstance.ProcessInstanceMigrationDialog;
import io.jmix.masquerade.component.DataGrid;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.text;
import static io.flowset.control.test_support.ui.TagNames.NOTIFICATION_CARD;
import static io.jmix.masquerade.JConditions.EXIST;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Migrate action on Process definition detail view")
public class ProcessDefinitionListViewMigrateUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;


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
        ProcessDefinitionListView listView = mainView.openProcessListView();

        DataGrid.Row processRow = listView.getRowByProcess("visitPlanning", sourceDefinition.getVersion().toString());

        listView.openOtherActions(processRow)
                .find(text("Migrate")).click();

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

    @Test
    @DisplayName("Migrate action: success notification is not shown after cancellation")
    void givenProcessWithRunningInstances_whenMigrateCancelled_thenSuccessNotificationNotShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning")
                .deploy("test_support/testVisitPlanningV2.bpmn");

        String sourceDefinitionId = dataManager.getDeployedProcessVersions("visitPlanning").get(0);
        ProcessDefinitionDto sourceDefinition = camundaRestTestHelper.getProcessById(camunda7, sourceDefinitionId);

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();
        DataGrid.Row processRow = listView.getRowByProcess("visitPlanning", sourceDefinition.getVersion().toString());

        listView.openOtherActions(processRow)
                .find(text("Migrate")).click();

        ProcessInstanceMigrationDialog dialog = $j(ProcessInstanceMigrationDialog.class)
                .exists()
                .displayed();
        dialog.getProcessDefinitionVersionComboBox()
                .shouldBe(VISIBLE)
                .setValue("2");
        dialog.getCancelBtn().click();

        // then
        $j(BatchNotificationContentFragment.class, NOTIFICATION_CARD)
                .shouldNotBe(EXIST);

    }
}
