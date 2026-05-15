/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.engine;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.*;
import io.jmix.masquerade.sys.View;
import lombok.Getter;

/**
 * Wrapper for the BPM engine detail view.
 * Source view: {@link io.flowset.control.view.bpmengine.BpmEngineDetailView}
 */
@Getter
@TestView(id = "BpmEngine.detail")
public class BpmEngineDetailView extends View<BpmEngineDetailView> {

    @TestComponent(path = "nameField")
    private TextField nameField;

    @TestComponent(path = "typeField")
    private Select typeField;

    @TestComponent(path = "defaultField")
    private Checkbox defaultField;

    @TestComponent(path = "baseUrlField")
    private TextField baseUrlField;

    @TestComponent(path = "environmentTypeField")
    private ComboBox environmentTypeField;

    @TestComponent(path = "details")
    private Unknown details;

    @TestComponent(path = "authEnabledField")
    private Checkbox authEnabledField;

    @TestComponent(path = "authTypeGroup")
    private RadioButtonGroup authTypeGroup;

    @TestComponent(path = "authBox")
    private Unknown authBox;

    @TestComponent(path = "basicAuthUsername")
    private TextField basicAuthUsernameField;

    @TestComponent(path = "basicAuthPassword")
    private PasswordField basicAuthPasswordField;


    @TestComponent(path = "httpHeaderName")
    private TextField httpHeaderNameField;

    @TestComponent(path = "httpHeaderValue")
    private PasswordField httpHeaderValueField;

    @TestComponent(path = "saveAndCloseButton")
    private Button saveAndCloseButton;

    @TestComponent(path = "closeButton")
    private Button closeButton;

    @TestComponent(path = "testConnectionBtn")
    private Button testConnectionBtn;
}
