package io.flowset.control.view.decisiondefinition.column;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.view.entitydetaillink.EntityDetailLinkFragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;

@FragmentDescriptor("decision-key-column-fragment.xml")
@RendererItemContainer("decisionDefinitionDc")
public class DecisionKeyColumnFragment extends EntityDetailLinkFragment<HorizontalLayout, DecisionDefinitionData> {

    @ViewComponent
    protected JmixButton keyBtn;

    @Override
    public void setItem(DecisionDefinitionData item) {
        super.setItem(item);

        keyBtn.setText(item.getKey());
    }

    @Subscribe(id = "keyBtn", subject = "clickListener")
    public void onKeyBtnClick(final ClickEvent<JmixButton> event) {
        openDetailView(DecisionDefinitionData.class);
    }
}