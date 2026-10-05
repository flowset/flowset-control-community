/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.util;

import com.google.common.base.Strings;
import org.jspecify.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.Set;

public class UrlUtils {

    /**
     * Schemes that HTTP clients used for BPM engine communication are able to handle.
     */
    protected static final Set<String> SUPPORTED_SCHEMES = Set.of("http", "https");

    public static boolean isValidUrl(@Nullable String url) {
        if (Strings.isNullOrEmpty(url)) {
            return false;
        }
        try {
            URL parsedUrl = new URI(url).toURL();
            // URL normalizes the scheme to the lower case.
            return SUPPORTED_SCHEMES.contains(parsedUrl.getProtocol())
                    && !Strings.isNullOrEmpty(parsedUrl.getHost());
        } catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
            return false;
        }
    }

    @Nullable
    public static String getBaseUrl(@Nullable String urlString) {
        if (urlString == null) {
            return null;
        }
        try {
            URL url = new URI(urlString).toURL();
            if (url.getPort() == -1) { // port is not set
                return url.getProtocol() + "://" + url.getHost();
            } else {
                return url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
            }

        } catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
            return null;
        }
    }

    public static Optional<String> findBaseUrl(@Nullable String urlString) {
        return Optional.ofNullable(getBaseUrl(urlString));
    }
}
