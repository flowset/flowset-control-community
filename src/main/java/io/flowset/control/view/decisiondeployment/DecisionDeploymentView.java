/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.decisiondeployment;


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
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.flowset.control.dto.DmnDecisionDefinition;
import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.entity.filter.DecisionDefinitionFilter;
import io.flowset.control.exception.RemoteProcessEngineException;
import io.flowset.control.restsupport.camunda.ResourceReport;
import io.flowset.control.service.decisiondefinition.DecisionDefinitionLoadContext;
import io.flowset.control.service.decisiondefinition.DecisionDefinitionService;
import io.flowset.control.service.deployment.DeploymentContext;
import io.flowset.control.service.deployment.DeploymentService;
import io.flowset.control.view.AbstractResourceDeploymentView;
import io.flowset.uikit.component.dmnviewer.event.DmnXmlImportCompleteEvent;
import io.flowset.uikit.fragment.dmnviewer.DmnViewerFragment;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.flowset.control.util.BpmParseUtil.parseDecisionsDefinitionsJson;

@Route(value = "bpm/decision-deployment", layout = DefaultMainViewParent.class)
@ViewController("bpm_DecisionDeploymentView")
@ViewDescriptor("decision-deployment-view.xml")
@Slf4j
public class DecisionDeploymentView extends AbstractResourceDeploymentView {

    @Autowired
    protected DeploymentService deploymentService;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected Fragments fragments;
    @Autowired
    protected DecisionDefinitionService decisionDefinitionService;

    @ViewComponent
    protected VerticalLayout previewVBox;
    @ViewComponent
    protected Span emptyPreviewText;
    @ViewComponent
    protected HorizontalLayout emptyPreviewHBox;
    @ViewComponent
    protected MessageBundle messageBundle;
    @ViewComponent
    protected JmixButton okBtn;
    @ViewComponent
    protected HorizontalLayout decisionInfoHBox;
    @ViewComponent
    protected Icon decisionCountInfoIcon;
    @ViewComponent
    protected H4 collaborationLabel;
    @ViewComponent
    protected Span decisionCountLabel;
    @ViewComponent
    protected H4 decisionLabel;
    @ViewComponent
    protected Span decisionIdLabel;
    @ViewComponent
    protected FileUploadField resourceUploadField;
    @ViewComponent
    protected DmnViewerFragment viewerFragment;

    protected List<DmnDecisionDefinition> decisionDefinitions = new ArrayList<>();

    @Subscribe
    public void onInit(final InitEvent event) {
        resourceUploadField.addClassNames(LumoUtility.Padding.Top.NONE);
        initEmptyPreviewStyles();
        initDecisionInfoHBoxStyles();
        initDeploymentErrorsButton();
    }

    @Subscribe(id = "okBtn", subject = "clickListener")
    public void onOkBtnClick(final ClickEvent<JmixButton> event) {
        byte[] uploadedXml = resourceUploadField.getValue();
        if (uploadedXml == null) {
            notifications.create(messageBundle.getMessage("bpmnFileNotSelected"))
                    .withType(Notifications.Type.ERROR)
                    .show();
            return;
        }
        List<DecisionDefinitionData> existingDecisions = findExistingDecisionDefinitionsByKeys();
        dialogs.createOptionDialog()
                .withHeader(messageBundle.getMessage("createDeploymentConfirmDialog.header"))
                .withContent(createConfirmDialogContent(existingDecisions))
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

    @Subscribe("resourceUploadField")
    public void onBpmnXmlUploadFieldComponentValueChange(
            final AbstractField.ComponentValueChangeEvent<FileUploadField, ?> event) {
        boolean emptyValue = event.getValue() == null;
        viewerFragment.setVisible(!emptyValue);
        emptyPreviewHBox.setVisible(emptyValue);
        okBtn.setEnabled(!emptyValue);
        if (emptyValue) {
            this.decisionDefinitions = new ArrayList<>();
            decisionInfoHBox.setVisible(false);
        }

        errorsBtn.setVisible(false);
    }

    @Subscribe("resourceUploadField")
    public void onBpmnXmlUploadFieldFileUploadSucceeded(final FileUploadSucceededEvent<FileUploadField> event) {
        if (resourceUploadField.getValue() != null) {
            String decisionDefinitionXml = new String(resourceUploadField.getValue(), StandardCharsets.UTF_8);

            viewerFragment.initViewer();
            viewerFragment.setDmnXml(decisionDefinitionXml);
            viewerFragment.addImportCompleteListener(this::updateImportedDecisions);
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
        emptyPreviewText.addClassNames(
                LumoUtility.TextColor.SECONDARY, LumoUtility.Width.LARGE, LumoUtility.Height.LARGE);
    }

    protected void initDecisionInfoHBoxStyles() {
        decisionInfoHBox.addClassNames(LumoUtility.Margin.Left.AUTO);
        decisionCountLabel.addClassNames(
                LumoUtility.FontSize.LARGE, LumoUtility.TextColor.SECONDARY, LumoUtility.FontWeight.MEDIUM);
        decisionIdLabel.addClassNames(
                LumoUtility.FontSize.LARGE, LumoUtility.TextColor.SECONDARY, LumoUtility.FontWeight.MEDIUM);
        decisionCountInfoIcon.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.PRIMARY);
        setDecisionsCountTooltipClassName();
    }

    protected void deployBpmnXml(byte[] uploadedXml) {
        deploymentReportDc.setItem(null);

        String uploadedFileName = resourceUploadField.getUploadedFileName();
        try (InputStream inputStream = new ByteArrayInputStream(uploadedXml)) {
            DeploymentWithDefinitions result = deploymentService.createDeployment(new DeploymentContext()
                    .withResource(uploadedFileName, inputStream));
            List<DecisionDefinition> deployedDecisionDefinitions = result.getDeployedDecisionDefinitions();
            int size = CollectionUtils.size(deployedDecisionDefinitions);
            notifications.create(messageBundle.formatMessage("decisionsDeployed", size))
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
        } catch (RemoteProcessEngineException ex) {
            log.error("Error on decision deployment", ex);

            if (ex.isProcessEngineException()) {
                ResourceReport.ProblemDetails problemDetails = new ResourceReport.ProblemDetails();
                problemDetails.setMessage(ex.getResponseMessage());

                ResourceReport resourceReport = new ResourceReport();
                resourceReport.setErrors(List.of(problemDetails));

                handleResourceReport(resourceReport, uploadedFileName);

            } else {
                notifications.create(messageBundle.getMessage("decisionsNotDeployed"), ex.getMessage())
                        .withType(Notifications.Type.ERROR)
                        .withDuration(2000)
                        .show();
            }

        }
    }

    @Nullable
    protected List<DecisionDefinitionData> findExistingDecisionDefinitionsByKeys() {
        List<DecisionDefinitionData> existingDecisions = null;
        if (CollectionUtils.isNotEmpty(decisionDefinitions)) {
            Set<String> decisionDefinitionKeys = decisionDefinitions.stream()
                    .map(DmnDecisionDefinition::getKey)
                    .collect(Collectors.toSet());
            DecisionDefinitionFilter decisionDefinitionFilter = metadata.create(DecisionDefinitionFilter.class);
            decisionDefinitionFilter.setLatestVersionOnly(true);
            List<DecisionDefinitionData> allDecisionDefinitions = decisionDefinitionService.findAll(
                    new DecisionDefinitionLoadContext().setFilter(decisionDefinitionFilter));
            existingDecisions = allDecisionDefinitions.stream()
                    .filter(e -> decisionDefinitionKeys.contains(e.getKey()))
                    .toList();

        }
        return existingDecisions;
    }

    protected void updateImportedDecisions(DmnXmlImportCompleteEvent importCompleteEvent) {
        this.decisionDefinitions = parseDecisionsDefinitionsJson(importCompleteEvent.getDecisionDefinitionsJson());
        updateDecisionsCountComponents();
    }

    protected void updateDecisionsCountComponents() {
        boolean hasDecisionDefinitions = !decisionDefinitions.isEmpty();
        if (!hasDecisionDefinitions) {
            decisionInfoHBox.setVisible(false);
            return;
        }
        boolean multipleDecisionDefinitions = decisionDefinitions.size() > 1;

        if (multipleDecisionDefinitions) {
            Tooltip tooltip = decisionCountInfoIcon.getTooltip();
            String decisionDefinitionsString = getDecisionDefinitionsString();
            tooltip.setText(decisionDefinitionsString);
            decisionCountLabel.setText(messageBundle.formatMessage(
                    "collaborationDecisionsLabel", String.valueOf(decisionDefinitions.size())));
        } else {
            decisionIdLabel.setText(decisionDefinitions.get(0).getKey());
        }

        decisionInfoHBox.setVisible(true);

        decisionCountInfoIcon.setVisible(multipleDecisionDefinitions);
        decisionCountLabel.setVisible(multipleDecisionDefinitions);
        collaborationLabel.setVisible(multipleDecisionDefinitions);

        decisionIdLabel.setVisible(!multipleDecisionDefinitions);
        decisionLabel.setVisible(!multipleDecisionDefinitions);
    }

    protected String getDecisionDefinitionsString() {
        return decisionDefinitions.stream().map(decisionDefinition -> {
            int idx = decisionDefinitions.indexOf(decisionDefinition) + 1;
            return messageBundle.formatMessage("importedDecisionsKeyAndName", idx,
                    StringUtils.defaultIfEmpty(decisionDefinition.getKey(), "-"),
                    StringUtils.defaultIfEmpty(decisionDefinition.getName(), "-"));
        }).collect(Collectors.joining("\n"));
    }

    protected void setDecisionsCountTooltipClassName() {
        //workaround to update a CSS class name for tooltip
        decisionCountInfoIcon.getElement().executeJs(
                """
                            if ($0.getElementsByTagName('vaadin-tooltip').length == 1) {
                               $0.getElementsByTagName('vaadin-tooltip')[0]._overlayElement.setAttribute(
                                   'class','decision-tooltip');
                            } else {
                               const tooltips = document.getElementsByTagName('vaadin-tooltip');
                               for (let i=0; i<tooltips.length; i++ ) {
                                   const tooltip = tooltips[i];
                                   if (tooltip._overlayElement.id === $0.getAttribute('aria-describedBy')) {
                                       tooltip._overlayElement.setAttribute('class','decision-tooltip')
                                   }
                               }
                            }
                        """, decisionCountInfoIcon);
    }

    protected VerticalLayout createConfirmDialogContent(List<DecisionDefinitionData> existingDecisions) {
        DecisionDeploymentConfirmContentFragment fragment =
                fragments.create(this, DecisionDeploymentConfirmContentFragment.class);
        fragment.setExistingDecisions(existingDecisions);
        fragment.setDeployingDecisions(decisionDefinitions);
        return fragment.getContent();
    }
}