/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view;

import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.PasswordField;
import io.jmix.masquerade.component.Select;
import io.jmix.masquerade.component.TextField;
import io.jmix.masquerade.sys.View;
import lombok.Getter;
import org.openqa.selenium.support.FindBy;

/**
 * Wrapper for the Login view.
 * Source view: {@link io.flowset.control.view.login.LoginView}
 */
@Getter
@TestView
public class LoginView extends View<LoginView> {

    @FindBy(css = "[slot='submit']")
    private Button submitButton;

    @FindBy(id = "vaadinLoginUsername")
    private TextField usernameField;

    @FindBy(id = "vaadinLoginPassword")
    private PasswordField passwordField;

    @FindBy(id = "localesSelect")
    private Select localeField;
}
