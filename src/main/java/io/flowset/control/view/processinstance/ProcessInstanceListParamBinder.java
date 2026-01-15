/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processinstance;

import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.QueryParameters;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.facet.UrlQueryParametersFacet;
import io.jmix.flowui.facet.urlqueryparameters.AbstractUrlQueryParametersBinder;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.flowset.control.entity.filter.ProcessInstanceFilter;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.entity.processinstance.ProcessInstanceState;
import io.flowset.control.view.processinstance.filter.ProcessInstanceStateHeaderFilter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class ProcessInstanceListParamBinder extends AbstractUrlQueryParametersBinder {

    private static final String MODE_URL_PARAM = "mode";
    private static final String PRIMARY_THEME = "primary";
    private static final String STATE_COLUMN_KEY = "state";

    private final InstanceContainer<ProcessInstanceFilter> filterDc;
    private final Runnable loadDelegate;
    private final ProcessInstanceStateHeaderFilter stateHeaderFilter;
    private final List<JmixButton> modeButtons;

    public ProcessInstanceListParamBinder(HorizontalLayout buttonsPanel,
                                          InstanceContainer<ProcessInstanceFilter> filterDc,
                                          Runnable loadDelegate,
                                          DataGrid<ProcessInstanceData> dataGrid) {

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
                            activateModeButton(buttonIdx);
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
                        activateModeButton(0);
                        break;
                    case ACTIVE:
                        activateModeButton(1);
                        break;
                    case COMPLETED:
                        activateModeButton(2);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void activateModeButton(int activeButtonIdx) {
        updateButtons(activeButtonIdx);

        ProcessInstanceViewMode mode = ProcessInstanceViewMode.values()[activeButtonIdx];

        loadInstances(mode);

        QueryParameters qp = new QueryParameters(ImmutableMap.of(MODE_URL_PARAM, Collections.singletonList(mode.getId())));
        fireQueryParametersChanged(new UrlQueryParametersFacet.UrlQueryParametersChangeEvent(this, qp));

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

    private void loadInstances(ProcessInstanceViewMode mode) {
        if (mode == null) {
            this.filterDc.getItem().setState(null);
            this.filterDc.getItem().setUnfinished(true);
            this.loadDelegate.run();
            return;
        }

        switch (mode) {
            case ALL -> {
                this.filterDc.getItem().setState(null);
                this.filterDc.getItem().setUnfinished(null);
                this.loadDelegate.run();
            }
            case COMPLETED -> {
                this.filterDc.getItem().setState(ProcessInstanceState.COMPLETED);
                this.filterDc.getItem().setUnfinished(null);
                this.loadDelegate.run();
            }
            default -> {
                this.filterDc.getItem().setState(null);
                this.filterDc.getItem().setUnfinished(true);
                this.loadDelegate.run();
            }
        }
    }
}
