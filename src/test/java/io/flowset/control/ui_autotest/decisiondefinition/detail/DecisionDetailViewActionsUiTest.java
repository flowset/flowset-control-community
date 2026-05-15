/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.decisiondefinition.detail;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.decisiondefinition.detail.DecisionDefinitionDetailView;
import io.flowset.control.test_support.ui.view.deployment.DeploymentDetailView;
import io.jmix.masquerade.component.Notification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.value;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions on Decision definition detail view")
public class DecisionDetailViewActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Info toggle action updates visibility of the decision information panel")
    void givenExistingDecisionDefinition_whenOpenDetailViewAndClickInfoToggle_thenPropertiesPanelToggled() {
        // given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/dmn/testDmn.dmn");
        MainView mainView = loginAsAdmin();

        // when
        DecisionDefinitionDetailView detailView = mainView.openDecisionDefinitionListView()
                .openDetailViewByKey("decision_testDmn");

        // then
        detailView.getGeneralPanel()
                .getPropertiesPanel()
                .shouldNotBe(VISIBLE);

        detailView.getGeneralPanel()
                .getInfoBtn().click();

        detailView.getGeneralPanel()
                .getPropertiesPanel()
                .shouldBe(VISIBLE);

        detailView.getGeneralPanel()
                .getInfoBtn().click();

        detailView.getGeneralPanel()
                .getPropertiesPanel()
                .shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("View deployment action opens Deployment detail view")
    void givenExistingDecisionDefinition_whenOpenDetailViewAndViewDeployment_thenDeploymentDetailViewOpened() {
        // given
        String deploymentId = camundaRestTestHelper.createDeployment(camunda7, "test_support/dmn/testDmn.dmn").getId();
        MainView mainView = loginAsAdmin();

        // when
        DecisionDefinitionDetailView detailView = mainView.openDecisionDefinitionListView()
                .openDetailViewByKey("decision_testDmn");

        detailView.getGeneralPanel()
                .getInfoBtn().click();

        detailView.getGeneralPanel()
                .getViewDeploymentBtn().click();

        // then
        DeploymentDetailView deploymentView = $j(DeploymentDetailView.class)
                .exists()
                .displayed();

        deploymentView.getDeploymentIdField()
                .shouldHave(value(deploymentId));
    }

    @Test
    @DisplayName("Copy key action shows notification")
    void givenOpenedDecisionDetailView_whenCopyKeyActionClicked_thenNotificationShown() {
        // given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/dmn/testDmn.dmn");

        MainView mainView = loginAsAdmin();

        // when
        DecisionDefinitionDetailView detailView = mainView.openDecisionDefinitionListView()
                .openDetailViewByKey("decision_testDmn");

        detailView.getGeneralPanel()
                .getInfoBtn()
                .shouldBe(VISIBLE).click();

        detailView.getGeneralPanel()
                .getCopyKeyButton().click();

        // then
        $j(Notification.class)
                .shouldBe(VISIBLE)
                .shouldHave(notificationTheme(Notification.Theme.SUCCESS))
                .shouldHave(text("Value copied!"));
    }

    @Test
    @DisplayName("Copy id action shows notification")
    void givenOpenedDecisionDetailView_whenCopyIdActionClicked_thenNotificationShown() {
        // given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/dmn/testDmn.dmn");
        MainView mainView = loginAsAdmin();

        // when
        DecisionDefinitionDetailView detailView = mainView.openDecisionDefinitionListView()
                .openDetailViewByKey("decision_testDmn");

        detailView.getGeneralPanel()
                .getInfoBtn()
                .shouldBe(VISIBLE).click();

        detailView.getGeneralPanel()
                .getCopyIdButton().click();

        // then
        $j(Notification.class)
                .shouldBe(VISIBLE)
                .shouldHave(notificationTheme(Notification.Theme.SUCCESS))
                .shouldHave(text("Value copied!"));
    }

    @Test
    @DisplayName("Close button closes the decision definition detail view")
    void givenExistingDecisionDefinition_whenClickClose_thenDecisionDefinitionDetailViewClosed() {
        // given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/dmn/testDmn.dmn");
        MainView mainView = loginAsAdmin();

        // when
        DecisionDefinitionDetailView detailView = mainView.openDecisionDefinitionListView()
                .openDetailViewByKey("decision_testDmn");

        detailView.getCloseButton().click();

        // then
        detailView.shouldNotBe(EXIST);
    }
}
