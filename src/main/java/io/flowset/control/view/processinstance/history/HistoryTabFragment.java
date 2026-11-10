/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processinstance.history;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.entity.engine.EngineType;
import io.flowset.control.service.engine.EngineService;
import io.flowset.control.view.processinstance.LazyTabContent;
import io.flowset.control.view.processinstance.event.*;
import io.flowset.control.view.util.ComponentHelper;
import io.jmix.core.Metadata;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.flowset.control.entity.filter.*;
import io.flowset.control.service.decisioninstance.DecisionInstanceService;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.service.activity.ActivityService;
import io.flowset.control.service.incident.IncidentService;
import io.flowset.control.service.usertask.UserTaskService;
import io.flowset.control.service.variable.VariableService;
import io.flowset.control.view.processinstance.event.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@FragmentDescriptor("history-tab-fragment.xml")
public class HistoryTabFragment extends Fragment<JmixTabSheet> {
    public static final String USER_TASKS_TAB_ID = "historyTasksTab";
    public static final String VARIABLES_TAB_ID = "historyVariablesTab";
    public static final String INCIDENTS_TAB_ID = "historyIncidentsTab";
    public static final String DECISIONS_TAB_ID = "historyDecisionsTab";

    public static final int ACTIVITIES_TAB_IDX = 0;

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected Fragments fragments;
    @ViewComponent
    protected MessageBundle messageBundle;

    @Autowired
    protected ActivityService activityService;
    @Autowired
    protected VariableService variableService;
    @Autowired
    protected UserTaskService userTaskService;
    @Autowired
    protected IncidentService incidentService;
    @Autowired
    protected DecisionInstanceService decisionInstanceService;

    @Autowired
    protected ComponentHelper componentHelper;

    @Autowired
    protected EngineService engineService;

    @ViewComponent
    protected InstanceContainer<ProcessInstanceData> processInstanceDataDc;

    @ViewComponent
    protected JmixTabSheet historyTabsheet;

    @ViewComponent
    protected ActivitiesTabFragment activitiesFragment;

    protected boolean initialized = false;
    protected List<HistoryTabType> historyTabTypes = new ArrayList<>();

    @Subscribe
    public void onReady(ReadyEvent event) {
        EngineType engineType = engineService.getSelectedEngineType();
        historyTabTypes = resolveTabTypes(engineType);
        for (int i = 0; i < historyTabTypes.size(); i++) {
            initTab(historyTabTypes.get(i), i);
        }
    }

    public void refresh() {
        if (!initialized) {
            loadAndUpdateActivitiesCount();
            loadAndUpdateUserTasksCount();
            loadAndUpdateVariablesCount();
            loadAndUpdateIncidentsCount();
            loadAndUpdateDecisionsCount();
            this.initialized = true;
        }
        historyTabsheet.setSelectedIndex(ACTIVITIES_TAB_IDX);
        activitiesFragment.refreshIfRequired();
    }

    @Subscribe("historyTabsheet")
    public void onHistoryTabsheetSelectedChange(final JmixTabSheet.SelectedChangeEvent event) {
        Tab selectedTab = event.getSelectedTab();
        Component tabContent = getTabContent(selectedTab);
        if (tabContent instanceof HasRefresh tabContentFragment) {
            tabContentFragment.refreshIfRequired();
        }
    }

    @EventListener
    public void handleActivityCountUpdate(HistoryActivityCountUpdateEvent event) {
        updateActivityTabCaption(event.getCount());
    }

    @EventListener
    public void handleUserTaskCountUpdate(HistoryUserTaskCountUpdateEvent event) {
        updateUserTasksTabCaption(event.getCount());
    }

    @EventListener
    public void handleVariableCountUpdate(HistoryVariableCountUpdateEvent event) {
        updateVariablesTabCaption(event.getCount());
    }

    @EventListener
    public void handleIncidentUpdate(IncidentUpdateEvent event) {
        loadAndUpdateIncidentsCount();
    }

    @EventListener
    public void handleJobRetriesUpdate(JobRetriesUpdateEvent event) {
        loadAndUpdateIncidentsCount();
    }

    @EventListener
    public void handleIncidentCountUpdate(HistoryIncidentCountUpdateEvent event) {
        updateIncidentTabCaption(event.getCount());
    }

    @EventListener
    public void handleDecisionCountUpdate(DecisionCountUpdateEvent event) {
        updateDecisionsTabCaption(event.getCount());
    }

    protected void loadAndUpdateActivitiesCount() {
        if (historyTabTypes.contains(HistoryTabType.ACTIVITIES)) {
            ActivityFilter activityFilter = metadata.create(ActivityFilter.class);
            activityFilter.setProcessInstanceId(processInstanceDataDc.getItem().getId());

            long activitiesCount = activityService.getHistoryActivitiesCount(activityFilter);

            updateActivityTabCaption(activitiesCount);
        }
    }

    protected void loadAndUpdateVariablesCount() {
        if (historyTabTypes.contains(HistoryTabType.VARIABLES)) {
            VariableFilter variableFilter = metadata.create(VariableFilter.class);
            variableFilter.setProcessInstanceId(processInstanceDataDc.getItem().getId());

            long variablesCount = variableService.getHistoricVariablesCount(variableFilter);

            updateVariablesTabCaption(variablesCount);
        }
    }

    protected void loadAndUpdateIncidentsCount() {
        if (historyTabTypes.contains(HistoryTabType.INCIDENTS)) {
            IncidentFilter incidentFilter = metadata.create(IncidentFilter.class);
            incidentFilter.setProcessInstanceId(processInstanceDataDc.getItem().getId());

            long incidentCount = incidentService.getHistoricIncidentCount(incidentFilter);

            updateIncidentTabCaption(incidentCount);
        }
    }

    protected void loadAndUpdateUserTasksCount() {
        if (historyTabTypes.contains(HistoryTabType.USER_TASKS)) {
            UserTaskFilter userTaskFilter = metadata.create(UserTaskFilter.class);
            userTaskFilter.setProcessInstanceId(processInstanceDataDc.getItem().getId());

            long historyTasksCount = userTaskService.getHistoryTasksCount(userTaskFilter);

            updateUserTasksTabCaption(historyTasksCount);
        }
    }

    protected void loadAndUpdateDecisionsCount() {
        if (historyTabTypes.contains(HistoryTabType.DECISIONS)) {
            DecisionInstanceFilter decisionInstanceFilter = metadata.create(DecisionInstanceFilter.class);
            decisionInstanceFilter.setProcessInstanceId(processInstanceDataDc.getItem().getId());

            long decisionsCount = decisionInstanceService.getHistoryDecisionInstancesCount(decisionInstanceFilter);
            updateDecisionsTabCaption(decisionsCount);
        }
    }

    /**
     * Returns the history tabs shown for the engine type, in the order of the tab sheet.
     *
     * @param engineType the type of the selected engine, or {@code null} if no engine is selected
     * @return the tab types
     */
    protected List<HistoryTabType> resolveTabTypes(@Nullable EngineType engineType) {
        List<HistoryTabType> types = new ArrayList<>(List.of(HistoryTabType.ACTIVITIES, HistoryTabType.USER_TASKS,
                HistoryTabType.VARIABLES));
        if (engineType == null || engineType.isCamunda7Compatible()) {
            types.add(HistoryTabType.INCIDENTS);
        }
        types.add(HistoryTabType.DECISIONS);
        return types;
    }

    protected void initTab(HistoryTabType type, int idx) {
        switch (type) {
            case ACTIVITIES -> {
                // Declared in the descriptor.
            }
            case USER_TASKS -> initUserTasksTab(idx);
            case VARIABLES -> initVariablesTab(idx);
            case INCIDENTS -> initIncidentsTab(idx);
            case DECISIONS -> initDecisionsTab(idx);
        }
    }

    @SuppressWarnings("JmixIncorrectCreateGuiComponent")
    protected void initUserTasksTab(int tabIndex) {
        Tab userTasksTab = createTab(USER_TASKS_TAB_ID, "ProcessInstanceEditHistoryFragment.tasksTabCaption",
                VaadinIcon.USER_CARD);
        historyTabsheet.add(userTasksTab, componentHelper.createLazyTabContent(() ->
                fragments.create(getParentController(), HistoryUserTasksTabFragment.class)), tabIndex);
    }

    @SuppressWarnings("JmixIncorrectCreateGuiComponent")
    protected void initVariablesTab(int tabIndex) {
        Tab variablesTab = createTab(VARIABLES_TAB_ID, "ProcessInstanceEditHistoryFragment.historicVariableInstancesTabCaption",
                VaadinIcon.COGS);
        historyTabsheet.add(variablesTab, componentHelper.createLazyTabContent(() ->
                fragments.create(getParentController(), HistoryVariablesTabFragment.class)), tabIndex);
    }

    @SuppressWarnings("JmixIncorrectCreateGuiComponent")
    protected void initIncidentsTab(int tabIndex) {
        Tab incidentsTab = createTab(INCIDENTS_TAB_ID, "ProcessInstanceEditHistoryFragment.incidentsTabCaption",
                VaadinIcon.WARNING);
        historyTabsheet.add(incidentsTab, componentHelper.createLazyTabContent(() ->
                fragments.create(getParentController(), HistoryIncidentsTabFragment.class)), tabIndex);
    }

    protected void initDecisionsTab(int tabIndex) {
        Tab decisionsTab = createTab(DECISIONS_TAB_ID, "ProcessInstanceEditHistoryFragment.decisionsTabCaption",
                VaadinIcon.TABLE);
        historyTabsheet.add(decisionsTab, componentHelper.createLazyTabContent(() ->
                fragments.create(getParentController(), HistoryDecisionsFragment.class)), tabIndex);
    }

    protected void updateUserTasksTabCaption(long userTasksCount) {
        int tabIndex = historyTabTypes.indexOf(HistoryTabType.USER_TASKS);
        if (tabIndex != -1) {
            updateTabCaption(tabIndex,
                    "ProcessInstanceEditHistoryFragment.tasksTabCaption",
                    userTasksCount, VaadinIcon.USER_CARD.create());
        }
    }

    protected void updateVariablesTabCaption(long variablesCount) {
        int tabIndex = historyTabTypes.indexOf(HistoryTabType.VARIABLES);
        if (tabIndex != -1) {
            updateTabCaption(tabIndex,
                    "ProcessInstanceEditHistoryFragment.historicVariableInstancesTabCaption",
                    variablesCount, VaadinIcon.CURLY_BRACKETS.create());
        }
    }

    protected void updateIncidentTabCaption(long incidentCount) {
        int tabIndex = historyTabTypes.indexOf(HistoryTabType.INCIDENTS);
        if (tabIndex != -1) {
            updateTabCaption(tabIndex,
                    "ProcessInstanceEditHistoryFragment.incidentsTabCaption",
                    incidentCount, VaadinIcon.WARNING.create());
        }
    }

    protected void updateDecisionsTabCaption(long decisionsCount) {
        int tabIndex = historyTabTypes.indexOf(HistoryTabType.DECISIONS);
        if (tabIndex != -1) {
            SvgIcon svgIcon = new SvgIcon("icons/table_view.svg");
            svgIcon.addClassNames(LumoUtility.IconSize.MEDIUM, LumoUtility.Padding.XSMALL);
            updateTabCaption(tabIndex,
                    "ProcessInstanceEditHistoryFragment.decisionsTabCaption",
                    decisionsCount, svgIcon);
        }
    }

    protected void updateActivityTabCaption(long activitiesCount) {
        updateTabCaption(ACTIVITIES_TAB_IDX,
                "ProcessInstanceEditHistoryFragment.historicActivityInstancesTabCaption",
                activitiesCount, VaadinIcon.CUBES.create());
    }

    protected void updateTabCaption(int tabIndex, String messageKey, long count, Component icon) {
        historyTabsheet.getTabAt(tabIndex).setLabel(messageBundle.formatMessage(messageKey, count));
        historyTabsheet.getTabAt(tabIndex).addComponentAsFirst(icon);
    }

    protected Tab createTab(String id, String messageKey, VaadinIcon icon) {
        Tab tab = uiComponents.create(Tab.class);
        tab.setId(id);
        tab.setLabel(messageBundle.formatMessage(messageKey, 0));
        tab.addComponentAsFirst(icon.create());
        return tab;
    }

    @Nullable
    protected Component getTabContent(Tab tab) {
        Component contentByTab = historyTabsheet.getContentByTab(tab);
        if (contentByTab instanceof LazyTabContent lazyTabContent) {
            return lazyTabContent.getContent();
        }
        return contentByTab != null
                ? contentByTab.getChildren()
                .findFirst()
                .orElse(null)
                : null;
    }

}
