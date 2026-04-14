/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.batch.column;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.progressbar.ProgressBar;
import io.flowset.control.entity.batch.BatchStatisticsData;
import io.jmix.core.metamodel.datatype.DatatypeFormatter;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.FragmentRenderer;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.view.ViewComponent;
import org.springframework.beans.factory.annotation.Autowired;

@FragmentDescriptor("batch-progress-column-fragment.xml")
@RendererItemContainer("batchStatisticsDc")
public class BatchProgressColumnFragment extends FragmentRenderer<Div, BatchStatisticsData> {

    @Autowired
    protected DatatypeFormatter datatypeFormatter;

    @ViewComponent
    protected ProgressBar progressBar;
    @ViewComponent
    protected Span progressLabel;

    @Override
    public void setItem(BatchStatisticsData item) {
        super.setItem(item);

        int totalJobs = item.getTotalJobs() != null && item.getTotalJobs() > 0 ? item.getTotalJobs() : 0;
        int completedJobs = item.getCompletedJobs() != null ? item.getCompletedJobs() : 0;

        int percent;
        if (totalJobs == 0) {
            percent = 0;
        } else {
            double ratio = completedJobs / (double) totalJobs;
            percent = (int) Math.round(ratio * 100);
        }
        progressLabel.setText(datatypeFormatter.formatInteger(percent) + "%");
        progressBar.setValue(percent);
    }
}