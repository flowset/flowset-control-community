/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.job;


import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.action.CopyComponentValueToClipboardAction;
import io.flowset.control.action.ViewProcessDefinitionAction;
import io.flowset.control.action.ViewProcessInstanceAction;
import io.flowset.control.action.job.ActivateJobAction;
import io.flowset.control.action.job.RetryJobAction;
import io.flowset.control.action.job.SuspendJobAction;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import io.flowset.control.view.job.column.state.JobStateColumnFragment;
import io.flowset.control.view.util.ComponentHelper;
import io.jmix.core.LoadContext;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.codeeditor.CodeEditor;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.job.JobData;
import io.flowset.control.entity.job.JobDefinitionData;
import io.flowset.control.service.job.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.Objects;

@Route(value = "bpm/job/:id", layout = DefaultMainViewParent.class)
@ViewController("JobData.detail")
@ViewDescriptor("job-data-detail-view.xml")
@DialogMode(minWidth = "40em", width = "80%", maxWidth = "80em")
@EditedEntityContainer("jobDataDc")
public class JobDataDetailView extends StandardDetailView<JobData> {
    @Autowired
    protected Dialogs dialogs;
    @ViewComponent
    protected MessageBundle messageBundle;
    @Autowired
    protected JobService jobService;
    @Autowired
    protected ProcessDefinitionService processDefinitionService;
    @Autowired
    protected ComponentHelper componentHelper;
    @Autowired
    protected UiComponents uiComponents;

    @ViewComponent
    protected CodeEditor stackTraceField;
    @ViewComponent
    protected TypedTextField<String> activityField;
    @ViewComponent
    protected TypedTextField<String> jobTypeField;
    @ViewComponent
    protected CopyComponentValueToClipboardAction copyIdAction;
    @ViewComponent
    protected TypedTextField<String> idField;

    @ViewComponent
    protected TypedTextField<Object> processDefinitionIdField;
    @ViewComponent
    protected JmixButton viewProcessBtn;
    @ViewComponent
    protected JmixButton viewProcessInstanceBtn;

    @ViewComponent
    protected CopyComponentValueToClipboardAction copyExceptionAction;
    @ViewComponent
    protected CopyComponentValueToClipboardAction copyErrorAction;
    @ViewComponent
    protected JmixTextArea exceptionMessageField;

    @ViewComponent
    protected JobStateColumnFragment stateFragment;
    @ViewComponent
    protected ActivateJobAction activateAction;
    @ViewComponent
    protected SuspendJobAction suspendAction;
    @ViewComponent
    protected RetryJobAction retryAction;
    @ViewComponent
    protected ViewProcessDefinitionAction viewProcessAction;
    @ViewComponent
    protected ViewProcessInstanceAction viewProcessInstanceAction;

    @Subscribe
    public void onInit(final InitEvent event) {
        addClassNames(LumoUtility.Padding.Top.XSMALL);

        initActions();
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        String stacktrace = jobService.getErrorDetails(getEditedEntity().getJobId());
        stackTraceField.setValue(stacktrace);

        JobDefinitionData jobDefinition = jobService.findJobDefinition(getEditedEntity().getJobDefinitionId());
        if (jobDefinition != null) {
            activityField.setTypedValue(jobDefinition.getActivityId());
            jobTypeField.setTypedValue(jobDefinition.getJobType());
        }

        configureActions();
        initStateField();
        initProcessFields();
    }

    @Install(to = "jobDataDl", target = Target.DATA_LOADER)
    protected JobData jobDataDlLoadDelegate(final LoadContext<JobData> loadContext) {
        return jobService.findById(Objects.requireNonNull(loadContext.getId()).toString());
    }

    protected void configureActions() {
        JobData jobData = getEditedEntity();
        activateAction.setJobData(jobData);
        activateAction.setAfterSaveHandler(() -> close(StandardOutcome.SAVE));
        suspendAction.setJobData(jobData);
        suspendAction.setAfterSaveHandler(() -> close(StandardOutcome.SAVE));
        retryAction.setJobData(jobData);
        retryAction.setAfterSaveHandler(() -> close(StandardOutcome.SAVE));
    }

    protected void initStateField() {
        stateFragment.setItem(getEditedEntity());
        stateFragment.getStyle().setHeight("min-content");
    }

    protected void initProcessFields() {
        String processLabel = getProcessLabel(getEditedEntity());
        processDefinitionIdField.setTypedValue(processLabel);

        viewProcessAction.setEntityId(getEditedEntity().getProcessDefinitionId());
        viewProcessInstanceAction.setEntityId(getEditedEntity().getProcessInstanceId());
    }

    @Nullable
    protected String getProcessLabel(JobData jobData) {
        if (jobData.getProcessDefinitionId() == null) {
            return null;
        }
        ProcessDefinitionData process = processDefinitionService.getById(jobData.getProcessDefinitionId());
        return componentHelper.getProcessLabel(process);
    }

    protected void initActions() {
        copyIdAction.setText("");
        copyIdAction.setTarget(idField);

        copyExceptionAction.setText("");
        copyExceptionAction.setTarget(stackTraceField);

        copyErrorAction.setText("");
        copyErrorAction.setTarget(exceptionMessageField);
    }
}
