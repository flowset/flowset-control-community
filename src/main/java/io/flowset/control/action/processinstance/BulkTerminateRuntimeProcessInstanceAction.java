/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.processinstance;

import io.flowset.control.entity.processinstance.RuntimeProcessInstanceData;
import io.flowset.control.security.accesscontext.processinstance.ProcessInstanceTerminateAccessContext;
import io.flowset.control.view.processinstance.BulkTerminateProcessInstanceView;
import io.jmix.core.Messages;
import io.jmix.core.AccessManager;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.list.ListDataComponentAction;
import io.jmix.flowui.view.StandardOutcome;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(BulkTerminateRuntimeProcessInstanceAction.ID)
public class BulkTerminateRuntimeProcessInstanceAction
        extends ListDataComponentAction<BulkTerminateRuntimeProcessInstanceAction, RuntimeProcessInstanceData> {

    public static final String ID = "control_bulkTerminateRuntimeProcessInstance";

    protected DialogWindows dialogWindows;
    protected Runnable afterSaveHandler;

    protected boolean visibleByActionUiPermission;

    public BulkTerminateRuntimeProcessInstanceAction() {
        super(ID);
    }

    public BulkTerminateRuntimeProcessInstanceAction(String id) {
        super(id);
    }

    @Autowired
    protected void setAccessManager(AccessManager accessManager) {
        ProcessInstanceTerminateAccessContext context = new ProcessInstanceTerminateAccessContext();
        accessManager.applyRegisteredConstraints(context);

        visibleByActionUiPermission = context.isPermitted();
    }

    @Autowired
    public void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.Terminate");
    }

    public void setAfterSaveHandler(Runnable afterSaveHandler) {
        this.afterSaveHandler = afterSaveHandler;
    }

    @Override
    public void execute() {
        checkTarget();
        List<String> ids = target.getSelectedItems().stream()
                .map(RuntimeProcessInstanceData::getInstanceId)
                .toList();
        dialogWindows.view(getCurrentView(), BulkTerminateProcessInstanceView.class)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE) && afterSaveHandler != null) {
                        afterSaveHandler.run();
                    }
                })
                .withViewConfigurer(view -> view.setProcessInstanceIds(ids))
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
        return super.isApplicable() && target != null && !target.getSelectedItems().isEmpty();
    }
}
