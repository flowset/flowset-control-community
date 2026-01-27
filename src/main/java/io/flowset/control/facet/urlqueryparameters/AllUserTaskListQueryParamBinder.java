/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.facet.urlqueryparameters;

import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.QueryParameters;
import io.flowset.control.entity.filter.ProcessDefinitionFilter;
import io.flowset.control.entity.filter.UserTaskFilter;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.service.processdefinition.ProcessDefinitionLoadContext;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import io.flowset.control.view.alltasks.AssignmentFilterOption;
import io.flowset.control.view.alltasks.UserTaskStateFilterOption;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup;
import io.jmix.flowui.model.InstanceContainer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.flowset.control.view.util.FilterQueryParamUtils.*;

public class AllUserTaskListQueryParamBinder extends AbstractFilterUrlQueryParamBinder {
    public static final String TASK_KEY_PARAM = "taskKey";
    public static final String TASK_NAME_PARAM = "taskName";
    public static final String PROCESS_KEY_PARAM = "processKey";
    public static final String STATE_PARAM = "state";
    public static final String ASSIGNEE_PARAM = "assignee";
    public static final String ASSIGNMENT_PARAM = "assignment";
    public static final String CREATED_AFTER_PARAM = "createdAfter";
    public static final String CREATED_BEFORE_PARAM = "createdBefore";

    protected final InstanceContainer<UserTaskFilter> filterDc;
    protected final Runnable loadDelegate;

    protected final ProcessDefinitionService processDefinitionService;
    protected final JmixRadioButtonGroup<AssignmentFilterOption> assignmentTypeGroup;
    protected final JmixRadioButtonGroup<UserTaskStateFilterOption> stateTypeGroup;
    protected final JmixComboBox<ProcessDefinitionData> processComboBox;

    public AllUserTaskListQueryParamBinder(InstanceContainer<UserTaskFilter> filterDc, Runnable loadDelegate,
                                           ProcessDefinitionService processDefinitionService,
                                           JmixFormLayout filterForm) {
        super();

        this.filterDc = filterDc;
        this.loadDelegate = loadDelegate;
        this.processDefinitionService = processDefinitionService;
        this.assignmentTypeGroup = (JmixRadioButtonGroup<AssignmentFilterOption>) filterForm.getComponent("assignmentTypeGroup");
        this.stateTypeGroup = (JmixRadioButtonGroup<UserTaskStateFilterOption>) filterForm.getComponent("stateTypeGroup");
        this.processComboBox = (JmixComboBox<ProcessDefinitionData>) filterForm.getComponent("processDefinitionLookup");

        addValueChangeListeners(filterForm);
    }

    @Override
    public Component getComponent() {
        return null;
    }

    @Override
    public void updateState(QueryParameters queryParameters) {
        updateGeneralFields(queryParameters);
        updateProcessField(queryParameters);
        updateStateField(queryParameters);
        updateAssignmentFields(queryParameters);
        updateCreationDateFields(queryParameters);

        this.loadDelegate.run();
    }

    protected void updateCreationDateFields(QueryParameters queryParameters) {
        UserTaskFilter item = this.filterDc.getItem();

        OffsetDateTime createdAfter = getOffsetDateTimeParam(queryParameters, CREATED_AFTER_PARAM);
        item.setCreatedAfter(createdAfter);

        OffsetDateTime createdBefore = getOffsetDateTimeParam(queryParameters, CREATED_BEFORE_PARAM);
        item.setCreatedBefore(createdBefore);
    }

    protected void updateGeneralFields(QueryParameters queryParameters) {
        UserTaskFilter item = filterDc.getItem();

        String taskKey = getStringParam(queryParameters, TASK_KEY_PARAM);
        item.setTaskKeyLike(taskKey);

        String taskName = getStringParam(queryParameters, TASK_NAME_PARAM);
        item.setTaskNameLike(taskName);
    }

    protected void updateAssignmentFields(QueryParameters queryParameters) {
        AssignmentFilterOption assignmentParamValue = getSingleParam(queryParameters, ASSIGNMENT_PARAM, AssignmentFilterOption::fromId);

        String assignee = getStringParam(queryParameters, ASSIGNEE_PARAM);
        UserTaskFilter userTaskFilter = filterDc.getItem();

        if (assignmentParamValue != null) {
            assignmentTypeGroup.setValue(assignmentParamValue);
            switch (assignmentParamValue) {
                case ASSIGNED:
                    userTaskFilter.setAssigned(true);
                    userTaskFilter.setUnassigned(null);
                    userTaskFilter.setAssigneeLike(assignee);
                    break;
                case UNASSIGNED:
                    userTaskFilter.setAssigned(null);
                    userTaskFilter.setUnassigned(true);
            }
        } else {
            assignmentTypeGroup.clear();
            userTaskFilter.setAssigned(null);
            userTaskFilter.setUnassigned(null);
            userTaskFilter.setAssigneeLike(assignee);
        }
    }

    protected void updateStateField(QueryParameters queryParameters) {
        UserTaskStateFilterOption stateFilter = getStateFromParam(queryParameters);
        UserTaskFilter userTaskFilter = filterDc.getItem();
        switch (stateFilter) {
            case ACTIVE:
                userTaskFilter.setActive(true);
                userTaskFilter.setSuspended(null);
                break;
            case SUSPENDED:
                userTaskFilter.setActive(null);
                userTaskFilter.setSuspended(true);
                break;
            case ALL:
                userTaskFilter.setActive(null);
                userTaskFilter.setSuspended(null);
                break;
        }
        stateTypeGroup.setValue(stateFilter);
    }

    protected UserTaskStateFilterOption getStateFromParam(QueryParameters queryParameters) {
        String stateParamValue = getStringParam(queryParameters, STATE_PARAM);
        if (stateParamValue == null) {
            return UserTaskStateFilterOption.ALL;
        }
        UserTaskStateFilterOption stateFilter = UserTaskStateFilterOption.fromId(stateParamValue);
        return stateFilter != null ? stateFilter : UserTaskStateFilterOption.ALL;
    }

    protected void updateProcessField(QueryParameters queryParameters) {
        String processKeyParamValue = getStringParam(queryParameters, PROCESS_KEY_PARAM);
        if (StringUtils.isNotEmpty(processKeyParamValue)) {
            //noinspection JmixIncorrectCreateEntity
            ProcessDefinitionFilter filter = new ProcessDefinitionFilter();
            filter.setKey(processKeyParamValue);
            List<ProcessDefinitionData> processes = processDefinitionService.findAll(new ProcessDefinitionLoadContext()
                    .setFilter(filter).setMaxResults(1));

            processComboBox.setValue(CollectionUtils.isNotEmpty(processes) ? processes.get(0) : null);
        }
    }

    @Override
    protected Map<String, List<String>> getEmptyParametersMap() {
        return ImmutableMap.of(
                TASK_KEY_PARAM, Collections.emptyList(),
                TASK_NAME_PARAM, Collections.emptyList(),
                STATE_PARAM, Collections.emptyList(),
                PROCESS_KEY_PARAM, Collections.emptyList(),
                ASSIGNMENT_PARAM, Collections.emptyList(),
                ASSIGNEE_PARAM, Collections.emptyList(),
                CREATED_AFTER_PARAM, Collections.emptyList(),
                CREATED_BEFORE_PARAM, Collections.emptyList()
        );
    }

    protected void addValueChangeListeners(JmixFormLayout filterForm) {
        addGeneralFieldListeners(filterForm);
        addAssignmentFieldListeners(filterForm);
        addCreationDateFieldListeners(filterForm);
    }

    protected void addCreationDateFieldListeners(JmixFormLayout filterForm) {
        Component createdAfterField = filterForm.getComponent("createdAfterField");
        addComponentValueChangeListener(createdAfterField, CREATED_AFTER_PARAM, o -> convertOffsetDateTimeParamValue((OffsetDateTime) o));

        Component createdBeforeField = filterForm.getComponent("createdBeforeField");
        addComponentValueChangeListener(createdBeforeField, CREATED_BEFORE_PARAM, o -> convertOffsetDateTimeParamValue((OffsetDateTime) o));
    }

    protected void addGeneralFieldListeners(JmixFormLayout filterForm) {
        addComponentValueChangeListener(stateTypeGroup, STATE_PARAM, o -> ((UserTaskStateFilterOption) o).getId().toLowerCase());
        addComponentValueChangeListener(processComboBox, PROCESS_KEY_PARAM, o -> ((ProcessDefinitionData) o).getKey());

        Component taskKeyField = filterForm.getComponent("taskKeyLikeField");
        addComponentValueChangeListener(taskKeyField, TASK_KEY_PARAM, o -> (String) o);

        Component taskNameField = filterForm.getComponent("taskNameLikeField");
        addComponentValueChangeListener(taskNameField, TASK_NAME_PARAM, o -> (String) o);
    }

    protected void addAssignmentFieldListeners(JmixFormLayout filterForm) {
        Component assigneeField = filterForm.getComponent("assigneeField");
        addComponentValueChangeListener(assigneeField, ASSIGNEE_PARAM, o -> (String) o);
        assignmentTypeGroup.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                Map<String, String> params = new HashMap<>();

                AssignmentFilterOption value = e.getValue();
                params.put(ASSIGNMENT_PARAM, value != null ? value.getId().toLowerCase() : null);
                if (value == null || value == AssignmentFilterOption.UNASSIGNED) {
                    params.put(ASSIGNEE_PARAM, null);
                }

                updateQueryParams(params);
            }
        });
    }
}
