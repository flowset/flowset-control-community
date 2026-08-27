/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.externaltask;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.sys.View;
import lombok.Getter;

/**
 * Wrapper for the External task detail view opened by route.
 * Source view: {@link io.flowset.control.view.externaltask.ExternalTaskDataDetailView}
 */
@Getter
@TestView(id = "ExternalTaskData.detail")
public class ExternalTaskDataDetailView extends View<ExternalTaskDataDetailView> {

    @TestComponent(path = "retryBtn")
    private Button retryBtn;
}
