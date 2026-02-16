/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.decisiondefinition;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.exception.ViewEngineConnectionFailedException;
import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.core.Sort;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.UiEventPublisher;
import io.jmix.flowui.component.codeeditor.CodeEditor;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.entity.filter.DecisionDefinitionFilter;
import io.flowset.control.service.decisiondefinition.DecisionDefinitionLoadContext;
import io.flowset.control.service.decisiondefinition.DecisionDefinitionService;
import io.flowset.control.service.decisioninstance.DecisionInstanceService;
import io.flowset.control.service.processinstance.ProcessInstanceService;
import io.flowset.control.view.decisiondefinition.detail.GeneralPanelFragment;
import io.flowset.control.view.event.TitleUpdateEvent;
import io.flowset.uikit.fragment.dmnviewer.DmnViewerFragment;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;
import java.util.stream.Stream;

@Route(value = "bpm/decision-definitions/:id", layout = DefaultMainViewParent.class)
@ViewController(id = "bpm_DecisionDefinition.detail")
@ViewDescriptor("decision-definition-detail-view.xml")
@EditedEntityContainer("decisionDefinitionDc")
@DialogMode(minWidth = "80em", width = "90%", minHeight = "50em", height = "80%")
@PrimaryDetailView(DecisionDefinitionData.class)
public class DecisionDefinitionDetailView extends StandardDetailView<DecisionDefinitionData> {

    @Autowired
    protected DecisionDefinitionService decisionDefinitionService;

    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected UiEventPublisher uiEventPublisher;
    @ViewComponent
    protected MessageBundle messageBundle;
    @Autowired
    protected DataManager dataManager;

    @ViewComponent
    protected InstanceContainer<DecisionDefinitionData> decisionDefinitionDc;
    @ViewComponent
    protected JmixComboBox<DecisionDefinitionData> versionComboBox;

    @ViewComponent
    protected CodeEditor dmnXmlEditor;
    @ViewComponent
    protected DmnViewerFragment viewerFragment;
    @ViewComponent
    protected JmixTabSheet tabSheet;

    @ViewComponent
    protected InstanceLoader<DecisionDefinitionData> decisionDefinitionDl;

    protected String title = "";


    @ViewComponent
    protected GeneralPanelFragment generalPanel;
    @ViewComponent("tabSheet.decisionInstancesTab")
    protected Tab tabSheetDecisionInstancesTab;

    @Autowired
    protected ProcessInstanceService processInstanceService;
    @Autowired
    protected DecisionInstanceService decisionInstanceService;

    @Subscribe
    public void onInit(final InitEvent event) {
        initTabIcons();
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        sendUpdateViewTitleEvent();
    }

    @Override
    public String getPageTitle() {
        return title;
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        decisionDefinitionDl.load();

        initVersionLookup(getEditedEntity());
    }

    protected void initTabIcons() {
        tabSheet.getTabAt(0).addComponentAsFirst(VaadinIcon.SITEMAP.create());
        tabSheet.getTabAt(1).addComponentAsFirst(VaadinIcon.FILE_CODE.create());
    }

    @Install(to = "decisionDefinitionDl", target = Target.DATA_LOADER)
    protected DecisionDefinitionData decisionDefinitionDlDelegate(
            final LoadContext<DecisionDefinitionData> loadContext) {
        DecisionDefinitionData item = decisionDefinitionDc.getItemOrNull();
        String id = item == null ? Objects.requireNonNull(loadContext.getId()).toString() : item.getId();
        try {
            return decisionDefinitionService.getById(id);
        } catch (EngineConnectionFailedException e) {
            throw new ViewEngineConnectionFailedException(e, this);
        }
    }

    @Subscribe("versionComboBox")
    protected void onVersionLookupValueChange(AbstractField.ComponentValueChangeEvent<ComboBox<DecisionDefinitionData>, DecisionDefinitionData> event) {
        if (event.isFromClient()) {
            DecisionDefinitionData selectedProcessDefinition = event.getValue();
            decisionDefinitionDc.setItem(selectedProcessDefinition);

            sendUpdateViewTitleEvent();
        }
    }

    @Subscribe(id = "decisionDefinitionDc", target = Target.DATA_CONTAINER)
    protected void onDecisionDefinitionDcItemChange(InstanceContainer.ItemChangeEvent<DecisionDefinitionData> event) {
        DecisionDefinitionData decisionDefinition = event.getItem();
        if (decisionDefinition == null) {
            return;
        }

        String dmnXml = decisionDefinitionService.getDmnXml(decisionDefinition.getDecisionDefinitionId());
        viewerFragment.initViewer();
        viewerFragment.setDmnXml(dmnXml, decisionDefinition.getKey());
        generalPanel.refresh();
        updateCurrentVersionInstancesCount();

        dmnXmlEditor.setValue(dmnXml);

        sendUpdateViewTitleEvent();
    }

    @Install(to = "versionComboBox", subject = "itemsFetchCallback")
    protected Stream<DecisionDefinitionData> versionComboBoxItemsFetchCallback(final Query<Object, String> query) {
        int limit = query.getLimit();
        int offset = query.getOffset();

        DecisionDefinitionFilter filter = dataManager.create(DecisionDefinitionFilter.class);
        filter.setKey(getEditedEntity().getKey());
        filter.setLatestVersionOnly(false);

        query.getFilter().ifPresent(s -> {
            if (StringUtils.isNotBlank(s) && NumberUtils.isParsable(s)) {
                int version = Integer.parseInt(s);
                filter.setVersion(version);
            }
        });

        DecisionDefinitionLoadContext loadContext = new DecisionDefinitionLoadContext()
                .setFilter(filter)
                .setSort(Sort.by(Sort.Direction.DESC, "version"))
                .setFirstResult(offset)
                .setMaxResults(limit);

        return decisionDefinitionService.findAll(loadContext).stream();
    }

    @Install(to = "versionComboBox", subject = "itemLabelGenerator")
    protected String versionComboBoxItemLabelGenerator(final DecisionDefinitionData item) {
        return item.getVersion() != null ? String.valueOf(item.getVersion()) : null;
    }

    protected void updateCurrentVersionInstancesCount() {
        long currentVersionInstancesCount = decisionInstanceService.getCountByDecisionDefinitionId(getEditedEntity().getDecisionDefinitionId());
        updateTabCaption(currentVersionInstancesCount);
    }

    protected void updateTabCaption(long count) {
        tabSheetDecisionInstancesTab.setLabel(messageBundle.formatMessage("decisionInstancesTab.label", count));
        tabSheetDecisionInstancesTab.addComponentAsFirst(VaadinIcon.TASKS.create());
    }

    protected void sendUpdateViewTitleEvent() {
        this.title = messageBundle.formatMessage("decisionDefinition.title.name", getEditedEntity().getKey());

        FlexLayout flexLayout = uiComponents.create(FlexLayout.class);
        flexLayout.addClassNames(LumoUtility.Margin.Left.XSMALL, LumoUtility.Gap.SMALL, LumoUtility.AlignItems.CENTER);

        Span version = uiComponents.create(Span.class);
        version.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontWeight.BOLD);
        version.setText(messageBundle.formatMessage("decisionDefinition.title.version",
                getEditedEntity().getVersion().toString()));

        flexLayout.add(version);

        uiEventPublisher.publishEventForCurrentUI(new TitleUpdateEvent(this, title, flexLayout));
    }

    protected void initVersionLookup(DecisionDefinitionData decisionDefinition) {
        versionComboBox.setValue(decisionDefinition);
    }
}
