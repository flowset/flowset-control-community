/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.taskreassign;

import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.entity.UserTaskData;
import io.flowset.control.service.usertask.UserTaskService;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@ViewController("bpm_TaskReassignView")
@ViewDescriptor("task-reassign-view.xml")
@DialogMode(width = "30em")
public class TaskReassignView extends StandardView {
    @Autowired
    protected UserTaskService userTaskService;
    @Autowired
    protected Notifications notifications;
    @ViewComponent
    protected MessageBundle messageBundle;
    @ViewComponent
    protected TypedTextField<String> newAssigneeField;

    protected List<String> taskIdList = new ArrayList<>();

    @Subscribe
    public void onInit(final InitEvent event) {
        addClassNames(LumoUtility.Padding.Top.SMALL, LumoUtility.Gap.XSMALL);
    }

    public void setTaskDataList(Collection<UserTaskData> userTaskDataList) {
        if (userTaskDataList != null) {
            this.taskIdList = userTaskDataList.stream()
                    .map(UserTaskData::getId)
                    .collect(Collectors.toList());
        }
    }

    @Subscribe("reassignAction")
    public void onReassignAction(ActionPerformedEvent event) {
        String newAssignee = newAssigneeField.getValue();
        if (StringUtils.isBlank(newAssignee)) {
            newAssignee = null;
        }
        for (String taskId : taskIdList) {
            userTaskService.setAssignee(taskId, newAssignee);
        }
        close(StandardOutcome.SAVE);
    }

    @Subscribe("closeAction")
    public void onCloseAction(ActionPerformedEvent event) {
        close(StandardOutcome.CLOSE);
    }
}