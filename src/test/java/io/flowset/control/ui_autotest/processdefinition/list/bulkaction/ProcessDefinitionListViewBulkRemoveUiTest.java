/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processdefinition.list.bulkaction;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processdefinition.ProcessDefinitionListView;
import io.flowset.control.test_support.ui.view.processdefinition.action.BulkDeleteProcessDefinitionView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.text;
import static io.flowset.control.test_support.ui.condition.ControlCondition.emptyGrid;
import static io.flowset.control.test_support.ui.condition.ControlCondition.visibleBodyRowCount;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Bulk Remove action on Process list view")
public class ProcessDefinitionListViewBulkRemoveUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Remove action enabled if row selected in data grid")
    void givenExistingProcessDefinition_whenRowSelectedInGrid_thenRemoveActionEnabled() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();

        // then
        listView.getBulkRemoveBtn()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        GridContextMenu gridContextMenu = listView.openProcessGridContextActions();
        gridContextMenu.find(text("Remove"))
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        gridContextMenu.close();

        listView.getProcessDefinitionsGrid().clickSelectAll();

        listView.getBulkRemoveBtn()
                .shouldBe(ENABLED);

        listView.openProcessGridContextActions()
                .find(text("Remove"))
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Remove action: selected processes are not removed from the grid after cancellation")
    void givenExistingProcessDefinition_whenBulkRemoveCancelled_thenProcessNotRemoved() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView()
                .waitUntilDataLoading();
        listView.getProcessDefinitionsGrid().clickSelectAll();
        listView.getBulkRemoveBtn().click();

        BulkDeleteProcessDefinitionView dialog = $j(BulkDeleteProcessDefinitionView.class).exists();

        dialog.getCancelBtn()
                .shouldBe(VISIBLE).click();

        // then
        listView.waitUntilDataLoading()
                .getProcessDefinitionsGrid()
                .shouldNotBe(emptyGrid);

        listView.getProcessDefinitionsGrid()
                .shouldHave(visibleBodyRowCount(1));
    }

    @Test
    @DisplayName("Remove action: selected processes are removed from the grid after confirmation")
    void givenMultipleProcessDefinitions_whenBulkRemoveConfirmed_thenAllSelectedProcessesRemoved() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .deploy("test_support/contractApproval.bpmn");
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView()
                .waitUntilDataLoading();
        listView.getProcessDefinitionsGrid().clickSelectAll();
        listView.getBulkRemoveBtn().click();

        BulkDeleteProcessDefinitionView deleteProcessView = $j(BulkDeleteProcessDefinitionView.class)
                .exists()
                .shouldBe(VISIBLE);

        deleteProcessView.getOkBtn()
                .shouldBe(VISIBLE).click();

        // then
        listView.waitUntilDataLoading()
                .getProcessDefinitionsGrid()
                .shouldBe(emptyGrid);
    }
}
