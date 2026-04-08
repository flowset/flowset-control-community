/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.externaltask;

import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.action.CopyComponentValueToClipboardAction;
import io.jmix.core.LoadContext;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import io.flowset.control.view.incidentdata.RetryExternalTaskView;
import io.flowset.control.view.processdefinition.ProcessDefinitionDetailView;
import io.flowset.control.view.processinstance.ProcessInstanceDetailView;
import io.flowset.control.view.util.ComponentHelper;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.codeeditor.CodeEditor;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.ExternalTaskData;
import io.flowset.control.service.externaltask.ExternalTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.Objects;

@Route(value = "external-tasks/:id", layout = DefaultMainViewParent.class)
@ViewController("ExternalTaskData.detail")
@ViewDescriptor("external-task-data-detail-view.xml")
@EditedEntityContainer("externalTaskDataDc")
@DialogMode(minWidth = "40em", width = "80%", maxWidth = "80em")
public class ExternalTaskDataDetailView extends StandardDetailView<ExternalTaskData> {

    @Autowired
    protected ExternalTaskService externalTaskService;
    @Autowired
    protected ProcessDefinitionService processDefinitionService;
    @Autowired
    protected ComponentHelper componentHelper;
    @Autowired
    protected ViewNavigators viewNavigators;
    @Autowired
    protected DialogWindows dialogWindows;

    @ViewComponent
    protected InstanceLoader<ExternalTaskData> externalTaskDataDl;
    @ViewComponent
    protected TypedTextField<String> externalTaskIdField;
    @ViewComponent
    protected TypedTextField<String> processDefinitionIdField;
    @ViewComponent
    protected JmixTextArea errorMessageField;
    @ViewComponent
    protected CodeEditor errorDetailsField;
    @ViewComponent
    protected JmixButton viewProcessBtn;
    @ViewComponent
    protected JmixButton viewProcessInstanceBtn;
    @ViewComponent
    protected CopyComponentValueToClipboardAction copyIdAction;
    @ViewComponent
    protected CopyComponentValueToClipboardAction copyErrorAction;
    @ViewComponent
    protected CopyComponentValueToClipboardAction copyErrorDetailsAction;
    @ViewComponent
    protected HorizontalLayout detailActions;
    @ViewComponent
    protected BaseAction retryAction;

    @Subscribe
    public void onInit(final InitEvent event) {
        addClassNames(LumoUtility.Padding.Top.XSMALL);
        initActions();
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        externalTaskDataDl.load();

        String errorDetails = externalTaskService.getErrorDetails(getEditedEntity().getExternalTaskId());
        errorDetailsField.setValue(errorDetails);

        if (getEditedEntity().getRetries() != null && getEditedEntity().getRetries() == 0) {
            retryAction.setVisible(true);
        }

        initProcessFields();

        boolean openedInDialog = UiComponentUtils.isComponentAttachedToDialog(this);
        detailActions.setJustifyContentMode(openedInDialog ? FlexComponent.JustifyContentMode.END : FlexComponent.JustifyContentMode.START);
    }

    @Subscribe("retryAction")
    public void onRetryAction(final ActionPerformedEvent event) {
        dialogWindows.view(this, RetryExternalTaskView.class)
                .withViewConfigurer(retryExternalTaskView -> retryExternalTaskView.setExternalTaskId(getEditedEntity().getExternalTaskId()))
                .withAfterCloseListener(afterCloseEvent -> {
                    if (afterCloseEvent.closedWith(StandardOutcome.SAVE)) {
                        close(StandardOutcome.SAVE);
                    }
                })
                .open();
    }

    @Subscribe("viewProcessAction")
    public void onViewProcess(final ActionPerformedEvent event) {
        openView(ProcessDefinitionDetailView.class, new RouteParameters("id", getEditedEntity().getProcessDefinitionId()));
    }

    @Subscribe("viewProcessInstanceAction")
    public void onViewProcessInstance(final ActionPerformedEvent event) {
        openView(ProcessInstanceDetailView.class, new RouteParameters("id", getEditedEntity().getProcessInstanceId()));
    }

    @Install(to = "externalTaskDataDl", target = Target.DATA_LOADER)
    protected ExternalTaskData externalTaskDataDlLoadDelegate(final LoadContext<ExternalTaskData> loadContext) {
        return externalTaskService.findById(Objects.requireNonNull(loadContext.getId()).toString());
    }

    protected void openView(Class<? extends StandardView> viewClass, RouteParameters routeParameters) {
        boolean isOpenedInDialog = UiComponentUtils.isComponentAttachedToDialog(this);
        if (!isOpenedInDialog) {
            viewNavigators.view(this, viewClass)
                    .withRouteParameters(routeParameters)
                    .withBackwardNavigation(false)
                    .navigate();
        } else {
            RouterLink routerLink = new RouterLink(viewClass, routeParameters);
            getUI().ifPresent(ui -> ui.getPage().open(routerLink.getHref()));
        }
    }

    protected void initProcessFields() {
        String processLabel = getProcessLabel(getEditedEntity());
        processDefinitionIdField.setTypedValue(processLabel != null ? processLabel : getEditedEntity().getProcessDefinitionId());

        viewProcessBtn.setVisible(getEditedEntity().getProcessDefinitionId() != null);
        viewProcessInstanceBtn.setVisible(getEditedEntity().getProcessInstanceId() != null);
    }

    @Nullable
    protected String getProcessLabel(ExternalTaskData externalTaskData) {
        if (externalTaskData.getProcessDefinitionId() == null) {
            return null;
        }
        ProcessDefinitionData processDefinitionData = processDefinitionService.getById(externalTaskData.getProcessDefinitionId());
        return componentHelper.getProcessLabel(processDefinitionData);
    }

    protected void initActions() {
        copyIdAction.setText("");
        copyIdAction.setTarget(externalTaskIdField);

        copyErrorAction.setText("");
        copyErrorAction.setTarget(errorMessageField);

        copyErrorDetailsAction.setText("");
        copyErrorDetailsAction.setTarget(errorDetailsField);
    }
}
