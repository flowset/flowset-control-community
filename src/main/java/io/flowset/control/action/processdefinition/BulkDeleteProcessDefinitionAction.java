/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.processdefinition;

import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.security.SecuritySupport;
import io.flowset.control.view.processdefinition.BulkDeleteProcessDefinitionView;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.list.ListDataComponentAction;
import io.jmix.flowui.view.StandardOutcome;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(BulkDeleteProcessDefinitionAction.ID)
public class BulkDeleteProcessDefinitionAction
        extends ListDataComponentAction<BulkDeleteProcessDefinitionAction, ProcessDefinitionData> {

    public static final String ID = "control_bulkDeleteProcessDefinition";

    protected DialogWindows dialogWindows;

    protected Runnable afterSaveHandler;

    protected boolean visibleByActionUiPermission;

    public BulkDeleteProcessDefinitionAction() {
        super(ID);
    }

    public BulkDeleteProcessDefinitionAction(String id) {
        super(id);
    }

    @Autowired
    protected void setSecuritySupport(SecuritySupport securitySupport) {
        visibleByActionUiPermission = securitySupport.isEntityDeletePermitted(ProcessDefinitionData.class);
    }

    @Autowired
    public void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.Remove");
    }

    public void setAfterSaveHandler(Runnable afterSaveHandler) {
        this.afterSaveHandler = afterSaveHandler;
    }

    @Override
    public void execute() {
        checkTarget();
        Set<ProcessDefinitionData> selectedItems =  target.getSelectedItems();
        if (selectedItems.isEmpty()) {
            return;
        }

        dialogWindows.view(getCurrentView(), BulkDeleteProcessDefinitionView.class)
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
        return super.isApplicable() && ! target.getSelectedItems().isEmpty();
    }
}
