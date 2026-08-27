/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support;

import io.flowset.control.test_support.engine.HasRunningEngineData;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.testcontainers.EngineContainer;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Provides common methods to invoke REST API of {@link EngineContainer} or {@link ExternalEngine}.
 */
@Component("control_EngineTestContainerRestHelper")
public class EngineTestContainerRestHelper {
    private final RestClient restClient;

    public EngineTestContainerRestHelper() {
        this.restClient = RestClient.builder().build();
    }

    /**
     * Performs a GET request to load a list of objects with the specified type from the specified engine container at the specified endpoint.
     *
     * @param engineContainer an engine container from which the data should be loaded
     * @param endpoint        an endpoint after {@link EngineContainer#getRestBaseUrl()} by which the data should be loaded.
     * @param itemClass       a class of list item
     * @param <V>             a type of list item
     * @return found items
     */
    public <V> List<V> getList(HasRunningEngineData engineContainer, String endpoint, Class<V> itemClass) {
        String restBaseUrl = engineContainer.getRestBaseUrl();
        ResolvableType resolvableType = ResolvableType.forClassWithGenerics(List.class, itemClass);

        return restClient.get()
                .uri(restBaseUrl + endpoint)
                .headers(httpHeaders -> addAuthHeader(httpHeaders, engineContainer))
                .retrieve()
                .body(ParameterizedTypeReference.forType(resolvableType.getType()));
    }

    /**
     * Performs a GET request to load an item with the specified type from the specified engine container at the specified endpoint.
     *
     * @param engineContainer an engine container from which the data should be loaded
     * @param endpoint        an endpoint after {@link EngineContainer#getRestBaseUrl()} by which the data should be loaded.
     * @param responseType    a class of response type
     * @param <V>             a type of item
     * @return found item
     */
    public <V> V getOne(HasRunningEngineData engineContainer, String endpoint, Class<V> responseType) {
        String restBaseUrl = engineContainer.getRestBaseUrl();

        return restClient.get()
                .uri(restBaseUrl + endpoint)
                .headers(httpHeaders -> addAuthHeader(httpHeaders, engineContainer))
                .retrieve()
                .body(responseType);
    }

    /**
     * Performs a POST request to the specified engine container at the specified endpoint and returns an item with the specified type.
     *
     * @param engineContainer BPM engine container to which the request should be sent
     * @param endpoint        the endpoint after {@link EngineContainer#getRestBaseUrl()} to which the request should be sent
     * @param body            request body
     * @param responseType    a class of response
     * @param <V>             a response type
     * @return response with specified type
     */
    public <V> V postOne(HasRunningEngineData engineContainer, String endpoint, Object body, Class<V> responseType) {
        String restBaseUrl = engineContainer.getRestBaseUrl();

        return restClient.post()
                .uri(restBaseUrl + endpoint)
                .headers(httpHeaders -> addAuthHeader(httpHeaders, engineContainer))
                .body(body)
                .retrieve()
                .body(responseType);
    }

    /**
     * Performs a POST request to the specified engine container at the specified endpoint and returns a list of items with the specified type.
     *
     * @param engineContainer BPM engine container to which the request should be sent
     * @param endpoint        the endpoint after {@link EngineContainer#getRestBaseUrl()} to which the request should be sent
     * @param body            request body
     * @param itemClass       a class of list item
     * @param <V>             list item type
     * @return loaded list of items
     */
    public <V> List<V> postList(HasRunningEngineData engineContainer, String endpoint, Object body, Class<V> itemClass) {
        String restBaseUrl = engineContainer.getRestBaseUrl();
        ResolvableType resolvableType = ResolvableType.forClassWithGenerics(List.class, itemClass);

        return restClient.post()
                .uri(restBaseUrl + endpoint)
                .headers(httpHeaders -> addAuthHeader(httpHeaders, engineContainer))
                .body(body)
                .retrieve()
                .body(ParameterizedTypeReference.forType(resolvableType.getType()));
    }

    /**
     * Performs a POST request to the specified engine container at the specified endpoint.
     *
     * @param engineContainer BPM engine container to which the request should be sent
     * @param endpoint        the endpoint after {@link EngineContainer#getRestBaseUrl()} to which the request should be sent
     * @param body            request body
     */
    public void postVoid(HasRunningEngineData engineContainer, String endpoint, Object body) {
        String restBaseUrl = engineContainer.getRestBaseUrl();

        restClient.post()
                .uri(restBaseUrl + endpoint)
                .headers(httpHeaders -> addAuthHeader(httpHeaders, engineContainer))
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * Performs a DELETE request to the specified engine container at the specified endpoint.
     *
     * @param engineContainer BPM engine container to which the request should be sent
     * @param endpoint        the endpoint after {@link EngineContainer#getRestBaseUrl()} to which the request should be sent
     */
    public void delete(HasRunningEngineData engineContainer, String endpoint) {
        String restBaseUrl = engineContainer.getRestBaseUrl();

        restClient.delete()
                .uri(restBaseUrl + endpoint)
                .headers(httpHeaders -> addAuthHeader(httpHeaders, engineContainer))
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * Performs a PUT request to the specified engine container at the specified endpoint.
     *
     * @param engineContainer BPM engine container to which the request should be sent
     * @param endpoint        the endpoint after {@link EngineContainer#getRestBaseUrl()} to which the request should be sent
     * @param body            request body
     */
    public void putVoid(HasRunningEngineData engineContainer, String endpoint, Object body) {
        String restBaseUrl = engineContainer.getRestBaseUrl();

        restClient.put()
                .uri(restBaseUrl + endpoint)
                .headers(httpHeaders -> addAuthHeader(httpHeaders, engineContainer))
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    private void addAuthHeader(HttpHeaders httpHeaders, HasRunningEngineData container) {
        if (container.isBasicAuthEnabled()) {
            httpHeaders.setBasicAuth(container.getBasicAuthUsername(), container.getBasicAuthPassword());
        } else if (container.isHeaderAuthEnabled()) {
            httpHeaders.add(container.getAuthHeaderName(), container.getAuthHeaderValue());
        }
    }

}
