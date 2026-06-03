/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.security;

import io.jmix.core.AccessManager;
import io.jmix.core.Metadata;
import io.jmix.core.accesscontext.SpecificOperationAccessContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.accesscontext.UiEntityContext;
import io.jmix.flowui.accesscontext.UiShowViewContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Class responsible for evaluating security permissions and access policies for entities and specific actions.
 * Provides methods to verify if certain operations on entities or specific actions are permitted based
 * on the applied security constraints.
 */
@Component("control_SecuritySupport")
@AllArgsConstructor
public class SecuritySupport {

    private final Metadata metadata;
    private final AccessManager accessManager;

    /**
     * Checks if the current user has permission to view entities of the specified class.
     *
     * @param entityClass the class of the entity to check permissions for.
     * @return true if the user has permission to view entities of the specified class, false otherwise.
     */
    public boolean isEntityViewPermitted(Class<?> entityClass) {
        UiEntityContext uiEntityContext = createUiEntityContext(entityClass);
        return uiEntityContext.isViewPermitted();
    }

    /**
     * Checks if the current user has permission to view the specified view.
     *
     * @param viewId the id of the view to check permissions for.
     * @return true if the user has permission to view the specified view, false otherwise.
     */
    public boolean isShowViewPermitted(String viewId) {
        UiShowViewContext uiShowViewContext = new UiShowViewContext(viewId);
        accessManager.applyRegisteredConstraints(uiShowViewContext);
        return uiShowViewContext.isPermitted();
    }

    /**
     * Checks if the current user has permission to create entities of the specified class.
     *
     * @param entityClass the class of the entity to check permissions for.
     * @return true if the user has permission to create entities of the specified class, false otherwise.
     */
    public boolean isEntityCreatePermitted(Class<?> entityClass) {
        UiEntityContext uiEntityContext = createUiEntityContext(entityClass);
        return uiEntityContext.isCreatePermitted();
    }

    /**
     * Checks if the current user has permission to delete entities of the specified class.
     *
     * @param entityClass the class of the entity to check permissions for.
     * @return true if the user has permission to delete entities of the specified class, false otherwise.
     */
    public boolean isEntityDeletePermitted(Class<?> entityClass) {
        UiEntityContext uiEntityContext = createUiEntityContext(entityClass);
        return uiEntityContext.isDeletePermitted();
    }

    /**
     * Checks if the current user has permission to perform a specific action.
     *
     * @param accessContext the context of the specific operation to check permissions for.
     * @param <T>           the type of the access context.
     * @return true if the user has permission to perform the action, false otherwise.
     */
    public <T extends SpecificOperationAccessContext> boolean isActionPermitted(T accessContext) {
        accessManager.applyRegisteredConstraints(accessContext);

        return accessContext.isPermitted();
    }

    protected UiEntityContext createUiEntityContext(Class<?> entityClass) {
        MetaClass metaClass = metadata.getClass(entityClass);
        UiEntityContext uiEntityContext = new UiEntityContext(metaClass);

        accessManager.applyRegisteredConstraints(uiEntityContext);
        return uiEntityContext;
    }
}
