/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.main;

import io.flowset.control.test_support.ui.view.engine.EngineConnectionSettingsView;
import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.Unknown;
import io.jmix.masquerade.sys.Composite;
import lombok.Getter;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.visible;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byChained;
import static io.jmix.masquerade.Masquerade.$j;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

/**
 * Wrapper for the engine status badge fragment shown in the main view header.
 * Source component: {@link io.flowset.control.view.main.EngineStatusBadgeFragment}
 */
@Getter
public class EngineStatusBadgeFragment extends Composite<EngineStatusBadgeFragment> {
    public static final By ENGINE_POPOVER_BY = cssSelector("vaadin-popover-overlay.select-engine-popover");

    public static final By POPOVER_CONTENT_BY = byChained(ENGINE_POPOVER_BY,
            className("engine-popover-content"));

    @TestComponent(path = "connectionStatusText")
    private Unknown connectionStatusText;

    @TestComponent(path = "viewEngineConfigBtn")
    private Button viewEngineConfigBtn;

    public SelectEnginePopoverContent openSelectEnginePopover() {
        viewEngineConfigBtn.shouldBe(VISIBLE)
                .click();

        return $j(SelectEnginePopoverContent.class, ENGINE_POPOVER_BY)
                .shouldBe(visible);
    }

    public EngineConnectionSettingsView openEngineConnectionView() {
        SelectEnginePopoverContent selectEnginePopover = openSelectEnginePopover();

        selectEnginePopover.getAdvancedModeBtn().click();

        return $j(EngineConnectionSettingsView.class)
                .exists();
    }
}
