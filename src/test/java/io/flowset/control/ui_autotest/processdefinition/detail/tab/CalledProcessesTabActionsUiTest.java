/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processdefinition.detail.tab;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processdefinition.ProcessDefinitionDiagramDialog;
import io.flowset.control.test_support.ui.view.processdefinition.detail.CalledProcessesTabFragment;
import io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessDefinitionDetailView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static io.flowset.control.test_support.ui.view.processdefinition.detail.CalledProcessesTabFragment.*;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions on Called processes tab in process detail view")
public class CalledProcessesTabActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("No actions available in called processes grid")
    void givenExistingCalledProcess_whenOpenDetailView_thenNoGridActionsAvailable() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/terminateinstance/testSkipSubprocess.bpmn")
                .deploy("test_support/terminateinstance/testSkipSubprocessMain.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        CalledProcessesTabFragment calledProcessesTab = mainView.openProcessListView()
                .openDetailViewByKey("testSkipSubprocessMain")
                .openCalledProcessesTab();

        // then
        calledProcessesTab.openProcessGridContextActions()
                .shouldHave(visibleItemsCount(0));
    }

    @Test
    @DisplayName("View action on Called processes tab in Key column opens Called process detail view")
    void givenExistingCalledProcess_whenOpenDetailView_thenViewActionOpensCalledProcessView() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/terminateinstance/testSkipSubprocess.bpmn")
                .deploy("test_support/terminateinstance/testSkipSubprocessMain.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        CalledProcessesTabFragment calledProcessesTab = mainView.openProcessListView()
                .openDetailViewByKey("testSkipSubprocessMain")
                .openCalledProcessesTab();

        // then
        calledProcessesTab.getRowByCalledElement("testSkipSubprocess")
                .getCellByIndex(CALLED_ELEMENT_COLUMN_INDEX)
                .getCellContent()
                .find(KEY_BUTTON_BY).click();

        $j(ProcessDefinitionDetailView.class)
                .exists()
                .getGeneralPanel()
                .getPropertiesPanel()
                .getKeyField()
                .shouldHave(value("testSkipSubprocess"));
    }

    @Test
    @DisplayName("Double-click on grid row on Called processes tab opens Called process detail view")
    void givenExistingCalledProcess_whenOpenDetailView_thenDoubleClickOpensCalledProcessView() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/terminateinstance/testSkipSubprocess.bpmn")
                .deploy("test_support/terminateinstance/testSkipSubprocessMain.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        CalledProcessesTabFragment calledProcessesTab = mainView.openProcessListView()
                .openDetailViewByKey("testSkipSubprocessMain")
                .openCalledProcessesTab();

        // then
        calledProcessesTab.getRowByCalledElement("testSkipSubprocess")
                .getCellByIndex(BINDING_COLUMN_INDEX)
                .getCellContent()
                .doubleClick();

        $j(ProcessDefinitionDetailView.class)
                .exists()
                .getGeneralPanel()
                .getPropertiesPanel()
                .getKeyField()
                .shouldHave(value("testSkipSubprocess"));
    }

    @Test
    @DisplayName("Preview action on Called processes tab opens Preview dialog")
    void givenExistingCalledProcess_whenOpenDetailView_thenPreviewActionOpensPreviewDialog() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/terminateinstance/testSkipSubprocess.bpmn")
                .deploy("test_support/terminateinstance/testSkipSubprocessMain.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        CalledProcessesTabFragment calledProcessesTab = mainView.openProcessListView()
                .openDetailViewByKey("testSkipSubprocessMain")
                .openCalledProcessesTab();

        // then
        calledProcessesTab.getRowByCalledElement("testSkipSubprocess")
                .getCellByIndex(CALLED_ELEMENT_COLUMN_INDEX)
                .getCellContent()
                .find(PREVIEW_BUTTON_BY).click();

        $j(ProcessDefinitionDiagramDialog.class).exists()
                .displayed()
                .getKeyField()
                .shouldBe(VISIBLE)
                .shouldHave(value("testSkipSubprocess"));
    }
}
