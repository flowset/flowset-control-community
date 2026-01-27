/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.facet.urlqueryparameters;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.QueryParameters;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.facet.UrlQueryParametersFacet;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A base class for implementing a binding the URL query parameters for the filter which values showing in the components.
 */
public abstract class AbstractFilterUrlQueryParamBinder extends ControlAbstractUrlQueryParametersBinder {

    protected AbstractFilterUrlQueryParamBinder() {
        super();
    }

    /**
     * Resets query parameters related to the filter.
     */
    public void resetParameters() {
        Map<String, List<String>> params = getEmptyParametersMap();

        fireQueryParametersChanged(new UrlQueryParametersFacet.UrlQueryParametersChangeEvent(this, new QueryParameters(params)));
    }

    /**
     * @return a map with query parameters and values which should be set when reset a filter.
     */
    protected abstract Map<String, List<String>> getEmptyParametersMap();

    /**
     * Adds a value change listener for the specified component that updates the specified query parameter if the value changed by user.
     *
     * @param component           a component in which the user can change the value
     * @param paramName           an URL query parameter name
     * @param paramValueConverter a function that converts component value to URL query parameter value
     */
    protected void addComponentValueChangeListener(Component component, String paramName, Function<Object, String> paramValueConverter) {
        if (component instanceof SupportsTypedValue<?, ?, ?, ?> typedField) {
            typedField.addTypedValueChangeListener(event -> {
                Object value = event.getValue();
                updateQueryParamIfRequired(event.isFromClient(), paramName, paramValueConverter, value);
            });
        } else if (component instanceof AbstractField<?, ?> abstractField) {
            abstractField.addValueChangeListener(event -> {
                Object value = event.getValue();
                updateQueryParamIfRequired(event.isFromClient(), paramName, paramValueConverter, value);
            });
        }
    }

    protected void updateQueryParamIfRequired(boolean isFromClient, String paramName,
                                              Function<Object, String> valueToParamValue, @Nullable Object value) {
        if (isFromClient) {
            updateQueryParam(paramName, value != null ? valueToParamValue.apply(value) : null);
        }
    }
}
