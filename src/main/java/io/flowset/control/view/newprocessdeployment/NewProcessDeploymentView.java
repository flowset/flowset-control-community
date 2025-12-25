/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.newprocessdeployment;


import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.core.Metadata;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.component.upload.FileUploadField;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.upload.event.FileUploadSucceededEvent;
import io.jmix.flowui.view.*;
import io.flowset.control.dto.BpmProcessDefinition;
import io.flowset.control.entity.filter.ProcessDefinitionFilter;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.exception.RemoteEngineParseException;
import io.flowset.control.exception.RemoteProcessEngineException;
import io.flowset.control.restsupport.camunda.ResourceReport;
import io.flowset.control.service.deployment.DeploymentContext;
import io.flowset.control.service.deployment.DeploymentService;
import io.flowset.control.service.processdefinition.ProcessDefinitionLoadContext;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import io.flowset.control.view.AbstractResourceDeploymentView;
import io.flowset.uikit.component.bpmnviewer.event.XmlImportCompleteEvent;
import io.flowset.uikit.fragment.bpmnviewer.BpmnViewerFragment;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.flowset.control.util.BpmParseUtil.parseProcessDefinitionsJson;

@Route(value = "bpm/new-process-deployment", layout = DefaultMainViewParent.class)
@ViewController("bpm_NewProcessDeploymentView")
@ViewDescriptor("new-process-deployment-view.xml")
@Slf4j
public class NewProcessDeploymentView extends AbstractResourceDeploymentView {

    @ViewComponent
    protected FileUploadField bpmnXmlUploadField;

    @Autowired
    protected DeploymentService deploymentService;
    @Autowired
    protected ProcessDefinitionService processDefinitionService;

    @Autowired
    protected Metadata metadata;
    @ViewComponent
    protected MessageBundle messageBundle;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected Fragments fragments;

    @ViewComponent
    protected VerticalLayout previewVBox;
    @ViewComponent
    protected Span emptyPreviewText;
    @ViewComponent
    protected HorizontalLayout emptyPreviewHBox;

    @ViewComponent
    protected JmixButton okBtn;

    @ViewComponent
    protected HorizontalLayout processInfoHBox;
    @ViewComponent
    protected Icon processCountInfoIcon;
    @ViewComponent
    protected H4 collaborationLabel;
    @ViewComponent
    protected Span processCountLabel;
    @ViewComponent
    protected H4 processLabel;
    @ViewComponent
    protected Span processIdLabel;

    @ViewComponent
    protected BpmnViewerFragment viewerFragment;

    protected List<BpmProcessDefinition> processDefinitions = new ArrayList<>();

    @Subscribe
    public void onInit(final InitEvent event) {
        bpmnXmlUploadField.addClassNames(LumoUtility.Padding.Top.NONE);
        initEmptyPreviewStyles();
        initProcessInfoHBoxStyles();
        initDeploymentErrorsButton();
    }

    @Subscribe(id = "okBtn", subject = "clickListener")
    public void onOkBtnClick(final ClickEvent<JmixButton> event) {
        byte[] uploadedXml = bpmnXmlUploadField.getValue();
        if (uploadedXml == null) {
            notifications.create(messageBundle.getMessage("bpmnFileNotSelected"))
                    .withType(Notifications.Type.ERROR)
                    .show();
            return;
        }
        List<ProcessDefinitionData> existingProcesses = findExistingProcessesByKeys();

        dialogs.createOptionDialog()
                .withHeader(messageBundle.getMessage("createDeploymentConfirmDialog.header"))
                .withContent(createConfirmDialogContent(existingProcesses))
                .withWidth("35em")
                .withActions(
                        new DialogAction(DialogAction.Type.YES)
                                .withHandler(e -> deployBpmnXml(uploadedXml))
                                .withText(messageBundle.getMessage("deploy"))
                                .withIcon(VaadinIcon.ROCKET)
                                .withVariant(ActionVariant.PRIMARY)
                        ,
                        new DialogAction(DialogAction.Type.CANCEL)
                                .withIcon(ComponentUtils.convertToIcon(VaadinIcon.BAN))
                )
                .open();

    }

    @Subscribe("bpmnXmlUploadField")
    public void onBpmnXmlUploadFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<FileUploadField, ?> event) {
        boolean emptyValue = event.getValue() == null;
        viewerFragment.setVisible(!emptyValue);

        emptyPreviewHBox.setVisible(emptyValue);

        okBtn.setEnabled(!emptyValue);
        if (emptyValue) {
            this.processDefinitions = new ArrayList<>();
            processInfoHBox.setVisible(false);
        }

        errorsBtn.setVisible(false);
    }

    @Subscribe("bpmnXmlUploadField")
    public void onBpmnXmlUploadFieldFileUploadSucceeded(final FileUploadSucceededEvent<FileUploadField> event) {
        if (bpmnXmlUploadField.getValue() != null) {
            String processDefinitionBpmnXml = new String(bpmnXmlUploadField.getValue(), StandardCharsets.UTF_8);

            viewerFragment.initViewer(processDefinitionBpmnXml);
            viewerFragment.addImportCompleteListener(this::updateImportedProcesses);
            viewerFragment.setVisible(true);

            emptyPreviewHBox.setVisible(false);
        }

        errorsBtn.setVisible(false);
    }

    @Subscribe(id = "cancelBtn", subject = "clickListener")
    public void onCancelBtnClick(final ClickEvent<JmixButton> event) {
        close(StandardOutcome.CLOSE);
    }

    protected void initEmptyPreviewStyles() {
        previewVBox.addClassNames(LumoUtility.Border.ALL,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.BorderColor.CONTRAST_20);
        emptyPreviewText.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.Width.LARGE, LumoUtility.Height.LARGE);
    }

    protected void initProcessInfoHBoxStyles() {
        processInfoHBox.addClassNames(LumoUtility.Margin.Left.AUTO);
        processCountLabel.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.TextColor.SECONDARY, LumoUtility.FontWeight.MEDIUM);
        processIdLabel.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.TextColor.SECONDARY, LumoUtility.FontWeight.MEDIUM);
        processCountInfoIcon.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.PRIMARY);
        setProcessCountTooltipClassName();
    }

    protected void deployBpmnXml(byte[] uploadedXml) {
        deploymentReportDc.setItem(null);

        String uploadedFileName = bpmnXmlUploadField.getUploadedFileName();

        try (InputStream inputStream = new ByteArrayInputStream(uploadedXml)) {
            DeploymentWithDefinitions result = deploymentService.createDeployment(new DeploymentContext()
                    .withResource(uploadedFileName, inputStream));
            List<ProcessDefinition> deployedProcessDefinitions = result.getDeployedProcessDefinitions();
            int size = CollectionUtils.size(deployedProcessDefinitions);

            notifications.create(messageBundle.formatMessage("processesDeployed", size))
                    .withType(Notifications.Type.SUCCESS)
                    .withDuration(2000)
                    .show();

            close(StandardOutcome.SAVE);
        } catch (IOException ex) {
            log.error("Error on uploaded file reading ", ex);
            notifications.create(messageBundle.getMessage("errorOnUploadedFileReading"))
                    .withType(Notifications.Type.ERROR)
                    .withDuration(2000)
                    .show();
        } catch (Exception ex) {
            log.error("Error on process deployment", ex);

            Throwable rootCause = ExceptionUtils.getRootCause(ex);
            if (rootCause instanceof RemoteEngineParseException parseException) {
                ResourceReport resourceReport = parseException.getDetails().get(uploadedFileName);
                handleResourceReport(resourceReport, uploadedFileName);
            } else if (ex instanceof RemoteProcessEngineException) {
                notifications.create(messageBundle.getMessage("processesNotDeployed"), ex.getMessage())
                        .withType(Notifications.Type.ERROR)
                        .withDuration(2000)
                        .show();
            } else {
                throw ex;
            }
        }
    }

    @Nullable
    protected List<ProcessDefinitionData> findExistingProcessesByKeys() {
        List<ProcessDefinitionData> existingProcesses = null;
        if (CollectionUtils.isNotEmpty(processDefinitions)) {
            ProcessDefinitionFilter filter = metadata.create(ProcessDefinitionFilter.class);
            filter.setKeyIn(processDefinitions.stream().map(BpmProcessDefinition::getKey).toList());
            existingProcesses = processDefinitionService.findAll(new ProcessDefinitionLoadContext().setFilter(filter));
        }
        return existingProcesses;
    }

    protected void updateImportedProcesses(XmlImportCompleteEvent importCompleteEvent) {
        this.processDefinitions = parseProcessDefinitionsJson(importCompleteEvent.getProcessDefinitionsJson());
        updateProcessCountComponents();
    }

    protected void updateProcessCountComponents() {
        boolean hasProcessDefinitions = !processDefinitions.isEmpty();
        if (!hasProcessDefinitions) {
            processInfoHBox.setVisible(false);
            return;
        }
        boolean multipleProcessDefinitions = processDefinitions.size() > 1;

        if (multipleProcessDefinitions) {
            Tooltip tooltip = processCountInfoIcon.getTooltip();
            String processDefinitionsString = getProcessDefinitionsString();
            tooltip.setText(processDefinitionsString);
            processCountLabel.setText(messageBundle.formatMessage("collaborationProcessesLabel", String.valueOf(processDefinitions.size())));
        } else {
            processIdLabel.setText(processDefinitions.get(0).getKey());
        }

        processInfoHBox.setVisible(true);

        //Processes collaboration fields
        processCountInfoIcon.setVisible(multipleProcessDefinitions);
        processCountLabel.setVisible(multipleProcessDefinitions);
        collaborationLabel.setVisible(multipleProcessDefinitions);

        //Single process fields
        processIdLabel.setVisible(!multipleProcessDefinitions);
        processLabel.setVisible(!multipleProcessDefinitions);
    }

    protected String getProcessDefinitionsString() {
        return processDefinitions.stream().map(bpmProcessDefinition -> {
            int idx = processDefinitions.indexOf(bpmProcessDefinition) + 1;
            return messageBundle.formatMessage("importedProcessKeyAndName", idx, StringUtils.defaultIfEmpty(bpmProcessDefinition.getKey(), "-"),
                    StringUtils.defaultIfEmpty(bpmProcessDefinition.getName(), "-"));
        }).collect(Collectors.joining("\n"));
    }

    protected void setProcessCountTooltipClassName() {
        //workaround to update a CSS class name for tooltip
        processCountInfoIcon.getElement().executeJs(
                """
                           if ($0.getElementsByTagName('vaadin-tooltip').length == 1) {
                               $0.getElementsByTagName('vaadin-tooltip')[0]._overlayElement.setAttribute('class','process-tooltip');
                           } else {
                               const tooltips = document.getElementsByTagName('vaadin-tooltip');
                               for (let i=0; i<tooltips.length; i++ ) {
                                   const tooltip = tooltips[i];
                                   if (tooltip._overlayElement.id === $0.getAttribute('aria-describedBy')) {
                                       tooltip._overlayElement.setAttribute('class','process-tooltip')
                                   }
                               }
                           }
                        """, processCountInfoIcon);
    }

    protected VerticalLayout createConfirmDialogContent(List<ProcessDefinitionData> existingProcesses) {
        DeploymentConfirmContentFragment fragment = fragments.create(this, DeploymentConfirmContentFragment.class);
        fragment.setExistingProcesses(existingProcesses);
        fragment.setDeployingProcesses(processDefinitions);
        return fragment.getContent();
    }
}