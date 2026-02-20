/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.decisioninstance;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.RouteParameters;
import io.flowset.control.action.CopyComponentValueToClipboardAction;
import io.flowset.control.entity.activity.HistoricActivityInstanceData;
import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.entity.decisioninstance.HistoricDecisionInstanceShortData;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.service.activity.ActivityService;
import io.flowset.control.service.processinstance.ProcessInstanceService;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@FragmentDescriptor("decision-instance-general-panel-fragment.xml")
public class DecisionInstanceGeneralPanelFragment extends Fragment<FlexLayout> {

    @Autowired
    protected ViewNavigators viewNavigators;

    @ViewComponent
    protected MessageBundle messageBundle;
    @Autowired
    protected ActivityService activityService;

    @Autowired
    protected ProcessInstanceService processInstanceService;

    @ViewComponent
    protected InstanceContainer<HistoricDecisionInstanceShortData> decisionInstanceDc;

    @ViewComponent
    protected TextField processDefinitionField;

    @ViewComponent
    protected VerticalLayout upperPanel;
    @ViewComponent
    protected JmixButton infoBtn;

    @ViewComponent
    protected JmixButton openProcessDefinitionEditorBtn;
    @ViewComponent
    protected JmixButton openProcessInstanceEditorBtn;

    @ViewComponent
    protected TypedTextField<String> decisionInstanceIdTextField;
    @ViewComponent
    protected TypedTextField<String> activityNameTextField;
    @ViewComponent
    protected TypedTextField<String> processBusinessKeyTextField;
    @ViewComponent
    protected CopyComponentValueToClipboardAction copyDecisionInstanceIdAction;


    @Subscribe(target = Target.HOST_CONTROLLER)
    public void onHostInit(final View.InitEvent event) {
        copyDecisionInstanceIdAction.setTarget(decisionInstanceIdTextField);
    }

    @Subscribe(target = Target.HOST_CONTROLLER)
    public void onHostBeforeShow(View.BeforeShowEvent event) {
        HistoricDecisionInstanceShortData decisionInstanceData = decisionInstanceDc.getItem();

        openProcessDefinitionEditorBtn.setVisible(decisionInstanceData.getProcessDefinitionId() != null);
        openProcessInstanceEditorBtn.setVisible(decisionInstanceData.getProcessInstanceId() != null);

        initAdditionalFields();
    }

    @Subscribe(id = "openDecisionDefinitionEditorBtn", subject = "clickListener")
    public void onOpenDecisionDefinitionEditorBtnClick(final ClickEvent<JmixButton> event) {
        viewNavigators.detailView(getCurrentView(), DecisionDefinitionData.class)
                .withRouteParameters(new RouteParameters("id", decisionInstanceDc.getItem().getDecisionDefinitionId()))
                .withBackwardNavigation(true)
                .navigate();
    }

    @Subscribe(id = "openProcessInstanceEditorBtn", subject = "clickListener")
    public void onOpenProcessInstanceEditorBtnClick(final ClickEvent<JmixButton> event) {
        viewNavigators.detailView(getCurrentView(), ProcessInstanceData.class)
                .withRouteParameters(new RouteParameters("id", decisionInstanceDc.getItem().getProcessInstanceId()))
                .withBackwardNavigation(true)
                .navigate();
    }

    @Subscribe("openProcessDefinitionEditorBtn")
    public void openProcessDefinitionEditor(ClickEvent<Button> event) {
        viewNavigators.detailView(getCurrentView(), ProcessDefinitionData.class)
                .withRouteParameters(new RouteParameters("id", decisionInstanceDc.getItem().getProcessDefinitionId()))
                .withBackwardNavigation(true)
                .navigate();
    }

    @Subscribe("infoBtn")
    protected void onInfoButtonClickBtnClick(ClickEvent<Button> event) {
        upperPanel.setVisible(!upperPanel.isVisible());

        if (upperPanel.isVisible()) {
            infoBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            infoBtn.setTitle(messageBundle.getMessage("hideDecisionInstanceDetails"));
        } else {
            infoBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            infoBtn.setTitle(messageBundle.getMessage("viewDecisionInstanceDetails"));
        }

    }

    protected void initAdditionalFields() {
        HistoricDecisionInstanceShortData decisionInstanceDcItem = decisionInstanceDc.getItem();

        String activityInstanceId = decisionInstanceDcItem.getActivityInstanceId();
        if (activityInstanceId != null) {
            HistoricActivityInstanceData activityInstanceData = activityService.findById(activityInstanceId);
            if (activityInstanceData != null) {
                activityNameTextField.setTypedValue(activityInstanceData.getActivityName());
            }
        }
        String processInstanceId = decisionInstanceDcItem.getProcessInstanceId();
        if (processInstanceId != null) {
            ProcessInstanceData processInstanceData = processInstanceService.getProcessInstanceById(processInstanceId);
            if (processInstanceData != null) {
                processBusinessKeyTextField.setTypedValue(processInstanceData.getBusinessKey());
            }
        }
    }
}
