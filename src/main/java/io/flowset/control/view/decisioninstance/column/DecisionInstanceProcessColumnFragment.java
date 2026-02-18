/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.decisioninstance.column;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.flowset.control.entity.decisioninstance.HistoricDecisionInstanceShortData;
import io.flowset.control.view.entitydetaillink.ProcessLinkColumnFragment;;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.Subscribe;

@FragmentDescriptor("decision-instance-process-column-fragment.xml")
@RendererItemContainer("decisionInstanceDc")
public class DecisionInstanceProcessColumnFragment extends ProcessLinkColumnFragment<HorizontalLayout, HistoricDecisionInstanceShortData> {

    @Subscribe(id = "processIdBtn", subject = "clickListener")
    public void onProcessIdBtnClick(final ClickEvent<JmixButton> event) {
        openProcessDetailView();
    }
}