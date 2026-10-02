/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.deploymenterror;

import io.flowset.control.entity.deployment.ResourceDeploymentReport;
import io.flowset.control.entity.deployment.ResourceValidationError;
import io.flowset.control.entity.deployment.ValidationErrorType;
import io.flowset.control.restsupport.camunda.ResourceReport;
import io.jmix.core.DataManager;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates the {@link ResourceDeploymentReport} shown in the {@link DeploymentErrorDialogView} from a resource report
 * returned by the BPM engine when it rejects a deployment.
 */
@Component("control_ResourceDeploymentReportFactory")
public class ResourceDeploymentReportFactory {

    protected final DataManager dataManager;

    public ResourceDeploymentReportFactory(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Creates a deployment report of a single resource: the engine errors followed by the engine warnings.
     *
     * @param report   resource report returned by the engine
     * @param fileName resource name
     * @return created deployment report
     */
    public ResourceDeploymentReport createReport(ResourceReport report, String fileName) {
        ResourceDeploymentReport deploymentReport = dataManager.create(ResourceDeploymentReport.class);
        deploymentReport.setFilename(fileName);

        List<ResourceValidationError> errors = new ArrayList<>();
        addValidationErrors(report.getErrors(), ValidationErrorType.ERROR, errors);
        addValidationErrors(report.getWarnings(), ValidationErrorType.WARNING, errors);

        deploymentReport.setValidationErrors(errors);

        return deploymentReport;
    }

    /**
     * Converts the engine problems to validation errors of the given type and adds them to the result.
     *
     * @param problemDetailsList engine problems, may be {@code null}
     * @param type               type of the created validation errors
     * @param result             list to add the created validation errors to
     */
    public void addValidationErrors(@Nullable List<ResourceReport.ProblemDetails> problemDetailsList,
                                    ValidationErrorType type,
                                    List<ResourceValidationError> result) {
        if (problemDetailsList != null) {
            problemDetailsList.forEach(problemDetails -> result.add(createValidationError(type, problemDetails)));
        }
    }

    /**
     * Converts a single engine problem to a validation error.
     *
     * @param type           type of the created validation error
     * @param problemDetails engine problem
     * @return created validation error
     */
    public ResourceValidationError createValidationError(ValidationErrorType type,
                                                         ResourceReport.ProblemDetails problemDetails) {
        ResourceValidationError resourceValidationError = dataManager.create(ResourceValidationError.class);
        resourceValidationError.setType(type);

        resourceValidationError.setMessage(problemDetails.getMessage());
        resourceValidationError.setColumn(problemDetails.getColumn());
        resourceValidationError.setMainElementId(problemDetails.getMainElementId());
        resourceValidationError.setLine(problemDetails.getLine());

        return resourceValidationError;
    }
}
