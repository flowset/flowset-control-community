/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.main;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.Unknown;
import io.jmix.masquerade.sys.Composite;
import lombok.Getter;

/**
 * Wrapper for the dashboard fragment displayed on the main view.
 * Source component: {@link io.flowset.control.view.dashboard.DashboardFragment}
 */
@Getter
public class DashboardFragment extends Composite<DashboardFragment> {

    @TestComponent(path = "dashboardFragmentWelcomeMessage")
    private Unknown welcomeMessage;

    @TestComponent(path = "dashboardFragmentNoEnginesContainer")
    private Unknown noEnginesContainer;

    @TestComponent(path = "deployedProcessesCard")
    private DeployedProcessesCard deployedProcessesCard;

    @TestComponent(path = "processInstancesCard")
    private ProcessInstancesCard processInstancesCard;

    @TestComponent(path = "userTasksCard")
    private UserTasksCard userTasksCard;

    @TestComponent(path = "recentActivityCard")
    private Unknown recentActivityCard;

    @TestComponent(path = {"runningInstancesAndIncidentsCard", "runningInstancesCard"})
    private RunningInstancesCard runningInstancesCard;

    @TestComponent(path = {"runningInstancesAndIncidentsCard", "incidentsCard"})
    private RunningInstancesCard incidentsCard;

    @TestComponent(path = "dashboardFragmentRefreshBtn")
    private Button refreshBtn;

    @TestComponent(path = "dashboardFragmentCreateBpmEngineBtn")
    private Button createBpmEngineBtn;

    @TestComponent(path = "dashboardFragmentSetupFirstConnectionText")
    private Unknown setupFirstConnectionText;
}
