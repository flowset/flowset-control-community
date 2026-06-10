/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processdefinition.detail;

import io.flowset.control.test_support.ui.component.BpmnViewerFragment;
import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.CodeEditor;
import io.jmix.masquerade.component.ComboBox;
import io.jmix.masquerade.component.TabSheet;
import io.jmix.masquerade.component.Unknown;
import io.jmix.masquerade.sys.View;
import lombok.Getter;

import static io.jmix.masquerade.JConditions.SELECTED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

/**
 * Wrapper for the Process detail view.
 * Source view: {@link io.flowset.control.view.processdefinition.ProcessDefinitionDetailView}
 */
@Getter
@TestView(id = "bpm_ProcessDefinition.detail")
public class ProcessDefinitionDetailView extends View<ProcessDefinitionDetailView> {

    @TestComponent(path = "allRunningInstancesGroup")
    private Unknown allRunningInstancesGroup;

    @TestComponent(path = "allVersionsInstancesCountSpan")
    private Unknown allVersionsInstancesCountValue;

    @TestComponent(path = "generalPanel")
    private ProcessGeneralPanelFragment generalPanel;

    @TestComponent(path = "tabsheet")
    private TabSheet tabs;

    @TestComponent(path = {"diagramBox", "viewerFragmentViewerVBox"})
    private BpmnViewerFragment bpmnViewerFragment;

    @TestComponent(path = "versionComboBox")
    private ComboBox versionComboBox;

    @TestComponent(path = "bpmnXmlEditor")
    private CodeEditor bpmnXmlEditor;


    /**
     * Opens the Process Instances tab.
     *
     * @return content of the Process Instances tab
     */
    public ProcessInstancesTabFragment openProcessInstancesTab() {
        tabs.getTabById("processInstancesTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        return $j(ProcessInstancesTabFragment.class, "processInstancesFragmentProcessInstanceVBox")
                .exists()
                .shouldBe(VISIBLE);
    }

    /**
     * Opens the Called Processes tab.
     *
     * @return content of the Called Processes tab
     */
    public CalledProcessesTabFragment openCalledProcessesTab() {
        tabs.getTabById("calledProcessesTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        return $j(CalledProcessesTabFragment.class, "calledProcessesFragmentRoot")
                .exists()
                .shouldBe(VISIBLE);
    }

    /**
     * Opens the Called Decisions tab.
     *
     * @return content of the Called Decisions tab
     */
    public CalledDecisionsTabFragment openDecisionsTab() {
        tabs.getTabById("decisionsTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        return $j(CalledDecisionsTabFragment.class, "decisionsFragmentRoot")
                .exists()
                .shouldBe(VISIBLE);
    }
}
