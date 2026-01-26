package io.flowset.control.view.resourcerolemodellist.column;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouteParameters;
import io.flowset.control.view.entitydetaillink.EntityDetailLinkFragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.securityflowui.view.resourcerole.ResourceRoleModelDetailView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.View;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@FragmentDescriptor("resource-role-name-column-fragment.xml")
@RendererItemContainer("resourceRoleDc")
public class ResourceRoleNameColumnFragment extends EntityDetailLinkFragment<HorizontalLayout, ResourceRoleModel> {
    @ViewComponent
    protected JmixButton nameBtn;
    @Autowired
    protected View view;
    @Autowired
    protected UrlParamSerializer urlParamSerializer;

    @Override
    public void setItem(ResourceRoleModel item) {
        super.setItem(item);

        nameBtn.setText(item.getName());
    }

    @Subscribe(id = "nameBtn", subject = "clickListener")
    public void onNameBtnClick(final ClickEvent<JmixButton> event) {
        String serializedCode = urlParamSerializer.serialize(item.getCode());

        viewNavigators.detailView(getCurrentView(), ResourceRoleModel.class)
                .withRouteParameters(new RouteParameters(ResourceRoleModelDetailView.ROUTE_PARAM_NAME, serializedCode))
                .navigate();
    }


}