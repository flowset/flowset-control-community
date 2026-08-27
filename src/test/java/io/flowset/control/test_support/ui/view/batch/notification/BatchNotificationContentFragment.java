/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.batch.notification;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.Unknown;
import io.jmix.masquerade.sys.Composite;
import lombok.Getter;

/**
 * Test wrapper for the notification content shown after a bulk batch action.
 * Source component: {@link io.flowset.control.view.batch.notification.BatchNotificationContentFragment}
 */
@Getter
public class BatchNotificationContentFragment extends Composite<BatchNotificationContentFragment> {

    @TestComponent(path = "titleText")
    private Unknown titleText;

    @TestComponent(path = "batchMessageBox")
    private Unknown batchDescription;

    @TestComponent(path = "openBatchBtn")
    private Button openBatchBtn;
}
