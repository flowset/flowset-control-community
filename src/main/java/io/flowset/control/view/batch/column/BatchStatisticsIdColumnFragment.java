/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.batch.column;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.flowset.control.entity.batch.BatchStatisticsData;
import io.flowset.control.view.entitydetaillink.EntityDetailLinkFragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import org.springframework.lang.NonNull;

@FragmentDescriptor("batch-statistics-id-column-fragment.xml")
@RendererItemContainer("batchStatisticsDc")
public class BatchStatisticsIdColumnFragment extends EntityDetailLinkFragment<HorizontalLayout, BatchStatisticsData> {
    @ViewComponent
    protected JmixButton idBtn;

    @Override
    public void setItem(@NonNull BatchStatisticsData item) {
        super.setItem(item);

        idBtn.setText(item.getId());
    }

    @Subscribe(id = "idBtn", subject = "clickListener")
    public void onIdBtnClick(final ClickEvent<JmixButton> event) {
        openDetailView(BatchStatisticsData.class);
    }
}
