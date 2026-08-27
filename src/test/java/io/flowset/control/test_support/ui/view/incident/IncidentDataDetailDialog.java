/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.incident;

import io.jmix.masquerade.TestView;
import io.jmix.masquerade.sys.DialogWindow;
import lombok.Getter;

/**
 * Wrapper for the Incident detail view opened in dialog mode.
 * Source view: {@link io.flowset.control.view.incidentdata.IncidentDataDetailView}
 */
@Getter
@TestView(id = "IncidentData.detail")
public class IncidentDataDetailDialog extends DialogWindow<IncidentDataDetailDialog> {

}
