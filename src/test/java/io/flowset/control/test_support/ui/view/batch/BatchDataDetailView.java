/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.batch;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.sys.View;
import lombok.Getter;

/**
 * Wrapper for the Batch detail view opened by route.
 * Source view: {@link io.flowset.control.view.batch.BatchDataDetailView}
 */
@Getter
@TestView(id = "BatchData.detail")
public class BatchDataDetailView extends View<BatchDataDetailView> {

    @TestComponent(path = "statisticsBox")
    private BatchJobsFragment jobsFragment;
}
