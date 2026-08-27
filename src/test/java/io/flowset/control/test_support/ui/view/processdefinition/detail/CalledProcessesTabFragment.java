/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processdefinition.detail;

import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.component.DataGrid;
import io.jmix.masquerade.sys.Composite;
import lombok.Getter;
import org.openqa.selenium.By;

import static io.flowset.control.test_support.ui.UiTestSupport.getRowByCellContent;
import static io.flowset.control.test_support.ui.UiTestSupport.openGridContextMenu;
import static io.jmix.masquerade.JSelectors.byPath;

/**
 * Wrapper for the Called processes tab fragment inside the Process detail view.
 * Source component: {@link io.flowset.control.view.processdefinition.detail.CalledProcessesTabFragment}
 */
@Getter
public class CalledProcessesTabFragment extends Composite<CalledProcessesTabFragment> {

    public static final By KEY_BUTTON_BY = byPath("root", "keyBtn");
    public static final By PREVIEW_BUTTON_BY = byPath("root", "previewBtn");
    public static final int CALLED_ELEMENT_COLUMN_INDEX = 0;
    public static final int BINDING_COLUMN_INDEX = 1;

    @TestComponent(path = "calledProcessesFragmentCalledProcessesGrid")
    private DataGrid calledProcessesGrid;

    /**
     * Find the first row in the called processes grid related to the called process with the specified process key.
     *
     * @param calledElement called element key displayed in the first column
     * @return found row or throws exception if row not found
     */
    public DataGrid.Row getRowByCalledElement(String calledElement) {
        return getRowByCellContent(calledProcessesGrid, CALLED_ELEMENT_COLUMN_INDEX, cell -> {
            String keyBtnText = cell.getCellContent()
                    .find(KEY_BUTTON_BY)
                    .getText();
            return keyBtnText.equals(calledElement);
        });
    }

    /**
     * Opens the context menu for the BPM engine grid.
     * @return opened grid context menu
     */
    public GridContextMenu openProcessGridContextActions() {
        return openGridContextMenu(calledProcessesGrid);
    }
}
