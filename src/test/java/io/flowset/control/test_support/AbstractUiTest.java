/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.junit5.TextReportExtension;
import io.flowset.control.FlowsetControlApplication;
import io.flowset.control.test_support.property.ControlUiTestingProperties;
import io.flowset.control.test_support.ui.view.LoginView;
import io.flowset.control.test_support.ui.view.MainView;
import io.jmix.core.UnconstrainedDataManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.Set;

import static com.codeborne.selenide.Selenide.*;
import static io.flowset.control.test_support.Constants.UI_TEST_PROFILE;
import static io.flowset.control.test_support.ui.UiTestSupport.LOGIN_OPEN_WAIT_DURATION_SEC;
import static io.flowset.control.test_support.ui.UiTestSupport.LOGIN_WAIT_DURATION_SEC;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@Slf4j
@SpringBootTest(classes = {
        FlowsetControlApplication.class,
        FlowsetControlUiTestConfiguration.class
})
@ActiveProfiles(value = {UI_TEST_PROFILE})
@Tag("uiTest")
@ExtendWith({TextReportExtension.class})
public abstract class AbstractUiTest {

    @Autowired
    protected ControlUiTestingProperties uiTestingProperties;

    @Autowired
    protected ControlTestDataCreator controlTestDataCreator;

    @Autowired
    protected UnconstrainedDataManager dataManager;

    @AfterEach
    public void afterEach() {
        closeExtraTabs();
        clearBrowserCookies();
        clearBrowserLocalStorage();

        Selenide.open("about:blank");
    }

    /**
     * Close all tabs except the current one.
     */
    private void closeExtraTabs() {
        Set<String> tabHandles = webdriver().driver().getWebDriver().getWindowHandles();
        if (tabHandles.size() <= 1) { // no extra tabs
            return;
        }
        String activeTab = null;
        for (String tab : tabHandles) {
            if (activeTab == null) {
                activeTab = tab;
                continue;
            }
            Selenide.switchTo().window(tab);
            Selenide.closeWindow();
        }
        Selenide.switchTo().window(activeTab);
    }

    /**
     * Login as admin user.
     *
     * @return opened MainView
     * @see ControlUiTestingProperties#getAdminUsername()
     */
    public MainView loginAsAdmin() {
        return loginAs(uiTestingProperties.getAdminUsername(), uiTestingProperties.getAdminPassword());
    }

    /**
     * Login with the specified username and password.
     *
     * @param username username to login
     * @param password password to login
     * @return opened MainView
     */
    public MainView loginAs(String username, String password) {
        open("/");

        LoginView loginView = $j(LoginView.class)
                .shouldBe(VISIBLE, Duration.ofSeconds(LOGIN_OPEN_WAIT_DURATION_SEC));

        loginView.getUsernameField().setValue("").setValue(username);
        loginView.getPasswordField().setValue("").setValue(password);
        loginView.getLocaleField().getDelegate().click();
        loginView.getLocaleField().getItemsOverlay().select("English");

        loginView.getUsernameField().shouldHave(value(username));
        loginView.getPasswordField().shouldHave(value(password));
        loginView.getLocaleField().shouldHave(value("English"));

        loginView.getSubmitButton().click();
        loginView.shouldNotBe(EXIST, Duration.ofSeconds(LOGIN_WAIT_DURATION_SEC));

        MainView mainView = $j(MainView.class);
        return mainView.displayed();
    }


    /**
     * Wrapper around {@link Selenide#open(String)}. Hits the deployed Control's URL.
     */
    protected void open(String relativeUrl) {
        Selenide.open(uiTestingProperties.getControlUrl() + relativeUrl);
    }
}
