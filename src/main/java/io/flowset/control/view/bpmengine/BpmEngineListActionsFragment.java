package io.flowset.control.view.bpmengine;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.FragmentRenderer;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.service.engine.EngineService;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;

@FragmentDescriptor("bpm-engine-list-actions-fragment.xml")
@RendererItemContainer("bpmEngineDc")
public class BpmEngineListActionsFragment extends FragmentRenderer<HorizontalLayout, BpmEngine> {

    protected DataGrid<BpmEngine> sourceDataGrid;
    @ViewComponent
    protected JmixButton markAsDefaultBtn;
    @Autowired
    protected Dialogs dialogs;
    @ViewComponent
    protected MessageBundle messageBundle;
    @Autowired
    protected EngineService engineService;
    @ViewComponent
    protected CollectionLoader<Object> bpmEnginesDl;

    public void setSourceDataGrid(DataGrid<BpmEngine> sourceDataGrid) {
        this.sourceDataGrid = sourceDataGrid;
    }

    @Override
    public void setItem(BpmEngine item) {
        super.setItem(item);

        if (BooleanUtils.isNotTrue(item.getIsDefault())) {
            markAsDefaultBtn.setVisible(true);
        }
    }

    @Subscribe(id = "markAsDefaultBtn", subject = "clickListener")
    public void onMarkAsDefaultBtnClick(final ClickEvent<JmixButton> event) {
        dialogs.createOptionDialog()
                .withHeader(messageBundle.getMessage("markAsDefault.header"))
                .withContent(new Html(messageBundle.formatMessage("markAsDefault.text", item.getName())))
                .withActions(new DialogAction(DialogAction.Type.OK)
                                .withVariant(ActionVariant.PRIMARY)
                                .withHandler(actionPerformedEvent -> {
                                    engineService.markAsDefault(item);
                                    bpmEnginesDl.load();
                                }),
                        new DialogAction(DialogAction.Type.CANCEL))
                .open();
    }
}