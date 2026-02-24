/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.incidentdata;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;
import feign.FeignException;
import io.flowset.control.action.CopyComponentValueToClipboardAction;
import io.flowset.control.entity.ExternalTaskData;
import io.flowset.control.entity.incident.IncidentData;
import io.flowset.control.entity.job.JobData;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.exception.ViewEngineConnectionFailedException;
import io.flowset.control.service.externaltask.ExternalTaskService;
import io.flowset.control.service.incident.IncidentService;
import io.flowset.control.service.job.JobService;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import io.flowset.control.view.event.TitleUpdateEvent;
import io.flowset.control.view.externaltask.ExternalTaskErrorDetailsView;
import io.flowset.control.view.job.JobErrorDetailsView;
import io.flowset.control.view.processdefinition.ProcessDefinitionDetailView;
import io.flowset.control.view.processinstance.ProcessInstanceDetailView;
import io.jmix.core.LoadContext;
import io.jmix.core.Messages;
import io.jmix.flowui.*;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.Optional;

import static io.flowset.control.util.ExceptionUtils.isNotFoundError;

@Route(value = "bpm/incident/:id", layout = DefaultMainViewParent.class)
@ViewController("IncidentData.detail")
@ViewDescriptor("incident-data-detail-view.xml")
@EditedEntityContainer("incidentDataDc")
@DialogMode(minWidth = "40em", width = "80%", maxWidth = "75em")
public class IncidentDataDetailView extends StandardDetailView<IncidentData> {

    @Autowired
    protected ViewNavigators viewNavigators;
    @ViewComponent
    protected MessageBundle messageBundle;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected UiEventPublisher uiEventPublisher;
    @Autowired
    protected Messages messages;
    @Autowired
    protected Dialogs dialogs;

    @Autowired
    protected IncidentService incidentService;
    @Autowired
    protected ProcessDefinitionService processDefinitionService;
    @Autowired
    protected JobService jobService;
    @Autowired
    protected ExternalTaskService externalTaskService;

    @ViewComponent
    protected JmixButton viewStacktraceBtn;
    @ViewComponent
    protected TypedTextField<String> configurationField;
    @ViewComponent
    protected TypedTextField<String> incidentIdField;
    @ViewComponent
    protected JmixButton configurationBtn;
    @ViewComponent
    protected JmixButton viewCauseIncidentBtn;
    @ViewComponent
    protected JmixButton viewRootCauseIncidentBtn;
    @ViewComponent
    protected TypedTextField<Object> causeIncidentIdField;
    @ViewComponent
    protected TypedTextField<Object> rootCauseIncidentIdField;
    @ViewComponent
    protected JmixButton retryBtn;
    @ViewComponent
    protected TypedTextField<String> processDefinitionIdField;
    @ViewComponent
    protected HorizontalLayout detailActions;

    @ViewComponent
    protected JmixButton viewProcessBtn;
    @ViewComponent
    protected JmixButton viewProcessInstanceBtn;

    @ViewComponent
    protected BaseAction openJobAction;
    @ViewComponent
    protected BaseAction openExternalTaskAction;
    @ViewComponent
    protected CopyComponentValueToClipboardAction copyConfigurationAction;
    @ViewComponent
    protected CopyComponentValueToClipboardAction copyIdAction;

    protected String title;

    @Subscribe
    public void onInit(final InitEvent event) {
        this.title = messageBundle.getMessage("incidentDetails.title");
        copyIdAction.setTarget(incidentIdField);
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        boolean openInDialog = isOpenInDialog();
        if (!openInDialog) {
            sendUpdateViewTitleEvent();
        }
        detailActions.setJustifyContentMode(openInDialog ? FlexComponent.JustifyContentMode.END : FlexComponent.JustifyContentMode.START);
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        boolean openInDialog = isOpenInDialog();
        if (openInDialog) {
            addClassNames(LumoUtility.Padding.Top.XSMALL, LumoUtility.Padding.Bottom.LARGE);
        } else {
            detailActions.addClassNames("sticky-buttons-bottom-panel");
        }
        initIncidentTypeRelatedFields();
        initProcessFields();
        initCauseIncidentFields();
        initRootCauseIncidentFields();
    }


    @Subscribe(id = "viewProcessBtn", subject = "clickListener")
    public void onViewProcessBtnClick(final ClickEvent<JmixButton> event) {
        openView(ProcessDefinitionDetailView.class, new RouteParameters("id", getEditedEntity().getProcessDefinitionId()));
    }

    @Subscribe(id = "viewCauseIncidentBtn", subject = "clickListener")
    public void onViewCauseIncidentBtnClick(final ClickEvent<JmixButton> event) {
        openView(IncidentDataDetailView.class, new RouteParameters("id", getEditedEntity().getCauseIncidentId()));
    }

    @Subscribe(id = "viewRootCauseIncidentBtn", subject = "clickListener")
    public void onViewRootCauseIncidentBtnClick(final ClickEvent<JmixButton> event) {
        openView(IncidentDataDetailView.class, new RouteParameters("id", getEditedEntity().getRootCauseIncidentId()));
    }

    @Subscribe(id = "viewProcessInstanceBtn", subject = "clickListener")
    public void onViewProcessInstanceBtnClick(final ClickEvent<JmixButton> event) {
        openView(ProcessInstanceDetailView.class, new RouteParameters("id", getEditedEntity().getProcessInstanceId()));
    }

    @Subscribe(id = "viewStacktraceBtn", subject = "clickListener")
    public void onViewStacktraceBtnClick(final ClickEvent<JmixButton> event) {
        if (getEditedEntity().isJobFailed()) {
            dialogWindows.view(this, JobErrorDetailsView.class)
                    .withViewConfigurer(view -> {
                        view.setJobId(getEditedEntity().getConfiguration());
                        view.setErrorMessage(getEditedEntity().getMessage());
                    })
                    .open();
        } else if (getEditedEntity().isExternalTaskFailed()) {
            dialogWindows.view(this, ExternalTaskErrorDetailsView.class)
                    .withViewConfigurer(view -> {
                        view.setExternalTaskId(getEditedEntity().getConfiguration());
                        view.setErrorMessage(getEditedEntity().getMessage());
                    })
                    .open();
        }
    }

    @Install(to = "incidentDataDl", target = Target.DATA_LOADER)
    protected IncidentData incidentDataDlLoadDelegate(final LoadContext<IncidentData> loadContext) {
        Object id = loadContext.getId();
        if (id != null) {
            try {
                return incidentService.findRuntimeIncidentById(id.toString());
            } catch (EngineConnectionFailedException e) {
                throw new ViewEngineConnectionFailedException(e, this);
            }
        }
        return null;
    }

    @Subscribe("openJobAction")
    public void onOpenJobAction(final ActionPerformedEvent event) {
        try {
            JobData job = jobService.findById(getEditedEntity().getConfiguration());
            dialogWindows.detail(this, JobData.class)
                    .editEntity(job)
                    .open();
        } catch (FeignException e) {
            if (isNotFoundError(e)) {
                notifications.create(messageBundle.getMessage("jobNotFound"))
                        .withType(Notifications.Type.WARNING)
                        .show();
            }
        }

    }

    @Subscribe("openExternalTaskAction")
    public void onOpenExternalTaskAction(final ActionPerformedEvent event) {
        try {
            ExternalTaskData externalTask = externalTaskService.findById(getEditedEntity().getConfiguration());
            if (externalTask != null) {
                dialogWindows.detail(this, ExternalTaskData.class)
                        .editEntity(externalTask)
                        .open();
            }
        } catch (FeignException e) {
            if (isNotFoundError(e)) {
                notifications.create(messageBundle.getMessage("externalTaskNotFound"))
                        .withType(Notifications.Type.WARNING)
                        .show();
            }
        }
    }

    @Subscribe(id = "retryBtn", subject = "clickListener")
    public void onRetryBtnClick(final ClickEvent<JmixButton> event) {
        if (getEditedEntity().isJobFailed()) {
            DialogWindow<RetryJobView> dialogWindow = dialogWindows.view(this, RetryJobView.class)
                    .withAfterCloseListener(afterClose -> {
                        if (afterClose.closedWith(StandardOutcome.SAVE)) {
                            close(StandardOutcome.SAVE);
                        }
                    })
                    .build();

            RetryJobView retryRuntimeJobView = dialogWindow.getView();
            retryRuntimeJobView.setJobId(getEditedEntity().getConfiguration());

            dialogWindow.open();
        } else if (getEditedEntity().isExternalTaskFailed()) {
            DialogWindow<RetryExternalTaskView> dialogWindow = dialogWindows.view(this, RetryExternalTaskView.class)
                    .withAfterCloseListener(closeEvent -> {
                        if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                            close(StandardOutcome.SAVE);
                        }
                    })
                    .build();

            RetryExternalTaskView retryExternalTaskView = dialogWindow.getView();
            retryExternalTaskView.setExternalTaskId(getEditedEntity().getConfiguration());

            dialogWindow.open();
        }
    }

    protected void sendUpdateViewTitleEvent() {
        String baseTitle = messageBundle.getMessage("incidentDataDetailView.baseTitle");
        this.title = messageBundle.formatMessage("incidentDataDetailView.title", getEditedEntity().getIncidentId());

        FlexLayout flexLayout = uiComponents.create(FlexLayout.class);
        flexLayout.addClassNames(LumoUtility.Margin.Left.XSMALL, LumoUtility.Gap.SMALL);
        flexLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        H5 instanceId = uiComponents.create(H5.class);
        instanceId.setHeightFull();
        instanceId.setText("\"" + getEditedEntity().getIncidentId() + "\"");
        instanceId.addClassNames(LumoUtility.TextColor.BODY);

        flexLayout.add(instanceId);

        uiEventPublisher.publishEventForCurrentUI(new TitleUpdateEvent(this, baseTitle, flexLayout));
    }

    @Override
    public String getPageTitle() {
        return title;
    }


    protected boolean isOpenInDialog() {
        return findAncestor(Dialog.class) != null;
    }

    @Override
    public boolean hasUnsavedChanges() {
        return false;
    }

    protected void openView(Class<? extends StandardView> viewClass, RouteParameters routeParameters) {
        if (!isOpenInDialog()) {
            viewNavigators.view(this, viewClass)
                    .withRouteParameters(routeParameters)
                    .withBackwardNavigation(false)
                    .navigate();
        } else {
            RouterLink routerLink = new RouterLink(viewClass, routeParameters);
            getUI().ifPresent(ui -> ui.getPage().open(routerLink.getHref()));
        }
    }


    protected void initIncidentTypeRelatedFields() {
        boolean notEmptyPayload = getEditedEntity().getConfiguration() != null;

        if (getEditedEntity().isExternalTaskFailed()) {
            viewStacktraceBtn.setVisible(notEmptyPayload);
            configurationBtn.setVisible(notEmptyPayload);
            configurationField.setLabel(messageBundle.getMessage("externalTaskIdLabel"));
            if (notEmptyPayload) {
                configurationBtn.setAction(openExternalTaskAction);
            }
        } else if (getEditedEntity().isJobFailed()) {
            configurationField.setLabel(messageBundle.getMessage("jobIdLabel"));
            configurationBtn.setVisible(notEmptyPayload);
            viewStacktraceBtn.setVisible(notEmptyPayload);
            if (notEmptyPayload) {
                configurationBtn.setAction(openJobAction);
            }
        } else {
            viewStacktraceBtn.setVisible(false);

            if (notEmptyPayload) {
                copyConfigurationAction.setTarget(configurationField);
                configurationBtn.setAction(copyConfigurationAction);
            }
        }
    }

    protected void initRootCauseIncidentFields() {
        String rootCauseIncidentLabel;
        String rootCauseIncidentId = getEditedEntity().getRootCauseIncidentId();
        boolean isRootCauseIncident = StringUtils.equals(getEditedEntity().getIncidentId(), getEditedEntity().getRootCauseIncidentId());
        retryBtn.setVisible(isRootCauseIncident && (getEditedEntity().isJobFailed()) || getEditedEntity().isExternalTaskFailed());
        if (isRootCauseIncident) {
            viewRootCauseIncidentBtn.setVisible(false);
            String processLabel = getEditedEntity().getProcessDefinitionId() != null ? processDefinitionIdField.getTypedValue() :
                    messageBundle.getMessage("withoutProcessLabel");

            rootCauseIncidentLabel = messageBundle.formatMessage("incidentWithProcess", rootCauseIncidentId, processLabel);
        } else {
            rootCauseIncidentLabel = getRelatedIncidentFieldLabel(rootCauseIncidentId);
        }
        rootCauseIncidentIdField.setValue(rootCauseIncidentLabel);
    }

    protected void initCauseIncidentFields() {
        String causeIncidentLabel;
        String causeIncidentId = getEditedEntity().getCauseIncidentId();
        if (StringUtils.equals(getEditedEntity().getIncidentId(), causeIncidentId)) {
            viewCauseIncidentBtn.setVisible(false);
            String relatedProcess = getEditedEntity().getProcessDefinitionId() != null ?
                    processDefinitionIdField.getTypedValue() : messageBundle.getMessage("withoutProcessLabel");
            causeIncidentLabel = messageBundle.formatMessage("incidentWithProcess", causeIncidentId, relatedProcess);
        } else {
            causeIncidentLabel = getRelatedIncidentFieldLabel(causeIncidentId);
        }
        causeIncidentIdField.setValue(causeIncidentLabel);
    }

    protected void initProcessFields() {
        String processLabel = getProcessLabel(getEditedEntity());
        processDefinitionIdField.setTypedValue(processLabel);

        viewProcessBtn.setVisible(getEditedEntity().getProcessDefinitionId() != null);
        viewProcessInstanceBtn.setVisible(getEditedEntity().getProcessInstanceId() != null);
    }

    protected String getRelatedIncidentFieldLabel(String relatedIncidentId) {
        String relatedIncidentLabel;
        IncidentData relatedIncident = incidentService.findRuntimeIncidentById(relatedIncidentId);
        if (relatedIncident != null) {
            String processLabel = relatedIncident.getProcessDefinitionId() != null ? getProcessLabel(relatedIncident) :
                    messageBundle.getMessage("withoutProcessLabel");

            relatedIncidentLabel = messageBundle.formatMessage("incidentWithProcess", relatedIncidentId, processLabel);
        } else {
            relatedIncidentLabel = relatedIncidentId;
        }
        return relatedIncidentLabel;
    }

    @Nullable
    protected String getProcessLabel(IncidentData incident) {
        if (incident.getProcessDefinitionId() == null) {
            return null;
        }
        ProcessDefinitionData process = processDefinitionService.getById(incident.getProcessDefinitionId());
        return Optional.ofNullable(process).map(ProcessDefinitionData::getKey).orElse(null);
    }
}
