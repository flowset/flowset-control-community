/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processdefinition;


import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import io.flowset.control.uicomponent.viewer.handler.CallActivityOverlayClickHandler;
import io.flowset.uikit.fragment.bpmnviewer.BpmnViewerFragment;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "process-definition-diagram", layout = DefaultMainViewParent.class)
@ViewController("ProcessDefinitionDiagramView")
@ViewDescriptor("process-definition-diagram-view.xml")
@DialogMode(width = "90%", height = "90%", minWidth = "40em", minHeight = "25em")
public class ProcessDefinitionDiagramView extends StandardView {

    protected ProcessDefinitionData processDefinition;
    @Autowired
    protected ProcessDefinitionService processDefinitionService;
    @ViewComponent
    protected BpmnViewerFragment viewerFragment;
    @ViewComponent
    protected InstanceContainer<ProcessDefinitionData> processDefinitionDc;
    @Autowired
    protected CallActivityOverlayClickHandler callActivityClickHandler;

    @SuppressWarnings("LombokSetterMayBeUsed")
    public void setProcessDefinition(ProcessDefinitionData processDefinitionId) {
        this.processDefinition = processDefinitionId;
    }

    @Subscribe
    public void onInit(final InitEvent event) {
        addClassNames(LumoUtility.Padding.Top.XSMALL, LumoUtility.Padding.Left.LARGE,
                LumoUtility.Padding.Right.LARGE);
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        processDefinitionDc.setItem(processDefinition);

        String bpmnXml = processDefinitionService.getBpmnXml(processDefinition.getProcessDefinitionId());
        viewerFragment.initViewer(bpmnXml);
        viewerFragment.showCalledProcessOverlays();
        viewerFragment.addCalledProcessOverlayClickListener(callActivityOverlayClickEvent -> {
            callActivityClickHandler.handleProcessNavigation(processDefinitionDc.getItem(),
                    callActivityOverlayClickEvent.getCallActivity(),
                    UiComponentUtils.isComponentAttachedToDialog(this));
        });
    }

}