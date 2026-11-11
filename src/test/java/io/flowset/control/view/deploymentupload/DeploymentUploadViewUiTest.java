/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.deploymentupload;

import io.flowset.control.FlowsetControlApplication;
import io.flowset.control.service.deployment.validation.DeploymentResourceStatus;
import io.flowset.control.service.deployment.validation.ReferenceLocation;
import io.flowset.control.service.deployment.validation.ResolvedReference;
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
import java.util.List;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * UI integration test of the {@link DeploymentUploadView} file checks that do not need a BPM engine: no engine is
 * selected, so the references to the engine cannot be checked.
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
    @DisplayName("Process with its decision and form: references resolved in this deployment, deploy enabled")
    void givenProcessDecisionAndForm_thenAllResolvedAndDeployEnabled() {
        DeploymentUploadView view = openView();

        upload(view, "invoice-approval.bpmn");
        upload(view, "ut-credit-check.dmn");
        upload(view, "ut-invoice-form.form");

        assertThat(view.uploadedFiles).hasSize(3)
                .allSatisfy(file -> assertThat(file.getStatus()).isEqualTo(DeploymentResourceStatus.OK));
        assertThat(view.resolvedReferences)
                .extracting(ResolvedReference::location)
                .containsOnly(ReferenceLocation.THIS_DEPLOYMENT);
        assertThat(view.referencesDataGrid.isVisible()).isTrue();
        assertThat(view.deploymentNameField.getValue()).isEqualTo("invoice-approval");
        assertThat(view.okBtn.isEnabled()).isTrue();
        assertThat(view.deployAttempted).isFalse();
    }

    @Test
    @DisplayName("Process alone: references to the missing decision and form are errors")
    void givenProcessWithoutReferencedResources_thenDeploymentBindingReferencesAreErrors() {
        DeploymentUploadView view = openView();

        upload(view, "invoice-approval.bpmn");

        assertThat(view.uploadedFiles).singleElement()
                .satisfies(file -> assertThat(file.getStatus()).isEqualTo(DeploymentResourceStatus.ERROR));
        assertThat(view.resolvedReferences)
                .extracting(ResolvedReference::location)
                .containsOnly(ReferenceLocation.MISSING);
        assertThat(view.okBtn.isEnabled()).isFalse();

        upload(view, "ut-credit-check.dmn");
        upload(view, "ut-invoice-form.form");

        assertThat(view.uploadedFiles)
                .allSatisfy(file -> assertThat(file.getStatus()).isEqualTo(DeploymentResourceStatus.OK));
        assertThat(view.okBtn.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Not executable process is a warning, invalid XML is an error that blocks the deployment until removed")
    void givenInvalidFiles_thenErrorsBlockDeployUntilRemoved() {
        DeploymentUploadView view = openView();

        upload(view, "ut-not-executable.bpmn");
        upload(view, "ut-garbage.bpmn");

        assertThat(view.uploadedFiles).hasSize(2);
        assertThat(view.uploadedFiles.get(0).getStatus()).isEqualTo(DeploymentResourceStatus.WARNING);
        assertThat(view.uploadedFiles.get(1).getStatus()).isEqualTo(DeploymentResourceStatus.ERROR);
        assertThat(view.uploadedFiles.get(1).getErrors()).isNotEmpty();
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
    @DisplayName("Missing references: deployment binding is an error, latest binding without engine is a warning")
    void givenMissingReferences_thenErrorAndNotCheckedWarning() {
        DeploymentUploadView view = openView();

        upload(view, "ut-missing-refs.bpmn");

        DeploymentUploadView.UploadedFile file = view.uploadedFiles.getFirst();
        assertThat(file.getStatus()).isEqualTo(DeploymentResourceStatus.ERROR);
        assertThat(file.getErrors()).hasSize(1);
        assertThat(file.getWarnings()).hasSize(1);
        assertThat(view.resolvedReferences)
                .extracting(ResolvedReference::location)
                .containsExactlyInAnyOrder(ReferenceLocation.MISSING, ReferenceLocation.NOT_CHECKED);
    }

    @Test
    @DisplayName("File limit: the upload is disabled and extra files are rejected")
    void givenFileLimitReached_thenUploadDisabledAndExtraFileRejected() {
        DeploymentUploadView view = openView();

        upload(view, "invoice-approval.bpmn");
        upload(view, "ut-credit-check.dmn");
        upload(view, "ut-invoice-form.form");

        assertThat(view.filesUpload.isEnabled()).isFalse();

        upload(view, "ut-missing-refs.bpmn");

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
