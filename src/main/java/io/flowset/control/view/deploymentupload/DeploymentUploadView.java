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
import io.flowset.control.service.deployment.validation.DeploymentReferenceCheckResult;
import io.flowset.control.service.deployment.validation.DeploymentReferenceChecker;
import io.flowset.control.service.deployment.validation.DeploymentResourceDescriptor;
import io.flowset.control.service.deployment.validation.DeploymentResourceStatus;
import io.flowset.control.service.deployment.validation.DeploymentResourceValidationResult;
import io.flowset.control.service.deployment.validation.DeploymentResourceValidator;
import io.flowset.control.service.deployment.validation.ReferenceLocation;
import io.flowset.control.service.deployment.validation.ResolvedReference;
import io.flowset.control.service.deployment.validation.ResourceReference;
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

    /**
     * Theme of the buttons that look like links, see styles/components/vaadin-button.css
     */
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
    protected DeploymentResourceValidator deploymentResourceValidator;
    @Autowired
    protected DeploymentReferenceChecker deploymentReferenceChecker;
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
    protected Span referencesHeader;
    @ViewComponent
    protected KeyValueCollectionContainer referencesDc;
    @ViewComponent
    protected DataGrid<KeyValueEntity> referencesDataGrid;
    @ViewComponent
    protected JmixButton okBtn;
    @ViewComponent
    protected DeploymentResourcePreviewFragment resourcePreviewFragment;

    protected final List<UploadedFile> uploadedFiles = new ArrayList<>();

    /**
     * References of the uploaded BPMN files resolved by the last applied reference check result.
     */
    protected List<ResolvedReference> resolvedReferences = List.of();

    /**
     * Whether the user has entered a custom deployment name, so the default name must not overwrite it.
     */
    protected boolean customDeploymentName = false;

    /**
     * Id of the file shown in the preview, {@code null} if no file is shown.
     */
    @Nullable
    protected UUID previewedFileId;

    /**
     * Id of the last selected file: with the multi-selection grid, it is the file shown in the preview.
     */
    @Nullable
    protected UUID activeFileId;

    /**
     * Whether the files grid items are being refreshed, so the grid selection events must be ignored.
     */
    protected boolean refreshingFiles = false;

    /**
     * {@code true} after a deploy attempt rejected by the engine: only then the OK status is known for sure.
     */
    protected boolean deployAttempted = false;

    /**
     * Whether the reference check (including the engine lookup) is performed for the current file list,
     * so the deployment can rely on its result.
     */
    protected boolean referenceCheckCompleted = true;

    /**
     * Whether the current user is permitted to deploy processes (BPMN files).
     */
    protected boolean canDeployProcesses;

    /**
     * Whether the current user is permitted to deploy decisions (DMN files).
     */
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
        // both grids share the free space; the panel scrolls if their minimum heights do not fit
        filesPanel.setFlexGrow(1, filesDataGrid, referencesDataGrid);
        filesPanel.getStyle().set("overflow", "auto");
        filesDataGrid.setPartNameGenerator(item -> item.getId().equals(previewedFileId) ? "previewed-row" : null);
    }

    protected void initDeploymentNameField() {
        deploymentNameField.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                // a blank value means the user cleared the field: the default name applies again on the next file change
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
        List<DeploymentResourceType> allowedTypes = getAllowedResourceTypes();
        filesUpload.setAcceptedFileTypes(allowedTypes.stream()
                .flatMap(type -> type.getExtensions().stream())
                .sorted(Comparator.comparingInt(String::length).reversed())
                .toArray(String[]::new));
        filesUpload.setMaxFileSize((int) Math.min(Integer.MAX_VALUE, getMaxFileSizeBytes()));

        String formats = allowedTypes.stream()
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

    /**
     * @return resource types the current user is permitted to deploy: BPMN files require the process deploy
     * permission, DMN files require the decision deploy permission, other resources require either of them
     */
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

    /**
     * Adds an uploaded file to the deployment and revalidates the files. The file is rejected with a notification
     * if the file limit is reached.
     */
    protected void addFile(String fileName, byte[] content) {
        if (uploadedFiles.size() >= getMaxFiles()) {
            notifications.create(messageBundle.formatMessage("tooManyFiles", getMaxFiles()))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }
        uploadedFiles.add(new UploadedFile(fileName, content, deploymentResourceValidator.validate(fileName, content)));
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
                        messageBundle.formatMessage("statusErrors", file.getErrors().size()));
                case WARNING -> createProblemsLink(file, VaadinIcon.WARNING, LumoUtility.TextColor.WARNING,
                        messageBundle.formatMessage("statusWarnings", file.getWarnings().size()));
                case OK -> deployAttempted ? createOkStatus() : new Span();
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

    protected Component createOkStatus() {
        return createStatus(VaadinIcon.CHECK_CIRCLE, LumoUtility.TextColor.SUCCESS,
                new Span(messages.getMessage(DeploymentResourceStatus.OK)));
    }

    /**
     * The icon is placed next to the text, not into the button, so that all the statuses have the same icon size.
     */
    protected Component createStatus(VaadinIcon vaadinIcon, String colorClassName, Component text) {
        Icon icon = vaadinIcon.create();
        icon.addClassName(colorClassName);
        icon.setSize(STATUS_ICON_SIZE);
        HorizontalLayout layout = new HorizontalLayout(icon, text);
        layout.setSpacing(false);
        layout.addClassNames(LumoUtility.Gap.XSMALL, LumoUtility.AlignItems.CENTER);
        return layout;
    }

    @Supply(to = "referencesDataGrid.location", subject = "renderer")
    protected Renderer<KeyValueEntity> referencesDataGridLocationRenderer() {
        return new ComponentRenderer<>(item -> {
            ReferenceLocation location = ReferenceLocation.fromId(item.getValue("location"));
            if (location == null) {
                return new Span();
            }
            Span badge = new Span(messages.getMessage(location));
            badge.getElement().getThemeList().add(switch (location) {
                case THIS_DEPLOYMENT -> "badge success";
                case ENGINE -> "badge";
                case MISSING -> "badge error";
                case NOT_CHECKED, CHECKING -> "badge contrast";
            });
            return badge;
        });
    }

    @Subscribe(id = "okBtn", subject = "clickListener")
    public void onOkBtnClick(final ClickEvent<JmixButton> event) {
        applyViewChecks();
        if (referenceCheckCompleted) {
            refreshFiles();
        } else {
            checkReferences();
        }
        if (uploadedFiles.isEmpty() || hasErrors()) {
            notifications.create(messageBundle.getMessage("validation.deployBlocked"))
                    .withType(Notifications.Type.ERROR)
                    .show();
            return;
        }

        deploy();
    }

    /**
     * Deploys the uploaded files as a single deployment. The engine deploys either all files or none of them:
     * if it rejects any file, the problems reported by the engine are shown for each rejected file.
     */
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

    /**
     * Shows the summary of the created deployment and navigates to its details.
     * <p>
     * The processes and decisions are taken from the deployment result. The deployment result of the REST client
     * does not contain the deployed Camunda Forms and other resources (HTML forms, scripts, images), so they are
     * counted by the resources of the created deployment loaded from the engine: with the "Skip unchanged resources"
     * option, unchanged resources are not deployed. If the resources cannot be loaded, the uploaded files are counted.
     */
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

        List<String> resourceNames = loadDeployedResourceNames(result != null ? result.getId() : null);
        if (resourceNames == null) {
            // fallback: the uploaded files are counted, which may include unchanged (not deployed) resources
            resourceNames = uploadedFiles.stream().map(UploadedFile::getName).toList();
        }
        long forms = countResources(resourceNames, EnumSet.of(DeploymentResourceType.CAMUNDA_FORM));
        long others = countResources(resourceNames, EnumSet.of(DeploymentResourceType.HTML_FORM,
                DeploymentResourceType.SCRIPT, DeploymentResourceType.IMAGE));

        String summary = messageBundle.formatMessage("deploymentCreated.summary", processes, decisions, forms);
        if (others > 0) {
            summary += messageBundle.formatMessage("deploymentCreated.summaryOther", others);
        }
        notifications.create(messageBundle.getMessage("deploymentCreated"), summary)
                .withType(Notifications.Type.SUCCESS)
                .show();
        closeAndNavigate(deploymentId);
    }

    /**
     * With the "Skip unchanged resources" option, the engine returns the existing deployment instead of creating
     * a new one when none of the resources differs from the deployed ones.
     */
    protected boolean isExistingDeployment(@Nullable DeploymentWithDefinitions result, Instant deployStart) {
        return result != null && result.getDeploymentTime() != null
                && result.getDeploymentTime().toInstant().isBefore(deployStart);
    }

    /**
     * Closes the view with the SAVE outcome (see ViewAnalyticsListener); instead of the backward navigation,
     * the details of the deployment are opened.
     */
    protected void closeAndNavigate(@Nullable String deploymentId) {
        if (StringUtils.isNotBlank(deploymentId)) {
            ViewControllerUtils.setViewCloseDelegate(this, view -> navigateToDeployment(deploymentId));
        }
        close(StandardOutcome.SAVE);
    }

    /**
     * Loads the names of the resources of the created deployment from the engine.
     *
     * @param deploymentId id of the created deployment
     * @return resource names or {@code null} if the deployment id is blank or the resources cannot be loaded
     */
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

    /**
     * Opens the details of the deployment. The backward navigation is disabled, so the upload view is not
     * opened again when the details view is closed.
     */
    protected void navigateToDeployment(String deploymentId) {
        viewNavigators.detailView(this, DeploymentData.class)
                .withViewClass(DeploymentDetailView.class)
                .withRouteParameters(new RouteParameters("id", deploymentId))
                .withBackwardNavigation(false)
                .navigate();
    }

    /**
     * Applies the problems reported by the engine to the uploaded files: the files with engine errors get the
     * {@link DeploymentResourceStatus#ERROR} status. Nothing is deployed, so the view stays open.
     */
    protected void handleEngineRejection(RemoteEngineParseException exception) {
        deployAttempted = true;
        Map<String, ResourceReport> details = exception.getDetails() != null ? exception.getDetails() : Map.of();
        int rejectedFiles = 0;
        UploadedFile firstRejectedFile = null;
        for (UploadedFile file : uploadedFiles) {
            ResourceReport report = details.get(file.getName());
            file.setEngineReport(report,
                    report != null ? formatEngineProblems(report.getErrors()) : List.of(),
                    report != null ? formatEngineProblems(report.getWarnings()) : List.of());
            if (!file.getEngineErrors().isEmpty()) {
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

    protected List<String> formatEngineProblems(@Nullable List<ResourceReport.ProblemDetails> problems) {
        if (problems == null) {
            return List.of();
        }
        return problems.stream()
                .map(problem -> StringUtils.isNotBlank(problem.getMainElementId())
                        ? messageBundle.formatMessage("engineProblemWithElement",
                        Objects.toString(problem.getMessage(), ""), problem.getMainElementId())
                        : Objects.toString(problem.getMessage(), ""))
                .toList();
    }

    /**
     * Opens the report of the problems of the file: the ones found by Control and, if the engine rejected the file,
     * the ones reported by the engine.
     */
    protected void openProblemsDialog(UploadedFile file) {
        ResourceDeploymentReport report = resourceDeploymentReportFactory.createReport(file.getName(),
                file.getLocalErrors(), file.getLocalWarnings(), file.getEngineReport());
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

    /**
     * Performs the view-level checks of the uploaded files (size limit, duplicate names, permissions) and the
     * reference check, then refreshes the grids.
     */
    protected void revalidate() {
        applyViewChecks();
        checkReferences();
    }

    /**
     * Applies the view-level checks (size limit, duplicate names, permissions) to the uploaded files. The content
     * checks are performed by {@link DeploymentResourceValidator} once, when a file is uploaded.
     */
    protected void applyViewChecks() {
        Map<String, Long> nameCounts = uploadedFiles.stream()
                .collect(Collectors.groupingBy(file -> normalizeName(file.getName()), Collectors.counting()));
        long maxFileSize = getMaxFileSizeBytes();

        for (UploadedFile file : uploadedFiles) {
            List<String> errors = new ArrayList<>();
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
    }

    /**
     * Invalidates the last reference check result and the engine reports of the last rejected deployment: must be
     * called on every change of the uploaded file list. The deployment is not possible until the reference check
     * is performed for the new list.
     */
    protected void onFilesChanged() {
        uploadedFiles.forEach(UploadedFile::clearEngineReport);
        deployAttempted = false;
        referenceCheckCompleted = false;
    }

    /**
     * Performs the reference check ({@link DeploymentReferenceChecker}: references between the files and to the
     * engine, duplicate keys) of the current files. The engine lookup is performed synchronously in the UI thread:
     * the engine failures are handled by the checker, the references that cannot be checked are reported as such.
     */
    protected void checkReferences() {
        List<DeploymentResourceDescriptor> descriptors = uploadedFiles.stream()
                .map(file -> DeploymentResourceDescriptor.of(file.getName(), file.getValidationResult()))
                .toList();
        if (descriptors.isEmpty()) {
            applyReferenceCheckResult(new DeploymentReferenceCheckResult());
            return;
        }

        DeploymentReferenceCheckResult result;
        try {
            result = deploymentReferenceChecker.check(descriptors);
        } catch (RuntimeException e) {
            log.warn("Unable to check the references of the deployment resources", e);
            result = deploymentReferenceChecker.checkEngineUnavailable(descriptors);
        }
        applyReferenceCheckResult(result);
    }

    /**
     * Applies the reference check result to the files and the references grid.
     */
    protected void applyReferenceCheckResult(DeploymentReferenceCheckResult result) {
        for (UploadedFile file : uploadedFiles) {
            file.setReferenceErrors(result.getErrors(file.getName()));
            file.setReferenceWarnings(result.getWarnings(file.getName()));
        }
        resolvedReferences = List.copyOf(result.getReferences());
        referenceCheckCompleted = true;
        refreshFiles();
    }

    protected void refreshReferences() {
        referencesDc.setItems(resolvedReferences.stream()
                .map(this::createReferenceItem)
                .toList());
        boolean visible = !resolvedReferences.isEmpty();
        referencesHeader.setVisible(visible);
        referencesDataGrid.setVisible(visible);
    }

    protected KeyValueEntity createReferenceItem(ResolvedReference resolvedReference) {
        ResourceReference reference = resolvedReference.reference();
        KeyValueEntity item = referencesDc.createEntity();
        item.setId(UUID.randomUUID());
        item.setValue("fileName", resolvedReference.fileName());
        item.setValue("element", Objects.toString(reference.getSourceElementId(), ""));
        item.setValue("kind", messages.getMessage(reference.getKind()));
        item.setValue("key", reference.getKey());
        item.setValue("binding", reference.getBinding());
        item.setValue("location", resolvedReference.location().getId());
        item.setValue("message", resolvedReference.message());
        return item;
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
        refreshReferences();

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
        item.setValue("type", file.getType() != null ? messages.getMessage(file.getType()) : "-");
        item.setValue("keys", String.join(", ", file.getValidationResult().getKeys()));
        item.setValue("status", file.getStatus().getId());
        return item;
    }

    /**
     * Shows the file selected in the grid in the preview, or clears the preview if no file is selected.
     */
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

    /**
     * @return the last selected file, {@code null} if no file is selected
     */
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

    /**
     * Sets the deployment name to the default one (the name of the first uploaded BPMN file without the extension)
     * if the user has not entered a custom name.
     */
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

    /**
     * @return whether any file has the {@link DeploymentResourceStatus#ERROR} status: such files block the deployment,
     * warnings do not
     */
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
        protected final DeploymentResourceValidationResult validationResult;
        /**
         * Errors of the view-level checks that depend on the other files and the settings
         * (duplicate names, size limit).
         */
        protected List<String> viewErrors = List.of();
        /**
         * Errors and warnings of the reference check that depends on the other files and the engine
         * (missing referenced resources, duplicate keys).
         */
        protected List<String> referenceErrors = List.of();
        protected List<String> referenceWarnings = List.of();
        /**
         * Report of the engine that rejected the last deployment attempt, {@code null} if the engine reported
         * no problems for this file. The engine errors and warnings are the formatted problems of the report.
         */
        @Nullable
        protected ResourceReport engineReport;
        protected List<String> engineErrors = List.of();
        protected List<String> engineWarnings = List.of();

        public UploadedFile(String name, byte[] content, DeploymentResourceValidationResult validationResult) {
            this.name = name;
            this.content = content;
            this.validationResult = validationResult;
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

        @Nullable
        public DeploymentResourceType getType() {
            return validationResult.getType();
        }

        public DeploymentResourceValidationResult getValidationResult() {
            return validationResult;
        }

        public void setViewErrors(List<String> viewErrors) {
            this.viewErrors = List.copyOf(viewErrors);
        }

        public void setReferenceErrors(List<String> referenceErrors) {
            this.referenceErrors = List.copyOf(referenceErrors);
        }

        public void setReferenceWarnings(List<String> referenceWarnings) {
            this.referenceWarnings = List.copyOf(referenceWarnings);
        }

        @Nullable
        public ResourceReport getEngineReport() {
            return engineReport;
        }

        public List<String> getEngineErrors() {
            return engineErrors;
        }

        public void setEngineReport(@Nullable ResourceReport engineReport, List<String> engineErrors,
                                    List<String> engineWarnings) {
            this.engineReport = engineReport;
            this.engineErrors = List.copyOf(engineErrors);
            this.engineWarnings = List.copyOf(engineWarnings);
        }

        public void clearEngineReport() {
            setEngineReport(null, List.of(), List.of());
        }

        /**
         * @return errors found by Control: the view-level checks, the content validation and the reference check
         */
        public List<String> getLocalErrors() {
            List<String> errors = new ArrayList<>(viewErrors);
            errors.addAll(validationResult.getErrors());
            errors.addAll(referenceErrors);
            return errors;
        }

        /**
         * @return warnings found by Control: the content validation and the reference check
         */
        public List<String> getLocalWarnings() {
            List<String> warnings = new ArrayList<>(validationResult.getWarnings());
            warnings.addAll(referenceWarnings);
            return warnings;
        }

        /**
         * @return errors found by Control followed by the errors reported by the engine
         */
        public List<String> getErrors() {
            List<String> errors = getLocalErrors();
            errors.addAll(engineErrors);
            return errors;
        }

        /**
         * @return warnings found by Control followed by the warnings reported by the engine
         */
        public List<String> getWarnings() {
            List<String> warnings = getLocalWarnings();
            warnings.addAll(engineWarnings);
            return warnings;
        }

        public DeploymentResourceStatus getStatus() {
            if (!getErrors().isEmpty()) {
                return DeploymentResourceStatus.ERROR;
            }
            return getWarnings().isEmpty() ? DeploymentResourceStatus.OK : DeploymentResourceStatus.WARNING;
        }
    }
}
