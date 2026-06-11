/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.job;

import com.vaadin.flow.component.Component;
import io.flowset.control.entity.job.JobData;
import io.flowset.control.entity.job.JobState;
import io.flowset.control.security.accesscontext.job.JobSuspendAccessContext;
import io.flowset.control.view.job.SuspendJobView;
import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.view.StandardOutcome;
import org.springframework.beans.factory.annotation.Autowired;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(SuspendJobAction.ID)
public class SuspendJobAction extends SecuredBaseAction implements ExecutableAction {

    public static final String ID = "control_suspendJob";

    protected DialogWindows dialogWindows;

    protected JobData jobData;
    protected Runnable afterSaveHandler;

    protected boolean visibleByActionUiPermission;

    public SuspendJobAction() {
        super(ID);
    }

    public SuspendJobAction(String id) {
        super(id);
    }

    @Autowired
    protected void setAccessManager(AccessManager accessManager) {
        JobSuspendAccessContext context = new JobSuspendAccessContext();
        accessManager.applyRegisteredConstraints(context);

        visibleByActionUiPermission = context.isPermitted();
    }

    @Autowired
    public void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.Suspend");
    }

    public void setJobData(JobData jobData) {
        this.jobData = jobData;
        refreshState();
    }

    public void setAfterSaveHandler(Runnable afterSaveHandler) {
        this.afterSaveHandler = afterSaveHandler;
    }

    @Override
    public void actionPerform(Component component) {
        execute();
    }

    @Override
    public void execute() {
        dialogWindows.view(getCurrentView(), SuspendJobView.class)
                .withViewConfigurer(view -> view.setJobId(jobData.getJobId()))
                .withAfterCloseListener(afterCloseEvent -> {
                    if (afterCloseEvent.closedWith(StandardOutcome.SAVE) && afterSaveHandler != null) {
                        afterSaveHandler.run();
                    }
                })
                .open();
    }

    @Override
    public void refreshState() {
        super.refreshState();

        setVisibleInternal(visibleExplicitly && visibleByActionUiPermission && isVisibleByState());
    }

    protected boolean isVisibleByState() {
        return jobData != null
                && jobData.getState() == JobState.ACTIVE;
    }
}
