/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.usertask;

import io.flowset.control.entity.UserTaskData;
import io.flowset.control.security.accesscontext.usertask.UserTaskReassignAccessContext;
import io.flowset.control.view.taskreassign.TaskReassignView;
import io.jmix.core.Messages;
import io.jmix.core.AccessManager;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.ActionType;
import com.vaadin.flow.component.Component;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.view.StandardOutcome;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(ReassignUserTaskAction.ID)
public class ReassignUserTaskAction extends SecuredBaseAction implements ExecutableAction {

    public static final String ID = "control_reassignUserTask";

    protected DialogWindows dialogWindows;
    protected Messages messages;

    protected UserTaskData userTask;
    protected Runnable afterSaveHandler;

    protected boolean visibleByActionUiPermission;

    public ReassignUserTaskAction() {
        super(ID);
    }

    public ReassignUserTaskAction(String id) {
        super(id);
    }

    @Autowired
    protected void setAccessManager(AccessManager accessManager) {
        UserTaskReassignAccessContext context = new UserTaskReassignAccessContext();
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
        this.text = messages.getMessage("actions.Reassign");
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
        dialogWindows.view(getCurrentView(), TaskReassignView.class)
                .withViewConfigurer(taskReassignView -> taskReassignView.setTaskDataList(Collections.singletonList(userTask)))
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE) && afterSaveHandler != null) {
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
