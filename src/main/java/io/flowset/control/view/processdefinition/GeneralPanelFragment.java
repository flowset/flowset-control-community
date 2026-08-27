/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processdefinition;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.flowset.control.action.CopyComponentValueToClipboardAction;
import io.flowset.control.action.deployment.ViewDeploymentAction;
import io.flowset.control.action.processdefinition.ActivateProcessDefinitionAction;
import io.flowset.control.action.processdefinition.DeleteProcessDefinitionAction;
import io.flowset.control.action.processdefinition.MigrateProcessDefinitionAction;
import io.flowset.control.action.processdefinition.StartProcessDefinitionAction;
import io.flowset.control.action.processdefinition.SuspendProcessDefinitionAction;
import io.flowset.control.entity.deployment.DeploymentData;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.service.deployment.DeploymentService;
import io.flowset.control.view.processdefinition.event.ReloadSelectedProcess;
import io.jmix.flowui.UiEventPublisher;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.HasLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Target;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewComponent;
import org.springframework.beans.factory.annotation.Autowired;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;
import static io.flowset.control.view.processdefinition.ProcessDefinitionDetailView.REMOVE_PROCESS_DEFINITION_CLOSE_ACTION;

@FragmentDescriptor("general-panel-fragment.xml")
public class GeneralPanelFragment extends Fragment<FlexLayout> {

    @ViewComponent
    protected VerticalLayout upperPanel;
    @ViewComponent
    protected JmixButton infoBtn;
    @ViewComponent
    protected InstanceContainer<ProcessDefinitionData> processDefinitionDataDc;

    @ViewComponent
    protected TypedTextField<String> keyField;
    @ViewComponent
    protected TypedTextField<String> idField;
    @ViewComponent
    protected MessageBundle messageBundle;
    @Autowired
    protected DeploymentService deploymentService;
    @ViewComponent
    protected TypedTextField<Object> deploymentIdField;
    @ViewComponent
    protected TypedTextField<Object> deploymentSourceField;
    @ViewComponent
    protected TypedDateTimePicker<Comparable> deploymentTimeField;
    @ViewComponent
    protected CollectionContainer<ProcessInstanceData> processInstanceDataDc;
    @ViewComponent
    protected CopyComponentValueToClipboardAction copyIdAction;
    @ViewComponent
    protected CopyComponentValueToClipboardAction copyKeyAction;
    @Autowired
    protected UiEventPublisher uiEventPublisher;

    @ViewComponent
    protected StartProcessDefinitionAction startProcessAction;
    @ViewComponent
    protected SuspendProcessDefinitionAction suspendAction;
    @ViewComponent
    protected ActivateProcessDefinitionAction activateAction;
    @ViewComponent
    protected MigrateProcessDefinitionAction migrateAction;
    @ViewComponent
    protected DeleteProcessDefinitionAction deleteAction;
    @ViewComponent
    protected ViewDeploymentAction viewDeploymentAction;

    @Subscribe(target = Target.HOST_CONTROLLER)
    public void onHostBeforeShow(View.BeforeShowEvent event) {
        initActions();

        copyIdAction.setTarget(idField);
        copyKeyAction.setTarget(keyField);

        infoBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    }

    public void refresh() {
        initActions();
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
            infoBtn.setTitle(messageBundle.getMessage("hideProcessInformation.title"));
        } else {
            infoBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            infoBtn.setTitle(messageBundle.getMessage("viewProcessInformation.title"));
        }

    }

    @Subscribe("refreshAction")
    public void onRefreshAction(final ActionPerformedEvent event) {
        reloadProcessDefinition();
    }

    protected void initActions() {
        ProcessDefinitionData item = processDefinitionDataDc.getItem();

        startProcessAction.setProcessDefinitionData(item);
        startProcessAction.setAfterSaveHandler(this::reloadProcessDefinition);

        suspendAction.setProcessDefinitionId(item.getId());
        suspendAction.setProcessDefinitionData(item);
        suspendAction.setAfterSaveHandler(this::reloadProcessDefinition);

        activateAction.setProcessDefinitionId(item.getId());
        activateAction.setProcessDefinitionData(item);
        activateAction.setAfterSaveHandler(this::reloadProcessDefinition);

        migrateAction.setProcessDefinitionData(item);
        migrateAction.setAfterSaveHandler(this::reloadProcessInstances);

        deleteAction.setProcessDefinitionId(item.getId());
        deleteAction.setAfterSaveHandler(() -> getCurrentView().close(REMOVE_PROCESS_DEFINITION_CLOSE_ACTION));

        viewDeploymentAction.setDeploymentId(item.getDeploymentId());
    }

    protected void reloadProcessInstances() {
        if (processInstanceDataDc instanceof HasLoader container) {
            DataLoader loader = container.getLoader();
            if (loader != null) {
                loader.load();
            }
        }
    }

    protected void initDeploymentData() {
        DeploymentData deployment = deploymentService.findById(processDefinitionDataDc.getItem().getDeploymentId());
        if (deployment != null) {
            deploymentIdField.setTypedValue(deployment.getDeploymentId());
            deploymentSourceField.setTypedValue(deployment.getSource());
            deploymentTimeField.setTypedValue(deployment.getDeploymentTime());
        }
    }

    protected void reloadProcessDefinition() {
        uiEventPublisher.publishEventForCurrentUI(new ReloadSelectedProcess(this));
    }
}
