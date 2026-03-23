/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.bpmengine;


import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import io.flowset.control.service.engine.auth.EngineAuthStateService;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Route(value = "unlock-engine-access-token-retrieval", layout = DefaultMainViewParent.class)
@ViewController(id = "UnlockEngineAccessTokenRetrievalView")
@ViewDescriptor(path = "unlock-engine-access-token-retrieval-view.xml")
public class UnlockEngineAccessTokenRetrievalView extends StandardView {

    protected UUID engineId;
    @Autowired
    private EngineAuthStateService engineAuthStateService;
    @Autowired
    private Notifications notifications;
    @ViewComponent
    private MessageBundle messageBundle;

    public void setEngineId(UUID engineId) {
        this.engineId = engineId;
    }
    @Subscribe("unlock")
    public void onUnlock(final ActionPerformedEvent event) {
        engineAuthStateService.unlock(engineId);
        notifications.create(messageBundle.getMessage("accessTokenRequestsEnabled"))
                .withType(Notifications.Type.SUCCESS)
                .withPosition(Notification.Position.BOTTOM_END)
                .show();
        close(StandardOutcome.SAVE);
    }
}