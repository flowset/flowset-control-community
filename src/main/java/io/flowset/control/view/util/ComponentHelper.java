/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.util;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.datatype.DatatypeFormatter;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.UiComponents;
import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.entity.processinstance.ProcessInstanceState;
import lombok.AllArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.TimeZone;

@Component
@AllArgsConstructor
public class ComponentHelper {
    protected final UiComponents uiComponents;
    protected final Messages messages;
    protected final DatatypeFormatter datatypeFormatter;
    protected final CurrentAuthentication currentAuthentication;

    public Span createProcessInstanceStateBadge(ProcessInstanceState state) {
        Icon icon;
        String stateTheme;

        switch (state) {
            case COMPLETED -> {
                icon = VaadinIcon.CHECK.create();
                icon.addClassNames(LumoUtility.Padding.XSMALL);
                stateTheme = "contrast";
            }
            case SUSPENDED -> {
                icon = VaadinIcon.PAUSE.create();
                icon.addClassNames("suspended-state-small-icon");
                stateTheme = "warning";
            }
            default -> {
                icon = VaadinIcon.HOURGLASS.create();
                icon.addClassNames(LumoUtility.Padding.XSMALL);
                stateTheme = "success";
            }
        }

        Span span = uiComponents.create(Span.class);
        span.getElement().getThemeList().add("badge pill " + stateTheme);

        Span text = uiComponents.create(Span.class);
        text.setText(messages.getMessage(state));
        span.add(icon, text);
        return span;
    }

    public Span createDateSpan(@Nullable OffsetDateTime date) {
        Span span = uiComponents.create(Span.class);

        TimeZone timeZone = currentAuthentication.getTimeZone();
        if (date != null) {
            LocalDateTime timestamp = date
                    .atZoneSameInstant(timeZone.toZoneId())
                    .toLocalDateTime();
            String formattedDate = datatypeFormatter.formatLocalDateTime(timestamp);
            span.setText(formattedDate);
        }

        return span;
    }

    /**
     * Retrieves a display name of the specified process in the <processKey> (ver. <processVersion>) format.
     *
     * @param process the process for showing in the column or in the text field.
     * @return the process label to show as a value of the Process column or text field.
     */
    @Nullable
    public String getProcessLabel(@Nullable ProcessDefinitionData process) {
        return Optional.ofNullable(process)
                .map(processDefinitionData -> getProcessLabel(process.getKey(), processDefinitionData.getVersion()))
                .orElse(null);
    }

    /**
     * Retrieves a display name of the specified decision in the <decisionKey> (ver. <decisionVersion>) format.
     *
     * @param decision the decision definition for showing in the column or in the text field.
     * @return the decision label to show as a value of the Decision column or text field.
     */
    @Nullable
    public String getDecisionLabel(@Nullable DecisionDefinitionData decision) {
        return Optional.ofNullable(decision)
                .map(decisionDefinitionData -> getDecisionLabel(decision.getKey(), decisionDefinitionData.getVersion()))
                .orElse(null);
    }

    /**
     * Retrieves a display name of the decision with the specified key and version in the <decisionKey> (ver. <decisionVersion>) format.
     *
     * @param decisionKey     the decision key for showing in the column or in the text field.
     * @param decisionVersion the decision version for showing in the column or in the text field.
     * @return the decision label to show as a value of the Decision column or text field.
     */
    @Nullable
    public String getDecisionLabel(String decisionKey, Integer decisionVersion) {
        return messages.formatMessage("", "common.decisionDefinitionKeyAndVersion", decisionKey, decisionVersion);
    }

    /**
     * Retrieves a display name of the process with the specified key and version in the <processKey> (ver. <processVersion>) format.
     *
     * @param processKey     the process key for showing in the column or in the text field.
     * @param processVersion the process version for showing in the column or in the text field.
     * @return the process label to show as a value of the Process column or text field.
     */
    public String getProcessLabel(String processKey, Integer processVersion) {
        return messages.formatMessage("", "common.processDefinitionKeyAndVersion", processKey, processVersion);
    }

    /**
     * Adds components to represent a "no data" state within the provided empty state container.
     *
     * @param emptyStateBox the container to which the "no data" state components will be added
     */
    public void addNoDataGridStateComponents(VerticalLayout emptyStateBox) {
        Icon mailbox = VaadinIcon.MAILBOX.create();
        mailbox.addClassNames("empty-grid-icon", LumoUtility.TextColor.TERTIARY);

        Span noDataHeader = new Span(messages.getMessage("dataGrid.emptyState.noData"));
        noDataHeader.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);
        emptyStateBox.add(mailbox, noDataHeader);
    }

    /**
     * Adds components to represent a "loading" state within the provided empty state container.
     *
     * @param emptyStateBox the container to which the "loading" state components will be added
     */
    public void addLoadingGridStateComponents(VerticalLayout emptyStateBox) {
        emptyStateBox.add(new Span(messages.getMessage("dataGrid.empty.loading")));
        emptyStateBox.add();
    }

    /**
     * Adds components to represent an "error" state within the provided empty state container.
     *
     * @param emptyStateBox the container to which the "error" state components will be added
     * @param errorText     the error message text to display within the "error" state
     */
    public void addErrorStateGridStateComponents(VerticalLayout emptyStateBox, String errorText) {
        Icon warningIcon = VaadinIcon.WARNING.create();
        warningIcon.addClassNames("empty-grid-icon", LumoUtility.TextColor.ERROR);

        Span headerError = new Span(messages.getMessage("dataGrid.empty.errorHeader"));
        headerError.addClassNames(LumoUtility.TextColor.ERROR, LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.MEDIUM);

        Span errorMessage = new Span(errorText);
        errorMessage.addClassNames(LumoUtility.TextColor.ERROR, LumoUtility.FontSize.SMALL);

        emptyStateBox.add(warningIcon, headerError, errorMessage);
    }
}