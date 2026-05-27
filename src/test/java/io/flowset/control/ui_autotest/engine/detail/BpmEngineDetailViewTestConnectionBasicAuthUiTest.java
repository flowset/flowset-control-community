/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.engine.detail;

import io.flowset.control.test_support.EnabledOnBasicAuthentication;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.engine.BpmEngineDetailView;
import io.jmix.masquerade.component.Notification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.codeborne.selenide.Condition.text;
import static io.flowset.control.test_support.ui.UiTestSupport.TEST_ENGINE_CONNECTION_WAIT_DURATION_SEC;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;


@DisplayName("Test connection action for BPM engine with basic authentication")
@WithRunningExternalEngine(save = false)
@EnabledOnBasicAuthentication
public class BpmEngineDetailViewTestConnectionBasicAuthUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine externalEngine;

    @Test
    @DisplayName("Successful notification is shown if successfully test connection to engine")
    public void givenNewEngine_whenTestConnection_thenSuccessfulNotificationShown() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        mainView.openBpmEngineListView().getCreateButton().click();

        BpmEngineDetailView detailView = $j(BpmEngineDetailView.class).exists();
        detailView.getNameField().setValue("New engine");
        detailView.getBaseUrlField().setValue(externalEngine.getRestBaseUrl());
        detailView.getAuthEnabledField().setChecked(true);
        detailView.getAuthTypeGroup().select("Basic");
        detailView.getBasicAuthUsernameField().setValue(externalEngine.getBasicAuthUsername());
        detailView.getBasicAuthPasswordField().setValue(externalEngine.getBasicAuthPassword());

        detailView.getTestConnectionBtn().click();

        //then
        Notification notification = $j(Notification.class);

        notification.shouldBe(VISIBLE, Duration.ofSeconds(TEST_ENGINE_CONNECTION_WAIT_DURATION_SEC))
                .shouldHave(notificationTheme(Notification.Theme.SUCCESS))
                .shouldHave(text("Successfully connected to '%s'".formatted(externalEngine.getRestBaseUrl())));
    }

    @Test
    @DisplayName("Error notification is shown if basic auth credentials are incorrect")
    public void givenUnavailableEngineWithInvalidCredentials_whenTestConnection_thenErrorNotificationShown() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        mainView.openBpmEngineListView().getCreateButton().click();

        BpmEngineDetailView detailView = $j(BpmEngineDetailView.class).exists();
        detailView.getNameField().setValue("New engine");
        detailView.getBaseUrlField().setValue(externalEngine.getRestBaseUrl());
        detailView.getAuthEnabledField().setChecked(true);
        detailView.getAuthTypeGroup().select("Basic");
        detailView.getBasicAuthUsernameField().setValue("username");
        detailView.getBasicAuthPasswordField().setValue("password");

        detailView.getTestConnectionBtn().click();

        //then
        Notification notification = $j(Notification.class);
        notification.shouldBe(VISIBLE, Duration.ofSeconds(TEST_ENGINE_CONNECTION_WAIT_DURATION_SEC))
                .shouldHave(notificationTheme(Notification.Theme.ERROR))
                .shouldHave(notificationTitle("Unable connect to engine"))
                .shouldHave(notificationMessageContains("Response code: 401"));
    }


}
