package io.flowset.control.view.deploymentdata.column;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.flowset.control.entity.deployment.DeploymentData;
import io.flowset.control.view.entitydetaillink.EntityDetailLinkFragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;

@FragmentDescriptor("deployment-id-column-fragment.xml")
@RendererItemContainer("deploymentDc")
public class DeploymentIdColumnFragment extends EntityDetailLinkFragment<HorizontalLayout, DeploymentData> {
    @ViewComponent
    protected JmixButton idBtn;

    @Override
    public void setItem(DeploymentData item) {
        super.setItem(item);

        idBtn.setText(item.getDeploymentId());
    }

    @Subscribe(id = "idBtn", subject = "clickListener")
    public void onIdBtnClick(final ClickEvent<JmixButton> event) {
        openDetailView(DeploymentData.class);
    }
}