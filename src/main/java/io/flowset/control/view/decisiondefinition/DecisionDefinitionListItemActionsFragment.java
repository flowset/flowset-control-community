package io.flowset.control.view.decisiondefinition;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.FragmentRenderer;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.Subscribe;
import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import org.springframework.beans.factory.annotation.Autowired;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@FragmentDescriptor("decision-definition-list-item-actions-fragment.xml")
@RendererItemContainer("decisionDefinitionDc")
public class DecisionDefinitionListItemActionsFragment extends FragmentRenderer<HorizontalLayout, DecisionDefinitionData> {

    @Autowired
    private ViewNavigators viewNavigators;

    @Subscribe(id = "viewDetailsBtn", subject = "clickListener")
    public void onViewDetailsBtnClick(final ClickEvent<JmixButton> event) {
        viewNavigators.detailView(getCurrentView(), DecisionDefinitionData.class)
                .withViewClass(DecisionDefinitionDetailView.class)
                .withRouteParameters(new RouteParameters("id", item.getId()))
                .withBackwardNavigation(true)
                .navigate();
    }
}