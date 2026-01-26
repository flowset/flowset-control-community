package io.flowset.control.view.decisioninstance.column;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouteParameters;
import io.flowset.control.entity.decisioninstance.HistoricDecisionInstanceShortData;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.view.processinstance.ProcessInstanceDetailView;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.FragmentRenderer;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import org.springframework.beans.factory.annotation.Autowired;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@FragmentDescriptor("decision-instance-process-instance-column-fragment.xml")
@RendererItemContainer("decisionInstanceDc")
public class DecisionInstanceProcessInstanceColumnFragment extends FragmentRenderer<HorizontalLayout, HistoricDecisionInstanceShortData> {
    @ViewComponent
    protected JmixButton processInstanceBtn;
    @Autowired
    protected ViewNavigators viewNavigators;

    @Override
    public void setItem(HistoricDecisionInstanceShortData item) {
        super.setItem(item);

        processInstanceBtn.setText(item.getProcessInstanceId());
    }

    @Subscribe(id = "processInstanceBtn", subject = "clickListener")
    public void onProcessInstanceBtnClick(final ClickEvent<JmixButton> event) {
        viewNavigators.detailView(getCurrentView(), ProcessInstanceData.class )
                .withViewClass(ProcessInstanceDetailView.class)
                .withRouteParameters(new RouteParameters("id", item.getProcessInstanceId()))
                .withBackwardNavigation(true)
                .navigate();
    }
}