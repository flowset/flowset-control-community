/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.decisioninstance.filter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.entity.decisioninstance.HistoricDecisionInstanceShortData;
import io.flowset.control.entity.filter.DecisionDefinitionFilter;
import io.flowset.control.entity.filter.DecisionInstanceFilter;
import io.flowset.control.facet.urlqueryparameters.HasFilterUrlParamHeaderFilter;
import io.flowset.control.service.decisiondefinition.DecisionDefinitionLoadContext;
import io.flowset.control.service.decisiondefinition.DecisionDefinitionService;
import io.flowset.control.uicomponent.ContainerDataGridHeaderFilter;
import io.jmix.core.Metadata;
import io.jmix.core.Sort;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.model.InstanceContainer;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.flowset.control.facet.urlqueryparameters.DecisionInstanceListQueryParamBinder.DECISION_ID_FILTER_PARAM;
import static io.flowset.control.facet.urlqueryparameters.DecisionInstanceListQueryParamBinder.DECISION_KEY_FILTER_PARAM;
import static io.flowset.control.view.util.FilterQueryParamUtils.getStringParam;

public class DecisionHeaderFilter extends ContainerDataGridHeaderFilter<DecisionInstanceFilter, HistoricDecisionInstanceShortData>
        implements HasFilterUrlParamHeaderFilter {

    protected DecisionDefinitionService decisionDefinitionService;
    protected Metadata metadata;
    protected JmixComboBox<DecisionDefinitionData> decisionVersionComboBox;
    protected JmixComboBox<String> decisionKeyComboBox;
    protected Checkbox useSpecificVersionChkBox;

    private final Runnable loadDelegate;

    public DecisionHeaderFilter(Grid<HistoricDecisionInstanceShortData> dataGrid, DataGridColumn<HistoricDecisionInstanceShortData> column, InstanceContainer<DecisionInstanceFilter> filterDc,
                                Runnable loadDelegate) {
        super(dataGrid, column, filterDc);
        this.loadDelegate = loadDelegate;
    }

    @Autowired
    public void setDecisionDefinitionService(DecisionDefinitionService decisionDefinitionService) {
        this.decisionDefinitionService = decisionDefinitionService;
    }

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public void apply() {
        updateFilterState();

        this.loadDelegate.run();
    }

    @Override
    public void updateComponents(QueryParameters queryParameters) {
        updateDecisionFields(queryParameters);
        updateFilterState();
    }

    @Override
    public Map<String, String> getQueryParamValues() {
        Map<String, String> paramValues = new HashMap<>();

        if (BooleanUtils.isTrue(useSpecificVersionChkBox.getValue())) {
            DecisionDefinitionData decisionVersion = decisionVersionComboBox.getValue();
            paramValues.put(DECISION_ID_FILTER_PARAM, decisionVersion != null ? decisionVersion.getDecisionDefinitionId() : null);
            paramValues.put(DECISION_KEY_FILTER_PARAM, null);
        } else {
            String decisionKey = decisionKeyComboBox.getValue();
            paramValues.put(DECISION_KEY_FILTER_PARAM, decisionKey);
            paramValues.put(DECISION_ID_FILTER_PARAM, null);
        }

        return paramValues;
    }

    protected void updateFilterState() {
        DecisionDefinitionData decisionVersion = null;
        String decisionKey = decisionKeyComboBox.getValue();
        boolean useVersion = BooleanUtils.isTrue(useSpecificVersionChkBox.getValue());

        DecisionInstanceFilter decisionInstanceFilter = filterDc.getItem();
        if (useVersion) {
            decisionVersion = decisionVersionComboBox.getValue();
            decisionInstanceFilter.setDecisionDefinitionId(decisionVersion != null ? decisionVersion.getDecisionDefinitionId() : null);
            decisionInstanceFilter.setDecisionDefinitionKey(null);
        } else {
            decisionInstanceFilter.setDecisionDefinitionKey(decisionKey);
            decisionInstanceFilter.setDecisionDefinitionId(null);
        }
        filterButton.getElement().setAttribute(COLUMN_FILTER_BUTTON_ACTIVATED_ATTRIBUTE_NAME, decisionVersion != null || decisionKey != null);
    }

    @Override
    protected Component createFilterComponent() {
        VerticalLayout layout = uiComponents.create(VerticalLayout.class);
        layout.addClassNames(LumoUtility.Gap.SMALL, LumoUtility.Padding.SMALL);
        layout.setSizeFull();

        decisionKeyComboBox = createDecisionComboBox();
        decisionVersionComboBox = createDecisionVersionComboBox();
        useSpecificVersionChkBox = uiComponents.create(Checkbox.class);
        useSpecificVersionChkBox.setLabel(messages.getMessage("processColumnFilter.useSpecificVersion.label"));
        useSpecificVersionChkBox.addValueChangeListener(event -> {
            boolean useVersion = BooleanUtils.isTrue(event.getValue());
            decisionVersionComboBox.setVisible(useVersion);
            decisionVersionComboBox.setRequired(useVersion);
        });

        VerticalLayout versionLayout = uiComponents.create(VerticalLayout.class);
        versionLayout.setPadding(false);
        versionLayout.add(useSpecificVersionChkBox);
        versionLayout.addAndExpand(decisionVersionComboBox);

        updateComboBoxVisibility(useSpecificVersionChkBox.getValue());

        layout.add(decisionKeyComboBox, versionLayout);
        return layout;
    }


    protected void updateDecisionFields(QueryParameters queryParameters) {
        String decisionIdValue = getStringParam(queryParameters, DECISION_ID_FILTER_PARAM);

        DecisionDefinitionData version = null;
        String decisionKey;
        if (decisionIdValue != null) {
            version = decisionDefinitionService.getById(decisionIdValue);
            decisionKey = version != null ? version.getKey() : null;
        } else {
            decisionKey = getStringParam(queryParameters, DECISION_KEY_FILTER_PARAM);
        }
        updateFields(version, decisionKey);
    }

    protected void updateFields(@Nullable DecisionDefinitionData version, @Nullable String decisionKey) {
        decisionKeyComboBox.setValue(decisionKey);
        useSpecificVersionChkBox.setValue(decisionKey != null && version != null);
        decisionVersionComboBox.setValue(version);
    }


    protected JmixComboBox<String> createDecisionComboBox() {
        JmixComboBox<String> decisionComboBox = uiComponents.create(JmixComboBox.class);
        decisionComboBox.addClassNames(LumoUtility.Padding.Top.NONE);
        decisionComboBox.setClearButtonVisible(true);
        decisionComboBox.setLabel(messages.getMessage(getClass(), "decisionColumnFilter.decisionField.label"));
        decisionComboBox.setPlaceholder(messages.getMessage(getClass(), "decisionColumnFilter.decisionField.placeholder"));
        decisionComboBox.setMinWidth("30em");
        decisionComboBox.addValueChangeListener(event -> {
            decisionVersionComboBox.setValue(null);
            updateVersions(decisionVersionComboBox);
        });
        decisionComboBox.setItemsFetchCallback(query -> {
            DecisionDefinitionFilter filter = metadata.create(DecisionDefinitionFilter.class);
            filter.setKeyLike(query.getFilter().orElse(null));
            filter.setLatestVersionOnly(true);
            DecisionDefinitionLoadContext context = new DecisionDefinitionLoadContext().setFilter(filter);
            context.setMaxResults(query.getLimit());
            context.setFirstResult(query.getOffset());
            context.setSort(Sort.by(Sort.Direction.ASC, "key"));

            return decisionDefinitionService.findAll(context).stream().map(DecisionDefinitionData::getKey);
        });
        return decisionComboBox;
    }

    protected JmixComboBox<DecisionDefinitionData> createDecisionVersionComboBox() {
        JmixComboBox<DecisionDefinitionData> decisionVersionComboBox = uiComponents.create(JmixComboBox.class);
        decisionVersionComboBox.addClassNames(LumoUtility.Padding.Top.NONE);
        decisionVersionComboBox.setItemsPageable((pageable, filterString) -> {
            int offset = Math.toIntExact(pageable.getOffset());
            int limit = pageable.getPageSize();
            if (decisionKeyComboBox.getValue() != null) {
                DecisionDefinitionFilter filter = metadata.create(DecisionDefinitionFilter.class);
                filter.setKey(decisionKeyComboBox.getValue());
                filter.setLatestVersionOnly(false);

                DecisionDefinitionLoadContext context = new DecisionDefinitionLoadContext().setFilter(filter);
                context.setMaxResults(limit);

                context.setFirstResult(offset);
                context.setSort(Sort.by(Sort.Direction.ASC, "version"));
                return decisionDefinitionService.findAll(context);
            } else {
                return List.of();
            }
        });
        decisionVersionComboBox.setClearButtonVisible(true);
        decisionVersionComboBox.setPlaceholder(messages.getMessage(getClass(), "decisionColumnFilter.decisionVersionField.placeholder"));
        decisionVersionComboBox.setLabel(messages.getMessage(getClass(), "decisionColumnFilter.decisionVersionField.label"));
        decisionVersionComboBox.setMinWidth("10em");
        decisionVersionComboBox.setItemLabelGenerator(item -> {
            Integer version = item.getVersion();
            return version != null ? String.valueOf(version) : null;
        });
        updateVersions(decisionVersionComboBox);


        return decisionVersionComboBox;
    }

    protected void updateComboBoxVisibility(Boolean useVersion) {
        decisionVersionComboBox.setVisible(BooleanUtils.isTrue(useVersion));
    }


    protected void updateVersions(JmixComboBox<DecisionDefinitionData> decisionDefinitionComboBox) {
        decisionDefinitionComboBox.getDataProvider().refreshAll();
    }

    @Override
    protected void resetFilterValues() {
        decisionKeyComboBox.setValue(null);
        useSpecificVersionChkBox.setValue(false);
        decisionVersionComboBox.setValue(null);
    }
}
