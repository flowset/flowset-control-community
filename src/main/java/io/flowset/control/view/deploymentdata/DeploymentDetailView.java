package io.flowset.control.view.deploymentdata;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.exception.ViewEngineConnectionFailedException;
import io.jmix.core.LoadContext;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.grid.JmixGrid;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.deployment.DeploymentData;
import io.flowset.control.entity.deployment.DeploymentProcessInstancesInfo;
import io.flowset.control.entity.deployment.DeploymentResource;
import io.flowset.control.entity.deployment.DeploymentResourceType;
import io.flowset.control.entity.filter.ProcessDefinitionFilter;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.exception.EngineResourceNotAvailableException;
import io.flowset.control.service.deployment.DeploymentService;
import io.flowset.control.service.processdefinition.ProcessDefinitionLoadContext;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import io.flowset.control.service.processinstance.ProcessInstanceService;
import io.flowset.control.view.deploymentresource.DeploymentResourcePreviewFragment;
import io.flowset.control.view.processdefinition.ProcessDefinitionDetailView;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Route(value = "bpm/deployments/:id", layout = DefaultMainViewParent.class)
@ViewController(id = "bpm_Deployment.detail")
@ViewDescriptor(path = "deployment-detail-view.xml")
@EditedEntityContainer("deploymentDataDc")
@DialogMode(width = "70em", height = "40em")
@PrimaryDetailView(DeploymentData.class)
public class DeploymentDetailView extends StandardDetailView<DeploymentData> {

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
    private ViewNavigators viewNavigators;

    @ViewComponent
    private InstanceContainer<DeploymentData> deploymentDataDc;
    @ViewComponent
    private DeploymentResourcePreviewFragment resourcePreviewFragment;
    @ViewComponent
    private DataGrid<DeploymentResource> resourcesDataGrid;
    @ViewComponent
    private Span deploymentResourcesLabel;
    private JmixButton downloadResourceButton;
    private String runningInstancesTabLabel;

    @Subscribe
    public void onInit(final InitEvent event) {
        runningInstancesTabLabel = messages.getMessage(getClass(), "viewTab.runningInstances");

        initResourcesDataGrid();
        initDownloadResourceButton();

        deploymentResourcesLabel.addClassNames(LumoUtility.TextColor.SECONDARY);
        deploymentResourcesLabel.addClassNames(LumoUtility.FontWeight.SEMIBOLD);

        addClassName(LumoUtility.Padding.Bottom.SMALL);
    }

    private void initDownloadResourceButton() {
        downloadResourceButton = uiComponents.create(JmixButton.class);
        downloadResourceButton.setId("downloadResourceButton");
        downloadResourceButton.setIcon(new Icon("lumo", "download"));
        downloadResourceButton.setText("Download");
        downloadResourceButton.addThemeName("tertiary-inline");
        downloadResourceButton.getStyle().set("align-self", "end");
        downloadResourceButton.setVisible(false);
        downloadResourceButton.addClickListener(this::onDownloadResourceButtonClick);

        // the suffix of the preview tab sheet, as it was declared in the view descriptor before
        resourcePreviewFragment.getResourceTabSheet().setSuffixComponent(downloadResourceButton);
    }

    private void onDownloadResourceButtonClick(final ClickEvent<Button> event) {
        DeploymentResource selectedResource = resourcesDataGrid.getSingleSelectedItem();
        if (selectedResource != null) {
            Resource deploymentResourceData = deploymentService.getDeploymentResourceData(
                    selectedResource.getDeploymentId(), selectedResource.getResourceId());
            byte[] byteArrayContent = getByteArrayContent(deploymentResourceData);
            String downloadName = FilenameUtils.getName(selectedResource.getName());
            if (StringUtils.isEmpty(downloadName)) {
                downloadName = "resource-" + selectedResource.getResourceId();
            }
            downloader.download(() -> new ByteArrayInputStream(byteArrayContent), downloadName);
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

        byte[] content = getByteArrayContent(deploymentResourceData);

        downloadResourceButton.setVisible(true);
        resourcePreviewFragment.showResource(resourceName, content);

        if (DeploymentResourceType.fromFileName(resourceName) == DeploymentResourceType.BPMN) {
            resourcePreviewFragment.addTab("runningInstancesTab", VaadinIcon.HOURGLASS.create(),
                    runningInstancesTabLabel, createProcessDefinitionViewer());
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

        try {
            return deploymentService.findById(id);
        } catch (EngineConnectionFailedException e) {
            throw new ViewEngineConnectionFailedException(e, this);
        }
    }

    private void initResourcesDataGrid() {
        Grid.Column<DeploymentResource> instanceCountColumn = resourcesDataGrid.addColumn(
                DeploymentResource::getName);
        instanceCountColumn.setHeader(messages.getMessage(DeploymentResource.class,
                "DeploymentResource.name"));
        instanceCountColumn.setResizable(true);
    }

    private JmixGrid<DeploymentProcessInstancesInfo> createProcessDefinitionViewer() {
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
        grid.setId("processGrid");
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
