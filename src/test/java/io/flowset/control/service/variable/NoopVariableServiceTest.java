/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.variable;

import io.flowset.control.entity.variable.HistoricVariableInstanceData;
import io.flowset.control.entity.variable.VariableInstanceData;
import io.flowset.control.exception.EngineNotSelectedException;
import io.flowset.control.test_support.AbstractIntegrationTest;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
public class NoopVariableServiceTest extends AbstractIntegrationTest {

    @Autowired
    VariableService variableService;

    @Test
    @DisplayName("Empty list of runtime variables returned when no engine exists")
    void givenNoEngine_whenFindRuntimeVariables_thenEmptyListReturned() {
        //when
        List<VariableInstanceData> variables = variableService.findRuntimeVariables(new VariableLoadContext());

        //then
        assertThat(variables).isEmpty();
    }

    @Test
    @DisplayName("Empty list of historic variables returned when no engine exists")
    void givenNoEngine_whenFindHistoricVariables_thenEmptyListReturned() {
        //when
        List<HistoricVariableInstanceData> variables = variableService
                .findHistoricVariables(new VariableLoadContext());

        //then
        assertThat(variables).isEmpty();
    }

    @Test
    @DisplayName("Zero returned as runtime variables count when no engine exists")
    void givenNoEngine_whenGetRuntimeVariablesCount_thenZeroReturned() {
        //when
        long count = variableService.getRuntimeVariablesCount(null);

        //then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Zero returned as historic variables count when no engine exists")
    void givenNoEngine_whenGetHistoricVariablesCount_thenZeroReturned() {
        //when
        long count = variableService.getHistoricVariablesCount(null);

        //then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Null returned for runtime variable when no engine exists")
    void givenNoEngine_whenFindRuntimeVariableById_thenNullReturned() {
        //when
        VariableInstanceData variable = variableService.findRuntimeVariableById("variableId");

        //then
        assertThat(variable).isNull();
    }

    @Test
    @DisplayName("Null returned as variable binary when no engine exists")
    void givenNoEngine_whenGetVariableInstanceBinary_thenNullReturned() {
        //when
        Resource binary = variableService.getVariableInstanceBinary("variableId");

        //then
        assertThat(binary).isNull();
    }

    @Test
    @DisplayName("Null returned for historic variable when no engine exists")
    void givenNoEngine_whenFindHistoricVariableById_thenNullReturned() {
        //when
        HistoricVariableInstanceData variable = variableService.findHistoricVariableById("variableId");

        //then
        assertThat(variable).isNull();
    }

    @Test
    @DisplayName("EngineNotSelectedException thrown when update variable if no engine exists")
    void givenNoEngine_whenUpdateVariableLocal_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> variableService.updateVariableLocal(new VariableInstanceData()))
                .isInstanceOf(EngineNotSelectedException.class);
    }

    @Test
    @DisplayName("EngineNotSelectedException thrown when remove variable if no engine exists")
    void givenNoEngine_whenRemoveVariableLocal_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> variableService.removeVariableLocal(new VariableInstanceData()))
                .isInstanceOf(EngineNotSelectedException.class);
    }

    @Test
    @DisplayName("EngineNotSelectedException thrown when remove variables if no engine exists")
    void givenNoEngine_whenRemoveVariablesLocal_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> variableService.removeVariablesLocal(Set.of(new VariableInstanceData())))
                .isInstanceOf(EngineNotSelectedException.class);
    }

    @Test
    @DisplayName("EngineNotSelectedException thrown when update variable binary if no engine exists")
    void givenNoEngine_whenUpdateVariableBinary_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> variableService.updateVariableBinary(new VariableInstanceData(),
                new File("variable.bin")))
                .isInstanceOf(EngineNotSelectedException.class);
    }
}
