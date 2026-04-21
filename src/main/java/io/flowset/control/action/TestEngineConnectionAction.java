package io.flowset.control.action;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.flowset.control.exception.AccessTokenConnectException;
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
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.web.client.HttpClientErrorException;

import static io.flowset.control.util.ExceptionUtils.isConnectionError;
import static io.flowset.control.util.UrlUtils.isValidUrl;

@ActionType(TestEngineConnectionAction.ID)
public class TestEngineConnectionAction extends SecuredBaseAction {
    public static final String ID = "control_testEngineConnection";
    protected static final Logger log = LoggerFactory.getLogger(TestEngineConnectionAction.class);

    protected BpmEngine engine;

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
        if (engine == null) {
            return;
        }

        if (!isValidUrl(engine.getBaseUrl())) {
            notifications.create(messages.getMessage("engineNotAvailable.title"),
                            messages.formatMessage("", "engineNotAvailable.incorrectUrl",
                                    Strings.nullToEmpty(engine.getBaseUrl())))
                    .withType(Notifications.Type.ERROR)
                    .show();
            return;
        }

        if (BooleanUtils.isTrue(engine.getAuthEnabled()) && engine.getAuthType() != null) {
            boolean valid = validateAuthFields();
            if (!valid) {
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
        } catch (OAuth2AuthorizationException | AccessTokenConnectException e) {
            log.error("Error during getting access token authorization", e);

            Throwable cause = ExceptionUtils.getRootCause(e);
            String errorMessage = cause instanceof HttpClientErrorException || isConnectionError(cause) ? cause.getMessage() : e.getMessage();
            notifications.create(messages.getMessage("oauth2AutorizationFailure.title"),
                            messages.formatMessage("", "oauth2AutorizationFailure.descriptionWithError", errorMessage))
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    protected boolean validateAuthFields() {
        AuthType authType = engine.getAuthType();
        if (authType == AuthType.BASIC) {
            return validateBasicAuthFields();
        }

        if (authType == AuthType.HTTP_HEADER) {
            return validateHttpHeaderAuthFields();
        }

        if (authType == AuthType.OAUTH2) {
            return validateOauth2AuthFields();
        }

        return true;
    }

    protected boolean validateOauth2AuthFields() {
        if (!isValidUrl(Strings.nullToEmpty(engine.getOauth2IssuerUri()))) {
            notifications.create(messages.getMessage("engineNotAvailable.title"),
                            messages.getMessage("engineNotAvailable.invalidOauth2IssuerUri"))
                    .withType(Notifications.Type.ERROR)
                    .show();
            return false;
        }
        
        if (StringUtils.isEmpty(engine.getOauth2ClientId())) {
            notifications.create(messages.getMessage("engineNotAvailable.title"),
                            messages.getMessage("engineNotAvailable.emptyOauth2ClientId"))
                    .withType(Notifications.Type.ERROR)
                    .show();
            return false;
        }
        
        if (StringUtils.isEmpty(engine.getOauth2ClientSecret())) {
            notifications.create(messages.getMessage("engineNotAvailable.title"),
                            messages.getMessage("engineNotAvailable.emptyOauth2ClientSecret"))
                    .withType(Notifications.Type.ERROR)
                    .show();
            return false;
        }

        return true;
    }

    protected boolean validateHttpHeaderAuthFields() {
        if (StringUtils.isEmpty(engine.getHttpHeaderName())) {
            notifications.create(messages.getMessage("engineNotAvailable.title"),
                            messages.getMessage("engineNotAvailable.emptyHeaderName"))
                    .withType(Notifications.Type.ERROR)
                    .show();
            return false;
        }
        return true;
    }

    protected boolean validateBasicAuthFields() {
        if (StringUtils.isEmpty(engine.getBasicAuthUsername())) {
            notifications.create(messages.getMessage("engineNotAvailable.title"),
                            messages.getMessage("engineNotAvailable.emptyAuthUsername"))
                    .withType(Notifications.Type.ERROR)
                    .show();
            return false;
        } else if (StringUtils.isEmpty(engine.getBasicAuthPassword())) {
            notifications.create(messages.getMessage("engineNotAvailable.title"),
                            messages.getMessage("engineNotAvailable.emptyAuthPassword"))
                    .withType(Notifications.Type.ERROR)
                    .show();
            return false;
        }

        return true;
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
}
