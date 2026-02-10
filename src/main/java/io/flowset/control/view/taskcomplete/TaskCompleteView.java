/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.taskcomplete;


import com.vaadin.flow.router.Route;
import io.flowset.control.entity.UserTaskData;
import io.flowset.control.entity.variable.ObjectTypeInfo;
import io.flowset.control.entity.variable.VariableInstanceData;
import io.flowset.control.entity.variable.VariableValueInfo;
import io.flowset.control.service.usertask.UserTaskService;
import io.flowset.control.view.processvariable.VariableInstanceDataDetail;
import io.jmix.core.Metadata;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@Route(value = "task-complete", layout = DefaultMainViewParent.class)
@ViewController(id = "TaskCompleteView")
@ViewDescriptor(path = "task-complete-view.xml")
@DialogMode(width = "50em")
public class TaskCompleteView extends StandardView {

    @Autowired
    protected UserTaskService userTaskService;
    @ViewComponent
    protected CollectionContainer<VariableInstanceData> variablesDc;
    @Autowired
    protected Notifications notifications;
    @ViewComponent
    protected MessageBundle messageBundle;
    @Autowired
    protected DialogWindows dialogWindows;
    @ViewComponent
    protected DataGrid<VariableInstanceData> variablesGrid;
    @Autowired
    protected Metadata metadata;

    protected UserTaskData userTask;

    public void setUserTask(UserTaskData userTask) {
        this.userTask = userTask;
    }

    @Subscribe("completeTaskAction")
    public void onCompleteTaskAction(final ActionPerformedEvent event) {
        userTaskService.completeTaskById(userTask.getTaskId(), variablesDc.getItems());
        String taskName = StringUtils.defaultIfEmpty(userTask.getName(), userTask.getTaskDefinitionKey());
        notifications.create(messageBundle.formatMessage("userTaskCompleted", taskName))
                .withType(Notifications.Type.SUCCESS)
                .show();
        close(StandardOutcome.SAVE);
    }

    @Subscribe("variablesGrid.add")
    public void onVariablesGridAdd(final ActionPerformedEvent event) {
        dialogWindows.detail(variablesGrid)
                .withViewClass(VariableInstanceDataDetail.class)
                .withViewConfigurer(view -> view.setNewVariable(true))
                .newEntity()
                .withInitializer(variableInstanceData -> {
                    VariableValueInfo variableValueInfo = metadata.create(VariableValueInfo.class);
                    ObjectTypeInfo objectTypeInfo = metadata.create(ObjectTypeInfo.class);
                    variableValueInfo.setObject(objectTypeInfo);
                    variableInstanceData.setValueInfo(variableValueInfo);
                })
                .open();
    }

    @Subscribe("variablesGrid.edit")
    public void onVariablesGridEdit(final ActionPerformedEvent event) {
        dialogWindows.detail(variablesGrid)
                .withViewClass(VariableInstanceDataDetail.class)
                .withViewConfigurer(view -> view.setNewVariable(true))
                .editEntity(Objects.requireNonNull(variablesGrid.getSingleSelectedItem()))
                .open();
    }
}