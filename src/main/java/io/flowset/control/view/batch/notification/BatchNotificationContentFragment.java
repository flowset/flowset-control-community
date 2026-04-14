/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.batch.notification;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import feign.FeignException;
import io.flowset.control.entity.batch.BatchData;
import io.flowset.control.service.batch.BatchService;
import io.flowset.control.view.batch.AllBatchListView;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import org.springframework.beans.factory.annotation.Autowired;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@FragmentDescriptor("batch-notification-content-fragment.xml")
public class BatchNotificationContentFragment extends Fragment<VerticalLayout> {
    public static final int DEFAULT_DURATION = 6000;

    @Autowired
    protected BatchService batchService;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected DialogWindows dialogWindows;
    @ViewComponent
    protected MessageBundle messageBundle;

    @ViewComponent
    protected Span titleText;
    @ViewComponent
    protected JmixButton openBatchBtn;

    protected String title;
    protected String batchId;

    public void setTitle(String title) {
        this.title = title;
        titleText.setText(title);
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    @Subscribe(id = "openBatchBtn", subject = "clickListener")
    public void onOpenBatchBtnClick(final ClickEvent<JmixButton> event) {
        if (batchId == null) {
            dialogWindows.view(getCurrentView(), AllBatchListView.class)
                    .open();
        } else {
            try {
                BatchData batchData = batchService.getById(batchId);
                if (batchData == null) {
                    showBatchNotFoundNotification();
                    return;
                }
                dialogWindows.detail(getCurrentView(), BatchData.class)
                        .editEntity(batchData)
                        .open();
            } catch (FeignException e) {
                if (e.status() == 404) {
                    showBatchNotFoundNotification();
                    return;
                }

                throw e;
            }
        }

    }

    protected void showBatchNotFoundNotification() {
        notifications.create(messageBundle.getMessage("batchNotFoundWarning"))
                .withType(Notifications.Type.WARNING)
                .show();
    }
}
