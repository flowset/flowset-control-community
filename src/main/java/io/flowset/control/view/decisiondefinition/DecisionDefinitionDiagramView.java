/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.decisiondefinition;

import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.service.decisiondefinition.DecisionDefinitionService;
import io.flowset.uikit.fragment.dmnviewer.DmnViewerFragment;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "decision-definition-diagram", layout = DefaultMainViewParent.class)
@ViewController("DecisionDefinitionDiagramView")
@ViewDescriptor("decision-definition-diagram-view.xml")
@DialogMode(width = "90%", height = "90%", minWidth = "40em", minHeight = "25em")
public class DecisionDefinitionDiagramView extends StandardView {

    protected DecisionDefinitionData decisionDefinition;
    @Autowired
    protected DecisionDefinitionService decisionDefinitionService;
    @ViewComponent
    protected DmnViewerFragment viewerFragment;
    @ViewComponent
    protected InstanceContainer<DecisionDefinitionData> decisionDefinitionDc;

    @SuppressWarnings("LombokSetterMayBeUsed")
    public void setDecisionDefinition(DecisionDefinitionData decisionDefinition) {
        this.decisionDefinition = decisionDefinition;
    }

    @Subscribe
    public void onInit(final InitEvent event) {
        addClassNames(LumoUtility.Padding.Top.XSMALL, LumoUtility.Padding.Left.LARGE,
                LumoUtility.Padding.Right.LARGE);
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        decisionDefinitionDc.setItem(decisionDefinition);

        String dmnXml = decisionDefinitionService.getDmnXml(decisionDefinition.getDecisionDefinitionId());
        viewerFragment.initViewer();
        viewerFragment.setDmnXml(dmnXml, decisionDefinition.getKey());
    }
}
