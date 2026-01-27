package io.flowset.control.view.decisiondefinition;

import com.google.common.base.Strings;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.entity.filter.DecisionDefinitionFilter;
import io.flowset.control.facet.urlqueryparameters.DecisionDefinitionListQueryParamBinder;
import io.flowset.control.service.decisiondefinition.DecisionDefinitionLoadContext;
import io.flowset.control.service.decisiondefinition.DecisionDefinitionService;
import io.flowset.control.view.AbstractListViewWithDelayedLoad;
import io.flowset.control.view.decisiondeployment.DecisionDeploymentView;
import io.jmix.core.DataLoadContext;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.facet.UrlQueryParametersFacet;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Target;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "bpmn/decision-definitions", layout = DefaultMainViewParent.class)
@ViewController(id = "bpm_DecisionDefinition.list")
@ViewDescriptor("decision-definition-list-view.xml")
public class DecisionDefinitionListView extends AbstractListViewWithDelayedLoad<DecisionDefinitionData> {

    @ViewComponent
    protected UrlQueryParametersFacet urlQueryParameters;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected DecisionDefinitionService decisionDefinitionService;
    @Autowired
    protected ViewNavigators viewNavigators;
    @Autowired
    protected Fragments fragments;

    @ViewComponent
    protected InstanceContainer<DecisionDefinitionFilter> decisionDefinitionFilterDc;
    @ViewComponent
    protected JmixFormLayout filterFormLayout;
    @ViewComponent
    protected HorizontalLayout filterPanel;
    @ViewComponent
    protected CollectionLoader<DecisionDefinitionData> decisionDefinitionsDl;
    @ViewComponent
    protected TypedTextField<String> nameField;
    @ViewComponent
    protected JmixCheckbox lastVersionOnlyCb;

    protected DecisionDefinitionListQueryParamBinder urlQueryParamBinder;

    @Subscribe
    public void onInit(final InitEvent event) {
        addClassNames(LumoUtility.Padding.Top.SMALL);
        initFilterFormStyles();
        initFilter();
        urlQueryParamBinder = new DecisionDefinitionListQueryParamBinder(decisionDefinitionFilterDc, this::startLoadData, filterFormLayout);
        urlQueryParameters.registerBinder(urlQueryParamBinder);
    }

    @Subscribe(id = "clearBtn", subject = "clickListener")
    public void onClearBtnClick(final ClickEvent<JmixButton> event) {
        DecisionDefinitionFilter filter = decisionDefinitionFilterDc.getItem();
        filter.setKeyLike(null);
        filter.setNameLike(null);
        filter.setLatestVersionOnly(true);
        urlQueryParamBinder.resetParameters();
    }

    @Subscribe("nameField")
    public void onNameFieldComponentValueChange(
            final AbstractField.ComponentValueChangeEvent<TypedTextField<String>, String> event) {
        boolean nameEmpty = Strings.isNullOrEmpty(nameField.getValue());
        if (!nameEmpty) {
            lastVersionOnlyCb.setValue(false);
        }
        lastVersionOnlyCb.setReadOnly(!nameEmpty);
        if (event.isFromClient()) {
            startLoadData();
        }
    }

    @Subscribe("keyField")
    public void onKeyFieldTypedValueChange(final SupportsTypedValue.TypedValueChangeEvent<TypedTextField<String>, String> event) {
        if (event.isFromClient()) {
            startLoadData();
        }
    }

    @Subscribe("lastVersionOnlyCb")
    public void onLastVersionOnlyCbComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixCheckbox, Boolean> event) {
        if (event.isFromClient()) {
            startLoadData();
        }
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
            context.setFirstResult(query.getFirstResult())
                    .setMaxResults(query.getMaxResults())
                    .setSort(query.getSort());
        }
        return loadItemsWithStateHandling(() -> decisionDefinitionService.findAll(context));
    }

    @Override
    protected void loadData() {
        decisionDefinitionsDl.load();
    }

    @Subscribe("decisionDefinitionsGrid.refresh")
    public void onDecisionDefinitionsGridRefresh(final ActionPerformedEvent event) {
        startLoadData();
    }
}
