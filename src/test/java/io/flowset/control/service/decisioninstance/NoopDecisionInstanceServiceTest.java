/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.decisioninstance;

import io.flowset.control.entity.decisioninstance.HistoricDecisionInstanceShortData;
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
public class NoopDecisionInstanceServiceTest extends AbstractIntegrationTest {

    @Autowired
    DecisionInstanceService decisionInstanceService;

    @Test
    @DisplayName("Empty list of decision instances returned when no engine exists")
    void givenNoEngine_whenFindAllHistoryDecisionInstances_thenEmptyListReturned() {
        //when
        List<HistoricDecisionInstanceShortData> decisionInstances = decisionInstanceService
                .findAllHistoryDecisionInstances(new DecisionInstanceLoadContext());

        //then
        assertThat(decisionInstances).isEmpty();
    }

    @Test
    @DisplayName("Zero returned as decision instances count when no engine exists")
    void givenNoEngine_whenGetHistoryDecisionInstancesCount_thenZeroReturned() {
        //when
        long count = decisionInstanceService.getHistoryDecisionInstancesCount(null);

        //then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Zero returned as decision instances count by definition id when no engine exists")
    void givenNoEngine_whenGetCountByDecisionDefinitionId_thenZeroReturned() {
        //when
        long count = decisionInstanceService.getCountByDecisionDefinitionId("decisionDefinitionId");

        //then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Zero returned as decision instances count by definition key when no engine exists")
    void givenNoEngine_whenGetCountByDecisionDefinitionKey_thenZeroReturned() {
        //when
        long count = decisionInstanceService.getCountByDecisionDefinitionKey("decisionDefinitionKey");

        //then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Null returned for decision instance when no engine exists")
    void givenNoEngine_whenGetById_thenNullReturned() {
        //when
        HistoricDecisionInstanceShortData decisionInstance = decisionInstanceService.getById("decisionInstanceId");

        //then
        assertThat(decisionInstance).isNull();
    }
}
