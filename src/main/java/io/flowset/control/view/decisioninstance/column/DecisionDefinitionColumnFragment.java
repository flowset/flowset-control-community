/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.decisioninstance.column;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.entity.decisioninstance.HistoricDecisionInstanceShortData;
import io.flowset.control.view.decisiondefinition.DecisionDefinitionDetailView;
import io.flowset.control.view.entitydetaillink.EntityDetailLinkFragment;
import io.jmix.core.Messages;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import org.springframework.beans.factory.annotation.Autowired;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@FragmentDescriptor("decision-definition-column-fragment.xml")
@RendererItemContainer("decisionInstanceDc")
public class DecisionDefinitionColumnFragment extends EntityDetailLinkFragment<HorizontalLayout, HistoricDecisionInstanceShortData> {

    @ViewComponent
    protected JmixButton idBtn;
    @Autowired
    protected Messages messages;

    protected DecisionDefinitionData decisionDefinitionData;

    public void setDecisionDefinitionData(DecisionDefinitionData decisionDefinitionData) {
        this.decisionDefinitionData = decisionDefinitionData;

    }

    @Override
    public void setItem(HistoricDecisionInstanceShortData item) {
        super.setItem(item);

        String linkText;
        if (decisionDefinitionData != null) {
            linkText = messages.formatMessage("", "common.decisionDefinitionKeyAndVersion", decisionDefinitionData.getKey(),
                    decisionDefinitionData.getVersion().toString());
        } else {
            linkText = item.getDecisionDefinitionKey();
        }
        idBtn.setText(linkText);
    }

    @Subscribe(id = "idBtn", subject = "clickListener")
    public void onIdBtnClick(final ClickEvent<JmixButton> event) {
        String decisionDefinitionId = item.getDecisionDefinitionId();
        if (UiComponentUtils.isComponentAttachedToDialog(this)) {
            RouterLink routerLink = new RouterLink(DecisionDefinitionDetailView.class, new RouteParameters("id", decisionDefinitionId));
            getUI().ifPresent(ui -> ui.getPage().open(routerLink.getHref()));
        } else {
            viewNavigators.detailView(getCurrentView(), DecisionDefinitionData.class)
                    .withRouteParameters(new RouteParameters("id", decisionDefinitionId))
                    .navigate();
        }
    }
}