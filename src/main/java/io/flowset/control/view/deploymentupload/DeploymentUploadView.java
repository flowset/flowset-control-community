/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.deploymentupload;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.selection.MultiSelectionEvent;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import io.flowset.control.entity.deployment.DeploymentData;
import io.flowset.control.entity.deployment.DeploymentResource;
import io.flowset.control.entity.deployment.DeploymentResourceType;
import io.flowset.control.entity.deployment.ResourceDeploymentReport;
import io.flowset.control.exception.RemoteEngineParseException;
import io.flowset.control.exception.RemoteProcessEngineException;
import io.flowset.control.property.UiProperties;
import io.flowset.control.restsupport.camunda.ResourceReport;
import io.flowset.control.security.SecuritySupport;
import io.flowset.control.security.accesscontext.decisiondefinition.DecisionDefinitionDeployAccessContext;
import io.flowset.control.security.accesscontext.processdefinition.ProcessDefinitionDeployAccessContext;
import io.flowset.control.service.deployment.DeploymentContext;
import io.flowset.control.service.deployment.DeploymentService;
import io.flowset.control.entity.deployment.DeploymentResourceStatus;
import io.flowset.control.service.engine.EngineTenantProvider;
import io.flowset.control.view.deploymentdata.DeploymentDetailView;
import io.flowset.control.view.deploymenterror.DeploymentErrorDialogView;
import io.flowset.control.view.deploymenterror.ResourceDeploymentReportFactory;
import io.flowset.control.view.deploymentresource.DeploymentResourcePreviewFragment;
import io.jmix.core.Messages;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.upload.FileUploadField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.upload.event.FileUploadFileRejectedEvent;
import io.jmix.flowui.kit.component.upload.event.FileUploadSucceededEvent;
import io.jmix.flowui.model.KeyValueCollectionContainer;
import io.jmix.flowui.view.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Route(value = "bpm/deployments/upload", layout = DefaultMainViewParent.class)
@ViewController("bpm_DeploymentUploadView")
@ViewDescriptor("deployment-upload-view.xml")
@Slf4j
public class DeploymentUploadView extends StandardView {

    protected static final long BYTES_IN_MB = 1024L * 1024L;

    protected static final String LINK_THEME_NAME = "link";
    protected static final String STATUS_ICON_SIZE = "1em";

    @Autowired
    protected DeploymentService deploymentService;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected SecuritySupport securitySupport;
    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    protected Messages messages;
    @Autowired
    protected EngineTenantProvider engineTenantProvider;
    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected ViewNavigators viewNavigators;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected ResourceDeploymentReportFactory resourceDeploymentReportFactory;
    @ViewComponent
    protected MessageBundle messageBundle;

    @ViewComponent
    protected TextField deploymentNameField;
    @ViewComponent
    protected Checkbox skipUnchangedField;
    @ViewComponent
    protected Span tenantInfo;
    @ViewComponent
    protected FileUploadField filesUpload;
    @ViewComponent
    protected Span filesUploadHelperText;
    @ViewComponent
    protected Span noDeployPermissionInfo;
    @ViewComponent
    protected KeyValueCollectionContainer filesDc;
    @ViewComponent
    protected DataGrid<KeyValueEntity> filesDataGrid;
    @ViewComponent
    protected VerticalLayout filesPanel;
    @ViewComponent
    protected JmixButton okBtn;
    @ViewComponent
    protected DeploymentResourcePreviewFragment resourcePreviewFragment;

    protected final List<UploadedFile> uploadedFiles = new ArrayList<>();

    protected boolean customDeploymentName = false;

    @Nullable
    protected UUID previewedFileId;

    @Nullable
    protected UUID activeFileId;

    protected boolean refreshingFiles = false;

    protected boolean deployAttempted = false;

    protected boolean canDeployProcesses;

    protected boolean canDeployDecisions;

    @Subscribe
    public void onInit(final InitEvent event) {
        canDeployProcesses = securitySupport.isActionPermitted(new ProcessDefinitionDeployAccessContext());
        canDeployDecisions = securitySupport.isActionPermitted(new DecisionDefinitionDeployAccessContext());
        okBtn.setVisible(canDeploy());
        initFilesUpload();
        initDeploymentNameField();
        initTenantInfo();
        initFilesPanel();
        refreshFiles();
    }

    protected void initFilesPanel() {
        filesPanel.setFlexGrow(1, filesDataGrid);
        filesPanel.getStyle().set("overflow", "auto");
        filesDataGrid.setPartNameGenerator(item -> item.getId().equals(previewedFileId) ? "previewed-row" : null);
    }

    protected void initDeploymentNameField() {
        deploymentNameField.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                customDeploymentName = StringUtils.isNotBlank(event.getValue());
            }
        });
    }

    protected void initTenantInfo() {
        String tenantId = engineTenantProvider.getCurrentUserTenantId();
        boolean tenantSet = StringUtils.isNotBlank(tenantId);
        tenantInfo.setVisible(tenantSet);
        if (tenantSet) {
            tenantInfo.setText(messageBundle.formatMessage("tenantInfo", tenantId));
        }
    }

    protected void initFilesUpload() {
        filesUpload.setMaxFileSize((int) Math.min(Integer.MAX_VALUE, getMaxFileSizeBytes()));

        String formats = getAllowedResourceTypes().stream()
                .filter(type -> type != DeploymentResourceType.OTHER)
                .map(type -> messages.getMessage(type))
                .collect(Collectors.joining(", "));
        filesUploadHelperText.setText(messageBundle.formatMessage("filesUpload.helperText",
                formats, getMaxFiles(), formatSizeInMb(getMaxFileSizeBytes())));

        noDeployPermissionInfo.setVisible(!canDeploy());
        updateUploadState();
    }

    protected boolean canDeploy() {
        return canDeployProcesses || canDeployDecisions;
    }

    protected List<DeploymentResourceType> getAllowedResourceTypes() {
        return Arrays.stream(DeploymentResourceType.values())
                .filter(type -> switch (type) {
                    case BPMN -> canDeployProcesses;
                    case DMN -> canDeployDecisions;
                    default -> canDeploy();
                })
                .toList();
    }

    @Subscribe("filesUpload")
    public void onFilesUploadSucceeded(final FileUploadSucceededEvent<FileUploadField, ?> event) {
        String fileName = Objects.toString(event.getFileName(), "");
        byte[] content = filesUpload.getValue();
        filesUpload.clear();
        if (content != null) {
            addFile(fileName, content);
        }
    }

    protected void addFile(String fileName, byte[] content) {
        if (uploadedFiles.size() >= getMaxFiles()) {
            notifications.create(messageBundle.formatMessage("tooManyFiles", getMaxFiles()))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }
        uploadedFiles.add(new UploadedFile(fileName, content));
        onFilesChanged();
        revalidate();
    }

    @Subscribe("filesUpload")
    public void onFilesUploadFileRejected(final FileUploadFileRejectedEvent<FileUploadField> event) {
        notifications.create(messageBundle.getMessage("fileRejected"), Objects.toString(event.getErrorMessage(), ""))
                .withType(Notifications.Type.WARNING)
                .show();
    }

    @Subscribe("filesDataGrid.remove")
    public void onFilesDataGridRemove(final ActionPerformedEvent event) {
        Set<Object> selectedIds = filesDataGrid.getSelectedItems().stream()
                .map(KeyValueEntity::getId)
                .collect(Collectors.toSet());
        if (uploadedFiles.removeIf(file -> selectedIds.contains(file.getId()))) {
            onFilesChanged();
        }
        revalidate();
    }

    @Subscribe("filesDataGrid")
    public void onFilesDataGridSelection(final SelectionEvent<DataGrid<KeyValueEntity>, KeyValueEntity> event) {
        if (refreshingFiles) {
            return;
        }
        KeyValueEntity active = event instanceof MultiSelectionEvent<?, KeyValueEntity> multiEvent
                && !multiEvent.getAddedSelection().isEmpty()
                ? multiEvent.getAddedSelection().iterator().next()
                : event.getFirstSelectedItem().orElse(null);
        activeFileId = active != null ? (UUID) active.getId() : null;
        updatePreview();
    }

    @Subscribe("filesDataGrid")
    public void onFilesDataGridItemClick(final ItemClickEvent<KeyValueEntity> event) {
        activeFileId = (UUID) event.getItem().getId();
        updatePreview();
    }

    @Supply(to = "filesDataGrid.status", subject = "renderer")
    protected Renderer<KeyValueEntity> filesDataGridStatusRenderer() {
        return new ComponentRenderer<>(item -> {
            UploadedFile file = findFile((UUID) item.getId());
            if (file == null) {
                return new Span();
            }
            return switch (file.getStatus()) {
                case ERROR -> createProblemsLink(file, VaadinIcon.CLOSE_CIRCLE, LumoUtility.TextColor.ERROR,
                        messageBundle.formatMessage("statusErrors", file.getErrorCount()));
                case WARNING -> createProblemsLink(file, VaadinIcon.WARNING, LumoUtility.TextColor.WARNING,
                        messageBundle.formatMessage("statusWarnings", file.getWarningCount()));
                case OK -> deployAttempted ? createNotCheckedStatus() : new Span();
            };
        });
    }

    protected Component createProblemsLink(UploadedFile file, VaadinIcon icon, String colorClassName, String text) {
        JmixButton link = uiComponents.create(JmixButton.class);
        link.setText(text);
        link.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        link.addThemeName(LINK_THEME_NAME);
        link.addClassName(colorClassName);
        link.addClickListener(event -> openProblemsDialog(file));
        return createStatus(icon, colorClassName, link);
    }

    protected Component createNotCheckedStatus() {
        Span text = new Span(messageBundle.getMessage("statusNotChecked"));
        Component status = createStatus(VaadinIcon.MINUS_CIRCLE_O, LumoUtility.TextColor.SECONDARY, text);
        status.getElement().setProperty("title", messageBundle.getMessage("statusNotChecked.tooltip"));
        return status;
    }

    protected Component createStatus(VaadinIcon vaadinIcon, String colorClassName, Component text) {
        Icon icon = vaadinIcon.create();
        icon.addClassName(colorClassName);
        icon.setSize(STATUS_ICON_SIZE);
        HorizontalLayout layout = new HorizontalLayout(icon, text);
        layout.setSpacing(false);
        layout.addClassNames(LumoUtility.Gap.XSMALL, LumoUtility.AlignItems.CENTER);
        return layout;
    }

    @Subscribe(id = "okBtn", subject = "clickListener")
    public void onOkBtnClick(final ClickEvent<JmixButton> event) {
        revalidate();
        if (uploadedFiles.isEmpty() || hasErrors()) {
            notifications.create(messageBundle.getMessage("validation.deployBlocked"))
                    .withType(Notifications.Type.ERROR)
                    .show();
            return;
        }

        deploy();
    }

    protected void deploy() {
        DeploymentContext context = new DeploymentContext()
                .withDeploymentName(deploymentNameField.getValue())
                .withSkipUnchangedResources(Boolean.TRUE.equals(skipUnchangedField.getValue()));
        uploadedFiles.forEach(file -> context.withResource(file.getName(), new ByteArrayInputStream(file.getContent())));

        Instant deployStart = Instant.now();
        try {
            DeploymentWithDefinitions result = deploymentService.createDeployment(context);
            onDeploymentCreated(result, deployStart);
        } catch (Exception ex) {
            log.error("Error on deployment upload", ex);
            RemoteEngineParseException parseException =
                    ExceptionUtils.throwableOfType(ex, RemoteEngineParseException.class);
            if (parseException != null) {
                handleEngineRejection(parseException);
            } else if (ex instanceof RemoteProcessEngineException) {
                notifications.create(messageBundle.getMessage("deploymentNotCreated"), ex.getMessage())
                        .withType(Notifications.Type.ERROR)
                        .show();
            } else {
                throw ex;
            }
        }
    }

    protected void onDeploymentCreated(@Nullable DeploymentWithDefinitions result, Instant deployStart) {
        String deploymentId = result != null ? result.getId() : null;
        if (isExistingDeployment(result, deployStart)) {
            notifications.create(messageBundle.getMessage("deploymentUnchanged"),
                            messageBundle.getMessage("deploymentUnchanged.description"))
                    .withType(Notifications.Type.WARNING)
                    .show();
            closeAndNavigate(deploymentId);
            return;
        }

        int processes = CollectionUtils.size(result != null ? result.getDeployedProcessDefinitions() : null);
        int decisions = CollectionUtils.size(result != null ? result.getDeployedDecisionDefinitions() : null);
        int cases = CollectionUtils.size(result != null ? result.getDeployedCaseDefinitions() : null);

        List<String> resourceNames = loadDeployedResourceNames(result != null ? result.getId() : null);
        if (resourceNames == null) {
            resourceNames = uploadedFiles.stream().map(UploadedFile::getName).toList();
        }
        long forms = countResources(resourceNames, EnumSet.of(DeploymentResourceType.CAMUNDA_FORM));
        long others = countResources(resourceNames, EnumSet.of(DeploymentResourceType.HTML_FORM,
                DeploymentResourceType.SCRIPT, DeploymentResourceType.IMAGE, DeploymentResourceType.OTHER));

        String summary = messageBundle.formatMessage("deploymentCreated.summary", processes, decisions, forms);
        if (cases > 0) {
            summary += messageBundle.formatMessage("deploymentCreated.summaryCases", cases);
        }
        if (others > 0) {
            summary += messageBundle.formatMessage("deploymentCreated.summaryOther", others);
        }
        notifications.create(messageBundle.getMessage("deploymentCreated"), summary)
                .withType(Notifications.Type.SUCCESS)
                .show();
        closeAndNavigate(deploymentId);
    }

    protected boolean isExistingDeployment(@Nullable DeploymentWithDefinitions result, Instant deployStart) {
        return result != null && result.getDeploymentTime() != null
                && result.getDeploymentTime().toInstant().isBefore(deployStart);
    }

    protected void closeAndNavigate(@Nullable String deploymentId) {
        if (StringUtils.isNotBlank(deploymentId)) {
            ViewControllerUtils.setViewCloseDelegate(this, view -> navigateToDeployment(deploymentId));
        }
        close(StandardOutcome.SAVE);
    }

    @Nullable
    protected List<String> loadDeployedResourceNames(@Nullable String deploymentId) {
        if (StringUtils.isBlank(deploymentId)) {
            return null;
        }
        try {
            List<DeploymentResource> resources = deploymentService.getDeploymentResources(deploymentId);
            if (resources == null) {
                log.warn("Unable to load the resources of the deployment {}: no resources returned", deploymentId);
                return null;
            }
            return resources.stream().map(DeploymentResource::getName).toList();
        } catch (Exception e) {
            log.warn("Unable to load the resources of the deployment {}", deploymentId, e);
            return null;
        }
    }

    protected long countResources(Collection<String> resourceNames, Set<DeploymentResourceType> types) {
        return resourceNames.stream()
                .map(DeploymentResourceType::fromFileName)
                .filter(type -> type != null && types.contains(type))
                .count();
    }

    protected void navigateToDeployment(String deploymentId) {
        viewNavigators.detailView(this, DeploymentData.class)
                .withViewClass(DeploymentDetailView.class)
                .withRouteParameters(new RouteParameters("id", deploymentId))
                .withBackwardNavigation(false)
                .navigate();
    }

    protected void handleEngineRejection(RemoteEngineParseException exception) {
        deployAttempted = true;
        Map<String, ResourceReport> details = exception.getDetails() != null ? exception.getDetails() : Map.of();
        int rejectedFiles = 0;
        UploadedFile firstRejectedFile = null;
        for (UploadedFile file : uploadedFiles) {
            ResourceReport report = details.get(file.getName());
            file.setEngineReport(report);
            if (file.hasEngineErrors()) {
                rejectedFiles++;
                if (firstRejectedFile == null) {
                    firstRejectedFile = file;
                }
            }
        }
        refreshFiles();

        if (firstRejectedFile != null) {
            UUID rejectedFileId = firstRejectedFile.getId();
            filesDataGrid.deselectAll();
            filesDc.getItems().stream()
                    .filter(item -> rejectedFileId.equals(item.getId()))
                    .findFirst()
                    .ifPresent(filesDataGrid::select);
            activeFileId = rejectedFileId;
            updatePreview();
        }

        if (rejectedFiles > 0) {
            notifications.create(messageBundle.getMessage("deploymentRejected"),
                            messageBundle.formatMessage("deploymentRejected.description", rejectedFiles))
                    .withType(Notifications.Type.ERROR)
                    .show();
        } else {
            notifications.create(messageBundle.getMessage("deploymentNotCreated"), exception.getMessage())
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    protected void openProblemsDialog(UploadedFile file) {
        ResourceDeploymentReport report = resourceDeploymentReportFactory.createReport(file.getName(),
                file.getViewErrors(), file.getEngineReport());
        dialogWindows.view(this, DeploymentErrorDialogView.class)
                .withViewConfigurer(view -> {
                    view.setResourceReport(report);
                    view.setEngineRejected(file.getEngineReport() != null);
                })
                .open();
    }

    @Subscribe(id = "cancelBtn", subject = "clickListener")
    public void onCancelBtnClick(final ClickEvent<JmixButton> event) {
        close(StandardOutcome.CLOSE);
    }

    protected void revalidate() {
        Map<String, Long> nameCounts = uploadedFiles.stream()
                .collect(Collectors.groupingBy(file -> normalizeName(file.getName()), Collectors.counting()));
        long maxFileSize = getMaxFileSizeBytes();

        for (UploadedFile file : uploadedFiles) {
            List<String> errors = new ArrayList<>();
            if (file.getContent().length == 0) {
                errors.add(messageBundle.getMessage("validation.emptyFile"));
            }
            if (file.getContent().length > maxFileSize) {
                errors.add(messageBundle.formatMessage("validation.fileTooLarge", formatSizeInMb(maxFileSize)));
            }
            if (nameCounts.getOrDefault(normalizeName(file.getName()), 0L) > 1) {
                errors.add(messageBundle.getMessage("validation.duplicateName"));
            }
            if (file.getType() == DeploymentResourceType.BPMN && !canDeployProcesses) {
                errors.add(messageBundle.getMessage("noProcessDeployPermission"));
            }
            if (file.getType() == DeploymentResourceType.DMN && !canDeployDecisions) {
                errors.add(messageBundle.getMessage("noDecisionDeployPermission"));
            }
            file.setViewErrors(errors);
        }
        refreshFiles();
    }

    protected void onFilesChanged() {
        uploadedFiles.forEach(UploadedFile::clearEngineReport);
        deployAttempted = false;
    }

    protected void refreshFiles() {
        Set<Object> selectedIds = filesDataGrid.getSelectedItems().stream()
                .map(KeyValueEntity::getId)
                .collect(Collectors.toSet());

        List<KeyValueEntity> items = uploadedFiles.stream()
                .map(this::createFileItem)
                .toList();

        refreshingFiles = true;
        try {
            filesDc.setItems(items);
            filesDataGrid.deselectAll();
            items.stream()
                    .filter(item -> selectedIds.contains(item.getId()))
                    .forEach(filesDataGrid::select);
            if (activeFileId != null && uploadedFiles.stream().noneMatch(file -> file.getId().equals(activeFileId))) {
                activeFileId = filesDataGrid.getSelectedItems().stream()
                        .findFirst()
                        .map(item -> (UUID) item.getId())
                        .orElse(null);
            }
        } finally {
            refreshingFiles = false;
        }
        updatePreview();

        updateOkBtnState();
        updateUploadState();
        updateDefaultDeploymentName();
    }

    protected void updateOkBtnState() {
        okBtn.setEnabled(!uploadedFiles.isEmpty() && !hasErrors());
    }

    protected KeyValueEntity createFileItem(UploadedFile file) {
        KeyValueEntity item = filesDc.createEntity();
        item.setId(file.getId());
        item.setValue("fileName", file.getName());
        item.setValue("type", messages.getMessage(file.getType()));
        item.setValue("status", file.getStatus().getId());
        return item;
    }

    protected void updatePreview() {
        UploadedFile selectedFile = getSelectedFile();
        UUID previousFileId = previewedFileId;
        if (selectedFile == null) {
            if (previewedFileId != null) {
                resourcePreviewFragment.clear();
                previewedFileId = null;
            }
        } else if (!selectedFile.getId().equals(previewedFileId)) {
            resourcePreviewFragment.showResource(selectedFile.getName(), selectedFile.getContent());
            previewedFileId = selectedFile.getId();
        }
        if (!Objects.equals(previousFileId, previewedFileId)) {
            filesDataGrid.getDataProvider().refreshAll();
        }
    }

    @Nullable
    protected UploadedFile getSelectedFile() {
        return findFile(activeFileId);
    }

    @Nullable
    protected UploadedFile findFile(@Nullable UUID fileId) {
        return fileId == null ? null : uploadedFiles.stream()
                .filter(file -> file.getId().equals(fileId))
                .findFirst()
                .orElse(null);
    }

    protected void updateDefaultDeploymentName() {
        if (customDeploymentName) {
            return;
        }
        String defaultName = getDefaultDeploymentName();
        if (!Objects.equals(defaultName, Objects.toString(deploymentNameField.getValue(), ""))) {
            deploymentNameField.setValue(defaultName);
        }
    }

    protected String getDefaultDeploymentName() {
        return uploadedFiles.stream()
                .filter(file -> file.getType() == DeploymentResourceType.BPMN)
                .findFirst()
                .map(file -> stripExtension(file.getName(), DeploymentResourceType.BPMN))
                .orElse("");
    }

    protected String stripExtension(String fileName, DeploymentResourceType type) {
        String lowerCaseName = normalizeName(fileName);
        return type.getExtensions().stream()
                .filter(lowerCaseName::endsWith)
                .max(Comparator.comparingInt(String::length))
                .map(extension -> fileName.substring(0, fileName.length() - extension.length()))
                .orElse(fileName);
    }

    protected void updateUploadState() {
        filesUpload.setEnabled(canDeploy() && uploadedFiles.size() < getMaxFiles());
    }

    protected boolean hasErrors() {
        return uploadedFiles.stream().anyMatch(file -> file.getStatus() == DeploymentResourceStatus.ERROR);
    }

    protected int getMaxFiles() {
        return uiProperties.getDeploymentUploadMaxFiles();
    }

    protected long getMaxFileSizeBytes() {
        return uiProperties.getDeploymentUploadMaxFileSizeBytes();
    }

    protected String formatSizeInMb(long bytes) {
        return BigDecimal.valueOf(bytes)
                .divide(BigDecimal.valueOf(BYTES_IN_MB), 2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    protected String normalizeName(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    /**
     * A file uploaded by the user and not yet deployed.
     */
    protected static class UploadedFile {
        protected final UUID id = UUID.randomUUID();
        protected final String name;
        protected final byte[] content;
        protected final DeploymentResourceType type;
        protected List<String> viewErrors = List.of();
        @Nullable
        protected ResourceReport engineReport;

        public UploadedFile(String name, byte[] content) {
            this.name = name;
            this.content = content;
            this.type = DeploymentResourceType.fromFileName(name);
        }

        public UUID getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public byte[] getContent() {
            return content;
        }

        public DeploymentResourceType getType() {
            return type;
        }

        public List<String> getViewErrors() {
            return viewErrors;
        }

        public void setViewErrors(List<String> viewErrors) {
            this.viewErrors = List.copyOf(viewErrors);
        }

        @Nullable
        public ResourceReport getEngineReport() {
            return engineReport;
        }

        public void setEngineReport(@Nullable ResourceReport engineReport) {
            this.engineReport = engineReport;
        }

        public void clearEngineReport() {
            this.engineReport = null;
        }

        public boolean hasEngineErrors() {
            return engineReport != null && CollectionUtils.isNotEmpty(engineReport.getErrors());
        }

        public int getErrorCount() {
            return viewErrors.size() + (engineReport != null ? CollectionUtils.size(engineReport.getErrors()) : 0);
        }

        public int getWarningCount() {
            return engineReport != null ? CollectionUtils.size(engineReport.getWarnings()) : 0;
        }

        public DeploymentResourceStatus getStatus() {
            if (getErrorCount() > 0) {
                return DeploymentResourceStatus.ERROR;
            }
            return getWarningCount() > 0 ? DeploymentResourceStatus.WARNING : DeploymentResourceStatus.OK;
        }
    }
}
