/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processdefinition;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.view.processinstancemigration.ProcessInstanceMigrationView;
import io.flowset.control.view.startprocess.StartProcessWithVariableView;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButton;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButtonItem;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.HasLoader;
import io.jmix.flowui.view.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

@FragmentDescriptor("process-definition-list-item-actions-fragment.xml")
public class ProcessDefinitionListItemActionsFragment extends Fragment<HorizontalLayout> {
    @ViewComponent
    protected CollectionContainer<ProcessDefinitionData> processDefinitionsDc;

    @Autowired
    protected ViewNavigators viewNavigators;
    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected Messages messages;
    @ViewComponent
    protected MessageBundle messageBundle;

    @ViewComponent
    protected JmixButton startProcessBtn;
    @ViewComponent
    protected Button activateBtn;
    @ViewComponent
    protected DropdownButton suspendedProcessActions;
    @ViewComponent
    protected DropdownButton activeProcessActions;

    protected ProcessDefinitionData processDefinition;

    public void setProcessDefinition(ProcessDefinitionData processDefinition) {
        this.processDefinition = processDefinition;
        updateButtonsVisibility();
    }

    @Subscribe
    public void onReady(ReadyEvent event) {
        activeProcessActions.addClassName(LumoUtility.Margin.End.AUTO);
        suspendedProcessActions.addClassName(LumoUtility.Margin.End.AUTO);
        startProcessBtn.addClassNames(LumoUtility.Height.MEDIUM);
        activateBtn.addClassNames(LumoUtility.Height.MEDIUM);
        activeProcessActions.addClassNames(LumoUtility.Height.MEDIUM);
        suspendedProcessActions.addClassNames(LumoUtility.Height.MEDIUM);
    }

    public void updateButtonsVisibility() {
        boolean suspended = Boolean.TRUE.equals(processDefinition.getSuspended());
        activateBtn.setVisible(suspended);
        startProcessBtn.setVisible(!suspended);

        suspendedProcessActions.setVisible(suspended);
        activeProcessActions.setVisible(!suspended);
    }

    @Subscribe(id = "startProcessBtn", subject = "clickListener")
    public void onStartProcessBtnClick(final ClickEvent<JmixButton> event) {
        dialogWindows.detail(getView(), ProcessDefinitionData.class)
                .withViewClass(StartProcessWithVariableView.class)
                .editEntity(processDefinition)
                .withAfterCloseListener(e -> {
                    if (e.closedWith(StandardOutcome.SAVE)) {
                        notifications.create(messages.formatMessage(getClass(), "startProcess.success",
                                        StringUtils.defaultIfEmpty(processDefinition.getName(), processDefinition.getKey())))
                                .withType(Notifications.Type.SUCCESS)
                                .build()
                                .open();
                    }
                })
                .open();
    }

    @Subscribe("activateBtn")
    protected void onActivateBtnClick(ClickEvent<Button> event) {
        dialogWindows.view(getView(), ActivateProcessDefinitionView.class)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                        reloadProcessDefinitions();
                    }
                })
                .withViewConfigurer(view -> view.setProcessDefinitionId(processDefinition.getId()))
                .build()
                .open();
    }

    @Subscribe("activeProcessActions.delete")
    public void onActiveProcessActionsRemoveClick(final DropdownButtonItem.ClickEvent event) {
        openDeleteConfirmDialog();
    }

    @Subscribe("activeProcessActions.suspend")
    public void onActiveProcessActionsSuspendClick(final DropdownButtonItem.ClickEvent event) {
        openSuspendConfirmDialog();
    }

    @Subscribe("activeProcessActions.migrate")
    public void onActiveProcessActionsMigrateClick(final DropdownButtonItem.ClickEvent event) {
        openProcessInstanceMigrationDialog();
    }

    @Subscribe("suspendedProcessActions.delete")
    public void onSuspendedProcessActionsRemoveClick(final DropdownButtonItem.ClickEvent event) {
        openDeleteConfirmDialog();
    }

    @Subscribe("suspendedProcessActions.migrate")
    public void onSuspendedProcessActionsMigrateClick(final DropdownButtonItem.ClickEvent event) {
        openProcessInstanceMigrationDialog();
    }


    protected void openDeleteConfirmDialog() {
        dialogWindows.view(getView(), DeleteProcessDefinitionView.class)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                        reloadProcessDefinitions();
                    }
                })
                .withViewConfigurer(view -> view.setProcessDefinitionId(processDefinition.getId()))
                .build()
                .open();
    }

    protected void openSuspendConfirmDialog() {
        dialogWindows.view(getView(), SuspendProcessDefinitionView.class)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                        reloadProcessDefinitions();
                    }
                })
                .withViewConfigurer(view -> view.setProcessDefinitionId(processDefinition.getId()))
                .build()
                .open();
    }

    protected void openProcessInstanceMigrationDialog() {
        dialogWindows.view(getView(), ProcessInstanceMigrationView.class)
                .withAfterCloseListener(afterCloseEvent -> {
                    if (afterCloseEvent.closedWith(StandardOutcome.SAVE)) {
                        notifications.create(messageBundle.getMessage("processInstancesMigrationStarted"))
                                .withType(Notifications.Type.SUCCESS)
                                .show();
                    }
                })
                .withViewConfigurer(view -> view.setProcessDefinitionData(processDefinition))
                .build()
                .open();
    }

    protected void reloadProcessDefinitions() {
        if (processDefinitionsDc instanceof HasLoader container) {
            DataLoader loader = container.getLoader();
            if (loader != null) {
                loader.load();
            }
        }
    }

    protected View<?> getView() {
        return (View<?>) getParentController();
    }

}
