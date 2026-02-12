/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.uicomponent;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.entity.filter.ProcessDefinitionFilter;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.facet.urlqueryparameters.HasFilterUrlParamHeaderFilter;
import io.flowset.control.service.processdefinition.ProcessDefinitionLoadContext;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
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

import static io.flowset.control.view.util.FilterQueryParamUtils.getStringParam;

public abstract class AbstractProcessHeaderFilter<F, E> extends ContainerDataGridHeaderFilter<F, E> implements HasFilterUrlParamHeaderFilter {
    public static final String PROCESS_KEY_PARAM = "processKey";
    public static final String PROCESS_ID_PARAM = "processId";

    protected ProcessDefinitionService processDefinitionService;
    protected Metadata metadata;
    protected JmixComboBox<ProcessDefinitionData> processVersionComboBox;
    protected JmixComboBox<String> processKeyComboBox;
    protected Checkbox useSpecificVersionChkBox;

    public AbstractProcessHeaderFilter(Grid<E> dataGrid, DataGridColumn<E> column, InstanceContainer<F> filterDc) {
        super(dataGrid, column, filterDc);
    }

    @Autowired
    public void setProcessDefinitionService(ProcessDefinitionService processDefinitionService) {
        this.processDefinitionService = processDefinitionService;
    }

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    protected Component createFilterComponent() {
        VerticalLayout layout = uiComponents.create(VerticalLayout.class);
        layout.addClassNames(LumoUtility.Gap.SMALL, LumoUtility.Padding.SMALL);
        layout.setSizeFull();

        processKeyComboBox = createProcessComboBox();
        processVersionComboBox = createProcessVersionComboBox();
        useSpecificVersionChkBox = uiComponents.create(Checkbox.class);
        useSpecificVersionChkBox.setLabel(messages.getMessage("processColumnFilter.useSpecificVersion.label"));
        useSpecificVersionChkBox.addValueChangeListener(event -> {
            boolean useVersion = BooleanUtils.isTrue(event.getValue());
            processVersionComboBox.setVisible(useVersion);
            processVersionComboBox.setRequired(useVersion);
        });

        VerticalLayout versionLayout = uiComponents.create(VerticalLayout.class);
        versionLayout.setPadding(false);
        versionLayout.add(useSpecificVersionChkBox);
        versionLayout.addAndExpand(processVersionComboBox);

        updateComboBoxVisibility(useSpecificVersionChkBox.getValue());

        layout.add(processKeyComboBox, versionLayout);
        return layout;
    }

    @Override
    public void updateComponents(QueryParameters queryParameters) {
        String processIdValue = getStringParam(queryParameters, PROCESS_ID_PARAM);

        ProcessDefinitionData version = null;
        String processKey;
        if (processIdValue != null) {
            version = processDefinitionService.getById(processIdValue);
            processKey = version != null ? version.getKey() : null;
        } else {
            processKey = getStringParam(queryParameters, PROCESS_KEY_PARAM);
        }
        updateFields(version, processKey);
        apply();
    }

    protected void updateFields(@Nullable ProcessDefinitionData version, @Nullable String processKey) {
        processKeyComboBox.setValue(processKey);
        useSpecificVersionChkBox.setValue(processKey != null && version != null);
        processVersionComboBox.setValue(version);
    }

    @Override
    public Map<String, String> getQueryParamValues() {
        Map<String, String> paramValues = new HashMap<>();

        if (BooleanUtils.isTrue(useSpecificVersionChkBox.getValue())) {
            ProcessDefinitionData processVersion = processVersionComboBox.getValue();
            paramValues.put(PROCESS_ID_PARAM, processVersion != null ? processVersion.getProcessDefinitionId() : null);
            paramValues.put(PROCESS_KEY_PARAM, null);
        } else {
            String processKey = processKeyComboBox.getValue();
            paramValues.put(PROCESS_KEY_PARAM, processKey);
            paramValues.put(PROCESS_ID_PARAM, null);
        }

        return paramValues;
    }


    protected JmixComboBox<String> createProcessComboBox() {
        JmixComboBox<String> processComboBox = uiComponents.create(JmixComboBox.class);
        processComboBox.addClassNames(LumoUtility.Padding.Top.NONE);
        processComboBox.setClearButtonVisible(true);
        processComboBox.setLabel(messages.getMessage("processColumnFilter.processField.label"));
        processComboBox.setMinWidth("30em");
        processComboBox.addValueChangeListener(event -> {
            processVersionComboBox.setValue(null);
            updateVersions(processVersionComboBox);
        });
        processComboBox.setItemsFetchCallback(query -> {
            ProcessDefinitionFilter filter = metadata.create(ProcessDefinitionFilter.class);
            filter.setKeyLike(query.getFilter().orElse(null));
            filter.setLatestVersionOnly(true);
            ProcessDefinitionLoadContext context = new ProcessDefinitionLoadContext().setFilter(filter);
            context.setMaxResults(query.getLimit());
            context.setFirstResult(query.getOffset());
            context.setSort(Sort.by(Sort.Direction.ASC, "key"));

            return processDefinitionService.findAll(context).stream().map(ProcessDefinitionData::getKey).sorted();
        });
        return processComboBox;
    }

    protected JmixComboBox<ProcessDefinitionData> createProcessVersionComboBox() {
        JmixComboBox<ProcessDefinitionData> processVersionComboBox = uiComponents.create(JmixComboBox.class);
        processVersionComboBox.addClassNames(LumoUtility.Padding.Top.NONE);
        processVersionComboBox.setItemsPageable((pageable, filterString) -> {
            int offset = Math.toIntExact(pageable.getOffset());
            int limit = pageable.getPageSize();
            if (processKeyComboBox.getValue() != null) {
                ProcessDefinitionFilter filter = metadata.create(ProcessDefinitionFilter.class);
                filter.setKey(processKeyComboBox.getValue());
                filter.setLatestVersionOnly(false);

                ProcessDefinitionLoadContext context = new ProcessDefinitionLoadContext().setFilter(filter);
                context.setMaxResults(limit);

                context.setFirstResult(offset);
                context.setSort(Sort.by(Sort.Direction.ASC, "version"));
                return processDefinitionService.findAll(context);
            } else {
                return List.of();
            }
        });
        processVersionComboBox.setClearButtonVisible(true);
        processVersionComboBox.setPlaceholder(messages.getMessage("processColumnFilter.versionField.placeholder"));
        processVersionComboBox.setLabel(messages.getMessage("processColumnFilter.versionField.label"));
        processVersionComboBox.setMinWidth("10em");
        processVersionComboBox.setItemLabelGenerator(item -> {
            Integer version = item.getVersion();
            return version != null ? String.valueOf(version) : null;
        });
        updateVersions(processVersionComboBox);


        return processVersionComboBox;
    }

    protected void updateComboBoxVisibility(Boolean useVersion) {
        processVersionComboBox.setVisible(BooleanUtils.isTrue(useVersion));
    }


    protected void updateVersions(JmixComboBox<ProcessDefinitionData> processDefinitionComboBox) {
        processDefinitionComboBox.getDataProvider().refreshAll();
    }

    @Override
    protected void resetFilterValues() {
        processKeyComboBox.setValue(null);
        useSpecificVersionChkBox.setValue(false);
        processVersionComboBox.setValue(null);
    }
}
