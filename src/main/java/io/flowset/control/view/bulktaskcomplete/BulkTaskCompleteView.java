/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.bulktaskcomplete;


import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
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
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Objects;

@Route(value = "bulk-task-complete", layout = DefaultMainViewParent.class)
@ViewController("BulkTaskCompleteView")
@ViewDescriptor("bulk-task-complete-view.xml")
@DialogMode(width = "50em")
public class BulkTaskCompleteView extends StandardView {
    @ViewComponent
    protected CollectionContainer<UserTaskData> userTasksDc;

    protected Collection<UserTaskData> userTasks;
    @Autowired
    protected DialogWindows dialogWindows;
    @ViewComponent
    protected DataGrid<VariableInstanceData> variableGrid;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected UserTaskService userTaskService;
    @ViewComponent
    protected CollectionContainer<VariableInstanceData> variableDc;
    @Autowired
    protected Notifications notifications;
    @ViewComponent
    protected MessageBundle messageBundle;

    @SuppressWarnings("LombokSetterMayBeUsed")
    public void setUserTasks(Collection<UserTaskData> userTasks) {
        this.userTasks = userTasks;
    }

    @Subscribe
    public void onInit(final InitEvent event) {
        addClassNames(LumoUtility.Padding.Top.NONE,
                LumoUtility.Padding.Left.LARGE,
                LumoUtility.Padding.Right.LARGE,
                LumoUtility.Padding.Bottom.MEDIUM);
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        userTasksDc.setItems(userTasks);
    }

    @Supply(to = "variableGrid.value", subject = "renderer")
    protected Renderer<VariableInstanceData> variableValueRenderer() {
        return new TextRenderer<>(e -> e.getValue() != null ? e.getValue().toString() : null);
    }

    @Subscribe("completeTasksAction")
    public void onCompleteTasksAction(final ActionPerformedEvent event) {
        userTasksDc.getItems().forEach(userTaskData -> userTaskService.completeTaskById(userTaskData.getTaskId(), variableDc.getItems()));
        notifications.create(messageBundle.getMessage("userTasksCompleted"))
                .withType(Notifications.Type.SUCCESS)
                .show();
        close(StandardOutcome.SAVE);
    }

    @Subscribe("cancelAction")
    public void onCancelAction(final ActionPerformedEvent event) {
        close(StandardOutcome.CLOSE);
    }

    @Subscribe("variableGrid.add")
    public void onVariableGridAdd(final ActionPerformedEvent event) {
        dialogWindows.detail(variableGrid)
                .withViewClass(VariableInstanceDataDetail.class)
                .withViewConfigurer(view -> view.setNewVariable(true))
                .newEntity()
                .withInitializer(variableInstanceData -> {
                    VariableValueInfo variableValueInfo = metadata.create(VariableValueInfo.class);
                    ObjectTypeInfo objectTypeInfo = metadata.create(ObjectTypeInfo.class);
                    variableValueInfo.setObject(objectTypeInfo);
                    variableInstanceData.setValueInfo(variableValueInfo);
                })
                .build()
                .open();
    }

    @Subscribe("variableGrid.edit")
    public void onVariableGridEdit(final ActionPerformedEvent event) {
        dialogWindows.detail(variableGrid)
                .withViewClass(VariableInstanceDataDetail.class)
                .withViewConfigurer(view -> view.setNewVariable(true))
                .editEntity(Objects.requireNonNull(variableGrid.getSingleSelectedItem()))
                .build()
                .open();
    }


}