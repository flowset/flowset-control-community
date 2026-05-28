/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.incident;

import io.flowset.control.entity.incident.IncidentData;
import io.flowset.control.security.accesscontext.incident.IncidentRetryAccessContext;
import io.flowset.control.view.incidentdata.BulkRetryIncidentView;
import io.jmix.core.Messages;
import io.jmix.core.AccessManager;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.list.ListDataComponentAction;
import io.jmix.flowui.view.StandardOutcome;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(BulkRetryIncidentAction.ID)
public class BulkRetryIncidentAction extends ListDataComponentAction<BulkRetryIncidentAction, IncidentData> {

    public static final String ID = "control_bulkRetryIncident";

    protected DialogWindows dialogWindows;
    protected Runnable afterSaveHandler;

    protected boolean visibleByActionUiPermission;

    public BulkRetryIncidentAction() {
        super(ID);
    }

    public BulkRetryIncidentAction(String id) {
        super(id);
    }

    @Autowired
    protected void setAccessManager(AccessManager accessManager) {
        IncidentRetryAccessContext context = new IncidentRetryAccessContext();
        accessManager.applyRegisteredConstraints(context);

        visibleByActionUiPermission = context.isPermitted();
    }

    @Autowired
    public void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.Retry");
    }

    public void setAfterSaveHandler(Runnable afterSaveHandler) {
        this.afterSaveHandler = afterSaveHandler;
    }

    @Override
    public void execute() {
        checkTarget();
        Set<IncidentData> selectedItems = target.getSelectedItems();

        dialogWindows.view(getCurrentView(), BulkRetryIncidentView.class)
                .withViewConfigurer(bulkRetryIncidentView -> bulkRetryIncidentView.setIncidentDataSet(selectedItems))
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE) && afterSaveHandler != null) {
                        afterSaveHandler.run();
                    }
                })
                .open();
    }
    @Override
    public void refreshState() {
        super.refreshState();

        setVisibleInternal(visibleExplicitly && visibleByActionUiPermission);
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable() && !target.getSelectedItems().isEmpty();
    }
}
