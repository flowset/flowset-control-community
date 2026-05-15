/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.usertask;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.sys.DialogWindow;
import lombok.Getter;

/**
 * Wrapper for the Bulk complete user tasks confirmation dialog opened from the User tasks list view.
 * Source view: {@link io.flowset.control.view.bulktaskcomplete.BulkTaskCompleteView}
 */
@Getter
@TestView(id = "BulkTaskCompleteView")
public class BulkTaskCompleteDialog extends DialogWindow<BulkTaskCompleteDialog> {

    @TestComponent(path = "completeTaskBtn")
    private Button completeBtn;

    @TestComponent(path = "cancelBtn")
    private Button cancelBtn;
}
