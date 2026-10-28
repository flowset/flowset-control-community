/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.bpmengine;

import com.vaadin.flow.component.Component;
import io.flowset.control.FlowsetControlApplication;
import io.flowset.control.entity.engine.AuthType;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.test_support.FlowsetControlTestConfiguration;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup;
import io.jmix.flowui.component.textfield.JmixPasswordField;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The OAuth2 fields of the engine detail view live in a fragment, so the view must validate them itself
 * on save (flowset-control-community#315).
 */
@UiTest
@SpringBootTest(classes = {FlowsetControlApplication.class, FlowuiTestAssistConfiguration.class,
        FlowsetControlTestConfiguration.class})
class BpmEngineDetailViewOAuth2ValidationUiTest {

    static final String ENGINE_NAME_PREFIX = "oauth2-validation-test-";

    @Autowired
    ViewNavigators viewNavigators;
    @Autowired
    UnconstrainedDataManager dataManager;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("main.datasource.url", () -> "jdbc:hsqldb:mem:bpm-engine-oauth2-validation-ui");
        registry.add("main.datasource.username", () -> "sa");
        registry.add("main.datasource.password", () -> "");
        registry.add("main.datasource.driver-class-name", () -> "org.hsqldb.jdbc.JDBCDriver");
    }

    @AfterEach
    void tearDown() {
        dataManager.load(BpmEngine.class)
                .query("e.name like :prefix").parameter("prefix", ENGINE_NAME_PREFIX + "%")
                .list()
                .forEach(dataManager::remove);
    }

    @Test
    @DisplayName("Engine is not saved when the required OAuth2 fields are empty")
    void givenOAuth2Engine_whenSaveWithEmptyFields_thenFieldsInvalidAndViewStaysOpen() {
        BpmEngineDetailView view = openNewOAuth2Engine("empty");

        clickSave(view);

        assertThat((Object) UiTestUtils.getCurrentView()).isSameAs(view);
        assertThat(this.<TypedTextField<String>>authField(view, "oauth2IssuerUriField").isInvalid()).isTrue();
        assertThat(this.<TypedTextField<String>>authField(view, "oauth2ClientIdField").isInvalid()).isTrue();
        assertThat(this.<JmixPasswordField>authField(view, "oauth2ClientSecretField").isInvalid()).isTrue();
        assertThat(loadEngines()).isEmpty();
    }

    @Test
    @DisplayName("Engine is not saved when the Issuer URI is not a URL")
    void givenOAuth2Engine_whenSaveWithInvalidIssuerUri_thenIssuerUriInvalidAndViewStaysOpen() {
        BpmEngineDetailView view = openNewOAuth2Engine("badurl");
        this.<TypedTextField<String>>authField(view, "oauth2IssuerUriField").setTypedValue("not a url");
        this.<TypedTextField<String>>authField(view, "oauth2ClientIdField").setTypedValue("control");
        this.<JmixPasswordField>authField(view, "oauth2ClientSecretField").setValue("secret");

        clickSave(view);

        assertThat((Object) UiTestUtils.getCurrentView()).isSameAs(view);
        assertThat(this.<TypedTextField<String>>authField(view, "oauth2IssuerUriField").isInvalid()).isTrue();
        assertThat(this.<TypedTextField<String>>authField(view, "oauth2ClientIdField").isInvalid()).isFalse();
        assertThat(loadEngines()).isEmpty();
    }

    @Test
    @DisplayName("Engine is saved when the OAuth2 fields are filled correctly")
    void givenOAuth2Engine_whenSaveWithValidFields_thenEngineSaved() {
        BpmEngineDetailView view = openNewOAuth2Engine("valid");
        this.<TypedTextField<String>>authField(view, "oauth2IssuerUriField")
                .setTypedValue("https://keycloak.example.com/realms/control");
        this.<TypedTextField<String>>authField(view, "oauth2ClientIdField").setTypedValue("control");
        this.<JmixPasswordField>authField(view, "oauth2ClientSecretField").setValue("secret");

        clickSave(view);

        assertThat((Object) UiTestUtils.getCurrentView()).isNotSameAs(view);
        assertThat(loadEngines())
                .singleElement()
                .satisfies(engine -> {
                    assertThat(engine.getAuthType()).isEqualTo(AuthType.OAUTH2);
                    assertThat(engine.getOauth2IssuerUri()).isEqualTo("https://keycloak.example.com/realms/control");
                    assertThat(engine.getOauth2ClientId()).isEqualTo("control");
                });
    }

    protected BpmEngineDetailView openNewOAuth2Engine(String nameSuffix) {
        viewNavigators.detailView(getCurrentView(), BpmEngine.class)
                .newEntity()
                .navigate();

        BpmEngineDetailView view = UiTestUtils.getCurrentView();
        this.<TypedTextField<String>>component(view, "nameField").setTypedValue(ENGINE_NAME_PREFIX + nameSuffix);
        this.<TypedTextField<String>>component(view, "baseUrlField")
                .setTypedValue("http://localhost:8080/engine-rest-" + nameSuffix);
        this.<JmixCheckbox>component(view, "authEnabledField").setValue(true);
        this.<JmixRadioButtonGroup<AuthType>>component(view, "authTypeGroup").setValue(AuthType.OAUTH2);
        return view;
    }

    protected void clickSave(BpmEngineDetailView view) {
        this.<JmixButton>component(view, "saveAndCloseButton").click();
    }

    protected List<BpmEngine> loadEngines() {
        return dataManager.load(BpmEngine.class)
                .query("e.name like :prefix").parameter("prefix", ENGINE_NAME_PREFIX + "%")
                .list();
    }

    protected <T extends Component> T component(BpmEngineDetailView view, String id) {
        return UiTestUtils.getComponent(view, id);
    }

    /**
     * Finds a field inside the auth fragment placed into {@code authBox}.
     */
    @SuppressWarnings("unchecked")
    protected <T extends Component> T authField(BpmEngineDetailView view, String id) {
        Component authBox = UiTestUtils.getComponent(view, "authBox");
        Component fragment = authBox.getChildren().findFirst()
                .orElseThrow(() -> new AssertionError("No auth fragment in authBox"));
        return (T) UiComponentUtils.findComponent(fragment, id)
                .orElseThrow(() -> new AssertionError("No '" + id + "' in the auth fragment"));
    }
}
