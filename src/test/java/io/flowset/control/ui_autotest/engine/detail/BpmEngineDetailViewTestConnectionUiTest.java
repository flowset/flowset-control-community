/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.engine.detail;

import io.flowset.control.test_support.EnabledOnNoAuthentication;
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
import java.util.UUID;

import static com.codeborne.selenide.Condition.text;
import static io.flowset.control.test_support.ui.UiTestSupport.TEST_ENGINE_CONNECTION_WAIT_DURATION_SEC;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@DisplayName("Test connection action for BPM engine")
@WithRunningExternalEngine(save = false)
public class BpmEngineDetailViewTestConnectionUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @EnabledOnNoAuthentication
    @Test
    @DisplayName("Successful notification is shown if successful test connection to engine")
    public void givenNewEngine_whenTestConnection_thenSuccessfulNotificationShown() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        mainView.openBpmEngineListView().getCreateButton().click();

        BpmEngineDetailView detailView = $j(BpmEngineDetailView.class).exists();
        detailView.getNameField().setValue("New engine");
        detailView.getBaseUrlField().setValue(camunda7.getRestBaseUrl());

        detailView.getTestConnectionBtn().click();

        //then
        Notification notification = $j(Notification.class);

        notification.shouldBe(VISIBLE, Duration.ofSeconds(TEST_ENGINE_CONNECTION_WAIT_DURATION_SEC))
                .shouldHave(notificationTheme(Notification.Theme.SUCCESS))
                .shouldHave(text("Successfully connected to '%s'".formatted(camunda7.getRestBaseUrl())));
    }

    @Test
    @DisplayName("Error notification is shown if engine is unavailable by URL")
    public void givenUnavailableEngineWithValidUrl_whenTestConnection_thenErrorNotificationShown() {
        // given
        MainView mainView = loginAsAdmin();
        String engineUrl = "http://%s.invalid/engine-rest".formatted(UUID.randomUUID());

        // when
        mainView.openBpmEngineListView().getCreateButton().click();

        BpmEngineDetailView detailView = $j(BpmEngineDetailView.class).exists();
        detailView.getNameField().setValue("New engine");
        detailView.getBaseUrlField().setValue(engineUrl);

        detailView.getTestConnectionBtn().click();

        //then
        Notification notification = $j(Notification.class);
        notification.shouldBe(VISIBLE, Duration.ofSeconds(TEST_ENGINE_CONNECTION_WAIT_DURATION_SEC))
                .shouldHave(notificationTheme(Notification.Theme.ERROR))
                .shouldHave(notificationTitle("Unable connect to engine"));
    }

    @Test
    @DisplayName("Error notification is shown if Base URL is not set for engine")
    public void givenEngineWithoutUrl_whenTestConnection_thenErrorNotificationShown() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        mainView.openBpmEngineListView().getCreateButton().click();

        BpmEngineDetailView detailView = $j(BpmEngineDetailView.class).exists();
        detailView.getNameField().setValue("New engine");

        detailView.getTestConnectionBtn().click();

        //then
        Notification notification = $j(Notification.class);
        notification.shouldBe(VISIBLE)
                .shouldHave(notificationTheme(Notification.Theme.ERROR))
                .shouldHave(notificationTitle("Unable connect to engine"))
                .shouldHave(notificationMessage("Incorrect URL specified: "));
    }

    @Test
    @DisplayName("Error notification is shown if Base URL is not valid URL")
    public void givenEngineWithInvalidUrl_whenTestConnection_thenErrorNotificationShown() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        mainView.openBpmEngineListView().getCreateButton().click();

        BpmEngineDetailView detailView = $j(BpmEngineDetailView.class).exists();
        detailView.getNameField().setValue("New engine");
        detailView.getBaseUrlField().setValue("invalid-url");
        detailView.getTestConnectionBtn().click();

        //then
        Notification notification = $j(Notification.class);
        notification.shouldBe(VISIBLE)
                .shouldHave(notificationTheme(Notification.Theme.ERROR))
                .shouldHave(notificationTitle("Unable connect to engine"))
                .shouldHave(notificationMessage("Incorrect URL specified: %s".formatted("invalid-url")));
    }

    @Test
    @DisplayName("Error notification is shown if header name is not set for HTTP header auth")
    public void givenEngineWithHeaderAuthAndWithoutHeaderName_whenTestConnection_thenErrorNotificationShown() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        mainView.openBpmEngineListView().getCreateButton().click();

        BpmEngineDetailView detailView = $j(BpmEngineDetailView.class).exists();
        detailView.getNameField().setValue("New engine");
        detailView.getBaseUrlField().setValue(camunda7.getRestBaseUrl());
        detailView.getAuthEnabledField().setChecked(true);
        detailView.getAuthTypeGroup().select("HTTP header");

        detailView.getTestConnectionBtn().click();

        //then
        Notification notification = $j(Notification.class);
        notification.shouldBe(VISIBLE)
                .shouldHave(notificationTheme(Notification.Theme.ERROR))
                .shouldHave(notificationTitle("Unable connect to engine"))
                .shouldHave(notificationMessage("Authentication header name isn't specified"));
    }

    @Test
    @DisplayName("Error notification is shown if username is not set")
    public void givenEngineWithBasicAuthAndWithoutUsername_whenTestConnection_thenErrorNotificationShown() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        mainView.openBpmEngineListView().getCreateButton().click();

        BpmEngineDetailView detailView = $j(BpmEngineDetailView.class).exists();
        detailView.getNameField().setValue("New engine");
        detailView.getBaseUrlField().setValue(camunda7.getRestBaseUrl());
        detailView.getAuthEnabledField().setChecked(true);
        detailView.getAuthTypeGroup().select("Basic");
        detailView.getTestConnectionBtn().click();

        //then
        Notification notification = $j(Notification.class);
        notification.shouldBe(VISIBLE)
                .shouldHave(notificationTheme(Notification.Theme.ERROR))
                .shouldHave(notificationTitle("Unable connect to engine"))
                .shouldHave(notificationMessage("Authentication username isn't specified"));
    }

    @Test
    @DisplayName("Error notification is shown if password is not set")
    public void givenEngineWithBasicAuthAndWithoutPassword_whenTestConnection_thenErrorNotificationShown() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        mainView.openBpmEngineListView().getCreateButton().click();

        BpmEngineDetailView detailView = $j(BpmEngineDetailView.class).exists();
        detailView.getNameField().setValue("New engine");
        detailView.getBaseUrlField().setValue(camunda7.getRestBaseUrl());
        detailView.getAuthEnabledField().setChecked(true);
        detailView.getAuthTypeGroup().select("Basic");
        detailView.getBasicAuthUsernameField().setValue("username");

        detailView.getTestConnectionBtn().click();

        //then
        Notification notification = $j(Notification.class);
        notification.shouldBe(VISIBLE)
                .shouldHave(notificationTheme(Notification.Theme.ERROR))
                .shouldHave(notificationTitle("Unable connect to engine"))
                .shouldHave(notificationMessage("Authentication password isn't specified"));
    }
}
