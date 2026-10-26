/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.decisioninstance.list;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.decisioninstance.DecisionInstanceListView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static io.flowset.control.test_support.ui.condition.ControlCondition.allBodyRowsHaveCellText;
import static io.flowset.control.test_support.ui.condition.ControlCondition.visibleBodyRowCount;
import static io.flowset.control.test_support.ui.view.decisioninstance.DecisionInstanceListView.ACTIVITY_ID_COLUMN_INDEX;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Filters on decision instance list view")
public class DecisionInstanceListViewFilterUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Opening list view with activity query parameter pre-applies the filter")
    void givenExistingDecisionInstances_whenOpenWithActivityIdParamInUrl_thenFilterPreapplied() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision")
                .evaluateDecisionByKey("decision_testDmn");

        loginAsAdmin();

        // when
        open("/bpm/decision-instances?activityId=evaluateDecisionTask");

        DecisionInstanceListView listView = $j(DecisionInstanceListView.class)
                .exists()
                .displayed();

        // then
        listView.getDecisionInstancesDataGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(ACTIVITY_ID_COLUMN_INDEX, "evaluateDecisionTask"));
    }
}
