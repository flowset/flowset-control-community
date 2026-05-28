/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.processinstance;

import com.vaadin.flow.component.Component;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.security.accesscontext.processinstance.ProcessInstanceActivateAccessContext;
import io.flowset.control.view.processinstance.ActivateProcessInstanceView;
import io.jmix.core.Messages;
import io.jmix.core.AccessManager;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.view.StandardOutcome;
import org.springframework.beans.factory.annotation.Autowired;
import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(ActivateProcessInstanceAction.ID)
public class ActivateProcessInstanceAction extends SecuredBaseAction implements ExecutableAction {

    public static final String ID = "control_activateProcessInstance";

    protected DialogWindows dialogWindows;
    protected Messages messages;

    protected ProcessInstanceData processInstanceData;
    protected Runnable afterSaveHandler;

    protected boolean visibleByActionUiPermission;

    public ActivateProcessInstanceAction() {
        super(ID);
    }

    public ActivateProcessInstanceAction(String id) {
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
        this.messages = messages;
        this.description = messages.getMessage("actions.Activate");
    }

    public void setProcessInstanceData(ProcessInstanceData processInstanceData) {
        this.processInstanceData = processInstanceData;
    }

    public void setAfterSaveHandler(Runnable afterSaveHandler) {
        this.afterSaveHandler = afterSaveHandler;
    }

    @Override
    public void execute() {
        dialogWindows.view(getCurrentView(), ActivateProcessInstanceView.class)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE) && afterSaveHandler != null) {
                        afterSaveHandler.run();
                    }
                })
                .withViewConfigurer(view -> view.setProcessInstanceData(processInstanceData))
                .build()
                .open();
    }

    @Override
    public void actionPerform(Component component) {
        execute();
    }

    @Override
    public void refreshState() {
        super.refreshState();

        setVisibleInternal(visibleExplicitly && visibleByActionUiPermission);
    }
}
