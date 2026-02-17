/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processinstance;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.service.processinstance.ProcessInstanceBulkTerminateContext;
import io.flowset.control.service.processinstance.ProcessInstanceService;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;

@Route(value = "bpm/bulkterminateprocessinstance", layout = DefaultMainViewParent.class)
@ViewController("bpm_BulkTerminateProcessInstance")
@ViewDescriptor("bulk-terminate-process-instance-view.xml")
@DialogMode(width = "42em")
public class BulkTerminateProcessInstanceView extends StandardView {
    public static final int REASON_MAX_LENGTH = 4000;

    @Autowired
    protected Notifications notifications;
    @ViewComponent
    protected MessageBundle messageBundle;

    @Autowired
    protected ProcessInstanceService processInstanceService;

    @ViewComponent
    protected TextArea reasonTextArea;
    @ViewComponent
    protected JmixCheckbox skipIoMappingsField;
    @ViewComponent
    protected JmixCheckbox skipCustomListenersField;
    @ViewComponent
    protected JmixCheckbox skipSubprocessesField;

    protected Collection<ProcessInstanceData> processInstances;

    @SuppressWarnings("LombokSetterMayBeUsed")
    public void setProcessInstances(Collection<ProcessInstanceData> processInstances) {
        this.processInstances = processInstances;
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        addClassNames(LumoUtility.Gap.XSMALL);
        reasonTextArea.getStyle().set("resize", "vertical");
        reasonTextArea.getStyle().set("overflow", "auto");
        reasonTextArea.setMaxLength(REASON_MAX_LENGTH);

        skipIoMappingsField.setValue(true);
        skipCustomListenersField.setValue(true);
    }

    @Subscribe("okBtn")
    protected void onOkBtnClick(ClickEvent<Button> event) {
        List<String> ids = processInstances.stream().map(ProcessInstanceData::getInstanceId).toList();
        ProcessInstanceBulkTerminateContext context = new ProcessInstanceBulkTerminateContext(ids)
                .setReason(reasonTextArea.getValue())
                .setSkipCustomListeners(skipCustomListenersField.getValue())
                .setSkipIoMappings(skipIoMappingsField.getValue())
                .setSkipSubprocesses(skipSubprocessesField.getValue());
        processInstanceService.terminateByIdsAsync(context);
        notifications.create(messageBundle.getMessage("bulkTerminateProcessesStarted"))
                .withThemeVariant(NotificationVariant.LUMO_PRIMARY)
                .show();

        close(StandardOutcome.SAVE);
    }

    @Subscribe("cancelBtn")
    protected void onCancelBtnClick(ClickEvent<Button> event) {
        close(StandardOutcome.DISCARD);
    }
}