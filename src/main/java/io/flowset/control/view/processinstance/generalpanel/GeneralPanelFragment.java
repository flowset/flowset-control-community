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
import io.flowset.control.action.ViewProcessDefinitionAction;
import io.flowset.control.action.ViewProcessInstanceAction;
import io.flowset.control.action.processinstance.ActivateProcessInstanceAction;
import io.flowset.control.action.processinstance.MigrateProcessInstanceAction;
import io.flowset.control.action.processinstance.SuspendProcessInstanceAction;
import io.flowset.control.action.processinstance.TerminateProcessInstanceAction;
import io.flowset.control.view.util.ComponentHelper;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.entity.processinstance.ProcessInstanceState;
import io.flowset.control.view.processinstance.ProcessInstanceDetailView;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Autowired;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@FragmentDescriptor("general-panel-fragment.xml")
public class GeneralPanelFragment extends Fragment<FlexLayout> {

    @ViewComponent
    protected InstanceContainer<ProcessInstanceData> processInstanceDataDc;

    @ViewComponent
    protected MessageBundle messageBundle;
    @Autowired
    protected ComponentHelper componentHelper;

    @ViewComponent
    protected TextField processDefinitionField;
    @ViewComponent
    protected DateTimePicker endTimeField;
    @ViewComponent
    protected JmixTextArea deleteReasonField;

    @ViewComponent
    protected JmixButton openSuperProcessInstanceEditorBtn;
    @ViewComponent
    protected JmixButton openRootProcessInstanceEditorBtn;
    @ViewComponent
    protected VerticalLayout upperPanel;
    @ViewComponent
    protected JmixButton infoBtn;
    @ViewComponent
    protected ActivateProcessInstanceAction activateAction;
    @ViewComponent
    protected SuspendProcessInstanceAction suspendAction;
    @ViewComponent
    protected TerminateProcessInstanceAction terminateAction;
    @ViewComponent
    protected MigrateProcessInstanceAction migrateAction;
    @ViewComponent
    protected ViewProcessDefinitionAction viewProcessDefinitionAction;
    @ViewComponent
    protected ViewProcessInstanceAction viewSuperProcessInstanceAction;
    @ViewComponent
    protected ViewProcessInstanceAction viewRootProcessInstanceAction;
    @ViewComponent
    protected VerticalLayout runtimeInstanceActions;
    @ViewComponent
    protected JmixCheckbox externallyTerminatedField;

    @Subscribe(target = Target.HOST_CONTROLLER)
    public void onHostBeforeShow(View.BeforeShowEvent event) {
        ProcessInstanceData processInstanceData = processInstanceDataDc.getItem();

        initProcessDefinitionField(processInstanceData);
        viewProcessDefinitionAction.setEntityId(processInstanceData.getProcessDefinitionId());

        initParentProcessInstanceFields(processInstanceData);

        boolean hasEndTime = processInstanceData.getEndTime() != null;
        endTimeField.setVisible(hasEndTime);
        deleteReasonField.setVisible(hasEndTime);
        externallyTerminatedField.setVisible(hasEndTime);

        initActionButtons();
    }

    protected void setupActions() {
        ProcessInstanceData item = processInstanceDataDc.getItem();
        activateAction.setProcessInstanceData(item);
        activateAction.setAfterSaveHandler(this::reopenProcessInstanceDetailsView);
        suspendAction.setProcessInstanceData(item);
        suspendAction.setAfterSaveHandler(this::reopenProcessInstanceDetailsView);
        terminateAction.setProcessInstanceData(item);
        terminateAction.setAfterSaveHandler(this::reopenProcessInstanceDetailsView);
        migrateAction.setProcessInstanceData(item);
        migrateAction.setAfterSaveHandler(this::reopenProcessInstanceDetailsView);
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

    protected void initParentProcessInstanceFields(ProcessInstanceData processInstanceData) {
        String superProcessInstanceId = processInstanceData.getSuperProcessInstanceId();
        viewSuperProcessInstanceAction.setEntityId(superProcessInstanceId);

        openSuperProcessInstanceEditorBtn.setVisible(superProcessInstanceId != null &&
                !Strings.CI.equals(processInstanceData.getInstanceId(), superProcessInstanceId));

        String rootInstanceId = processInstanceData.getRootProcessInstanceId();
        viewRootProcessInstanceAction.setEntityId(rootInstanceId);
        openRootProcessInstanceEditorBtn.setVisible(rootInstanceId != null &&
                !Strings.CI.equals(processInstanceData.getInstanceId(), rootInstanceId));
    }

    protected void initActionButtons() {
        setupActions();

        ProcessInstanceData item = processInstanceDataDc.getItem();
        if (item.getState() == ProcessInstanceState.COMPLETED) {
            runtimeInstanceActions.setVisible(false);
        } else {
            boolean suspended = Boolean.TRUE.equals(item.getSuspended());
            activateAction.setVisible(suspended);
            suspendAction.setVisible(!suspended);
        }
    }

    @Subscribe("refreshAction")
    public void onRefreshAction(final ActionPerformedEvent event) {
        reopenProcessInstanceDetailsView();
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
