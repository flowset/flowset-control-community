/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.incidentsstatistics;


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

@Route(value = "incidents-statistics", layout = DefaultMainViewParent.class)
@ViewController("IncidentsStatisticsView")
@ViewDescriptor("incidents-statistics-view.xml")
@DialogMode(width = "37em", maxHeight = "30em")
public class IncidentsStatisticsView extends StandardView {
    protected List<KeyValueEntity> incidentsStatistics;
    @ViewComponent
    protected KeyValueCollectionContainer statisticsDc;

    @ViewComponent
    protected DataGrid<Object> incidentsDataGrid;

    public void setStatistics(List<KeyValueEntity> groupedStatisticsList) {
        this.incidentsStatistics = groupedStatisticsList;
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        statisticsDc.setItems(incidentsStatistics);
        setDefaultSort();
    }

    @Subscribe(id = "closeBtn", subject = "clickListener")
    public void onCloseBtnClick(final ClickEvent<JmixButton> event) {
        close(StandardOutcome.CLOSE);
    }


    protected void setDefaultSort() {
        List<GridSortOrder<Object>> gridSortOrders = Collections.singletonList(new GridSortOrder<>(incidentsDataGrid.getColumnByKey("process"),
                SortDirection.ASCENDING));
        incidentsDataGrid.sort(gridSortOrders);
    }
}