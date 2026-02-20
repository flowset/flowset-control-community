/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.externaltask;

import com.vaadin.flow.router.Route;
import io.flowset.control.action.CopyComponentValueToClipboardAction;
import io.flowset.control.service.externaltask.ExternalTaskService;
import io.jmix.flowui.kit.component.codeeditor.JmixCodeEditor;
import io.jmix.flowui.view.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "external-task-error-details", layout = DefaultMainViewParent.class)
@ViewController("ExternalTaskErrorDetailsView")
@ViewDescriptor("external-task-error-details-view.xml")
@DialogMode(minWidth = "60em", width = "70%", minHeight = "40em", height = "70%", resizable = true)
public class ExternalTaskErrorDetailsView extends StandardView {
    @Autowired
    protected ExternalTaskService externalTaskService;
    @ViewComponent
    protected JmixCodeEditor errorDetailsCodeEditor;
    @ViewComponent
    protected JmixCodeEditor errorMessageCodeEditor;
    @ViewComponent
    protected CopyComponentValueToClipboardAction copyErrorDetailsAction;
    @ViewComponent
    protected CopyComponentValueToClipboardAction copyErrorMessageAction;

    protected String externalTaskId;
    protected String errorMessage;

    public void setExternalTaskId(String externalTaskId) {
        this.externalTaskId = externalTaskId;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        String stacktrace = externalTaskService.getErrorDetails(externalTaskId);

        errorDetailsCodeEditor.setValue(stacktrace);
        copyErrorDetailsAction.setTarget(errorDetailsCodeEditor);
        copyErrorDetailsAction.setVisible(StringUtils.isNotEmpty(stacktrace));

        errorMessageCodeEditor.setValue(errorMessage);
        copyErrorMessageAction.setTarget(errorMessageCodeEditor);
        copyErrorMessageAction.setVisible(StringUtils.isNotEmpty(errorMessage));
    }
}