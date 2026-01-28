/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.incidentdata.column;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.flowset.control.entity.incident.IncidentData;
import io.flowset.control.view.entitydetaillink.ProcessInstanceLinkColumnFragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.Subscribe;

@FragmentDescriptor("incident-process-instance-id-column-fragment.xml")
@RendererItemContainer("incidentDc")
public class IncidentProcessInstanceIdColumnFragment extends ProcessInstanceLinkColumnFragment<HorizontalLayout, IncidentData> {

    @Subscribe(id = "idBtn", subject = "clickListener")
    public void onIdBtnClick(final ClickEvent<JmixButton> event) {
       openProcessInstanceDetailView();
    }
}