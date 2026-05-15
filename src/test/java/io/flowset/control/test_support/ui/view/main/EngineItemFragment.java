/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.main;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.component.Unknown;
import io.jmix.masquerade.sys.Composite;
import org.openqa.selenium.By;

import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byUiTestId;

/**
 * Wrapper for a single engine item rendered inside the {@code engineListBox} of the engine selection popover.
 * Source component: {@link io.flowset.control.view.main.selectenginepopover.EngineItemFragment}
 * <p>
 * Wired via masquerade — must be constructed through {@code $j(EngineItemFragment.class, By)} so that
 * the {@link TestComponent}-annotated children get injected.
 */
public class EngineItemFragment extends Composite<EngineItemFragment> {

    private static final By ENGINE_NAME_BY = byUiTestId("engineName");

    @TestComponent(path = "engineName")
    private Unknown engineName;

    @TestComponent(path = "urlField")
    private Unknown urlField;

    @TestComponent(path = "envField")
    private Unknown envField;

    /**
     * @return engine name without the trailing {@code " (type)"} suffix added by
     * {@link io.flowset.control.view.main.selectenginepopover.EngineItemFragment#setItem}.
     * For example, returns {@code "Non-default engine"} when the displayed text is
     * {@code "Non-default engine (Camunda 7)"}.
     */
    public String getEngineName() {
        return parseEngineName(getDisplayedName());
    }

    /**
     * @return full text of the engine-name span — formatted as {@code "{name} ({type})"} by the
     * production fragment, e.g. {@code "Non-default engine (Camunda 7)"}.
     */
    public String getDisplayedName() {
        return engineName.getDelegate()
                .shouldBe(VISIBLE)
                .text();
    }

    /**
     * @return base URL displayed below the engine name, e.g. {@code "http://localhost:8080/engine-rest"}.
     */
    public String getUrl() {
        return urlField.getDelegate()
                .shouldBe(VISIBLE)
                .text();
    }

    /**
     * @return wrapper for the environment badge ({@code envField}) shown next to the URL.
     */
    public String getEnvironment() {
        return envField.getDelegate()
                .shouldBe(VISIBLE)
                .text();
    }

    /**
     * Strips the trailing {@code " (type)"} suffix from a displayed name string.
     */
    public static String parseEngineName(String displayed) {
        int parenIdx = displayed.lastIndexOf(" (");
        return parenIdx > 0 ? displayed.substring(0, parenIdx) : displayed;
    }
}
