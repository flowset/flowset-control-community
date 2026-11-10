/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.util;

import io.jmix.core.Sort;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

@DisplayName("Deployment sort options in QueryUtils")
class QueryUtilsDeploymentSortTest {

    private DeploymentQuery deploymentQuery;

    @BeforeEach
    void setUp() {
        deploymentQuery = mock(DeploymentQuery.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"deploymentTime", "time"})
    @DisplayName("Sort by deployment time is applied with the requested direction")
    void givenDeploymentTimeSort_whenAddDeploymentSort_thenOrderedByTimeWithDirection(String property) {
        // when
        QueryUtils.addDeploymentSort(deploymentQuery, Sort.by(Sort.Direction.DESC, property));

        // then
        InOrder inOrder = inOrder(deploymentQuery);
        inOrder.verify(deploymentQuery).orderByDeploymentTime();
        inOrder.verify(deploymentQuery).desc();
    }

    @ParameterizedTest
    @ValueSource(strings = {"deploymentId", "id"})
    @DisplayName("Sort by deployment id is applied with the requested direction")
    void givenDeploymentIdSort_whenAddDeploymentSort_thenOrderedByIdWithDirection(String property) {
        // when
        QueryUtils.addDeploymentSort(deploymentQuery, Sort.by(Sort.Direction.ASC, property));

        // then
        InOrder inOrder = inOrder(deploymentQuery);
        inOrder.verify(deploymentQuery).orderByDeploymentId();
        inOrder.verify(deploymentQuery).asc();
    }

    @Test
    @DisplayName("Sort by name and tenant id are applied")
    void givenNameAndTenantSort_whenAddDeploymentSort_thenBothOrdersApplied() {
        // when
        QueryUtils.addDeploymentSort(deploymentQuery, Sort.by(
                Sort.Order.asc("name"),
                Sort.Order.desc("tenantId")));

        // then
        InOrder inOrder = inOrder(deploymentQuery);
        inOrder.verify(deploymentQuery).orderByDeploymentName();
        inOrder.verify(deploymentQuery).asc();
        inOrder.verify(deploymentQuery).orderByTenantId();
        inOrder.verify(deploymentQuery).desc();
    }

    @Test
    @DisplayName("Unsupported sort property is ignored together with its direction")
    void givenUnsupportedProperty_whenAddDeploymentSort_thenQueryNotChanged() {
        // when
        QueryUtils.addDeploymentSort(deploymentQuery, Sort.by(Sort.Direction.ASC, "source"));

        // then
        verifyNoInteractions(deploymentQuery);
    }

    @Test
    @DisplayName("Null sort leaves the query unchanged")
    void givenNullSort_whenAddDeploymentSort_thenQueryNotChanged() {
        // when
        QueryUtils.addDeploymentSort(deploymentQuery, null);

        // then
        verifyNoInteractions(deploymentQuery);
    }
}
