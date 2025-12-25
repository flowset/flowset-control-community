/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.bpmengine;

import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.engine.BpmEngine;
import org.springframework.beans.factory.annotation.Autowired;


@Route(value = "bpm/engines", layout = DefaultMainViewParent.class)
@ViewController(id = "BpmEngine.list")
@ViewDescriptor(path = "bpm-engine-list-view.xml")
@LookupComponent("bpmEnginesDataGrid")
@DialogMode(width = "64em")
public class BpmEngineListView extends StandardListView<BpmEngine> {
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected ViewNavigators viewNavigators;

    @Autowired
    protected Messages messages;
    @ViewComponent
    protected DataGrid<BpmEngine> bpmEnginesDataGrid;
    @ViewComponent
    protected MessageBundle messageBundle;
    @ViewComponent
    protected CollectionLoader<BpmEngine> bpmEnginesDl;
    @Autowired
    protected Fragments fragments;

    @Supply(to = "bpmEnginesDataGrid.actions", subject = "renderer")
    protected Renderer<BpmEngine> bpmEnginesDataGridActionsRenderer() {
        return new ComponentRenderer<>(engine -> {
            BpmEngineListActionsFragment bpmEngineListActionsFragment = fragments.create(this, BpmEngineListActionsFragment.class);
            bpmEngineListActionsFragment.setItem(engine);
            bpmEngineListActionsFragment.setSourceDataGrid(bpmEnginesDataGrid);

            return bpmEngineListActionsFragment;
        });
    }
}