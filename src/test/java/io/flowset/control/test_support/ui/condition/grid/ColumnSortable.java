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
 * Asserts the column at {@code columnIndex} is sortable — its header cell renders
 * a visible {@code <vaadin-grid-sorter>} element.
 */
public class ColumnSortable extends WebElementCondition {

    private final int columnIndex;

    public ColumnSortable(int columnIndex) {
        super("column[" + columnIndex + "] sortable");
        this.columnIndex = columnIndex;
    }

    @Override
    public CheckResult check(Driver driver, WebElement element) {
        SelenideElement sorter = GridConditionSupport.resolveGrid(element)
                .getHeaderCellByIndex(0, columnIndex)
                .getCellContent()
                .find(GRID_COLUMN_SORTER_BY);
        if (!sorter.exists()) {
            return new CheckResult(false, "no sorter on column " + columnIndex);
        }
        if (!sorter.isDisplayed()) {
            return new CheckResult(false, "sorter on column " + columnIndex + " not displayed");
        }
        return new CheckResult(true, "sortable");
    }
}
