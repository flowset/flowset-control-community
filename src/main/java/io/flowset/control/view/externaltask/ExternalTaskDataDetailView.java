/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.externaltask;

import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.action.CopyComponentValueToClipboardAction;
import io.flowset.control.action.externaltask.RetryExternalTaskAction;
import io.flowset.control.action.ViewProcessDefinitionAction;
import io.flowset.control.action.ViewProcessInstanceAction;
import io.jmix.core.LoadContext;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import io.flowset.control.view.util.ComponentHelper;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.codeeditor.CodeEditor;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.ExternalTaskData;
import io.flowset.control.service.externaltask.ExternalTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.Objects;

@Route(value = "bpm/external-tasks/:id", layout = DefaultMainViewParent.class)
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
    protected RetryExternalTaskAction retryAction;
    @ViewComponent
    protected ViewProcessDefinitionAction viewProcessAction;
    @ViewComponent
    protected ViewProcessInstanceAction viewProcessInstanceAction;

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
        retryAction.setExternalTaskData(getEditedEntity());
        retryAction.setAfterSaveHandler(() -> close(StandardOutcome.SAVE));

        initProcessFields();

        boolean openedInDialog = UiComponentUtils.isComponentAttachedToDialog(this);
        detailActions.setJustifyContentMode(openedInDialog ? FlexComponent.JustifyContentMode.END : FlexComponent.JustifyContentMode.START);
    }

    @Install(to = "externalTaskDataDl", target = Target.DATA_LOADER)
    protected ExternalTaskData externalTaskDataDlLoadDelegate(final LoadContext<ExternalTaskData> loadContext) {
        return externalTaskService.findById(Objects.requireNonNull(loadContext.getId()).toString());
    }

    protected void initProcessFields() {
        String processLabel = getProcessLabel(getEditedEntity());
        processDefinitionIdField.setTypedValue(processLabel != null ? processLabel : getEditedEntity().getProcessDefinitionId());

        viewProcessAction.setEntityId(getEditedEntity().getProcessDefinitionId());
        viewProcessInstanceAction.setEntityId(getEditedEntity().getProcessInstanceId());
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
