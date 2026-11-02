/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.component;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebElementCondition;
import io.jmix.masquerade.sys.Composite;

import static com.codeborne.selenide.Condition.cssValue;
import static com.codeborne.selenide.Selectors.shadowCss;

/**
 * Wrapper for the DMN diagram viewer fragment used in decision definition, decision instance and deployment views.
 * Source component: {@link io.flowset.uikit.fragment.dmnviewer.DmnViewerFragment}.
 */
public class DmnViewerFragment extends Composite<DmnViewerFragment> {
    public static final String DMN_VIEWER_TAG = "flowset-control-dmn-viewer";

    public static final WebElementCondition HIGHLIGHTED_CELL = cssValue("background-color", "rgba(224, 242, 251, 1)");

    /**
     * Returns the diagram element by its id: a DRD shape or, in a decision table, the index cell of a rule.
     *
     * @param elementId the id of the element from DMN XML
     * @return the diagram element
     */
    public SelenideElement getDiagramElementById(String elementId) {
        return getDelegate().$(shadowCss("[data-element-id=\"" + elementId + "\"]", DMN_VIEWER_TAG));
    }

    /**
     * Returns the decision table cell of the given rule and input or output clause.
     *
     * @param ruleId   the id of the rule from DMN XML
     * @param columnId the id of the input or output clause from DMN XML
     * @return the decision table cell
     */
    public SelenideElement getDecisionTableCell(String ruleId, String columnId) {
        return getDelegate().$(shadowCss(
                "td.cell[data-row-id=\"" + ruleId + "\"][data-col-id=\"" + columnId + "\"]",
                DMN_VIEWER_TAG));
    }
}
