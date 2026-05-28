/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view;

import com.codeborne.selenide.SelenideElement;
import io.flowset.control.test_support.ui.view.batch.AllBatchListView;
import io.flowset.control.test_support.ui.view.decisiondefinition.DecisionDefinitionListView;
import io.flowset.control.test_support.ui.view.decisioninstance.DecisionInstanceListView;
import io.flowset.control.test_support.ui.view.deployment.DeploymentListView;
import io.flowset.control.test_support.ui.view.engine.BpmEngineListView;
import io.flowset.control.test_support.ui.view.engine.EngineConnectionSettingsView;
import io.flowset.control.test_support.ui.view.incident.IncidentListView;
import io.flowset.control.test_support.ui.view.main.DashboardFragment;
import io.flowset.control.test_support.ui.view.main.EngineStatusBadgeFragment;
import io.flowset.control.test_support.ui.view.processdefinition.ProcessDefinitionListView;
import io.flowset.control.test_support.ui.view.processinstance.ProcessInstanceListView;
import io.flowset.control.test_support.ui.view.user.UserListView;
import io.flowset.control.test_support.ui.view.usertask.AllTasksListView;
import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.ListMenu;
import io.jmix.masquerade.sys.Composite;
import io.jmix.masquerade.sys.View;
import lombok.Getter;

import java.time.Duration;

import static io.flowset.control.test_support.ui.UiTestSupport.MENU_ITEM_OPENING_WAIT_DURATION_SEC;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byUiTestId;
import static io.jmix.masquerade.Masquerade.$j;

/**
 * Wrapper for the main view opening after login.
 * Source view: {@link io.flowset.control.view.main.MainView}
 */
@Getter
@TestView
public class MainView extends View<MainView> {

    @TestComponent(path = "menu")
    private ListMenu listMenu;

    @TestComponent(path = "engineStatusRootBox")
    private EngineStatusBadgeFragment engineStatusBadge;

    @TestComponent(path = "dashboardFragmentDashboardRootBox")
    private DashboardFragment dashboardFragment;

    /**
     * Opens a menu item by its id.
     *
     * @param viewClass view class to open
     * @param menuId    menu item id
     * @param <T>       view class type
     * @return opened view instance
     */
    public <T extends Composite<T>> T openItem(Class<T> viewClass, String menuId) {
        return listMenu.openItem(viewClass, menuId + "ListItem")
                .shouldBe(VISIBLE, Duration.ofSeconds(MENU_ITEM_OPENING_WAIT_DURATION_SEC));
    }

    /**
     * Returns the element with the text title of the view.
     *
     * @return view title element
     */
    public SelenideElement getViewTextTitle() {
        return displayed()
                .getDelegate()
                .find(byUiTestId("viewTitle"))
                .shouldBe(VISIBLE);
    }

    /**
     * Returns the element with the custom title of the view. E.g. component with badges in the title.
     *
     * @return view title element
     */
    public SelenideElement getViewCustomTitle() {
        return displayed()
                .getDelegate()
                .find(byUiTestId("viewTitleDiv"))
                .shouldBe(VISIBLE);
    }

    /**
     * Returns the menu item element by its id.
     *
     * @param menuId menu item id
     * @return menu item element
     */
    public SelenideElement getMenuItem(String menuId) {
        return $j(menuId + "ListItem");
    }

    /**
     * Opens "Administration -> BPM Engines" from the main menu.
     *
     * @return opened BPM Engine list view instance
     */
    public BpmEngineListView openBpmEngineListView() {
        return openItem(BpmEngineListView.class, "bpmEngine.list");
    }

    /**
     * Opens "Administration -> Users" from the main menu.
     *
     * @return opened User list view instance
     */
    public UserListView openUserListView() {
        return openItem(UserListView.class, "user.list");
    }

    /**
     * Opens MAIN -> "Processes" from the main menu.
     *
     * @return opened Process list view instance
     */
    public ProcessDefinitionListView openProcessListView() {
        return openItem(ProcessDefinitionListView.class, "processDefinitions");
    }

    /**
     * Opens DMN -> "Decisions" from the main menu.
     *
     * @return opened Decision list view instance
     */
    public DecisionDefinitionListView openDecisionDefinitionListView() {
        return openItem(DecisionDefinitionListView.class, "decisions");
    }

    /**
     * Opens DMN -> "Decision Instances" from the main menu.
     *
     * @return opened Decision Instance list view instance
     */
    public DecisionInstanceListView openDecisionInstanceListView() {
        return openItem(DecisionInstanceListView.class, "decisionInstances");
    }

    /**
     * Opens MAIN -> "Process Instances" from the main menu.
     *
     * @return opened Process Instance list view instance
     */
    public ProcessInstanceListView openProcessInstanceListView() {
        return openItem(ProcessInstanceListView.class, "processInstances");
    }

    /**
     * Opens MAIN -> "Incidents" from the main menu.
     *
     * @return opened Incident list view instance
     */
    public IncidentListView openIncidentListView() {
        return openItem(IncidentListView.class, "incidents");
    }

    /**
     * Opens MAIN -> "User Tasks" from the main menu.
     *
     * @return opened User Task list view instance
     */
    public AllTasksListView openUserTaskListView() {
        return openItem(AllTasksListView.class, "userTasks");
    }

    /**
     * Opens SYSTEM -> "Deployments" from the main menu.
     *
     * @return opened Deployment list view instance
     */
    public DeploymentListView openDeploymentListView() {
        return openItem(DeploymentListView.class, "deployments");
    }

    /**
     * Opens SYSTEM -> "Batches" from the main menu.
     *
     * @return opened Batch list view instance
     */
    public AllBatchListView openAllBatchListView() {
        return openItem(AllBatchListView.class, "batches");
    }

    /**
     * Opens the engine connection settings view from the engine status badge.
     *
     * @return opened engine connection settings view instance
     */
    public EngineConnectionSettingsView openEngineConnectionView() {
        return engineStatusBadge.openEngineConnectionView()
                .displayed();
    }
}
