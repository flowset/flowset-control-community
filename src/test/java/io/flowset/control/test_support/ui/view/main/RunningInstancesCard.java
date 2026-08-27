/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.main;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.sys.Composite;
import lombok.Getter;

/**
 * Wrapper for the Running Instances card on Dashboard.
 * Source: part of {@link io.flowset.control.view.dashboard.RunningInstancesAndIncidentsFragment}
 */
@Getter
public class RunningInstancesCard extends Composite<RunningInstancesCard> {

    @TestComponent(path = "viewRunningInstancesStatBtn")
    private Button viewRunningInstancesStatBtn;

}
