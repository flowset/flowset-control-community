/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.decisiondefinition;

import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.test_support.AbstractIntegrationTest;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
public class NoopDecisionDefinitionServiceTest extends AbstractIntegrationTest {

    @Autowired
    DecisionDefinitionService decisionDefinitionService;

    @Test
    @DisplayName("Empty list of latest decision versions returned when no engine exists")
    void givenNoEngine_whenFindLatestVersions_thenEmptyListReturned() {
        //when
        List<DecisionDefinitionData> decisionDefinitions = decisionDefinitionService.findLatestVersions();

        //then
        assertThat(decisionDefinitions).isEmpty();
    }

    @Test
    @DisplayName("Empty list of decision definitions returned when no engine exists")
    void givenNoEngine_whenFindAll_thenEmptyListReturned() {
        //when
        List<DecisionDefinitionData> decisionDefinitions = decisionDefinitionService
                .findAll(new DecisionDefinitionLoadContext());

        //then
        assertThat(decisionDefinitions).isEmpty();
    }

    @Test
    @DisplayName("Empty list of decision versions by key returned when no engine exists")
    void givenNoEngine_whenFindAllByKey_thenEmptyListReturned() {
        //when
        List<DecisionDefinitionData> decisionDefinitions = decisionDefinitionService
                .findAllByKey("decisionDefinitionKey");

        //then
        assertThat(decisionDefinitions).isEmpty();
    }

    @Test
    @DisplayName("Empty list of decision definitions by ids returned when no engine exists")
    void givenNoEngine_whenFindAllByIds_thenEmptyListReturned() {
        //when
        List<DecisionDefinitionData> decisionDefinitions = decisionDefinitionService
                .findAllByIds(List.of("decisionDefinitionId"));

        //then
        assertThat(decisionDefinitions).isEmpty();
    }

    @Test
    @DisplayName("Zero returned as decision definitions count when no engine exists")
    void givenNoEngine_whenGetCount_thenZeroReturned() {
        //when
        long count = decisionDefinitionService.getCount(null);

        //then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Null returned for decision definition when no engine exists")
    void givenNoEngine_whenGetById_thenNullReturned() {
        //when
        DecisionDefinitionData decisionDefinition = decisionDefinitionService.getById("decisionDefinitionId");

        //then
        assertThat(decisionDefinition).isNull();
    }

    @Test
    @DisplayName("Null returned as DMN XML when no engine exists")
    void givenNoEngine_whenGetDmnXml_thenNullReturned() {
        //when
        String dmnXml = decisionDefinitionService.getDmnXml("decisionDefinitionId");

        //then
        assertThat(dmnXml).isNull();
    }
}
