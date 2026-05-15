/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processdefinition;

import io.jmix.masquerade.TestView;
import io.jmix.masquerade.sys.View;
import lombok.Getter;

/**
 * Wrapper for the process deployment view.
 * Source component: {@link io.flowset.control.view.newprocessdeployment.NewProcessDeploymentView}
 */
@Getter
@TestView(id = "bpm_NewProcessDeploymentView")
public class NewProcessDeploymentView extends View<NewProcessDeploymentView> {
}
