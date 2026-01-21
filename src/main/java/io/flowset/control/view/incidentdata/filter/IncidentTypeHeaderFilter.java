/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.incidentdata.filter;

import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.model.InstanceContainer;
import io.flowset.control.entity.filter.IncidentFilter;
import io.flowset.control.entity.incident.IncidentData;
import io.flowset.control.facet.urlqueryparameters.HasFilterUrlParamHeaderFilter;
import io.flowset.control.view.incidentdata.IncidentHeaderFilter;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.runtime.Incident;

import java.util.HashMap;
import java.util.Map;

import static io.flowset.control.facet.urlqueryparameters.IncidentListQueryParamBinder.TYPE_FILTER_PARAM;
import static io.flowset.control.view.util.FilterQueryParamUtils.getStringParam;

public class IncidentTypeHeaderFilter extends IncidentHeaderFilter implements HasFilterUrlParamHeaderFilter {
    private static final String CUSTOM_TYPE = "custom";

    protected TypedTextField<String> customTypeField;
    protected RadioButtonGroup<String> incidentTypeGroup;

    public IncidentTypeHeaderFilter(Grid<IncidentData> dataGrid,
                                    DataGridColumn<IncidentData> column,
                                    InstanceContainer<IncidentFilter> filterDc) {
        super(dataGrid, column, filterDc);
    }


    @Override
    protected Component createFilterComponent() {
        return createIncidentTypeFilter();
    }

    @Override
    protected void resetFilterValues() {
        incidentTypeGroup.clear();
        customTypeField.clear();
    }

    @Override
    public void apply() {
        String value = incidentTypeGroup.getValue();
        if (StringUtils.equals(value, CUSTOM_TYPE)) {
            filterDc.getItem().setIncidentType(customTypeField.getValue());
        } else {
            filterDc.getItem().setIncidentType(value);
        }

        filterButton.getElement().setAttribute(COLUMN_FILTER_BUTTON_ACTIVATED_ATTRIBUTE_NAME, value != null);
    }

    @Override
    public void updateComponents(QueryParameters queryParameters) {
        String typeParam = getStringParam(queryParameters, TYPE_FILTER_PARAM);
        if (typeParam == null) {
            incidentTypeGroup.clear();
            customTypeField.clear();
        } else if (StringUtils.equalsAny(typeParam, Incident.FAILED_JOB_HANDLER_TYPE, Incident.EXTERNAL_TASK_HANDLER_TYPE)) {
            incidentTypeGroup.setValue(typeParam);
            customTypeField.clear();
        } else {
            incidentTypeGroup.setValue(CUSTOM_TYPE);
            customTypeField.setTypedValue(typeParam);
        }
        apply();
    }

    @Override
    public Map<String, String> getQueryParamValues() {
        Map<String, String> paramValues = new HashMap<>();
        String value = incidentTypeGroup.getValue();
        if (StringUtils.equals(value, CUSTOM_TYPE)) {
            paramValues.put(TYPE_FILTER_PARAM, customTypeField.getTypedValue());
        } else {
            paramValues.put(TYPE_FILTER_PARAM, value);
        }
        return paramValues;
    }

    protected VerticalLayout createIncidentTypeFilter() {
        incidentTypeGroup = uiComponents.create(RadioButtonGroup.class);
        incidentTypeGroup.setLabel(messages.getMessage(IncidentFilter.class, "IncidentFilter.incidentType"));
        incidentTypeGroup.addValueChangeListener(event -> {
            String value = event.getValue();

            if (value == null || !value.equals(CUSTOM_TYPE)) {
                customTypeField.clear();
                customTypeField.setVisible(false);
            } else {
                customTypeField.setVisible(true);
            }
        });

        Map<String, String> incidentTypesMap = ImmutableMap.of(
                Incident.FAILED_JOB_HANDLER_TYPE, Incident.FAILED_JOB_HANDLER_TYPE,
                Incident.EXTERNAL_TASK_HANDLER_TYPE, Incident.EXTERNAL_TASK_HANDLER_TYPE,
                CUSTOM_TYPE, messages.getMessage(getClass(), "customType")
        );
        ComponentUtils.setItemsMap(incidentTypeGroup, incidentTypesMap);

        customTypeField = uiComponents.create(TypedTextField.class);
        customTypeField.setVisible(false);
        customTypeField.setWidthFull();
        customTypeField.setMinWidth("10em");
        customTypeField.setClearButtonVisible(true);
        customTypeField.setLabel(messages.getMessage(getClass(), "customIncidentType.label"));
        customTypeField.setPlaceholder(messages.getMessage(getClass(), "customIncidentType.placeholder"));

        VerticalLayout verticalLayout = uiComponents.create(VerticalLayout.class);
        verticalLayout.setPadding(false);
        verticalLayout.setWidthFull();
        verticalLayout.addClassNames(LumoUtility.Gap.SMALL);

        verticalLayout.add(incidentTypeGroup, customTypeField);
        return verticalLayout;
    }
}
