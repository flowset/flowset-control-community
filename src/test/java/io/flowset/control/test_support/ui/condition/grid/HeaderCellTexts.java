/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.condition.grid;

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.WebElementCondition;
import io.jmix.masquerade.component.DataGrid;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebElement;

import java.util.Collection;
import java.util.List;

import static io.flowset.control.test_support.ui.UiTestSupport.getHeaderCells;
import static io.flowset.control.test_support.ui.condition.grid.GridConditionSupport.resolveGrid;

/**
 * Asserts the visible non-empty header cell texts include every value from
 * {@code expected} collection. Cells with empty rendered text are excluded from the comparison.
 * For strict equality use {@link HeaderCellTextsExactly}.
 */
public class HeaderCellTexts extends WebElementCondition {

    private final Collection<String> expected;

    public HeaderCellTexts(Collection<String> expected) {
        super("non-empty header cell texts include " + expected);
        this.expected = expected;
    }

    @Override
    public CheckResult check(Driver driver, WebElement element) {
        List<DataGrid.Cell> cells = getHeaderCells(resolveGrid(element));
        List<String> actual = cells.stream()
                .map(c -> c.getCellContent().getText())
                .filter(StringUtils::isNotEmpty)
                .toList();

        return new CheckResult(actual.containsAll(expected), "got " + actual);
    }
}
