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
 * Wrapper for the Process instances card on the dashboard.
 * Source component: {@link io.flowset.control.view.dashboard.ProcessInstanceStatisticsCardFragment}
 */
@Getter
public class ProcessInstancesCard extends Composite<ProcessInstancesCard> {
    @TestComponent(path = "runningProcessInstances")
    private Unknown runningProcessInstances;

    @TestComponent(path = "suspendedProcessInstances")
    private Unknown suspendedProcessInstances;

    @TestComponent(path = "viewInstancesBtn")
    private Button viewInstancesBtn;
}
