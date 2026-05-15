/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processdefinition.detail;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import io.flowset.control.entity.processdefinition.CalledProcessReferenceData;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.uicomponent.viewer.handler.CallActivityOverlayClickHandler;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Supply;
import io.jmix.flowui.view.ViewComponent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

@FragmentDescriptor("called-processes-tab-fragment.xml")
public class CalledProcessesTabFragment extends Fragment<VerticalLayout> {

    private static final String LATEST_VERSION_BINDING = "latest";

    @Autowired
    protected CallActivityOverlayClickHandler callActivityClickHandler;
    @ViewComponent
    protected DataGrid<CalledProcessReferenceData> calledProcessesGrid;
    @ViewComponent
    protected InstanceContainer<ProcessDefinitionData> processDefinitionDataDc;

    @Supply(to = "calledProcessesGrid.binding", subject = "renderer")
    protected Renderer<CalledProcessReferenceData> calledProcessesGridBindingRenderer() {
        return new TextRenderer<>(calledProcessReferenceData ->
                StringUtils.defaultIfEmpty(calledProcessReferenceData.getBinding(), LATEST_VERSION_BINDING));
    }

    @Install(to = "calledProcessesGrid.elementId", subject = "tooltipGenerator")
    private String calledProcessesGridElementIdTooltipGenerator(final CalledProcessReferenceData calledProcessReferenceData) {
        return StringUtils.defaultIfEmpty(calledProcessReferenceData.getElementName(), calledProcessReferenceData.getElementId());
    }

    @Subscribe("calledProcessesGrid.view")
    public void onCalledProcessesGridView(final ActionPerformedEvent event) {
        CalledProcessReferenceData selectedItem = calledProcessesGrid.getSingleSelectedItem();

        callActivityClickHandler.handleProcessNavigation(
                processDefinitionDataDc.getItem(),
                selectedItem,
                UiComponentUtils.isComponentAttachedToDialog(this));
    }
}
