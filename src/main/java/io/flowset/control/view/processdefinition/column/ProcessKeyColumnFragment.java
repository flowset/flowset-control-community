/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processdefinition.column;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.view.entitydetaillink.EntityDetailLinkFragment;
import io.flowset.control.view.processdefinition.ProcessDefinitionDiagramView;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import org.springframework.beans.factory.annotation.Autowired;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@FragmentDescriptor("process-key-column-fragment.xml")
@RendererItemContainer("processDefinitionDc")
public class ProcessKeyColumnFragment extends EntityDetailLinkFragment<HorizontalLayout, ProcessDefinitionData> {

    @ViewComponent
    protected JmixButton keyBtn;
    @Autowired
    protected ViewNavigators viewNavigators;
    @ViewComponent
    protected JmixButton previewBtn;
    @Autowired
    protected DialogWindows dialogWindows;

    @Subscribe
    public void onAttachEvent(final AttachEvent event) {
        previewBtn.setIcon(new SvgIcon("icons/preview.svg"));
    }

    @Override
    public void setItem(ProcessDefinitionData item) {
        super.setItem(item);

        keyBtn.setText(item.getKey());
    }

    @Subscribe(id = "keyBtn", subject = "clickListener")
    public void onKeyBtnClick(final ClickEvent<JmixButton> event) {
        openDetailView(ProcessDefinitionData.class);
    }

    @Subscribe(id = "previewBtn", subject = "clickListener")
    public void onPreviewBtnClick(final ClickEvent<JmixButton> event) {
        dialogWindows.view(getCurrentView(), ProcessDefinitionDiagramView.class)
                .withViewConfigurer(view -> view.setProcessDefinition(item))
                .open();
    }
}