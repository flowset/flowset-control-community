/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.processdefinition;

import com.vaadin.flow.component.Component;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.security.SecuritySupport;
import io.flowset.control.view.processdefinition.DeleteProcessDefinitionView;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.view.StandardOutcome;
import org.springframework.beans.factory.annotation.Autowired;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(DeleteProcessDefinitionAction.ID)
public class DeleteProcessDefinitionAction extends SecuredBaseAction implements ExecutableAction {

    public static final String ID = "control_deleteProcessDefinition";

    protected DialogWindows dialogWindows;

    protected String processDefinitionId;
    protected Runnable afterSaveHandler;

    protected boolean visibleByActionUiPermission;

    public DeleteProcessDefinitionAction() {
        super(ID);
    }

    public DeleteProcessDefinitionAction(String id) {
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
        this.description = messages.getMessage("actions.Remove");
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
        refreshState();
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
        dialogWindows.view(getCurrentView(), DeleteProcessDefinitionView.class)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE) && afterSaveHandler != null) {
                        afterSaveHandler.run();
                    }
                })
                .withViewConfigurer(view -> view.setProcessDefinitionId(processDefinitionId))
                .build()
                .open();
    }

    @Override
    public void refreshState() {
        super.refreshState();

        setVisibleInternal(visibleExplicitly && visibleByActionUiPermission);
    }
}
