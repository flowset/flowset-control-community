/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.externaltask;

import io.flowset.control.entity.ExternalTaskData;
import io.flowset.control.security.accesscontext.externaltask.ExternalTaskRetryAccessContext;
import io.flowset.control.view.incidentdata.RetryExternalTaskView;
import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.list.ListDataComponentAction;
import io.jmix.flowui.view.StandardOutcome;
import org.springframework.beans.factory.annotation.Autowired;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(RetryExternalTaskGridAction.ID)
public class RetryExternalTaskGridAction
        extends ListDataComponentAction<RetryExternalTaskGridAction, ExternalTaskData> {

    public static final String ID = "control_retryGridExternalTask";

    protected DialogWindows dialogWindows;
    protected Runnable afterSaveHandler;

    protected boolean visibleByActionUiPermission;

    public RetryExternalTaskGridAction() {
        super(ID);
    }

    public RetryExternalTaskGridAction(String id) {
        super(id);
    }

    @Autowired
    protected void setAccessManager(AccessManager accessManager) {
        ExternalTaskRetryAccessContext context = new ExternalTaskRetryAccessContext();
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

        ExternalTaskData selectedTask = target.getSingleSelectedItem();
        if (selectedTask == null) {
            return;
        }
        dialogWindows.view(getCurrentView(), RetryExternalTaskView.class)
                .withAfterCloseListener(afterClose -> {
                    if (afterClose.closedWith(StandardOutcome.SAVE) && afterSaveHandler != null) {
                        afterSaveHandler.run();
                    }
                })
                .withViewConfigurer(retryExternalTaskView ->
                        retryExternalTaskView.setExternalTaskId(selectedTask.getExternalTaskId()))
                .build()
                .open();
    }

    @Override
    protected boolean isApplicable() {
        ExternalTaskData selectedTask = target.getSingleSelectedItem();
        return super.isApplicable()
                && selectedTask != null
                && selectedTask.getRetries() != null
                && selectedTask.getRetries() == 0;
    }
}
