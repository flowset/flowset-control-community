package io.flowset.control.view.bpmengine;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.flowset.control.action.engine.MarkAsDefaultEngineAction;
import io.flowset.control.entity.engine.BpmEngine;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.FragmentRenderer;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import org.apache.commons.lang3.BooleanUtils;

@FragmentDescriptor("bpm-engine-list-actions-fragment.xml")
@RendererItemContainer("bpmEngineDc")
public class BpmEngineListActionsFragment extends FragmentRenderer<HorizontalLayout, BpmEngine> {

    protected DataGrid<BpmEngine> sourceDataGrid;
    @ViewComponent
    protected JmixButton markAsDefaultBtn;
    @ViewComponent
    protected MarkAsDefaultEngineAction markAsDefaultAction;
    @ViewComponent
    protected CollectionLoader<Object> bpmEnginesDl;

    public void setSourceDataGrid(DataGrid<BpmEngine> sourceDataGrid) {
        this.sourceDataGrid = sourceDataGrid;
    }

    @Subscribe
    public void onAttachEvent(final AttachEvent event) {
        markAsDefaultBtn.setIcon(new SvgIcon("icons/bookmark_check.svg"));
    }

    @Override
    public void setItem(BpmEngine item) {
        super.setItem(item);

        if (BooleanUtils.isNotTrue(item.getIsDefault())) {
            markAsDefaultBtn.setVisible(true);
            markAsDefaultAction.setEngine(item);
            markAsDefaultAction.setAfterSaveHandler(bpmEnginesDl::load);
        }
    }
}