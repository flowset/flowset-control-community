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
     * Opens Runtime tab -> Variables tab.
     * @return content of the opened Variables tab
     */
    public RuntimeVariablesTabFragment openRuntimeVariablesTab() {
        tabs.getTabById("runtimeTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        return $j(RuntimeVariablesTabFragment.class, "runtimeTabRoot")
                .exists()
                .shouldBe(VISIBLE);
    }

    /**
     * Opens Runtime tab -> User Tasks tab.
     * @return content of the opened User Tasks tab
     */
    public RuntimeUserTasksTabFragment openRuntimeUserTasksTab() {
        tabs.getTabById("runtimeTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        $j(TabSheet.class, "runtimeTabsheet")
                .exists()
                .shouldBe(VISIBLE)
                .getTabById("userTasksTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        return $j(RuntimeUserTasksTabFragment.class, "runtimeTabRoot")
                .exists()
                .shouldBe(VISIBLE);
    }

    /**
     * Opens Runtime tab -> Jobs tab.
     * @return content of the opened Jobs tab
     */
    public JobsTabFragment openRuntimeJobsTab() {
        tabs.getTabById("runtimeTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        $j(TabSheet.class, "runtimeTabsheet")
                .exists()
                .shouldBe(VISIBLE)
                .getTabById("jobsTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        return $j(JobsTabFragment.class, "runtimeTabRoot")
                .exists()
                .shouldBe(VISIBLE);
    }

    /**
     * Opens Runtime tab -> External Tasks tab.
     * @return content of the opened External Tasks tab
     */
    public ExternalTasksTabFragment openRuntimeExternalTasksTab() {
        tabs.getTabById("runtimeTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        $j(TabSheet.class, "runtimeTabsheet")
                .exists()
                .shouldBe(VISIBLE)
                .getTabById("externalTasksTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        return $j(ExternalTasksTabFragment.class, "runtimeTabRoot")
                .exists()
                .shouldBe(VISIBLE);
    }

    /**
     * Opens History tab -> Activities tab.
     * @return content of the opened Activities tab
     */
    public HistoryActivitiesTabFragment openHistoryActivitiesTab() {
        tabs.getTabById("historyTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        $j(TabSheet.class, "historyTabsheet")
                .exists()
                .shouldBe(VISIBLE)
                .getTabById("historicActivityInstancesTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        return $j(HistoryActivitiesTabFragment.class, "historyTabsheet")
                .exists()
                .shouldBe(VISIBLE);
    }

    /**
     * Opens History tab -> Decisions tab.
     * @return content of the opened Decisions tab
     */
    public HistoryDecisionsTabFragment openHistoryDecisionsTab() {
        tabs.getTabById("historyTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        $j(TabSheet.class, "historyTabsheet")
                .exists()
                .shouldBe(VISIBLE)
                .getTabById("historyDecisionsTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        return $j(HistoryDecisionsTabFragment.class, "historyTabsheet")
                .exists()
                .shouldBe(VISIBLE);
    }

    /**
     * Opens History tab -> Incidents tab.
     * @return content of the opened Incidents tab
     */
    public HistoryIncidentsTabFragment openHistoryIncidentsTab() {
        tabs.getTabById("historyTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        $j(TabSheet.class, "historyTabsheet")
                .exists()
                .shouldBe(VISIBLE)
                .getTabById("historyIncidentsTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        return $j(HistoryIncidentsTabFragment.class, "historyTabsheet")
                .exists()
                .shouldBe(VISIBLE);
    }

    /**
     * Opens History tab -> Variables tab.
     * @return content of the opened Variables tab
     */
    public HistoryVariablesTabFragment openHistoryVariablesTab() {
        tabs.getTabById("historyTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        $j(TabSheet.class, "historyTabsheet")
                .exists()
                .shouldBe(VISIBLE)
                .getTabById("historyVariablesTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        return $j(HistoryVariablesTabFragment.class, "historyTabsheet")
                .exists()
                .shouldBe(VISIBLE);
    }

    /**
     * Opens History tab -> User Tasks tab.
     * @return content of the opened User Tasks tab
     */
    public HistoryUserTasksTabFragment openHistoryUserTasksTab() {
        tabs.getTabById("historyTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        $j(TabSheet.class, "historyTabsheet")
                .exists()
                .shouldBe(VISIBLE)
                .getTabById("historyTasksTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        return $j(HistoryUserTasksTabFragment.class, "historyTabsheet")
                .exists()
                .shouldBe(VISIBLE);
    }

    /**
     * Opens Runtime tab -> Incidents tab.
     * @return content of the opened Incidents tab
     */
    public RuntimeIncidentsTabFragment openRuntimeIncidentsTab() {
        tabs.getTabById("runtimeTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        $j(TabSheet.class, "runtimeTabsheet")
                .exists()
                .shouldBe(VISIBLE)
                .getTabById("incidentsTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);

        return $j(RuntimeIncidentsTabFragment.class, "runtimeTabRoot")
                .exists()
                .shouldBe(VISIBLE);
    }
}
