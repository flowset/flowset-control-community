/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.processdefinition;

import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.security.accesscontext.processdefinition.ProcessDefinitionSuspendAccessContext;
import io.flowset.control.view.processdefinition.SuspendProcessDefinitionView;
import io.jmix.core.Messages;
import io.jmix.core.AccessManager;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.ActionType;
import com.vaadin.flow.component.Component;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.view.StandardOutcome;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(SuspendProcessDefinitionAction.ID)
public class SuspendProcessDefinitionAction extends SecuredBaseAction implements ExecutableAction {

    public static final String ID = "control_suspendProcessDefinition";

    protected DialogWindows dialogWindows;

    protected String processDefinitionId;
    protected ProcessDefinitionData processDefinitionData;
    protected Runnable afterSaveHandler;

    protected boolean visibleByActionUiPermission;

    public SuspendProcessDefinitionAction() {
        super(ID);
    }

    public SuspendProcessDefinitionAction(String id) {
        super(id);
    }

    @Autowired
    protected void setAccessManager(AccessManager accessManager) {
        ProcessDefinitionSuspendAccessContext context = new ProcessDefinitionSuspendAccessContext();
        accessManager.applyRegisteredConstraints(context);

        visibleByActionUiPermission = context.isPermitted();
    }

    @Autowired
    public void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.description = messages.getMessage("actions.Suspend");
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
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
        dialogWindows.view(getCurrentView(), SuspendProcessDefinitionView.class)
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

        setVisibleInternal(visibleExplicitly && visibleByActionUiPermission && isVisibleByState());
    }

    protected boolean isVisibleByState() {
        return processDefinitionData != null && BooleanUtils.isNotTrue(processDefinitionData.getSuspended());
    }

}
