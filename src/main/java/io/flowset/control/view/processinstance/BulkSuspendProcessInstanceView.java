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

@Route(value = "bulk-suspend-process-instance-view", layout = DefaultMainViewParent.class)
@ViewController(id = "BulkSuspendProcessInstanceView")
@ViewDescriptor(path = "bulk-suspend-process-instance-view.xml")
public class BulkSuspendProcessInstanceView extends StandardView {

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

    @Subscribe("suspendAction")
    public void onSuspendAction(final ActionPerformedEvent event) {
        BatchData batchData = processInstanceService.suspendByIdsAsync(instancesIds);

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
        batchNotificationContent.setTitle(messageBundle.getMessage("bulkSuspendProcessInstancesStarted"));

        Notification notification = notifications.create(batchNotificationContent.getContent())
                .withCloseable(true)
                .build();
        notification.setDuration(BatchNotificationContentFragment.DEFAULT_DURATION);
        notification.open();
    }
}
