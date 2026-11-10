/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.incident;

import io.flowset.control.dto.ActivityIncidentData;
import io.flowset.control.entity.incident.HistoricIncidentData;
import io.flowset.control.entity.incident.IncidentData;
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
public class NoopIncidentServiceTest extends AbstractIntegrationTest {

    @Autowired
    IncidentService incidentService;

    @Test
    @DisplayName("Empty list of process instance incidents returned when no engine exists")
    void givenNoEngine_whenFindRuntimeIncidentsByProcessInstanceId_thenEmptyListReturned() {
        //when
        List<ActivityIncidentData> incidents = incidentService.findRuntimeIncidents("instanceId");

        //then
        assertThat(incidents).isEmpty();
    }

    @Test
    @DisplayName("Empty list of runtime incidents returned when no engine exists")
    void givenNoEngine_whenFindRuntimeIncidents_thenEmptyListReturned() {
        //when
        List<IncidentData> incidents = incidentService.findRuntimeIncidents(new IncidentLoadContext());

        //then
        assertThat(incidents).isEmpty();
    }

    @Test
    @DisplayName("Empty list of historic incidents returned when no engine exists")
    void givenNoEngine_whenFindHistoricIncidents_thenEmptyListReturned() {
        //when
        List<HistoricIncidentData> incidents = incidentService.findHistoricIncidents(new IncidentLoadContext());

        //then
        assertThat(incidents).isEmpty();
    }

    @Test
    @DisplayName("Zero returned as runtime incidents count when no engine exists")
    void givenNoEngine_whenGetRuntimeIncidentCount_thenZeroReturned() {
        //when
        long count = incidentService.getRuntimeIncidentCount(null);

        //then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Zero returned as historic incidents count when no engine exists")
    void givenNoEngine_whenGetHistoricIncidentCount_thenZeroReturned() {
        //when
        long count = incidentService.getHistoricIncidentCount(null);

        //then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Null returned for runtime incident when no engine exists")
    void givenNoEngine_whenFindRuntimeIncidentById_thenNullReturned() {
        //when
        IncidentData incident = incidentService.findRuntimeIncidentById("incidentId");

        //then
        assertThat(incident).isNull();
    }

    @Test
    @DisplayName("Null returned for historic incident when no engine exists")
    void givenNoEngine_whenFindHistoricIncidentById_thenNullReturned() {
        //when
        HistoricIncidentData incident = incidentService.findHistoricIncidentById("incidentId");

        //then
        assertThat(incident).isNull();
    }
}
