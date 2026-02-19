package io.flowset.control.view.decisioninstance;

import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.view.event.TitleUpdateEvent;
import io.flowset.control.view.util.ComponentHelper;
import io.jmix.core.LoadContext;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.UiEventPublisher;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.decisioninstance.HistoricDecisionInputInstanceShortData;
import io.flowset.control.entity.decisioninstance.HistoricDecisionInstanceShortData;
import io.flowset.control.entity.decisioninstance.HistoricDecisionOutputInstanceShortData;
import io.flowset.control.service.decisiondefinition.DecisionDefinitionService;
import io.flowset.control.service.decisioninstance.DecisionInstanceService;
import io.flowset.uikit.component.dmnviewer.command.ShowDecisionInstanceCmd;
import io.flowset.uikit.component.dmnviewer.model.DecisionInstanceOutputData;
import io.flowset.uikit.fragment.dmnviewer.DmnViewerFragment;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@Route(value = "decision-instances/:id", layout = DefaultMainViewParent.class)
@ViewController(id = "bpm_DecisionInstance.detail")
@ViewDescriptor("decision-instance-detail-view.xml")
@EditedEntityContainer("decisionInstanceDc")
@DialogMode(minWidth = "80em", width = "90%", minHeight = "50em", height = "80%")
@PrimaryDetailView(HistoricDecisionInstanceShortData.class)
public class DecisionInstanceDetailView extends StandardDetailView<HistoricDecisionInstanceShortData> {

    @Autowired
    protected DecisionDefinitionService decisionDefinitionService;
    @Autowired
    protected DecisionInstanceService decisionInstanceService;
    @Autowired
    protected ViewNavigators viewNavigators;

    @ViewComponent
    protected InstanceContainer<HistoricDecisionInstanceShortData> decisionInstanceDc;
    @ViewComponent
    protected DmnViewerFragment dmnViewerFragment;
    @Autowired
    protected UiEventPublisher uiEventPublisher;
    @ViewComponent
    protected MessageBundle messageBundle;

    @Autowired
    protected UiComponents uiComponents;

    @Autowired
    protected ComponentHelper componentHelper;

    @ViewComponent
    protected InstanceContainer<DecisionDefinitionData> decisionDefinitionDc;

    @ViewComponent
    protected HorizontalLayout detailActions;
    @ViewComponent("tabsheet.inputsTab")
    protected Tab tabsheetInputsTab;
    @ViewComponent("tabsheet.outputsTab")
    protected Tab tabsheetOutputsTab;
    
    protected String title = "";

    @Subscribe
    public void onInit(final InitEvent event) {
        detailActions.addClassNames(LumoUtility.Padding.Top.SMALL);

        boolean openedInDialog = UiComponentUtils.isComponentAttachedToDialog(this);
        detailActions.setJustifyContentMode(openedInDialog ? FlexComponent.JustifyContentMode.END : FlexComponent.JustifyContentMode.START);

        initTabIcons();
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        String decisionDefinitionId = decisionInstanceDc.getItem().getDecisionDefinitionId();
        DecisionDefinitionData decisionDefinitionData = decisionDefinitionService.getById(decisionDefinitionId);
        decisionDefinitionDc.setItem(decisionDefinitionData);

        dmnViewerFragment.initViewer();
        String dmnXml = decisionDefinitionService.getDmnXml(decisionDefinitionId);
        dmnViewerFragment.setDmnXml(dmnXml, setDmnXmlJson ->
                dmnViewerFragment.showDecisionDefinition(decisionInstanceDc.getItem().getDecisionDefinitionKey(),
                        showDecisionDefinitionJson -> dmnViewerFragment.showDecisionInstance(
                                createDecisionInstanceClientData(decisionInstanceDc.getItem())))
        );
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        sendUpdateViewTitleEvent();
    }

    @Override
    public String getPageTitle() {
        return title;
    }

    @Supply(to = "inputsDataGrid.value", subject = "renderer")
    protected Renderer<HistoricDecisionInputInstanceShortData> inputValueRenderer() {
        return new TextRenderer<>(e -> e.getValue() != null ? e.getValue().toString() : null);
    }

    @Supply(to = "outputsDataGrid.value", subject = "renderer")
    protected Renderer<HistoricDecisionOutputInstanceShortData> outputValueRenderer() {
        return new TextRenderer<>(e -> e.getValue() != null ? e.getValue().toString() : null);
    }

    @Install(to = "decisionInstanceDl", target = Target.DATA_LOADER)
    protected HistoricDecisionInstanceShortData decisionDefinitionDlDelegate(
            final LoadContext<HistoricDecisionInstanceShortData> loadContext) {
        HistoricDecisionInstanceShortData item = decisionInstanceDc.getItemOrNull();
        String id = item == null ? Objects.requireNonNull(loadContext.getId()).toString() : item.getId();
        return decisionInstanceService.getById(id);
    }

    protected void sendUpdateViewTitleEvent() {
        this.title = messageBundle.formatMessage("decisionInstanceDetailView.title", getEditedEntity().getDecisionInstanceId());

        String titleText = messageBundle.getMessage("decisionInstanceDetailView.baseTitle");
        FlexLayout titleLayout = createTitleLayout();

        uiEventPublisher.publishEventForCurrentUI(new TitleUpdateEvent(this, titleText, titleLayout));
    }

    protected FlexLayout createTitleLayout() {
        FlexLayout flexLayout = uiComponents.create(FlexLayout.class);
        flexLayout.addClassNames(LumoUtility.Margin.Left.XSMALL, LumoUtility.Gap.SMALL);
        flexLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        H5 instanceId = createInstanceIdComponent();

        Span decisionDefinitionBadge = createDecisionBadge();

        flexLayout.add(instanceId, decisionDefinitionBadge);
        return flexLayout;
    }

    protected Span createDecisionBadge() {
        Span decisionDefinitionBadge = uiComponents.create(Span.class);
        decisionDefinitionBadge.getElement().getThemeList().add("badge normal pill");

        String decisionBadgeText = componentHelper.getDecisionLabel(decisionDefinitionDc.getItemOrNull());
        decisionDefinitionBadge.setText(decisionBadgeText);

        return decisionDefinitionBadge;
    }
    protected H5 createInstanceIdComponent() {
        H5 instanceId = new H5("\"%s\"".formatted(getEditedEntity().getDecisionInstanceId()));
        instanceId.setHeightFull();
        instanceId.addClassNames(LumoUtility.TextColor.BODY);
        return instanceId;
    }

    protected ShowDecisionInstanceCmd createDecisionInstanceClientData(
            HistoricDecisionInstanceShortData decisionInstance) {
        ShowDecisionInstanceCmd decisionInstanceClientData = new ShowDecisionInstanceCmd();
        decisionInstanceClientData.setOutputDataList(decisionInstance.getOutputs().stream().map(output -> {
            DecisionInstanceOutputData result = new DecisionInstanceOutputData();
            result.setValue(output.getValue() != null ? output.getValue().toString() : "");
            result.setDataRowId(output.getRuleId());
            result.setDataColId(output.getClauseId());
            return result;
        }).toList());
        return decisionInstanceClientData;
    }

    protected void initTabIcons() {
        tabsheetInputsTab.addComponentAsFirst(VaadinIcon.DOWNLOAD_ALT.create());
        tabsheetOutputsTab.addComponentAsFirst(VaadinIcon.UPLOAD_ALT.create());
    }
}
