/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.export;

import io.jmix.core.DateTimeTransformations;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.Notifications;
import io.jmix.gridexportflowui.GridExportProperties;
import io.jmix.gridexportflowui.exporter.entitiesloader.AllEntitiesLoaderFactory;
import io.jmix.gridexportflowui.exporter.excel.ExcelExporter;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Primary
@Component("control_ExcelExporter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ControlExcelExporter extends ExcelExporter {

    public ControlExcelExporter(GridExportProperties gridExportProperties,
                                Notifications notifications, AllEntitiesLoaderFactory allEntitiesLoaderFactory,
                                CurrentAuthentication currentAuthentication,
                                DateTimeTransformations dateTimeTransformations) {
        super(gridExportProperties, notifications, allEntitiesLoaderFactory, currentAuthentication, dateTimeTransformations);
    }

    @Override
    protected void createWorkbookWithSheet() {
        if (gridExportProperties.getExcel().isUseSxssf()) {
            // A shared strings table is required to keep the bold font of header cells: without it
            // SXSSF writes string cells as inline strings and drops rich text formatting runs.
            wb = new SXSSFWorkbook(null, SXSSFWorkbook.DEFAULT_WINDOW_SIZE, false, true);
        } else {
            wb = new XSSFWorkbook();
        }

        sheet = wb.createSheet("Export");
    }
}
