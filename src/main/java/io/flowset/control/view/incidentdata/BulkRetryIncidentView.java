package io.flowset.control.view.incidentdata;


import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import io.flowset.control.entity.batch.BatchData;
import io.flowset.control.view.batch.notification.BatchNotificationContentFragment;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.incident.IncidentData;
import io.flowset.control.service.externaltask.ExternalTaskService;
import io.flowset.control.service.job.JobService;
import org.apache.commons.collections4.CollectionUtils;
import org.camunda.bpm.engine.runtime.Incident;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Set;

@Route(value = "bulk-retry-incident-view", layout = DefaultMainViewParent.class)
@ViewController(id = "BulkRetryIncidentView")
@ViewDescriptor(path = "bulk-retry-incident-view.xml")
public class BulkRetryIncidentView extends StandardView {

    @Autowired
    protected ViewValidation viewValidation;
    @ViewComponent
    protected JmixFormLayout form;
    @ViewComponent
    protected TypedTextField<Integer> retriesField;
    @Autowired
    protected ExternalTaskService externalTaskService;
    @Autowired
    protected JobService jobService;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected Fragments fragments;
    
    @ViewComponent
    protected MessageBundle messageBundle;

    protected Set<IncidentData> incidentDataSet;


    public void setIncidentDataSet(Set<IncidentData> incidentDataSet) {
        this.incidentDataSet = incidentDataSet;
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
        BatchData externalTaskBatch = null;
        List<String> externalTaskIds = getIncidentsByType(incidentDataSet, Incident.EXTERNAL_TASK_HANDLER_TYPE);
        if (CollectionUtils.isNotEmpty(externalTaskIds)) {
            externalTaskBatch = externalTaskService.setRetriesAsync(externalTaskIds, retries);
        }


        List<String> jobIds = getIncidentsByType(incidentDataSet, Incident.FAILED_JOB_HANDLER_TYPE);
        BatchData jobBatch = null;
        if (CollectionUtils.isNotEmpty(jobIds)) {
            jobBatch = jobService.setJobRetriesAsync(jobIds, retries);
        }

        showBatchNotification(externalTaskBatch, jobBatch);

        close(StandardOutcome.SAVE);
    }

    @Subscribe("cancelAction")
    public void onCancelAction(final ActionPerformedEvent event) {
        close(StandardOutcome.CLOSE);
    }

    protected List<String> getIncidentsByType(Set<IncidentData> selectedItems, String incidentType) {
        return selectedItems.stream()
                .filter(incidentData -> incidentData.getType().equals(incidentType) && incidentData.getConfiguration() != null)
                .map(IncidentData::getConfiguration)
                .toList();
    }

    protected void showBatchNotification(@Nullable BatchData externalTaskBatch, @Nullable BatchData jobBatch) {
        BatchNotificationContentFragment batchNotificationContent = fragments.create(this, BatchNotificationContentFragment.class);
        String batchId = null;
        if (externalTaskBatch != null && jobBatch == null) {
            batchId = externalTaskBatch.getId();
        } else if (externalTaskBatch == null && jobBatch != null) {
            batchId = jobBatch.getId();
        }

        batchNotificationContent.setBatchId(batchId);
        batchNotificationContent.setTitle(messageBundle.getMessage("retriesBulkUpdateStarted"));

        Notification notification = notifications.create(batchNotificationContent.getContent())
                .withCloseable(true)
                .build();
        notification.setDuration(BatchNotificationContentFragment.DEFAULT_DURATION);
        notification.open();
    }
}
