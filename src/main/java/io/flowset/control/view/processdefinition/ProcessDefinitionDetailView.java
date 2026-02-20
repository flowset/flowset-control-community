/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processdefinition;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.exception.ViewEngineConnectionFailedException;
import io.jmix.core.LoadContext;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.Sort;
import io.jmix.flowui.*;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.codeeditor.CodeEditor;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.activity.ProcessActivityStatistics;
import io.flowset.control.entity.dashboard.IncidentStatistics;
import io.flowset.control.entity.filter.ProcessDefinitionFilter;
import io.flowset.control.entity.filter.ProcessInstanceFilter;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.entity.processinstance.RuntimeProcessInstanceData;
import io.flowset.control.service.activity.ActivityService;
import io.flowset.control.service.processdefinition.ProcessDefinitionLoadContext;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import io.flowset.control.service.processinstance.ProcessInstanceLoadContext;
import io.flowset.control.service.processinstance.ProcessInstanceService;
import io.flowset.control.uicomponent.viewer.handler.BusinessRuleTaskOverlayClickHandler;
import io.flowset.control.uicomponent.viewer.handler.CallActivityOverlayClickHandler;
import io.flowset.control.view.event.TitleUpdateEvent;
import io.flowset.control.view.processdefinition.event.ReloadSelectedProcess;
import io.flowset.control.view.processdefinition.event.ResetActivityEvent;
import io.flowset.uikit.component.bpmnviewer.ViewerMode;
import io.flowset.uikit.component.bpmnviewer.command.AddMarkerCmd;
import io.flowset.uikit.component.bpmnviewer.command.ElementMarkerType;
import io.flowset.uikit.component.bpmnviewer.command.RemoveMarkerCmd;
import io.flowset.uikit.component.bpmnviewer.command.SetActivityStatisticsCmd;
import io.flowset.uikit.component.bpmnviewer.event.ElementClickEvent;
import io.flowset.uikit.fragment.bpmnviewer.BpmnViewerFragment;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Route(value = "bpm/process-definitions/:id", layout = DefaultMainViewParent.class)
@ViewController("bpm_ProcessDefinition.detail")
@ViewDescriptor("process-definition-detail-view.xml")
@EditedEntityContainer("processDefinitionDataDc")
@DialogMode(width = "50em", height = "37.5em")
@PrimaryDetailView(ProcessDefinitionData.class)
public class ProcessDefinitionDetailView extends StandardDetailView<ProcessDefinitionData> {
    public static final CloseAction REMOVE_PROCESS_DEFINITION_CLOSE_ACTION = new StandardCloseAction("removeProcessDefinition");

    protected String title = "";

    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected DialogWindows dialogWindows;
    @ViewComponent
    protected MessageBundle messageBundle;
    @Autowired
    protected ViewNavigators viewNavigators;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected Messages messages;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected UiEventPublisher uiEventPublisher;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected CallActivityOverlayClickHandler callActivityClickHandler;
    @Autowired
    protected BusinessRuleTaskOverlayClickHandler businessRuleTaskClickHandler;

    @ViewComponent
    protected InstanceContainer<ProcessDefinitionData> processDefinitionDataDc;
    @ViewComponent
    protected DataLoader processDefinitionDataDl;

    @ViewComponent
    protected CollectionContainer<RuntimeProcessInstanceData> processInstanceDataDc;
    @ViewComponent
    protected CollectionLoader<RuntimeProcessInstanceData> processInstanceDataDl;
    @ViewComponent
    protected InstanceContainer<ProcessInstanceFilter> processInstanceFilterDc;

    @ViewComponent
    protected ComboBox<ProcessDefinitionData> versionComboBox;

    @Autowired
    protected ProcessInstanceService processInstanceService;
    @Autowired
    protected ProcessDefinitionService processDefinitionService;
    @Autowired
    protected ActivityService activityService;

    @ViewComponent
    protected ProcessInstancesFragment processInstancesFragment;

    @ViewComponent
    protected BpmnViewerFragment viewerFragment;

    @ViewComponent
    protected CodeEditor bpmnXmlEditor;

    @ViewComponent
    protected Span allVersionsInstancesCountSpan;

    @ViewComponent
    protected GeneralPanelFragment generalPanel;

    @ViewComponent("tabsheet.processInstancesTab")
    protected Tab tabsheetProcessInstancesTab;

    @ViewComponent("tabsheet.bpmnXmlTab")
    protected Tab tabsheetBpmnXmlTab;

    @Subscribe
    public void onInit(final InitEvent event) {
        addClassNames(LumoUtility.Padding.Top.NONE);
        initTabIcons();
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        initProcessInstanceFilter();

        processDefinitionDataDl.load();
        initVersionLookup(getEditedEntity());

        updateAllRunningInstancesCount();

        viewerFragment.showStatisticsButton(true);
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        sendUpdateViewTitleEvent();
    }

    @Override
    public String getPageTitle() {
        return title;
    }

    protected void sendUpdateViewTitleEvent() {
        this.title = messageBundle.formatMessage("processDefinitionDetail.processName", getEditedEntity().getKey());

        FlexLayout flexLayout = uiComponents.create(FlexLayout.class);
        flexLayout.addClassNames(LumoUtility.Margin.Left.XSMALL, LumoUtility.Gap.SMALL, LumoUtility.AlignItems.CENTER);

        Span version = uiComponents.create(Span.class);
        version.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontWeight.BOLD);
        version.setText(messageBundle.formatMessage("processDefinitionDetail.processVersion", getEditedEntity().getVersion()));

        boolean active = BooleanUtils.isNotTrue(getEditedEntity().getSuspended());
        String text = active ? messageBundle.getMessage("active") : messageBundle.getMessage("suspended");
        Span badge = createStateBadge(active, text);

        flexLayout.add(version, badge);

        uiEventPublisher.publishEventForCurrentUI(new TitleUpdateEvent(this, title, flexLayout));
    }

    protected void initTabIcons() {
        tabsheetProcessInstancesTab.addComponentAsFirst(VaadinIcon.TASKS.create());
        tabsheetBpmnXmlTab.addComponentAsFirst(VaadinIcon.FILE_CODE.create());
    }


    @Subscribe("versionComboBox")
    protected void onVersionLookupValueChange(AbstractField.ComponentValueChangeEvent<ComboBox<ProcessDefinitionData>, ProcessDefinitionData> event) {
        if (event.isFromClient()) {
            ProcessDefinitionData selectedProcessDefinition = event.getValue();
            processDefinitionDataDc.setItem(selectedProcessDefinition);
        }
    }

    @Subscribe(id = "processDefinitionDataDc", target = Target.DATA_CONTAINER)
    protected void onProcessDefinitionDcItemChange(InstanceContainer.ItemChangeEvent<ProcessDefinitionData> event) {
        ProcessDefinitionData processDefinition = event.getItem();

        if (processDefinition != null) {
            String bpmnXml = processDefinitionService.getBpmnXml(processDefinition.getProcessDefinitionId());

            generalPanel.refresh();

            initViewer(bpmnXml);
            bpmnXmlEditor.setValue(bpmnXml);

            processInstanceFilterDc.getItem().setProcessDefinitionId(processDefinition.getProcessDefinitionId());

            sendUpdateViewTitleEvent();
        }
    }

    @Install(to = "processInstanceDataDl", target = Target.DATA_LOADER)
    protected List<RuntimeProcessInstanceData> processInstanceDataDlLoadDelegate(final LoadContext<RuntimeProcessInstanceData> loadContext) {
        ProcessInstanceFilter filter = processInstanceFilterDc.getItem();

        ProcessInstanceLoadContext context = new ProcessInstanceLoadContext().setFilter(filter)
                .setLoadIncidents(true);

        LoadContext.Query query = loadContext.getQuery();
        if (query != null) {
            context = context.setFirstResult(query.getFirstResult())
                    .setMaxResults(query.getMaxResults())
                    .setSort(query.getSort());
        }

        return processInstanceService.findAllRuntimeInstances(context);
    }

    @Install(to = "processDefinitionDataDl", target = Target.DATA_LOADER)
    protected ProcessDefinitionData loadProcessDefinition(LoadContext<ProcessDefinitionData> loadContext) {
        String id = Objects.requireNonNull(loadContext.getId()).toString();
        try {
            return processDefinitionService.getById(id);
        } catch (EngineConnectionFailedException e) {
            throw new ViewEngineConnectionFailedException(e, this);
        }
    }

    @Subscribe(id = "processInstanceFilterDc", target = Target.DATA_CONTAINER)
    protected void onProcessInstanceFilterDcItemPropertyChange(final InstanceContainer.ItemPropertyChangeEvent<ProcessInstanceFilter> event) {
        if (event.getProperty().equals("activeActivityIdIn")
                || event.getProperty().equals("processDefinitionId")) {
            updateCurrentVersionInstancesCount(event.getItem());
            processInstanceDataDl.load();
        }
    }

    @EventListener
    public void handleResetActivity(ResetActivityEvent resetActivityEvent) {
        String activityId = resetActivityEvent.getActivityId();
        viewerFragment.removeMarker(new RemoveMarkerCmd(activityId, ElementMarkerType.PRIMARY_COLOR_ACTIVITY));

        processInstanceFilterDc.getItem().setActiveActivityIdIn(null);
    }

    @EventListener
    public void handleReloadSelectedProcess(ReloadSelectedProcess reloadSelectedProcess) {
        ProcessDefinitionData item = processDefinitionDataDc.getItem();
        ProcessDefinitionData reloaded = processDefinitionService.getById(item.getId());

        processDefinitionDataDc.setItem(reloaded);

        updateCurrentVersionInstancesCount(processInstanceFilterDc.getItem());
        processInstanceDataDl.load();
    }

    protected void initViewer(String bpmnXml) {
        viewerFragment.initViewer(bpmnXml);
        viewerFragment.showCalledProcessOverlays();
        viewerFragment.addCalledProcessOverlayClickListener(callActivityOverlayClickEvent -> {
            callActivityClickHandler.handleProcessNavigation(processDefinitionDataDc.getItem(),
                    callActivityOverlayClickEvent.getCallActivity(),
                    UiComponentUtils.isComponentAttachedToDialog(this));
        });
        viewerFragment.showDecisionLinkOverlays();
        viewerFragment.addDecisionLinkOverlayClickListener(businessRuleTaskOverlayClickEvent -> {
            businessRuleTaskClickHandler.handleDecisionNavigation(processDefinitionDataDc.getItem(),
                    businessRuleTaskOverlayClickEvent.getBusinessRuleTask(),
                    UiComponentUtils.isComponentAttachedToDialog(this));
        });
        showStatistics();
    }

    protected void showStatistics() {
        List<ProcessActivityStatistics> processStatistics = activityService.getStatisticsByProcessId(getEditedEntity().getProcessDefinitionId());

        List<String> activeElements = new ArrayList<>();
        processStatistics.forEach(activityStatistics -> {
            Optional<Integer> totalIncidentCount = CollectionUtils.emptyIfNull(activityStatistics.getIncidents())
                    .stream()
                    .map(IncidentStatistics::getIncidentCount)
                    .filter(Objects::nonNull)
                    .reduce(Integer::sum);

            SetActivityStatisticsCmd cmd = new SetActivityStatisticsCmd(activityStatistics.getActivityId());
            cmd.setIncidentCount(totalIncidentCount.orElse(null));
            cmd.setInstanceCount(activityStatistics.getInstanceCount());

            viewerFragment.setActivityStatistics(cmd);
            activeElements.add(activityStatistics.getActivityId());
        });

        viewerFragment.setMode(ViewerMode.INTERACTIVE);
        viewerFragment.setActiveElements(activeElements);
        viewerFragment.addElementClickListener(this::handleDiagramElementClick);
    }

    protected void handleDiagramElementClick(ElementClickEvent elementClickEvent) {
        ProcessInstanceFilter instanceFilter = processInstanceFilterDc.getItem();

        List<String> activityIdIn = instanceFilter.getActiveActivityIdIn();
        String clickedElementId = elementClickEvent.getElementId();
        boolean sameElementClicked = false;

        if (CollectionUtils.isNotEmpty(activityIdIn)) {
            activityIdIn.forEach(activityId -> viewerFragment.removeMarker(new RemoveMarkerCmd(activityId, ElementMarkerType.PRIMARY_COLOR_ACTIVITY)));
            sameElementClicked = activityIdIn.contains(clickedElementId);
        }

        if (sameElementClicked) {
            instanceFilter.setActiveActivityIdIn(null);
            processInstancesFragment.clearActivity();
        } else {
            instanceFilter.setActiveActivityIdIn(List.of(clickedElementId));
            viewerFragment.addMarker(new AddMarkerCmd(clickedElementId, ElementMarkerType.PRIMARY_COLOR_ACTIVITY));
            processInstancesFragment.showActivity(clickedElementId, elementClickEvent.getElementType(),
                    elementClickEvent.getElementName());
        }
    }

    protected void updateAllRunningInstancesCount() {
        long allVersionsInstancesCount = processInstanceService.getCountByProcessDefinitionKey(getEditedEntity().getKey());
        allVersionsInstancesCountSpan.setText(String.valueOf(allVersionsInstancesCount));
    }

    protected void updateCurrentVersionInstancesCount(ProcessInstanceFilter filter) {
        long currentVersionInstancesCount = processInstanceService.getRuntimeInstancesCount(filter);
        updateTabCaption(currentVersionInstancesCount);
    }

    protected void updateTabCaption(long count) {
        tabsheetProcessInstancesTab.setLabel(messageBundle.formatMessage("processInstancesTab.label", count));
        tabsheetProcessInstancesTab.addComponentAsFirst(VaadinIcon.TASKS.create());
    }

    protected void initProcessInstanceFilter() {
        ProcessInstanceFilter processInstanceFilter = metadata.create(ProcessInstanceFilter.class);
        processInstanceFilter.setUnfinished(true);
        processInstanceFilterDc.setItem(processInstanceFilter);
    }

    protected void initVersionLookup(ProcessDefinitionData processDefinition) {
        ProcessDefinitionFilter filter = metadata.create(ProcessDefinitionFilter.class);
        filter.setLatestVersionOnly(false);
        filter.setKey(processDefinition.getKey());

        List<ProcessDefinitionData> optionsList = processDefinitionService.findAll(new ProcessDefinitionLoadContext()
                .setFilter(filter)
                .setSort(Sort.by(Sort.Direction.DESC, "version")));
        versionComboBox.setItems(optionsList);
        versionComboBox.setItemLabelGenerator(item -> item.getVersion() != null ? String.valueOf(item.getVersion()) : null);
        versionComboBox.setValue(processDefinition);
    }

    protected Span createStateBadge(boolean success, String text) {
        Span badge = uiComponents.create(Span.class);
        String themeNames = success ? "badge success pill" : "badge warning pill";
        badge.getElement().getThemeList().add(themeNames);

        badge.setText(text);
        return badge;
    }
}
