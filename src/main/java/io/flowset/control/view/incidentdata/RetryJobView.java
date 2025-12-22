package io.flowset.control.view.incidentdata;


import com.vaadin.flow.router.Route;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.*;
import io.flowset.control.service.job.JobService;
import io.flowset.control.view.main.MainView;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "retry-job-view", layout = MainView.class)
@ViewController(id = "RetryJobView")
@ViewDescriptor(path = "retry-job-view.xml")
public class RetryJobView extends StandardView {

    @Autowired
    protected ViewValidation viewValidation;
    @ViewComponent
    protected JmixFormLayout form;
    @ViewComponent
    protected TypedTextField<Integer> retriesField;
    @Autowired
    protected JobService jobService;
    @Autowired
    protected Notifications notifications;

    @ViewComponent
    protected MessageBundle messageBundle;

    protected String jobId;

    public void setJobId(String jobId) {
        this.jobId = jobId;
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
        jobService.setJobRetries(jobId, retries);

        notifications.create(messageBundle.getMessage("jobRetriesUpdated"))
                .withType(Notifications.Type.SUCCESS)
                .show();

        close(StandardOutcome.SAVE);
    }

    @Subscribe("cancelAction")
    public void onCancelAction(final ActionPerformedEvent event) {
        close(StandardOutcome.CLOSE);
    }
}