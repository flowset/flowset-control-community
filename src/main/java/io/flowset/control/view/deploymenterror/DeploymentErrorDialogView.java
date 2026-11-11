/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.deploymenterror;


import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.jmix.core.LoadContext;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Target;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.jmix.gridexportflowui.action.ExcelExportAction;
import io.jmix.gridexportflowui.exporter.ExportMode;
import io.flowset.control.entity.deployment.ResourceDeploymentReport;
import io.flowset.control.entity.deployment.ResourceValidationError;
import org.apache.commons.io.FilenameUtils;

import java.util.List;

@ViewController(id = "DeploymentErrorDialogView")
@ViewDescriptor(path = "deployment-error-dialog-view.xml")
@DialogMode(width = "60em")
public class DeploymentErrorDialogView extends StandardView {

    @ViewComponent("resourceValidationErrorsDataGrid.excelExport")
    protected ExcelExportAction resourceValidationErrorsDataGridExcelExport;

    @ViewComponent
    protected InstanceContainer<ResourceDeploymentReport> deploymentReportDc;

    @ViewComponent
    protected CollectionLoader<ResourceValidationError> validationErrorsDl;

    @ViewComponent
    protected HorizontalLayout notDeployedBox;

    public void setResourceReport(ResourceDeploymentReport report) {
        deploymentReportDc.setItem(report);
    }

    /**
     * Shows or hides the "Resource not deployed" message: it makes sense only when the engine rejected the resource,
     * not for the problems found before the deployment.
     */
    public void setEngineRejected(boolean engineRejected) {
        notDeployedBox.setVisible(engineRejected);
    }

    @Subscribe
    protected void onBeforeShow(final BeforeShowEvent event) {
        validationErrorsDl.load();

        resourceValidationErrorsDataGridExcelExport.setAvailableExportModes(List.of(ExportMode.ALL_ROWS));
        ResourceDeploymentReport item = deploymentReportDc.getItem();
        resourceValidationErrorsDataGridExcelExport.setFileName(FilenameUtils.getBaseName(item.getFilename()) + "-errors");
    }

    @Install(to = "validationErrorsDl", target = Target.DATA_LOADER)
    protected List<ResourceValidationError> validationErrorsDlLoadDelegate(final LoadContext<ResourceValidationError> loadContext) {
        //workaround for Excel export action because it does not work with container without loader
        return deploymentReportDc.getItem().getValidationErrors();
    }

}