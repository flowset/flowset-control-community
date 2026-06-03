/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processdefinition;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.action.processdefinition.ActivateProcessDefinitionAction;
import io.flowset.control.action.processdefinition.DeleteProcessDefinitionAction;
import io.flowset.control.action.processdefinition.MigrateProcessDefinitionAction;
import io.flowset.control.action.processdefinition.StartProcessDefinitionAction;
import io.flowset.control.action.processdefinition.SuspendProcessDefinitionAction;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButton;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButtonItem;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.HasLoader;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;

@FragmentDescriptor("process-definition-list-item-actions-fragment.xml")
public class ProcessDefinitionListItemActionsFragment extends Fragment<HorizontalLayout> {
    @ViewComponent
    protected CollectionContainer<ProcessDefinitionData> processDefinitionsDc;

    @ViewComponent
    protected JmixButton startProcessBtn;
    @ViewComponent
    protected JmixButton activateBtn;
    @ViewComponent
    protected DropdownButton processActions;
    @ViewComponent
    protected StartProcessDefinitionAction startProcessAction;
    @ViewComponent
    protected ActivateProcessDefinitionAction activateAction;
    @ViewComponent
    protected SuspendProcessDefinitionAction suspendAction;
    @ViewComponent
    protected MigrateProcessDefinitionAction migrateAction;
    @ViewComponent
    protected DeleteProcessDefinitionAction deleteAction;

    protected ProcessDefinitionData processDefinition;

    public void setProcessDefinition(ProcessDefinitionData processDefinition) {
        this.processDefinition = processDefinition;
        configureActions();
    }

    @Subscribe
    public void onReady(ReadyEvent event) {
        processActions.addClassName(LumoUtility.Margin.End.AUTO);
        startProcessBtn.addClassNames(LumoUtility.Height.MEDIUM);
        activateBtn.addClassNames(LumoUtility.Height.MEDIUM);
        processActions.addClassNames(LumoUtility.Height.MEDIUM);
    }

    protected void configureActions() {
        startProcessAction.setProcessDefinitionData(processDefinition);
        startProcessAction.setAfterSaveHandler(this::reloadProcessDefinitions);

        activateAction.setProcessDefinitionId(processDefinition.getId());
        activateAction.setProcessDefinitionData(processDefinition);
        activateAction.setAfterSaveHandler(this::reloadProcessDefinitions);

        suspendAction.setProcessDefinitionId(processDefinition.getId());
        suspendAction.setProcessDefinitionData(processDefinition);
        suspendAction.setAfterSaveHandler(this::reloadProcessDefinitions);

        migrateAction.setProcessDefinitionData(processDefinition);
        migrateAction.setAfterSaveHandler(this::reloadProcessDefinitions);

        deleteAction.setProcessDefinitionId(processDefinition.getId());
        deleteAction.setAfterSaveHandler(this::reloadProcessDefinitions);

        boolean anyActionVisible = processActions.getItems().stream().anyMatch(DropdownButtonItem::isVisible);
        processActions.setVisible(anyActionVisible);
    }

    protected void reloadProcessDefinitions() {
        if (processDefinitionsDc instanceof HasLoader container) {
            DataLoader loader = container.getLoader();
            if (loader != null) {
                loader.load();
            }
        }
    }
}
