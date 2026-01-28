/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.entitydetaillink;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.view.processinstance.ProcessInstanceDetailView;
import io.jmix.core.entity.EntityValues;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.Subscribe;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

/**
 * Abstract class responsible for rendering a UI fragment that provides a link to a process instance detail view.
 * This class is used to navigate to the process instance detail view from the process-instance-related column in the data grid.
 *
 * @param <E> the type of the UI component to which this fragment is attached
 * @param <V> the type of the data entity used by the fragment
 */
public abstract class ProcessInstanceLinkColumnFragment<E extends Component, V> extends EntityDetailLinkFragment<E, V> {
    protected String processInstanceIdProperty;

    public void setProcessInstanceIdProperty(String processInstanceIdProperty) {
        this.processInstanceIdProperty = processInstanceIdProperty;
    }

    @Subscribe
    public void onAttachEvent(final AttachEvent event) {
        JmixButton linkButton = findLinkButton();
        if (linkButton != null) {
            String processInstanceId = getProcessInstanceId();
            linkButton.setText(processInstanceId);
            linkButton.setVisible(StringUtils.isNotEmpty(processInstanceId));
        }
    }

    protected void openProcessInstanceDetailView() {
        String processInstanceId = getProcessInstanceId();
        if (UiComponentUtils.isComponentAttachedToDialog(this)) {
            RouterLink routerLink = new RouterLink(ProcessInstanceDetailView.class, new RouteParameters("id", processInstanceId));
            getUI().ifPresent(ui -> ui.getPage().open(routerLink.getHref()));
        } else {
            viewNavigators.detailView(getCurrentView(), ProcessInstanceData.class)
                    .withRouteParameters(new RouteParameters("id", processInstanceId))
                    .navigate();
        }
    }


    protected String getProcessInstanceId() {
        return EntityValues.getValue(item,
                Objects.requireNonNullElse(processInstanceIdProperty, "processInstanceId"));
    }
}
