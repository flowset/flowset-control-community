/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.deploymentupload;

import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Content of the confirmation dialog of the {@link DeploymentUploadView}: lists the definitions and the resources
 * that will be created by the deployment, split by type. Processes and decisions are marked as a new definition
 * or a new version of an existing definition, depending on whether a definition with the same key exists in the
 * engine. Camunda Forms cannot be looked up in the engine, so they are marked as deployed only.
 */
@FragmentDescriptor("deployment-upload-confirm-fragment.xml")
public class DeploymentUploadConfirmFragment extends Fragment<VerticalLayout> {

    @ViewComponent
    protected MessageBundle messageBundle;
    @ViewComponent
    protected Span summaryText;
    @ViewComponent
    protected Span deploymentNameText;
    @ViewComponent
    protected Span skipUnchangedText;
    @ViewComponent
    protected HorizontalLayout existingDefinitionsUnknownBox;
    @ViewComponent
    protected Icon unknownWarningIcon;
    @ViewComponent
    protected VerticalLayout sectionsBox;

    @Subscribe
    public void onReady(final ReadyEvent event) {
        unknownWarningIcon.addClassNames(LumoUtility.TextColor.WARNING);
        existingDefinitionsUnknownBox.addClassNames(LumoUtility.Padding.SMALL,
                LumoUtility.BorderColor.WARNING, LumoUtility.Border.ALL, LumoUtility.BorderRadius.LARGE,
                LumoUtility.Background.WARNING_10);
        deploymentNameText.addClassNames(LumoUtility.TextColor.SECONDARY);
        skipUnchangedText.addClassNames(LumoUtility.TextColor.SECONDARY);
    }

    /**
     * Fills the dialog content.
     *
     * @param data what will be deployed and which definitions already exist in the engine
     */
    public void setData(ConfirmData data) {
        deploymentNameText.setText(messageBundle.formatMessage("confirmDialog.deploymentName",
                StringUtils.defaultIfBlank(data.deploymentName(), messageBundle.getMessage("confirmDialog.noName"))));
        skipUnchangedText.setText(messageBundle.getMessage(data.skipUnchanged()
                ? "confirmDialog.skipUnchanged.yes"
                : "confirmDialog.skipUnchanged.no"));
        summaryText.setText(messageBundle.formatMessage("confirmDialog.summary",
                data.processKeys().size(), data.decisionKeys().size(), data.formKeys().size(),
                data.otherFileNames().size()));

        boolean existingUnknown = (!data.processKeys().isEmpty() && data.existingProcessKeys() == null)
                || (!data.decisionKeys().isEmpty() && data.existingDecisionKeys() == null);
        existingDefinitionsUnknownBox.setVisible(existingUnknown);

        sectionsBox.removeAll();
        addDefinitionSection("confirmDialog.processes", data.processKeys(), data.existingProcessKeys());
        addDefinitionSection("confirmDialog.decisions", data.decisionKeys(), data.existingDecisionKeys());
        addFormSection(data.formKeys());
        addOtherResourcesSection(data.otherFileNames());
    }

    protected void addDefinitionSection(String headerKey, List<String> keys, @Nullable Set<String> existingKeys) {
        if (keys.isEmpty()) {
            return;
        }
        VerticalLayout section = createSection(headerKey, keys.size());
        for (String key : keys) {
            Span badge;
            if (existingKeys == null) {
                badge = createBadge(messageBundle.getMessage("confirmDialog.versionUnknown"), "badge contrast");
            } else if (existingKeys.contains(key)) {
                badge = createBadge(messageBundle.getMessage("confirmDialog.newVersion"), "badge");
            } else {
                badge = createBadge(messageBundle.getMessage("confirmDialog.newDefinition"), "badge success");
            }
            section.add(createRow(key, badge));
        }
        sectionsBox.add(section);
    }

    protected void addFormSection(List<String> formKeys) {
        if (formKeys.isEmpty()) {
            return;
        }
        VerticalLayout section = createSection("confirmDialog.forms", formKeys.size());
        Span note = uiComponents.create(Span.class);
        note.setText(messageBundle.getMessage("formNoVersionInfo"));
        note.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);
        section.add(note);
        for (String key : formKeys) {
            section.add(createRow(key, createBadge(messageBundle.getMessage("confirmDialog.deployed"),
                    "badge contrast")));
        }
        sectionsBox.add(section);
    }

    protected void addOtherResourcesSection(List<String> fileNames) {
        if (fileNames.isEmpty()) {
            return;
        }
        VerticalLayout section = createSection("confirmDialog.otherResources", fileNames.size());
        for (String fileName : fileNames) {
            section.add(createRow(fileName, null));
        }
        sectionsBox.add(section);
    }

    protected VerticalLayout createSection(String headerKey, int count) {
        VerticalLayout section = uiComponents.create(VerticalLayout.class);
        section.setPadding(false);
        section.setSpacing(false);
        section.setWidthFull();
        section.addClassNames(LumoUtility.Gap.XSMALL);

        H5 header = uiComponents.create(H5.class);
        header.setText(messageBundle.formatMessage(headerKey, count));
        section.add(header);
        return section;
    }

    protected HorizontalLayout createRow(String text, @Nullable Span badge) {
        HorizontalLayout row = uiComponents.create(HorizontalLayout.class);
        row.setPadding(false);
        row.setSpacing(false);
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.addClassNames(LumoUtility.Gap.SMALL);

        Span textSpan = uiComponents.create(Span.class);
        textSpan.setText(text);
        textSpan.addClassNames(LumoUtility.Overflow.HIDDEN, LumoUtility.TextOverflow.ELLIPSIS);
        textSpan.getElement().setAttribute("title", text);
        row.add(textSpan);
        if (badge != null) {
            row.add(badge);
        }
        return row;
    }

    protected Span createBadge(String text, String themeNames) {
        Span badge = uiComponents.create(Span.class);
        badge.setText(text);
        badge.getElement().getThemeList().add(themeNames);
        return badge;
    }

    /**
     * What will be deployed and which definitions already exist in the engine.
     *
     * @param deploymentName       deployment name, may be blank
     * @param skipUnchanged        whether unchanged resources are skipped (duplicate filtering)
     * @param processKeys          keys of the processes of the deployed BPMN files
     * @param decisionKeys         keys of the decisions of the deployed DMN files
     * @param formKeys             ids of the deployed Camunda Forms
     * @param otherFileNames       names of the other deployed files: HTML forms, scripts, images
     * @param existingProcessKeys  keys of the processes that already exist in the engine, {@code null} if unknown
     * @param existingDecisionKeys keys of the decisions that already exist in the engine, {@code null} if unknown
     */
    public record ConfirmData(@Nullable String deploymentName,
                              boolean skipUnchanged,
                              List<String> processKeys,
                              List<String> decisionKeys,
                              List<String> formKeys,
                              List<String> otherFileNames,
                              @Nullable Set<String> existingProcessKeys,
                              @Nullable Set<String> existingDecisionKeys) {
    }
}
