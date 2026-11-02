/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.engine;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.format.DateTimeFormatter;

/**
 * A request to obtain a date serialized by the engine with the offset of its server.
 */
@Getter
@Setter
@Accessors(chain = true)
public class ZoneOffsetSource {
    /**
     * The request path relative to the engine base URL.
     */
    protected String path;

    /**
     * The name of the date attribute in the JSON response.
     */
    protected String dateField;

    /**
     * The format in which the engine serializes the date attribute, including its offset.
     */
    protected DateTimeFormatter dateFormatter;
}
