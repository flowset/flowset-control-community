/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.incidentdata.filter;


import com.vaadin.flow.component.grid.Grid;
import io.flowset.control.entity.filter.IncidentFilter;
import io.flowset.control.entity.incident.IncidentData;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.uicomponent.AbstractProcessHeaderFilter;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.model.InstanceContainer;
import org.apache.commons.lang3.BooleanUtils;

public class ProcessHeaderFilter extends AbstractProcessHeaderFilter<IncidentFilter, IncidentData> {

    public ProcessHeaderFilter(Grid<IncidentData> dataGrid, DataGridColumn<IncidentData> column, InstanceContainer<IncidentFilter> filterDc) {
        super(dataGrid, column, filterDc);
    }

    @Override
    public void apply() {
        ProcessDefinitionData processVersion = null;
        String processKey = processKeyComboBox.getValue();
        boolean useVersion = BooleanUtils.isTrue(useSpecificVersionChkBox.getValue());

        IncidentFilter incidentFilter = filterDc.getItem();
        if (useVersion) {
            processVersion = processVersionComboBox.getValue();
            incidentFilter.setProcessDefinitionId(processVersion != null ? processVersion.getProcessDefinitionId() : null);
            incidentFilter.setProcessDefinitionKey(null);
        } else {
            incidentFilter.setProcessDefinitionKey(processKey);
            incidentFilter.setProcessDefinitionId(null);
        }
        filterButton.getElement().setAttribute(COLUMN_FILTER_BUTTON_ACTIVATED_ATTRIBUTE_NAME, processVersion != null || processKey != null);
    }

}
