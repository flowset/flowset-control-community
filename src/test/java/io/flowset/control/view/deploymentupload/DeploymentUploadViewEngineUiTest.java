/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.deploymentupload;

import io.flowset.control.FlowsetControlApplication;
import io.flowset.control.service.deployment.validation.DeploymentResourceStatus;
import io.flowset.control.test_support.AbstractIntegrationTest;
import io.flowset.control.test_support.FlowsetControlTestConfiguration;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.view.deploymentdata.DeploymentDetailView;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.UncheckedIOException;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * UI integration test of the {@link DeploymentUploadView} deployment to a running BPM engine.
 */
@UiTest
@SpringBootTest(classes = {FlowsetControlApplication.class, FlowuiTestAssistConfiguration.class,
        FlowsetControlTestConfiguration.class})
@WithRunningEngine
class DeploymentUploadViewEngineUiTest extends AbstractIntegrationTest {

    static final String RESOURCES = "test_support/deployment_upload/";

    @Autowired
    ViewNavigators viewNavigators;

    @Test
    @DisplayName("Valid resources are deployed as one deployment and its details are opened")
    void givenValidResources_whenDeploy_thenDeploymentDetailsOpened() {
        DeploymentUploadView view = openView();
        upload(view, "invoice-approval.bpmn");
        upload(view, "ut-credit-check.dmn");
        upload(view, "ut-invoice-form.form");
        view.deploymentNameField.setValue("ui-test-invoice");

        view.okBtn.click();

        assertThat((Object) UiTestUtils.getCurrentView()).isInstanceOf(DeploymentDetailView.class);
    }

    @Test
    @DisplayName("Engine rejection: nothing is deployed, the rejected file gets the engine report, the rest are OK")
    void givenFileRejectedByEngine_whenDeploy_thenViewStaysWithEngineReport() {
        DeploymentUploadView view = openView();
        upload(view, "invoice-approval.bpmn");
        upload(view, "ut-credit-check.dmn");
        upload(view, "ut-invoice-form.form");
        upload(view, "ut-engine-reject.bpmn");
        assertThat(view.okBtn.isEnabled()).isTrue();

        view.okBtn.click();

        assertThat((Object) UiTestUtils.getCurrentView()).isSameAs(view);
        assertThat(view.deployAttempted).isTrue();
        assertThat(view.okBtn.isEnabled()).isFalse();
        assertThat(view.uploadedFiles).hasSize(4);
        DeploymentUploadView.UploadedFile rejected = view.uploadedFiles.get(3);
        assertThat(rejected.getStatus()).isEqualTo(DeploymentResourceStatus.ERROR);
        assertThat(rejected.getEngineReport()).isNotNull();
        assertThat(rejected.getEngineErrors()).isNotEmpty();
        assertThat(view.uploadedFiles.subList(0, 3))
                .allSatisfy(file -> {
                    assertThat(file.getStatus()).isEqualTo(DeploymentResourceStatus.OK);
                    assertThat(file.getEngineReport()).isNull();
                });

        view.filesDataGrid.select(view.filesDc.getItems().get(3));
        view.filesDataGrid.getAction("remove").actionPerform(view.filesDataGrid);

        assertThat(view.uploadedFiles).hasSize(3);
        assertThat(view.deployAttempted).isFalse();
        assertThat(view.okBtn.isEnabled()).isTrue();
    }

    private DeploymentUploadView openView() {
        viewNavigators.view(getCurrentView(), DeploymentUploadView.class).navigate();
        return UiTestUtils.getCurrentView();
    }

    private void upload(DeploymentUploadView view, String fileName) {
        try {
            view.addFile(fileName, new ClassPathResource(RESOURCES + fileName).getContentAsByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
