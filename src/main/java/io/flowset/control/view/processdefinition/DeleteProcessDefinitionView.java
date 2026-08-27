/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processdefinition;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.exception.RemoteProcessEngineException;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.view.*;
import io.flowset.control.service.processinstance.ProcessInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

@ViewController("bpm_DeleteProcessDefinition")
@ViewDescriptor("delete-process-definition-view.xml")
@DialogMode(width = "35em")
@Slf4j
public class DeleteProcessDefinitionView extends StandardView {

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected ProcessDefinitionService processDefinitionService;
    @Autowired
    private ProcessInstanceService processInstanceService;

    @ViewComponent
    protected Icon allInstancesContextHelp;

    @ViewComponent
    protected JmixCheckbox deleteProcessInstancesCheckBox;

    protected String processDefinitionId;
    @ViewComponent
    private MessageBundle messageBundle;

    @SuppressWarnings("LombokSetterMayBeUsed")
    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    @Subscribe
    public void onInit(final InitEvent event) {
        addClassNames(LumoUtility.Padding.Top.NONE, LumoUtility.Padding.Left.LARGE);
        allInstancesContextHelp.addClassNames(LumoUtility.TextColor.SECONDARY);
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        deleteProcessInstancesCheckBox.setValue(true);

        long countByProcessDefinitionId = processInstanceService.getCountByProcessDefinitionId(processDefinitionId);
        if (countByProcessDefinitionId > 0) {
            allInstancesContextHelp.setTooltipText(messageBundle.getMessage("deleteAllRunningInstances.tooltip"));
        }
        deleteProcessInstancesCheckBox.setEnabled(countByProcessDefinitionId == 0);
    }

    @Subscribe("okBtn")
    protected void onActivateBtnClick(ClickEvent<Button> event) {
        boolean deleteAllRelatedInstances = BooleanUtils.isTrue(deleteProcessInstancesCheckBox.getValue());
        try {
            processDefinitionService.deleteById(processDefinitionId, deleteAllRelatedInstances);
        } catch (Exception e) {
            if (e instanceof RemoteProcessEngineException engineException) {
                log.error("Unable to delete process definition version", e);

                String errorReason;
                String responseMessage = engineException.getResponseMessage();
                if (StringUtils.isNotBlank(responseMessage)) {
                    errorReason = responseMessage.replaceAll("\\.", ".\n");
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