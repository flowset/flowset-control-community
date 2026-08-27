/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.user.detail;

import io.flowset.control.test_support.AbstractUiTest;
import io.flowset.control.test_support.property.ControlUiTestingProperties;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.user.UserDetailView;
import io.flowset.control.test_support.ui.view.user.UserListView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.jmix.masquerade.JConditions.EXIST;

@DisplayName("Actions on User detail view")
public class UserDetailViewActionsUiTest extends AbstractUiTest {

    @Autowired
    ControlUiTestingProperties uiProperties;

    @Test
    @DisplayName("Save action closes the User detail view")
    void givenExistingUser_whenSaveAndClose_thenDetailViewClosed() {
        // given
        MainView mainView = loginAsAdmin();
        UserListView listView = mainView.openUserListView();

        // when
        UserDetailView detailView = listView.openUserDetailView(uiProperties.getAdminUsername());
        detailView.getSaveAndCloseBtn().click();

        // then
        detailView.shouldNotBe(EXIST);
    }

    @Test
    @DisplayName("Close action closes the User detail view")
    void givenExistingUser_whenClose_thenDetailViewClosed() {
        // given
        MainView mainView = loginAsAdmin();
        UserListView listView = mainView.openUserListView();

        // when
        UserDetailView detailView = listView.openUserDetailView(uiProperties.getAdminUsername());
        detailView.getCloseBtn().click();

        // then
        detailView.shouldNotBe(EXIST);
    }
}
