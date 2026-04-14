/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.batch.column;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.flowset.control.entity.batch.BatchState;
import io.flowset.control.entity.batch.BatchStatisticsData;
import io.jmix.core.Messages;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.FragmentRenderer;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.view.ViewComponent;
import org.springframework.beans.factory.annotation.Autowired;

@FragmentDescriptor("batch-state-column-fragment.xml")
@RendererItemContainer("batchStatisticsDc")
public class BatchStateColumnFragment extends FragmentRenderer<HorizontalLayout, BatchStatisticsData> {

    @Autowired
    protected Messages messages;

    @ViewComponent
    protected Span stateBadge;
    @ViewComponent
    protected Icon failedJobsWarningIcon;

    @Override
    public void setItem(BatchStatisticsData item) {
        super.setItem(item);

        BatchState state = item.getState();
        stateBadge.getElement().getThemeList().add(state == BatchState.SUSPENDED ? "warning" : "success");

        failedJobsWarningIcon.setVisible(item.isFailed());
    }
}
