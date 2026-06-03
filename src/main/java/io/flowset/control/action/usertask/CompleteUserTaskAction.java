/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.usertask;

import io.flowset.control.entity.UserTaskData;
import io.flowset.control.security.accesscontext.usertask.UserTaskCompleteAccessContext;
import io.flowset.control.view.taskcomplete.TaskCompleteView;
import io.jmix.core.Messages;
import io.jmix.core.AccessManager;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.ActionType;
import com.vaadin.flow.component.Component;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.view.StandardOutcome;
import org.springframework.beans.factory.annotation.Autowired;
import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(CompleteUserTaskAction.ID)
public class CompleteUserTaskAction extends SecuredBaseAction implements ExecutableAction {

    public static final String ID = "control_completeUserTask";

    protected DialogWindows dialogWindows;
    protected Messages messages;

    protected UserTaskData userTask;
    protected Runnable afterSaveHandler;

    protected boolean visibleByActionUiPermission;

    public CompleteUserTaskAction() {
        super(ID);
    }

    public CompleteUserTaskAction(String id) {
        super(id);
    }

    @Autowired
    protected void setAccessManager(AccessManager accessManager) {
        UserTaskCompleteAccessContext context = new UserTaskCompleteAccessContext();
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
        this.text = messages.getMessage("actions.Complete");
    }

    public void setUserTask(UserTaskData userTask) {
        this.userTask = userTask;
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
        dialogWindows.view(getCurrentView(), TaskCompleteView.class)
                .withViewConfigurer(taskCompleteView -> taskCompleteView.setUserTask(userTask))
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

        setVisibleInternal(visibleExplicitly && visibleByActionUiPermission);
    }

}
