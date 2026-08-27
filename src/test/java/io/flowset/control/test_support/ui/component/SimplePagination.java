/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.component;

import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.Unknown;
import io.jmix.masquerade.sys.Composite;
import lombok.Getter;
import org.openqa.selenium.support.FindBy;

/**
 * Test wrapper for the Jmix {@link io.jmix.flowui.component.pagination.SimplePagination} component.
 */
@Getter
public class SimplePagination extends Composite<SimplePagination> {

    @FindBy(css = ".jmix-simple-pagination-navigation-button.first")
    private Button firstButton;

    @FindBy(css = ".jmix-simple-pagination-navigation-button.prev")
    private Button previousButton;

    @FindBy(css = ".jmix-simple-pagination-navigation-button.next")
    private Button nextButton;

    @FindBy(css = ".jmix-simple-pagination-navigation-button.last")
    private Button lastButton;

    @FindBy(css = ".jmix-simple-pagination-status")
    private Button rowsStatusLabel;

    @FindBy(css = ".jmix-simple-pagination-status-bar")
    private Unknown rowsStatusBox;

    @FindBy(css = ".jmix-simple-pagination-total-count")
    private Unknown totalCountLabel;
}
