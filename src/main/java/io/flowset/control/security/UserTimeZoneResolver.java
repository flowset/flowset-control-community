/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.security;

import io.jmix.core.HasTimeZone;
import io.jmix.core.security.CurrentAuthentication;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.TimeZone;

/**
 * Resolves the time zone used to display engine dates for the current user.
 * <p>
 * Uses the time zone selected in the user settings ({@link HasTimeZone#getTimeZoneId()}).
 * If it is not set, the time zone of the Flowset Control server is used.
 * Unlike {@link CurrentAuthentication#getTimeZone()}, the device or client time zone is never used.
 */
@Component("control_UserTimeZoneResolver")
public class UserTimeZoneResolver {

    protected final CurrentAuthentication currentAuthentication;

    public UserTimeZoneResolver(CurrentAuthentication currentAuthentication) {
        this.currentAuthentication = currentAuthentication;
    }

    /**
     * @return time zone of the current user or the time zone of the Flowset Control server
     */
    public TimeZone getTimeZone() {
        if (currentAuthentication.isSet()) {
            Authentication authentication = currentAuthentication.getAuthentication();
            if (authentication.getPrincipal() instanceof HasTimeZone hasTimeZone
                    && StringUtils.isNotBlank(hasTimeZone.getTimeZoneId())) {
                return TimeZone.getTimeZone(hasTimeZone.getTimeZoneId());
            }
        }
        return TimeZone.getDefault();
    }
}
