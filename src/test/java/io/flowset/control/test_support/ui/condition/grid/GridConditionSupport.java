/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.condition.grid;

import io.jmix.masquerade.component.DataGrid;
import org.openqa.selenium.WebElement;

import java.util.Objects;

import static io.jmix.masquerade.JSelectors.byUiTestId;
import static io.jmix.masquerade.Masquerade.$j;
import static io.jmix.masquerade.Masquerade.UI_TEST_ID;

/**
 * Shared helpers for grid-scoped {@link com.codeborne.selenide.WebElementCondition}s.
 * Reconstructs a masquerade {@link DataGrid} wrapper from the {@code <vaadin-grid>}
 * host element passed by Selenide into {@code check(...)}.
 */
public class GridConditionSupport {

    public static DataGrid resolveGrid(WebElement element) {
        return $j(DataGrid.class, byUiTestId(Objects.requireNonNull(element.getDomAttribute(UI_TEST_ID))));
    }
}
