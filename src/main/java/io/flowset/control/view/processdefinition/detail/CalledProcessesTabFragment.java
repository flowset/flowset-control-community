/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processdefinition.detail;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import io.flowset.control.entity.processdefinition.CalledProcessReferenceData;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.Supply;
import org.apache.commons.lang3.StringUtils;

@FragmentDescriptor("called-processes-tab-fragment.xml")
public class CalledProcessesTabFragment extends Fragment<VerticalLayout> {

    private static final String LATEST_VERSION_BINDING = "latest";

    @Supply(to = "calledProcessesGrid.binding", subject = "renderer")
    protected Renderer<CalledProcessReferenceData> calledProcessesGridBindingRenderer() {
        return new TextRenderer<>(calledProcessReferenceData ->
                StringUtils.defaultIfEmpty(calledProcessReferenceData.getBinding(), LATEST_VERSION_BINDING));
    }

    @Install(to = "calledProcessesGrid.elementId", subject = "tooltipGenerator")
    private String calledProcessesGridElementIdTooltipGenerator(final CalledProcessReferenceData calledProcessReferenceData) {
        return StringUtils.defaultIfEmpty(calledProcessReferenceData.getElementName(), calledProcessReferenceData.getElementId());
    }
}
