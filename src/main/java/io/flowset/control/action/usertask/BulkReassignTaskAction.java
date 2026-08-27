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
import io.jmix.flowui.action.list.ListDataComponentAction;
import io.jmix.flowui.view.StandardOutcome;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(BulkReassignTaskAction.ID)
public class BulkReassignTaskAction extends ListDataComponentAction<BulkReassignTaskAction, UserTaskData> {

    public static final String ID = "control_bulkReassignUserTask";

    protected DialogWindows dialogWindows;
    protected Runnable afterSaveHandler;

    protected boolean visibleByActionUiPermission;

    public BulkReassignTaskAction() {
        super(ID);
    }

    public BulkReassignTaskAction(String id) {
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
        this.text = messages.getMessage("actions.Reassign");
    }

    public void setAfterSaveHandler(Runnable afterSaveHandler) {
        this.afterSaveHandler = afterSaveHandler;
    }


    @Override
    public void execute() {
        checkTarget();
        dialogWindows.view(getCurrentView(), TaskReassignView.class)
                .withViewConfigurer(view -> view.setTaskDataList(target.getSelectedItems()))
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE) && afterSaveHandler != null) {
                        afterSaveHandler.run();
                    }
                })
                .build()
                .open();
    }

    @Override
    public void refreshState() {
        super.refreshState();

        setVisibleInternal(visibleExplicitly && visibleByActionUiPermission);
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable()
                && !target.getSelectedItems().isEmpty()
                && target.getSelectedItems().stream().noneMatch(t -> BooleanUtils.isTrue(t.getSuspended()));
    }
}
