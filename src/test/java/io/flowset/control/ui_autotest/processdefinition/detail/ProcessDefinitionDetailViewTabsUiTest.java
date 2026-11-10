/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processdefinition.detail;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processdefinition.detail.CalledDecisionsTabFragment;
import io.flowset.control.test_support.ui.view.processdefinition.detail.CalledProcessesTabFragment;
import io.jmix.masquerade.component.DataGrid;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static com.codeborne.selenide.Condition.empty;
import static com.codeborne.selenide.Condition.exactText;
import static io.flowset.control.test_support.ui.condition.ControlCondition.headerCellTextsExactly;

@WithRunningExternalEngine
@DisplayName("Tabs of Process detail view")
public class ProcessDefinitionDetailViewTabsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Called processes grid displays Key, Binding, Version, Version tag, Activity columns")
    void givenExistingCalledProcess_whenOpenCalledProcessesTab_thenVersionColumnsVisible() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVersionBindings.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        CalledProcessesTabFragment calledProcessesTab = mainView.openProcessListView()
                .openDetailViewByKey("testVersionBindings")
                .openCalledProcessesTab();

        // then
        calledProcessesTab.getCalledProcessesGrid()
                .shouldHave(headerCellTextsExactly(List.of("Key", "Binding", "Version", "Version tag", "Activity")));
    }

    @Test
    @DisplayName("Called processes grid displays version and version tag of called processes")
    void givenCalledProcessesBoundByVersionAndVersionTag_whenOpenCalledProcessesTab_thenVersionAndVersionTagShown() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVersionBindings.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        CalledProcessesTabFragment calledProcessesTab = mainView.openProcessListView()
                .openDetailViewByKey("testVersionBindings")
                .openCalledProcessesTab();

        // then
        DataGrid.Row versionRow = calledProcessesTab.getRowByCalledElement("testCalledByVersion");
        versionRow.getCellByIndex(CalledProcessesTabFragment.BINDING_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(exactText("version"));
        versionRow.getCellByIndex(CalledProcessesTabFragment.VERSION_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(exactText("2"));
        versionRow.getCellByIndex(CalledProcessesTabFragment.VERSION_TAG_COLUMN_INDEX)
                .getCellContent()
                .shouldBe(empty);

        DataGrid.Row versionTagRow = calledProcessesTab.getRowByCalledElement("testCalledByVersionTag");
        versionTagRow.getCellByIndex(CalledProcessesTabFragment.BINDING_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(exactText("versionTag"));
        versionTagRow.getCellByIndex(CalledProcessesTabFragment.VERSION_COLUMN_INDEX)
                .getCellContent()
                .shouldBe(empty);
        versionTagRow.getCellByIndex(CalledProcessesTabFragment.VERSION_TAG_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(exactText("v1"));
    }

    @Test
    @DisplayName("Decisions grid displays Key, Binding, Version, Version tag, Activity columns")
    void givenExistingCalledDecision_whenOpenDecisionsTab_thenVersionColumnsVisible() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVersionBindings.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        CalledDecisionsTabFragment decisionsTab = mainView.openProcessListView()
                .openDetailViewByKey("testVersionBindings")
                .openDecisionsTab();

        // then
        decisionsTab.getDecisionsGrid()
                .shouldHave(headerCellTextsExactly(List.of("Key", "Binding", "Version", "Version tag", "Activity")));
    }

    @Test
    @DisplayName("Decisions grid displays version and version tag of called decisions")
    void givenCalledDecisionsBoundByVersionAndVersionTag_whenOpenDecisionsTab_thenVersionAndVersionTagShown() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVersionBindings.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        CalledDecisionsTabFragment decisionsTab = mainView.openProcessListView()
                .openDetailViewByKey("testVersionBindings")
                .openDecisionsTab();

        // then
        DataGrid.Row versionRow = decisionsTab.getRowByDecisionRef("decision_calledByVersion");
        versionRow.getCellByIndex(CalledDecisionsTabFragment.BINDING_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(exactText("version"));
        versionRow.getCellByIndex(CalledDecisionsTabFragment.VERSION_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(exactText("3"));
        versionRow.getCellByIndex(CalledDecisionsTabFragment.VERSION_TAG_COLUMN_INDEX)
                .getCellContent()
                .shouldBe(empty);

        DataGrid.Row versionTagRow = decisionsTab.getRowByDecisionRef("decision_calledByVersionTag");
        versionTagRow.getCellByIndex(CalledDecisionsTabFragment.BINDING_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(exactText("versionTag"));
        versionTagRow.getCellByIndex(CalledDecisionsTabFragment.VERSION_COLUMN_INDEX)
                .getCellContent()
                .shouldBe(empty);
        versionTagRow.getCellByIndex(CalledDecisionsTabFragment.VERSION_TAG_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(exactText("1.0"));
    }
}
