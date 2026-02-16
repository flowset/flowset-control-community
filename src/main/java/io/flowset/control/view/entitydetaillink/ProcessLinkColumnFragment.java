/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.entitydetaillink;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.view.processdefinition.ProcessDefinitionDetailView;
import io.flowset.control.view.util.ComponentHelper;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.Subscribe;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

/**
 * Abstract class responsible for rendering a UI fragment that provides a link to a process detail view.
 * This class is used to navigate to the process definition detail view from the process-related column in the data grid.
 *
 * @param <E> the type of the UI component to which this fragment is attached
 * @param <V> the type of the data entity used by the fragment
 */
public abstract class ProcessLinkColumnFragment<E extends Component, V> extends EntityDetailLinkFragment<E, V> {
    @Autowired
    protected ComponentHelper componentHelper;

    protected ProcessDefinitionData processDefinitionData;

    public void setProcessDefinitionData(ProcessDefinitionData processDefinitionData) {
        this.processDefinitionData = processDefinitionData;
    }

    @Subscribe
    public void onAttachEvent(final AttachEvent event) {
        JmixButton linkButton = findLinkButton();
        if (linkButton != null) {
            String processDefinitionId = getProcessDefinitionId();

            linkButton.setVisible(StringUtils.isNotEmpty(processDefinitionId));
            linkButton.setText(getProcessLabel());
        }
    }

    protected void openProcessDetailView() {
        String processDefinitionId = getProcessDefinitionId();
        if (UiComponentUtils.isComponentAttachedToDialog(this)) {
            RouterLink routerLink = new RouterLink(ProcessDefinitionDetailView.class, new RouteParameters("id", processDefinitionId));
            getUI().ifPresent(ui -> ui.getPage().open(routerLink.getHref()));
        } else {
            viewNavigators.detailView(getCurrentView(), ProcessDefinitionData.class)
                    .withRouteParameters(new RouteParameters("id", processDefinitionId))
                    .navigate();
        }
    }

    @Nullable
    protected String getProcessLabel() {
        if (processDefinitionData == null) {
            return null;
        }
        return componentHelper.getProcessLabel(processDefinitionData);
    }

    @Nullable
    protected String getProcessDefinitionId() {
        return processDefinitionData != null ? processDefinitionData.getId() : null;
    }
}
