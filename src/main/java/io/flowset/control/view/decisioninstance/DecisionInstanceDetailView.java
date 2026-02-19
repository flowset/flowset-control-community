package io.flowset.control.view.decisioninstance;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.core.LoadContext;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.flowset.control.action.CopyComponentValueToClipboardAction;
import io.flowset.control.entity.activity.HistoricActivityInstanceData;
import io.flowset.control.entity.decisioninstance.HistoricDecisionInputInstanceShortData;
import io.flowset.control.entity.decisioninstance.HistoricDecisionInstanceShortData;
import io.flowset.control.entity.decisioninstance.HistoricDecisionOutputInstanceShortData;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.service.activity.ActivityService;
import io.flowset.control.service.decisiondefinition.DecisionDefinitionService;
import io.flowset.control.service.decisioninstance.DecisionInstanceService;
import io.flowset.control.service.processinstance.ProcessInstanceService;
import io.flowset.control.view.processdefinition.ProcessDefinitionDetailView;
import io.flowset.control.view.processinstance.ProcessInstanceDetailView;
import io.flowset.uikit.component.dmnviewer.command.ShowDecisionInstanceCmd;
import io.flowset.uikit.component.dmnviewer.model.DecisionInstanceOutputData;
import io.flowset.uikit.fragment.dmnviewer.DmnViewerFragment;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@Route(value = "bpm/decision-instances/:id", layout = DefaultMainViewParent.class)
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
    @Autowired
    protected ProcessInstanceService processInstanceService;
    @Autowired
    protected ActivityService activityService;

    @ViewComponent
    protected InstanceContainer<HistoricDecisionInstanceShortData> decisionInstanceDc;
    @ViewComponent
    protected DmnViewerFragment dmnViewerFragment;
    @ViewComponent
    protected CopyComponentValueToClipboardAction copyToClipboardAction;
    @ViewComponent
    protected TypedTextField<String> decisionInstanceIdTextField;
    @ViewComponent
    protected HorizontalLayout detailActions;
    @ViewComponent
    protected TypedTextField<Object> activityNameTextField;
    @ViewComponent
    protected TypedTextField<Object> processBusinessKeyTextField;

    @Subscribe
    public void onInit(final InitEvent event) {
        detailActions.addClassNames(LumoUtility.Padding.Top.SMALL);

        boolean openedInDialog = UiComponentUtils.isComponentAttachedToDialog(this);
        detailActions.setJustifyContentMode(openedInDialog ? FlexComponent.JustifyContentMode.END: FlexComponent.JustifyContentMode.START);
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        dmnViewerFragment.initViewer();
        String dmnXml = decisionDefinitionService.getDmnXml(decisionInstanceDc.getItem().getDecisionDefinitionId());
        dmnViewerFragment.setDmnXml(dmnXml, setDmnXmlJson ->
                dmnViewerFragment.showDecisionDefinition(decisionInstanceDc.getItem().getDecisionDefinitionKey(),
                        showDecisionDefinitionJson -> dmnViewerFragment.showDecisionInstance(
                                createDecisionInstanceClientData(decisionInstanceDc.getItem())))
        );
        initAdditionalFields();
    }

    @Subscribe(id = "copyDecisionInstanceId", subject = "clickListener")
    public void onCopyDecisionInstanceIdClick(final ClickEvent<JmixButton> event) {
        copyToClipboardAction.setTarget(decisionInstanceIdTextField);
        copyToClipboardAction.actionPerform(event.getSource());
    }

    @Subscribe(id = "viewProcessDefinition", subject = "clickListener")
    public void onViewProcessDefinitionClick(final ClickEvent<JmixButton> event) {
        if (UiComponentUtils.isComponentAttachedToDialog(this)) {
            RouterLink routerLink = new RouterLink(ProcessDefinitionDetailView.class, new RouteParameters("id", getEditedEntity().getProcessDefinitionId()));
            getUI().ifPresent(ui -> ui.getPage().open(routerLink.getHref()));
        } else {
            viewNavigators.detailView(this, ProcessDefinitionData.class)
                    .withViewClass(ProcessDefinitionDetailView.class)
                    .withRouteParameters(new RouteParameters("id", getEditedEntity().getProcessDefinitionId()))
                    .withBackwardNavigation(true)
                    .navigate();
        }

    }

    @Subscribe(id = "viewProcessInstance", subject = "clickListener")
    public void onViewProcessInstanceClick(final ClickEvent<JmixButton> event) {
        if (UiComponentUtils.isComponentAttachedToDialog(this)) {
            RouterLink routerLink = new RouterLink(ProcessInstanceDetailView.class, new RouteParameters("id", getEditedEntity().getProcessInstanceId()));
            getUI().ifPresent(ui -> ui.getPage().open(routerLink.getHref()));
        } else {
            viewNavigators.detailView(this, ProcessInstanceData.class)
                    .withViewClass(ProcessInstanceDetailView.class)
                    .withRouteParameters(new RouteParameters("id", getEditedEntity().getProcessInstanceId()))
                    .withBackwardNavigation(true)
                    .navigate();
        }
    }

    @Supply(to = "inputsDataGrid.value", subject = "renderer")
    protected Renderer<HistoricDecisionInputInstanceShortData> inputValueRenderer() {
        return new TextRenderer<>(e -> e.getValue() != null ? e.getValue().toString() : null);
    }

    @Supply(to = "outputsDataGrid.value", subject = "renderer")
    protected Renderer<HistoricDecisionOutputInstanceShortData> outputValueRenderer() {
        return new TextRenderer<>(e -> e.getValue() != null ? e.getValue().toString() : null);
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

    @Install(to = "decisionInstanceDl", target = Target.DATA_LOADER)
    protected HistoricDecisionInstanceShortData decisionDefinitionDlDelegate(
            final LoadContext<HistoricDecisionInstanceShortData> loadContext) {
        HistoricDecisionInstanceShortData item = decisionInstanceDc.getItemOrNull();
        String id = item == null ? Objects.requireNonNull(loadContext.getId()).toString() : item.getId();
        return decisionInstanceService.getById(id);
    }

    protected void initAdditionalFields() {
        HistoricDecisionInstanceShortData decisionInstanceDcItem = decisionInstanceDc.getItem();
        if (decisionInstanceDcItem.getActivityInstanceId() != null) {
            HistoricActivityInstanceData activityInstanceData = activityService.findById(
                    decisionInstanceDcItem.getActivityInstanceId());
            if (activityInstanceData != null) {
                activityNameTextField.setTypedValue(activityInstanceData.getActivityName());
            }
        }
        if (decisionInstanceDcItem.getProcessInstanceId() != null) {
            ProcessInstanceData processInstanceData = processInstanceService.getProcessInstanceById(
                    decisionInstanceDcItem.getProcessInstanceId());
            if (processInstanceData != null) {
                processBusinessKeyTextField.setTypedValue(processInstanceData.getBusinessKey());
            }
        }
    }
}
