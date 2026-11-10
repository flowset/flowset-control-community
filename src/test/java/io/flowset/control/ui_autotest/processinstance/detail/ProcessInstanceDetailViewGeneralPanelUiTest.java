/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.detail;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processinstance.detail.ProcessInstanceDetailView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static io.jmix.masquerade.JConditions.CHECKED;
import static io.jmix.masquerade.JConditions.VISIBLE;

@WithRunningExternalEngine
@DisplayName("General panel of Process instance detail view")
public class ProcessInstanceDetailViewGeneralPanelUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Externally terminated field is hidden for active instance")
    void givenActiveProcessInstance_whenOpenDetailView_thenExternallyTerminatedFieldHidden() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        detailView.getGeneralPanel().getInfoBtn().click();

        // then
        detailView.getGeneralPanel()
                .getPropertiesPanel()
                .getExternallyTerminatedField()
                .shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Externally terminated field is visible and unchecked for completed instance")
    void givenCompletedProcessInstance_whenOpenDetailView_thenExternallyTerminatedFieldVisible() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testCompletedInstance.bpmn")
                .startByKey("testCompletedInstance")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testCompletedInstance").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .switchToAllViewMode()
                .openDetailViewByInstanceId(instanceId);

        detailView.getGeneralPanel().getInfoBtn().click();

        // then
        detailView.getGeneralPanel()
                .getPropertiesPanel()
                .getExternallyTerminatedField()
                .shouldBe(VISIBLE)
                .shouldNotBe(CHECKED);
    }
}
