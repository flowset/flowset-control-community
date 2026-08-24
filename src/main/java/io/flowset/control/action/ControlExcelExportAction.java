/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action;

import io.flowset.control.export.ControlExcelExporter;
import io.jmix.core.TimeSource;
import io.jmix.core.metamodel.datatype.DatatypeFormatter;
import io.jmix.flowui.action.ActionType;
import io.jmix.gridexportflowui.action.ExcelExportAction;
import io.jmix.gridexportflowui.action.ExportAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

/**
 * Extends {@link ExcelExportAction} with extended functionality for exporting data to Excel format.
 * For example, it allows using view name and current time as a file name.
 */
@ActionType(ControlExcelExportAction.ID)
public class ControlExcelExportAction extends ExcelExportAction {
    public static final String ID = "control_excelExport";

    protected boolean useViewNameAsFileName = true;

    protected TimeSource timeSource;
    protected DatatypeFormatter datatypeFormatter;

    public ControlExcelExportAction() {
        this(ID);
    }

    public ControlExcelExportAction(String id) {
        super(id);
    }

    public boolean isUseViewNameAsFileName() {
        return useViewNameAsFileName;
    }

    public void setUseViewNameAsFileName(boolean useViewNameAsFileName) {
        this.useViewNameAsFileName = useViewNameAsFileName;
    }

    @Autowired
    public void setTimeSource(TimeSource timeSource) {
        this.timeSource = timeSource;
    }
    
    @Autowired
    public void setDatatypeFormatter(DatatypeFormatter datatypeFormatter) {
        this.datatypeFormatter = datatypeFormatter;
    }

    @Override
    public void execute() {
        if (useViewNameAsFileName) {
            String pageTitle = getCurrentView().getPageTitle().toLowerCase();
            String currentDateTimeFormatted =  datatypeFormatter.formatDateTime(timeSource.currentTimestamp())
                    .replaceAll("[^a-zA-Z0-9.-]", "_");

            String fileName = "%s_%s".formatted(pageTitle.toLowerCase(), currentDateTimeFormatted);

            dataGridExporter.setFileName(fileName);
        }
        super.execute();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        super.setApplicationContext(applicationContext);
        withExporter(ControlExcelExporter.class);
    }

    protected String getMessage(String id) {
        return messages.getMessage(ExportAction.class, id);
    }

}
