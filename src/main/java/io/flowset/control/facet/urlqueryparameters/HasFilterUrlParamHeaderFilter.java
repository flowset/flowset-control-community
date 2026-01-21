package io.flowset.control.facet.urlqueryparameters;

import com.vaadin.flow.router.QueryParameters;

import java.util.Map;

/**
 * Interface representing a filter component for a data grid that integrates with URL query parameters.
 * This enables synchronized updates between the filter UI components in the header of a data grid
 * and the query parameters in the URL. Implementations of this interface provide functionality to
 * extract filter values into query parameters and to update filter components based on query parameters.
 */
public interface HasFilterUrlParamHeaderFilter {

    /**
     * Updates data grid header components with the specified URL query parameters.
     *
     * @param queryParameters an URL query parameters
     */
    void updateComponents(QueryParameters queryParameters);

    /**
     * Gets current values of the filter converted to the query parameters values.
     *
     * @return a map that contains filter values where key - query parameter name, value - filter value as a serialized string.
     */
    Map<String, String> getQueryParamValues();
}
