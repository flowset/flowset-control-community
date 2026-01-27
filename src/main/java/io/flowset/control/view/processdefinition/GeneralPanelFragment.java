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
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiEventPublisher;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.HasLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Target;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewComponent;
import io.flowset.control.action.CopyComponentValueToClipboardAction;
import io.flowset.control.entity.deployment.DeploymentData;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.service.deployment.DeploymentService;
import io.flowset.control.view.deploymentdata.DeploymentDetailView;
import io.flowset.control.view.processdefinition.event.ReloadSelectedProcess;
import io.flowset.control.view.processinstancemigration.ProcessInstanceMigrationView;
import io.flowset.control.view.startprocess.StartProcessWithVariableView;
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
    protected JmixButton activateBtn;
    @ViewComponent
    protected JmixButton suspendBtn;

    @Autowired
    protected ViewNavigators viewNavigators;
    @ViewComponent
    protected TypedTextField<String> keyField;
    @Autowired
    protected Notifications notifications;
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
    protected JmixButton startProcessBtn;
    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected Messages messages;
    @ViewComponent
    protected CollectionContainer<ProcessInstanceData> processInstanceDataDc;
    @ViewComponent
    protected CopyComponentValueToClipboardAction copyIdAction;
    @ViewComponent
    protected CopyComponentValueToClipboardAction copyKeyAction;
    @Autowired
    protected UiEventPublisher uiEventPublisher;

    @Subscribe(target = Target.HOST_CONTROLLER)
    public void onHostBeforeShow(View.BeforeShowEvent event) {
        initActionButtons();

        copyIdAction.setTarget(idField);
        copyKeyAction.setTarget(keyField);

        infoBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    }

    public void refresh() {
        initActionButtons();
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

    @Subscribe(id = "viewDeployment", subject = "clickListener")
    public void onViewDeploymentClick(final ClickEvent<JmixButton> event) {
        viewNavigators.detailView(getCurrentView(), DeploymentData.class)
                .withViewClass(DeploymentDetailView.class)
                .withRouteParameters(new RouteParameters("id", processDefinitionDataDc.getItem().getDeploymentId()))
                .withBackwardNavigation(true)
                .navigate();
    }

    @Subscribe(id = "startProcessBtn", subject = "clickListener")
    public void onStartProcessBtnClick(final ClickEvent<JmixButton> event) {
        dialogWindows.detail(getCurrentView(), ProcessDefinitionData.class)
                .withViewClass(StartProcessWithVariableView.class)
                .editEntity(processDefinitionDataDc.getItem())
                .withAfterCloseListener(e -> {
                    if (e.closedWith(StandardOutcome.SAVE)) {
                        reloadProcessInstances();
                        notifications.create(messages.formatMessage(ProcessDefinitionDetailView.class, "startProcess.success",
                                        e.getView().getEditedEntity().getProcessDefinitionId()))
                                .withType(Notifications.Type.SUCCESS)
                                .build()
                                .open();
                    }
                })
                .open();
    }

    @Subscribe(id = "migrateBtn", subject = "clickListener")
    protected void onMigrateBtnClick(final ClickEvent<JmixButton> event) {
        dialogWindows.view(getCurrentView(), ProcessInstanceMigrationView.class)
                .withAfterCloseListener(afterCloseEvent -> {
                    if (afterCloseEvent.closedWith(StandardOutcome.SAVE)) {
                        reloadProcessInstances();
                        notifications.create(messageBundle.getMessage("processInstancesMigrationStarted"))
                                .withType(Notifications.Type.SUCCESS)
                                .show();
                    }
                })
                .withViewConfigurer(view -> view.setProcessDefinitionData(processDefinitionDataDc.getItem()))
                .build()
                .open();
    }

    @Subscribe(id = "suspendBtn", subject = "clickListener")
    protected void onSuspendBtnClick(final ClickEvent<JmixButton> event) {
        dialogWindows.view(getCurrentView(), SuspendProcessDefinitionView.class)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                        reloadProcessDefinition();
                        initActionButtons();
                    }
                })
                .withViewConfigurer(view -> view.setProcessDefinitionId(processDefinitionDataDc.getItem().getId()))
                .build()
                .open();
    }

    @Subscribe(id = "deleteBtn", subject = "clickListener")
    protected void onDeleteBtnClick(final ClickEvent<JmixButton> event) {
        dialogWindows.view(getCurrentView(), DeleteProcessDefinitionView.class)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                        getCurrentView().close(REMOVE_PROCESS_DEFINITION_CLOSE_ACTION);
                    }
                })
                .withViewConfigurer(view -> view.setProcessDefinitionId(processDefinitionDataDc.getItem().getId()))
                .build()
                .open();
    }

    @Subscribe(id = "activateBtn", subject = "clickListener")
    protected void onActivateBtnClick(final ClickEvent<JmixButton> event) {
        dialogWindows.view(getCurrentView(), ActivateProcessDefinitionView.class)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                        reloadProcessDefinition();
                        initActionButtons();
                    }
                })
                .withViewConfigurer(view -> view.setProcessDefinitionId(processDefinitionDataDc.getItem().getId()))
                .build()
                .open();
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

    protected void initActionButtons() {
        ProcessDefinitionData item = processDefinitionDataDc.getItem();
        boolean suspended = Boolean.TRUE.equals(item.getSuspended());
        activateBtn.setVisible(suspended);
        suspendBtn.setVisible(!suspended);
        startProcessBtn.setVisible(!suspended);
    }


    protected void reloadProcessDefinition() {
        uiEventPublisher.publishEventForCurrentUI(new ReloadSelectedProcess(this));
    }
}