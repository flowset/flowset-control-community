/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.job;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.kit.component.codeeditor.JmixCodeEditor;
import io.jmix.flowui.view.*;
import io.flowset.control.action.CopyComponentValueToClipboardAction;
import io.flowset.control.service.job.JobService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "job-error-details", layout = DefaultMainViewParent.class)
@ViewController("JobErrorDetailsView")
@ViewDescriptor("job-error-details-view.xml")
@DialogMode(minWidth = "60em", width = "70%", minHeight = "40em", height = "70%", resizable = true)
public class JobErrorDetailsView extends StandardView {
    @Autowired
    protected JobService jobService;

    @ViewComponent
    protected JmixCodeEditor errorDetailsCodeEditor;
    @ViewComponent
    protected JmixCodeEditor errorMessageCodeEditor;
    @ViewComponent
    protected CopyComponentValueToClipboardAction copyErrorDetailsAction;
    @ViewComponent
    protected CopyComponentValueToClipboardAction copyErrorMessageAction;

    protected String jobId;
    protected String errorMessage;

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        String errorDetails = jobService.getErrorDetails(jobId);

        errorDetailsCodeEditor.setValue(errorDetails);
        copyErrorDetailsAction.setTarget(errorDetailsCodeEditor);
        copyErrorDetailsAction.setVisible(StringUtils.isNotEmpty(errorDetails));

        errorMessageCodeEditor.setValue(errorMessage);
        copyErrorMessageAction.setTarget(errorMessageCodeEditor);
        copyErrorMessageAction.setVisible(StringUtils.isNotEmpty(errorMessage));
    }
}