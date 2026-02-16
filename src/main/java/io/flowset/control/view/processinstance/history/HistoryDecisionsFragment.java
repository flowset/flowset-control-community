/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processinstance.history;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.entity.decisioninstance.HistoricDecisionInstanceShortData;
import io.flowset.control.entity.filter.DecisionInstanceFilter;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.service.decisiondefinition.DecisionDefinitionService;
import io.flowset.control.service.decisioninstance.DecisionInstanceLoadContext;
import io.flowset.control.service.decisioninstance.DecisionInstanceService;
import io.flowset.control.view.decisioninstance.column.DecisionDefinitionColumnFragment;
import io.flowset.control.view.processinstance.event.DecisionCountUpdateEvent;
import io.jmix.core.DataLoadContext;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.UiEventPublisher;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.Supply;
import io.jmix.flowui.view.Target;
import io.jmix.flowui.view.ViewComponent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@FragmentDescriptor("history-decisions-fragment.xml")
@RequiredArgsConstructor
public class HistoryDecisionsFragment extends Fragment<VerticalLayout> implements HasRefresh {
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected DecisionInstanceService decisionInstanceService;
    @Autowired
    protected DecisionDefinitionService decisionDefinitionService;
    @Autowired
    protected Fragments fragments;
    @Autowired
    protected UiEventPublisher uiEventPublisher;

    @ViewComponent
    protected InstanceContainer<ProcessInstanceData> processInstanceDataDc;
    @ViewComponent
    protected CollectionLoader<HistoricDecisionInstanceShortData> historyDecisionsDl;

    protected DecisionInstanceFilter filter;
    protected boolean initialized;

    protected Map<String, DecisionDefinitionData> decisionDefinitionsMap = new HashMap<>();


    @Override
    public void refreshIfRequired() {
        if (!initialized) {
            this.filter = metadata.create(DecisionInstanceFilter.class);
            filter.setProcessInstanceId(processInstanceDataDc.getItem().getId());

            historyDecisionsDl.load();
            this.initialized = true;
        }
    }

    @Install(to = "historyDecisionsDl", target = Target.DATA_LOADER)
    protected List<HistoricDecisionInstanceShortData> historyDecisionsDlLoadDelegate(final LoadContext<HistoricDecisionInstanceShortData> loadContext) {
        LoadContext.Query query = loadContext.getQuery();
        DecisionInstanceLoadContext context = new DecisionInstanceLoadContext().setFilter(filter);

        if (query != null) {
            context.setFirstResult(query.getFirstResult())
                    .setMaxResults(query.getMaxResults())
                    .setSort(query.getSort());
        }

        List<HistoricDecisionInstanceShortData> decisionInstances = decisionInstanceService.findAllHistoryDecisionInstances(context);
        loadDecisionDefinitions(decisionInstances);
        return decisionInstances;
    }

    @Install(to = "decisionsPagination", subject = "totalCountDelegate")
    protected Integer decisionsPaginationTotalCountDelegate(final DataLoadContext dataLoadContext) {
        int decisionInstancesCount = (int) decisionInstanceService.getHistoryDecisionInstancesCount(filter);
        uiEventPublisher.publishEventForCurrentUI(new DecisionCountUpdateEvent(this, decisionInstancesCount));
        return decisionInstancesCount;
    }

    @Supply(to = "historyTasksGrid.decisionDefinitionId", subject = "renderer")
    protected Renderer<HistoricDecisionInstanceShortData> historyTasksGridDecisionDefinitionIdRenderer() {
        return new ComponentRenderer<>(decisionInstance -> {
            if (decisionInstance.getDecisionDefinitionId() == null) {
                return null;
            }
            DecisionDefinitionColumnFragment definitionColumnFragment = fragments.create(this, DecisionDefinitionColumnFragment.class);
            definitionColumnFragment.setDecisionDefinitionData(decisionDefinitionsMap.get(decisionInstance.getDecisionDefinitionId()));
            definitionColumnFragment.setItem(decisionInstance);
            return definitionColumnFragment;
        });
    }

    protected void loadDecisionDefinitions(List<HistoricDecisionInstanceShortData> decisionInstances) {
        Set<String> idsToLoad = decisionInstances.stream()
                .map(HistoricDecisionInstanceShortData::getDecisionDefinitionId)
                .filter(Objects::nonNull)
                .filter(decisionDefinitionId -> !decisionDefinitionsMap.containsKey(decisionDefinitionId))
                .collect(Collectors.toSet());

        decisionDefinitionService.findAllByIds(idsToLoad)
                .forEach(decisionDefinitionData -> decisionDefinitionsMap.put(decisionDefinitionData.getId(), decisionDefinitionData));
    }
}