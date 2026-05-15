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
public class DecisionInstanceDto extends IdDto {
    private String activityId;
    private String id;
    private String processInstanceId;
    private String processDefinitionId;
    private String processDefinitionKey;
    private String rootProcessInstanceId;
    private String decisionDefinitionId;
    private String decisionDefinitionKey;
    private String decisionDefinitionName;

}
