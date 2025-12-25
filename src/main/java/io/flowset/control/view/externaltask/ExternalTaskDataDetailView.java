/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.externaltask;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.ExternalTaskData;
import io.flowset.control.service.externaltask.ExternalTaskService;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "external-tasks/:id", layout = DefaultMainViewParent.class)
@ViewController("ExternalTaskData.detail")
@ViewDescriptor("external-task-data-detail-view.xml")
@EditedEntityContainer("externalTaskDataDc")
@DialogMode(width = "60em", resizable = true)
public class ExternalTaskDataDetailView extends StandardDetailView<ExternalTaskData> {

    @Autowired
    protected ExternalTaskService externalTaskService;
    @ViewComponent
    protected TextArea errorDetailsField;
    @ViewComponent
    protected JmixButton retryBtn;
    @Autowired
    protected Dialogs dialogs;
    @ViewComponent
    protected MessageBundle messageBundle;
    @Autowired
    protected Messages messages;

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        String errorDetails = externalTaskService.getErrorDetails(getEditedEntity().getExternalTaskId());
        errorDetailsField.setValue(errorDetails);

        if (getEditedEntity().getRetries() != null && getEditedEntity().getRetries() == 0) {
            retryBtn.setVisible(true);
        }
    }

    @Subscribe("retryBtn")
    protected void onRestoreFailedJobBtnClick(ClickEvent<Button> event) {
        dialogs.createOptionDialog()
                .withHeader(messageBundle.getMessage("retryExternalTaskDialog.header"))
                .withText(messageBundle.getMessage("retryExternalTaskDialog.text"))
                .withActions(new DialogAction(DialogAction.Type.YES)
                                .withIcon(VaadinIcon.ROTATE_LEFT.create())
                                .withText(messages.getMessage("actions.Retry"))
                                .withVariant(ActionVariant.PRIMARY)
                                .withHandler(actionPerformedEvent -> {
                                    externalTaskService.setRetries(getEditedEntity().getExternalTaskId(), 1);
                                    close(StandardOutcome.SAVE);
                                }),
                        new DialogAction(DialogAction.Type.CANCEL))
                .open();
    }

}
