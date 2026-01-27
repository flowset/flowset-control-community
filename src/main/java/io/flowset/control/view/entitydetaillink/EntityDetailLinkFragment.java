/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.entitydetaillink;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
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
import io.jmix.flowui.view.OpenMode;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;
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

    public void setOpenMode(OpenMode openMode) {
        this.openMode = openMode;
    }

    public void setLinkButtonId(String linkButtonId) {
        this.linkButtonId = linkButtonId;
    }

    @Override
    public void setItem(V item) {
        super.setItem(item);

        JmixButton linkBtn = findLinkButton();
        if (linkBtn != null) {
            boolean isPermitted = isActionPermitted(item);
            linkBtn.setEnabled(isPermitted);
        }
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

    protected boolean isActionPermitted(V item) {
        MetaClass metaClass = metadata.getClass(item);
        InMemoryCrudEntityContext context = new InMemoryCrudEntityContext(metaClass, applicationContext);
        accessManager.applyRegisteredConstraints(context);

        return context.isReadPermitted(item) || context.isUpdatePermitted(item);
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
        dialogWindows.detail(getCurrentView(), entityClass)
                .editEntity(item)
                .open();
    }

    protected void openDetailViewInNewTab(Class<V> entityClass) {
        ViewInfo detailViewInfo = viewRegistry.getDetailViewInfo(entityClass);
        RouterLink routerLink = new RouterLink(detailViewInfo.getControllerClass(), new RouteParameters("id", getEntityId()));
        getUI().ifPresent(ui -> ui.getPage().open(routerLink.getHref()));
    }
}