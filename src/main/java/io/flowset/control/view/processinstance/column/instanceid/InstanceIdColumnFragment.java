/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processinstance.column.instanceid;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.entity.processinstance.RuntimeProcessInstanceData;
import io.flowset.control.view.entitydetaillink.EntityDetailLinkFragment;
import io.flowset.control.view.processinstance.ProcessInstanceDetailView;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@FragmentDescriptor("instance-id-column-fragment.xml")
@RendererItemContainer("processInstanceDc")
public class InstanceIdColumnFragment extends EntityDetailLinkFragment<HorizontalLayout, RuntimeProcessInstanceData> {

    @ViewComponent
    protected JmixButton idBtn;
    @ViewComponent
    protected JmixButton copyBtn;

    protected boolean showCopyButton = true;

    public void setShowCopyButton(boolean showCopyButton) {
        this.showCopyButton = showCopyButton;
    }

    @Override
    public void setItem(RuntimeProcessInstanceData item) {
        super.setItem(item);

        idBtn.setText(item.getInstanceId());
        copyBtn.setVisible(showCopyButton);
    }

    @Subscribe(id = "idBtn", subject = "clickListener")
    public void onIdBtnClick(final ClickEvent<JmixButton> event) {
        boolean isFromDialog = UiComponentUtils.isComponentAttachedToDialog(this);
        RouteParameters routeParameters = new RouteParameters("id", item.getId());
        if (isFromDialog) {
            RouterLink routerLink = new RouterLink(ProcessInstanceDetailView.class, routeParameters);
            getUI().ifPresent(ui -> ui.getPage().open(routerLink.getHref()));
        } else {
            viewNavigators.detailView(getCurrentView(), ProcessInstanceData.class)
                    .withViewClass(ProcessInstanceDetailView.class)
                    .withRouteParameters(routeParameters)
                    .withBackwardNavigation(true)
                    .navigate();
        }

    }
}