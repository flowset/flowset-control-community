/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processdefinition.detail;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import io.flowset.control.entity.decisiondefinition.DecisionReferenceData;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.Supply;
import org.apache.commons.lang3.StringUtils;

@FragmentDescriptor("called-decisions-tab-fragment.xml")
public class CalledDecisionsTabFragment extends Fragment<VerticalLayout> {

    private static final String LATEST_VERSION_BINDING = "latest";

    @Supply(to = "decisionsGrid.binding", subject = "renderer")
    protected Renderer<DecisionReferenceData> decisionsGridBindingRenderer() {
        return new TextRenderer<>(decisionReferenceData ->
                StringUtils.defaultIfEmpty(decisionReferenceData.getBinding(), LATEST_VERSION_BINDING));
    }

    @Install(to = "decisionsGrid.elementId", subject = "tooltipGenerator")
    private String decisionsGridElementIdTooltipGenerator(final DecisionReferenceData decisionReferenceData) {
        return StringUtils.defaultIfEmpty(decisionReferenceData.getElementName(), decisionReferenceData.getElementId());
    }
}
