/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.externaltask;

import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.response.ExternalTaskDto;
import io.flowset.control.test_support.security.AuthenticatedAsUser;
import io.flowset.control.test_support.security.WithTestUser;
import io.flowset.control.test_support.security.role.externaltask.TestExternalTaskRetryPermissionRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@WithRunningEngine
public class Camunda7ExternalTaskServiceSecurityTest extends AbstractCamunda7IntegrationTest {

    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ExternalTaskService externalTaskService;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Set external task retries when user has retry permission")
    @WithTestUser(username = "test-user-secured-external-task-retry",
            roles = TestExternalTaskRetryPermissionRole.class)
    @AuthenticatedAsUser(username = "test-user-secured-external-task-retry")
    void givenUserWithExternalTaskRetryPermission_whenSetRetries_thenRetriesUpdated() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager.deploy("test_support/testExternalTaskRetriesUpdate.bpmn")
                .startByKey("testExternalTaskRetriesUpdate");
        List<String> instanceIds = sampleDataManager.getStartedInstances("testExternalTaskRetriesUpdate");
        ExternalTaskDto sourceTask = camundaRestTestHelper.getExternalTasks(camunda7, instanceIds).get(0);

        //when
        externalTaskService.setRetries(sourceTask.getId(), 5);

        //then
        ExternalTaskDto updatedTask = camundaRestTestHelper.getExternalTaskById(camunda7, sourceTask.getId());
        assertThat(updatedTask).isNotNull();
        assertThat(updatedTask.getRetries()).isEqualTo(5);
    }
}
