/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processdefinition.column;

import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.flowset.control.action.processdefinition.ViewCalledProcessAction;
import io.flowset.control.action.processdefinition.PreviewCalledProcessAction;
import io.flowset.control.entity.processdefinition.CalledProcessReferenceData;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.FragmentRenderer;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;

@FragmentDescriptor("called-process-key-column-fragment.xml")
@RendererItemContainer("calledProcessReferenceDc")
public class CalledProcessKeyColumnFragment extends FragmentRenderer<HorizontalLayout, CalledProcessReferenceData> {

    @ViewComponent
    protected JmixButton keyBtn;
    @ViewComponent
    protected JmixButton previewBtn;
    @ViewComponent
    protected InstanceContainer<ProcessDefinitionData> processDefinitionDataDc;
    @ViewComponent
    protected ViewCalledProcessAction keyAction;
    @ViewComponent
    protected PreviewCalledProcessAction previewAction;

    @Subscribe
    public void onReady(final ReadyEvent event) {
        previewBtn.setIcon(new SvgIcon("icons/preview.svg"));
    }

    @Override
    public void setItem(CalledProcessReferenceData item) {
        super.setItem(item);

        keyBtn.setText(item.getCalledElement());
        ProcessDefinitionData processDefinition = processDefinitionDataDc.getItem();
        keyAction.setProcessDefinitionData(processDefinition);
        keyAction.setCalledProcessReference(item);
        previewAction.setProcessDefinitionData(processDefinition);
        previewAction.setCalledProcessReference(item);
    }
}
