/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.usertaskdata.column;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.flowset.control.entity.UserTaskData;
import io.flowset.control.view.entitydetaillink.EntityDetailLinkFragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@FragmentDescriptor("user-task-id-column-fragment.xml")
@RendererItemContainer("userTaskDc")
public class UserTaskIdColumnFragment extends EntityDetailLinkFragment<HorizontalLayout, UserTaskData> {
    @ViewComponent
    protected JmixButton idBtn;
    protected Runnable afterSaveCloseListener;

    public void setAfterSaveCloseListener(Runnable afterSaveCloseListener) {
        this.afterSaveCloseListener = afterSaveCloseListener;
    }

    @Override
    public void setItem(UserTaskData item) {
        super.setItem(item);

        idBtn.setText(item.getTaskId());
    }

    @Subscribe(id = "idBtn", subject = "clickListener")
    public void onIdBtnClick(final ClickEvent<JmixButton> event) {
        dialogWindows.detail(getCurrentView(), UserTaskData.class)
                .withAfterCloseListener(viewAfterCloseEvent -> {
                    if (viewAfterCloseEvent.closedWith(StandardOutcome.SAVE) && afterSaveCloseListener != null) {
                        afterSaveCloseListener.run();
                    }
                })
                .editEntity(item)
                .open();
    }
}