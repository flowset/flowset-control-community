package io.flowset.control.view.processinstanceterminate;


import io.flowset.control.service.processinstance.ProcessInstanceBulkTerminateContext;
import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.service.processinstance.ProcessInstanceService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

@ViewController(id = "ProcessInstanceTerminateView")
@ViewDescriptor(path = "process-instance-terminate-view.xml")
public class ProcessInstanceTerminateView extends StandardView {

    @Autowired
    private ProcessInstanceService processInstanceService;

    @ViewComponent
    protected JmixTextArea reasonTextArea;
    @ViewComponent
    protected JmixCheckbox skipIoMappingsField;
    @ViewComponent
    protected JmixCheckbox skipCustomListenersField;
    @ViewComponent
    protected JmixCheckbox skipSubprocessesField;
    @Autowired
    protected Notifications notifications;

    @Autowired
    protected Messages messages;

    protected ProcessInstanceData processInstanceData;

    public void setProcessInstanceData(ProcessInstanceData processInstanceData) {
        this.processInstanceData = processInstanceData;
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        skipIoMappingsField.setValue(true);
        skipCustomListenersField.setValue(true);
    }

    @Subscribe("terminateAction")
    public void onTerminateAction(final ActionPerformedEvent event) {
        String processInstanceId = processInstanceData.getId();
        String reasonValue = reasonTextArea.getValue();

        ProcessInstanceBulkTerminateContext context = new ProcessInstanceBulkTerminateContext(Collections.singletonList(processInstanceId))
                .setReason(reasonValue)
                .setSkipCustomListeners(skipCustomListenersField.getValue())
                .setSkipIoMappings(skipIoMappingsField.getValue())
                .setSkipSubprocesses(skipSubprocessesField.getValue());

        processInstanceService.terminateByIdsAsync(context);
        notifications.create(messages.getMessage("io.flowset.control.view.processinstance.generalpanel/processInstanceTerminated"))
                .withType(Notifications.Type.SUCCESS)
                .show();
        close(StandardOutcome.SAVE);
    }

    @Subscribe("cancelAction")
    public void onCancelAction(final ActionPerformedEvent event) {
        close(StandardOutcome.DISCARD);
    }
}