/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.util;

import io.flowset.control.dto.DmnDecisionDefinition;
import io.flowset.control.dto.BpmProcessDefinition;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

public class BpmParseUtil {
    private static final JsonMapper objectMapper = new JsonMapper();
    private static final Logger log = LoggerFactory.getLogger(BpmParseUtil.class);

    public static List<BpmProcessDefinition> parseProcessDefinitionsJson(String processDefinitionsJson) {
        try {
            if (StringUtils.isNotBlank(processDefinitionsJson)) {
                return objectMapper.readValue(processDefinitionsJson, new TypeReference<>() {
                });
            }
        } catch (JacksonException e) {
            log.error("Unable parse definitions JSON {}", processDefinitionsJson);
        }
        return List.of();
    }

    public static List<DmnDecisionDefinition> parseDecisionsDefinitionsJson(String decisionDefinitionsJson) {
        try {
            if (StringUtils.isNotBlank(decisionDefinitionsJson)) {
                return objectMapper.readValue(decisionDefinitionsJson, new TypeReference<>() {
                });
            }
        } catch (JacksonException e) {
            log.error("Unable parse definitions JSON {}", decisionDefinitionsJson);
        }
        return List.of();
    }
}
