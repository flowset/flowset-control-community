/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.incidentdata;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.action.ViewProcessDefinitionAction;
import io.flowset.control.action.ViewProcessInstanceAction;
import io.flowset.control.action.CopyComponentValueToClipboardAction;
import io.flowset.control.action.incident.ViewIncidentExternalTaskAction;
import io.flowset.control.action.incident.ViewIncidentJobAction;
import io.flowset.control.action.incident.RetryIncidentAction;
import io.flowset.control.action.incident.ViewIncidentAction;
import io.flowset.control.action.incident.ViewIncidentStacktraceAction;
import io.flowset.control.entity.incident.IncidentData;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.exception.ViewEngineConnectionFailedException;
import io.flowset.control.service.incident.IncidentService;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import io.flowset.control.view.event.TitleUpdateEvent;
import io.jmix.core.LoadContext;
import io.jmix.flowui.*;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.Optional;

@Route(value = "bpm/incidents/:id", layout = DefaultMainViewParent.class)
@ViewController("IncidentData.detail")
@ViewDescriptor("incident-data-detail-view.xml")
@EditedEntityContainer("incidentDataDc")
@DialogMode(minWidth = "40em", width = "80%", maxWidth = "75em")
public class IncidentDataDetailView extends StandardDetailView<IncidentData> {

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
    protected IncidentService incidentService;
    @Autowired
    protected ProcessDefinitionService processDefinitionService;
    @ViewComponent
    protected RetryIncidentAction retryAction;
    @ViewComponent
    protected ViewIncidentStacktraceAction viewStacktraceAction;
    @ViewComponent
    protected ViewIncidentJobAction viewJobAction;
    @ViewComponent
    protected ViewIncidentExternalTaskAction viewExternalTaskAction;
    @ViewComponent
    protected ViewIncidentAction viewCauseIncidentAction;
    @ViewComponent
    protected ViewIncidentAction viewRootCauseIncidentAction;
    @ViewComponent
    protected ViewProcessDefinitionAction viewProcessAction;
    @ViewComponent
    protected ViewProcessInstanceAction viewProcessInstanceAction;
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
    protected TypedTextField<String> processDefinitionIdField;
    @ViewComponent
    protected HorizontalLayout detailActions;

    @ViewComponent
    protected JmixButton viewProcessBtn;
    @ViewComponent
    protected JmixButton viewProcessInstanceBtn;

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
        initActions();
        initIncidentTypeRelatedFields();
        initProcessFields();
        initCauseIncidentFields();
        initRootCauseIncidentFields();
    }

    protected void initActions() {
        IncidentData incidentData = getEditedEntity();

        retryAction.setIncidentData(incidentData);
        retryAction.setAfterSaveHandler(() -> close(StandardOutcome.SAVE));

        viewStacktraceAction.setIncidentData(incidentData);
        viewCauseIncidentAction.setEntityId(incidentData.getCauseIncidentId());
        viewRootCauseIncidentAction.setEntityId(incidentData.getRootCauseIncidentId());
        viewProcessAction.setEntityId(incidentData.getProcessDefinitionId());
        viewProcessInstanceAction.setEntityId(incidentData.getProcessInstanceId());

        viewJobAction.setIncidentData(incidentData);
        viewExternalTaskAction.setIncidentData(incidentData);
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

    protected void initIncidentTypeRelatedFields() {
        boolean notEmptyPayload = getEditedEntity().getConfiguration() != null;

        if (getEditedEntity().isExternalTaskFailed()) {
            viewStacktraceAction.setVisible(notEmptyPayload);
            configurationBtn.setVisible(notEmptyPayload);
            configurationField.setLabel(messageBundle.getMessage("externalTaskIdLabel"));
            if (notEmptyPayload) {
                configurationBtn.setAction(viewExternalTaskAction);
            }
        } else if (getEditedEntity().isJobFailed()) {
            configurationField.setLabel(messageBundle.getMessage("jobIdLabel"));
            configurationBtn.setVisible(notEmptyPayload);
            viewStacktraceAction.setVisible(notEmptyPayload);
            if (notEmptyPayload) {
                configurationBtn.setAction(viewJobAction);
            }
        } else {
            viewStacktraceAction.setVisible(false);

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
        retryAction.setVisible(isRootCauseIncident && (getEditedEntity().isJobFailed()) || getEditedEntity().isExternalTaskFailed());
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
