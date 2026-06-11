/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.incident;

import com.vaadin.flow.component.Component;
import feign.FeignException;
import io.flowset.control.entity.ExternalTaskData;
import io.flowset.control.entity.incident.IncidentData;
import io.flowset.control.security.SecuritySupport;
import io.flowset.control.service.externaltask.ExternalTaskService;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.SecuredBaseAction;
import org.springframework.beans.factory.annotation.Autowired;

import static io.flowset.control.util.ExceptionUtils.isNotFoundError;
import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(ViewIncidentExternalTaskAction.ID)
public class ViewIncidentExternalTaskAction extends SecuredBaseAction implements ExecutableAction {

    public static final String ID = "control_viewIncidentExternalTask";

    protected boolean visibleByActionUiPermission;
    protected ExternalTaskService externalTaskService;
    protected DialogWindows dialogWindows;
    protected Notifications notifications;
    protected Messages messages;

    protected IncidentData incidentData;

    public ViewIncidentExternalTaskAction() {
        super(ID);
    }

    public ViewIncidentExternalTaskAction(String id) {
        super(id);
    }

    @Autowired
    public void setSecuritySupport(SecuritySupport securitySupport) {
        visibleByActionUiPermission = securitySupport.isEntityViewPermitted(ExternalTaskData.class);
    }

    @Autowired
    public void setExternalTaskService(ExternalTaskService externalTaskService) {
        this.externalTaskService = externalTaskService;
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
    }

    public void setIncidentData(IncidentData incidentData) {
        this.incidentData = incidentData;
    }

    @Override
    public void refreshState() {
        super.refreshState();

        setVisibleInternal(visibleExplicitly && visibleByActionUiPermission);
    }

    @Override
    public void actionPerform(Component component) {
        execute();
    }

    @Override
    public void execute() {
        if (incidentData == null) {
            return;
        }

        try {
            ExternalTaskData externalTask = externalTaskService.findById(incidentData.getConfiguration());
            if (externalTask != null) {
                dialogWindows.detail(getCurrentView(), ExternalTaskData.class)
                        .editEntity(externalTask)
                        .open();
            }
        } catch (FeignException e) {
            if (isNotFoundError(e)) {
                notifications.create(messages.getMessage("io.flowset.control.view.incidentdata/externalTaskNotFound"))
                        .withType(Notifications.Type.WARNING)
                        .show();
            }
        }
    }
}
