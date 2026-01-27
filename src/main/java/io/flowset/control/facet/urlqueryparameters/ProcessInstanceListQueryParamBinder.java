/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.facet.urlqueryparameters;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.QueryParameters;
import io.flowset.control.entity.filter.ProcessInstanceFilter;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.entity.processinstance.ProcessInstanceState;
import io.flowset.control.view.processinstance.ProcessInstanceViewMode;
import io.flowset.control.view.processinstance.filter.ProcessInstanceStateHeaderFilter;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.headerfilter.DataGridHeaderFilter;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

public class ProcessInstanceListQueryParamBinder extends GridCustomHeaderFilterUrlQueryParametersBinder<ProcessInstanceData> {
    public static final String INSTANCE_ID_FILTER_PARAM = "id";
    public static final String BUSINESS_KEY_FILTER_PARAM = "businessKey";
    public static final String STATE_FILTER_PARAM = "state";
    public static final String HAS_INCIDENTS_FILTER_PARAM = "withIncidents";
    public static final String STARTED_AFTER_FILTER_PARAM = "startedAfter";
    public static final String STARTED_BEFORE_FILTER_PARAM = "startedBefore";
    public static final String END_AFTER_FILTER_PARAM = "endAfter";
    public static final String END_BEFORE_FILTER_PARAM = "endBefore";

    private static final String MODE_URL_PARAM = "mode";
    private static final String PRIMARY_THEME = "primary";
    private static final String STATE_COLUMN_KEY = "state";

    private final InstanceContainer<ProcessInstanceFilter> filterDc;
    private final Runnable loadDelegate;
    private final ProcessInstanceStateHeaderFilter stateHeaderFilter;
    private final List<JmixButton> modeButtons;

    public ProcessInstanceListQueryParamBinder(HorizontalLayout buttonsPanel,
                                               InstanceContainer<ProcessInstanceFilter> filterDc,
                                               Runnable loadDelegate,
                                               DataGrid<ProcessInstanceData> dataGrid) {
        super(dataGrid);

        this.filterDc = filterDc;
        this.loadDelegate = loadDelegate;
        this.stateHeaderFilter = Optional.ofNullable(dataGrid.getColumnByKey(STATE_COLUMN_KEY))
                .map(column -> (ProcessInstanceStateHeaderFilter) column.getHeaderComponent())
                .orElse(null);

        this.modeButtons = IntStream.range(0, buttonsPanel.getComponentCount())
                .mapToObj(buttonIdx -> {
                    JmixButton modeBtn = (JmixButton) buttonsPanel.getComponentAt(buttonIdx);
                    modeBtn.addClickListener(clickEvent -> {
                        boolean active = modeBtn.hasThemeName(PRIMARY_THEME);
                        if (!active) {
                            activateModeButton(buttonIdx, true);
                        }
                    });
                    return modeBtn;
                }).toList();

    }

    @Override
    public Component getComponent() {
        return null;
    }

    @Override
    public void updateState(QueryParameters queryParameters) {
        List<String> modeStrings = queryParameters.getParameters().get(MODE_URL_PARAM);
        if (CollectionUtils.isNotEmpty(modeStrings)) {
            ProcessInstanceViewMode mode = ProcessInstanceViewMode.fromId(modeStrings.get(0));

            if (mode != null) {
                switch (mode) {
                    case ALL:
                        activateModeButton(0, false);
                        break;
                    case ACTIVE:
                        activateModeButton(1, false);
                        break;
                    case COMPLETED:
                        activateModeButton(2, false);
                        break;
                    default:
                        break;
                }
            }
        }
        updateFilterValues(queryParameters);
    }

    protected void handleFilterApply(DataGridHeaderFilter.ApplyEvent applyEvent) {
        super.handleFilterApply(applyEvent);

        this.loadDelegate.run();
    }

    protected void updateFilterValues(QueryParameters queryParameters) {
        super.updateFilterValues(queryParameters);

        this.loadDelegate.run();
    }

    private void activateModeButton(int activeButtonIdx, boolean load) {
        updateButtons(activeButtonIdx);

        ProcessInstanceViewMode mode = ProcessInstanceViewMode.values()[activeButtonIdx];

        updateFilterByMode(mode, this.filterDc.getItem());
        if (load) {
            this.loadDelegate.run();
        }

        Map<String, String> params = new HashMap<>();
        params.put(MODE_URL_PARAM, mode.getId());
        if (mode == ProcessInstanceViewMode.COMPLETED) {
            params.put(STATE_FILTER_PARAM, null);
        }

        updateQueryParams(params);

        if (stateHeaderFilter != null) {
            stateHeaderFilter.update(mode);
        }
    }

    private void updateButtons(int activeIdx) {
        IntStream.range(0, modeButtons.size())
                .forEach(idx -> {
                    JmixButton modeBtn = modeButtons.get(idx);
                    if (activeIdx == idx) {
                        modeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                    } else {
                        modeBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
                    }
                });
    }

    protected void updateFilterByMode(ProcessInstanceViewMode mode, ProcessInstanceFilter filter) {
        if (mode == null) {
            filter.setState(null);
            filter.setUnfinished(true);
            return;
        }

        switch (mode) {
            case ALL -> filter.setUnfinished(null);
            case COMPLETED -> {
                filter.setState(ProcessInstanceState.COMPLETED);
                filter.setUnfinished(null);
            }
            default -> {
                if (filter.getState() == ProcessInstanceState.COMPLETED) {
                    filter.setState(null);
                }
                filter.setUnfinished(true);
            }
        }
    }
}
