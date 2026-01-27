/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.usertaskdata;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.entity.UserTaskData;
import io.flowset.control.service.usertask.UserTaskService;
import io.flowset.control.view.alltasks.AllTasksView;
import io.flowset.control.view.processdefinition.ProcessDefinitionDetailView;
import io.flowset.control.view.processinstance.ProcessInstanceDetailView;
import io.jmix.core.LoadContext;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
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


    @Subscribe
    public void onInit(final InitEvent event) {
        addClassNames(LumoUtility.Padding.Top.XSMALL);
        if(event.getSource() instanceof AllTasksView) {
            processDefinitionIdField.setVisible(true);
            processInstanceIdField.setVisible(true);
        }
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {

        Date endTime = getEditedEntity().getEndTime();
        if (endTime != null) {
            delegationStateField.setVisible(false);
            createTimeField.setVisible(false);
            lastUpdatedField.setVisible(false);
            formKeyField.setVisible(false);
        } else {
            startTimeField.setVisible(false);
            endTimeField.setVisible(false);
        }
    }

    @Install(to = "userTaskDataDl", target = Target.DATA_LOADER)
    protected UserTaskData customerDlLoadDelegate(final LoadContext<UserTaskData> loadContext) {
        String taskId = (String) loadContext.getId();
        return userTaskService.findTaskById(taskId);
    }

    @Subscribe(id = "viewProcessInstance", subject = "clickListener")
    public void onViewProcessInstanceClick(final ClickEvent<JmixButton> event) {
        RouterLink routerLink = new RouterLink(ProcessInstanceDetailView.class, new RouteParameters("id", getEditedEntity().getProcessInstanceId()));
        getUI().ifPresent(ui -> ui.getPage().open(routerLink.getHref()));
    }

    @Subscribe(id = "viewProcessDefinition", subject = "clickListener")
    public void onViewProcessDefinitionClick(final ClickEvent<JmixButton> event) {
        RouterLink routerLink = new RouterLink(ProcessDefinitionDetailView.class, new RouteParameters("id", getEditedEntity().getProcessDefinitionId()));
        getUI().ifPresent(ui -> ui.getPage().open(routerLink.getHref()));
    }
}
