/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.camunda7.dto.response;

import io.flowset.control.test_support.camunda7.dto.IdDto;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class BatchDto extends IdDto {
    private String type;
    private OffsetDateTime startTime;
}
