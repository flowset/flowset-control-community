/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processinstance.runtime;

import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.event.SortEvent;
import io.flowset.control.entity.job.JobState;
import io.flowset.control.view.job.ActivateJobView;
import io.flowset.control.view.job.SuspendJobView;
import io.jmix.core.DataLoadContext;
import io.jmix.core.LoadContext;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.datatype.DatatypeFormatter;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.UiEventPublisher;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.filter.JobFilter;
import io.flowset.control.entity.job.JobData;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.service.job.JobLoadContext;
import io.flowset.control.service.job.JobService;
import io.flowset.control.view.incidentdata.RetryJobView;
import io.flowset.control.view.processinstance.event.JobCountUpdateEvent;
import io.flowset.control.view.processinstance.event.JobRetriesUpdateEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@FragmentDescriptor("jobs-tab-fragment.xml")
public class JobsTabFragment extends Fragment<VerticalLayout> {
    @Autowired
    protected JobService jobService;
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
    @Autowired
    protected DatatypeFormatter datatypeFormatter;

    @ViewComponent
    protected CollectionLoader<JobData> runtimeJobsDl;

    @ViewComponent
    protected CollectionContainer<JobData> runtimeJobsDc;

    @ViewComponent
    protected DataGrid<JobData> runtimeJobsGrid;

    @ViewComponent
    protected InstanceContainer<ProcessInstanceData> processInstanceDataDc;
    protected JobFilter filter;
    protected boolean initialized = false;


    public void refreshIfRequired() {
        if (!initialized) {
            this.filter = metadata.create(JobFilter.class);
            this.filter.setProcessInstanceId(processInstanceDataDc.getItem().getId());

            runtimeJobsDl.load();
            this.initialized = true;
        }
    }

    @Install(to = "runtimeJobsDl", target = Target.DATA_LOADER)
    protected List<JobData> runtimeJobsDlLoadDelegate(final LoadContext<JobData> loadContext) {
        LoadContext.Query query = loadContext.getQuery();
        JobLoadContext context = new JobLoadContext()
                .setFilter(filter);

        if (query != null) {
            context.setFirstResult(query.getFirstResult())
                    .setMaxResults(query.getMaxResults())
                    .setSort(query.getSort());
        }
        return jobService.findAll(context);
    }

    @Install(to = "jobsPagination", subject = "totalCountDelegate")
    protected Integer jobsPaginationTotalCountDelegate(final DataLoadContext dataLoadContext) {
        long count = jobService.getCount(filter);

        uiEventPublisher.publishEventForCurrentUI(new JobCountUpdateEvent(this, count));
        return (int) count;
    }

    @Subscribe("runtimeJobsGrid.edit")
    public void onRuntimeJobsEdit(ActionPerformedEvent event) {
        JobData selectedJob = runtimeJobsGrid.getSingleSelectedItem();
        if (selectedJob == null) {
            return;
        }
        dialogWindows.detail(getCurrentView(), JobData.class)
                .editEntity(selectedJob)
                .withAfterCloseListener(afterCloseEvent -> {
                    if (afterCloseEvent.closedWith(StandardOutcome.SAVE)) {
                        reloadJobs();
                    }
                })
                .build()
                .open();

    }

    @Subscribe("runtimeJobsGrid")
    public void onRuntimeJobsGridSort(final SortEvent<DataGrid<JobData>, GridSortOrder<DataGrid<JobData>>> event) {
        runtimeJobsDl.load();
    }

    @Install(to = "runtimeJobsGrid.retries", subject = "partNameGenerator")
    protected String runtimeJobsGridRetriesPartNameGenerator(final JobData jobData) {
        return jobData.getRetries() != null && jobData.getRetries() == 0 ? "error-cell" : null;
    }

    @Install(to = "runtimeJobsGrid.retry", subject = "enabledRule")
    protected boolean runtimeJobsGridRetryEnabledRule() {
        JobData selectedJob = runtimeJobsGrid.getSingleSelectedItem();
        return selectedJob != null && selectedJob.getRetries() != null && selectedJob.getRetries() == 0;
    }

    @Subscribe("runtimeJobsGrid.retry")
    public void onRuntimeJobsGridRetry(final ActionPerformedEvent event) {
        JobData selectedJob = runtimeJobsGrid.getSingleSelectedItem();
        if (selectedJob == null) {
            return;
        }

        DialogWindow<RetryJobView> dialogWindow = dialogWindows.view(getCurrentView(), RetryJobView.class)
                .withAfterCloseListener(afterClose -> {
                    if (afterClose.closedWith(StandardOutcome.SAVE)) {
                        reloadJobs();
                    }
                })
                .build();

        RetryJobView retryJobView = dialogWindow.getView();
        retryJobView.setJobId(selectedJob.getJobId());

        dialogWindow.open();
    }

    protected void reloadJobs() {
        runtimeJobsDl.load();
        uiEventPublisher.publishEventForCurrentUI(new JobRetriesUpdateEvent(this));
    }

    @Install(to = "runtimeJobsGrid.failedActivityId", subject = "tooltipGenerator")
    protected String runtimeJobsGridFailedActivityIdTooltipGenerator(final JobData jobData) {
        return jobData.getFailedActivityId();
    }

    @Install(to = "runtimeJobsGrid.createTime", subject = "tooltipGenerator")
    protected String runtimeJobsGridCreateTimeTooltipGenerator(final JobData jobData) {
        Date createTime = jobData.getCreateTime();
        return datatypeFormatter.formatDateTime(createTime);
    }

    @Subscribe("runtimeJobsGrid.activate")
    public void onRuntimeJobsGridActivate(final ActionPerformedEvent event) {
        JobData selectedItem = runtimeJobsGrid.getSingleSelectedItem();
        if (selectedItem == null) {
            return;
        }

        dialogWindows.view(getCurrentView(), ActivateJobView.class)
                .withViewConfigurer(activateJobView -> activateJobView.setJobId(selectedItem.getJobId()))
                .withAfterCloseListener(afterCloseEvent -> {
                    if (afterCloseEvent.closedWith(StandardOutcome.SAVE)) {
                        reloadJobs();
                    }
                })
                .open();
    }

    @Install(to = "runtimeJobsGrid.activate", subject = "enabledRule")
    private boolean runtimeJobsGridActivateEnabledRule() {
        JobData selectedItem = runtimeJobsGrid.getSingleSelectedItem();
        return selectedItem != null && selectedItem.getState() == JobState.SUSPENDED;
    }

    @Subscribe("runtimeJobsGrid.suspend")
    public void onRuntimeJobsGridSuspend(final ActionPerformedEvent event) {
        JobData selectedItem = runtimeJobsGrid.getSingleSelectedItem();
        if (selectedItem == null) {
            return;
        }

        dialogWindows.view(getCurrentView(), SuspendJobView.class)
                .withViewConfigurer(activateJobView -> activateJobView.setJobId(selectedItem.getJobId()))
                .withAfterCloseListener(afterCloseEvent -> {
                    if (afterCloseEvent.closedWith(StandardOutcome.SAVE)) {
                        reloadJobs();
                    }
                })
                .open();
    }

    @Install(to = "runtimeJobsGrid.suspend", subject = "enabledRule")
    private boolean runtimeJobsGridSuspendEnabledRule() {
        JobData selectedItem = runtimeJobsGrid.getSingleSelectedItem();
        return selectedItem != null && selectedItem.getState() == JobState.ACTIVE;
    }
}
