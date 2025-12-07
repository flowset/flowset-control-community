package io.flowset.control.view.processinstanceterminate;


import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.service.processinstance.ProcessInstanceService;
import io.flowset.control.view.main.MainView;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

@Route(value = "process-instance-terminate-view", layout = MainView.class)
@ViewController(id = "ProcessInstanceTerminateView")
@ViewDescriptor(path = "process-instance-terminate-view.xml")
public class ProcessInstanceTerminateView extends StandardView {

    @Autowired
    private ProcessInstanceService processInstanceService;

    @ViewComponent
    private JmixTextArea reasonTextArea;

    protected ProcessInstanceData processInstanceData;

    public void setProcessInstanceData(ProcessInstanceData processInstanceData) {
        this.processInstanceData = processInstanceData;
    }

    @Subscribe("terminateAction")
    public void onTerminateAction(final ActionPerformedEvent event) {
        String processInstanceId = processInstanceData.getId();
        String reasonValue = reasonTextArea.getValue();

        processInstanceService.terminateByIdsAsync(Collections.singletonList(processInstanceId), reasonValue);

        close(StandardOutcome.SAVE);
    }

    @Subscribe("cancelAction")
    public void onCancelAction(final ActionPerformedEvent event) {
        close(StandardOutcome.DISCARD);
    }
}