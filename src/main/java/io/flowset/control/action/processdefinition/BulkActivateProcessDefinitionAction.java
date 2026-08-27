/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.processdefinition;

import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.security.accesscontext.processdefinition.ProcessDefinitionActivateAccessContext;
import io.flowset.control.view.processdefinition.BulkActivateProcessDefinitionView;
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

@ActionType(BulkActivateProcessDefinitionAction.ID)
public class BulkActivateProcessDefinitionAction
        extends ListDataComponentAction<BulkActivateProcessDefinitionAction, ProcessDefinitionData> {

    public static final String ID = "control_bulkActivateProcessDefinition";

    protected DialogWindows dialogWindows;

    protected Runnable afterSaveHandler;

    protected boolean visibleByActionUiPermission;

    public BulkActivateProcessDefinitionAction() {
        super(ID);
    }

    public BulkActivateProcessDefinitionAction(String id) {
        super(id);
    }

    @Autowired
    protected void setAccessManager(AccessManager accessManager) {
        ProcessDefinitionActivateAccessContext context = new ProcessDefinitionActivateAccessContext();
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
        Set<ProcessDefinitionData> selectedItems = target.getSelectedItems();
        dialogWindows.view(getCurrentView(), BulkActivateProcessDefinitionView.class)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE) && afterSaveHandler != null) {
                        afterSaveHandler.run();
                    }
                })
                .withViewConfigurer(view -> view.setProcessDefinitions(selectedItems))
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
                && isApplicableByState();
    }

    protected boolean isApplicableByState() {
        return target.getSelectedItems().stream().anyMatch(definition ->
                BooleanUtils.isTrue(definition.getSuspended()));
    }
}
