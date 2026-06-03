/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.externaltask;

import com.vaadin.flow.component.Component;
import io.flowset.control.entity.ExternalTaskData;
import io.flowset.control.security.accesscontext.externaltask.ExternalTaskRetryAccessContext;
import io.flowset.control.view.incidentdata.RetryExternalTaskView;
import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.view.StandardOutcome;
import org.springframework.beans.factory.annotation.Autowired;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(RetryExternalTaskAction.ID)
public class RetryExternalTaskAction extends SecuredBaseAction implements ExecutableAction {

    public static final String ID = "control_retryExternalTask";

    protected DialogWindows dialogWindows;
    protected Messages messages;

    protected ExternalTaskData externalTaskData;
    protected Runnable afterSaveHandler;

    protected boolean visibleByActionUiPermission;

    public RetryExternalTaskAction() {
        super(ID);
    }

    public RetryExternalTaskAction(String id) {
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
        this.messages = messages;
        this.text = messages.getMessage("actions.Retry");
    }

    public void setExternalTaskData(ExternalTaskData externalTaskData) {
        this.externalTaskData = externalTaskData;
        refreshState();
    }

    public void setAfterSaveHandler(Runnable afterSaveHandler) {
        this.afterSaveHandler = afterSaveHandler;
    }

    @Override
    public void refreshState() {
        super.refreshState();

        setVisibleInternal(visibleExplicitly && visibleByActionUiPermission && isVisibleByState());
    }

    @Override
    public void actionPerform(Component component) {
        execute();
    }

    @Override
    public void execute() {
        dialogWindows.view(getCurrentView(), RetryExternalTaskView.class)
                .withViewConfigurer(view ->
                        view.setExternalTaskId(externalTaskData.getExternalTaskId()))
                .withAfterCloseListener(afterCloseEvent -> {
                    if (afterCloseEvent.closedWith(StandardOutcome.SAVE) && afterSaveHandler != null) {
                        afterSaveHandler.run();
                    }
                })
                .open();
    }

    protected boolean isVisibleByState() {
        return externalTaskData != null && externalTaskData.getRetries() != null
                && externalTaskData.getRetries() == 0;
    }
}
