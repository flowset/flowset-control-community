package io.flowset.control.view.decisiondefinition;

import com.google.common.base.Strings;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.core.DataLoadContext;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.entity.filter.DecisionDefinitionFilter;
import io.flowset.control.service.decisiondefinition.DecisionDefinitionLoadContext;
import io.flowset.control.service.decisiondefinition.DecisionDefinitionService;
import io.flowset.control.view.decisiondeployment.DecisionDeploymentView;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "bpmn/decision-definitions", layout = DefaultMainViewParent.class)
@ViewController(id = "bpm_DecisionDefinition.list")
@ViewDescriptor("decision-definition-list-view.xml")
public class DecisionDefinitionListView extends StandardListView<DecisionDefinitionData> {

    @Autowired
    private Metadata metadata;
    @Autowired
    private DecisionDefinitionService decisionDefinitionService;
    @Autowired
    private ViewNavigators viewNavigators;
    @Autowired
    private Fragments fragments;

    @ViewComponent
    private InstanceContainer<DecisionDefinitionFilter> decisionDefinitionFilterDc;
    @ViewComponent
    private JmixFormLayout filterFormLayout;
    @ViewComponent
    private HorizontalLayout filterPanel;
    @ViewComponent
    private CollectionLoader<DecisionDefinitionData> decisionDefinitionsDl;
    @ViewComponent
    private TypedTextField<String> nameField;
    @ViewComponent
    private JmixCheckbox lastVersionOnlyCb;

    @Subscribe
    public void onInit(final InitEvent event) {
        addClassNames(LumoUtility.Padding.Top.SMALL);
        initFilterFormStyles();
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        initFilter();
        decisionDefinitionsDl.load();
    }

    @Subscribe(id = "clearBtn", subject = "clickListener")
    public void onClearBtnClick(final ClickEvent<JmixButton> event) {
        DecisionDefinitionFilter filter = decisionDefinitionFilterDc.getItem();
        filter.setKeyLike(null);
        filter.setNameLike(null);
        filter.setLatestVersionOnly(true);
    }

    @Subscribe(id = "decisionDefinitionFilterDc", target = Target.DATA_CONTAINER)
    public void onDecisionDefinitionFilterDcItemPropertyChange(
            final InstanceContainer.ItemPropertyChangeEvent<DecisionDefinitionFilter> event) {
        if (lastVersionOnlyCb.getValue() && !Strings.isNullOrEmpty(nameField.getValue())) {
            return;
        }
        decisionDefinitionsDl.load();
    }

    @Subscribe("nameField")
    public void onNameFieldComponentValueChange(
            final AbstractField.ComponentValueChangeEvent<TypedTextField<String>, String> event) {
        boolean nameEmpty = Strings.isNullOrEmpty(nameField.getValue());
        if (!nameEmpty) {
            lastVersionOnlyCb.setValue(false);
        }
        lastVersionOnlyCb.setReadOnly(!nameEmpty);
    }

    @Subscribe("decisionDefinitionsGrid.deploy")
    protected void onDecisionDefinitionsGridDeploy(final ActionPerformedEvent event) {
        viewNavigators.view(this, DecisionDeploymentView.class)
                .withBackwardNavigation(true)
                .navigate();
    }

    protected void initFilter() {
        DecisionDefinitionFilter filter = metadata.create(DecisionDefinitionFilter.class);
        filter.setLatestVersionOnly(true);
        decisionDefinitionFilterDc.setItem(filter);
    }

    protected void initFilterFormStyles() {
        filterFormLayout.getOwnComponents().forEach(c -> c.addClassName(LumoUtility.Padding.Top.XSMALL));
        filterPanel.addClassNames(LumoUtility.Padding.Top.XSMALL, LumoUtility.Padding.Left.MEDIUM,
                LumoUtility.Padding.Bottom.XSMALL, LumoUtility.Padding.Right.MEDIUM,
                LumoUtility.Border.ALL, LumoUtility.BorderRadius.LARGE, LumoUtility.BorderColor.CONTRAST_20);
    }

    @Install(to = "decisionDefinitionPagination", subject = "totalCountDelegate")
    protected Integer decisionDefinitionPaginationTotalCountDelegate(final DataLoadContext dataLoadContext) {
        return (int) decisionDefinitionService.getCount(decisionDefinitionFilterDc.getItemOrNull());
    }

    @Install(to = "decisionDefinitionsDl", target = Target.DATA_LOADER)
    protected List<DecisionDefinitionData> decisionDefinitionsDlLoadDelegate(
            LoadContext<DecisionDefinitionData> loadContext) {
        LoadContext.Query query = loadContext.getQuery();
        DecisionDefinitionFilter filter = decisionDefinitionFilterDc.getItemOrNull();
        DecisionDefinitionLoadContext context = new DecisionDefinitionLoadContext().setFilter(filter);
        if (query != null) {
            context = context.setFirstResult(query.getFirstResult())
                    .setMaxResults(query.getMaxResults())
                    .setSort(query.getSort());
        }
        return decisionDefinitionService.findAll(context);
    }

    @Supply(to = "decisionDefinitionsGrid.actions", subject = "renderer")
    private Renderer<DecisionDefinitionData> decisionDefinitionsGridActionsRenderer() {
        return new ComponentRenderer<>(decisiondefinitiondata -> {
            DecisionDefinitionListItemActionsFragment fragment = fragments.create(this, DecisionDefinitionListItemActionsFragment.class);
            fragment.setDecisionDefinition(decisiondefinitiondata);
            return fragment;
        });
    }
}
