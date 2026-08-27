/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.condition.menu;

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.WebElementCondition;
import org.openqa.selenium.WebElement;

import java.util.List;

import static io.jmix.masquerade.Masquerade.UI_TEST_ID;
import static org.openqa.selenium.By.tagName;
import static org.openqa.selenium.By.xpath;

/**
 * Asserts the menu element contains exactly the specified labels in DOM order.
 */
public class MenuItemsExactly extends WebElementCondition {

    private final List<String> expectedLabels;

    public MenuItemsExactly(List<String> expectedLabels) {
        super("menu items exactly " + expectedLabels);
        this.expectedLabels = expectedLabels;
    }

    @Override
    public CheckResult check(Driver driver, WebElement element) {
        List<String> actual = element
                .findElements(xpath(".//*[substring(@" + UI_TEST_ID + ", string-length(@" + UI_TEST_ID + ") - 7) = 'ListItem']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .map(webElement -> {
                    List<WebElement> vaadinDetails = webElement.findElements(tagName("vaadin-details"));
                    if (!vaadinDetails.isEmpty()) {
                        return vaadinDetails
                                .get(0)
                                .findElement(tagName("vaadin-details-summary"))
                                .getText();
                    }

                    return webElement.getText();
                })
                .toList();
        if (actual.equals(expectedLabels)) {
            return new CheckResult(true, "matched");
        }
        return new CheckResult(false, "expected " + expectedLabels + " but got " + actual);
    }
}
