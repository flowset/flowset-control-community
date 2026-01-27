/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.facet.urlqueryparameters;

import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.QueryParameters;
import io.flowset.control.entity.filter.ProcessDefinitionFilter;
import io.flowset.control.entity.processdefinition.ProcessDefinitionState;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.model.InstanceContainer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.flowset.control.view.util.FilterQueryParamUtils.*;

public class ProcessDefinitionListQueryParamBinder extends AbstractFilterUrlQueryParamBinder {
    public static final String NAME_PARAM = "name";
    public static final String KEY_PARAM = "key";
    public static final String STATE_PARAM = "state";
    public static final String LATEST_VERSION_ONLY_PARAM = "latestVersionOnly";

    protected final InstanceContainer<ProcessDefinitionFilter> filterDc;
    protected final Runnable loadDelegate;

    public ProcessDefinitionListQueryParamBinder(InstanceContainer<ProcessDefinitionFilter> filterDc, Runnable loadDelegate, JmixFormLayout filterForm) {
        super();
        this.filterDc = filterDc;
        this.loadDelegate = loadDelegate;

        addValueChangeListeners(filterForm);
    }

    @Override
    public Component getComponent() {
        return null;
    }

    @Override
    public void updateState(QueryParameters queryParameters) {
        updateFilterParams(queryParameters);

        loadDelegate.run();
    }

    protected void updateFilterParams(QueryParameters queryParameters) {
        ProcessDefinitionFilter processDefinitionFilter = filterDc.getItem();

        String nameParam = getStringParam(queryParameters, NAME_PARAM);
        processDefinitionFilter.setNameLike(nameParam);

        String keyParam = getStringParam(queryParameters, KEY_PARAM);
        processDefinitionFilter.setKeyLike(keyParam);

        ProcessDefinitionState stateParam = getSingleParam(queryParameters, STATE_PARAM, ProcessDefinitionState::fromId);
        processDefinitionFilter.setState(stateParam);

        Boolean latestVersionParam = getBooleanParam(queryParameters, LATEST_VERSION_ONLY_PARAM);
        processDefinitionFilter.setLatestVersionOnly(latestVersionParam != null ? latestVersionParam : true);
    }

    @Override
    protected Map<String, List<String>> getEmptyParametersMap() {
        return ImmutableMap.of(
                NAME_PARAM, Collections.emptyList(),
                KEY_PARAM, Collections.emptyList(),
                STATE_PARAM, Collections.emptyList(),
                LATEST_VERSION_ONLY_PARAM, Collections.emptyList());
    }

    protected void addValueChangeListeners(JmixFormLayout filterForm) {
        Component nameField = filterForm.getOwnComponent("nameField");
        addComponentValueChangeListener(nameField, NAME_PARAM, o -> (String) o);

        Component keyField = filterForm.getOwnComponent("keyField");
        addComponentValueChangeListener(keyField, KEY_PARAM, o -> (String) o);

        Component latestVersionOnlyCb = filterForm.getComponent("lastVersionOnlyCb");
        addComponentValueChangeListener(latestVersionOnlyCb, LATEST_VERSION_ONLY_PARAM, o -> Boolean.toString((Boolean) o));

        Component stateComboBox = filterForm.getComponent("stateComboBox");
        addComponentValueChangeListener(stateComboBox, STATE_PARAM, o -> ((ProcessDefinitionState) o).getId().toLowerCase());
    }
}
