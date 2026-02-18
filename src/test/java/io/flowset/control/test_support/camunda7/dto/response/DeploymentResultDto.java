/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.camunda7.dto.response;

import io.flowset.control.test_support.camunda7.dto.IdDto;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class DeploymentResultDto extends IdDto {
    private Map<String, ProcessDefinitionDto> deployedProcessDefinitions = new HashMap<>();
    private Map<String, DecisionDefinitionDto> deployedDecisionDefinitions = new HashMap<>();
}
