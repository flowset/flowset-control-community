/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.usertaskdata;

import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.action.ViewProcessDefinitionAction;
import io.flowset.control.action.ViewProcessInstanceAction;
import io.flowset.control.action.usertask.CompleteUserTaskAction;
import io.flowset.control.action.usertask.ReassignUserTaskAction;
import io.flowset.control.entity.UserTaskData;
import io.flowset.control.service.usertask.UserTaskService;
import io.flowset.control.view.alltasks.AllTasksView;
import io.jmix.core.LoadContext;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@Route(value = "bpm/user-task/:id", layout = DefaultMainViewParent.class)
@ViewController("bpm_UserTaskData.detail")
@ViewDescriptor("user-task-data-detail-view.xml")
@EditedEntityContainer("userTaskDataDc")
@DialogMode(maxWidth = "80em", minWidth = "40em", width = "auto")
public class UserTaskDataDetailView extends StandardDetailView<UserTaskData> {

    @Autowired
    protected UserTaskService userTaskService;
    @ViewComponent
    protected TypedTextField<String> delegationStateField;
    @ViewComponent
    protected TypedDateTimePicker<Date> createTimeField;
    @ViewComponent
    protected TypedDateTimePicker<Date> lastUpdatedField;
    @ViewComponent
    protected TypedDateTimePicker<Date> startTimeField;
    @ViewComponent
    protected TypedDateTimePicker<Date> endTimeField;
    @ViewComponent
    protected TypedTextField<String> formKeyField;
    @ViewComponent
    protected TypedTextField<String> processDefinitionIdField;
    @ViewComponent
    protected TypedTextField<String> processInstanceIdField;
    @ViewComponent
    protected ReassignUserTaskAction reassignAction;
    @ViewComponent
    protected CompleteUserTaskAction completeAction;
    @ViewComponent
    protected JmixButton viewProcessInstance;
    @ViewComponent
    protected JmixButton viewProcessDefinition;
    @ViewComponent
    protected ViewProcessInstanceAction viewProcessInstanceAction;
    @ViewComponent
    protected ViewProcessDefinitionAction viewProcessDefinitionAction;

    @Subscribe
    public void onInit(final InitEvent event) {
        addClassNames(LumoUtility.Padding.Top.XSMALL);
        if (event.getSource() instanceof AllTasksView) {
            processDefinitionIdField.setVisible(true);
            processInstanceIdField.setVisible(true);
        }
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        reassignAction.setUserTask(getEditedEntity());
        reassignAction.setAfterSaveHandler(() -> close(StandardOutcome.SAVE));
        completeAction.setUserTask(getEditedEntity());
        completeAction.setAfterSaveHandler(() -> close(StandardOutcome.SAVE));

        Date endTime = getEditedEntity().getEndTime();
        if (endTime != null) {
            delegationStateField.setVisible(false);
            createTimeField.setVisible(false);
            lastUpdatedField.setVisible(false);
            formKeyField.setVisible(false);
            reassignAction.setVisible(false);
            completeAction.setVisible(false);
        } else {
            boolean suspended = BooleanUtils.isTrue(getEditedEntity().getSuspended());
            completeAction.setVisible(!suspended);
            reassignAction.setVisible(!suspended);
            startTimeField.setVisible(false);
            endTimeField.setVisible(false);
        }

        viewProcessInstanceAction.setEntityId(getEditedEntity().getProcessInstanceId());
        viewProcessDefinitionAction.setEntityId(getEditedEntity().getProcessDefinitionId());
    }

    @Install(to = "userTaskDataDl", target = Target.DATA_LOADER)
    protected UserTaskData customerDlLoadDelegate(final LoadContext<UserTaskData> loadContext) {
        String taskId = (String) loadContext.getId();
        return userTaskService.findTaskById(taskId);
    }

}
