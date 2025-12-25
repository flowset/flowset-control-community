package io.flowset.control.view.deploymentdata;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.core.LoadContext;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.codeeditor.CodeEditorMode;
import io.jmix.flowui.kit.component.codeeditor.JmixCodeEditor;
import io.jmix.flowui.kit.component.grid.JmixGrid;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.flowset.control.action.CopyComponentValueToClipboardAction;
import io.flowset.control.entity.deployment.DeploymentData;
import io.flowset.control.entity.deployment.DeploymentProcessInstancesInfo;
import io.flowset.control.entity.deployment.DeploymentResource;
import io.flowset.control.entity.filter.ProcessDefinitionFilter;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.exception.EngineResourceNotAvailableException;
import io.flowset.control.service.deployment.DeploymentService;
import io.flowset.control.service.processdefinition.ProcessDefinitionLoadContext;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import io.flowset.control.service.processinstance.ProcessInstanceService;
import io.flowset.control.view.processdefinition.ProcessDefinitionDetailView;
import io.flowset.uikit.fragment.bpmnviewer.BpmnViewerFragment;
import io.flowset.uikit.fragment.dmnviewer.DmnViewerFragment;
import io.flowset.uikit.fragment.formviewer.FormViewerFragment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Route(value = "bpm/deployment/:id", layout = DefaultMainViewParent.class)
@ViewController(id = "bpm_Deployment.detail")
@ViewDescriptor(path = "deployment-detail-view.xml")
@EditedEntityContainer("deploymentDataDc")
@DialogMode(width = "70em", height = "40em")
@PrimaryDetailView(DeploymentData.class)
public class DeploymentDetailView extends StandardDetailView<DeploymentData> {

    private static final Pattern BPMN_PATTERN = Pattern.compile(".*\\.(bpmn\\d*\\.xml|bpmn)$");
    private static final Pattern DMN_PATTERN = Pattern.compile(".*\\.(dmn\\d*\\.xml|dmn)$");
    private static final Pattern FORM_PATTERN = Pattern.compile(".*\\.form$");
    private static final Pattern IMAGE_PATTERN = Pattern.compile(".*\\.(gif|jpg|jpeg|jpe|png|svg|tif|tiff)$");
    private static final Pattern HTML_PATTERN = Pattern.compile(".*\\.html$");

    @Autowired
    private DeploymentService deploymentService;
    @Autowired
    private Downloader downloader;
    @Autowired
    private ProcessDefinitionService processDefinitionService;
    @Autowired
    private Metadata metadata;
    @Autowired
    private ProcessInstanceService processInstanceService;
    @Autowired
    private Messages messages;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Fragments fragments;
    @Autowired
    private ViewNavigators viewNavigators;

    @ViewComponent
    private InstanceContainer<DeploymentData> deploymentDataDc;
    @ViewComponent
    private JmixTabSheet resourceTabSheet;
    @ViewComponent
    private JmixButton downloadResourceButton;
    @ViewComponent
    private Div emptyResourceMessageContainer;
    @ViewComponent
    private DataGrid<DeploymentResource> resourcesDataGrid;
    @ViewComponent
    private Span deploymentResourcesLabel;
    @ViewComponent
    private CopyComponentValueToClipboardAction copyToClipboardAction;
    @ViewComponent
    private TypedTextField<String> nameTextField;
    @ViewComponent
    private TypedTextField<String> deploymentIdTextField;

    private String viewTabLabel;
    private String sourceTabLabel;
    private String runningInstancesTabLabel;

    @Subscribe
    public void onInit(final InitEvent event) {
        viewTabLabel = messages.getMessage(getClass(), "viewTab.title");
        sourceTabLabel = messages.getMessage(getClass(), "viewTab.source");
        runningInstancesTabLabel = messages.getMessage(getClass(), "viewTab.runningInstances");

        initResourcesDataGrid();

        deploymentResourcesLabel.addClassNames(LumoUtility.TextColor.SECONDARY);
        deploymentResourcesLabel.addClassNames(LumoUtility.FontWeight.SEMIBOLD);

        addClassName(LumoUtility.Padding.Bottom.SMALL);
    }

    @Subscribe(id = "copyDeploymentId", subject = "clickListener")
    public void onCopyDeploymentIdClick(final ClickEvent<JmixButton> event) {
        copyToClipboardAction.setTarget(deploymentIdTextField);
        copyToClipboardAction.actionPerform(event.getSource());
    }

    @Subscribe(id = "copyName", subject = "clickListener")
    public void onCopyNameClick(final ClickEvent<JmixButton> event) {
        copyToClipboardAction.setTarget(nameTextField);
        copyToClipboardAction.actionPerform(event.getSource());
    }

    @Subscribe(id = "downloadResourceButton", subject = "clickListener")
    public void onDownloadResourceButtonClick(final ClickEvent<JmixButton> event) {
        DeploymentResource selectedResource = resourcesDataGrid.getSingleSelectedItem();
        if (selectedResource != null) {
            Resource deploymentResourceData = deploymentService.getDeploymentResourceData(
                    selectedResource.getDeploymentId(), selectedResource.getResourceId());
            byte[] byteArrayContent = getByteArrayContent(deploymentResourceData);
            downloader.download(() -> new ByteArrayInputStream(byteArrayContent), selectedResource.getName());
        }
    }

    @Subscribe("resourcesDataGrid")
    public void onResourcesDataGridSelection(
            final SelectionEvent<DataGrid<DeploymentResource>, DeploymentResource> event) {
        DeploymentResource selectedResourceName = event.getSource().getSingleSelectedItem();
        if (selectedResourceName == null) {
            return;
        }
        String resourceName = selectedResourceName.getName();
        Resource deploymentResourceData = deploymentService.getDeploymentResourceData(
                selectedResourceName.getDeploymentId(), selectedResourceName.getResourceId());

        resourceTabSheet.setVisible(true);
        downloadResourceButton.setVisible(true);
        emptyResourceMessageContainer.setVisible(false);

        if (BPMN_PATTERN.matcher(resourceName).matches()) {
            showBpmn(deploymentResourceData);
        } else if (DMN_PATTERN.matcher(resourceName).matches()) {
            showDmn(deploymentResourceData);
        }  else if (FORM_PATTERN.matcher(resourceName).matches()) {
            showForm(deploymentResourceData);
        }  else if (IMAGE_PATTERN.matcher(resourceName).matches()) {
            showImage(deploymentResourceData);
        }  else if (HTML_PATTERN.matcher(resourceName).matches()) {
            showHtml(deploymentResourceData);
        } else {
            showUnsupportedResource(deploymentResourceData);
        }
    }

    @Subscribe(id = "deploymentDataDc", target = Target.DATA_CONTAINER)
    protected void onDeploymentDataDcItemChange(InstanceContainer.ItemChangeEvent<DeploymentData> event) {
        DeploymentData deploymentData = event.getItem();
        if (deploymentData != null) {
            List<DeploymentResource> deploymentResourceNames = deploymentService.getDeploymentResources(
                    deploymentData.getDeploymentId());
            resourcesDataGrid.setItems(new ArrayList<>(
                    deploymentResourceNames != null ? deploymentResourceNames : List.of()));
        }
    }

    @Install(to = "deploymentDataDl", target = Target.DATA_LOADER)
    private DeploymentData customerDlLoadDelegate(final LoadContext<DeploymentData> loadContext) {
        DeploymentData item = deploymentDataDc.getItemOrNull();
        String id = item == null ? Objects.requireNonNull(loadContext.getId()).toString() : item.getId();

        return deploymentService.findById(id);
    }

    private void initResourcesDataGrid() {
        Grid.Column<DeploymentResource> instanceCountColumn = resourcesDataGrid.addColumn(
                DeploymentResource::getName);
        instanceCountColumn.setHeader(messages.getMessage(DeploymentResource.class,
                "DeploymentResource.name"));
        instanceCountColumn.setResizable(true);
    }

    private void showUnsupportedResource(Resource deploymentResourceData) {
        clearTabSheet(resourceTabSheet);

        String textContent = getTextContent(deploymentResourceData);

        createTab(resourceTabSheet, VaadinIcon.EYE.create(), sourceTabLabel,
                createCodeEditor(textContent, CodeEditorMode.TEXT));
    }

    private void showHtml(Resource deploymentResourceData) {
        clearTabSheet(resourceTabSheet);

        byte[] byteArrayContent = getByteArrayContent(deploymentResourceData);
        String textContent = getTextContent(deploymentResourceData);

        createTab(resourceTabSheet, VaadinIcon.EYE.create(), viewTabLabel, createHtmlViewer(byteArrayContent));
        createTab(resourceTabSheet, VaadinIcon.FILE_CODE.create(), sourceTabLabel,
                createCodeEditor(textContent, CodeEditorMode.HTML));
    }

    private Component createHtmlViewer(byte[] byteArrayContent) {
        String base64Html = Base64.getEncoder().encodeToString(byteArrayContent);
        String dataUrl = "data:text/html;base64," + base64Html;

        IFrame iframe = uiComponents.create(IFrame.class);
        iframe.setSrc(dataUrl);

        iframe.setWidth("100%");
        iframe.setHeight("100%");
        iframe.getStyle().set("border", "none");
        iframe.getStyle().set("padding", "0");

        return iframe;
    }

    private void showImage(Resource deploymentResourceData) {
        clearTabSheet(resourceTabSheet);

        byte[] byteArrayContent = getByteArrayContent(deploymentResourceData);

        createTab(resourceTabSheet, VaadinIcon.PICTURE.create(), viewTabLabel, createImageViewer(
                deploymentResourceData.getFilename(), byteArrayContent));
    }

    private void clearTabSheet(JmixTabSheet resourceTabSheet) {
        resourceTabSheet.getChildren().forEach(component -> resourceTabSheet.remove((Tab) component));
    }

    private Component createImageViewer(String fileName, byte[] byteArrayContent) {
        Image image = new Image();
        image.setSrc(new StreamResource(fileName, () -> new ByteArrayInputStream(byteArrayContent)));

        return image;
    }

    private void showForm(Resource deploymentResourceData) {
        clearTabSheet(resourceTabSheet);

        String textContent = getTextContent(deploymentResourceData);

        createTab(resourceTabSheet, VaadinIcon.EYE.create(), viewTabLabel, createFormViewer(textContent));
        createTab(resourceTabSheet, VaadinIcon.FILE_CODE.create(), sourceTabLabel,
                createCodeEditor(textContent, CodeEditorMode.XML));
    }

    private void showDmn(Resource deploymentResourceData) {
        clearTabSheet(resourceTabSheet);

        String textContent = getTextContent(deploymentResourceData);

        createTab(resourceTabSheet, VaadinIcon.SITEMAP.create(), viewTabLabel, createDmnViewer(textContent));
        createTab(resourceTabSheet, VaadinIcon.FILE_CODE.create(), sourceTabLabel,
                createCodeEditor(textContent, CodeEditorMode.XML));
    }

    private void showBpmn(Resource deploymentResourceData) {
        clearTabSheet(resourceTabSheet);

        String textContent = getTextContent(deploymentResourceData);

        createTab(resourceTabSheet, VaadinIcon.SITEMAP.create(), viewTabLabel, createBpmnViewer(textContent));
        createTab(resourceTabSheet, VaadinIcon.FILE_CODE.create(), sourceTabLabel,
                createCodeEditor(textContent, CodeEditorMode.XML));
        createTab(resourceTabSheet, VaadinIcon.HOURGLASS.create(), runningInstancesTabLabel,
                createProcessDefinitionViewer());
    }

    private void createTab(JmixTabSheet parent, Icon icon, String tabLabel, Component tabComponent) {
        Tab tab = uiComponents.create(Tab.class);
        tab.setLabel(tabLabel);
        tab.addComponentAsFirst(icon);
        parent.add(tab, tabComponent);
    }

    private Component createProcessDefinitionViewer() {
        ProcessDefinitionFilter filter = metadata.create(ProcessDefinitionFilter.class);

        filter.setDeploymentId(deploymentDataDc.getItem().getDeploymentId());
        filter.setLatestVersionOnly(false);
        ProcessDefinitionLoadContext context = new ProcessDefinitionLoadContext().setFilter(filter);
        List<ProcessDefinitionData> deploymentProcessDefinitions = processDefinitionService.findAll(context);

        List<DeploymentProcessInstancesInfo> deploymentProcessInstancesInfos = new ArrayList<>();
        deploymentProcessDefinitions.forEach(processDefinitionData -> {
            DeploymentProcessInstancesInfo deploymentProcessInstancesInfo =
                    metadata.create(DeploymentProcessInstancesInfo.class);
            deploymentProcessInstancesInfo.setProcessDefinitionId(processDefinitionData.getProcessDefinitionId());
            deploymentProcessInstancesInfo.setProcessDefinitionName(processDefinitionData.getName());
            deploymentProcessInstancesInfo.setProcessDefinitionKey(processDefinitionData.getKey());
            deploymentProcessInstancesInfo.setProcessInstanceCount(processInstanceService.getCountByProcessDefinitionId(
                    processDefinitionData.getProcessDefinitionId()));

            deploymentProcessInstancesInfos.add(deploymentProcessInstancesInfo);
        });

        JmixGrid<DeploymentProcessInstancesInfo> grid = uiComponents.create(JmixGrid.class);
        grid.setWidth("100%");
        grid.setHeight("100%");

        Grid.Column<DeploymentProcessInstancesInfo> nameColumn = grid.addColumn(
                DeploymentProcessInstancesInfo::getProcessDefinitionName);
        nameColumn.setHeader(messages.getMessage(DeploymentProcessInstancesInfo.class,
                "DeploymentProcessInstancesInfo.processDefinitionName"));
        nameColumn.setResizable(true);

        Grid.Column<DeploymentProcessInstancesInfo> keyColumn = grid.addColumn(
                DeploymentProcessInstancesInfo::getProcessDefinitionKey);
        keyColumn.setHeader(messages.getMessage(DeploymentProcessInstancesInfo.class,
                "DeploymentProcessInstancesInfo.processDefinitionKey"));
        keyColumn.setResizable(true);
        keyColumn.setRenderer(new ComponentRenderer<>((SerializableFunction<DeploymentProcessInstancesInfo, JmixButton>)
                deploymentProcessInstancesInfo -> {

                    JmixButton button = uiComponents.create(JmixButton.class);
                    button.setText(deploymentProcessInstancesInfo.getProcessDefinitionKey());
                    button.addThemeName("tertiary-inline");
                    button.addClickListener((ComponentEventListener<ClickEvent<Button>>) event -> viewNavigators.detailView(DeploymentDetailView.this, ProcessDefinitionData.class)
                            .withViewClass(ProcessDefinitionDetailView.class)
                            .withRouteParameters(new RouteParameters("id", deploymentProcessInstancesInfo.getProcessDefinitionId()))
                            .withBackwardNavigation(true)
                            .navigate());
                    return button;
                }));

        Grid.Column<DeploymentProcessInstancesInfo> instanceCountColumn = grid.addColumn(
                DeploymentProcessInstancesInfo::getProcessInstanceCount);
        instanceCountColumn.setHeader(messages.getMessage(DeploymentProcessInstancesInfo.class,
                "DeploymentProcessInstancesInfo.processInstanceCount"));
        instanceCountColumn.setResizable(true);

        grid.setItems(deploymentProcessInstancesInfos);

        return grid;
    }

    private Component createBpmnViewer(String xmlData) {
        BpmnViewerFragment bpmnViewerFragment = fragments.create(this, BpmnViewerFragment.class);
        bpmnViewerFragment.initViewer(xmlData);

        return bpmnViewerFragment;
    }

    private Component createDmnViewer(String xmlData) {
        DmnViewerFragment dmnViewerFragment = fragments.create(this, DmnViewerFragment.class);
        dmnViewerFragment.initViewer();
        dmnViewerFragment.setDmnXml(xmlData);

        return dmnViewerFragment;
    }

    private Component createFormViewer(String jsonData) {
        FormViewerFragment formViewerFragment = fragments.create(this, FormViewerFragment.class);
        formViewerFragment.initViewer(jsonData);

        return formViewerFragment;
    }

    private Component createCodeEditor(String codeEditorData, CodeEditorMode codeEditorMode) {
        JmixCodeEditor codeEditor = uiComponents.create(JmixCodeEditor.class);
        codeEditor.setMode(codeEditorMode);
        codeEditor.getStyle().set("padding", "0");
        codeEditor.setWidth("100%");
        codeEditor.setHeight("100%");
        codeEditor.setReadOnly(true);
        codeEditor.setValue(codeEditorData);
        return codeEditor;
    }

    private static String getTextContent(Resource deploymentResourceData) {
        String xmlString;
        try {
            xmlString = new String(deploymentResourceData.getContentAsByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new EngineResourceNotAvailableException(deploymentResourceData.getFilename());
        }
        return xmlString;
    }

    private static byte[] getByteArrayContent(Resource deploymentResourceData) {
        byte[] byteArray;
        try {
            byteArray = deploymentResourceData.getContentAsByteArray();
        } catch (IOException e) {
            throw new EngineResourceNotAvailableException(deploymentResourceData.getFilename());
        }
        return byteArray;
    }
}
