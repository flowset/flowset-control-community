/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.main;

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.jmix.masquerade.condition.SpecificCondition;
import io.jmix.masquerade.condition.UnsupportedConditionException;
import io.jmix.masquerade.condition.Value;
import io.jmix.masquerade.condition.ValueContains;
import io.jmix.masquerade.sys.Composite;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static io.jmix.masquerade.JConditions.EXIST;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byChained;
import static io.jmix.masquerade.Masquerade.$j;
import static org.openqa.selenium.By.*;

/**
 * Wrapper for the engine list box shown in the engine selection popover.
 * Part of {@link io.flowset.control.view.main.selectenginepopover.SelectEnginePopoverContentFragment}.
 * Source component: {@link io.jmix.flowui.component.listbox.JmixListBox}
 */
public class EngineListBox extends Composite<EngineListBox> {

    public static final By ITEM_BY = tagName("vaadin-item");
    public static final By SELECTED_ITEM_BY = cssSelector("vaadin-item[selected]");

    /**
     * @return all {@code vaadin-item} child elements of the list box.
     */
    public ElementsCollection getItems() {
        return $$(byChained(by, ITEM_BY));
    }

    public EngineItemFragment findItemByEngineName(String engineName) {
        By engineNameXPath = xpath(".//vaadin-item[.//*[@j-test-id='engineName'"
                + " and starts-with(normalize-space(text()), '%s')]]".formatted(engineName));

        return $j(EngineItemFragment.class, byChained(by, engineNameXPath));
    }


    @Override
    public CheckResult check(SpecificCondition condition) {
        if (condition instanceof Value valueCondition) {
            String currentValue = getSelectedEngineName();
            return new CheckResult(valueCondition.getValue().equals(currentValue), currentValue);
        } else if (condition instanceof ValueContains valueContainsCondition) {
            String currentValue = getSelectedEngineName();
            return new CheckResult(currentValue.contains(valueContainsCondition.getValue()), currentValue);
        }

        throw new UnsupportedConditionException(condition, this);
    }

    /**
     * @return parsed engine name of the currently selected item, or empty string if no item is selected.
     */
    protected String getSelectedEngineName() {
        SelenideElement selected = $(byChained(by, SELECTED_ITEM_BY));
        return selected.shouldBe(EXIST)
                .shouldBe(VISIBLE)
                .text();
    }
}
