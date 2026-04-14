/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.batch.jobstatistics;

import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.SortDirection;
import io.flowset.control.entity.batch.BatchStatisticsData;
import io.flowset.control.entity.filter.JobFilter;
import io.flowset.control.entity.job.JobData;
import io.flowset.control.service.job.JobLoadContext;
import io.flowset.control.service.job.JobService;
import io.jmix.core.DataLoadContext;
import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

@FragmentDescriptor("batch-job-statistics-fragment.xml")
public class BatchJobStatisticsFragment extends Fragment<VerticalLayout> {

    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected JobService jobService;

    @ViewComponent
    protected InstanceContainer<JobFilter> jobFilterDc;
    @ViewComponent
    protected CollectionLoader<JobData> jobsDl;

    @ViewComponent
    protected DataGrid<JobData> jobsDataGrid;
    @ViewComponent
    protected Span totalJobsValue;
    @ViewComponent
    protected Span completedJobsValue;
    @ViewComponent
    protected Span completedJobsPercentValue;
    @ViewComponent
    protected Span failedJobsValue;
    @ViewComponent
    protected Span remainingJobsValue;

    protected String batchJobDefinitionId;

    public void setBatchJobDefinitionId(String batchJobDefinitionId) {
        this.batchJobDefinitionId = batchJobDefinitionId;
    }


    @Subscribe(target = Target.HOST_CONTROLLER)
    public void onHostInit(final View.InitEvent event) {
        jobsDataGrid.sort(Collections.singletonList(new GridSortOrder<>(
                jobsDataGrid.getColumnByKey("createTime"), SortDirection.DESCENDING)));

        initJobFilter();
    }

    public void refresh() {
        jobFilterDc.getItem().setJobDefinitionId(batchJobDefinitionId);
        jobsDl.load();
    }

    @Install(to = "jobsPagination", subject = "totalCountDelegate")
    public Integer jobsPaginationTotalCountDelegate(DataLoadContext dataLoadContext) {
        return (int) jobService.getCount(jobFilterDc.getItemOrNull());
    }

    @Install(to = "jobsDl", target = Target.DATA_LOADER)
    public List<JobData> jobsDlLoadDelegate(LoadContext<JobData> loadContext) {
        LoadContext.Query query = loadContext.getQuery();
        JobLoadContext context = new JobLoadContext()
                .setFilter(jobFilterDc.getItemOrNull());

        if (query != null) {
            context.setFirstResult(query.getFirstResult())
                    .setMaxResults(query.getMaxResults())
                    .setSort(query.getSort());
        }
        return jobService.findAll(context);
    }

    @Subscribe(id = "batchStatisticsDc", target = Target.DATA_CONTAINER)
    protected void onBatchStatisticsDataDcItemChange(InstanceContainer.ItemChangeEvent<BatchStatisticsData> event) {
        BatchStatisticsData item = event.getItem();
        if (item == null) {
            return;
        }

        int totalJobs = item.getTotalJobs();
        int completedJobs = item.getCompletedJobs();
        int percent = totalJobs > 0 ? (int) Math.round((completedJobs / (double) totalJobs) * 100) : 0;

        totalJobsValue.setText(String.valueOf(totalJobs));
        completedJobsValue.setText(String.valueOf(completedJobs));
        completedJobsPercentValue.setText(percent + "%");
        failedJobsValue.setText(String.valueOf(item.getFailedJobs()));
        remainingJobsValue.setText(String.valueOf(item.getRemainingJobs()));
    }

    @Install(to = "jobsDataGrid.retries", subject = "partNameGenerator")
    public String jobsDataGridRetriesPartNameGenerator(final JobData jobData) {
        return jobData.getRetries() != null && jobData.getRetries() == 0 ? "error-cell" : null;
    }

    protected void initJobFilter() {
        JobFilter jobFilter = dataManager.create(JobFilter.class);
        jobFilter.setJobDefinitionId(batchJobDefinitionId);
        jobFilterDc.setItem(jobFilter);
    }
}