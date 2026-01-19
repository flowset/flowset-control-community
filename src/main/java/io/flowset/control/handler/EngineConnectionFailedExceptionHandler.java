package io.flowset.control.handler;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.exception.ViewEngineConnectionFailedException;
import io.flowset.control.service.engine.EngineService;
import io.jmix.core.Messages;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.exception.AbstractUiExceptionHandler;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * UI exception handler for {@link ViewEngineConnectionFailedException} and {@link EngineConnectionFailedException} exceptions.
 */
@Component
public class EngineConnectionFailedExceptionHandler extends AbstractUiExceptionHandler {

    protected final Notifications notifications;
    protected final ViewNavigationSupport viewNavigationSupport;
    protected final UiProperties uiProperties;
    protected final Dialogs dialogs;
    protected final EngineService engineService;
    protected final UiComponents uiComponents;
    protected final Messages messages;

    public EngineConnectionFailedExceptionHandler(Notifications notifications,
                                                  ViewNavigationSupport viewNavigationSupport,
                                                  UiProperties uiProperties, Dialogs dialogs,
                                                  EngineService engineService, UiComponents uiComponents,
                                                  Messages messages) {
        super(ViewEngineConnectionFailedException.class.getName(), EngineConnectionFailedException.class.getName());
        this.notifications = notifications;
        this.viewNavigationSupport = viewNavigationSupport;
        this.uiProperties = uiProperties;
        this.dialogs = dialogs;
        this.engineService = engineService;
        this.uiComponents = uiComponents;
        this.messages = messages;
    }


    @Override
    protected void doHandle(@NonNull String className, @NonNull String message, @Nullable Throwable throwable) {
        if (throwable instanceof ViewEngineConnectionFailedException) {
            dialogs.createOptionDialog()
                    .withHeader(messages.getMessage(getClass(), "exceptionDialog.engineNotAvailable.header"))
                    .withContent(createContent())
                    .withActions(new BaseAction("retry")
                                    .withText(messages.getMessage("actions.Retry"))
                                    .withIcon(VaadinIcon.REFRESH.create())
                                    .withHandler(actionPerformedEvent -> {
                                        UI.getCurrent().getPage().reload();
                                    }),
                            new DialogAction(DialogAction.Type.CLOSE)
                                    .withIcon(VaadinIcon.BAN.create())
                                    .withHandler(actionPerformedEvent -> {
                                        viewNavigationSupport.navigate(uiProperties.getMainViewId());
                                    }))
                    .open();
        } else if (throwable instanceof EngineConnectionFailedException) {
            notifications.create(messages.getMessage(getClass(), "exceptionDialog.engineNotAvailable.header"),
                            getErrorText())
                    .withType(Notifications.Type.ERROR)
                    .show();
        }

    }

    protected HorizontalLayout createContent() {
        HorizontalLayout horizontalLayout = uiComponents.create(HorizontalLayout.class);
        horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        horizontalLayout.addClassNames(LumoUtility.Border.ALL, LumoUtility.BorderRadius.LARGE,
                LumoUtility.BorderColor.ERROR, LumoUtility.Background.ERROR_10, LumoUtility.Padding.SMALL);

        Icon warningIcon = VaadinIcon.WARNING.create();
        warningIcon.setSize("1.25em");
        warningIcon.addClassNames(LumoUtility.TextColor.ERROR);

        String text = getErrorText();
        Span description = new Span(text);
        description.addClassNames(LumoUtility.TextColor.ERROR, LumoUtility.Whitespace.PRE, LumoUtility.FontSize.SMALL);

        horizontalLayout.add(warningIcon, description);
        return horizontalLayout;
    }

    protected String getErrorText() {
        BpmEngine selectedEngine = engineService.getSelectedEngine();
        String text;
        if (selectedEngine != null) {
            text = messages.formatMessage(getClass(), "exceptionDialog.engineNotAvailable.engineDescription", selectedEngine.getBaseUrl());
        } else {
            text = messages.getMessage(getClass(), "exceptionDialog.engineNotAvailable.defaultDescription");
        }
        return text;
    }
}
