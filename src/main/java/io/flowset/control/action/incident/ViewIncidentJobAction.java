/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.incident;

import com.vaadin.flow.component.Component;
import feign.FeignException;
import io.flowset.control.entity.incident.IncidentData;
import io.flowset.control.entity.job.JobData;
import io.flowset.control.security.SecuritySupport;
import io.flowset.control.service.job.JobService;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.SecuredBaseAction;
import org.springframework.beans.factory.annotation.Autowired;

import static io.flowset.control.util.ExceptionUtils.isNotFoundError;
import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(ViewIncidentJobAction.ID)
public class ViewIncidentJobAction extends SecuredBaseAction implements ExecutableAction {

    public static final String ID = "control_viewIncidentJob";

    protected SecuritySupport securitySupport;
    protected boolean visibleByActionUiPermission;
    protected JobService jobService;
    protected DialogWindows dialogWindows;
    protected Notifications notifications;
    protected Messages messages;

    protected IncidentData incidentData;

    public ViewIncidentJobAction() {
        super(ID);
    }

    public ViewIncidentJobAction(String id) {
        super(id);
    }

    @Autowired
    public void setSecuritySupport(SecuritySupport securitySupport) {
        this.securitySupport = securitySupport;
        visibleByActionUiPermission = securitySupport.isEntityViewPermitted(JobData.class);
    }

    @Autowired
    public void setJobService(JobService jobService) {
        this.jobService = jobService;
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
            JobData job = jobService.findById(incidentData.getConfiguration());
            dialogWindows.detail(getCurrentView(), JobData.class)
                    .editEntity(job)
                    .open();
        } catch (FeignException e) {
            if (isNotFoundError(e)) {
                notifications.create(messages.getMessage("io.flowset.control.view.incidentdata/jobNotFound"))
                        .withType(Notifications.Type.WARNING)
                        .show();
            }
        }
    }
}
