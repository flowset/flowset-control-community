/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.main;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.TextField;
import io.jmix.masquerade.component.Unknown;
import io.jmix.masquerade.sys.Composite;
import lombok.Getter;

/**
 * Wrapper for the BPM engine selection popover opened from the engine status badge.
 * Source component: {@link io.flowset.control.view.main.selectenginepopover.SelectEnginePopoverContentFragment}
 */
@Getter
public class SelectEnginePopoverContent extends Composite<SelectEnginePopoverContent> {

    @TestComponent(path = "createBpmEngineBtn")
    public Button createBpmEngineBtn;

    @TestComponent(path = "advancedModeBtn")
    public Button advancedModeBtn;

    @TestComponent(path = "closeBtn")
    public Button closeBtn;

    @TestComponent(path = "searchField")
    public TextField searchField;

    @TestComponent(path = "emptyEnginesBox")
    public Unknown emptyEnginesBox;

    @TestComponent(path = "engineListBox")
    public EngineListBox engineListBox;
}
