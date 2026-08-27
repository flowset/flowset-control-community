/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.user;

import io.jmix.masquerade.TestView;
import io.jmix.masquerade.sys.View;

/**
 * Wrapper for the Jmix add-on User substitution view (route view).
 * Source view: {@link io.jmix.securityflowui.view.usersubstitution.UserSubstitutionView}
 */
@TestView(id = "sec_UserSubstitution.view")
public class UserSubstitutionView extends View<UserSubstitutionView> {
}
