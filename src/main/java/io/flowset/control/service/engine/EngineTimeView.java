/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.engine;

/**
 * Current engine time formatted for display.
 *
 * @param serverTime engine time in the time zone of the engine server
 *                   (equals {@code userTime} if the engine time zone is unknown)
 * @param userTime   engine time in the time zone of the current user
 */
public record EngineTimeView(String serverTime, String userTime) {
}
