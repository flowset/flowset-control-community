/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.deploymentresource;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import io.flowset.control.entity.deployment.DeploymentResourceType;
import io.flowset.uikit.fragment.bpmnviewer.BpmnViewerFragment;
import io.flowset.uikit.fragment.dmnviewer.DmnViewerFragment;
import io.flowset.uikit.fragment.formviewer.FormViewerFragment;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.component.codeeditor.CodeEditorMode;
import io.jmix.flowui.kit.component.codeeditor.JmixCodeEditor;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.ViewComponent;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Shows a preview of a deployment resource: a diagram (BPMN), a decision table (DMN), a form (Camunda Form,
 * HTML form), an image or a text, with an additional tab containing the resource source.
 */
@FragmentDescriptor("deployment-resource-preview-fragment.xml")
public class DeploymentResourcePreviewFragment extends Fragment<VerticalLayout> {

    @Autowired
    protected Fragments fragments;

    @ViewComponent
    protected MessageBundle messageBundle;
    @ViewComponent
    protected JmixTabSheet resourceTabSheet;
    @ViewComponent
    protected Div emptyResourceMessageContainer;

    /**
     * Shows the preview of the resource. The preview kind is selected by the file name extension.
     *
     * @param fileName resource file name
     * @param content  resource content
     */
    public void showResource(String fileName, byte[] content) {
        clearTabSheet();

        resourceTabSheet.setVisible(true);
        emptyResourceMessageContainer.setVisible(false);

        DeploymentResourceType type = DeploymentResourceType.fromFileName(fileName);
        if (type == null) {
            showUnsupportedResource(content);
            return;
        }
        switch (type) {
            case BPMN -> showBpmn(content);
            case DMN -> showDmn(content);
            case CAMUNDA_FORM -> showForm(content);
            case HTML_FORM -> showHtml(content);
            case IMAGE -> showImage(fileName, content);
            case SCRIPT -> showUnsupportedResource(content);
        }
    }

    /**
     * Removes the preview and shows the "select resource" message.
     */
    public void clear() {
        clearTabSheet();
        resourceTabSheet.setVisible(false);
        emptyResourceMessageContainer.setVisible(true);
    }

    /**
     * @return the tab sheet containing the preview tabs, e.g. to set a suffix component or add extra tabs
     */
    public JmixTabSheet getResourceTabSheet() {
        return resourceTabSheet;
    }

    /**
     * Adds a tab to the preview tab sheet.
     *
     * @param tabId   tab id
     * @param icon    tab icon
     * @param label   tab label
     * @param content tab content
     */
    public void addTab(String tabId, @Nullable Icon icon, String label, Component content) {
        Tab tab = uiComponents.create(Tab.class);
        tab.setId(tabId);
        tab.setLabel(label);
        if (icon != null) {
            tab.addComponentAsFirst(icon);
        }
        resourceTabSheet.add(tab, content);
    }

    protected void showUnsupportedResource(byte[] content) {
        addTab("viewTab", VaadinIcon.EYE.create(), getSourceTabLabel(),
                createCodeEditor(toText(content), CodeEditorMode.TEXT));
    }

    protected void showHtml(byte[] content) {
        addTab("viewTab", VaadinIcon.EYE.create(), getViewTabLabel(), createHtmlViewer(content));
        addTab("contentTab", VaadinIcon.FILE_CODE.create(), getSourceTabLabel(),
                createCodeEditor(toText(content), CodeEditorMode.HTML));
    }

    protected void showImage(String fileName, byte[] content) {
        addTab("viewTab", VaadinIcon.PICTURE.create(), getViewTabLabel(), createImageViewer(fileName, content));
    }

    protected void showForm(byte[] content) {
        String textContent = toText(content);
        addTab("viewTab", VaadinIcon.EYE.create(), getViewTabLabel(), createFormViewer(textContent));
        addTab("contentTab", VaadinIcon.FILE_CODE.create(), getSourceTabLabel(),
                createCodeEditor(textContent, CodeEditorMode.JSON));
    }

    protected void showDmn(byte[] content) {
        String textContent = toText(content);
        addTab("viewTab", VaadinIcon.SITEMAP.create(), getViewTabLabel(), createDmnViewer(textContent));
        addTab("contentTab", VaadinIcon.FILE_CODE.create(), getSourceTabLabel(),
                createCodeEditor(textContent, CodeEditorMode.XML));
    }

    protected void showBpmn(byte[] content) {
        String textContent = toText(content);
        addTab("viewTab", VaadinIcon.SITEMAP.create(), getViewTabLabel(), createBpmnViewer(textContent));
        addTab("contentTab", VaadinIcon.FILE_CODE.create(), getSourceTabLabel(),
                createCodeEditor(textContent, CodeEditorMode.XML));
    }

    protected void clearTabSheet() {
        List<Component> tabs = new ArrayList<>(resourceTabSheet.getChildren().toList());
        tabs.forEach(component -> resourceTabSheet.remove((Tab) component));
    }

    protected Component createHtmlViewer(byte[] content) {
        String dataUrl = "data:text/html;base64," + Base64.getEncoder().encodeToString(content);

        IFrame iframe = uiComponents.create(IFrame.class);
        iframe.setSrc(dataUrl);
        iframe.setWidth("100%");
        iframe.setHeight("100%");
        iframe.getStyle().set("border", "none");
        iframe.getStyle().set("padding", "0");
        // an empty sandbox: scripts, forms, popups and same-origin access are disabled in the preview
        iframe.getElement().setAttribute("sandbox", "");
        return iframe;
    }

    protected Component createImageViewer(String fileName, byte[] content) {
        return new Image(content, fileName);
    }

    protected Component createBpmnViewer(String xmlData) {
        final BpmnViewerFragment bpmnViewerFragment = fragments.create(this, BpmnViewerFragment.class);
        bpmnViewerFragment.setId("viewerFragment");
        bpmnViewerFragment.initViewer(xmlData);
        return bpmnViewerFragment;
    }

    protected Component createDmnViewer(String xmlData) {
        final DmnViewerFragment dmnViewerFragment = fragments.create(this, DmnViewerFragment.class);
        dmnViewerFragment.setId("dmnViewerFragment");
        dmnViewerFragment.initViewer();
        dmnViewerFragment.setDmnXml(xmlData);
        return dmnViewerFragment;
    }

    protected Component createFormViewer(String jsonData) {
        final FormViewerFragment formViewerFragment = fragments.create(this, FormViewerFragment.class);
        formViewerFragment.setId("formViewerFragment");
        formViewerFragment.initViewer(jsonData);
        return formViewerFragment;
    }

    protected Component createCodeEditor(String codeEditorData, CodeEditorMode codeEditorMode) {
        JmixCodeEditor codeEditor = uiComponents.create(JmixCodeEditor.class);
        codeEditor.setId("contentCodeEditor");
        codeEditor.setMode(codeEditorMode);
        codeEditor.getStyle().set("padding", "0");
        codeEditor.setWidth("100%");
        codeEditor.setHeight("100%");
        codeEditor.setReadOnly(true);
        codeEditor.setValue(codeEditorData);
        return codeEditor;
    }

    protected String toText(byte[] content) {
        return new String(content, StandardCharsets.UTF_8);
    }

    protected String getViewTabLabel() {
        return messageBundle.getMessage("viewTab.title");
    }

    protected String getSourceTabLabel() {
        return messageBundle.getMessage("viewTab.source");
    }
}
