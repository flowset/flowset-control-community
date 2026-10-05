/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integrationTest")
@DisplayName("URL validation in UrlUtils")
class UrlUtilsTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "http://localhost:8080/engine-rest",
            "https://flowset-operaton.test.com/engine-rest",
            "http://host.docker.internal:8080/engine-rest",
            "https://10.5.44.44:8080/engine-rest",
            "HTTP://Localhost:8080/engine-rest",
            "https://localhost"
    })
    @DisplayName("HTTP and HTTPS URL with a host is valid")
    void givenHttpUrlWithHost_whenIsValidUrl_thenTrueReturned(String url) {
        // given, when
        boolean valid = UrlUtils.isValidUrl(url);

        // then
        assertThat(valid).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "qqq",
            "123",
            "www.google.com",
            "//localhost:8080/engine-rest",
            "localhost:8080/engine-rest",
            "http://localhost:notaport/engine-rest",
            "http://local host:8080/engine-rest",
            "   "
    })
    @DisplayName("Value that cannot be parsed as a URL is not valid")
    void givenNotParsableValue_whenIsValidUrl_thenFalseReturned(String url) {
        // given, when
        boolean valid = UrlUtils.isValidUrl(url);

        // then
        assertThat(valid).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ftp://localhost:8080/engine-rest",
            "file:///c:/engine-rest",
            "jar:file:/engine.jar!/engine-rest",
            "mailto:user@example.com"
    })
    @DisplayName("URL with a scheme other than HTTP or HTTPS is not valid")
    void givenUrlWithUnsupportedScheme_whenIsValidUrl_thenFalseReturned(String url) {
        // given, when
        boolean valid = UrlUtils.isValidUrl(url);

        // then
        assertThat(valid).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://",
            "http:///engine-rest",
            "http://:8080/engine-rest"
    })
    @DisplayName("HTTP URL without a host is not valid")
    void givenHttpUrlWithoutHost_whenIsValidUrl_thenFalseReturned(String url) {
        // given, when
        boolean valid = UrlUtils.isValidUrl(url);

        // then
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("Null or empty value is not valid")
    void givenNullOrEmptyValue_whenIsValidUrl_thenFalseReturned() {
        // given, when, then
        assertThat(UrlUtils.isValidUrl(null)).isFalse();
        assertThat(UrlUtils.isValidUrl("")).isFalse();
    }
}
