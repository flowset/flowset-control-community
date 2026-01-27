/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processinstance.filter;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.flowset.control.entity.filter.ProcessInstanceFilter;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.entity.processinstance.ProcessInstanceState;
import io.flowset.control.facet.urlqueryparameters.HasFilterUrlParamHeaderFilter;
import io.flowset.control.view.processinstance.ProcessInstanceViewMode;
import io.flowset.control.view.util.ComponentHelper;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.flowset.control.facet.urlqueryparameters.ProcessInstanceListQueryParamBinder.HAS_INCIDENTS_FILTER_PARAM;
import static io.flowset.control.facet.urlqueryparameters.ProcessInstanceListQueryParamBinder.STATE_FILTER_PARAM;
import static io.flowset.control.view.util.FilterQueryParamUtils.getBooleanParam;
import static io.flowset.control.view.util.FilterQueryParamUtils.getSingleParam;

public class ProcessInstanceStateHeaderFilter extends ProcessInstanceDataGridHeaderFilter implements HasFilterUrlParamHeaderFilter {
    private JmixComboBox<ProcessInstanceState> stateComboBox;
    private JmixCheckbox withIncidentsCheckBox;
    private ComponentHelper componentHelper;
    private ProcessInstanceViewMode viewMode;

    public ProcessInstanceStateHeaderFilter(DataGrid<ProcessInstanceData> dataGrid, DataGridColumn<ProcessInstanceData> column,
                                            InstanceContainer<ProcessInstanceFilter> filterDc) {
        super(dataGrid, column, filterDc);
        this.viewMode = ProcessInstanceViewMode.ACTIVE;
    }


    @Autowired
    public void setComponentHelper(ComponentHelper componentHelper) {
        this.componentHelper = componentHelper;
    }

    @Override
    protected Component createFilterComponent() {
        VerticalLayout verticalLayout = uiComponents.create(VerticalLayout.class);
        verticalLayout.setPadding(false);

        stateComboBox = uiComponents.create(JmixComboBox.class);
        stateComboBox.setItems(List.of(ProcessInstanceState.ACTIVE, ProcessInstanceState.SUSPENDED));
        stateComboBox.setRenderer(new ComponentRenderer<>(componentHelper::createProcessInstanceStateBadge));
        stateComboBox.setLabel(messages.getMessage(getClass(), "stateLabel"));
        stateComboBox.setClearButtonVisible(true);
        stateComboBox.setMinWidth("10em");
        stateComboBox.setPlaceholder(messages.getMessage(getClass(), "selectState"));

        withIncidentsCheckBox = uiComponents.create(JmixCheckbox.class);
        withIncidentsCheckBox.setLabel(messages.getMessage(ProcessInstanceFilter.class, "ProcessInstanceFilter.withIncidents"));

        verticalLayout.add(stateComboBox, withIncidentsCheckBox);
        return verticalLayout;
    }

    @Override
    protected void onClearButtonClick(ClickEvent<Button> event) {
        resetFilterValues();
    }

    @Override
    protected void resetFilterValues() {
        stateComboBox.setValue(null);
        withIncidentsCheckBox.setValue(false);
    }

    @Override
    protected JmixButton createResetButton() {
        JmixButton resetButton = super.createResetButton();

        resetButton.addClassNames(LumoUtility.Margin.Right.XLARGE);
        return resetButton;
    }

    @Override
    protected JmixButton createApplyButton() {
        JmixButton applyButton = super.createApplyButton();
        applyButton.addClassNames(LumoUtility.Margin.Left.XLARGE);
        return applyButton;
    }

    @Override
    public void apply() {
        ProcessInstanceFilter instanceFilter = filterDc.getItem();

        ProcessInstanceState state = stateComboBox.getValue();
        if (viewMode == ProcessInstanceViewMode.ALL || viewMode == ProcessInstanceViewMode.ACTIVE) {
            instanceFilter.setState(state);
        }

        Boolean withIncidents = withIncidentsCheckBox.getValue();
        if (BooleanUtils.isTrue(withIncidents)) {
            instanceFilter.setWithIncidents(withIncidents);
        } else {
            instanceFilter.setWithIncidents(null);
        }

        filterButton.getElement().setAttribute(COLUMN_FILTER_BUTTON_ACTIVATED_ATTRIBUTE_NAME, state != null || BooleanUtils.isTrue(withIncidents));
    }

    public void update(ProcessInstanceViewMode newViewMode) {
        this.viewMode = newViewMode;
        this.stateComboBox.setValue(null);
        switch (newViewMode) {
            case ALL -> {
                this.stateComboBox.setEnabled(true);
                this.stateComboBox.setItems(ProcessInstanceState.class);
            }
            case COMPLETED -> this.stateComboBox.setEnabled(false);
            case ACTIVE -> {
                this.stateComboBox.setEnabled(true);
                this.stateComboBox.setItems(List.of(ProcessInstanceState.ACTIVE, ProcessInstanceState.SUSPENDED));
            }
        }
    }

    @Override
    public void updateComponents(QueryParameters queryParameters) {
        if (viewMode == null || viewMode == ProcessInstanceViewMode.ALL || viewMode == ProcessInstanceViewMode.ACTIVE) {
            ProcessInstanceState state = getSingleParam(queryParameters, STATE_FILTER_PARAM, ProcessInstanceState::fromId);
            stateComboBox.setValue(state);
        }

        Boolean withIncidents = getBooleanParam(queryParameters, HAS_INCIDENTS_FILTER_PARAM);
        if (withIncidents != null) {
            withIncidentsCheckBox.setValue(withIncidents);
        } else {
            withIncidentsCheckBox.clear();
        }

        apply();
    }

    @Override
    public Map<String, String> getQueryParamValues() {
        Map<String, String> paramValues = new HashMap<>();
        ProcessInstanceState state = null;
        if (viewMode == null || viewMode == ProcessInstanceViewMode.ALL || viewMode == ProcessInstanceViewMode.ACTIVE) {
            state = stateComboBox.getValue();
        }
        paramValues.put(STATE_FILTER_PARAM, state != null ? state.getId().toLowerCase() : null);

        Boolean withIncidents = withIncidentsCheckBox.getValue();
        paramValues.put(HAS_INCIDENTS_FILTER_PARAM, withIncidents != null ? Boolean.toString(withIncidents) : null);

        return paramValues;
    }
}
