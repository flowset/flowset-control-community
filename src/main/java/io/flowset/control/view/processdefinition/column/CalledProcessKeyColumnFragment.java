/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processdefinition.column;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.flowset.control.entity.processdefinition.CalledProcessReferenceData;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.uicomponent.viewer.handler.CallActivityOverlayClickHandler;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.FragmentRenderer;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import org.springframework.beans.factory.annotation.Autowired;

@FragmentDescriptor("called-process-key-column-fragment.xml")
@RendererItemContainer("calledProcessReferenceDc")
public class CalledProcessKeyColumnFragment extends FragmentRenderer<HorizontalLayout, CalledProcessReferenceData> {

    @Autowired
    protected CallActivityOverlayClickHandler callActivityClickHandler;
    @ViewComponent
    protected JmixButton keyBtn;
    @ViewComponent
    protected JmixButton previewBtn;
    @ViewComponent
    protected InstanceContainer<ProcessDefinitionData> processDefinitionDataDc;

    @Subscribe
    public void onReady(final ReadyEvent event) {
        previewBtn.setIcon(new SvgIcon("icons/preview.svg"));
    }

    @Override
    public void setItem(CalledProcessReferenceData item) {
        super.setItem(item);

        keyBtn.setText(item.getCalledElement());
    }

    @Subscribe(id = "keyBtn", subject = "clickListener")
    public void onKeyBtnClick(final ClickEvent<JmixButton> event) {
        callActivityClickHandler.handleProcessNavigation(
                processDefinitionDataDc.getItem(),
                item,
                UiComponentUtils.isComponentAttachedToDialog(this));
    }

    @Subscribe(id = "previewBtn", subject = "clickListener")
    public void onPreviewBtnClick(final ClickEvent<JmixButton> event) {
        callActivityClickHandler.handleProcessPreview(
                processDefinitionDataDc.getItem(),
                item);
    }
}
