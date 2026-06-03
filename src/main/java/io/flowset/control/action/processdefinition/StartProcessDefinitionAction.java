/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.processdefinition;

import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.security.accesscontext.processdefinition.ProcessDefinitionStartAccessContext;
import io.flowset.control.view.startprocess.StartProcessWithVariableView;
import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.action.ActionType;
import com.vaadin.flow.component.Component;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.view.StandardOutcome;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(StartProcessDefinitionAction.ID)
public class StartProcessDefinitionAction extends SecuredBaseAction implements ExecutableAction {

    public static final String ID = "control_startProcessDefinition";

    protected DialogWindows dialogWindows;
    protected Notifications notifications;
    protected Messages messages;

    protected ProcessDefinitionData processDefinitionData;
    protected Runnable afterSaveHandler;

    protected boolean visibleByActionUiPermission;

    public StartProcessDefinitionAction() {
        super(ID);
    }

    public StartProcessDefinitionAction(String id) {
        super(id);
    }

    @Autowired
    protected void setAccessManager(AccessManager accessManager) {
        ProcessDefinitionStartAccessContext context = new ProcessDefinitionStartAccessContext();
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
        this.description = messages.getMessage("actions.StartProcess");
    }

    public void setProcessDefinitionData(ProcessDefinitionData processDefinitionData) {
        this.processDefinitionData = processDefinitionData;
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
        dialogWindows.detail(getCurrentView(), ProcessDefinitionData.class)
                .withViewClass(StartProcessWithVariableView.class)
                .editEntity(processDefinitionData)
                .withAfterCloseListener(e -> {
                    if (e.closedWith(StandardOutcome.SAVE)) {
                        String label = StringUtils.defaultIfEmpty(
                                processDefinitionData.getName(), processDefinitionData.getKey());
                        notifications.create(messages.formatMessage("io.flowset.control.view.processdefinition",
                                        "startProcess.success", label))
                                .withType(Notifications.Type.SUCCESS)
                                .build()
                                .open();
                        if (afterSaveHandler != null) {
                            afterSaveHandler.run();
                        }
                    }
                })
                .open();
    }

    @Override
    public void refreshState() {
        super.refreshState();

        setVisibleInternal(visibleExplicitly && visibleByActionUiPermission && isVisibleByState());
    }

    protected boolean isVisibleByState() {
        return processDefinitionData != null && BooleanUtils.isNotTrue(processDefinitionData.getSuspended());
    }

}
