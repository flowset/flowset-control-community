/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.usertask;

import io.flowset.control.entity.UserTaskData;
import io.flowset.control.security.accesscontext.usertask.UserTaskCompleteAccessContext;
import io.flowset.control.view.bulktaskcomplete.BulkTaskCompleteView;
import io.jmix.core.Messages;
import io.jmix.core.AccessManager;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.list.ListDataComponentAction;
import io.jmix.flowui.view.StandardOutcome;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(BulkCompleteUserTaskAction.ID)
public class BulkCompleteUserTaskAction extends ListDataComponentAction<BulkCompleteUserTaskAction, UserTaskData> {

    public static final String ID = "control_bulkCompleteUserTask";

    protected DialogWindows dialogWindows;
    protected Runnable afterSaveHandler;
    protected boolean visibleByActionUiPermission;

    public BulkCompleteUserTaskAction() {
        super(ID);
    }

    public BulkCompleteUserTaskAction(String id) {
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
        this.text = messages.getMessage("actions.Complete");
    }

    public void setAfterSaveHandler(Runnable afterSaveHandler) {
        this.afterSaveHandler = afterSaveHandler;
    }

    @Override
    public void execute() {
        checkTarget();

        Set<UserTaskData> selectedItems = target.getSelectedItems();
        dialogWindows.view(getCurrentView(), BulkTaskCompleteView.class)
                .withViewConfigurer(bulkTaskCompleteView -> bulkTaskCompleteView.setUserTasks(selectedItems))
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
                && target.getSelectedItems().stream().noneMatch(userTaskData -> BooleanUtils.isTrue(userTaskData.getSuspended()));
    }
}
