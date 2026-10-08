/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.security;

import io.jmix.core.security.CurrentAuthentication;
import io.flowset.control.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserTimeZoneResolverTest {

    private CurrentAuthentication currentAuthentication;
    private UserTimeZoneResolver resolver;

    @BeforeEach
    void setUp() {
        currentAuthentication = mock(CurrentAuthentication.class);
        resolver = new UserTimeZoneResolver(currentAuthentication);
    }

    @Test
    void givenUserWithTimeZone_whenGetTimeZone_thenUserTimeZoneReturned() {
        authenticate("Asia/Vladivostok");

        assertThat(resolver.getTimeZone().getID()).isEqualTo("Asia/Vladivostok");
    }

    @Test
    void givenUserWithoutTimeZone_whenGetTimeZone_thenServerTimeZoneReturned() {
        authenticate(null);

        assertThat(resolver.getTimeZone()).isEqualTo(TimeZone.getDefault());
    }

    @Test
    void givenNoAuthentication_whenGetTimeZone_thenServerTimeZoneReturned() {
        when(currentAuthentication.isSet()).thenReturn(false);

        assertThat(resolver.getTimeZone()).isEqualTo(TimeZone.getDefault());
    }

    private void authenticate(String timeZoneId) {
        User user = new User();
        user.setTimeZoneId(timeZoneId);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        when(currentAuthentication.isSet()).thenReturn(true);
        when(currentAuthentication.getAuthentication()).thenReturn(authentication);
    }
}
