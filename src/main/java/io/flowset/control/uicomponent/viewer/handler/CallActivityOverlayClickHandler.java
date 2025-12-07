/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.uicomponent.viewer.handler;

import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.view.View;
import io.flowset.control.entity.filter.ProcessDefinitionFilter;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.service.processdefinition.ProcessDefinitionLoadContext;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import io.flowset.control.view.processdefinition.ProcessDefinitionDetailView;
import io.flowset.control.view.processinstance.CalledProcessInstanceDataListView;
import io.flowset.control.view.processinstance.ProcessInstanceDetailView;
import io.flowset.uikit.component.bpmnviewer.model.CallActivityData;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

/**
 * Handles {@link io.flowset.uikit.component.bpmnviewer.event.CalledProcessOverlayClickEvent}
 * and {@link io.flowset.uikit.component.bpmnviewer.event.CalledProcessInstanceOverlayClickEvent} events.
 */
@AllArgsConstructor
@Component("control_CallActivityOverlayClickHandler")
public class CallActivityOverlayClickHandler {

    protected final ProcessDefinitionService processDefinitionService;
    protected final Metadata metadata;
    protected final Notifications notifications;
    protected final ViewNavigators viewNavigators;
    protected final DialogWindows dialogWindows;
    protected final Messages messages;

    /**
     * Opens {@link ProcessDefinitionDetailView} for the process called from the specified process and activity.
     *
     * @param parentProcess    parent process
     * @param callActivityData the data from Call activity element from the parent process
     * @param fromDialog       current view is open in dialog or not
     */
    public void handleProcessNavigation(ProcessDefinitionData parentProcess,
                                        CallActivityData callActivityData,
                                        boolean fromDialog) {
        ProcessDefinitionData calledProcess = findCalledProcess(callActivityData, parentProcess);
        if (calledProcess != null) {
            View<?> currentView = getCurrentView();
            if (fromDialog) {
                RouterLink routerLink = new RouterLink(ProcessDefinitionDetailView.class, new RouteParameters("id", calledProcess.getId()));
                currentView.getUI().ifPresent(ui -> ui.getPage().open(routerLink.getHref()));
            } else {
                viewNavigators.detailView(currentView, ProcessDefinitionData.class)
                        .withViewClass(ProcessDefinitionDetailView.class)
                        .withRouteParameters(new RouteParameters("id", calledProcess.getId()))
                        .withBackwardNavigation(true)
                        .navigate();
            }
        }
    }

    /**
     * Handles a navigation to the specified called process instances. If only one instance is provided, then {@link ProcessInstanceDetailView} is opened.
     * Otherwise, opens {@link CalledProcessInstanceDataListView}.
     *
     * @param calledProcessInstanceIds a list of process instances called from the other process instance
     */
    public void handleInstancesNavigation(List<String> calledProcessInstanceIds) {
        if (CollectionUtils.size(calledProcessInstanceIds) == 1) {
            viewNavigators.detailView(getCurrentView(), ProcessInstanceData.class)
                    .withViewClass(ProcessInstanceDetailView.class)
                    .withRouteParameters(new RouteParameters("id", calledProcessInstanceIds.get(0)))
                    .withBackwardNavigation(true)
                    .navigate();
        } else {
            dialogWindows.view(getCurrentView(), CalledProcessInstanceDataListView.class)
                    .withViewConfigurer(view -> view.setProcessInstanceIds(calledProcessInstanceIds))
                    .open();
        }
    }

    @Nullable
    protected ProcessDefinitionData findCalledProcess(CallActivityData callActivityData, ProcessDefinitionData parentProcess) {
        ProcessDefinitionFilter filter = createFilter(callActivityData, parentProcess);
        if (filter == null) {
            return null;
        }

        List<ProcessDefinitionData> calledProcesses = processDefinitionService.findAll(new ProcessDefinitionLoadContext()
                .setFilter(filter));

        if (CollectionUtils.isEmpty(calledProcesses)) {
            notifications.create(messages.formatMessage("", "calledProcessNotFound.title", callActivityData.getCalledElement()),
                            getAdditionalFilterMessage(callActivityData.getBinding(), filter))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return null;
        } else {
            return calledProcesses.get(0);
        }
    }

    protected String getAdditionalFilterMessage(@Nullable String binding, ProcessDefinitionFilter filter) {
        String bindingName = StringUtils.defaultIfEmpty(binding, "latest");
        String additionalParamMessage = switch (bindingName) {
            case "version" -> messages.formatMessage("", "calledProcessNotFound.description.version",
                    filter.getVersion());
            case "versionTag" -> messages.formatMessage("", "calledProcessNotFound.description.versionTag",
                    filter.getVersionTag());
            case "deployment" -> messages.formatMessage("", "calledProcessNotFound.description.deployment",
                    filter.getDeploymentId());
            default -> "";
        };

        return String.join("\n", messages.formatMessage("", "calledProcessNotFound.description.binding",
                bindingName), additionalParamMessage);
    }

    @Nullable
    protected ProcessDefinitionFilter createFilter(CallActivityData callActivityData, ProcessDefinitionData parentProcess) {
        String processKey = callActivityData.getCalledElement();
        String binding = callActivityData.getBinding();

        ProcessDefinitionFilter filter = metadata.create(ProcessDefinitionFilter.class);
        filter.setLatestVersionOnly(false);
        filter.setKey(processKey);

        if (binding == null) {
            filter.setLatestVersionOnly(true);
            return filter;
        }

        switch (binding) {
            case "version" -> {
                try {
                    Integer version = Integer.parseInt(callActivityData.getVersion());
                    filter.setVersion(version);
                } catch (NumberFormatException e) {
                    notifications.create(messages.formatMessage("", "calledProcessNotFound.title", processKey),
                                    messages.formatMessage("", "calledProcessNotFound.description.invalidVersion", callActivityData.getVersion()))
                            .withType(Notifications.Type.WARNING)
                            .show();
                    return null;
                }
            }
            case "versionTag" -> filter.setVersionTag(callActivityData.getVersionTag());
            case "deployment" -> filter.setDeploymentId(parentProcess.getDeploymentId());
            default -> filter.setLatestVersionOnly(true);
        }

        return filter;
    }
}
