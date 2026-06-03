/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.processinstance;

import com.vaadin.flow.component.Component;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.security.accesscontext.processinstance.ProcessInstanceTerminateAccessContext;
import io.flowset.control.view.processinstanceterminate.ProcessInstanceTerminateView;
import io.jmix.core.Messages;
import io.jmix.core.AccessManager;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.view.StandardOutcome;
import org.springframework.beans.factory.annotation.Autowired;
import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(TerminateProcessInstanceAction.ID)
public class TerminateProcessInstanceAction extends SecuredBaseAction implements ExecutableAction {

    public static final String ID = "control_terminateProcessInstance";

    protected DialogWindows dialogWindows;
    protected Notifications notifications;
    protected Messages messages;

    protected ProcessInstanceData processInstanceData;
    protected Runnable afterSaveHandler;

    protected boolean visibleByActionUiPermission;

    public TerminateProcessInstanceAction() {
        super(ID);
    }

    public TerminateProcessInstanceAction(String id) {
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
    public void setNotifications(Notifications notifications) {
        this.notifications = notifications;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
        this.description = messages.getMessage("actions.Terminate");
    }

    public void setProcessInstanceData(ProcessInstanceData processInstanceData) {
        this.processInstanceData = processInstanceData;
    }

    public void setAfterSaveHandler(Runnable afterSaveHandler) {
        this.afterSaveHandler = afterSaveHandler;
    }

    @Override
    public void execute() {
        dialogWindows.view(getCurrentView(), ProcessInstanceTerminateView.class)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                        if (afterSaveHandler != null) {
                            afterSaveHandler.run();
                        }
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
