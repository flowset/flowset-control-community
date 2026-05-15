/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.condition.grid;

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebElementCondition;
import org.openqa.selenium.WebElement;

import static io.flowset.control.test_support.ui.TagNames.GRID_COLUMN_SORTER_BY;

/**
 * Asserts the column at {@code columnIndex} is not sortable — its header cell does
 * not render a {@code <vaadin-grid-sorter>} element at all.
 */
public class ColumnNotSortable extends WebElementCondition {

    private final int columnIndex;

    public ColumnNotSortable(int columnIndex) {
        super("column[" + columnIndex + "] not sortable");
        this.columnIndex = columnIndex;
    }

    @Override
    public CheckResult check(Driver driver, WebElement element) {
        SelenideElement sorter = GridConditionSupport.resolveGrid(element)
                .getHeaderCellByIndex(0, columnIndex)
                .getCellContent()
                .find(GRID_COLUMN_SORTER_BY);
        return new CheckResult(!sorter.exists(),
                sorter.exists() ? "sorter present on column " + columnIndex : "no sorter");
    }
}
