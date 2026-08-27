/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.batch.notification;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.flowset.control.action.batch.ViewBatchAction;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.view.ViewComponent;

@FragmentDescriptor("batch-notification-content-fragment.xml")
public class BatchNotificationContentFragment extends Fragment<VerticalLayout> {
    public static final int DEFAULT_DURATION = 6000;

    @ViewComponent
    protected Span titleText;
    @ViewComponent
    protected ViewBatchAction openBatchAction;
    @ViewComponent
    private HorizontalLayout batchMessageBox;

    public void setTitle(String title) {
        titleText.setText(title);
    }

    public void setBatchId(String batchId) {
        openBatchAction.setBatchId(batchId);
        batchMessageBox.setVisible(openBatchAction.isVisible());
    }
}
