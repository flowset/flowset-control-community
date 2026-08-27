/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.processinstance;

import io.flowset.control.entity.processinstance.RuntimeProcessInstanceData;
import io.flowset.control.security.accesscontext.processinstance.ProcessInstanceSuspendAccessContext;
import io.flowset.control.view.processinstance.BulkSuspendProcessInstanceView;
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

@ActionType(BulkSuspendRuntimeProcessInstanceAction.ID)
public class BulkSuspendRuntimeProcessInstanceAction
        extends ListDataComponentAction<BulkSuspendRuntimeProcessInstanceAction, RuntimeProcessInstanceData> {

    public static final String ID = "control_bulkSuspendRuntimeProcessInstance";

    protected DialogWindows dialogWindows;
    protected Runnable afterSaveHandler;

    protected boolean visibleByActionUiPermission;

    public BulkSuspendRuntimeProcessInstanceAction() {
        super(ID);
    }

    public BulkSuspendRuntimeProcessInstanceAction(String id) {
        super(id);
    }

    @Autowired
    protected void setAccessManager(AccessManager accessManager) {
        ProcessInstanceSuspendAccessContext context = new ProcessInstanceSuspendAccessContext();
        accessManager.applyRegisteredConstraints(context);

        visibleByActionUiPermission = context.isPermitted();
    }

    @Autowired
    public void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.Suspend");
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
        dialogWindows.view(getCurrentView(), BulkSuspendProcessInstanceView.class)
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
        return target.getSelectedItems().stream()
                .anyMatch(item -> BooleanUtils.isNotTrue(item.getSuspended()));
    }
}
