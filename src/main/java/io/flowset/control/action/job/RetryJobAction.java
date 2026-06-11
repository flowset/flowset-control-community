/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.job;

import com.vaadin.flow.component.Component;
import io.flowset.control.entity.job.JobData;
import io.flowset.control.view.incidentdata.RetryJobView;
import io.flowset.control.security.accesscontext.job.JobRetryAccessContext;
import io.jmix.core.Messages;
import io.jmix.core.AccessManager;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.view.StandardOutcome;
import org.springframework.beans.factory.annotation.Autowired;
import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(RetryJobAction.ID)
public class RetryJobAction extends SecuredBaseAction implements ExecutableAction {

    public static final String ID = "control_retryJob";

    protected DialogWindows dialogWindows;

    protected JobData jobData;
    protected Runnable afterSaveHandler;

    protected boolean visibleByActionUiPermission;

    public RetryJobAction() {
        super(ID);
    }

    public RetryJobAction(String id) {
        super(id);
    }

    @Autowired
    protected void setAccessManager(AccessManager accessManager) {
        JobRetryAccessContext context = new JobRetryAccessContext();
        accessManager.applyRegisteredConstraints(context);
        visibleByActionUiPermission = context.isPermitted();
    }

    @Autowired
    public void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.Retry");
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
        dialogWindows.view(getCurrentView(), RetryJobView.class)
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
                && jobData.getRetries() != null
                && jobData.getRetries() == 0;
    }
}
