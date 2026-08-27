/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.batch;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.Route;
import io.flowset.control.entity.batch.BatchData;
import io.flowset.control.entity.batch.BatchState;
import io.flowset.control.entity.batch.BatchStatisticsData;
import io.flowset.control.service.batch.BatchService;
import io.flowset.control.service.job.JobService;
import io.flowset.control.view.batch.jobstatistics.BatchJobStatisticsFragment;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@Route(value = "bpm/batches/:id", layout = DefaultMainViewParent.class)
@ViewController("BatchData.detail")
@ViewDescriptor("batch-data-detail-view.xml")
@EditedEntityContainer("batchDataDc")
@DialogMode(resizable = true)
@PrimaryDetailView(BatchData.class)
public class BatchDataDetailView extends StandardDetailView<BatchData> {

    @ViewComponent
    protected MessageBundle messageBundle;
    @Autowired
    protected BatchService batchService;
    @Autowired
    protected JobService jobService;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected UiComponents uiComponents;

    @ViewComponent
    protected InstanceLoader<BatchData> batchDataDl;
    @ViewComponent
    protected InstanceContainer<BatchStatisticsData> batchStatisticsDc;

    @ViewComponent
    protected VerticalLayout statisticsBox;
    @ViewComponent
    protected Div mainInfoBox;
    @ViewComponent
    protected TypedTextField<Integer> jobsCreatedField;
    @ViewComponent
    protected BatchJobStatisticsFragment jobsFragment;
    @ViewComponent
    protected HorizontalLayout detailActions;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        batchDataDl.load();

        BatchState state = getEditedEntity().getState();
        if (state != BatchState.COMPLETED) {
            BatchStatisticsData batchStatistics = batchService.findBatchStatisticsById(getEditedEntity().getId());
            batchStatisticsDc.setItem(batchStatistics);

            jobsFragment.setBatchJobDefinitionId(getEditedEntity().getBatchJobDefinitionId());
            jobsFragment.refresh();

            jobsCreatedField.setVisible(true);
            statisticsBox.setVisible(true);
            updateActiveBatchLayout();
        } else {
            updateCompletedBatchLayout();
        }

        boolean openedInDialog = UiComponentUtils.isComponentAttachedToDialog(this);
        detailActions.setJustifyContentMode(openedInDialog ? FlexComponent.JustifyContentMode.END
                : FlexComponent.JustifyContentMode.START);
    }

    protected void updateCompletedBatchLayout() {
        Dialog dialog = UiComponentUtils.findDialog(this);
        if (dialog != null) {
            dialog.setWidth("65em");
        }
    }

    protected void updateActiveBatchLayout() {
        SplitLayout splitLayout = uiComponents.create(SplitLayout.class);
        splitLayout.setWidthFull();
        splitLayout.setHeightFull();
        splitLayout.setSplitterPosition(30);
        splitLayout.addThemeNames("splitter-spacing");

        splitLayout.addToPrimary(mainInfoBox);
        splitLayout.addToSecondary(statisticsBox);

        getContent().addComponentAsFirst(splitLayout);

        Dialog dialog = UiComponentUtils.findDialog(this);
        if (dialog != null) {
            dialog.setWidth("80%");
            dialog.setMinWidth("65em");
        }
    }

    @Install(to = "batchDataDl", target = Target.DATA_LOADER)
    public BatchData batchDataDlLoadDelegate(LoadContext<BatchData> loadContext) {
        return batchService.getById(Objects.requireNonNull(loadContext.getId()).toString());
    }
}
