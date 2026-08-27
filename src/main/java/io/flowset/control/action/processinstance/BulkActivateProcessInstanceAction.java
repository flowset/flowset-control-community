/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.processinstance;

import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.security.accesscontext.processinstance.ProcessInstanceActivateAccessContext;
import io.flowset.control.view.processinstance.BulkActivateProcessInstanceView;
import io.jmix.core.Messages;
import io.jmix.core.AccessManager;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.list.ListDataComponentAction;
import io.jmix.flowui.view.StandardOutcome;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(BulkActivateProcessInstanceAction.ID)
public class BulkActivateProcessInstanceAction
        extends ListDataComponentAction<BulkActivateProcessInstanceAction, ProcessInstanceData> {

    public static final String ID = "control_bulkActivateProcessInstance";

    protected DialogWindows dialogWindows;
    protected Runnable afterSaveHandler;

    protected boolean visibleByActionUiPermission;

    public BulkActivateProcessInstanceAction() {
        super(ID);
    }

    public BulkActivateProcessInstanceAction(String id) {
        super(id);
    }

    @Autowired
    protected void setAccessManager(AccessManager accessManager) {
        ProcessInstanceActivateAccessContext context = new ProcessInstanceActivateAccessContext();
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
    public void execute() {
        checkTarget();
        List<String> ids = target.getSelectedItems().stream()
                .map(ProcessInstanceData::getInstanceId)
                .toList();
        dialogWindows.view(getCurrentView(), BulkActivateProcessInstanceView.class)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE) && afterSaveHandler != null) {
                        afterSaveHandler.run();
                    }
                })
                .withViewConfigurer(view -> view.setInstancesIds(ids))
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
        if (!super.isApplicable() || target == null || target.getSelectedItems().isEmpty()) {
            return false;
        }
        boolean suspendedSelected = target.getSelectedItems().stream()
                .anyMatch(item -> BooleanUtils.isTrue(item.getSuspended()) && BooleanUtils.isNotTrue(item.getComplete()));

        boolean noneCompleted = target.getSelectedItems().stream()
                .noneMatch(item -> BooleanUtils.isTrue(item.getComplete()));
        return suspendedSelected && noneCompleted;
    }
}
