/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processdefinition;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.exception.RemoteProcessEngineException;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import io.flowset.control.service.processinstance.ProcessInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

@ViewController("bpm_BulkDeleteProcessDefinition")
@ViewDescriptor("bulk-delete-process-definition-view.xml")
@DialogMode(width = "35em")
@Slf4j
public class BulkDeleteProcessDefinitionView extends ProcessDefinitionBulkOperationView {

    @ViewComponent
    protected MessageBundle messageBundle;

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected ProcessDefinitionService processDefinitionService;

    @Autowired
    private ProcessInstanceService processInstanceService;

    @ViewComponent
    protected JmixCheckbox deleteAllVersionsCheckBox;

    @ViewComponent
    protected JmixCheckbox deleteProcessInstancesCheckBox;

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        deleteProcessInstancesCheckBox.setValue(true);

        boolean hasRunningInstances = false;
        for (ProcessDefinitionData processDefinitionData : processDefinitions) {
            long countByProcessDefinitionId = processInstanceService.getCountByProcessDefinitionId(
                    processDefinitionData.getProcessDefinitionId());
            if (countByProcessDefinitionId > 0) {
                hasRunningInstances = true;
                break;
            }
        }

        if (hasRunningInstances) {
            allInstancesContextHelp.setTooltipText(messageBundle.getMessage("bulkDeleteAllRunningInstances.tooltip"));
        }
        deleteProcessInstancesCheckBox.setEnabled(!hasRunningInstances);
    }

    @Subscribe("okBtn")
    protected void onOkBtnClick(ClickEvent<Button> event) {
        boolean deleteAllRelatedInstances = BooleanUtils.isTrue(deleteProcessInstancesCheckBox.getValue());
        try {
            if (BooleanUtils.isTrue(deleteAllVersionsCheckBox.getValue())) {
                Set<String> processDefinitionKeys = collectProcessDefinitionKeys();
                for (String key : processDefinitionKeys) {
                    processDefinitionService.deleteAllVersionsByKey(key, deleteAllRelatedInstances);
                }
            } else {
                for (ProcessDefinitionData processDefinition : processDefinitions) {
                    processDefinitionService.deleteById(processDefinition.getId(), deleteAllRelatedInstances);
                }
            }

            notifications.create(messageBundle.getMessage("processesDeleted"))
                    .withType(Notifications.Type.SUCCESS)
                    .show();
        } catch (Exception e) {
            if (e instanceof RemoteProcessEngineException processEngineException) {
                log.error("Unable to delete process definitions", e);
                String errorReason;
                String responseMessage = processEngineException.getResponseMessage();
                if (StringUtils.isNotEmpty(responseMessage)) {
                    errorReason = responseMessage.replaceAll("\\.", ".\n"); //add new lines for long messages
                } else {
                    errorReason = e.getMessage();
                }

                notifications.create(errorReason)
                        .withType(Notifications.Type.ERROR)
                        .withDuration(10000)
                        .show();
                return;
            }
            throw e;
        }
        close(StandardOutcome.SAVE);
    }

    @Subscribe("cancelBtn")
    protected void onCancelBtnClick(ClickEvent<Button> event) {
        close(StandardOutcome.DISCARD);
    }
}