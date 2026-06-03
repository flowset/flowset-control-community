/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import io.flowset.control.security.SecuritySupport;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

/**
 * Base class for actions that open a detail view for the entity with the configured identifier.
 */
public abstract class ViewEntityDetailAction extends SecuredBaseAction {

    protected ViewNavigators viewNavigators;
    protected SecuritySupport securitySupport;
    protected final Class<?> entityClass;

    protected String entityId;

    protected boolean visibleByActionUiPermission;

    private ViewRegistry viewRegistry;

    protected ViewEntityDetailAction(String id, Class<?> entityClass) {
        super(id);
        this.entityClass = entityClass;
    }

    @Autowired
    public void setViewNavigators(ViewNavigators viewNavigators) {
        this.viewNavigators = viewNavigators;
    }

    @Autowired
    public void setViewRegistry(ViewRegistry viewRegistry) {
        this.viewRegistry = viewRegistry;
    }

    @Autowired
    public void setSecuritySupport(SecuritySupport securitySupport) {
        this.securitySupport = securitySupport;

        visibleByActionUiPermission = securitySupport.isEntityViewPermitted(entityClass);
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
        refreshState();
    }

    @Override
    public void refreshState() {
        super.refreshState();

        setVisibleInternal(visibleExplicitly && visibleByActionUiPermission && isVisibleByState());
    }

    public String getEntityId() {
        return entityId;
    }

    @Override
    public void actionPerform(Component component) {
        if (StringUtils.isBlank(entityId)) {
            return;
        }

        RouteParameters routeParameters = new RouteParameters(StandardDetailView.DEFAULT_ROUTE_PARAM, entityId);

        boolean openedInDialog = UiComponentUtils.isComponentAttachedToDialog(component);
        if (!openedInDialog) {
            viewNavigators.detailView(getCurrentView(), entityClass)
                    .withRouteParameters(routeParameters)
                    .withBackwardNavigation(false)
                    .navigate();
        } else {
            ViewInfo detailViewInfo = viewRegistry.getDetailViewInfo(entityClass);
            RouterLink routerLink = new RouterLink(detailViewInfo.getControllerClass(), routeParameters);
            getCurrentView().getUI().ifPresent(ui -> ui.getPage().open(routerLink.getHref()));
        }
    }

    protected boolean isVisibleByState() {
        return StringUtils.isNotBlank(entityId);
    }
}
