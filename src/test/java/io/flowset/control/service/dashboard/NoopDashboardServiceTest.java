/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.dashboard;

import io.flowset.control.entity.ProcessExecutionGraphEntry;
import io.flowset.control.entity.dashboard.ProcessDefinitionStatistics;
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
public class NoopDashboardServiceTest extends AbstractIntegrationTest {

    @Autowired
    DashboardService dashboardService;

    @Test
    @DisplayName("Empty recent activity statistics returned when no engine exists")
    void givenNoEngine_whenGetRecentActivityStatistics_thenEmptyListReturned() {
        //when
        List<ProcessExecutionGraphEntry> statistics = dashboardService.getRecentActivityStatistics(null);

        //then
        assertThat(statistics).isEmpty();
    }

    @Test
    @DisplayName("Empty process definition statistics returned when no engine exists")
    void givenNoEngine_whenGetProcessDefinitionStatistics_thenEmptyListReturned() {
        //when
        List<ProcessDefinitionStatistics> statistics = dashboardService.getProcessDefinitionStatistics(null);

        //then
        assertThat(statistics).isEmpty();
    }

    @Test
    @DisplayName("Zero returned as user tasks count when no engine exists")
    void givenNoEngine_whenGetUserTasksCount_thenZeroReturned() {
        //when
        long count = dashboardService.getUserTasksCount(null);

        //then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Zero returned as deployed processes count when no engine exists")
    void givenNoEngine_whenGetDeployedProcessesCount_thenZeroReturned() {
        //when
        long count = dashboardService.getDeployedProcessesCount(null);

        //then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Zero returned as running processes count when no engine exists")
    void givenNoEngine_whenGetRunningProcessCount_thenZeroReturned() {
        //when
        long count = dashboardService.getRunningProcessCount(null);

        //then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Zero returned as suspended processes count when no engine exists")
    void givenNoEngine_whenGetSuspendedProcessCount_thenZeroReturned() {
        //when
        long count = dashboardService.getSuspendedProcessCount(null);

        //then
        assertThat(count).isZero();
    }
}
