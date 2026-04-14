package io.flowset.control.view.processinstance;


import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.notification.Notification;
import io.flowset.control.entity.batch.BatchData;
import io.flowset.control.view.batch.notification.BatchNotificationContentFragment;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.*;
import io.flowset.control.service.processinstance.ProcessInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.List;

@Route(value = "bulk-activate-process-instance-view", layout = DefaultMainViewParent.class)
@ViewController(id = "BulkActivateProcessInstanceView")
@ViewDescriptor(path = "bulk-activate-process-instance-view.xml")
public class BulkActivateProcessInstanceView extends StandardView {

    @Autowired
    protected ProcessInstanceService processInstanceService;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected Fragments fragments;

    @ViewComponent
    protected MessageBundle messageBundle;

    protected List<String> instancesIds;

    public void setInstancesIds(List<String> instancesIds) {
        this.instancesIds = instancesIds;
    }

    @Subscribe("activateAction")
    public void onActivateAction(final ActionPerformedEvent event) {
        BatchData batchData = processInstanceService.activateByIdsAsync(instancesIds);

        showBatchNotification(batchData);

        close(StandardOutcome.SAVE);
    }

    @Subscribe("cancelAction")
    public void onCancelAction(final ActionPerformedEvent event) {
        close(StandardOutcome.CLOSE);
    }

    protected void showBatchNotification(@Nullable BatchData batchData) {
        BatchNotificationContentFragment batchNotificationContent = fragments.create(this, BatchNotificationContentFragment.class);
        batchNotificationContent.setBatchId(batchData != null ? batchData.getId() : null);
        batchNotificationContent.setTitle(messageBundle.getMessage("bulkActivateProcessInstancesStarted"));

        Notification notification = notifications.create(batchNotificationContent.getContent())
                .withCloseable(true)
                .build();
        notification.setDuration(BatchNotificationContentFragment.DEFAULT_DURATION);
        notification.open();
    }
}
