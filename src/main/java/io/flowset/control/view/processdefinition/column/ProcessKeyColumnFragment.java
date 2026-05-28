/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processdefinition.column;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.flowset.control.action.processdefinition.PreviewProcessDefinitionAction;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.view.entitydetaillink.EntityDetailLinkFragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;

@FragmentDescriptor("process-key-column-fragment.xml")
@RendererItemContainer("processDefinitionDc")
public class ProcessKeyColumnFragment extends EntityDetailLinkFragment<HorizontalLayout, ProcessDefinitionData> {

    @ViewComponent
    protected JmixButton keyBtn;
    @ViewComponent
    protected JmixButton previewBtn;
    @ViewComponent
    protected PreviewProcessDefinitionAction previewAction;

    @Subscribe
    public void onAttachEvent(final AttachEvent event) {
        previewBtn.setIcon(new SvgIcon("icons/preview.svg"));
    }

    @Override
    public void setItem(ProcessDefinitionData item) {
        super.setItem(item);

        keyBtn.setText(item.getKey());
        previewAction.setProcessDefinition(item);
    }

    @Subscribe(id = "keyBtn", subject = "clickListener")
    public void onKeyBtnClick(final ClickEvent<JmixButton> event) {
        openDetailView(ProcessDefinitionData.class);
    }
}