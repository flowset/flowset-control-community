/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processinstance.filter;

import io.flowset.control.entity.filter.ProcessInstanceFilter;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.uicomponent.AbstractProcessHeaderFilter;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.model.InstanceContainer;
import org.apache.commons.lang3.BooleanUtils;

public class ProcessHeaderFilter extends AbstractProcessHeaderFilter<ProcessInstanceFilter, ProcessInstanceData> {

    public ProcessHeaderFilter(DataGrid<ProcessInstanceData> dataGrid, DataGridColumn<ProcessInstanceData> column,
                               InstanceContainer<ProcessInstanceFilter> filterDc) {
        super(dataGrid, column, filterDc);
    }

    @Override
    public void apply() {
        ProcessInstanceFilter instanceFilter = filterDc.getItem();

        ProcessDefinitionData processVersion = null;
        boolean useVersion = BooleanUtils.isTrue(useSpecificVersionChkBox.getValue());
        String processKey = null;

        if (useVersion) {
            processVersion = processVersionComboBox.getValue();
            instanceFilter.setProcessDefinitionId(processVersion != null ? processVersion.getProcessDefinitionId() : null);
            instanceFilter.setProcessDefinitionKey(null);
        } else {
            processKey = processKeyComboBox.getValue();
            instanceFilter.setProcessDefinitionId(null);
            instanceFilter.setProcessDefinitionKey(processKey);
        }
        filterButton.getElement().setAttribute(COLUMN_FILTER_BUTTON_ACTIVATED_ATTRIBUTE_NAME, processVersion != null || processKey != null);
    }
}
