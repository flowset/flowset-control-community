/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processdefinition.column;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.view.entitydetaillink.EntityDetailLinkFragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import org.apache.commons.lang3.StringUtils;

@FragmentDescriptor("process-name-column-fragment.xml")
@RendererItemContainer("processDefinitionDc")
public class ProcessNameColumnFragment extends EntityDetailLinkFragment<HorizontalLayout, ProcessDefinitionData> {

    @ViewComponent
    protected JmixButton nameBtn;


    @Override
    public void setItem(ProcessDefinitionData item) {
        super.setItem(item);

        String name = item.getName();
        nameBtn.setVisible(StringUtils.isNotEmpty(name));
        nameBtn.setText(name);
    }

    @Subscribe(id = "nameBtn", subject = "clickListener")
    public void onNameBtnClick(final ClickEvent<JmixButton> event) {
        openDetailView(ProcessDefinitionData.class);
    }
}