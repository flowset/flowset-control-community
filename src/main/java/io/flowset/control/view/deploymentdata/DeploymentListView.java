package io.flowset.control.view.deploymentdata;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import feign.RequestInterceptor;
import feign.auth.BasicAuthRequestInterceptor;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.flowset.control.entity.deployment.DeploymentData;
import io.flowset.control.entity.engine.AuthType;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.filter.DeploymentFilter;
import io.flowset.control.property.EngineConnectionCheckProperties;
import io.flowset.control.restsupport.FeignClientProvider;
import io.flowset.control.service.deployment.DeploymentLoadContext;
import io.flowset.control.service.deployment.DeploymentService;
import io.flowset.control.service.engine.EngineService;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Set;

@Route(value = "bpmn/deployments", layout = DefaultMainViewParent.class)
@ViewController(id = "bpm_Deployment.list")
@ViewDescriptor(path = "deployment-list-view.xml")
public class DeploymentListView extends StandardListView<DeploymentData> {

    @Autowired
    private DeploymentService deploymentService;
    @Autowired
    private Metadata metadata;

    @ViewComponent
    private InstanceContainer<DeploymentFilter> deploymentFilterDc;
    @ViewComponent
    private CollectionLoader<DeploymentData> deploymentDatasDl;
    @ViewComponent
    private JmixFormLayout filterFormLayout;
    @ViewComponent
    private HorizontalLayout filterPanel;
    @Autowired
    private Fragments fragments;
    @Autowired
    private FeignClientProvider feignClientProvider;
    @Autowired
    private EngineConnectionCheckProperties engineConnectionCheckProperties;
    @Autowired
    private EngineService engineService;
    @ViewComponent
    private DataGrid<DeploymentData> deploymentsDataGrid;
    @Autowired
    private DialogWindows dialogWindows;

    @Subscribe
    public void onInit(final InitEvent event) {
        addClassNames(LumoUtility.Padding.Top.SMALL);
        initFilterFormStyles();
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        initFilter();
        deploymentDatasDl.load();
    }

    @Subscribe("applyFilter")
    public void onApplyFilter(ActionPerformedEvent event) {
        deploymentDatasDl.load();
    }

    @Subscribe(id = "deploymentFilterDc", target = Target.DATA_CONTAINER)
    public void onDeploymentFilterDcItemPropertyChange(
            final InstanceContainer.ItemPropertyChangeEvent<DeploymentFilter> event) {
        deploymentDatasDl.load();
    }

    @Subscribe(id = "clearBtn", subject = "clickListener")
    public void onClearBtnClick(final ClickEvent<JmixButton> event) {
        DeploymentFilter filter = deploymentFilterDc.getItem();

        filter.setNameLike(null);
        filter.setDeploymentAfter(null);
        filter.setDeploymentBefore(null);
    }


    @Nullable
    protected RequestInterceptor createBpmEngineRequestInterceptor(BpmEngine engine) {
        RequestInterceptor requestInterceptor = null;
        if (BooleanUtils.isTrue(engine.getAuthEnabled())) {
            if (engine.getAuthType() == AuthType.BASIC) {
                requestInterceptor = new BasicAuthRequestInterceptor(engine.getBasicAuthUsername(), engine.getBasicAuthPassword());
            } else if (engine.getAuthType() == AuthType.HTTP_HEADER) {
                requestInterceptor = requestTemplate ->
                    requestTemplate.header(engine.getHttpHeaderName(), engine.getHttpHeaderValue());
            }
        }
        return requestInterceptor;
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
            context = context.setFirstResult(query.getFirstResult())
                    .setMaxResults(query.getMaxResults())
                    .setSort(query.getSort());
        }

        return deploymentService.findAll(context);
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
                            deploymentDatasDl.load();
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
                        deploymentDatasDl.load();
                    }
                })
                .withViewConfigurer(view -> view.setDeployments(selectedItems))
                .build()
                .open();
    }
}
