/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view;

import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewComponent;
import io.flowset.control.entity.deployment.ResourceDeploymentReport;
import io.flowset.control.entity.deployment.ResourceValidationError;
import io.flowset.control.entity.deployment.ValidationErrorType;
import io.flowset.control.restsupport.camunda.ResourceReport;
import io.flowset.control.view.deploymenterror.DeploymentErrorDialogView;
import io.flowset.control.view.deploymenterror.ResourceDeploymentReportFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public abstract class AbstractResourceDeploymentView extends StandardView {
    @ViewComponent
    protected InstanceContainer<ResourceDeploymentReport> deploymentReportDc;

    @Autowired
    protected Messages messages;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected DialogWindows dialogWindows;

    @Autowired
    protected ResourceDeploymentReportFactory resourceDeploymentReportFactory;

    @ViewComponent
    protected JmixButton errorsBtn;


    protected void initDeploymentErrorsButton() {
        errorsBtn.addClickListener(event -> openValidationErrorDialogsView());
    }

    protected void handleResourceReport(ResourceReport resourceReport, String uploadedFileName) {
        if (resourceReport != null) {
            ResourceDeploymentReport deploymentReport = createResourceReport(resourceReport, uploadedFileName);

            deploymentReportDc.setItem(deploymentReport);
        }

        openValidationErrorDialogsView();

        updateErrorButtonText();
    }

    protected void updateErrorButtonText() {
        ResourceDeploymentReport report = deploymentReportDc.getItem();
        int size = CollectionUtils.size(report.getValidationErrors());
        errorsBtn.setVisible(true);

        String sizeText = size > 99 ? "99+" : String.valueOf(size);
        String errorBtnText = messages.formatMessage(AbstractResourceDeploymentView.class, "deploymentErrorsBtn.text", sizeText);
        errorsBtn.setText(errorBtnText);
    }


    protected ResourceDeploymentReport createResourceReport(ResourceReport report, String uploadedFileName) {
        return resourceDeploymentReportFactory.createReport(report, uploadedFileName);
    }

    protected void openValidationErrorDialogsView() {
        dialogWindows.view(this, DeploymentErrorDialogView.class)
                .withViewConfigurer(deploymentErrorDialogView -> {
                    deploymentErrorDialogView.setResourceReport(deploymentReportDc.getItem());
                })
                .open();
    }

    protected void addValidationErrors(List<ResourceReport.ProblemDetails> problemDetailsList, ValidationErrorType warning, List<ResourceValidationError> result) {
        resourceDeploymentReportFactory.addValidationErrors(problemDetailsList, warning, result);
    }

    protected ResourceValidationError createValidationError(ValidationErrorType error, ResourceReport.ProblemDetails problemDetails) {
        return resourceDeploymentReportFactory.createValidationError(error, problemDetails);
    }
}
