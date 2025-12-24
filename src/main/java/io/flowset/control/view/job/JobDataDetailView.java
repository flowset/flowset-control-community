/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.job;


import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.action.CopyComponentValueToClipboardAction;
import io.flowset.control.entity.job.JobState;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import io.flowset.control.view.incidentdata.RetryJobView;
import io.flowset.control.view.job.column.state.JobStateColumnFragment;
import io.flowset.control.view.processdefinition.ProcessDefinitionDetailView;
import io.flowset.control.view.processinstance.ProcessInstanceDetailView;
import io.flowset.control.view.util.ComponentHelper;
import io.jmix.core.LoadContext;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.codeeditor.CodeEditor;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.job.JobData;
import io.flowset.control.entity.job.JobDefinitionData;
import io.flowset.control.service.job.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

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
    protected ViewNavigators viewNavigators;
    @Autowired
    protected UiComponents uiComponents;

    @ViewComponent
    protected CodeEditor stackTraceField;
    @ViewComponent
    protected TypedTextField<String> activityField;
    @ViewComponent
    protected TypedTextField<String> jobTypeField;
    @Autowired
    protected Messages messages;
    @Autowired
    protected DialogWindows dialogWindows;
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
    protected BaseAction activateAction;
    @ViewComponent
    protected BaseAction suspendAction;
    @ViewComponent
    protected BaseAction retryAction;

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
            activityField.setValue(jobDefinition.getActivityId());
            jobTypeField.setValue(jobDefinition.getJobType());
        }

        updateActionsVisibility();
        initStateField();
        initProcessFields();
    }

    @Subscribe("suspendAction")
    public void onSuspend(final ActionPerformedEvent event) {
        dialogWindows.view(this, SuspendJobView.class)
                .withViewConfigurer(suspendJobView -> {
                    suspendJobView.setJobId(getEditedEntity().getJobId());
                })
                .withAfterCloseListener(afterCloseEvent -> {
                    if (afterCloseEvent.closedWith(StandardOutcome.SAVE)) {
                        close(StandardOutcome.SAVE);
                    }
                })
                .open();
    }
    @Subscribe("activateAction")
    public void onActivate(final ActionPerformedEvent event) {
        dialogWindows.view(this, ActivateJobView.class)
                .withViewConfigurer(activateJobView -> {
                    activateJobView.setJobId(getEditedEntity().getJobId());
                })
                .withAfterCloseListener(afterCloseEvent -> {
                    if (afterCloseEvent.closedWith(StandardOutcome.SAVE)) {
                        close(StandardOutcome.SAVE);
                    }
                })
                .open();
    }

    @Subscribe("viewProcessAction")
    public void onViewProcess(final ActionPerformedEvent event) {
        openView(ProcessDefinitionDetailView.class, new RouteParameters("id", getEditedEntity().getProcessDefinitionId()));
    }

    @Subscribe("viewProcessInstanceAction")
    public void onViewProcessInstance(final ActionPerformedEvent event) {
        openView(ProcessInstanceDetailView.class, new RouteParameters("id", getEditedEntity().getProcessInstanceId()));
    }

    @Subscribe("retryAction")
    public void onRetryAction(final ActionPerformedEvent event) {
        dialogWindows.view(this, RetryJobView.class)
                .withViewConfigurer(retryJobView -> {
                    retryJobView.setJobId(getEditedEntity().getJobId());
                })
                .withAfterCloseListener(afterCloseEvent -> {
                    if (afterCloseEvent.closedWith(StandardOutcome.SAVE)) {
                        close(StandardOutcome.SAVE);
                    }
                })
                .open();
    }

    @Install(to = "jobDataDl", target = Target.DATA_LOADER)
    protected JobData jobDataDlLoadDelegate(final LoadContext<JobData> loadContext) {
        return jobService.findById(loadContext.getId().toString());
    }

    protected void openView(Class<? extends StandardView> viewClass, RouteParameters routeParameters) {
        boolean isOpenedInDialog = UiComponentUtils.isComponentAttachedToDialog(this);
        if (!isOpenedInDialog) {
            viewNavigators.view(this, viewClass)
                    .withRouteParameters(routeParameters)
                    .withBackwardNavigation(false)
                    .navigate();
        } else {
            RouterLink routerLink = new RouterLink(viewClass, routeParameters);
            getUI().ifPresent(ui -> ui.getPage().open(routerLink.getHref()));
        }
    }

    protected void updateActionsVisibility() {
        if (getEditedEntity().getRetries() != null && getEditedEntity().getRetries() == 0) {
            retryAction.setVisible(true);
        }

        JobState state = getEditedEntity().getState();
        activateAction.setVisible(state == JobState.SUSPENDED);
        suspendAction.setVisible(state == JobState.ACTIVE);
    }

    protected void initStateField() {
        stateFragment.setItem(getEditedEntity());
        stateFragment.getStyle().setHeight("min-content");
    }

    protected void initProcessFields() {
        String processLabel = getProcessLabel(getEditedEntity());
        processDefinitionIdField.setTypedValue(processLabel);

        viewProcessBtn.setVisible(getEditedEntity().getProcessDefinitionId() != null);
        viewProcessInstanceBtn.setVisible(getEditedEntity().getProcessInstanceId() != null);
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