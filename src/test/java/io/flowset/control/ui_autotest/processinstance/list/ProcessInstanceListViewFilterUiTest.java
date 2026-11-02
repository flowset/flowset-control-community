/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.list;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processinstance.ProcessInstanceListView;
import io.flowset.control.test_support.ui.view.processinstance.ProcessInstanceStateFilterDialog;
import io.flowset.control.view.processinstance.ProcessInstanceViewMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.jmix.masquerade.JConditions.CHECKED;
import static io.jmix.masquerade.JConditions.DISABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Filters on process instance list view")
public class ProcessInstanceListViewFilterUiTest extends AbstractCamunda7UiTest {

    @Test
    @DisplayName("State filter shows unchecked 'With incidents' checkbox by default")
    void givenLoggedInUser_whenOpenStateFilter_thenWithIncidentsCheckboxVisibleAndNotChecked() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceStateFilterDialog filterDialog = mainView.openProcessInstanceListView()
                .openStateFilter();

        // then
        filterDialog.getWithIncidentsCheckbox()
                .shouldBe(VISIBLE)
                .shouldNotBe(CHECKED);
    }

    @Test
    @DisplayName("Opening list view with withIncidents query parameter checks 'With incidents' checkbox")
    void givenLoggedInUser_whenOpenWithIncidentsParamInUrl_thenFilterPreapplied() {
        // given
        loginAsAdmin();

        // when
        open("/bpm/process-instances?withIncidents=true");

        ProcessInstanceStateFilterDialog filterDialog = $j(ProcessInstanceListView.class)
                .exists()
                .displayed()
                .openStateFilter();

        // then
        filterDialog.getWithIncidentsCheckbox()
                .shouldBe(VISIBLE)
                .shouldBe(CHECKED);
    }

    @Test
    @DisplayName("State field of the state filter is disabled in Completed mode")
    void givenLoggedInUser_whenSwitchToCompletedMode_thenStateFieldDisabled() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceStateFilterDialog filterDialog = mainView.openProcessInstanceListView()
                .switchToMode(ProcessInstanceViewMode.COMPLETED)
                .openStateFilter();

        // then
        filterDialog.getStateComboBox()
                .shouldBe(VISIBLE)
                .shouldBe(DISABLED);
    }
}
