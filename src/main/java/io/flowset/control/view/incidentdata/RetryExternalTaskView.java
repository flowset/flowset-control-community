package io.flowset.control.view.incidentdata;


import com.vaadin.flow.router.Route;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.*;
import io.flowset.control.service.externaltask.ExternalTaskService;
import io.flowset.control.view.main.MainView;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "retry-external-task-view", layout = MainView.class)
@ViewController(id = "RetryExternalTaskView")
@ViewDescriptor(path = "retry-external-task-view.xml")
public class RetryExternalTaskView extends StandardView {

    @ViewComponent
    protected TypedTextField<Integer> retriesField;
    @Autowired
    protected ViewValidation viewValidation;
    @ViewComponent
    protected JmixFormLayout form;
    @Autowired
    private Notifications notifications;
    @Autowired
    protected ExternalTaskService externalTaskService;

    @ViewComponent
    private MessageBundle messageBundle;

    protected String externalTaskId;

    public void setExternalTaskId(String externalTaskId) {
        this.externalTaskId = externalTaskId;
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        retriesField.setTypedValue(1);
    }

    @Subscribe("retryAction")
    public void onRetryAction(final ActionPerformedEvent event) {
        ValidationErrors validationErrors = viewValidation.validateUiComponents(form);
        if (!validationErrors.isEmpty()) {
            viewValidation.showValidationErrors(validationErrors);
            return;
        }

        Integer retries = retriesField.getTypedValue();
        if (retries == null) {
            return;
        }
        externalTaskService.setRetries(externalTaskId, retries);

        notifications.create(messageBundle.getMessage("externalTaskRetriesUpdated"))
                .withType(Notifications.Type.SUCCESS)
                .show();

        close(StandardOutcome.SAVE);
    }

    @Subscribe("cancelAction")
    public void onCancelAction(final ActionPerformedEvent event) {
        close(StandardOutcome.CLOSE);
    }
}