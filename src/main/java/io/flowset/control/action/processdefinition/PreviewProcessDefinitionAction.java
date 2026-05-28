/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.processdefinition;

import com.vaadin.flow.component.Component;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.security.SecuritySupport;
import io.flowset.control.view.processdefinition.ProcessDefinitionDiagramView;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.SecuredBaseAction;
import org.springframework.beans.factory.annotation.Autowired;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(PreviewProcessDefinitionAction.ID)
public class PreviewProcessDefinitionAction extends SecuredBaseAction implements ExecutableAction {

    public static final String ID = "control_previewProcessDefinition";

    protected DialogWindows dialogWindows;

    protected ProcessDefinitionData processDefinition;

    protected boolean visibleByActionUiPermission;

    public PreviewProcessDefinitionAction() {
        super(ID);
    }

    public PreviewProcessDefinitionAction(String id) {
        super(id);
    }

    @Autowired
    public void setSecuritySupport(SecuritySupport securitySupport) {
        visibleByActionUiPermission = securitySupport.isEntityViewPermitted(ProcessDefinitionData.class);
    }

    @Autowired
    public void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    public void setProcessDefinition(ProcessDefinitionData processDefinition) {
        this.processDefinition = processDefinition;
    }

    @Override
    public void actionPerform(Component component) {
        execute();
    }

    @Override
    public void execute() {
        dialogWindows.view(getCurrentView(), ProcessDefinitionDiagramView.class)
                .withViewConfigurer(view ->
                        view.setProcessDefinition(processDefinition))
                .open();
    }

    @Override
    public void refreshState() {
        super.refreshState();

        setVisibleInternal(visibleExplicitly && visibleByActionUiPermission);
    }
}
