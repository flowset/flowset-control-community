/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.job;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.TextField;
import io.jmix.masquerade.sys.DialogWindow;
import lombok.Getter;

/**
 * Wrapper for the job retry view opened in the dialog mode.
 * Source view: {@link io.flowset.control.view.incidentdata.RetryJobView}
 */
@Getter
@TestView(id = "RetryJobView")
public class RetryJobDialog extends DialogWindow<RetryJobDialog> {

    @TestComponent(path = "retriesField")
    private TextField retriesField;

    @TestComponent(path = "retryBtn")
    private Button retryBtn;

    @TestComponent(path = "cancelBtn")
    private Button cancelBtn;
}
