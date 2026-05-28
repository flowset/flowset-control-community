/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.accesscontext.UiEntityContext;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.flowset.control.entity.engine.AuthType;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.service.engine.EngineUiService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

@ActionType(TestEngineConnectionAction.ID)
public class TestEngineConnectionAction extends SecuredBaseAction {
    public static final String ID = "control_testEngineConnection";

    private BpmEngine engine;

    protected Messages messages;
    protected EngineUiService engineUiService;
    protected Notifications notifications;
    protected Metadata metadata;
    protected AccessManager accessManager;

    public TestEngineConnectionAction() {
        super(ID);
    }

    public TestEngineConnectionAction(String id) {
        super(id);

        this.icon = ComponentUtils.convertToIcon(VaadinIcon.CONNECT);
    }

    public void setEngine(BpmEngine engine) {
        this.engine = engine;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
        this.text = messages.getMessage("actions.TestConnection");
    }

    @Autowired
    public void setEngineUiService(EngineUiService engineUiService) {
        this.engineUiService = engineUiService;
    }

    @Autowired
    public void setNotifications(Notifications notifications) {
        this.notifications = notifications;
    }

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Autowired
    public void setAccessManager(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Override
    public void actionPerform(Component component) {
        if (engine != null) {
            if (!isValidUrl(engine.getBaseUrl())) {
                notifications.create(messages.getMessage("engineNotAvailable.title"),
                        messages.formatMessage("", "engineNotAvailable.incorrectUrl",
                                Strings.nullToEmpty(engine.getBaseUrl())))
                        .withType(Notifications.Type.ERROR)
                        .show();
                return;
            }
            if (engine.getAuthEnabled() && engine.getAuthType() != null) {
                if (AuthType.BASIC == engine.getAuthType() && StringUtils.isEmpty(engine.getBasicAuthUsername())) {
                    notifications.create(messages.getMessage("engineNotAvailable.title"),
                                    messages.getMessage("engineNotAvailable.emptyAuthUsername"))
                            .withType(Notifications.Type.ERROR)
                            .show();
                    return;
                } else if (AuthType.BASIC == engine.getAuthType()
                        && StringUtils.isEmpty(engine.getBasicAuthPassword())) {
                    notifications.create(messages.getMessage("engineNotAvailable.title"),
                                    messages.getMessage("engineNotAvailable.emptyAuthPassword"))
                            .withType(Notifications.Type.ERROR)
                            .show();
                    return;
                } else if (AuthType.HTTP_HEADER == engine.getAuthType()
                        && StringUtils.isEmpty(engine.getHttpHeaderName())) {
                    notifications.create(messages.getMessage("engineNotAvailable.title"),
                                    messages.getMessage("engineNotAvailable.emptyHeaderName"))
                            .withType(Notifications.Type.ERROR)
                            .show();
                    return;
                }
            }
            try {
                engineUiService.getVersion(engine);
                notifications.create(messages.formatMessage("", "engineAvailable", engine.getBaseUrl()))
                        .withType(Notifications.Type.SUCCESS)
                        .show();
            } catch (EngineConnectionFailedException e) {
                if (e.getStatusCode() > 0) {
                    notifications.create(messages.getMessage("engineNotAvailable.title"),
                                    messages.formatMessage("", "engineNotAvailable.description", e.getStatusCode()))
                            .withType(Notifications.Type.ERROR)
                            .show();
                } else {
                    String errorMessage = StringUtils.defaultIfBlank(e.getResponseErrorMessage(), e.getMessage());
                    notifications.create(messages.getMessage("engineNotAvailable.title"),
                                    messages.formatMessage("", "engineNotAvailable.descriptionWithError", errorMessage))
                            .withType(Notifications.Type.ERROR)
                            .show();
                }
            }
        }
    }

    @Override
    protected boolean isPermitted() {
        if (engine == null) {
            return false;
        }

        MetaClass metaClass = metadata.getClass(engine);

        UiEntityContext entityContext = new UiEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(entityContext);

        if (!entityContext.isViewPermitted()) {
            return false;
        }

        return super.isPermitted();
    }

    private boolean isValidUrl(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (URISyntaxException | MalformedURLException e) {
            return false;
        }
    }
}
