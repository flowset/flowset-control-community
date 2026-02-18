/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.decisiondefinition;

import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@WithRunningEngine
public class Camunda7DecisionDefinitionServiceTest extends AbstractCamunda7IntegrationTest {
    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    DecisionDefinitionService decisionDefinitionService;

    @Test
    @DisplayName("EngineConnectionFailedException thrown when find decision definition by ids if engine is not available")
    void givenDeployedDecisionDefinitionsNotAvailableEngine_whenFindAllByIds_thenExceptionThrown() {
        //given
        List<String> dmnIds = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .getDeployedDecisionVersions("decision_testDmn");

        camunda7.stop();


        //when and then
        assertThatThrownBy(() -> decisionDefinitionService.findAllByIds(dmnIds))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("Find decision definitions by ids")
    void givenDeployedDecisionDefinitions_whenFindAllByIds_thenDecisionDefinitionsReturned() {
        //given
        List<String> dmnIds = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/dmn/testDmn2.dmn")
                .getDeployedDecisionVersions("decision_testDmn");


        //when
        List<DecisionDefinitionData> foundDecisions = decisionDefinitionService.findAllByIds(dmnIds);

        //then
        assertThat(foundDecisions).hasSize(1)
                .extracting(DecisionDefinitionData::getDecisionDefinitionId)
                .containsAll(dmnIds);
    }
}
