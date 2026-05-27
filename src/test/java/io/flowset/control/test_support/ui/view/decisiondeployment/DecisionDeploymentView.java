/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.decisiondeployment;

import io.jmix.masquerade.TestView;
import io.jmix.masquerade.sys.View;
import lombok.Getter;

/**
 * Test wrapper for the Decision Deployment view.
 * Source view: {@link io.flowset.control.view.decisiondeployment.DecisionDeploymentView}
 */
@Getter
@TestView(id = "bpm_DecisionDeploymentView")
public class DecisionDeploymentView extends View<DecisionDeploymentView> {
}
