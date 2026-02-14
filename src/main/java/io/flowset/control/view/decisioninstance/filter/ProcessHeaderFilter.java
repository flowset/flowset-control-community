/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.decisioninstance.filter;

import io.flowset.control.entity.decisioninstance.HistoricDecisionInstanceShortData;
import io.flowset.control.entity.filter.DecisionInstanceFilter;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.uicomponent.AbstractProcessHeaderFilter;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.model.InstanceContainer;
import org.apache.commons.lang3.BooleanUtils;

public class ProcessHeaderFilter
        extends AbstractProcessHeaderFilter<DecisionInstanceFilter, HistoricDecisionInstanceShortData> {


    protected final Runnable loadDelegate;
    public ProcessHeaderFilter(DataGrid<HistoricDecisionInstanceShortData> dataGrid,
                               DataGridColumn<HistoricDecisionInstanceShortData> column,
                               InstanceContainer<DecisionInstanceFilter> filterDc, Runnable loadDelegate) {
        super(dataGrid, column, filterDc);
        this.loadDelegate = loadDelegate;
    }

    @Override
    public void apply() {
        ProcessDefinitionData processVersion = null;
        String processKey = processKeyComboBox.getValue();
        boolean useVersion = BooleanUtils.isTrue(useSpecificVersionChkBox.getValue());

        DecisionInstanceFilter incidentFilter = filterDc.getItem();
        if (useVersion) {
            processVersion = processVersionComboBox.getValue();
            incidentFilter.setProcessDefinitionId(processVersion != null ? processVersion.getProcessDefinitionId() : null);
            incidentFilter.setProcessDefinitionKey(null);
        } else {
            incidentFilter.setProcessDefinitionKey(processKey);
            incidentFilter.setProcessDefinitionId(null);
        }
        filterButton.getElement().setAttribute(COLUMN_FILTER_BUTTON_ACTIVATED_ATTRIBUTE_NAME, processVersion != null || processKey != null);

        this.loadDelegate.run();
    }

}
