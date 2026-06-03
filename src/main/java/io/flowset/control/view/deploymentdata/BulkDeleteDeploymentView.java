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
import io.flowset.control.entity.deployment.DeploymentData;
import io.flowset.control.exception.RemoteProcessEngineException;
import io.flowset.control.service.deployment.DeploymentService;
import io.flowset.control.service.processinstance.ProcessInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

@ViewController("bpm_BulkDeleteDeployment")
@ViewDescriptor("bulk-delete-deployment-view.xml")
@DialogMode(width = "35em")
@Slf4j
public class BulkDeleteDeploymentView extends StandardView {

    @Autowired
    protected Notifications notifications;
    @Autowired
    protected ProcessInstanceService processInstanceService;
    @Autowired
    protected DeploymentService deploymentService;

    @ViewComponent
    protected MessageBundle messageBundle;
    @ViewComponent
    protected JmixCheckbox deleteProcessInstancesCheckBox;
    @ViewComponent
    protected Icon allInstancesContextHelp;
    @ViewComponent
    protected JmixCheckbox skipCustomListenersCheckBox;
    @ViewComponent
    protected JmixCheckbox skipIOMappingsCheckBox;

    protected Collection<DeploymentData> deployments;

    @Subscribe
    public void onInit(final InitEvent event) {
        onInit();
    }

    @SuppressWarnings("LombokSetterMayBeUsed")
    public void setDeployments(Collection<DeploymentData> deployments) {
        this.deployments = deployments;
    }

    protected void onInit() {
        addClassNames(LumoUtility.Padding.Top.NONE, LumoUtility.Padding.Left.LARGE);
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        deleteProcessInstancesCheckBox.setValue(false);

        boolean hasRunningInstances = false;
        for (DeploymentData deploymentData : deployments) {
            long countByDeploymentId = processInstanceService.getCountByDeploymentId(
                    deploymentData.getDeploymentId());
            if (countByDeploymentId > 0) {
                hasRunningInstances = true;
                break;
            }
        }

        if (hasRunningInstances) {
            deleteProcessInstancesCheckBox.setValue(true);
            allInstancesContextHelp.setTooltipText(messageBundle.getMessage("bulkDeleteAllRunningInstances.tooltip"));
        }
        deleteProcessInstancesCheckBox.setEnabled(!hasRunningInstances);
    }

    @Subscribe("okBtn")
    protected void onOkBtnClick(ClickEvent<Button> event) {
        boolean deleteAllRelatedInstances = BooleanUtils.isTrue(deleteProcessInstancesCheckBox.getValue());
        boolean skipCustomListeners = BooleanUtils.isTrue(skipCustomListenersCheckBox.getValue());
        boolean skipIOMappings = BooleanUtils.isTrue(skipIOMappingsCheckBox.getValue());
        try {
            for (DeploymentData deploymentData : deployments) {
                deploymentService.deleteById(deploymentData.getId(), deleteAllRelatedInstances, skipCustomListeners,
                        skipIOMappings);
            }

            notifications.create(messageBundle.getMessage("deploymentsDeleted"))
                    .withType(Notifications.Type.SUCCESS)
                    .show();
        } catch (Exception e) {
            if (e instanceof RemoteProcessEngineException processEngineException) {
                log.error("Unable to delete deployments", e);

                String errorReason;
                String responseMessage = processEngineException.getResponseMessage();
                if (StringUtils.isNotEmpty(responseMessage)) {
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