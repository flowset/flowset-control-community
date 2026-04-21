/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.util;

import org.springframework.lang.Nullable;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

public class UrlUtils {

    public static boolean isValidUrl(String url) {
        try {
            //noinspection ResultOfMethodCallIgnored
            new URI(url).toURL();
            return true;
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
