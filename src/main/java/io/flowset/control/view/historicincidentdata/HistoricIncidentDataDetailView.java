/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.historicincidentdata;

import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.core.LoadContext;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.incident.HistoricIncidentData;
import io.flowset.control.service.incident.IncidentService;
import io.flowset.control.service.job.impl.JobServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "historic-incidents/:id", layout = DefaultMainViewParent.class)
@ViewController("HistoricIncidentData.detail")
@ViewDescriptor("historic-incident-data-detail-view.xml")
@EditedEntityContainer("historicIncidentDataDc")
public class HistoricIncidentDataDetailView extends StandardDetailView<HistoricIncidentData> {

    @Autowired
    protected IncidentService incidentService;

    @ViewComponent
    protected TypedTextField<Object> configurationField;
    @Autowired
    protected Messages messages;
    @ViewComponent
    protected TypedTextField<Object> causeIncidentIdField;
    @ViewComponent
    protected TypedTextField<Object> rootCauseIncidentIdField;
    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected JobServiceImpl jobService;

    @ViewComponent
    protected JmixTextArea messageField;

    @Subscribe
    public void onInit(final InitEvent event) {
        addClassNames(LumoUtility.Padding.Top.XSMALL);
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        initIncidentTypeRelatedFields();
        initCauseIncidentFields();
        initRootCauseIncidentFields();
        messageField.getStyle().set("resize", "vertical");
        messageField.getStyle().set("overflow", "auto");
    }

    @Install(to = "historicIncidentDataDl", target = Target.DATA_LOADER)
    protected HistoricIncidentData historicIncidentDataDlLoadDelegate(final LoadContext<HistoricIncidentData> loadContext) {
        Object id = loadContext.getId();
        if (id != null) {
            return incidentService.findHistoricIncidentById(id.toString());
        }
        return null;
    }

    protected void initIncidentTypeRelatedFields() {
        if (getEditedEntity().isExternalTaskFailed()) {
            configurationField.setLabel(messages.getMessage("io.flowset.control.view.incidentdata/externalTaskIdLabel"));
        } else if (getEditedEntity().isJobFailed()) {
            configurationField.setLabel(messages.getMessage("io.flowset.control.view.incidentdata/jobIdLabel"));
        }
    }

    protected void initCauseIncidentFields() {
        String causeIncidentLabel;
        String causeIncidentId = getEditedEntity().getCauseIncidentId();
        if (StringUtils.equals(getEditedEntity().getIncidentId(), causeIncidentId)) {
            causeIncidentLabel = messages.formatMessage("io.flowset.control.view.incidentdata", "incidentWithProcess", causeIncidentId, getEditedEntity().getProcessDefinitionKey());
        } else {
            causeIncidentLabel = getRelatedIncidentFieldLabel(causeIncidentId);
        }
        causeIncidentIdField.setValue(causeIncidentLabel);
    }

    protected String getRelatedIncidentFieldLabel(String relatedIncidentId) {
        String relatedIncidentLabel;
        HistoricIncidentData relatedIncident = incidentService.findHistoricIncidentById(relatedIncidentId);
        if (relatedIncident != null) {
            relatedIncidentLabel = messages.formatMessage("io.flowset.control.view.incidentdata", "incidentWithProcess", relatedIncidentId, relatedIncident.getProcessDefinitionKey());
        } else {
            relatedIncidentLabel = relatedIncidentId;
        }
        return relatedIncidentLabel;
    }

    protected void initRootCauseIncidentFields() {
        String rootCauseIncidentLabel;
        String rootCauseIncidentId = getEditedEntity().getRootCauseIncidentId();
        boolean sameIncident = StringUtils.equals(getEditedEntity().getIncidentId(), getEditedEntity().getRootCauseIncidentId());
        if (sameIncident) {
            rootCauseIncidentLabel = messages.formatMessage("io.flowset.control.view.incidentdata", "incidentWithProcess", rootCauseIncidentId, getEditedEntity().getProcessDefinitionKey());
        } else {
            rootCauseIncidentLabel = getRelatedIncidentFieldLabel(rootCauseIncidentId);
        }
        rootCauseIncidentIdField.setValue(rootCauseIncidentLabel);
    }
}
