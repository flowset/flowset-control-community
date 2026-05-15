/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processdefinition.detail;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import io.flowset.control.entity.decisiondefinition.DecisionReferenceData;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.uicomponent.viewer.handler.BusinessRuleTaskOverlayClickHandler;
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

@FragmentDescriptor("called-decisions-tab-fragment.xml")
public class CalledDecisionsTabFragment extends Fragment<VerticalLayout> {

    private static final String LATEST_VERSION_BINDING = "latest";

    @Autowired
    protected BusinessRuleTaskOverlayClickHandler businessRuleTaskClickHandler;
    @ViewComponent
    protected InstanceContainer<ProcessDefinitionData> processDefinitionDataDc;
    @ViewComponent
    protected DataGrid<DecisionReferenceData> decisionsGrid;

    @Supply(to = "decisionsGrid.binding", subject = "renderer")
    protected Renderer<DecisionReferenceData> decisionsGridBindingRenderer() {
        return new TextRenderer<>(decisionReferenceData ->
                StringUtils.defaultIfEmpty(decisionReferenceData.getBinding(), LATEST_VERSION_BINDING));
    }

    @Install(to = "decisionsGrid.elementId", subject = "tooltipGenerator")
    private String decisionsGridElementIdTooltipGenerator(final DecisionReferenceData decisionReferenceData) {
        return StringUtils.defaultIfEmpty(decisionReferenceData.getElementName(), decisionReferenceData.getElementId());
    }

    @Subscribe("decisionsGrid.view")
    public void onDecisionsGridView(final ActionPerformedEvent event) {
        DecisionReferenceData selectedItem = decisionsGrid.getSingleSelectedItem();

        businessRuleTaskClickHandler.handleDecisionNavigation(
                processDefinitionDataDc.getItem(),
                selectedItem,
                UiComponentUtils.isComponentAttachedToDialog(this));
    }
}
