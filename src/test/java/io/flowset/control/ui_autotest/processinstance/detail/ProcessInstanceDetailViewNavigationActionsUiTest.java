/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.detail;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessDefinitionDetailView;
import io.flowset.control.test_support.ui.view.processinstance.detail.ProcessInstanceDetailView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JConditions.value;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Navigation actions in Process instance detail view")
public class ProcessInstanceDetailViewNavigationActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Navigation button in the Process field opens Process detail view")
    void givenExistingProcessInstance_whenOpenProcessDefinitionLinkClicked_thenProcessDefinitionDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView instanceDetailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        instanceDetailView.getGeneralPanel().getInfoBtn().click();

        instanceDetailView.getGeneralPanel()
                .getPropertiesPanel()
                .getOpenProcessDefinitionEditorBtn()
                .click();

        // then
        ProcessDefinitionDetailView definitionDetailView = $j(ProcessDefinitionDetailView.class)
                .exists()
                .displayed();

        definitionDetailView.getGeneralPanel()
                .getPropertiesPanel()
                .getKeyField()
                .shouldHave(value("vacation_approval"));
    }

    @Test
    @DisplayName("Navigation button in the Root process field opens Process instance detail view")
    void givenExistingChildProcessInstance_whenOpenRootProcessInstanceLinkClicked_thenRootProcessInstanceDetailViewOpened() {
        // given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/terminateinstance/testSkipSubprocess.bpmn")
                .deploy("test_support/terminateinstance/testSkipSubprocessMain.bpmn")
                .startByKey("testSkipSubprocessMain");

        String rootInstanceId = camundaSampleDataManager.getStartedInstances("testSkipSubprocessMain").get(0);
        String childInstanceId = camundaRestTestHelper.getActiveRuntimeInstancesByKey(camunda7, "testSkipSubprocess")
                .get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView childDetailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(childInstanceId);

        childDetailView.getGeneralPanel().getInfoBtn().click();

        childDetailView.getGeneralPanel()
                .getPropertiesPanel()
                .getOpenRootProcessInstanceEditorBtn()
                .click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/process-instances/" + rootInstanceId));

        $j(ProcessInstanceDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Navigation button in the Root process field is hidden when root instance is current instance")
    void givenExistingTopLevelProcessInstance_whenOpenDetailView_thenOpenRootProcessInstanceActionHidden() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        detailView.getGeneralPanel().getInfoBtn().click();

        // then
        detailView.getGeneralPanel()
                .getPropertiesPanel()
                .getOpenRootProcessInstanceEditorBtn()
                .shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Navigation button in the Parent process field opens Process instance detail view")
    void givenExistingChildProcessInstance_whenOpenParentProcessInstanceLinkClicked_thenParentProcessInstanceDetailViewOpened() {
        // given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/terminateinstance/testSkipSubprocess.bpmn")
                .deploy("test_support/terminateinstance/testSkipSubprocessMain.bpmn")
                .startByKey("testSkipSubprocessMain");

        String rootInstanceId = camundaSampleDataManager.getStartedInstances("testSkipSubprocessMain").get(0);
        String childInstanceId = camundaRestTestHelper.getActiveRuntimeInstancesByKey(camunda7, "testSkipSubprocess")
                .get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView childDetailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(childInstanceId);

        childDetailView.getGeneralPanel().getInfoBtn().click();

        childDetailView.getGeneralPanel()
                .getPropertiesPanel()
                .getOpenSuperProcessInstanceEditorBtn()
                .click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/process-instances/" + rootInstanceId));

        $j(ProcessInstanceDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Navigation button in the Parent process field is hidden when parent instance is current instance")
    void givenExistingTopLevelProcessInstance_whenOpenDetailView_thenOpenParentProcessInstanceActionHidden() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        detailView.getGeneralPanel().getInfoBtn().click();

        // then
        detailView.getGeneralPanel()
                .getPropertiesPanel()
                .getOpenSuperProcessInstanceEditorBtn()
                .shouldNotBe(VISIBLE);
    }
}
