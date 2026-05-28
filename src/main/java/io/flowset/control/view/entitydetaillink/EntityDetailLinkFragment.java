/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.entitydetaillink;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import io.flowset.control.security.SecuritySupport;
import io.flowset.control.view.util.ComponentHelper;
import io.jmix.core.AccessManager;
import io.jmix.core.Metadata;
import io.jmix.core.accesscontext.InMemoryCrudEntityContext;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.fragmentrenderer.FragmentRenderer;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;

import java.util.Objects;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

/**
 * Abstract class that represents a fragment renderer responsible for navigating to an entity detail view.
 * It extends {@link FragmentRenderer} and provides functionality for opening detail view in different mode and supports security permissions.
 * If the action is not permitted, the link button becomes disabled.
 *
 * @param <E> the component type to which the fragment is attached
 * @param <V> the entity type being handled by the fragment
 */
public abstract class EntityDetailLinkFragment<E extends Component, V> extends FragmentRenderer<E, V> {
    protected OpenMode openMode;
    protected boolean useDialogFullScreen;
    protected String linkButtonId;

    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected ViewNavigators viewNavigators;

    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected ViewRegistry viewRegistry;
    @Autowired
    protected ComponentHelper componentHelper;
    @Autowired
    protected SecuritySupport securitySupport;

    protected Runnable afterSaveHandler;

    public void setOpenMode(OpenMode openMode) {
        this.openMode = openMode;
    }

    public void setUseDialogFullScreen(boolean useDialogFullScreen) {
        this.useDialogFullScreen = useDialogFullScreen;
    }

    public void setLinkButtonId(String linkButtonId) {
        this.linkButtonId = linkButtonId;
    }

    public void setAfterSaveHandler(Runnable afterSaveHandler) {
        this.afterSaveHandler = afterSaveHandler;
    }

    @Override
    public void setItem(V item) {
        super.setItem(item);

        updateLinkButtonAvailability();
    }

    @Subscribe
    public void onAttachEvent(final AttachEvent event) {
        updateLinkButtonAvailability();
    }

    @Nullable
    protected JmixButton findLinkButton() {
        if (linkButtonId != null) {
            return (JmixButton) UiComponentUtils.findComponent(getContent(), linkButtonId).orElse(null);
        }
        return getContent().getChildren()
                .filter(component -> component instanceof JmixButton)
                .map(component -> (JmixButton) component)
                .findFirst()
                .orElse(null);
    }

    protected void updateLinkButtonAvailability() {
        JmixButton linkBtn = findLinkButton();
        if (linkBtn != null) {
            linkBtn.setEnabled(isActionPermitted());
        }
    }

    /**
     * Permits the link click only when the current user has UI view permission on the target entity
     * type AND, if a source item is bound, satisfies the row-level read constraints on it.
     * Mirrors the dual-context check used by Jmix's edit / read actions.
     */
    protected boolean isActionPermitted() {
        Class<?> targetClass = getTargetEntityClass();
        if (targetClass == null) {
            return false;
        }
        boolean entityViewPermitted = securitySupport.isEntityViewPermitted(targetClass);

        // Row-level check is meaningful only when the source row is itself the target instance.
        return entityViewPermitted && isDetailViewPermitted() && isItemReadPermitted();
    }

    protected boolean isDetailViewPermitted() {
        Class<?> targetEntityClass = getTargetEntityClass();
        ViewInfo detailViewInfo = viewRegistry.getDetailViewInfo(targetEntityClass);

        return securitySupport.isShowViewPermitted(detailViewInfo.getId());
    }

    protected boolean isItemReadPermitted() {
        MetaClass metaClass = metadata.getClass(item);
        InMemoryCrudEntityContext context = new InMemoryCrudEntityContext(metaClass, applicationContext);
        accessManager.applyRegisteredConstraints(context);

        return context.isReadPermitted(item);
    }

    /**
     * @return target entity class the link navigates to. Subclasses MUST override when the link's
     * destination differs from the displayed item type (which is the common case — a row of entity A
     * linking to entity B).
     */
    protected Class<?> getTargetEntityClass() {
        return item != null ? item.getClass() : null;
    }

    protected void openDetailView(Class<V> entityClass) {
        if (openMode == OpenMode.DIALOG) {
            openDialogDetailView(entityClass);
        } else if (UiComponentUtils.isComponentAttachedToDialog(this)) {
            openDetailViewInNewTab(entityClass);
        } else {
            navigateToDetailView(entityClass);
        }
    }

    protected void navigateToDetailView(Class<V> entityClass) {
        viewNavigators.detailView(getCurrentView(), entityClass)
                .withRouteParameters(new RouteParameters("id", getEntityId()))
                .navigate();
    }

    protected String getEntityId() {
        return Objects.requireNonNull(EntityValues.getId(item)).toString();
    }

    protected void openDialogDetailView(Class<V> entityClass) {
        DialogWindow<View<?>> dialog = dialogWindows.detail(getCurrentView(), entityClass)
                .editEntity(item)
                .withAfterCloseListener(viewAfterCloseEvent -> {
                    if (viewAfterCloseEvent.closedWith(StandardOutcome.SAVE)
                            && afterSaveHandler != null) {
                        afterSaveHandler.run();
                    }
                })
                .build();
        if (useDialogFullScreen) {
            componentHelper.addFullScreenButton(dialog);
        }
        dialog.open();
    }

    protected void openDetailViewInNewTab(Class<V> entityClass) {
        ViewInfo detailViewInfo = viewRegistry.getDetailViewInfo(entityClass);
        RouterLink routerLink = new RouterLink(detailViewInfo.getControllerClass(), new RouteParameters("id", getEntityId()));
        getUI().ifPresent(ui -> ui.getPage().open(routerLink.getHref()));
    }
}
