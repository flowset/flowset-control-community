/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.facet.urlqueryparameters;

import io.flowset.control.view.AbstractListViewWithDelayedLoad;
import io.jmix.flowui.component.PaginationComponent;
import io.jmix.flowui.facet.urlqueryparameters.PaginationUrlQueryParametersBinder;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import org.apache.commons.lang3.BooleanUtils;

import java.util.function.Supplier;

/**
 * A pagination binder that does not update the URL query parameters while
 * an {@link AbstractListViewWithDelayedLoad} loads the data by itself. Such a load is performed
 * in a separate server request, and the pagination parameters would overwrite the parameters
 * of the applied filter that are still being pushed to the browser.
 */
public class ControlPaginationUrlQueryParametersBinder extends PaginationUrlQueryParametersBinder {

    protected Supplier<Boolean> viewInitiatedLoadSupplier;

    public ControlPaginationUrlQueryParametersBinder(PaginationComponent<?> pagination,
                                                     UrlParamSerializer urlParamSerializer,
                                                     Supplier<Boolean> viewInitiatedLoadSupplier) {
        super(pagination, urlParamSerializer);

        this.viewInitiatedLoadSupplier = viewInitiatedLoadSupplier;
    }

    @Override
    protected void onAfterRefresh(PaginationComponent.AfterRefreshEvent<?> event) {
        if (BooleanUtils.isTrue(viewInitiatedLoadSupplier.get())) {
            return;
        }

        super.onAfterRefresh(event);
    }
}
