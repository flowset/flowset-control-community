/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.batch;

import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import io.flowset.control.entity.batch.BatchStatisticsData;
import io.flowset.control.service.batch.BatchService;
import io.flowset.control.service.job.JobService;
import io.flowset.control.view.batch.jobstatistics.BatchJobStatisticsFragment;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@Route(value = "bpm/batches/:id/statistics", layout = DefaultMainViewParent.class)
@ViewController("BatchStatisticsData.detail")
@ViewDescriptor("batch-statistics-detail-view.xml")
@EditedEntityContainer("batchStatisticsDc")
@DialogMode(minWidth = "65em", width = "80%", resizable = true)
@PrimaryDetailView(BatchStatisticsData.class)
public class BatchStatisticsDetailView extends StandardDetailView<BatchStatisticsData> {

    @Autowired
    protected BatchService batchService;
    @Autowired
    protected JobService jobService;
    @Autowired
    protected Metadata metadata;
    @ViewComponent
    protected InstanceLoader<BatchStatisticsData> batchStatisticsDl;
    @ViewComponent
    protected InstanceContainer<BatchStatisticsData> batchStatisticsDc;

    @ViewComponent
    protected MessageBundle messageBundle;
    @ViewComponent
    protected BatchJobStatisticsFragment jobsFragment;
    @ViewComponent
    protected HorizontalLayout detailActions;

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        batchStatisticsDl.load();

        jobsFragment.setBatchJobDefinitionId(getEditedEntity().getBatchJobDefinitionId());
        jobsFragment.refresh();

        boolean openedInDialog = UiComponentUtils.isComponentAttachedToDialog(this);
        detailActions.setJustifyContentMode(openedInDialog ? FlexComponent.JustifyContentMode.END
                : FlexComponent.JustifyContentMode.START);
    }

    @Install(to = "batchStatisticsDl", target = Target.DATA_LOADER)
    protected BatchStatisticsData batchStatisticsDlLoadDelegate(LoadContext<BatchStatisticsData> loadContext) {
        return batchService.findBatchStatisticsById(Objects.requireNonNull(loadContext.getId()).toString());
    }
}
