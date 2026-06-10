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
 * Wrapper for the deployed processes card on the dashboard.
 * Source component: {@link io.flowset.control.view.dashboard.DeployedProcessesStatisticsCardFragment}
 */
@Getter
public class DeployedProcessesCard extends Composite<DeployedProcessesCard> {
    @TestComponent(path = "deployProcessesCount")
    private Unknown deployProcessesCount;

    @TestComponent(path = "viewDefinitionsBtn")
    private Button viewDefinitionsBtn;
}
