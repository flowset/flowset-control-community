package io.flowset.control.view.user.column;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.flowset.control.entity.User;
import io.flowset.control.view.entitydetaillink.EntityDetailLinkFragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;

@FragmentDescriptor("username-column-fragment.xml")
@RendererItemContainer("userDc")
public class UsernameColumnFragment extends EntityDetailLinkFragment<HorizontalLayout, User> {
    @ViewComponent
    protected JmixButton usernameBtn;

    @Override
    public void setItem(User item) {
        super.setItem(item);

        usernameBtn.setText(item.getUsername());
    }

    @Subscribe(id = "usernameBtn", subject = "clickListener")
    public void onUsernameBtnClick(final ClickEvent<JmixButton> event) {
        openDetailView(User.class);
    }
}