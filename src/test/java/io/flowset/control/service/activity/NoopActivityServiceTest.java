/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.activity;

import io.flowset.control.entity.activity.ActivityInstanceTreeItem;
import io.flowset.control.entity.activity.ActivityShortData;
import io.flowset.control.entity.activity.HistoricActivityInstanceData;
import io.flowset.control.entity.activity.ProcessActivityStatistics;
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
public class NoopActivityServiceTest extends AbstractIntegrationTest {

    @Autowired
    ActivityService activityService;

    @Test
    @DisplayName("Empty list of running activities returned when no engine exists")
    void givenNoEngine_whenFindRunningActivities_thenEmptyListReturned() {
        //when
        List<ActivityShortData> activities = activityService.findRunningActivities("instanceId");

        //then
        assertThat(activities).isEmpty();
    }

    @Test
    @DisplayName("Empty list of finished activities returned when no engine exists")
    void givenNoEngine_whenFindFinishedActivities_thenEmptyListReturned() {
        //when
        List<ActivityShortData> activities = activityService.findFinishedActivities("instanceId");

        //then
        assertThat(activities).isEmpty();
    }

    @Test
    @DisplayName("Empty activity instances tree returned when no engine exists")
    void givenNoEngine_whenGetActivityInstancesTree_thenEmptyListReturned() {
        //when
        List<ActivityInstanceTreeItem> treeItems = activityService.getActivityInstancesTree("instanceId");

        //then
        assertThat(treeItems).isEmpty();
    }

    @Test
    @DisplayName("Empty list of history activities returned when no engine exists")
    void givenNoEngine_whenFindAllHistoryActivities_thenEmptyListReturned() {
        //when
        List<HistoricActivityInstanceData> activities = activityService
                .findAllHistoryActivities(new ActivityLoadContext());

        //then
        assertThat(activities).isEmpty();
    }

    @Test
    @DisplayName("Empty activity statistics returned when no engine exists")
    void givenNoEngine_whenGetStatisticsByProcessId_thenEmptyListReturned() {
        //when
        List<ProcessActivityStatistics> statistics = activityService.getStatisticsByProcessId("processDefinitionId");

        //then
        assertThat(statistics).isEmpty();
    }

    @Test
    @DisplayName("Zero returned as history activities count when no engine exists")
    void givenNoEngine_whenGetHistoryActivitiesCount_thenZeroReturned() {
        //when
        long count = activityService.getHistoryActivitiesCount(null);

        //then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Null returned for history activity when no engine exists")
    void givenNoEngine_whenFindById_thenNullReturned() {
        //when
        HistoricActivityInstanceData activity = activityService.findById("activityInstanceId");

        //then
        assertThat(activity).isNull();
    }
}
