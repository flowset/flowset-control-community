package io.flowset.control.view.job;


import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import io.flowset.control.service.job.JobService;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "activate-job", layout = DefaultMainViewParent.class)
@ViewController(id = "ActivateJobView")
@ViewDescriptor(path = "activate-job-view.xml")
public class ActivateJobView extends StandardView {

    protected String jobId;
    @Autowired
    protected JobService jobService;
    @Autowired
    protected Notifications notifications;
    @ViewComponent
    protected MessageBundle messageBundle;

    public ActivateJobView() {
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    @Subscribe("activateAction")
    public void onActivateAction(final ActionPerformedEvent event) {
        jobService.activateJob(jobId);
        close(StandardOutcome.SAVE);

        notifications.create(messageBundle.getMessage("jobActivated.text"))
                .withType(Notifications.Type.SUCCESS)
                .withPosition(Notification.Position.BOTTOM_END)
                .show();
    }
}