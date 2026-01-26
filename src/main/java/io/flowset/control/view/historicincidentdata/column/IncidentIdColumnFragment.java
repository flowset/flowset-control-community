package io.flowset.control.view.historicincidentdata.column;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.flowset.control.entity.incident.HistoricIncidentData;
import io.flowset.control.view.entitydetaillink.EntityDetailLinkFragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;

@FragmentDescriptor("incident-id-column-fragment.xml")
@RendererItemContainer("incidentDc")
public class IncidentIdColumnFragment extends EntityDetailLinkFragment<HorizontalLayout, HistoricIncidentData> {
    @ViewComponent
    protected JmixButton idBtn;

    @Override
    public void setItem(HistoricIncidentData item) {
        super.setItem(item);

        idBtn.setText(item.getIncidentId());
    }

    @Subscribe(id = "idBtn", subject = "clickListener")
    public void onIdBtnClick(final ClickEvent<JmixButton> event) {
        openDetailView(HistoricIncidentData.class);
    }
}