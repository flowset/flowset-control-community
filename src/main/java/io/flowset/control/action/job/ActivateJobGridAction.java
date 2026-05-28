/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.job;

import io.flowset.control.entity.job.JobData;
import io.flowset.control.entity.job.JobState;
import io.flowset.control.view.job.ActivateJobView;
import io.flowset.control.security.accesscontext.job.JobActivateAccessContext;
import io.jmix.core.Messages;
import io.jmix.core.AccessManager;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.list.ListDataComponentAction;
import io.jmix.flowui.view.StandardOutcome;
import org.springframework.beans.factory.annotation.Autowired;
import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(ActivateJobGridAction.ID)
public class ActivateJobGridAction extends ListDataComponentAction<ActivateJobGridAction, JobData> {

    public static final String ID = "control_activateGridJob";

    protected DialogWindows dialogWindows;
    protected Runnable afterSaveHandler;

    protected boolean visibleByActionUiPermission;

    public ActivateJobGridAction() {
        super(ID);
    }

    public ActivateJobGridAction(String id) {
        super(id);
    }

    @Autowired
    protected void setAccessManager(AccessManager accessManager) {
        JobActivateAccessContext context = new JobActivateAccessContext();
        accessManager.applyRegisteredConstraints(context);

        visibleByActionUiPermission = context.isPermitted();
    }

    @Autowired
    public void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.Activate");
    }

    public void setAfterSaveHandler(Runnable afterSaveHandler) {
        this.afterSaveHandler = afterSaveHandler;
    }

    @Override
    public void refreshState() {
        super.refreshState();

        setVisibleInternal(visibleExplicitly && visibleByActionUiPermission);
    }
    @Override
    public void execute() {
        checkTarget();
        JobData selectedJob = target.getSingleSelectedItem();
        if (selectedJob == null) {
            return;
        }
        dialogWindows.view(getCurrentView(), ActivateJobView.class)
                .withViewConfigurer(view -> view.setJobId(selectedJob.getJobId()))
                .withAfterCloseListener(afterClose -> {
                    if (afterClose.closedWith(StandardOutcome.SAVE) && afterSaveHandler != null) {
                        afterSaveHandler.run();
                    }
                })
                .build()
                .open();
    }

    @Override
    protected boolean isApplicable() {
        JobData selectedJob = target.getSingleSelectedItem();

        return super.isApplicable()
                && selectedJob != null
                && selectedJob.getState() == JobState.SUSPENDED;
    }
}
