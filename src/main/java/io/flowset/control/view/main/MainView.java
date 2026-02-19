/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.main;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Facets;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.app.main.StandardMainView;
import io.jmix.flowui.asynctask.UiAsyncTasks;
import io.jmix.flowui.facet.Timer;
import io.jmix.flowui.kit.component.main.ListMenu;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.flowset.control.entity.EngineConnectionCheckResult;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.event.UserEngineSelectEvent;
import io.flowset.control.property.EngineConnectionCheckProperties;
import io.flowset.control.service.engine.EngineService;
import io.flowset.control.service.engine.EngineUiService;
import io.flowset.control.uicomponent.menu.ControlListMenu;
import io.flowset.control.view.dashboard.DashboardFragment;
import io.flowset.control.view.event.TitleUpdateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@Slf4j
@Route("")
@ViewController("MainView")
@ViewDescriptor("main-view.xml")
public class MainView extends StandardMainView {
    @ViewComponent
    protected H1 viewTitle;
    @ViewComponent
    protected Div viewTitleDiv;

    @Autowired
    protected Fragments fragments;
    @ViewComponent
    protected InstanceContainer<EngineConnectionCheckResult> engineConnectionStatusDc;
    @Autowired
    protected EngineService engineService;
    @Autowired
    protected EngineUiService engineUiService;

    @Autowired
    protected DialogWindows dialogWindows;
    @ViewComponent
    protected MessageBundle messageBundle;
    @Autowired
    protected Facets facets;

    @ViewComponent
    protected Timer connectionCheckTimer;

    @Autowired
    protected EngineConnectionCheckProperties checkProperties;

    @ViewComponent
    protected Anchor baseLink;

    @Autowired
    protected UiAsyncTasks uiAsyncTasks;

    @ViewComponent
    protected InstanceContainer<BpmEngine> selectedEngineDc;

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected ViewNavigationSupport viewNavigationSupport;

    @ViewComponent
    protected Header header;

    @ViewComponent
    protected ControlListMenu menu;

    protected EngineStatusBadgeFragment engineStatusFragment;

    protected AtomicBoolean statusCheckRunning = new AtomicBoolean(false);

    @Subscribe
    public void onQueryParametersChange(final QueryParametersChangeEvent event) {
        viewTitleDiv.removeAll();
    }

    @Subscribe
    public void onInit(final InitEvent event) {
        BpmEngine selectedEngine = engineService.getSelectedEngine();
        selectedEngineDc.setItem(selectedEngine);

        initMenu();
        initBaseLink();
        initConnectionCheckTimer();
        initInitialLayout();
        initEngineStatusFragment();
    }


    @Subscribe
    public void onReady(final ReadyEvent event) {
        updateEngineStatusManually();
    }

    @EventListener
    protected void onTitleUpdated(TitleUpdateEvent event) {
        String title = event.getTitle();
        viewTitle.setText(title);

        Component titleComponent = event.getSuffixComponent();
        if (titleComponent != null) {
            viewTitleDiv.removeAll();
            viewTitleDiv.add(titleComponent);
        }
    }

    @EventListener
    protected void onUserEngineSelectEvent(UserEngineSelectEvent event) {
        notifications.create(messageBundle.formatMessage("engineChanged", event.getEngine().getName()),
                        messageBundle.formatMessage("engineChanged.description", event.getEngine().getBaseUrl()))
                .withPosition(Notification.Position.TOP_END)
                .withThemeVariant(NotificationVariant.LUMO_PRIMARY)
                .withDuration(4000)
                .show();

        selectedEngineDc.setItem(event.getEngine());

        updateEngineStatusManually();

        View<?> currentView = getCurrentView();
        if (currentView == this) {
            refreshDashboard();
        } else {
            viewNavigationSupport.navigate(getClass());
        }

    }

    protected void refreshDashboard() {
        Component initialLayout = getInitialLayout();
        if (initialLayout != null) {
            Component component = initialLayout.getChildren()
                    .findFirst()
                    .orElse(null);
            if (component instanceof DashboardFragment dashboardFragment) {
                dashboardFragment.updateDashboard();
            }
        }
    }

    protected void initMenu() {
        menu.addMenuItemBefore(new ControlListMenu.GroupLabelMenuItem("mainLabel")
                .withTitle(messageBundle.getMessage("menu.mainGroup.label")), "dashboard");

        menu.addMenuItemBefore(new ControlListMenu.GroupLabelMenuItem("dmnLabel")
                .withTitle(messageBundle.getMessage("menu.dmnGroup.label")), "decisions");

        menu.addMenuItemBefore(new ControlListMenu.GroupLabelMenuItem("systemLabel")
                .withTitle(messageBundle.getMessage("menu.systemGroup.label")), "deployments");

        menu.addMenuItemBefore(new ControlListMenu.GroupLabelMenuItem("supportLabel")
                        .withTitle(messageBundle.getMessage("menu.supportGroup.label")),
                "about");

        ListMenu.MenuItem decisionsMenu = menu.getMenuItem("decisions");
        if (decisionsMenu != null) {
            decisionsMenu.setPrefixComponent(new SvgIcon("icons/table.svg"));
        }

        ListMenu.MenuItem decisionInstancesMenu = menu.getMenuItem("decisionInstances");
        if (decisionInstancesMenu != null) {
            decisionInstancesMenu.setPrefixComponent(new SvgIcon("icons/table_view.svg"));
        }

        ListMenu.MenuItem dashboardMenu = menu.getMenuItem("dashboard");
        if (dashboardMenu != null) {
            dashboardMenu.setPrefixComponent(new SvgIcon("icons/dashboard.svg"));
        }
    }

    protected void initInitialLayout() {
        Component initialLayout = getInitialLayout();
        if (initialLayout instanceof HasComponents container) {
            initialLayout.addAttachListener(attachEvent -> {
                if (container.getElement().getChildCount() == 0) {
                    initDashboard(container);
                } else {
                    Component component = initialLayout.getChildren()
                            .findFirst()
                            .orElse(null);
                    if (component instanceof DashboardFragment dashboardFragment) {
                        dashboardFragment.updateDashboard();
                    }
                }
            });
        }
    }

    protected void initBaseLink() {
        SvgIcon logoIcon = new SvgIcon("icons/logo.svg");
        logoIcon.addClassNames("logo-icon");
        baseLink.addComponentAsFirst(logoIcon);
    }

    protected void initDashboard(HasComponents container) {
        DashboardFragment dashboardFragment = fragments.create(this, DashboardFragment.class);
        container.add(dashboardFragment);
    }

    protected void initConnectionCheckTimer() {
        connectionCheckTimer.setDelay(checkProperties.getIntervalSec() * 1000);
    }

    protected void initEngineStatusFragment() {
        boolean engineStatusFragmentExists = header.getChildren().anyMatch(component -> component instanceof EngineStatusBadgeFragment);
        if (!engineStatusFragmentExists) {
            this.engineStatusFragment = fragments.create(this, EngineStatusBadgeFragment.class);
            header.add(engineStatusFragment);
        }
    }

    @Subscribe("connectionCheckTimer")
    public void onConnectionCheckTimerTimerAction(final Timer.TimerActionEvent event) {
        BpmEngine selectedEngine = engineService.getSelectedEngine();
        selectedEngineDc.setItem(selectedEngine);

        if (selectedEngine == null) {
            handleNoEngineSelected();
            return;
        }

        if (statusCheckRunning.get()) { //skip the check if the previous one is in progress
            return;
        }

        runEngineStatusCheckAsync(selectedEngine,
                result -> {
                    statusCheckRunning.set(false);
                    handleCheckResult(result);
                }, throwable -> {
                    statusCheckRunning.set(false);
                    handleErrorCheckResult();
                });

        statusCheckRunning.set(true);
    }


    protected void updateEngineStatusManually() {
        connectionCheckTimer.stop();

        BpmEngine bpmEngine = selectedEngineDc.getItemOrNull();
        if (bpmEngine == null) {
            handleNoEngineSelected();
            refreshDashboard();
            connectionCheckTimer.start();
            return;
        }

        runEngineStatusCheckAsync(bpmEngine,
                result -> {
                    handleCheckResult(result);
                    refreshDashboard();
                    connectionCheckTimer.start();
                }, throwable -> {
                    handleErrorCheckResult();
                    refreshDashboard();
                    connectionCheckTimer.start();
                });
    }

    protected void runEngineStatusCheckAsync(BpmEngine engine,
                                             Consumer<EngineConnectionCheckResult> successHandler,
                                             Consumer<Throwable> errorHandler) {

        uiAsyncTasks.supplierConfigurer(() -> engineUiService.checkConnection(engine))
                .withResultHandler(successHandler)
                .withExceptionHandler(ex -> {
                    log.error("Unable to check engine status", ex);
                    errorHandler.accept(ex);
                })
                .supplyAsync();


    }

    protected void handleNoEngineSelected() {
        engineStatusFragment.setNoSelectedEngineStatus();
    }

    protected void handleCheckResult(EngineConnectionCheckResult result) {
        engineConnectionStatusDc.setItem(result);
        engineStatusFragment.updateConnectionStatusComponents();
    }

    protected void handleErrorCheckResult() {
        engineStatusFragment.setFailedConnectionStatus();
    }
}
