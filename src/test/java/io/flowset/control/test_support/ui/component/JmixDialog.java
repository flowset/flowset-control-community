/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.component;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.sys.Composite;
import lombok.Getter;
import org.openqa.selenium.By;

import static org.openqa.selenium.By.cssSelector;

/**
 * Wrapper for Jmix dialog opening with {@link io.jmix.flowui.Dialogs}.
 * Source component: {@link com.vaadin.flow.component.dialog.Dialog}
 */
@Getter
public class JmixDialog extends Composite<JmixDialog> {

    public static final By OVERLAY = cssSelector("vaadin-dialog-overlay[role='dialog']");

    @TestComponent(path = "yes")
    private Button yesBtn;

    @TestComponent(path = "no")
    private Button noBtn;

    @TestComponent(path = "ok")
    private Button okBtn;

    @TestComponent(path = "cancel")
    private Button cancelBtn;

    @TestComponent(path = "close")
    private Button closeBtn;
}
