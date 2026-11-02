/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processinstance.action.ProcessInstanceTerminateDialog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static io.jmix.masquerade.JConditions.CHECKED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Terminate process instance dialog")
public class ProcessInstanceTerminateViewUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Skip I/O mappings field is checked by default")
    void givenActiveProcessInstance_whenClickTerminate_thenSkipIoMappingsFieldChecked() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .getGeneralPanel()
                .getTerminateBtn()
                .click();

        // then
        $j(ProcessInstanceTerminateDialog.class)
                .exists()
                .displayed()
                .getSkipIoMappingsField()
                .shouldBe(VISIBLE)
                .shouldBe(CHECKED);
    }

    @Test
    @DisplayName("Skip custom listeners field is checked by default")
    void givenActiveProcessInstance_whenClickTerminate_thenSkipCustomListenersFieldChecked() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .getGeneralPanel()
                .getTerminateBtn()
                .click();

        // then
        $j(ProcessInstanceTerminateDialog.class)
                .exists()
                .displayed()
                .getSkipCustomListenersField()
                .shouldBe(VISIBLE)
                .shouldBe(CHECKED);
    }

    @Test
    @DisplayName("Skip subprocesses field is unchecked by default")
    void givenActiveProcessInstance_whenClickTerminate_thenSkipSubprocessesFieldNotChecked() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .getGeneralPanel()
                .getTerminateBtn()
                .click();

        // then
        $j(ProcessInstanceTerminateDialog.class)
                .exists()
                .displayed()
                .getSkipSubprocessesField()
                .shouldBe(VISIBLE)
                .shouldNotBe(CHECKED);
    }
}
