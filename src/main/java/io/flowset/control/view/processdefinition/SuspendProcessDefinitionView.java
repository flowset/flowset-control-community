/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processdefinition;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@ViewController("bpm_SuspendProcessDefinition")
@ViewDescriptor("suspend-process-definition-view.xml")
@DialogMode(width = "35em")
public class SuspendProcessDefinitionView extends StandardView {
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected Messages messages;

    @Autowired
    protected ProcessDefinitionService processDefinitionService;
    @ViewComponent
    protected Checkbox suspendProcessInstancesCheckBox;

    @ViewComponent
    protected Icon allInstancesContextHelp;

    protected String processDefinitionId;

    @SuppressWarnings("LombokSetterMayBeUsed")
    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    @Subscribe
    public void onInit(final InitEvent event) {
        allInstancesContextHelp.addClassNames(LumoUtility.TextColor.SECONDARY);
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        suspendProcessInstancesCheckBox.setValue(true);
    }

    @Subscribe("suspendBtn")
    protected void onSuspendBtnClick(ClickEvent<Button> event) {
        processDefinitionService.suspendById(
                processDefinitionId,
                Boolean.TRUE.equals(suspendProcessInstancesCheckBox.getValue())
        );
        notifications.create(messages.getMessage(getClass(), "suspendSuccessNotification.headed"))
                .withType(Notifications.Type.SUCCESS)
                .build()
                .open();
        close(StandardOutcome.SAVE);
    }

    @Subscribe("cancelBtn")
    protected void onCancelBtnClick(ClickEvent<Button> event) {
        close(StandardOutcome.DISCARD);
    }

}