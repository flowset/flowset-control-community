/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.job;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.sys.DialogWindow;
import lombok.Getter;

/**
 * Wrapper for the Activate job confirmation dialog.
 * Source view: {@link io.flowset.control.view.job.ActivateJobView}
 */
@Getter
@TestView(id = "ActivateJobView")
public class ActivateJobDialog extends DialogWindow<ActivateJobDialog> {

    @TestComponent(path = "suspendBtn")
    private Button activateBtn;

    @TestComponent(path = "cancelBtn")
    private Button cancelBtn;
}
