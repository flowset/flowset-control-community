package io.flowset.control.view.processinstance;


import com.vaadin.flow.router.Route;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.*;
import io.flowset.control.service.processinstance.ProcessInstanceService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "bulk-activate-process-instance-view", layout = DefaultMainViewParent.class)
@ViewController(id = "BulkActivateProcessInstanceView")
@ViewDescriptor(path = "bulk-activate-process-instance-view.xml")
public class BulkActivateProcessInstanceView extends StandardView {

    @Autowired
    private ProcessInstanceService processInstanceService;
    @Autowired
    private Notifications notifications;

    @ViewComponent
    private MessageBundle messageBundle;

    protected List<String> instancesIds;

    public void setInstancesIds(List<String> instancesIds) {
        this.instancesIds = instancesIds;
    }

    @Subscribe("activateAction")
    public void onActivateAction(final ActionPerformedEvent event) {
        processInstanceService.activateByIdsAsync(instancesIds);
        notifications.create(messageBundle.getMessage("bulkActivateProcessInstancesStarted"))
                .withType(Notifications.Type.SUCCESS)
                .show();

        close(StandardOutcome.SAVE);
    }

    @Subscribe("cancelAction")
    public void onCancelAction(final ActionPerformedEvent event) {
        close(StandardOutcome.CLOSE);
    }
}