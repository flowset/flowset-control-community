package io.flowset.control.view.processinstance;


import com.vaadin.flow.router.Route;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.service.processinstance.ProcessInstanceService;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "suspend-process-instance-view", layout = DefaultMainViewParent.class)
@ViewController(id = "SuspendProcessInstanceView")
@ViewDescriptor(path = "suspend-process-instance-view.xml")
public class SuspendProcessInstanceView extends StandardView {

    @Autowired
    private ProcessInstanceService processInstanceService;
    @Autowired
    private Notifications notifications;

    @ViewComponent
    private MessageBundle messageBundle;

    protected ProcessInstanceData processInstanceData;

    public void setProcessInstanceData(ProcessInstanceData processInstanceData) {
        this.processInstanceData = processInstanceData;
    }

    @Subscribe("suspendAction")
    public void onSuspendAction(final ActionPerformedEvent event) {
        processInstanceService.suspendById(processInstanceData.getId());

        notifications.create(messageBundle.getMessage("processInstanceSuspended"))
                .withType(Notifications.Type.SUCCESS)
                .show();

        close(StandardOutcome.SAVE);
    }

    @Subscribe("cancelAction")
    public void onCancelAction(final ActionPerformedEvent event) {
        close(StandardOutcome.CLOSE);
    }
}