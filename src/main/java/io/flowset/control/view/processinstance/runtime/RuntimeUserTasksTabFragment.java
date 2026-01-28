/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processinstance.runtime;

import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.event.SortEvent;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import io.flowset.control.view.processinstance.event.UserTaskUpdateEvent;
import io.flowset.control.view.usertaskdata.column.UserTaskIdColumnFragment;
import io.jmix.core.DataLoadContext;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.flowui.*;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.UserTaskData;
import io.flowset.control.entity.filter.UserTaskFilter;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.service.usertask.UserTaskLoadContext;
import io.flowset.control.service.usertask.UserTaskService;
import io.flowset.control.view.processinstance.event.UserTaskCountUpdateEvent;
import io.flowset.control.view.taskreassign.TaskReassignView;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@FragmentDescriptor("runtime-user-tasks-tab-fragment.xml")
public class RuntimeUserTasksTabFragment extends Fragment<VerticalLayout> {
    @Autowired
    protected UserTaskService userTaskService;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected UiEventPublisher uiEventPublisher;
    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected ViewNavigators viewNavigators;
    @Autowired
    protected Fragments fragments;

    @ViewComponent
    protected CollectionLoader<UserTaskData> runtimeUserTasksDl;

    @ViewComponent
    protected DataGrid<UserTaskData> runtimeUserTasksGrid;

    @ViewComponent
    protected InstanceContainer<ProcessInstanceData> processInstanceDataDc;

    protected UserTaskFilter filter;
    protected String selectedActivityInstanceId;
    protected boolean initialized = false;

    @SuppressWarnings("LombokSetterMayBeUsed")
    public void setSelectedActivityInstanceId(String selectedActivityInstanceId) {
        this.selectedActivityInstanceId = selectedActivityInstanceId;
    }

    public void refreshIfChanged(String selectedActivityInstanceId) {
        if (!initialized) {
            this.filter = metadata.create(UserTaskFilter.class);
            filter.setProcessInstanceId(processInstanceDataDc.getItem().getId());
            runtimeUserTasksDl.load();
            this.initialized = true;
            return;
        }

        if (!StringUtils.equals(this.selectedActivityInstanceId, selectedActivityInstanceId)) {
            this.selectedActivityInstanceId = selectedActivityInstanceId;
            filter.setActivityInstanceId(selectedActivityInstanceId);
            runtimeUserTasksDl.load();
        }
    }

    @Subscribe("runtimeUserTasksGrid.view")
    public void onViewAction(final ActionPerformedEvent event) {
        UserTaskData userTaskData = runtimeUserTasksGrid.getSingleSelectedItem();
        if (userTaskData == null) {
            return;
        }
        dialogWindows.detail(getCurrentView(), UserTaskData.class)
                .editEntity(userTaskData)
                .build()
                .open();
    }

    @Install(to = "runtimeUserTasksGrid.reassign", subject = "enabledRule")
    protected boolean runtimeUserTasksGridReassignEnabledRule() {
        UserTaskData selectedTask = runtimeUserTasksGrid.getSingleSelectedItem();
        return selectedTask != null && BooleanUtils.isNotTrue(selectedTask.getSuspended());
    }

    @Subscribe("runtimeUserTasksGrid.reassign")
    public void onReassignAction(final ActionPerformedEvent event) {
        Set<UserTaskData> userTasks = runtimeUserTasksGrid.getSelectedItems();
        if (userTasks.isEmpty()) {
            return;
        }
        dialogWindows.view(getCurrentView(), TaskReassignView.class)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                        runtimeUserTasksDl.load();
                    }
                })
                .withViewConfigurer(taskReassignView -> taskReassignView.setTaskDataList(userTasks))
                .build()
                .open();
    }

    @Install(to = "runtimeUserTasksDl", target = Target.DATA_LOADER)
    protected List<UserTaskData> runtimeUserTasksDlLoadDelegate(final LoadContext<UserTaskData> loadContext) {
        LoadContext.Query query = loadContext.getQuery();
        UserTaskLoadContext context = new UserTaskLoadContext().setFilter(filter);

        if (query != null) {
            context.setFirstResult(query.getFirstResult())
                    .setMaxResults(query.getMaxResults())
                    .setSort(query.getSort());
        }

        return userTaskService.findRuntimeTasks(context);
    }

    @Install(to = "userTasksPagination", subject = "totalCountDelegate")
    protected Integer userTasksPaginationTotalCountDelegate(final DataLoadContext dataLoadContext) {
        long runtimeTasksCount = userTaskService.getRuntimeTasksCount(filter);

        uiEventPublisher.publishEventForCurrentUI(new UserTaskCountUpdateEvent(this, runtimeTasksCount));
        return (int) runtimeTasksCount;
    }

    @Subscribe("runtimeUserTasksGrid")
    public void onRuntimeUserTasksGridGridSort(final SortEvent<DataGrid<UserTaskData>, GridSortOrder<DataGrid<UserTaskData>>> event) {
        runtimeUserTasksDl.load();
    }

    @Supply(to = "runtimeUserTasksGrid.taskId", subject = "renderer")
    private Renderer<UserTaskData> runtimeUserTasksGridTaskIdRenderer() {
        return new ComponentRenderer<>(userTask -> {
            UserTaskIdColumnFragment taskIdFragment = fragments.create(this, UserTaskIdColumnFragment.class);
            taskIdFragment.setItem(userTask);
            taskIdFragment.setAfterSaveCloseListener(() ->
                    uiEventPublisher.publishEventForCurrentUI(new UserTaskUpdateEvent(this)));
            return taskIdFragment;
        });
    }
}
