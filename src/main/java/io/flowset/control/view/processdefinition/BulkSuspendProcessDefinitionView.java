/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processdefinition;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.view.*;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

@ViewController("bpm_BulkSuspendProcessDefinition")
@ViewDescriptor("bulk-suspend-process-definition-view.xml")
@DialogMode(width = "35em")
public class BulkSuspendProcessDefinitionView extends ProcessDefinitionBulkOperationView {
    @Autowired
    protected ProcessDefinitionService processDefinitionService;

    @ViewComponent
    protected Checkbox suspendProcessInstancesCheckBox;

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected Messages messages;

    @ViewComponent
    protected JmixCheckbox suspendAllVersionsCheckBox;


    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        suspendProcessInstancesCheckBox.setValue(true);
    }

    @Subscribe("suspendBtn")
    protected void onSuspendBtnClick(ClickEvent<Button> event) {
        if (BooleanUtils.isTrue(suspendAllVersionsCheckBox.getValue())) {
            Set<String> processDefinitionKeys = collectProcessDefinitionKeys();
            for (String key : processDefinitionKeys) {
                processDefinitionService.suspendAllVersionsByKey(key, Boolean.TRUE.equals(suspendProcessInstancesCheckBox.getValue()));
            }
        } else {
            for (ProcessDefinitionData processDefinition : processDefinitions) {
                processDefinitionService.suspendById(processDefinition.getId(),
                        Boolean.TRUE.equals(suspendProcessInstancesCheckBox.getValue()));
            }
        }

        notifications.create(messages.getMessage(getClass(), "bulkSuspendSuccessNotification.headed"))
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