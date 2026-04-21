/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.engineconnectionsettings;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.entity.engine.EngineAuthState;
import io.flowset.control.entity.engine.EnvironmentType;
import io.flowset.control.service.engine.auth.EngineAuthStateService;
import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.UiEventPublisher;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.textfield.JmixPasswordField;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.flowset.control.action.TestEngineConnectionAction;
import io.flowset.control.entity.engine.AuthType;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.service.engine.EngineService;
import io.flowset.control.service.engine.EngineUiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import static io.flowset.control.view.util.JsUtils.COPY_SCRIPT_TEXT;

@Slf4j
@Route(value = "engine-connection-settings", layout = DefaultMainViewParent.class)
@ViewController("EngineConnectionSettingsView")
@ViewDescriptor("engine-connection-settings-view.xml")
@DialogMode(minWidth = "32em", maxWidth = "42em")
public class EngineConnectionSettingsView extends StandardView {
    public static final String ENGINE_WITH_TYPE_LABEL_FORMAT = "%s (%s)";

    @ViewComponent
    protected TypedTextField<String> baseUrlField;
    @ViewComponent
    protected TypedTextField<Object> basicAuthUsername;
    @ViewComponent
    protected JmixPasswordField basicAuthPassword;
    @ViewComponent
    protected TypedTextField<Object> engineNameField;
    @Autowired
    protected EngineService engineService;
    @Autowired
    protected EngineAuthStateService engineAuthStateService;
    @Autowired
    protected Notifications notifications;
    @ViewComponent
    protected MessageBundle messageBundle;
    @ViewComponent
    protected TypedTextField<String> authenticationTypeField;
    @Autowired
    protected Messages messages;
    @ViewComponent
    protected HorizontalLayout basicAuthSettingsHBox;
    @ViewComponent
    protected VerticalLayout customHttpHeaderSettingsVBox;
    @ViewComponent
    protected VerticalLayout oauth2SettingsVBox;
    @ViewComponent
    protected TypedTextField<String> customHeaderName;
    @ViewComponent
    protected JmixPasswordField customHeaderValue;
    @ViewComponent
    protected TypedTextField<String> oauth2IssuerUriField;
    @ViewComponent
    protected TypedTextField<String> oauth2ClientIdField;
    @ViewComponent
    protected JmixPasswordField oauth2ClientSecretField;
    @ViewComponent
    protected TypedTextField<String> oauth2ScopeField;
    @ViewComponent
    protected EntityComboBox<BpmEngine> bpmEnginesComboBox;
    @Autowired
    protected UiComponents uiComponents;
    @ViewComponent
    protected TestEngineConnectionAction testConnectionAction;
    @Autowired
    protected UiEventPublisher uiEventPublisher;
    @Autowired
    protected EngineUiService engineUiService;
    @ViewComponent
    protected InstanceContainer<BpmEngine> engineDc;
    @ViewComponent
    protected InstanceContainer<EngineAuthState> engineAuthStateDc;
    @ViewComponent
    protected TypedTextField<EnvironmentType> environmentTypeField;
    @ViewComponent
    protected HorizontalLayout authStateBox;

    @Subscribe
    public void onInit(final InitEvent event) {
        addClassNames(LumoUtility.Padding.Top.XSMALL);
        BpmEngine selectedEngine = engineService.getSelectedEngine();

        bpmEnginesComboBox.setValue(selectedEngine);
    }

    protected void initEngineFields(@Nullable BpmEngine bpmEngine) {
        testConnectionAction.setEngine(bpmEngine);
        testConnectionAction.refreshState();

        if (bpmEngine != null) {
            environmentTypeField.setTypedValue(bpmEngine.getEnvironmentType());
            if (BooleanUtils.isTrue(bpmEngine.getAuthEnabled())) {
                authenticationTypeField.setTypedValue(messages.getMessage(bpmEngine.getAuthType()));

                if (bpmEngine.getAuthType() == AuthType.HTTP_HEADER) {
                    customHttpHeaderSettingsVBox.setVisible(true);
                    basicAuthSettingsHBox.setVisible(false);
                    oauth2SettingsVBox.setVisible(false);
                } else if (bpmEngine.getAuthType() == AuthType.BASIC) {
                    customHttpHeaderSettingsVBox.setVisible(false);
                    basicAuthSettingsHBox.setVisible(true);
                    oauth2SettingsVBox.setVisible(false);
                } else if (bpmEngine.getAuthType() == AuthType.OAUTH2) {
                    customHttpHeaderSettingsVBox.setVisible(false);
                    basicAuthSettingsHBox.setVisible(false);
                    oauth2SettingsVBox.setVisible(true);
                } else {
                    customHttpHeaderSettingsVBox.setVisible(false);
                    basicAuthSettingsHBox.setVisible(false);
                    oauth2SettingsVBox.setVisible(false);
                }
            } else {
                authenticationTypeField.setTypedValue(messageBundle.getMessage("noAuth"));
                customHttpHeaderSettingsVBox.setVisible(false);
                basicAuthSettingsHBox.setVisible(false);
                oauth2SettingsVBox.setVisible(false);
            }
        } else {
            basicAuthSettingsHBox.setVisible(false);
            customHttpHeaderSettingsVBox.setVisible(false);
            oauth2SettingsVBox.setVisible(false);
            authenticationTypeField.setTypedValue(null);
            environmentTypeField.setTypedValue(null);
        }

        refreshAuthState(bpmEngine);
    }

    protected void refreshAuthState(@Nullable BpmEngine bpmEngine) {
        if (bpmEngine != null && BooleanUtils.isTrue(bpmEngine.getAuthEnabled()) && bpmEngine.getAuthType() == AuthType.OAUTH2) {
            EngineAuthState authState = engineAuthStateService.findByEngineId(bpmEngine.getId());
            engineAuthStateDc.setItem(authState);
        } else {
            engineAuthStateDc.setItem(null);
        }
    }

    @Subscribe("bpmEnginesComboBox")
    protected void onBpmEnginesComboBoxComponentValueChange(final AbstractField.ComponentValueChangeEvent<EntityComboBox<BpmEngine>, BpmEngine> event) {
        engineDc.setItem(event.getValue());
        initEngineFields(engineDc.getItemOrNull());
    }

    @Subscribe(id = "engineAuthStateDc", target = Target.DATA_CONTAINER)
    public void onEngineAuthStateDcItemChange(final InstanceContainer.ItemChangeEvent<EngineAuthState> event) {
        EngineAuthState authState = event.getItem();
        authStateBox.setVisible(authState != null && BooleanUtils.isTrue(authState.getIsLocked()));
    }

    @Subscribe(id = "copyBaseUrlBtn", subject = "clickListener")
    public void onCopyBaseUrlBtnClick(final ClickEvent<JmixButton> event) {
        String valueToCopy = baseUrlField.getValue();
        copyValue(event, valueToCopy, "baseUrlCopied", "baseUrlCopyFailed");
    }

    @Subscribe(id = "copyCustomHeaderNameBtn", subject = "clickListener")
    public void onCopyCustomHeaderNameBtnClick(final ClickEvent<JmixButton> event) {
        String valueToCopy = customHeaderName.getValue();
        copyValue(event, valueToCopy, "headerNameCopied", "headerNameCopyFailed");
    }

    @Subscribe(id = "copyCustomHeaderValueBtn", subject = "clickListener")
    public void onCopyCustomHeaderValueBtnClick(final ClickEvent<JmixButton> event) {
        String valueToCopy = customHeaderValue.getValue();
        copyValue(event, valueToCopy, "headerValueCopied", "headerValueCopyFailed");
    }

    @Subscribe(id = "copyBasicAuthUsernameBtn", subject = "clickListener")
    public void onCopyBasicAuthUsernameBtnClick(final ClickEvent<JmixButton> event) {
        String valueToCopy = basicAuthUsername.getValue();
        copyValue(event, valueToCopy, "basicAuthUsernameCopied", "basicAuthUsernameCopyFailed");
    }

    @Subscribe(id = "copyBasicAuthPasswordBtn", subject = "clickListener")
    public void onCopyBasicAuthPasswordBtnClick(final ClickEvent<JmixButton> event) {
        String valueToCopy = basicAuthPassword.getValue();
        copyValue(event, valueToCopy, "basicAuthPasswordCopied", "basicAuthPasswordCopyFailed");
    }

    @Subscribe(id = "copyOauth2IssuerUriBtn", subject = "clickListener")
    public void onCopyOauth2IssuerUriBtnClick(final ClickEvent<JmixButton> event) {
        copyValue(event, oauth2IssuerUriField.getValue(), "oauth2IssuerUriCopied", "oauth2IssuerUriCopyFailed");
    }

    @Subscribe(id = "copyOauth2ClientIdBtn", subject = "clickListener")
    public void onCopyOauth2ClientIdBtnClick(final ClickEvent<JmixButton> event) {
        copyValue(event, oauth2ClientIdField.getValue(), "oauth2ClientIdCopied", "oauth2ClientIdCopyFailed");
    }

    protected void copyValue(ClickEvent<JmixButton> event, String valueToCopy, String valueCopiedMessageKey, String copyFailedMessageKey) {
        Element buttonElement = event.getSource().getElement();
        buttonElement.executeJs(COPY_SCRIPT_TEXT, valueToCopy)
                .then(successResult -> notifications.create(messageBundle.getMessage(valueCopiedMessageKey))
                                .withPosition(Notification.Position.TOP_END)
                                .withThemeVariant(NotificationVariant.LUMO_SUCCESS)
                                .show(),
                        errorResult -> notifications.create(messageBundle.getMessage(copyFailedMessageKey))
                                .withPosition(Notification.Position.TOP_END)
                                .withThemeVariant(NotificationVariant.LUMO_ERROR)
                                .show());
    }

    @Subscribe(id = "updateEngineBtn", subject = "clickListener")
    protected void onUpdateEngineBtnClick(final ClickEvent<JmixButton> event) {
        try {
            bpmEnginesComboBox.executeValidators();
        } catch (ValidationException e) {
            return;
        }

        BpmEngine value = bpmEnginesComboBox.getValue();
        engineUiService.selectEngine(value);

        close(StandardOutcome.CLOSE);
    }

    @Install(to = "bpmEnginesComboBox", subject = "itemLabelGenerator")
    protected String bpmEnginesComboBoxItemLabelGenerator(final BpmEngine engine) {
        return ENGINE_WITH_TYPE_LABEL_FORMAT.formatted(engine.getName(), messages.getMessage(engine.getType()));
    }

    @Supply(to = "bpmEnginesComboBox", subject = "renderer")
    protected Renderer<BpmEngine> bpmEnginesComboBoxRenderer() {
        return new ComponentRenderer<>(bpmEngine -> {
            HorizontalLayout horizontalLayout = uiComponents.create(HorizontalLayout.class);
            horizontalLayout.setPadding(false);
            horizontalLayout.addClassNames(LumoUtility.Gap.SMALL);

            Span name = new Span(ENGINE_WITH_TYPE_LABEL_FORMAT.formatted(bpmEngine.getName(), messages.getMessage(bpmEngine.getType())));

            Span url = new Span(bpmEngine.getBaseUrl());
            url.addClassNames(LumoUtility.TextColor.TERTIARY);
            horizontalLayout.add(name, url);
            return horizontalLayout;
        });
    }
}
