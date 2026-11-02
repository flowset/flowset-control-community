/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processdefinition.detail;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessDefinitionDetailView;
import io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessPropertiesPanelFragment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.exactValue;
import static com.codeborne.selenide.Condition.value;
import static io.jmix.masquerade.JConditions.CHECKED;
import static io.jmix.masquerade.JConditions.VISIBLE;

@WithRunningExternalEngine
@DisplayName("General panel of Process detail view")
public class ProcessDefinitionDetailViewGeneralPanelUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Version tag field shows version tag of the process")
    void givenExistingProcessDefinitionWithVersionTag_whenOpenDetailView_thenVersionTagShown() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn");
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("visitPlanning");

        // then
        detailView.getVersionTagField()
                .shouldBe(VISIBLE)
                .shouldHave(value("v1"));
    }

    @Test
    @DisplayName("Startable in task list checkbox is checked for process startable in task list")
    void givenExistingProcessDefinition_whenOpenDetailView_thenStartableInTaskListChecked() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");

        // then
        detailView.getGeneralPanel()
                .getPropertiesPanel()
                .getStartableInTaskListField()
                .shouldBe(VISIBLE)
                .shouldBe(CHECKED);
    }

    @Test
    @DisplayName("Startable in task list checkbox is unchecked for process not startable in task list")
    void givenNotStartableProcessDefinition_whenOpenDetailView_thenStartableInTaskListUnchecked() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/supportRequest.bpmn");
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("supportRequest");

        // then
        detailView.getGeneralPanel()
                .getPropertiesPanel()
                .getStartableInTaskListField()
                .shouldBe(VISIBLE)
                .shouldNotBe(CHECKED);
    }

    @Test
    @DisplayName("Deployment group shows deployment id, time and source of the process deployment")
    void givenExistingProcessDefinitionWithDeploymentSource_whenOpenDetailView_thenDeploymentDataShown() {
        // given
        String deploymentId = camundaRestTestHelper.createDeploymentWithSource(camunda7,
                        "test_support/vacationApproval.bpmn", "test-source")
                .getId();
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");

        // then
        ProcessPropertiesPanelFragment propertiesPanel = detailView.getGeneralPanel().getPropertiesPanel();
        propertiesPanel.getDeploymentIdField()
                .shouldHave(value(deploymentId));
        propertiesPanel.getDeploymentTimeField()
                .shouldBe(VISIBLE)
                .shouldNotHave(exactValue(""));
        propertiesPanel.getDeploymentSourceField()
                .shouldHave(value("test-source"));
    }
}
