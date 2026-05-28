/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.engine;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.security.accesscontext.engine.EngineMarkAsDefaultAccessContext;
import io.flowset.control.service.engine.EngineService;
import io.jmix.core.Messages;
import io.jmix.core.AccessManager;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.kit.action.ActionVariant;
import org.springframework.beans.factory.annotation.Autowired;

@ActionType(MarkAsDefaultEngineAction.ID)
public class MarkAsDefaultEngineAction extends SecuredBaseAction implements ExecutableAction {

    public static final String ID = "control_markAsDefaultEngine";

    protected boolean visibleByActionUiPermission;
    protected Dialogs dialogs;
    protected Messages messages;
    protected EngineService engineService;

    protected BpmEngine engine;
    protected Runnable afterSaveHandler;

    public MarkAsDefaultEngineAction() {
        super(ID);
    }

    public MarkAsDefaultEngineAction(String id) {
        super(id);
    }

    @Autowired
    protected void setAccessManager(AccessManager accessManager) {
        EngineMarkAsDefaultAccessContext context = new EngineMarkAsDefaultAccessContext();
        accessManager.applyRegisteredConstraints(context);
        visibleByActionUiPermission = context.isPermitted();
    }

    @Autowired
    public void setDialogs(Dialogs dialogs) {
        this.dialogs = dialogs;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
        this.description = messages.getMessage("actions.MarkAsDefault");
    }

    @Autowired
    public void setEngineService(EngineService engineService) {
        this.engineService = engineService;
    }

    public void setEngine(BpmEngine engine) {
        this.engine = engine;

        refreshState();
    }

    public void setAfterSaveHandler(Runnable afterSaveHandler) {
        this.afterSaveHandler = afterSaveHandler;
    }

    @Override
    public void refreshState() {
        super.refreshState();

        setVisibleInternal(visibleExplicitly && visibleByActionUiPermission);
    }

    @Override
    public void execute() {
        dialogs.createOptionDialog()
                .withHeader(messages.getMessage("io.flowset.control.view.bpmengine/markAsDefault.header"))
                .withContent(new Html(messages.formatMessage("io.flowset.control.view.bpmengine", "markAsDefault.text", engine.getName())))
                .withActions(
                        new DialogAction(DialogAction.Type.OK)
                                .withVariant(ActionVariant.PRIMARY)
                                .withHandler(event -> {
                                    engineService.markAsDefault(engine);
                                    if (afterSaveHandler != null) {
                                        afterSaveHandler.run();
                                    }
                                }),
                        new DialogAction(DialogAction.Type.CANCEL))
                .open();
    }

    @Override
    public void actionPerform(Component component) {
        execute();
    }
}
