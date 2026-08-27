/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processinstance.runtime;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import io.flowset.control.action.externaltask.RetryExternalTaskGridAction;
import io.flowset.control.view.externaltask.column.ExternalTaskIdColumnFragment;
import io.jmix.core.DataLoadContext;
import io.jmix.core.LoadContext;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.flowui.*;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.ExternalTaskData;
import io.flowset.control.entity.filter.ExternalTaskFilter;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.service.externaltask.ExternalTaskLoadContext;
import io.flowset.control.service.externaltask.ExternalTaskService;
import io.flowset.control.view.processinstance.event.ExternalTaskCountUpdateEvent;
import io.flowset.control.view.processinstance.event.ExternalTaskRetriesUpdateEvent;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@FragmentDescriptor("external-tasks-tab-fragment.xml")
public class ExternalTasksTabFragment extends Fragment<VerticalLayout> {
    @Autowired
    protected ExternalTaskService externalTaskService;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected UiEventPublisher uiEventPublisher;
    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected ViewNavigators viewNavigators;
    @Autowired
    protected Dialogs dialogs;
    @ViewComponent
    protected MessageBundle messageBundle;
    @Autowired
    protected Messages messages;

    @ViewComponent
    protected CollectionLoader<ExternalTaskData> runtimeExternalTasksDl;

    @ViewComponent
    protected CollectionContainer<ExternalTaskData> runtimeExternalTasksDc;

    @ViewComponent
    protected DataGrid<ExternalTaskData> runtimeExternalTasksGrid;
    @ViewComponent("runtimeExternalTasksGrid.retry")
    protected RetryExternalTaskGridAction retryAction;

    @ViewComponent
    protected InstanceContainer<ProcessInstanceData> processInstanceDataDc;
    protected ExternalTaskFilter filter;
    protected String selectedActivityId;
    protected boolean initialized = false;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Fragments fragments;

    @SuppressWarnings("LombokSetterMayBeUsed")
    public void setSelectedActivityId(String selectedActivityId) {
        this.selectedActivityId = selectedActivityId;
    }

    public void refreshIfChanged(String selectedActivityId) {
        if (!initialized) {
            this.filter = metadata.create(ExternalTaskFilter.class);
            this.filter.setProcessInstanceId(processInstanceDataDc.getItem().getId());
            retryAction.setAfterSaveHandler(this::reloadExternalTasks);
            runtimeExternalTasksDl.load();
            this.initialized = true;
            return;
        }

        if (!Strings.CS.equals(this.selectedActivityId, selectedActivityId)) {
            this.selectedActivityId = selectedActivityId;
            filter.setActivityId(selectedActivityId);
            runtimeExternalTasksDl.load();
        }
    }

    @Install(to = "runtimeExternalTasksDl", target = Target.DATA_LOADER)
    protected List<ExternalTaskData> runtimeExternalTasksDlLoadDelegate(final LoadContext<ExternalTaskData> loadContext) {
        LoadContext.Query query = loadContext.getQuery();
        ExternalTaskLoadContext context = new ExternalTaskLoadContext()
                .setFilter(filter);

        if (query != null) {
            context.setFirstResult(query.getFirstResult())
                    .setMaxResults(query.getMaxResults())
                    .setSort(query.getSort());
        }
        return externalTaskService.findRunningTasks(context);
    }

    @Install(to = "pagination", subject = "totalCountDelegate")
    protected Integer paginationTotalCountDelegate(final DataLoadContext dataLoadContext) {
        long count = externalTaskService.getRunningTasksCount(filter);

        uiEventPublisher.publishEventForCurrentUI(new ExternalTaskCountUpdateEvent(this, count));
        return (int) count;
    }

    @Subscribe("runtimeExternalTasksGrid.edit")
    public void onRuntimeExternalTasksEdit(ActionPerformedEvent event) {
        ExternalTaskData selectedTask = runtimeExternalTasksGrid.getSingleSelectedItem();
        if (selectedTask == null) {
            return;
        }
        dialogWindows.detail(getCurrentView(), ExternalTaskData.class)
                .editEntity(selectedTask)
                .withAfterCloseListener(afterCloseEvent -> {
                    if (afterCloseEvent.closedWith(StandardOutcome.SAVE)) {
                        reloadExternalTasks();
                    }
                })
                .build()
                .open();
    }

    @Install(to = "runtimeExternalTasksGrid.retries", subject = "partNameGenerator")
    protected String runtimeExternalTasksGridRetriesPartNameGenerator(final ExternalTaskData externalTaskData) {
        return externalTaskData.getRetries() != null && externalTaskData.getRetries() == 0 ? "error-cell" : null;
    }

    @Supply(to = "runtimeExternalTasksGrid.externalTaskId", subject = "renderer")
    private Renderer<ExternalTaskData> runtimeExternalTasksGridExternalTaskIdRenderer() {
        return new ComponentRenderer<>(externalTask -> {
            ExternalTaskIdColumnFragment fragment = fragments.create(this, ExternalTaskIdColumnFragment.class);
            fragment.setOpenMode(OpenMode.DIALOG);
            fragment.setItem(externalTask);
            fragment.setAfterSaveHandler(this::reloadExternalTasks);
            return fragment;
        });
    }

    protected void reloadExternalTasks() {
        runtimeExternalTasksDl.load();
        uiEventPublisher.publishEventForCurrentUI(new ExternalTaskRetriesUpdateEvent(this));
    }
}
