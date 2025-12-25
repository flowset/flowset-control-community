package io.flowset.control.view.job;


import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import io.flowset.control.service.job.JobService;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "suspend-job", layout = DefaultMainViewParent.class)
@ViewController(id = "SuspendJobView")
@ViewDescriptor(path = "suspend-job-view.xml")
public class SuspendJobView extends StandardView {

    protected String jobId;
    @Autowired
    protected JobService jobService;
    @Autowired
    protected Notifications notifications;
    @ViewComponent
    protected MessageBundle messageBundle;

    public SuspendJobView() {
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    @Subscribe("suspendAction")
    public void onSuspendAction(final ActionPerformedEvent event) {
        jobService.suspendJob(jobId);
        close(StandardOutcome.SAVE);

        notifications.create(messageBundle.getMessage("jobSuspended.text"))
                .withType(Notifications.Type.SUCCESS)
                .withPosition(Notification.Position.BOTTOM_END)
                .show();
    }
}