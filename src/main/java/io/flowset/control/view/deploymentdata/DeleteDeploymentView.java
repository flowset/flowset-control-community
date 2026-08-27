/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.deploymentdata;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.flowset.control.exception.RemoteProcessEngineException;
import io.flowset.control.service.deployment.DeploymentService;
import io.flowset.control.service.processinstance.ProcessInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

@ViewController("bpm_DeleteDeployment")
@ViewDescriptor("delete-deployment-view.xml")
@DialogMode(width = "35em")
@Slf4j
public class DeleteDeploymentView extends StandardView {

    @Autowired
    protected Notifications notifications;
    @Autowired
    private ProcessInstanceService processInstanceService;
    @Autowired
    private DeploymentService deploymentService;

    @ViewComponent
    protected Icon allInstancesContextHelp;
    @ViewComponent
    protected JmixCheckbox deleteProcessInstancesCheckBox;
    @ViewComponent
    private MessageBundle messageBundle;
    @ViewComponent
    private JmixCheckbox skipCustomListenersCheckBox;
    @ViewComponent
    private JmixCheckbox skipIOMappingsCheckBox;

    protected String deploymentId;

    @SuppressWarnings("LombokSetterMayBeUsed")
    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    @Subscribe
    public void onInit(final InitEvent event) {
        addClassNames(LumoUtility.Padding.Top.NONE, LumoUtility.Padding.Left.LARGE);
        allInstancesContextHelp.addClassNames(LumoUtility.TextColor.SECONDARY);
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        deleteProcessInstancesCheckBox.setValue(false);

        long countByDeploymentId = processInstanceService.getCountByDeploymentId(deploymentId);
        if (countByDeploymentId > 0) {
            deleteProcessInstancesCheckBox.setValue(true);
            allInstancesContextHelp.setTooltipText(messageBundle.getMessage("deleteAllRunningInstances.tooltip"));
        }
        deleteProcessInstancesCheckBox.setEnabled(countByDeploymentId == 0);
    }

    @Subscribe("okBtn")
    protected void onActivateBtnClick(ClickEvent<Button> event) {
        boolean deleteAllRelatedInstances = BooleanUtils.isTrue(deleteProcessInstancesCheckBox.getValue());
        boolean skipCustomListeners = BooleanUtils.isTrue(skipCustomListenersCheckBox.getValue());
        boolean skipIOMappings = BooleanUtils.isTrue(skipIOMappingsCheckBox.getValue());
        try {
            deploymentService.deleteById(deploymentId, deleteAllRelatedInstances, skipCustomListeners, skipIOMappings);
        } catch (Exception e) {
            if (e instanceof RemoteProcessEngineException processEngineException) {
                log.error("Unable to delete deployment", e);

                notifications.create(StringUtils.defaultIfEmpty(processEngineException.getResponseMessage(), e.getMessage()))
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