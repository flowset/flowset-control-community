package io.flowset.control.view.deploymentdata;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.facet.urlqueryparameters.DeploymentListQueryParamBinder;
import io.flowset.control.view.AbstractListViewWithDelayedLoad;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.component.ComponentContainer;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.facet.UrlQueryParametersFacet;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.deployment.DeploymentData;
import io.flowset.control.entity.filter.DeploymentFilter;
import io.flowset.control.service.deployment.DeploymentLoadContext;
import io.flowset.control.service.deployment.DeploymentService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

@Route(value = "bpmn/deployments", layout = DefaultMainViewParent.class)
@ViewController(id = "bpm_Deployment.list")
@ViewDescriptor(path = "deployment-list-view.xml")
public class DeploymentListView extends AbstractListViewWithDelayedLoad<DeploymentData> {

    @ViewComponent
    protected UrlQueryParametersFacet urlQueryParameters;
    @Autowired
    protected DeploymentService deploymentService;
    @Autowired
    protected Metadata metadata;

    @ViewComponent
    protected InstanceContainer<DeploymentFilter> deploymentFilterDc;
    @ViewComponent
    protected CollectionLoader<DeploymentData> deploymentDatasDl;
    @ViewComponent
    protected JmixFormLayout filterFormLayout;
    @ViewComponent
    protected HorizontalLayout filterPanel;
    @Autowired
    protected Fragments fragments;
    @ViewComponent
    protected DataGrid<DeploymentData> deploymentsDataGrid;
    @Autowired
    protected DialogWindows dialogWindows;
    private DeploymentListQueryParamBinder queryParamBinder;

    @Subscribe
    public void onInit(final InitEvent event) {
        addClassNames(LumoUtility.Padding.Top.SMALL);
        initFilterFormStyles();
        initFilter();

        queryParamBinder = new DeploymentListQueryParamBinder(deploymentFilterDc, this::startLoadData, filterFormLayout);
        urlQueryParameters.registerBinder(queryParamBinder);

        addFilterValueChangeListeners(filterFormLayout);
    }

    @Subscribe("applyFilter")
    public void onApplyFilter(ActionPerformedEvent event) {
        startLoadData();
    }

    @Subscribe(id = "clearBtn", subject = "clickListener")
    public void onClearBtnClick(final ClickEvent<JmixButton> event) {
        DeploymentFilter filter = deploymentFilterDc.getItem();

        filter.setNameLike(null);
        filter.setDeploymentAfter(null);
        filter.setDeploymentBefore(null);

        queryParamBinder.resetParameters();
        startLoadData();
    }

    protected void initFilterFormStyles() {
        filterFormLayout.getOwnComponents().forEach(component -> component.addClassName(LumoUtility.Padding.Top.XSMALL));
        filterPanel.addClassNames(LumoUtility.Padding.Top.XSMALL, LumoUtility.Padding.Left.MEDIUM,
                LumoUtility.Padding.Bottom.XSMALL, LumoUtility.Padding.Right.MEDIUM,
                LumoUtility.Border.ALL, LumoUtility.BorderRadius.LARGE, LumoUtility.BorderColor.CONTRAST_20);
    }

    protected void initFilter() {
        DeploymentFilter filter = metadata.create(DeploymentFilter.class);
        deploymentFilterDc.setItem(filter);
    }

    @Install(to = "deploymentDatasDl", target = Target.DATA_LOADER)
    protected List<DeploymentData> deploymentDatasDlLoadDelegate(LoadContext<DeploymentData> loadContext) {
        LoadContext.Query query = loadContext.getQuery();
        DeploymentFilter filter = deploymentFilterDc.getItemOrNull();

        DeploymentLoadContext context = new DeploymentLoadContext().setFilter(filter);
        if (query != null) {
            context.setFirstResult(query.getFirstResult())
                    .setMaxResults(query.getMaxResults())
                    .setSort(query.getSort());
        }

        return loadItemsWithStateHandling(() -> deploymentService.findAll(context));
    }

    @Supply(to = "deploymentsDataGrid.actions", subject = "renderer")
    protected Renderer<DeploymentData> deploymentsDataGridActionsRenderer() {
        return new ComponentRenderer<>((deploymentData) -> {
            DeploymentListItemActionsFragment actionsFragment =
                    fragments.create(this, DeploymentListItemActionsFragment.class);
            actionsFragment.setDeploymentData(deploymentData);
            return actionsFragment;
        });
    }

    @Subscribe("deploymentsDataGrid.bulkRemove")
    protected void onDeploymentsDataGridBulkRemove(final ActionPerformedEvent event) {
        Set<DeploymentData> selectedItems = deploymentsDataGrid.getSelectedItems();
        if (selectedItems.isEmpty()) {
            return;
        }

        if (selectedItems.size() == 1) {
            dialogWindows.view(this, DeleteDeploymentView.class)
                    .withAfterCloseListener(closeEvent -> {
                        if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                            startLoadData();
                        }
                    })
                    .withViewConfigurer(view -> view.setDeploymentId(
                            deploymentsDataGrid.getSingleSelectedItem().getId()))
                    .build()
                    .open();
            return;
        }

        dialogWindows.view(this, BulkDeleteDeploymentView.class)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                        startLoadData();
                    }
                })
                .withViewConfigurer(view -> view.setDeployments(selectedItems))
                .build()
                .open();
    }

    @Override
    protected void loadData() {
        deploymentDatasDl.load();
    }

    @Subscribe("deploymentsDataGrid.refresh")
    public void onDeploymentsDataGridRefresh(final ActionPerformedEvent event) {
        startLoadData();
    }

    protected void addFilterValueChangeListeners(ComponentContainer componentContainer) {
        for (Component component : componentContainer.getOwnComponents()) {
            if (component instanceof HasValue<?, ?> hasValue) {
                hasValue.addValueChangeListener(valueChangeEvent -> {
                    if (valueChangeEvent.isFromClient()) {
                        startLoadData();
                    }
                });
            }
        }
    }
}
