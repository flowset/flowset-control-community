package io.flowset.control.facet.urlqueryparameters;

import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.router.QueryParameters;
import io.jmix.flowui.facet.UrlQueryParametersFacet;
import io.jmix.flowui.facet.urlqueryparameters.AbstractUrlQueryParametersBinder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for implementing {@link AbstractFilterUrlQueryParamBinder} in Control.
 * Contains helpful methods for updating single query parameter or use a map with parameter values.
 */
public abstract class ControlAbstractUrlQueryParametersBinder extends AbstractUrlQueryParametersBinder {

    protected void updateQueryParam(String paramName, @Nullable String value) {
        QueryParameters qp = new QueryParameters(ImmutableMap.of(paramName,
                createSingleQueryParamValue(value)));
        fireQueryParametersChanged(new UrlQueryParametersFacet.UrlQueryParametersChangeEvent(this, qp));
    }

    protected void updateQueryParams(Map<String, String> params) {
        Map<String, List<String>> resParams = new HashMap<>();
        params.forEach((paramName, value) -> {
            List<String> paramValue = createSingleQueryParamValue(value);
            resParams.put(paramName, paramValue);
        });
        QueryParameters qp = new QueryParameters(resParams);
        fireQueryParametersChanged(new UrlQueryParametersFacet.UrlQueryParametersChangeEvent(this, qp));
    }

    protected List<String> createSingleQueryParamValue(@Nullable String value) {
        return StringUtils.isNotEmpty(value) ? Collections.singletonList(value) : Collections.emptyList();
    }
}
