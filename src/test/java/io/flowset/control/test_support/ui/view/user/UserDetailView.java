/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.user;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.PasswordField;
import io.jmix.masquerade.component.TextField;
import io.jmix.masquerade.sys.View;
import lombok.Getter;

/**
 * Wrapper for the User detail view opened as a route view.
 * Source view: {@link io.flowset.control.view.user.UserDetailView}
 */
@Getter
@TestView(id = "User.detail")
public class UserDetailView extends View<UserDetailView> {

    @TestComponent(path = "usernameField")
    private TextField usernameField;

    @TestComponent(path = "firstNameField")
    private TextField firstNameField;

    @TestComponent(path = "lastNameField")
    private TextField lastNameField;

    @TestComponent(path = "emailField")
    private TextField emailField;

    @TestComponent(path = "passwordField")
    private PasswordField passwordField;

    @TestComponent(path = "confirmPasswordField")
    private PasswordField confirmPasswordField;

    @TestComponent(path = "saveAndCloseBtn")
    private Button saveAndCloseBtn;

    @TestComponent(path = "closeBtn")
    private Button closeBtn;
}
