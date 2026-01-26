package io.flowset.control.view.historicactivityinstancedata.column;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.flowset.control.entity.activity.HistoricActivityInstanceData;
import io.flowset.control.view.entitydetaillink.EntityDetailLinkFragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;

@FragmentDescriptor("activity-instance-id-column-fragment.xml")
@RendererItemContainer("historicActivityDc")
public class ActivityInstanceIdColumnFragment extends EntityDetailLinkFragment<HorizontalLayout, HistoricActivityInstanceData> {
    @ViewComponent
    protected JmixButton idBtn;

    @Override
    public void setItem(HistoricActivityInstanceData item) {
        super.setItem(item);

        idBtn.setText(item.getActivityInstanceId());
    }

    @Subscribe(id = "idBtn", subject = "clickListener")
    public void onIdBtnClick(final ClickEvent<JmixButton> event) {
        openDetailView(HistoricActivityInstanceData.class);
    }

}