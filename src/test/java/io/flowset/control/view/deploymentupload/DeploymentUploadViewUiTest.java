/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.deploymentupload;

import io.flowset.control.FlowsetControlApplication;
import io.flowset.control.entity.deployment.DeploymentResourceStatus;
import io.flowset.control.entity.deployment.DeploymentResourceType;
import io.flowset.control.test_support.FlowsetControlTestConfiguration;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * UI integration test of the {@link DeploymentUploadView} checks that do not need a BPM engine.
 */
@UiTest
@SpringBootTest(classes = {FlowsetControlApplication.class, FlowuiTestAssistConfiguration.class,
        FlowsetControlTestConfiguration.class},
        properties = "flowset.control.ui.deployment-upload-max-files=3")
class DeploymentUploadViewUiTest {

    static final String RESOURCES = "test_support/deployment_upload/";

    @Autowired
    ViewNavigators viewNavigators;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("main.datasource.url", () -> "jdbc:hsqldb:mem:deployment-upload-ui");
        registry.add("main.datasource.username", () -> "sa");
        registry.add("main.datasource.password", () -> "");
        registry.add("main.datasource.driver-class-name", () -> "org.hsqldb.jdbc.JDBCDriver");
    }

    @Test
    @DisplayName("Empty view: nothing to deploy")
    void givenNoFiles_thenDeployDisabled() {
        DeploymentUploadView view = openView();

        assertThat(view.okBtn.isVisible()).isTrue();
        assertThat(view.okBtn.isEnabled()).isFalse();
        assertThat(view.filesUpload.isEnabled()).isTrue();
        assertThat(view.deploymentNameField.getValue()).isEmpty();
    }

    @Test
    @DisplayName("Supported files: types resolved, default deployment name, deploy enabled")
    void givenSupportedFiles_thenDeployEnabled() {
        DeploymentUploadView view = openView();

        upload(view, "invoice-approval.bpmn");
        upload(view, "ut-credit-check.dmn");
        upload(view, "ut-invoice-form.form");

        assertThat(view.uploadedFiles).hasSize(3)
                .allSatisfy(file -> {
                    assertThat(file.getType()).isNotNull();
                    assertThat(file.getStatus()).isEqualTo(DeploymentResourceStatus.OK);
                });
        assertThat(view.deploymentNameField.getValue()).isEqualTo("invoice-approval");
        assertThat(view.okBtn.isEnabled()).isTrue();
        assertThat(view.deployAttempted).isFalse();
    }

    @Test
    @DisplayName("Unknown extension is another resource; an empty file is an error that blocks the deployment until removed")
    void givenOtherAndEmptyFiles_thenEmptyFileBlocksDeployUntilRemoved() {
        DeploymentUploadView view = openView();

        view.addFile("template.ftl", "Dear ${name}".getBytes(StandardCharsets.UTF_8));
        view.addFile("empty.bpmn", new byte[0]);

        assertThat(view.uploadedFiles).hasSize(2);
        assertThat(view.uploadedFiles.get(0).getType()).isEqualTo(DeploymentResourceType.OTHER);
        assertThat(view.uploadedFiles.get(0).getStatus()).isEqualTo(DeploymentResourceStatus.OK);
        assertThat(view.uploadedFiles.get(1).getStatus()).isEqualTo(DeploymentResourceStatus.ERROR);
        assertThat(view.uploadedFiles.get(1).getViewErrors()).hasSize(1);
        assertThat(view.okBtn.isEnabled()).isFalse();

        view.okBtn.click();
        assertThat((Object) UiTestUtils.getCurrentView()).isSameAs(view);

        selectAll(view);
        view.filesDataGrid.getAction("remove").actionPerform(view.filesDataGrid);

        assertThat(view.uploadedFiles).isEmpty();
        assertThat(view.okBtn.isEnabled()).isFalse();
        assertThat(view.deploymentNameField.getValue()).isEmpty();
    }

    @Test
    @DisplayName("Duplicate file name is an error for both files")
    void givenDuplicateFileName_thenBothFilesHaveError() {
        DeploymentUploadView view = openView();

        upload(view, "ut-credit-check.dmn");
        upload(view, "ut-credit-check.dmn");

        assertThat(view.uploadedFiles).hasSize(2)
                .allSatisfy(file -> assertThat(file.getStatus()).isEqualTo(DeploymentResourceStatus.ERROR));
        assertThat(view.okBtn.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("File limit: the upload is disabled and extra files are rejected")
    void givenFileLimitReached_thenUploadDisabledAndExtraFileRejected() {
        DeploymentUploadView view = openView();

        upload(view, "invoice-approval.bpmn");
        upload(view, "ut-credit-check.dmn");
        upload(view, "ut-invoice-form.form");

        assertThat(view.filesUpload.isEnabled()).isFalse();

        upload(view, "ut-engine-reject.bpmn");

        assertThat(view.uploadedFiles).hasSize(3);
    }

    @Test
    @DisplayName("Selection: the last selected file is previewed, removal moves the preview to a remaining file")
    void givenSelection_thenLastSelectedFilePreviewed() {
        DeploymentUploadView view = openView();

        upload(view, "invoice-approval.bpmn");
        upload(view, "ut-credit-check.dmn");
        List<io.jmix.core.entity.KeyValueEntity> items = view.filesDc.getItems();

        view.filesDataGrid.select(items.get(0));
        assertThat(view.previewedFileId).isEqualTo(items.get(0).getId());

        view.filesDataGrid.select(items.get(1));
        assertThat(view.previewedFileId).isEqualTo(items.get(1).getId());

        view.filesDataGrid.deselect(items.get(0));
        view.filesDataGrid.getAction("remove").actionPerform(view.filesDataGrid);

        assertThat(view.uploadedFiles).singleElement()
                .satisfies(file -> assertThat(file.getName()).isEqualTo("invoice-approval.bpmn"));
        assertThat(view.previewedFileId).isNull();
    }

    private DeploymentUploadView openView() {
        viewNavigators.view(getCurrentView(), DeploymentUploadView.class).navigate();
        return UiTestUtils.getCurrentView();
    }

    private void upload(DeploymentUploadView view, String fileName) {
        view.addFile(fileName, read(fileName));
    }

    private void selectAll(DeploymentUploadView view) {
        view.filesDc.getItems().forEach(view.filesDataGrid::select);
    }

    private static byte[] read(String fileName) {
        try {
            return new ClassPathResource(RESOURCES + fileName).getContentAsByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
