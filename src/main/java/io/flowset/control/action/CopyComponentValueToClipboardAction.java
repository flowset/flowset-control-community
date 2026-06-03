/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.dom.Element;
import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.TargetAction;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.ComponentUtils;
import org.springframework.beans.factory.annotation.Autowired;

import static io.flowset.control.view.util.JsUtils.COPY_SCRIPT_TEXT;

@ActionType(CopyComponentValueToClipboardAction.ID)
public class CopyComponentValueToClipboardAction extends BaseAction implements TargetAction<HasValue<?, ?>> {

    public static final String ID = "control_copyComponentValueToClipboard";

    protected Messages messages;
    protected Notifications notifications;

    protected HasValue<?, ?> target;

    public CopyComponentValueToClipboardAction() {
        super(ID);
    }

    public CopyComponentValueToClipboardAction(String id) {
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

    @Override
    public HasValue<?, ?> getTarget() {
        return target;
    }

    @Override
    public void setTarget(HasValue target) {
        this.target = target;
    }

    @Override
    public void actionPerform(Component component) {
        if (target != null) {
            String valueAsString = target.isEmpty() ? "" : target.getValue().toString();
            Element componentElement = component.getElement();
            componentElement.executeJs(COPY_SCRIPT_TEXT, valueAsString)
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
            UI.getCurrent().getPage().executeJs("window.copyToClipboard($0)", valueAsString);
        } else {
            notifications.show(messages.formatMessage(getClass(),
                    "copyComponentValueAction.errorMessage", component.getClassName()));
        }
    }
}
