/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processinstance;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.component.Checkbox;
import io.jmix.masquerade.component.ComboBox;
import io.jmix.masquerade.sys.Composite;
import lombok.Getter;
import org.openqa.selenium.By;

import static org.openqa.selenium.By.cssSelector;

/**
 * Wrapper for the dialog of the State column filter in the process instance list.
 * Source component: {@link io.flowset.control.view.processinstance.filter.ProcessInstanceStateHeaderFilter}
 */
@Getter
public class ProcessInstanceStateFilterDialog extends Composite<ProcessInstanceStateFilterDialog> {

    public static final By OVERLAY = cssSelector("vaadin-dialog.column-filter-dialog[opened]");

    @TestComponent(path = "stateComboBox")
    private ComboBox stateComboBox;

    @TestComponent(path = "withIncidentsCheckBox")
    private Checkbox withIncidentsCheckbox;
}
