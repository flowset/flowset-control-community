/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.condition;

import com.codeborne.selenide.WebElementCondition;
import io.flowset.control.test_support.ui.condition.grid.*;
import io.flowset.control.test_support.ui.condition.pagination.TotalCount;
import io.flowset.control.test_support.ui.condition.pagination.TotalCountHidden;
import io.flowset.control.test_support.ui.condition.pagination.TotalCountUnknown;
import org.openqa.selenium.By;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Facade for custom Selenide {@link WebElementCondition}s.
 */
public class ControlCondition {

    /**
     * Asserts the grid shows the {@code "No data"} empty-state placeholder.
     */
    public static final WebElementCondition emptyGrid = new EmptyGrid();

    /**
     * Asserts the grid shows the loading placeholder.
     */
    public static final WebElementCondition gridLoading = new GridLoading();

    /**
     * Asserts the simple-pagination total-count label is not visible.
     */
    public static final WebElementCondition totalCountHidden = new TotalCountHidden();

    /**
     * Asserts the simple-pagination total-count label shows the {@code "[?]"} placeholder.
     */
    public static final WebElementCondition totalCountUnknown = new TotalCountUnknown();

    /**
     * Asserts the simple-pagination total-count label shows the resolved numeric value.
     */
    public static WebElementCondition totalCount(int expected) {
        return new TotalCount(expected);
    }

    /**
     * Asserts the total row count in the grid body.
     * <br>
     * <strong>NOTE:</strong> It is different from visible row count in the grid body because
     * Vaadin Grid uses virtualization and shows only ~17-20 rows but not all rows available
     * for showing in the current page of the grid.
     * To get only visible row count use {@link ControlCondition#visibleBodyRowCount}.
     *
     * @param expected expected row count in the data grid page
     * @return instance of condition
     */
    public static WebElementCondition gridBodyRowCount(int expected) {
        return new GridBodyRowCount(expected);
    }

    /**
     * Asserts the visible row count in the grid body.
     * <br>
     * <strong>NOTE:</strong> It is different from total row count in the grid body because
     * Vaadin Grid uses virtualization and shows only ~17-20 rows but not all rows available
     * for showing in the current page of the grid.
     * To get all rows' count for the grid page use {@link ControlCondition#gridBodyRowCount}.
     *
     * @param expected expected visible row count in the data grid page
     * @return instance of condition
     */
    public static WebElementCondition visibleBodyRowCount(int expected) {
        return new VisibleBodyRowCount(expected);
    }

    /**
     * Asserts the all visible grid body rows have the expected text in the specified column.
     *
     * @param columnIndex index of the column to check the text in
     * @param expected    expected text in the cell
     * @return instance of condition
     */
    public static WebElementCondition allBodyRowsHaveCellText(int columnIndex, String expected) {
        return new AllBodyRowsHaveCellText(columnIndex, expected);
    }

    /**
     * Asserts the all visible grid body rows have the expected text in the component with the specified selector in the specified column.
     *
     * @param columnIndex index of the column to check the text in
     * @param selector    selector of the component in the cell to check the text in
     * @param expected    expected text in the cell
     * @return instance of condition
     */
    public static WebElementCondition allBodyRowsHaveCellElementText(int columnIndex, By selector, String expected) {
        return new AllBodyRowsHaveCellElementText(columnIndex, selector, expected);
    }

    /**
     * Asserts any visible grid body row has the expected text in the specified column.
     *
     * @param columnIndex index of the column to check the text in
     * @param expected    expected text in the cell
     * @return instance of condition
     */
    public static WebElementCondition anyBodyRowHaveCellText(int columnIndex, String expected) {
        return new AnyBodyRowsHaveCellText(columnIndex, expected);
    }

    /**
     * Asserts any visible grid body row has the expected text in the component with the specified selector in the specified column.
     *
     * @param columnIndex index of the column to check the text in
     * @param selector    selector of the component in the cell to check the text in
     * @param expected    expected text in the cell
     * @return instance of condition
     */
    public static WebElementCondition anyBodyRowHaveCellElementText(int columnIndex, By selector, String expected) {
        return new AnyBodyRowHaveCellElementText(columnIndex, selector, expected);
    }

    /**
     * Asserts the all visible grid body rows have the expected text in the specified column.
     *
     * @param columnIndex index of the column to check the text in
     * @param expected    expected text in the cell
     * @return instance of condition
     */
    public static WebElementCondition allBodyRowsHaveCellTextIn(int columnIndex, Collection<String> expected) {
        return new AllBodyRowsHaveCellTextIn(columnIndex, expected);
    }

    /**
     * Asserts the all visible grid body rows have one of the expected texts in the component with the specified selector in the specified column.
     *
     * @param columnIndex index of the column to check the text in
     * @param selector    selector of the component in the cell to check the text in
     * @param expected    expected texts one of which should be shown in the cell
     * @return instance of condition
     */
    public static WebElementCondition allBodyRowsHaveCellElementTextIn(int columnIndex, By selector,
                                                                       Collection<String> expected) {
        return new AllBodyRowsHaveCellElementTextIn(columnIndex, selector, expected);
    }

    /**
     * Asserts the visible body rows have, in order, plain cell texts at the specified column
     * equal to the specified texts. Implicitly asserts size.
     *
     * @param columnIndex index of the column to check the texts in the specified order
     * @param expected    expected texts in the specified order
     * @return instance of condition
     */
    public static WebElementCondition bodyCellTextsExactly(int columnIndex, List<String> expected) {
        return new BodyCellTextsExactly(columnIndex, expected);
    }

    /**
     * Asserts the visible body rows have, in order, specified cell texts at the specified column.
     *
     * @param expected a map where the key is the column index and the value is the expected texts
     * @return instance of condition
     */
    public static WebElementCondition bodyRowWithCellTexts(Map<Integer, String> expected) {
        return new BodyRowWithCellTexts(expected);
    }

    /**
     * Asserts the visible body rows have, in order, specified cell texts at the specified column.
     *
     * @param columnIndex grid header column index
     * @param selector    CSS selector for the cell content element
     * @param expected    expected texts in the specified order
     * @return instance of condition
     */
    public static WebElementCondition bodyCellElementTextsExactly(int columnIndex, By selector,
                                                                  List<String> expected) {
        return new BodyCellElementTextsExactly(columnIndex, selector, expected);
    }

    /**
     * Asserts the header cell texts in the grid. Empty cells are ignored.
     *
     * @param expected a collection of expected texts show in header cells
     * @return instance of condition
     */
    public static WebElementCondition headerCellTexts(Collection<String> expected) {
        return new HeaderCellTexts(expected);
    }

    /**
     * Asserts the header cell texts in the grid exactly.
     *
     * @param expected a collection of expected texts show in header cells
     * @return instance of condition
     */
    public static WebElementCondition headerCellTextsExactly(List<String> expected) {
        return new HeaderCellTextsExactly(expected);
    }

    /**
     * Asserts the header column with a specified index in the data grid is sortable.
     *
     * @param columnIndex grid header column index
     * @return instance of condition
     */
    public static WebElementCondition columnSortable(int columnIndex) {
        return new ColumnSortable(columnIndex);
    }

    /**
     * Asserts the header column with a specified index in the data grid is not sortable.
     *
     * @param columnIndex grid header column index
     * @return instance of condition
     */
    public static WebElementCondition columnNotSortable(int columnIndex) {
        return new ColumnNotSortable(columnIndex);
    }
}
