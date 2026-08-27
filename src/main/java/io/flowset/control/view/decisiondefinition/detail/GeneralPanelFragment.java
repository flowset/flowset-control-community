/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.decisiondefinition.detail;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.flowset.control.action.CopyComponentValueToClipboardAction;
import io.flowset.control.action.deployment.ViewDeploymentAction;
import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.entity.deployment.DeploymentData;
import io.flowset.control.service.deployment.DeploymentService;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@FragmentDescriptor("general-panel-fragment.xml")
public class GeneralPanelFragment extends Fragment<FlexLayout> {
    @ViewComponent
    protected MessageBundle messageBundle;
    @Autowired
    protected DeploymentService deploymentService;

    @ViewComponent
    protected InstanceContainer<DecisionDefinitionData> decisionDefinitionDc;

    @ViewComponent
    protected VerticalLayout upperPanel;
    @ViewComponent
    protected JmixButton infoBtn;

    @ViewComponent
    protected TypedTextField<String> keyField;
    @ViewComponent
    protected TypedTextField<String> idField;
    @ViewComponent
    protected TypedTextField<Object> deploymentIdField;
    @ViewComponent
    protected TypedTextField<Object> deploymentSourceField;
    @ViewComponent
    protected TypedDateTimePicker<Comparable> deploymentTimeField;
    @ViewComponent
    protected CopyComponentValueToClipboardAction copyIdAction;
    @ViewComponent
    protected CopyComponentValueToClipboardAction copyKeyAction;
    @ViewComponent
    protected ViewDeploymentAction viewDeploymentAction;

    @Subscribe(target = Target.HOST_CONTROLLER)
    public void onHostBeforeShow(View.BeforeShowEvent event) {
        copyIdAction.setTarget(idField);
        copyKeyAction.setTarget(keyField);
    }

    public void refresh() {
        initDeploymentData();
    }

    @Subscribe("infoBtn")
    protected void onInfoButtonClickBtnClick(ClickEvent<Button> event) {
        boolean active = upperPanel.hasClassName("active");
        if (active) {
            upperPanel.removeClassName("active");
        } else {
            upperPanel.addClassName("active");
        }

        if (upperPanel.hasClassName("active")) {
            infoBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            infoBtn.setTitle(messageBundle.getMessage("hideDecisionInformation.title"));
        } else {
            infoBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            infoBtn.setTitle(messageBundle.getMessage("viewDecisionInformation.title"));
        }

    }

    protected void initDeploymentData() {
        String deploymentId = decisionDefinitionDc.getItem().getDeploymentId();
        viewDeploymentAction.setDeploymentId(deploymentId);

        DeploymentData deployment = deploymentService.findById(deploymentId);
        if (deployment != null) {
            deploymentIdField.setTypedValue(deployment.getDeploymentId());
            deploymentSourceField.setTypedValue(deployment.getSource());
            deploymentTimeField.setTypedValue(deployment.getDeploymentTime());
        }
    }
}