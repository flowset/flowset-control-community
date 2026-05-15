/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.detail;

import com.codeborne.selenide.SelenideElement;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.component.BpmnViewerFragment;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processinstance.detail.ProcessInstanceDetailView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.*;
import static io.flowset.control.test_support.ui.component.BpmnViewerFragment.INCIDENT_COUNT_OVERLAY;

@WithRunningExternalEngine
@DisplayName("BPMN diagram in Process instance detail view")
public class ProcessInstanceDiagramUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Incident count overlay is visible if incidents exist")
    void givenProcessInstanceWithIncident_whenOpenDetailView_thenIncidentCountOverlayVisible() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        BpmnViewerFragment viewerFragment = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .getBpmnViewerFragment();

        // then
        SelenideElement incidentOverlay = viewerFragment.getIncidentCountOverlay("throwsExceptionTask")
                .shouldBe(exist)
                .shouldBe(visible);

        incidentOverlay.find(INCIDENT_COUNT_OVERLAY)
                .shouldHave(text("1"))
                .shouldHave(attribute("title", "Incidents: 1"));
    }

    @Test
    @DisplayName("Incident count overlay is hidden if incidents do not exist")
    void givenProcessInstanceWithoutIncident_whenOpenDetailView_thenIncidentCountOverlayNotExist() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        // then
        detailView.getBpmnViewerFragment()
                .getIncidentCountOverlay("approveVacationTask")
                .shouldNot(exist);
    }
}
