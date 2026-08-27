/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.engine;

import com.codeborne.selenide.SelenideElement;
import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.EntityComboBox;
import io.jmix.masquerade.component.TextField;
import io.jmix.masquerade.sys.View;
import lombok.Getter;
import org.openqa.selenium.support.FindBy;

/**
 * Wrapper for the Engine Connection Settings view (BPM engine badge -> Advanced mode).
 * Source view: {@link io.flowset.control.view.engineconnectionsettings.EngineConnectionSettingsView}
 */
@Getter
@TestView(id = "EngineConnectionSettingsView")
public class EngineConnectionSettingsView extends View<EngineConnectionSettingsView> {

    @TestComponent(path = "bpmEnginesComboBox")
    private EntityComboBox bpmEnginesComboBox;

    @TestComponent(path = "baseUrlField")
    private TextField baseUrlField;

    @TestComponent(path = "environmentTypeField")
    private TextField environmentTypeField;

    @FindBy(id = "authenticationGroupHeader")
    private SelenideElement authenticationGroupHeader;

    @TestComponent(path = "authenticationTypeField")
    private TextField authenticationTypeField;

    @FindBy(id = "basicAuthSettingsHBox")
    private SelenideElement basicAuthSettingsHBox;

    @FindBy(id = "customHttpHeaderSettingsVBox")
    private SelenideElement customHttpHeaderSettingsVBox;

    @TestComponent(path = "updateEngineBtn")
    private Button updateEngineBtn;

    @TestComponent(path = "closeBtn")
    private Button closeBtn;

    @TestComponent(path = "testConnectionBtn")
    private Button testConnectionBtn;
}
