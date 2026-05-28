/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.incident;

import com.vaadin.flow.component.Component;
import io.flowset.control.entity.incident.IncidentData;
import io.flowset.control.view.externaltask.ExternalTaskErrorDetailsView;
import io.flowset.control.view.job.JobErrorDetailsView;
import io.flowset.control.view.util.ComponentHelper;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.view.DialogWindow;
import org.springframework.beans.factory.annotation.Autowired;
import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(ViewIncidentStacktraceAction.ID)
public class ViewIncidentStacktraceAction extends SecuredBaseAction implements ExecutableAction {

    public static final String ID = "control_viewIncidentStacktrace";

    protected DialogWindows dialogWindows;
    protected ComponentHelper componentHelper;

    protected IncidentData incidentData;

    public ViewIncidentStacktraceAction() {
        super(ID);
    }

    public ViewIncidentStacktraceAction(String id) {
        super(id);
    }

    @Autowired
    public void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Autowired
    public void setComponentHelper(ComponentHelper componentHelper) {
        this.componentHelper = componentHelper;
    }

    public void setIncidentData(IncidentData incidentData) {
        this.incidentData = incidentData;
    }

    @Override
    public void execute() {
        if (incidentData.isJobFailed()) {
            DialogWindow<JobErrorDetailsView> dialogWindow = dialogWindows.view(getCurrentView(), JobErrorDetailsView.class)
                    .withViewConfigurer(view -> {
                        view.setJobId(incidentData.getConfiguration());
                        view.setErrorMessage(incidentData.getMessage());
                    })
                    .build();
            componentHelper.addFullScreenButton(dialogWindow);
            dialogWindow.open();
        } else if (incidentData.isExternalTaskFailed()) {
            DialogWindow<ExternalTaskErrorDetailsView> dialogWindow = dialogWindows.view(getCurrentView(),
                            ExternalTaskErrorDetailsView.class)
                    .withViewConfigurer(view -> {
                        view.setExternalTaskId(incidentData.getConfiguration());
                        view.setErrorMessage(incidentData.getMessage());
                    })
                    .build();
            componentHelper.addFullScreenButton(dialogWindow);
            dialogWindow.open();
        }
    }

    @Override
    public void actionPerform(Component component) {
        execute();
    }

    @Override
    public void refreshState() {
        super.refreshState();

        setVisibleInternal(visibleExplicitly && isVisibleByState());
    }

    protected boolean isVisibleByState() {
        return incidentData != null && (incidentData.isExternalTaskFailed() || incidentData.isJobFailed());
    }
}
