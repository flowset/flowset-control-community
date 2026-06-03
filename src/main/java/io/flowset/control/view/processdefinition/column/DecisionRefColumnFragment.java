/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processdefinition.column;

import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.flowset.control.action.decisiondefinition.DecisionTablePreviewAction;
import io.flowset.control.action.decisiondefinition.ViewCalledDecisionAction;
import io.flowset.control.entity.decisiondefinition.DecisionReferenceData;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.FragmentRenderer;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;

@FragmentDescriptor("decision-ref-column-fragment.xml")
@RendererItemContainer("decisionReferenceDc")
public class DecisionRefColumnFragment extends FragmentRenderer<HorizontalLayout, DecisionReferenceData> {

    @ViewComponent
    protected JmixButton keyBtn;
    @ViewComponent
    protected JmixButton previewBtn;
    @ViewComponent
    protected InstanceContainer<ProcessDefinitionData> processDefinitionDataDc;
    @ViewComponent
    protected ViewCalledDecisionAction keyAction;
    @ViewComponent
    protected DecisionTablePreviewAction previewAction;

    @Subscribe
    public void onReady(final ReadyEvent event) {
        previewBtn.setIcon(new SvgIcon("icons/preview.svg"));
    }

    @Override
    public void setItem(DecisionReferenceData item) {
        super.setItem(item);

        keyBtn.setText(item.getDecisionRef());
        ProcessDefinitionData processDefinition = processDefinitionDataDc.getItem();
        keyAction.setProcessDefinitionData(processDefinition);
        keyAction.setDecisionReference(item);
        previewAction.setProcessDefinitionData(processDefinition);
        previewAction.setDecisionReference(item);
    }
}
