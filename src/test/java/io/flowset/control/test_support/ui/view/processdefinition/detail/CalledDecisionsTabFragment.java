/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processdefinition.detail;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.component.DataGrid;
import io.jmix.masquerade.sys.Composite;
import lombok.Getter;
import org.openqa.selenium.By;

import static io.flowset.control.test_support.ui.UiTestSupport.getRowByCellContent;
import static io.jmix.masquerade.JSelectors.byPath;

/**
 * Wrapper for the Called decisions tab fragment inside the Process detail view.
 * Source component: {@link io.flowset.control.view.processdefinition.detail.CalledDecisionsTabFragment}
 */
@Getter
public class CalledDecisionsTabFragment extends Composite<CalledDecisionsTabFragment> {

    public static final By KEY_BUTTON_BY = byPath("root", "keyBtn");
    public static final By PREVIEW_BUTTON_BY = byPath("root", "previewBtn");

    public static final int DECISION_REF_COLUMN_INDEX = 0;
    public static final int BINDING_COLUMN_INDEX = 1;
    public static final int VERSION_COLUMN_INDEX = 2;
    public static final int ELEMENT_ID_COLUMN_INDEX = 4;

    @TestComponent(path = "decisionsFragmentDecisionsGrid")
    private DataGrid decisionsGrid;

    /**
     * Find the first row in the called decisions grid related to the decision with the specified decision table.
     *
     * @param decisionRef decision reference displayed in the first column
     * @return found row or throws exception if row not found
     */
    public DataGrid.Row getRowByDecisionRef(String decisionRef) {
        return getRowByCellContent(decisionsGrid, DECISION_REF_COLUMN_INDEX, cell -> {
            String keyBtnText = cell.getCellContent()
                    .find(KEY_BUTTON_BY)
                    .getText();
            return keyBtnText.equals(decisionRef);
        });
    }
}
