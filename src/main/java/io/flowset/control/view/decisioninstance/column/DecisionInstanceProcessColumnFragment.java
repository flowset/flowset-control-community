package io.flowset.control.view.decisioninstance.column;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouteParameters;
import io.flowset.control.entity.decisioninstance.HistoricDecisionInstanceShortData;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.view.processdefinition.ProcessDefinitionDetailView;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.FragmentRenderer;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import org.springframework.beans.factory.annotation.Autowired;

@FragmentDescriptor("decision-instance-process-column-fragment.xml")
@RendererItemContainer("decisionInstanceDc")
public class DecisionInstanceProcessColumnFragment extends FragmentRenderer<HorizontalLayout, HistoricDecisionInstanceShortData> {
    @ViewComponent
    protected JmixButton processKeyBtn;
    @Autowired
    protected ViewNavigators viewNavigators;

    @Override
    public void setItem(HistoricDecisionInstanceShortData item) {
        super.setItem(item);

        processKeyBtn.setText(item.getProcessDefinitionKey());
    }

    @Subscribe(id = "processKeyBtn", subject = "clickListener")
    public void onProcessKeyBtnClick(final ClickEvent<JmixButton> event) {
        viewNavigators.detailView(UiComponentUtils.getCurrentView(), ProcessDefinitionData.class)
                .withViewClass(ProcessDefinitionDetailView.class)
                .withRouteParameters(new RouteParameters("id", item.getProcessDefinitionId()))
                .withBackwardNavigation(true)
                .navigate();
    }
}