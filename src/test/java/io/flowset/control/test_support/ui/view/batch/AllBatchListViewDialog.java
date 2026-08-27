/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.batch;

import io.jmix.masquerade.TestView;
import io.jmix.masquerade.sys.DialogWindow;
import lombok.Getter;

/**
 * Wrapper for the Batch list view opened in the dialog mode.
 * Source view: {@link io.flowset.control.view.batch.AllBatchListView}
 */
@Getter
@TestView(id = "bpm_AllBatchListView")
public class AllBatchListViewDialog extends DialogWindow<AllBatchListViewDialog> {

}
