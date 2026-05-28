/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.processinstance;

import com.vaadin.flow.component.Component;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.security.accesscontext.processinstance.ProcessInstanceMigrateAccessContext;
import io.flowset.control.view.processinstancemigration.ProcessInstanceMigrationView;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.AccessManager;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.view.StandardOutcome;
import org.springframework.beans.factory.annotation.Autowired;
import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(MigrateProcessInstanceAction.ID)
public class MigrateProcessInstanceAction extends SecuredBaseAction implements ExecutableAction {

    public static final String ID = "control_migrateProcessInstance";

    protected DialogWindows dialogWindows;
    protected Notifications notifications;
    protected Messages messages;
    protected Metadata metadata;

    protected ProcessInstanceData processInstanceData;
    protected Runnable afterSaveHandler;

    protected boolean visibleByActionUiPermission;

    public MigrateProcessInstanceAction() {
        super(ID);
    }

    public MigrateProcessInstanceAction(String id) {
        super(id);
    }

    @Autowired
    protected void setAccessManager(AccessManager accessManager) {
        ProcessInstanceMigrateAccessContext context = new ProcessInstanceMigrateAccessContext();
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
        this.description = messages.getMessage("actions.Migrate");
    }

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public void setProcessInstanceData(ProcessInstanceData processInstanceData) {
        this.processInstanceData = processInstanceData;
    }

    public void setAfterSaveHandler(Runnable afterSaveHandler) {
        this.afterSaveHandler = afterSaveHandler;
    }

    @Override
    public void execute() {
        ProcessDefinitionData processDefinitionData = metadata.create(ProcessDefinitionData.class);
        processDefinitionData.setId(processInstanceData.getProcessDefinitionId());
        processDefinitionData.setKey(processInstanceData.getProcessDefinitionKey());
        processDefinitionData.setVersion(processInstanceData.getProcessDefinitionVersion());

        dialogWindows.view(getCurrentView(), ProcessInstanceMigrationView.class)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                        notifications.create(messages.getMessage("io.flowset.control.view.processinstance.generalpanel/processInstanceMigrated"))
                                .withType(Notifications.Type.SUCCESS)
                                .show();
                        if (afterSaveHandler != null) {
                            afterSaveHandler.run();
                        }
                    }
                })
                .withViewConfigurer(view -> {
                    view.setProcessDefinitionData(processDefinitionData);
                    view.setProcessInstanceData(processInstanceData);
                })
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
