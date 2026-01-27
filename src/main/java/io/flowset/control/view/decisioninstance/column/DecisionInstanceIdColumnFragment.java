/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.decisioninstance.column;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.flowset.control.entity.decisioninstance.HistoricDecisionInstanceShortData;
import io.flowset.control.view.entitydetaillink.EntityDetailLinkFragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;

@FragmentDescriptor("decision-instance-id-column-fragment.xml")
@RendererItemContainer("decisionInstanceDc")
public class DecisionInstanceIdColumnFragment extends EntityDetailLinkFragment<HorizontalLayout, HistoricDecisionInstanceShortData> {

    @ViewComponent
    protected JmixButton idBtn;

    @Override
    public void setItem(HistoricDecisionInstanceShortData item) {
        super.setItem(item);

        idBtn.setText(item.getDecisionInstanceId());
    }

    @Subscribe(id = "idBtn", subject = "clickListener")
    public void onIdBtnClick(final ClickEvent<JmixButton> event) {
        openDetailView(HistoricDecisionInstanceShortData.class);
    }
}