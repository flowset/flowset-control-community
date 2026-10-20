/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.camunda7.dto.response;

import io.flowset.control.test_support.camunda7.dto.IdDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExecutionDto extends IdDto {
    private String processInstanceId;
    private Boolean ended;
}
