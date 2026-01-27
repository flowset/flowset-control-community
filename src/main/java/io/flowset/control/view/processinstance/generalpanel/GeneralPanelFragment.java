/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processinstance.generalpanel;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.RouteParameters;
import io.flowset.control.view.util.ComponentHelper;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.entity.processinstance.ProcessInstanceState;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import io.flowset.control.service.processinstance.ProcessInstanceService;
import io.flowset.control.view.processinstance.ActivateProcessInstanceView;
import io.flowset.control.view.processinstance.ProcessInstanceDetailView;
import io.flowset.control.view.processinstance.SuspendProcessInstanceView;
import io.flowset.control.view.processinstancemigration.ProcessInstanceMigrationView;
import io.flowset.control.view.processinstanceterminate.ProcessInstanceTerminateView;
import org.springframework.beans.factory.annotation.Autowired;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@FragmentDescriptor("general-panel-fragment.xml")
public class GeneralPanelFragment extends Fragment<FlexLayout> {

    @ViewComponent
    protected InstanceContainer<ProcessInstanceData> processInstanceDataDc;

    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected ViewNavigators viewNavigators;
    @Autowired
    protected Metadata metadata;
    @ViewComponent
    protected MessageBundle messageBundle;
    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected Messages messages;
    @Autowired
    protected ComponentHelper componentHelper;

    @Autowired
    protected ProcessInstanceService processInstanceService;
    @Autowired
    protected ProcessDefinitionService processDefinitionService;

    @ViewComponent
    protected TextField processDefinitionField;
    @ViewComponent
    protected DateTimePicker endTimeField;
    @ViewComponent
    protected JmixTextArea deleteReason;

    @ViewComponent
    protected VerticalLayout upperPanel;
    @ViewComponent
    protected JmixButton infoBtn;
    @ViewComponent
    protected Button suspendBtn;
    @ViewComponent
    protected Button activateBtn;
    @ViewComponent
    protected VerticalLayout runtimeInstanceActions;
    @ViewComponent
    protected JmixCheckbox externallyTerminatedField;

    @Subscribe(target = Target.HOST_CONTROLLER)
    public void onHostBeforeShow(View.BeforeShowEvent event) {
        ProcessInstanceData processInstanceData = processInstanceDataDc.getItem();

        initProcessDefinitionField(processInstanceData);

        boolean hasEndTime = processInstanceData.getEndTime() != null;
        endTimeField.setVisible(hasEndTime);
        deleteReason.setVisible(hasEndTime);
        externallyTerminatedField.setVisible(hasEndTime);

        initActionButtons();
    }

    protected void initProcessDefinitionField(ProcessInstanceData processInstanceData) {
        String value;
        if (processInstanceData.getProcessDefinitionVersion() != null) {
            value = componentHelper.getProcessLabel(processInstanceData.getProcessDefinitionKey(), processInstanceData.getProcessDefinitionVersion());
        } else {
            value = processInstanceData.getProcessDefinitionId();
        }
        processDefinitionField.setValue(value);
    }

    protected void initActionButtons() {
        ProcessInstanceData item = processInstanceDataDc.getItem();
        if (item.getState() == ProcessInstanceState.COMPLETED) {
            runtimeInstanceActions.setVisible(false);
        } else {
            boolean suspended = Boolean.TRUE.equals(processInstanceDataDc.getItem().getSuspended());
            activateBtn.setVisible(suspended);
            suspendBtn.setVisible(!suspended);
        }
    }

    @Subscribe("refreshAction")
    public void onRefreshAction(final ActionPerformedEvent event) {
        reopenProcessInstanceDetailsView();
    }

    @Subscribe("activateAction")
    public void onActivateAction(final ActionPerformedEvent event) {
        ProcessInstanceData processInstanceData = processInstanceDataDc.getItem();

        DialogWindow<ActivateProcessInstanceView> dialogWindow = dialogWindows.view(getCurrentView(), ActivateProcessInstanceView.class)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                        reopenProcessInstanceDetailsView();
                    }
                })
                .build();

        ActivateProcessInstanceView activateProcessInstanceView = dialogWindow.getView();
        activateProcessInstanceView.setProcessInstanceData(processInstanceData);

        dialogWindow.open();
    }

    @Subscribe("openProcessDefinitionEditorBtn")
    public void openProcessDefinitionEditor(ClickEvent<Button> event) {
        ProcessDefinitionData processDefinitionData = processDefinitionService.getById(processInstanceDataDc.getItem().getProcessDefinitionId());
        if (processDefinitionData != null) {
            viewNavigators.detailView(getCurrentView(), ProcessDefinitionData.class)
                    .withRouteParameters(new RouteParameters("id", processDefinitionData.getId()))
                    .withBackwardNavigation(true)
                    .navigate();
        } else {
            notifications.create(messageBundle.getMessage(("processDoesNotExist")))
                    .withType(Notifications.Type.WARNING)
                    .show();
        }
    }

    @Subscribe("suspendAction")
    public void onSuspendAction(final ActionPerformedEvent event) {
        DialogWindow<SuspendProcessInstanceView> dialogWindow = dialogWindows.view(getCurrentView(), SuspendProcessInstanceView.class)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                        reopenProcessInstanceDetailsView();
                    }
                })
                .build();

        SuspendProcessInstanceView suspendProcessInstanceView = dialogWindow.getView();
        suspendProcessInstanceView.setProcessInstanceData(processInstanceDataDc.getItem());

        dialogWindow.open();
    }

    @Subscribe("terminateAction")
    public void onTerminateAction(final ActionPerformedEvent event) {
        DialogWindow<ProcessInstanceTerminateView> dialog = dialogWindows.view(getCurrentView(), ProcessInstanceTerminateView.class)
                .withAfterCloseListener(afterCloseEvent -> {
                    if (afterCloseEvent.closedWith(StandardOutcome.SAVE)) {
                        notifications.create(messageBundle.getMessage("processInstanceTerminated"))
                                .withType(Notifications.Type.SUCCESS)
                                .show();
                        reopenProcessInstanceDetailsView();
                    }
                })
                .build();

        dialog.getView().setProcessInstanceData(processInstanceDataDc.getItem());
        dialog.open();
    }

    @Subscribe("migrateAction")
    public void onMigrateAction(final ActionPerformedEvent event) {
        ProcessDefinitionData processDefinitionData = metadata.create(ProcessDefinitionData.class);
        ProcessInstanceData processInstanceData = processInstanceDataDc.getItem();
        processDefinitionData.setId(processInstanceData.getProcessDefinitionId());
        processDefinitionData.setKey(processInstanceData.getProcessDefinitionKey());
        processDefinitionData.setVersion(processInstanceData.getProcessDefinitionVersion());

        DialogWindow<ProcessInstanceMigrationView> dialog =
                dialogWindows.view(getCurrentView(), ProcessInstanceMigrationView.class)
                        .withAfterCloseListener(afterCloseEvent -> {
                            if (afterCloseEvent.closedWith(StandardOutcome.SAVE)) {
                                notifications.create(messageBundle.getMessage("processInstanceMigrated"))
                                        .withType(Notifications.Type.SUCCESS)
                                        .show();

                                reopenProcessInstanceDetailsView();
                            }
                        })
                        .build();

        dialog.getView().setProcessDefinitionData(processDefinitionData);
        dialog.getView().setProcessInstanceData(processInstanceData);
        dialog.open();
    }


    @Subscribe("infoBtn")
    protected void onInfoButtonClickBtnClick(ClickEvent<Button> event) {
        upperPanel.setVisible(!upperPanel.isVisible());

        if (upperPanel.isVisible()) {
            infoBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            infoBtn.setTitle(messageBundle.getMessage("hideProcessInstanceDetails"));
        } else {
            infoBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            infoBtn.setTitle(messageBundle.getMessage("viewProcessInstanceDetails"));
        }

    }

    protected void reopenProcessInstanceDetailsView() {
        ProcessInstanceDetailView view = (ProcessInstanceDetailView) getCurrentView();
        view.reopenView();
    }
}
