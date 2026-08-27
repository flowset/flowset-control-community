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

@ViewController("bpm_BulkActivateProcessDefinition")
@ViewDescriptor("bulk-activate-process-definition-view.xml")
@DialogMode(width = "35em")
public class BulkActivateProcessDefinitionView extends ProcessDefinitionBulkOperationView {

    @Autowired
    protected Notifications notifications;
    @Autowired
    protected Messages messages;

    @Autowired
    protected ProcessDefinitionService processDefinitionService;

    @ViewComponent
    protected JmixCheckbox activateAllVersionsCheckBox;

    @ViewComponent
    protected Checkbox activateProcessInstancesCheckBox;

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        activateProcessInstancesCheckBox.setValue(true);
    }

    @Subscribe("activateBtn")
    protected void onActivateBtnClick(ClickEvent<Button> event) {
        if (BooleanUtils.isTrue(activateAllVersionsCheckBox.getValue())) {
            Set<String> processDefinitionKeys = collectProcessDefinitionKeys();
            for (String key : processDefinitionKeys) {
                processDefinitionService.activateAllVersionsByKey(key, Boolean.TRUE.equals(activateProcessInstancesCheckBox.getValue()));
            }
        } else {
            for (ProcessDefinitionData processDefinition : processDefinitions) {
                processDefinitionService.activateById(processDefinition.getId(), Boolean.TRUE.equals(activateProcessInstancesCheckBox.getValue()));
            }
        }
        notifications.create(messages.getMessage(getClass(), "bulkActivationSuccessNotification.headed"))
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