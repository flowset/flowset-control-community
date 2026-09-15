/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.engine.auth;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.property.EngineOAuth2Properties;
import org.springframework.http.HttpHeaders;

/**
 * Interface for authenticating requests to BPM engines.
 */
public interface EngineAuthenticator {

    /**
     * Adds authentication headers to the specified HTTP headers according to the authentication type of the specified BPM engine.
     * In the case of OAuth 2 authentication, the access token is obtained and added to the request template using the following rules:
     * <ul>
     *     <li>It is also checked that getting an access token is locked for the BPM engine</li>
     *     <li>If the access token is obtained successfully earlier and not expired, then the cached value is used</li>
     *     <li>If unable to get an access token, the <code>Authorization</code> header is not added</li>
     *     <li>If an access token obtaining fails because of a connection error or invalid credentials, then a failed attempt is registered.</li>
     * </ul>
     *
     * @param engine  a BPM engine containing authentication details
     * @param headers HTTP headers to add authentication headers to
     */
    void applyAuthentication(BpmEngine engine, HttpHeaders headers);

    /**
     * Adds authentication headers to the specified request template according to the authentication type of the specified BPM engine.
     * In the case of OAuth 2 authentication, the access token is obtained and added to the request template using the following rules:
     * <ul>
     *     <li>It is also checked that getting an access token is locked for the BPM engine</li>
     *     <li>If the access token is obtained successfully earlier and not expired, then the cached value is used</li>
     *     <li>If unable to get an access token, the <code>Authorization</code> header is not added</li>
     *     <li>If an access token obtaining fails because of a connection error or invalid credentials, then a failed attempt is registered.</li>
     * </ul>
     *
     * @param engine   a BPM engine containing authentication details
     * @param template a request template to add authentication headers to
     */
    void applyAuthentication(BpmEngine engine, RequestTemplate template);

    /**
     * Creates a request interceptor that adds authentication headers to requests according to the authentication type of the specified BPM engine.
     * In the case of OAuth 2 authentication:
     * <ul>
     *     <li>It is also checked that getting an access token is locked for the BPM engine</li>
     *     <li>If the access token is obtained successfully earlier and not expired, then the cached value is used</li>
     *     <li>If unable to get an access token, the <code>Authorization</code> header is not added</li>
     *     <li>If an access token obtaining fails because of a connection error or invalid credentials, then a failed attempt is registered.</li>
     * </ul>
     *
     * @param engine a BPM engine containing authentication details
     * @return a request interceptor that adds authentication headers to requests
     * @see EngineOAuth2Properties
     */
    RequestInterceptor createLockAwareAuthInterceptor(BpmEngine engine);

    /**
     * Creates a request interceptor that adds authentication headers to requests according to the authentication type of the specified BPM engine.
     * In the case of OAuth 2 authentication:
     * <ul>
     *     <li>Locking of access token retrieval is not checked</li>
     *     <li>If the access token is obtained successfully, it is not cached and a client for the BPM engine is not cached</li>
     *     <li>If an access token obtaining fails because of a connection error or invalid credentials, then a failed attempt is NOT registered.</li>
     * </ul>
     *
     * @param engine a BPM engine containing authentication details
     * @return a request interceptor that adds authentication headers to requests
     */
    RequestInterceptor createStatelessAuthInterceptor(BpmEngine engine);
}
