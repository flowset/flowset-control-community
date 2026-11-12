/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.list;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processinstance.terminatereason.InstanceTerminateReasonDialog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static io.flowset.control.test_support.ui.condition.ControlCondition.anyBodyRowHaveCellText;
import static io.flowset.control.test_support.ui.condition.ControlCondition.visibleBodyRowCount;
import static io.flowset.control.test_support.ui.view.processinstance.ProcessInstanceListView.STATE_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.processinstance.ProcessInstanceListView.VIEW_TERMINATE_REASON_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.processinstance.terminatereason.InstanceTerminateReasonDialog.ACTIVITY_ID_COLUMN_INDEX;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Terminate reason dialog of Process instance list view")
public class InstanceTerminateReasonViewUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Incidents group lists the incidents of the terminated instance")
    void givenTerminatedInstanceWithIncident_whenViewTerminateReasonClicked_thenIncidentsGroupVisible() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").getFirst();
        camundaRestTestHelper.terminateExternallyInstance(camunda7, instanceId);

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessInstanceListView()
                .switchToAllViewMode()
                .getRowByInstanceId(instanceId)
                .getCellByIndex(STATE_COLUMN_INDEX)
                .getCellContent()
                .find(VIEW_TERMINATE_REASON_BUTTON_BY)
                .click();

        // then
        InstanceTerminateReasonDialog dialog = $j(InstanceTerminateReasonDialog.class)
                .exists()
                .displayed();
        dialog.getIncidentsGroup()
                .shouldBe(VISIBLE);
        dialog.getIncidentsGrid()
                .shouldBe(VISIBLE)
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(anyBodyRowHaveCellText(ACTIVITY_ID_COLUMN_INDEX, "throwsExceptionTask"));
    }
}
