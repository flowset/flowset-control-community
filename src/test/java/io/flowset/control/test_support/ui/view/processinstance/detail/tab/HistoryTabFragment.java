/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processinstance.detail.tab;

import com.codeborne.selenide.ElementsCollection;
import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.component.TabSheet;
import io.jmix.masquerade.sys.Composite;
import lombok.Getter;
import org.openqa.selenium.By;

/**
 * Wrapper for the History tab fragment of the Process instance detail view.
 * Source component: {@link io.flowset.control.view.processinstance.history.HistoryTabFragment}
 */
@Getter
public class HistoryTabFragment extends Composite<HistoryTabFragment> {

    public static final By TAB_BY = By.xpath("./vaadin-tabs/vaadin-tab");

    @TestComponent(path = "historyTabFragmentHistoryTabsheet")
    private TabSheet tabsheet;

    /**
     * Returns the tabs of the history tab sheet in the displayed order.
     *
     * @return the collection of tab elements
     */
    public ElementsCollection getTabs() {
        return tabsheet.getDelegate().$$(TAB_BY);
    }
}
