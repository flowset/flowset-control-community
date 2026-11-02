/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processdefinition;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.component.BpmnViewerFragment;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processdefinition.NewProcessDeploymentView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("BPMN diagram preview in Process deployment view")
public class NewProcessDeploymentViewUiTest extends AbstractCamunda7UiTest {

    @Test
    @DisplayName("Uploaded BPMN file is shown in diagram preview")
    void givenLoggedInUser_whenUploadBpmnFile_thenDiagramShown() throws FileNotFoundException {
        // given
        File bpmnFile = ResourceUtils.getFile("classpath:test_support/vacationApproval.bpmn");
        List<String> elementIds = List.of("startEvent", "approveVacationTask", "checkStateGateway", "saveDetailsTask",
                "approvedEndEvent", "notApprovedEndEvent");

        MainView mainView = loginAsAdmin();
        mainView.openProcessListView()
                .getDeployBtn()
                .click();

        NewProcessDeploymentView deploymentView = $j(NewProcessDeploymentView.class)
                .exists()
                .displayed();

        // when
        deploymentView.getBpmnXmlUploadField().setValue(bpmnFile);

        // then
        deploymentView.getProcessIdLabel().shouldHave(text("vacation_approval"));

        BpmnViewerFragment viewerFragment = deploymentView.getBpmnViewerFragment();
        elementIds.forEach(elementId -> viewerFragment.getProcessDiagramElementById(elementId)
                .shouldBe(visible));
    }
}
