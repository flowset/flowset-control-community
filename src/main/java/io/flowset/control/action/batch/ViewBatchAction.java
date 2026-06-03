/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.batch;

import com.vaadin.flow.component.Component;
import feign.FeignException;
import io.flowset.control.entity.batch.BatchData;
import io.flowset.control.security.SecuritySupport;
import io.flowset.control.service.batch.BatchService;
import io.flowset.control.view.batch.AllBatchListView;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.SecuredBaseAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(ViewBatchAction.ID)
public class ViewBatchAction extends SecuredBaseAction {

    public static final String ID = "control_viewBatch";

    protected SecuritySupport securitySupport;

    protected BatchService batchService;
    protected Notifications notifications;
    protected DialogWindows dialogWindows;
    protected Messages messages;

    protected String batchId;
    protected boolean visibleByActionUiPermission;

    public ViewBatchAction() {
        super(ID);
    }

    public ViewBatchAction(String id) {
        super(id);
    }

    @Autowired
    public void setSecuritySupport(SecuritySupport securitySupport) {
        this.securitySupport = securitySupport;

        visibleByActionUiPermission = securitySupport.isEntityViewPermitted(BatchData.class);
    }

    @Override
    public void refreshState() {
        super.refreshState();

        setVisibleInternal(visibleExplicitly && visibleByActionUiPermission);
    }

    @Autowired
    public void setBatchService(BatchService batchService) {
        this.batchService = batchService;
    }

    @Autowired
    public void setNotifications(Notifications notifications) {
        this.notifications = notifications;
    }

    @Autowired
    public void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    public void setBatchId(@Nullable String batchId) {
        this.batchId = batchId;
        refreshState();
    }

    @Override
    public void actionPerform(Component component) {
        if (batchId == null) {
            dialogWindows.view(getCurrentView(), AllBatchListView.class)
                    .open();
        } else {
            try {
                BatchData batchData = batchService.getById(batchId);
                if (batchData == null) {
                    showBatchNotFoundNotification();
                    return;
                }
                dialogWindows.detail(getCurrentView(), BatchData.class)
                        .editEntity(batchData)
                        .open();
            } catch (FeignException e) {
                if (e.status() == 404) {
                    showBatchNotFoundNotification();
                    return;
                }
                throw e;
            }
        }
    }

    protected void showBatchNotFoundNotification() {
        notifications.create(messages.getMessage(ViewBatchAction.class, "batchNotFoundWarning"))
                .withType(Notifications.Type.WARNING)
                .show();
    }
}
