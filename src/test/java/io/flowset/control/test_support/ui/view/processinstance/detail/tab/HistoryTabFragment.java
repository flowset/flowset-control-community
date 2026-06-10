/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processinstance.detail.tab;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.component.TabSheet;
import io.jmix.masquerade.sys.Composite;
import lombok.Getter;

/**
 * Wrapper for the History tab fragment of the Process instance detail view.
 * Source component: {@link io.flowset.control.view.processinstance.history.HistoryTabFragment}
 */
@Getter
public class HistoryTabFragment extends Composite<HistoryTabFragment> {

    @TestComponent(path = "historyTabFragmentHistoryTabsheet")
    private TabSheet tabsheet;
}
