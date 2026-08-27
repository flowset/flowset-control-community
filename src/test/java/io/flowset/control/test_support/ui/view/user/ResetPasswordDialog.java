/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.user;

import io.jmix.masquerade.TestView;
import io.jmix.masquerade.sys.DialogWindow;

/**
 * Wrapper for the Jmix add-on Reset password view opened in dialog mode.
 * Source view: {@link io.jmix.securityflowui.view.resetpassword.ResetPasswordView}
 */
@TestView(id = "resetPasswordView")
public class ResetPasswordDialog extends DialogWindow<ResetPasswordDialog> {
}
