/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.engine;

import io.jmix.masquerade.TestView;
import io.jmix.masquerade.sys.DialogWindow;

/**
 * Wrapper for the BPM engine detail view opened in dialog mode.
 * Source view: {@link io.flowset.control.view.bpmengine.BpmEngineDetailView}
 */
@TestView(id = "BpmEngine.detail")
public class BpmEngineDetailDialog extends DialogWindow<BpmEngineDetailDialog> {
}
