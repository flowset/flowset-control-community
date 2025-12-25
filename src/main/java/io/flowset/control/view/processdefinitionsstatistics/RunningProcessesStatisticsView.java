/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processdefinitionsstatistics;


import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.Route;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.KeyValueCollectionContainer;
import io.jmix.flowui.view.*;

import java.util.Collections;
import java.util.List;

@Route(value = "running-processes-statistics", layout = DefaultMainViewParent.class)
@ViewController("RunningProcessesStatisticsView")
@ViewDescriptor("running-processes-statistics-view.xml")
@DialogMode(width = "37em", maxHeight = "30em")
public class RunningProcessesStatisticsView extends StandardView {
    protected List<KeyValueEntity> runningInstancesStatistics;

    @ViewComponent
    protected KeyValueCollectionContainer statisticsDc;
    @ViewComponent
    protected DataGrid<Object> runningInstancesStatisticsDataGrid;

    public void setStatistics(List<KeyValueEntity> groupedStatisticsList) {
        this.runningInstancesStatistics = groupedStatisticsList;
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        statisticsDc.setItems(runningInstancesStatistics);
        setDefaultSort();
    }

    @Subscribe(id = "closeBtn", subject = "clickListener")
    public void onCloseBtnClick(final ClickEvent<JmixButton> event) {
        close(StandardOutcome.CLOSE);
    }


    protected void setDefaultSort() {
        List<GridSortOrder<Object>> gridSortOrders = Collections.singletonList(new GridSortOrder<>(runningInstancesStatisticsDataGrid.getColumnByKey("process"),
                SortDirection.ASCENDING));
        runningInstancesStatisticsDataGrid.sort(gridSortOrders);
    }
}