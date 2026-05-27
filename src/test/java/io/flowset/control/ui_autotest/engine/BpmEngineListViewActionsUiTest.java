/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.engine;

import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.test_support.AbstractUiTest;
import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.flowset.control.test_support.ui.component.JmixDialog;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.engine.BpmEngineDetailView;
import io.flowset.control.test_support.ui.view.engine.BpmEngineListView;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static io.flowset.control.test_support.ui.condition.ControlCondition.anyBodyRowHaveCellElementText;
import static io.flowset.control.test_support.ui.condition.ControlCondition.emptyGrid;
import static io.flowset.control.test_support.ui.view.engine.BpmEngineListView.*;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@Slf4j
@DisplayName("Actions on BPM engine list view")
public class BpmEngineListViewActionsUiTest extends AbstractUiTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("delete from CONTROL_BPM_ENGINE where NAME like '%test-engine-%'");
    }

    @Test
    @DisplayName("Refresh action availability on BPM engine list view")
    void givenLoggedInUser_whenOpenEngineList_thenRefreshActionEnabledRuleApplied() {
        // given
        MainView mainView = loginAsAdmin();

        //when
        BpmEngineListView listView = mainView.openBpmEngineListView();

        //then
        listView.getRefreshButton()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        listView.openBpmEngineGridContextActions()
                .find(text("Refresh"))
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Refresh action reloads the BPM engine grid after a new engine is added")
    void givenNewlyCreatedBpmEngine_whenClickRefresh_thenGridReloaded() {
        // given
        MainView mainView = loginAsAdmin();
        BpmEngineListView listView = mainView.openBpmEngineListView();

        // when
        String engineName = "test-engine-" + System.currentTimeMillis();
        controlTestDataCreator.createRandomBpmEngine(engineName, true);

        listView.getRefreshButton().click();

        // then
        listView.getBpmEnginesDataGrid()
                .shouldHave(anyBodyRowHaveCellElementText(NAME_COLUMN_INDEX, NAME_BUTTON_BY, engineName));
    }

    @Test
    @DisplayName("Create action opens the BPM engine detail view for a new entity")
    void givenLoggedInUser_whenClickCreate_thenBpmEngineDetailViewOpened() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        BpmEngineListView listView = mainView.openBpmEngineListView();
        listView.getCreateButton().click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/engines/new"));

        $j(BpmEngineDetailView.class).exists()
                .getNameField()
                .shouldBe(VISIBLE)
                .shouldHave(value(""));

        mainView.getViewTextTitle()
                .shouldHave(text("New BPM engine"));
    }

    @Test
    @DisplayName("Double-clicking a BPM engine row opens the BPM engine detail view")
    void givenExistingBpmEngine_whenRowDoubleClicked_thenBpmEngineDetailViewOpened() {
        // given
        String engineName = "test-engine-" + System.currentTimeMillis();
        BpmEngine engine = controlTestDataCreator.createRandomBpmEngine(engineName, false);

        MainView mainView = loginAsAdmin();

        // when
        BpmEngineListView listView = mainView.openBpmEngineListView();
        listView.getRowByEngineName(engineName)
                .getCellByIndex(BASE_URL_COLUMN_INDEX)
                .getCellContent()
                .doubleClick();

        // then
        webdriver().shouldHave(urlContaining("/bpm/engines/" + engine.getId()));

        $j(BpmEngineDetailView.class).exists()
                .displayed();
    }

    @Test
    @DisplayName("Remove action availability on BPM engine list view")
    void givenExistingBpmEngine_whenOpenEngineList_thenRemoveActionEnabledRuleApplied() {
        // given
        String engineName = "test-engine-" + System.currentTimeMillis();
        controlTestDataCreator.createRandomBpmEngine(engineName, false);

        MainView mainView = loginAsAdmin();

        //when
        BpmEngineListView listView = mainView.openBpmEngineListView();

        //then
        listView.getRemoveButton()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        GridContextMenu gridContextMenu = listView.openBpmEngineGridContextActions();
        gridContextMenu.find(text("Remove"))
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        gridContextMenu.close();

        listView.getBpmEnginesDataGrid().clickSelectAll();

        listView.getRemoveButton()
                .shouldBe(ENABLED);

        listView.openBpmEngineGridContextActions()
                .find(text("Remove"))
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Remove action: engine is removed from the grid after confirmation")
    void givenExistingBpmEngine_whenRemoveConfirmed_thenEngineRemovedFromGrid() {
        // given
        String engineName = "test-engine-" + System.currentTimeMillis();
        controlTestDataCreator.createRandomBpmEngine(engineName, false);

        MainView mainView = loginAsAdmin();
        BpmEngineListView listView = mainView.openBpmEngineListView();

        // when
        listView.getBpmEnginesDataGrid().clickSelectAll();
        listView.getRemoveButton().click();

        $j(JmixDialog.class, JmixDialog.OVERLAY)
                .exists()
                .displayed()
                .getYesBtn()
                .click();

        // then
        listView.getBpmEnginesDataGrid()
                .shouldBe(VISIBLE)
                .shouldBe(emptyGrid);
    }

    @Test
    @DisplayName("Remove action: engine is not removed from the grid after cancellation")
    void givenExistingBpmEngine_whenRemoveCancelled_thenEngineNotRemoved() {
        // given
        String engineName = "test-engine-" + System.currentTimeMillis();
        controlTestDataCreator.createRandomBpmEngine(engineName, false);

        MainView mainView = loginAsAdmin();
        BpmEngineListView listView = mainView.openBpmEngineListView();

        // when
        listView.getBpmEnginesDataGrid().clickSelectAll();
        listView.getRemoveButton().click();

        $j(JmixDialog.class, JmixDialog.OVERLAY)
                .exists()
                .displayed()
                .getNoBtn()
                .click();

        // then
        listView.getBpmEnginesDataGrid()
                .shouldBe(VISIBLE)
                .shouldHave(anyBodyRowHaveCellElementText(NAME_COLUMN_INDEX, NAME_BUTTON_BY, engineName));
    }


    @Test
    @DisplayName("Mark as default action availability on BPM engine list view")
    void givenExistingBpmEngines_whenOpenEngineList_thenMarkAsDefaultActionAvailabilityRulesApplied() {
        // given
        String defaultEngineName = "default-test-engine-" + System.currentTimeMillis();
        controlTestDataCreator.createRandomBpmEngine(defaultEngineName, true);

        String nonDefaultEngineName = "non-default-test-engine-" + System.currentTimeMillis();
        controlTestDataCreator.createRandomBpmEngine(nonDefaultEngineName, false);

        MainView mainView = loginAsAdmin();

        // when
        BpmEngineListView listView = mainView.openBpmEngineListView();

        // then
        listView.getRowByEngineName(defaultEngineName)
                .getCellByIndex(ACTIONS_COLUMN_INDEX)
                .getCellContent()
                .find(MARK_AS_DEFAULT_BUTTON_BY)
                .shouldNotBe(VISIBLE);

        listView.getRowByEngineName(nonDefaultEngineName)
                .getCellByIndex(ACTIONS_COLUMN_INDEX)
                .getCellContent()
                .find(MARK_AS_DEFAULT_BUTTON_BY)
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Mark as default action: confirming the dialog promotes the engine to default")
    void givenExistingBpmEngine_whenMarkAsDefaultConfirmed_thenEngineBecomesDefault() {
        // given
        String defaultEngineName = "default-test-engine-" + System.currentTimeMillis();
        controlTestDataCreator.createRandomBpmEngine(defaultEngineName, true);

        String nonDefaultEngineName = "non-default-test-engine-" + System.currentTimeMillis();
        controlTestDataCreator.createRandomBpmEngine(nonDefaultEngineName, false);

        MainView mainView = loginAsAdmin();
        BpmEngineListView listView = mainView.openBpmEngineListView();

        // when
        listView.getRowByEngineName(nonDefaultEngineName)
                .getCellByIndex(ACTIONS_COLUMN_INDEX)
                .getCellContent()
                .find(MARK_AS_DEFAULT_BUTTON_BY).click();

        JmixDialog markAsDefaultDialog = $j(JmixDialog.class, JmixDialog.OVERLAY)
                .exists()
                .displayed();
        markAsDefaultDialog.getDelegate()
                .shouldHave(text(String.format("Are you sure you want to mark the \"%s\" engine as default?", nonDefaultEngineName)));
        markAsDefaultDialog.getOkBtn().click();

        // then
        listView.getRowByEngineName(nonDefaultEngineName)
                .getCellByIndex(ACTIONS_COLUMN_INDEX)
                .getCellContent()
                .find(MARK_AS_DEFAULT_BUTTON_BY)
                .shouldNotBe(VISIBLE);

        listView.getRowByEngineName(defaultEngineName)
                .getCellByIndex(ACTIONS_COLUMN_INDEX)
                .getCellContent()
                .find(MARK_AS_DEFAULT_BUTTON_BY)
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Mark as default action: cancelling the dialog leaves the default engine unchanged")
    void givenExistingBpmEngine_whenMarkAsDefaultCancelled_thenDefaultEngineUnchanged() {
        // given
        String defaultEngineName = "default-test-engine-" + System.currentTimeMillis();
        controlTestDataCreator.createRandomBpmEngine(defaultEngineName, true);

        String nonDefaultEngineName = "non-default-test-engine-" + System.currentTimeMillis();
        controlTestDataCreator.createRandomBpmEngine(nonDefaultEngineName, false);

        MainView mainView = loginAsAdmin();
        BpmEngineListView listView = mainView.openBpmEngineListView();

        // when
        listView.getRowByEngineName(nonDefaultEngineName)
                .getCellByIndex(ACTIONS_COLUMN_INDEX)
                .getCellContent()
                .find(MARK_AS_DEFAULT_BUTTON_BY).click();

        $j(JmixDialog.class, JmixDialog.OVERLAY)
                .exists()
                .displayed()
                .getCancelBtn()
                .click();

        // then
        listView.getRowByEngineName(defaultEngineName)
                .getCellByIndex(ACTIONS_COLUMN_INDEX)
                .getCellContent()
                .find(MARK_AS_DEFAULT_BUTTON_BY)
                .shouldNotBe(VISIBLE);

        listView.getRowByEngineName(nonDefaultEngineName)
                .getCellByIndex(ACTIONS_COLUMN_INDEX)
                .getCellContent()
                .find(MARK_AS_DEFAULT_BUTTON_BY)
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("View action in the Name column view opens detail view")
    void givenExistingBpmEngine_whenOpenEngineList_thenViewActionOpensDetailView() {
        // given
        String engineName = "non-default-test-engine-" + System.currentTimeMillis();
        BpmEngine engine = controlTestDataCreator.createRandomBpmEngine(engineName, false);

        MainView mainView = loginAsAdmin();

        // when
        BpmEngineListView listView = mainView.openBpmEngineListView();

        // then
        listView.getRowByEngineName(engineName)
                .getCellByIndex(NAME_COLUMN_INDEX)
                .getCellContent()
                .find(NAME_BUTTON_BY).click();

        webdriver().shouldHave(urlContaining("/bpm/engines/" + engine.getId()));

        $j(BpmEngineDetailView.class).exists()
                .displayed();
    }

    @Test
    @DisplayName("List of available actions on BPM engine list view")
    void givenExistingBpmEngine_whenOpenEngineList_thenAllActionsAvailable() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        BpmEngineListView listView = mainView.openBpmEngineListView();

        // then
        listView.getRefreshButton().shouldBe(VISIBLE);
        listView.getCreateButton().shouldBe(VISIBLE);
        listView.getRemoveButton().shouldBe(VISIBLE);


        listView.openBpmEngineGridContextActions()
                .shouldHave(visibleItems("Refresh", "Create", "Remove"));
    }
}
