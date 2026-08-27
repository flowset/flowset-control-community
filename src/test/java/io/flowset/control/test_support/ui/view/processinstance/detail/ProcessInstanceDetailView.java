/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processinstance.detail;

import io.flowset.control.test_support.ui.component.BpmnViewerFragment;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.*;
import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.TabSheet;
import io.jmix.masquerade.sys.View;
import lombok.Getter;

import static io.jmix.masquerade.JConditions.SELECTED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;
import static org.openqa.selenium.By.cssSelector;

/**
 * Wrapper for the Process instance detail view.
 * Source view: {@link io.flowset.control.view.processinstance.ProcessInstanceDetailView}
 */
@Getter
@TestView(id = "bpm_ProcessInstanceData.detail")
public class ProcessInstanceDetailView extends View<ProcessInstanceDetailView> {

    @TestComponent(path = "generalPanel")
    private ProcessInstanceGeneralPanel generalPanel;

    @TestComponent(path = "relatedEntitiesTabSheet")
    private TabSheet tabs;

    @TestComponent(path = "viewerFragmentViewerVBox")
    private BpmnViewerFragment bpmnViewerFragment;

    @TestComponent(path = "closeAction")
    private Button closeButton;

    /**
     * Opens Runtime tab.
     *
     * @return content of the opened Runtime tab
     */
    public RuntimeTabFragment openRuntimeTab() {
        tabs.getTabById("runtimeTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        return $j(RuntimeTabFragment.class, "runtimeTabFragmentRuntimeTabRoot")
                .exists()
                .shouldBe(VISIBLE);
    }

    /**
     * Opens History tab.
     *
     * @return content of the opened History tab
     */
    public HistoryTabFragment openHistoryTab() {
        tabs.getTabById("historyTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        return $j(HistoryTabFragment.class, cssSelector("div[role='tabpanel'][tab='historyTab']"))
                .exists()
                .shouldBe(VISIBLE);
    }

    /**
     * Opens Runtime tab -> Variables tab.
     *
     * @return content of the opened Variables tab
     */
    public RuntimeVariablesTabFragment openRuntimeVariablesTab() {
        openRuntimeTab();

        return $j(RuntimeVariablesTabFragment.class, "runtimeTabFragmentRuntimeTabRoot")
                .exists()
                .shouldBe(VISIBLE);
    }

    /**
     * Opens Runtime tab -> User Tasks tab.
     *
     * @return content of the opened User Tasks tab
     */
    public RuntimeUserTasksTabFragment openRuntimeUserTasksTab() {
        RuntimeTabFragment runtimeTabFragment = openRuntimeTab();

        runtimeTabFragment.getTabsheet()
                .getTabById("userTasksTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        return $j(RuntimeUserTasksTabFragment.class, "runtimeTabFragmentRuntimeTabRoot")
                .exists()
                .shouldBe(VISIBLE);
    }

    /**
     * Opens Runtime tab -> Jobs tab.
     *
     * @return content of the opened Jobs tab
     */
    public JobsTabFragment openRuntimeJobsTab() {
        RuntimeTabFragment runtimeTabFragment = openRuntimeTab();

        runtimeTabFragment.getTabsheet()
                .getTabById("jobsTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        return $j(JobsTabFragment.class, "runtimeTabFragmentRuntimeTabRoot")
                .exists()
                .shouldBe(VISIBLE);
    }

    /**
     * Opens Runtime tab -> External Tasks tab.
     *
     * @return content of the opened External Tasks tab
     */
    public ExternalTasksTabFragment openRuntimeExternalTasksTab() {
        RuntimeTabFragment runtimeTabFragment = openRuntimeTab();

        runtimeTabFragment.getTabsheet()
                .getTabById("externalTasksTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        return $j(ExternalTasksTabFragment.class, "runtimeTabFragmentRuntimeTabRoot")
                .exists()
                .shouldBe(VISIBLE);
    }

    /**
     * Opens History tab -> Activities tab.
     *
     * @return content of the opened Activities tab
     */
    public HistoryActivitiesTabFragment openHistoryActivitiesTab() {
        HistoryTabFragment historyTab = openHistoryTab();

        historyTab.getTabsheet()
                .getTabById("historicActivityInstancesTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        return $j(HistoryActivitiesTabFragment.class, "historyTabFragmentHistoryTabsheet")
                .exists()
                .shouldBe(VISIBLE);
    }

    /**
     * Opens History tab -> Decisions tab.
     *
     * @return content of the opened Decisions tab
     */
    public HistoryDecisionsTabFragment openHistoryDecisionsTab() {
        HistoryTabFragment historyTab = openHistoryTab();

        historyTab.getTabsheet()
                .getTabById("historyDecisionsTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        return $j(HistoryDecisionsTabFragment.class, "historyTabFragmentHistoryTabsheet")
                .exists()
                .shouldBe(VISIBLE);
    }

    /**
     * Opens History tab -> Incidents tab.
     *
     * @return content of the opened Incidents tab
     */
    public HistoryIncidentsTabFragment openHistoryIncidentsTab() {
        HistoryTabFragment historyTab = openHistoryTab();

        historyTab.getTabsheet()
                .getTabById("historyIncidentsTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        return $j(HistoryIncidentsTabFragment.class, "historyTabFragmentHistoryTabsheet")
                .exists()
                .shouldBe(VISIBLE);
    }

    /**
     * Opens History tab -> Variables tab.
     *
     * @return content of the opened Variables tab
     */
    public HistoryVariablesTabFragment openHistoryVariablesTab() {
        HistoryTabFragment historyTab = openHistoryTab();

        historyTab.getTabsheet()
                .getTabById("historyVariablesTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        return $j(HistoryVariablesTabFragment.class, "historyTabFragmentHistoryTabsheet")
                .exists()
                .shouldBe(VISIBLE);
    }

    /**
     * Opens History tab -> User Tasks tab.
     *
     * @return content of the opened User Tasks tab
     */
    public HistoryUserTasksTabFragment openHistoryUserTasksTab() {
        HistoryTabFragment historyTab = openHistoryTab();

        historyTab.getTabsheet()
                .getTabById("historyTasksTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        return $j(HistoryUserTasksTabFragment.class, "historyTabFragmentHistoryTabsheet")
                .exists()
                .shouldBe(VISIBLE);
    }

    /**
     * Opens Runtime tab -> Incidents tab.
     *
     * @return content of the opened Incidents tab
     */
    public RuntimeIncidentsTabFragment openRuntimeIncidentsTab() {
        RuntimeTabFragment runtimeTabFragment = openRuntimeTab();

        runtimeTabFragment.getTabsheet()
                .getTabById("incidentsTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        return $j(RuntimeIncidentsTabFragment.class, "runtimeTabFragmentRuntimeTabRoot")
                .exists()
                .shouldBe(VISIBLE);
    }
}
