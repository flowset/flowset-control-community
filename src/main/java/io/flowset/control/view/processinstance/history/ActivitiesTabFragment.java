/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processinstance.history;

import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.event.SortEvent;
import io.jmix.core.DataLoadContext;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiEventPublisher;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Target;
import io.jmix.flowui.view.ViewComponent;
import io.flowset.control.entity.activity.HistoricActivityInstanceData;
import io.flowset.control.entity.filter.ActivityFilter;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.service.activity.ActivityLoadContext;
import io.flowset.control.service.activity.ActivityService;
import io.flowset.control.view.processinstance.event.HistoryActivityCountUpdateEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@FragmentDescriptor("activities-tab-fragment.xml")
public class ActivitiesTabFragment extends Fragment<VerticalLayout> implements HasRefresh {
    @Autowired
    protected ActivityService activityService;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected UiEventPublisher uiEventPublisher;
    @Autowired
    protected DialogWindows dialogWindows;

    @ViewComponent
    protected CollectionLoader<HistoricActivityInstanceData> historicActivityInstancesDl;

    @ViewComponent
    protected DataGrid<HistoricActivityInstanceData> historicActivityInstancesGrid;

    @ViewComponent
    protected InstanceContainer<ProcessInstanceData> processInstanceDataDc;

    protected ActivityFilter filter;

    protected boolean initialized = false;

    public void refreshIfRequired() {
        if (!initialized) {
            this.filter = metadata.create(ActivityFilter.class);
            filter.setProcessInstanceId(processInstanceDataDc.getItem().getId());
            historicActivityInstancesDl.load();
            this.initialized = true;
        }
    }

    @Subscribe("historicActivityInstancesGrid.view")
    public void onViewAction(final ActionPerformedEvent event) {
        HistoricActivityInstanceData activityInstanceData = historicActivityInstancesGrid.getSingleSelectedItem();
        if (activityInstanceData == null) {
            return;
        }
        dialogWindows.detail(getCurrentView(), HistoricActivityInstanceData.class)
                .withAfterCloseListener(closeEvent -> historicActivityInstancesDl.load())
                .editEntity(activityInstanceData)
                .build()
                .open();
    }


    @Install(to = "historicActivityInstancesDl", target = Target.DATA_LOADER)
    protected List<HistoricActivityInstanceData> historicActivityInstancesDlLoadDelegate(final LoadContext<HistoricActivityInstanceData> loadContext) {
        LoadContext.Query query = loadContext.getQuery();
        ActivityLoadContext context = new ActivityLoadContext().setFilter(filter);

        if (query != null) {
            context.setFirstResult(query.getFirstResult())
                    .setMaxResults(query.getMaxResults())
                    .setSort(query.getSort());
        }

        return activityService.findAllHistoryActivities(context);
    }

    @Install(to = "historicActivityInstancesPagination", subject = "totalCountDelegate")
    protected Integer historicActivityInstancesPaginationTotalCountDelegate(final DataLoadContext dataLoadContext) {
        long historyActivitiesCount = activityService.getHistoryActivitiesCount(filter);

        uiEventPublisher.publishEventForCurrentUI(new HistoryActivityCountUpdateEvent(this, historyActivitiesCount));
        return (int) historyActivitiesCount;
    }

    @Subscribe("historicActivityInstancesGrid")
    public void onHistoricActivityInstancesGridGridSort(final SortEvent<DataGrid<HistoricActivityInstanceData>, GridSortOrder<DataGrid<HistoricActivityInstanceData>>> event) {
        historicActivityInstancesDl.load();
    }

}
