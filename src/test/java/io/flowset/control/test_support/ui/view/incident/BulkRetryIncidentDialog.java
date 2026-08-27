/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.incident;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.TextField;
import io.jmix.masquerade.sys.DialogWindow;
import lombok.Getter;

/**
 * Wrapper for the bulk retry incident dialog.
 * Source view: {@link io.flowset.control.view.incidentdata.BulkRetryIncidentView}
 */
@Getter
@TestView(id = "BulkRetryIncidentView")
public class BulkRetryIncidentDialog extends DialogWindow<BulkRetryIncidentDialog> {

    @TestComponent(path = "retryBtn")
    private Button retryBtn;

    @TestComponent(path = "cancelBtn")
    private Button cancelBtn;

    @TestComponent(path = "retriesField")
    private TextField retriesField;
}
