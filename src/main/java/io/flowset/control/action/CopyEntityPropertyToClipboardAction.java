/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import io.jmix.core.Messages;
import io.jmix.core.entity.EntityValues;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentData;
import io.jmix.flowui.fragment.FragmentUtils;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;

@ActionType(CopyEntityPropertyToClipboardAction.ID)
public class CopyEntityPropertyToClipboardAction extends BaseAction {

    public static final String ID = "control_copyEntityPropertyToClipboard";

    protected Messages messages;
    protected Notifications notifications;

    protected String dataContainer;
    protected String property;

    protected InstanceContainer<?> instanceContainer;

    public CopyEntityPropertyToClipboardAction() {
        super(ID);
    }

    public CopyEntityPropertyToClipboardAction(String id) {
        super(id);

        this.icon = ComponentUtils.convertToIcon(VaadinIcon.COPY);
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;

        this.text = messages.getMessage("actions.Copy");
    }

    @Autowired
    public void setNotifications(Notifications notifications) {
        this.notifications = notifications;
    }

    public void setDataContainer(String dataContainer) {
        this.dataContainer = dataContainer;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public void setInstanceContainer(InstanceContainer<?> instanceContainer) {
        this.instanceContainer = instanceContainer;
    }

    @Override
    public void actionPerform(Component component) {
        InstanceContainer<?> container = getInstanceContainer(component);

        Object item = container.getItem();
        Object propertyValue = EntityValues.getValue(item, property);
        String valueAsString = propertyValue != null ? propertyValue.toString() : "";


        UiComponentUtils.copyToClipboard(valueAsString)
                .then(successResult -> notifications.create(
                                        messages.getMessage(getClass(), "copyComponentValueAction.copied"))
                                .withPosition(Notification.Position.TOP_END)
                                .withThemeVariant(NotificationVariant.LUMO_SUCCESS)
                                .show(),
                        errorResult -> notifications.create(
                                        messages.getMessage(getClass(), "copyComponentValueAction.copyFailed"))
                                .withPosition(Notification.Position.TOP_END)
                                .withThemeVariant(NotificationVariant.LUMO_ERROR)
                                .show());
    }

    protected InstanceContainer<?> getInstanceContainer(Component component) {
        if (instanceContainer == null) {
            Fragment<?> fragment = UiComponentUtils.findFragment(component);

            if (fragment != null) {
                FragmentData fragmentData = FragmentUtils.getFragmentData(fragment);
                instanceContainer = fragmentData.getContainer(dataContainer);

            } else {
                View<?> view = UiComponentUtils.findView(component);
                if (view == null) {
                    throw new IllegalStateException("View not found for action " + getId());
                }
                ViewData viewData = ViewControllerUtils.getViewData(view);
                instanceContainer = viewData.getContainer(dataContainer);
            }
        }

        return instanceContainer;
    }
}
