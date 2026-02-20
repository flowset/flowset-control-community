/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.uicomponent.viewer.handler;

import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.entity.filter.DecisionDefinitionFilter;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.service.decisiondefinition.DecisionDefinitionLoadContext;
import io.flowset.control.service.decisiondefinition.DecisionDefinitionService;
import io.flowset.control.view.decisiondefinition.DecisionDefinitionDetailView;
import io.flowset.uikit.component.bpmnviewer.event.DecisionLinkOverlayClickEvent;
import io.flowset.uikit.component.bpmnviewer.model.BusinessRuleTaskData;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.view.View;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

/**
 * Handles {@link DecisionLinkOverlayClickEvent} event.
 */
@AllArgsConstructor
@Component("control_BusinessRuleTaskOverlayClickHandler")
public class BusinessRuleTaskOverlayClickHandler {

    protected final DecisionDefinitionService decisionDefinitionService;
    protected final Metadata metadata;
    protected final Notifications notifications;
    protected final ViewNavigators viewNavigators;
    protected final DialogWindows dialogWindows;
    protected final Messages messages;

    /**
     * Opens {@link DecisionDefinitionDetailView} for the decision called from the specified process and activity.
     *
     * @param parentProcess        parent process
     * @param businessRuleTaskData the data from Call activity element from the parent process
     * @param fromDialog           current view is open in dialog or not
     */
    public void handleDecisionNavigation(ProcessDefinitionData parentProcess,
                                         BusinessRuleTaskData businessRuleTaskData,
                                         boolean fromDialog) {
        DecisionDefinitionData decisionDefinition = findDecisionDefinition(businessRuleTaskData, parentProcess);
        if (decisionDefinition != null) {
            View<?> currentView = getCurrentView();
            if (fromDialog) {
                RouterLink routerLink = new RouterLink(DecisionDefinitionDetailView.class, new RouteParameters("id", decisionDefinition.getId()));
                currentView.getUI().ifPresent(ui -> ui.getPage().open(routerLink.getHref()));
            } else {
                viewNavigators.detailView(currentView, DecisionDefinitionData.class)
                        .withViewClass(DecisionDefinitionDetailView.class)
                        .withRouteParameters(new RouteParameters("id", decisionDefinition.getId()))
                        .withBackwardNavigation(true)
                        .navigate();
            }
        }
    }

    @Nullable
    protected DecisionDefinitionData findDecisionDefinition(BusinessRuleTaskData businessRuleTaskData, ProcessDefinitionData parentProcess) {
        DecisionDefinitionFilter filter = createFilter(businessRuleTaskData, parentProcess);
        if (filter == null) {
            return null;
        }

        List<DecisionDefinitionData> decisionDefinitions = decisionDefinitionService.findAll(new DecisionDefinitionLoadContext()
                .setFilter(filter));

        if (CollectionUtils.isEmpty(decisionDefinitions)) {
            notifications.create(messages.formatMessage("", "decisionNotFound.title", businessRuleTaskData.getDecisionRef()),
                            getAdditionalFilterMessage(businessRuleTaskData.getBinding(), filter))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return null;
        } else {
            return decisionDefinitions.get(0);
        }
    }

    protected String getAdditionalFilterMessage(@Nullable String binding, DecisionDefinitionFilter filter) {
        String bindingName = StringUtils.defaultIfEmpty(binding, "latest");
        String additionalParamMessage = switch (bindingName) {
            case "version" -> messages.formatMessage("", "decisionNotFound.description.version",
                    filter.getVersion());
            case "versionTag" -> messages.formatMessage("", "decisionNotFound.description.versionTag",
                    filter.getVersionTag());
            case "deployment" -> messages.formatMessage("", "decisionNotFound.description.deployment",
                    filter.getDeploymentId());
            default -> "";
        };

        return String.join("\n", messages.formatMessage("", "decisionNotFound.description.binding",
                bindingName), additionalParamMessage);
    }

    @Nullable
    protected DecisionDefinitionFilter createFilter(BusinessRuleTaskData businessRuleTaskData, ProcessDefinitionData parentProcess) {
        String decisionKey = businessRuleTaskData.getDecisionRef();
        String binding = businessRuleTaskData.getBinding();

        DecisionDefinitionFilter filter = metadata.create(DecisionDefinitionFilter.class);
        filter.setLatestVersionOnly(false);
        filter.setKey(decisionKey);

        if (binding == null) {
            filter.setLatestVersionOnly(true);
            return filter;
        }

        switch (binding) {
            case "version" -> {
                try {
                    Integer version = Integer.parseInt(businessRuleTaskData.getVersion());
                    filter.setVersion(version);
                } catch (NumberFormatException e) {
                    notifications.create(messages.formatMessage("", "decisionNotFound.title", decisionKey),
                                    messages.formatMessage("", "decisionNotFound.description.invalidVersion", businessRuleTaskData.getVersion()))
                            .withType(Notifications.Type.WARNING)
                            .show();
                    return null;
                }
            }
            case "versionTag" -> filter.setVersionTag(businessRuleTaskData.getVersionTag());
            case "deployment" -> filter.setDeploymentId(parentProcess.getDeploymentId());
            default -> filter.setLatestVersionOnly(true);
        }

        return filter;
    }
}
