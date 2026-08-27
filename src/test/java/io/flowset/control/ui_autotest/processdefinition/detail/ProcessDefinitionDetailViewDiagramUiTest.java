/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processdefinition.detail;

import com.codeborne.selenide.SelenideElement;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.component.BpmnViewerFragment;
import io.flowset.control.test_support.ui.view.MainView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static io.flowset.control.test_support.ui.component.BpmnViewerFragment.ACTIVE_ELEMENT;
import static io.flowset.control.test_support.ui.component.BpmnViewerFragment.SELECTED_ELEMENT;

@WithRunningExternalEngine
@DisplayName("BPMN diagram viewer in process detail view")
public class ProcessDefinitionDetailViewDiagramUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Interactive mode: no one element is active if no running instances exist")
    void givenExistingActiveProcessDefinition_whenOpenDetailView_thenAllElementsNotActive() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAsAdmin();
        List<String> elementIds = List.of("startEvent", "approveVacationTask", "checkStateGateway", "saveDetailsTask",
                "approvedEndEvent", "notApprovedEndEvent");

        // when
        BpmnViewerFragment viewerFragment = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval")
                .getBpmnViewerFragment();

        // then
        elementIds.forEach(elementId -> {
            SelenideElement processElement = viewerFragment.getProcessDiagramElementById(elementId);

            processElement.hover();

            processElement.shouldNotHave(ACTIVE_ELEMENT);
        });
    }

    @Test
    @DisplayName("Interactive mode: element is active on process detail view if running instances on it exist")
    void givenExistingActiveProcessDefinition_whenOpenDetailView_thenElementIsHovered() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval", 2);

        MainView mainView = loginAsAdmin();

        // when
        BpmnViewerFragment viewerFragment = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval")
                .getBpmnViewerFragment();

        SelenideElement processElement = viewerFragment.getProcessDiagramElementById("approveVacationTask");
        processElement.hover();

        // then
        processElement.shouldHave(ACTIVE_ELEMENT);
    }

    @Test
    @DisplayName("Interactive mode: clicking active process diagram element on process detail view")
    void givenExistingActiveProcessDefinition_whenClickOnDiagramElement_thenElementSelected() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval", 2);

        MainView mainView = loginAsAdmin();

        // when
        BpmnViewerFragment viewerFragment = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval")
                .getBpmnViewerFragment();

        SelenideElement processElement = viewerFragment.getProcessDiagramElementById("approveVacationTask");
        processElement.click();

        // then
        processElement.shouldHave(SELECTED_ELEMENT);

        processElement.click();

        processElement.shouldNotHave(SELECTED_ELEMENT)
                .shouldHave(ACTIVE_ELEMENT);
    }

    @Test
    @DisplayName("Interactive mode: clicking not active process diagram element on process detail view")
    void givenExistingActiveProcessDefinition_whenClickOnDiagramElement_thenElementNotSelected() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        BpmnViewerFragment viewerFragment = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval")
                .getBpmnViewerFragment();

        SelenideElement processElement = viewerFragment.getProcessDiagramElementById("approveVacationTask");
        processElement.click();

        // then
        processElement.shouldNotHave(SELECTED_ELEMENT);
    }

    @Test
    @DisplayName("Interactive mode: element is not active on process detail view if no running instances on it exist")
    void givenNoExistingActiveProcessDefinition_whenOpenDetailView_thenElementIsNotHovered() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval", 2);

        MainView mainView = loginAsAdmin();

        // when
        BpmnViewerFragment viewerFragment = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval")
                .getBpmnViewerFragment();

        SelenideElement processElement = viewerFragment.getProcessDiagramElementById("saveDetailsTask");
        processElement.hover();

        // then
        processElement.shouldNotHave(ACTIVE_ELEMENT);
    }
}
