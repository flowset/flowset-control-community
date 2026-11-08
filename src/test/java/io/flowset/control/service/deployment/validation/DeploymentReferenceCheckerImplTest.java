/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.deployment.validation;

import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.entity.deployment.DeploymentResourceType;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.property.ResourceValidationProperties;
import io.flowset.control.service.decisiondefinition.DecisionDefinitionService;
import io.flowset.control.service.engine.EngineService;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import io.jmix.core.Messages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test of {@link DeploymentReferenceCheckerImpl}: no Spring context, engine services are mocked,
 * {@link Messages} returns message keys.
 */
class DeploymentReferenceCheckerImplTest {

    private ProcessDefinitionService processDefinitionService;
    private DecisionDefinitionService decisionDefinitionService;
    private EngineService engineService;
    private DeploymentReferenceCheckerImpl checker;

    @BeforeEach
    void setUp() {
        processDefinitionService = mock(ProcessDefinitionService.class);
        decisionDefinitionService = mock(DecisionDefinitionService.class);
        engineService = mock(EngineService.class);
        when(engineService.getSelectedEngine()).thenReturn(mock(BpmEngine.class));
        // formatMessage(group, key, params) returns the key
        Messages messages = Mockito.mock(Messages.class, invocation ->
                invocation.getMethod().getReturnType() == String.class && invocation.getArguments().length >= 2
                        ? invocation.getArgument(1)
                        : "label");
        checker = new DeploymentReferenceCheckerImpl(processDefinitionService, decisionDefinitionService,
                engineService, messages);
    }

    @Test
    void callActivityWithDeploymentBindingMissing() {
        DeploymentResourceDescriptor bpmn = bpmn("order.bpmn", List.of("order"),
                reference(ReferenceKind.PROCESS, "shipping", "deployment"));

        DeploymentReferenceCheckResult result = checker.check(List.of(bpmn));

        assertThat(result.getReferences()).singleElement().satisfies(resolved -> {
            assertThat(resolved.location()).isEqualTo(ReferenceLocation.MISSING);
            assertThat(resolved.severity()).isEqualTo(DeploymentResourceStatus.ERROR);
        });
        assertThat(result.getErrors("order.bpmn")).containsExactly("referenceMissingInDeployment");
        verify(processDefinitionService, never()).findAll(any());
    }

    @Test
    void callActivityWithLatestBindingFoundInEngine() {
        ProcessDefinitionData definition = new ProcessDefinitionData();
        definition.setKey("shipping");
        when(processDefinitionService.findAll(any())).thenReturn(List.of(definition));
        DeploymentResourceDescriptor bpmn = bpmn("order.bpmn", List.of("order"),
                reference(ReferenceKind.PROCESS, "shipping", "latest"),
                reference(ReferenceKind.PROCESS, "billing", "latest"));

        DeploymentReferenceCheckResult result = checker.check(List.of(bpmn));

        assertThat(result.getReferences()).hasSize(2);
        assertThat(result.getReferences().get(0).location()).isEqualTo(ReferenceLocation.ENGINE);
        assertThat(result.getReferences().get(0).severity()).isEqualTo(DeploymentResourceStatus.OK);
        assertThat(result.getReferences().get(1).location()).isEqualTo(ReferenceLocation.MISSING);
        assertThat(result.getReferences().get(1).severity()).isEqualTo(DeploymentResourceStatus.WARNING);
        assertThat(result.getErrors("order.bpmn")).isEmpty();
        assertThat(result.getWarnings("order.bpmn")).containsExactly("referenceMissing");
    }

    @Test
    void decisionInThisDeployment() {
        DeploymentResourceDescriptor bpmn = bpmn("order.bpmn", List.of("order"),
                reference(ReferenceKind.DECISION, "discount", "deployment"));
        DeploymentResourceDescriptor dmn = new DeploymentResourceDescriptor("discount.dmn", DeploymentResourceType.DMN,
                List.of("discount"), List.of());

        DeploymentReferenceCheckResult result = checker.check(List.of(bpmn, dmn));

        assertThat(result.getReferences()).singleElement().satisfies(resolved -> {
            assertThat(resolved.location()).isEqualTo(ReferenceLocation.THIS_DEPLOYMENT);
            assertThat(resolved.severity()).isEqualTo(DeploymentResourceStatus.OK);
        });
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getWarnings()).isEmpty();
        verify(decisionDefinitionService, never()).findAllByKey(any());
    }

    @Test
    void resourceFoundByLastPathSegmentHasWarning() {
        ResourceReference formReference = ResourceReference.builder()
                .kind(ReferenceKind.HTML_FORM)
                .key("forms/start.html")
                .binding("deployment")
                .deploymentBinding(true)
                .byResourceName(true)
                .build();
        DeploymentResourceDescriptor bpmn = bpmn("order.bpmn", List.of("order"), formReference);
        DeploymentResourceDescriptor html = new DeploymentResourceDescriptor("start.html",
                DeploymentResourceType.HTML_FORM, List.of(), List.of());

        DeploymentReferenceCheckResult result = checker.check(List.of(bpmn, html));

        assertThat(result.getReferences()).singleElement().satisfies(resolved -> {
            assertThat(resolved.location()).isEqualTo(ReferenceLocation.THIS_DEPLOYMENT);
            assertThat(resolved.severity()).isEqualTo(DeploymentResourceStatus.WARNING);
        });
        assertThat(result.getWarnings("order.bpmn")).containsExactly("referencePathDiffers");
    }

    @Test
    void duplicateProcessKeyInTwoFiles() {
        DeploymentResourceDescriptor first = bpmn("first.bpmn", List.of("order", "invoice"));
        DeploymentResourceDescriptor second = bpmn("second.bpmn", List.of("order"));

        DeploymentReferenceCheckResult result = checker.check(List.of(first, second));

        assertThat(result.getErrors("first.bpmn")).containsExactly("duplicateKey");
        assertThat(result.getErrors("second.bpmn")).containsExactly("duplicateKey");
    }

    @Test
    void engineFailureMakesReferencesNotChecked() {
        when(processDefinitionService.findAll(any()))
                .thenThrow(new EngineConnectionFailedException("Connection refused", -1, "Connection refused"));
        DeploymentResourceDescriptor bpmn = bpmn("order.bpmn", List.of("order"),
                reference(ReferenceKind.PROCESS, "shipping", "latest"),
                reference(ReferenceKind.DECISION, "discount", "latest"));

        DeploymentReferenceCheckResult result = checker.check(List.of(bpmn));

        assertThat(result.getReferences()).hasSize(2).allSatisfy(resolved -> {
            assertThat(resolved.location()).isEqualTo(ReferenceLocation.NOT_CHECKED);
            assertThat(resolved.severity()).isEqualTo(DeploymentResourceStatus.WARNING);
        });
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getWarnings("order.bpmn"))
                .containsExactly("referenceEngineUnavailable", "referenceEngineUnavailable");
    }

    @Test
    void engineFailureDoesNotAffectDuplicateKeys() {
        when(processDefinitionService.findAll(any()))
                .thenThrow(new EngineConnectionFailedException("Connection refused", -1, "Connection refused"));
        DeploymentResourceDescriptor first = bpmn("first.bpmn", List.of("order"),
                reference(ReferenceKind.PROCESS, "shipping", "latest"));
        DeploymentResourceDescriptor second = bpmn("second.bpmn", List.of("order"));

        DeploymentReferenceCheckResult result = checker.check(List.of(first, second));

        assertThat(result.getErrors("first.bpmn")).containsExactly("duplicateKey");
        assertThat(result.getErrors("second.bpmn")).containsExactly("duplicateKey");
        assertThat(result.getWarnings("first.bpmn")).containsExactly("referenceEngineUnavailable");
    }

    @Test
    void engineNotSelectedMakesReferencesNotChecked() {
        when(engineService.getSelectedEngine()).thenReturn(null);
        DeploymentResourceDescriptor bpmn = bpmn("order.bpmn", List.of("order"),
                reference(ReferenceKind.PROCESS, "shipping", "latest"));

        DeploymentReferenceCheckResult result = checker.check(List.of(bpmn));

        assertThat(result.getReferences()).singleElement().satisfies(resolved -> {
            assertThat(resolved.location()).isEqualTo(ReferenceLocation.NOT_CHECKED);
            assertThat(resolved.severity()).isEqualTo(DeploymentResourceStatus.WARNING);
        });
        assertThat(result.getWarnings("order.bpmn")).containsExactly("referenceEngineUnavailable");
        verify(processDefinitionService, never()).findAll(any());
    }

    @Test
    void unexpectedExceptionIsPropagated() {
        DeploymentResourceDescriptor bpmn = bpmn("order.bpmn", List.of("order"),
                reference(ReferenceKind.PROCESS, "shipping", "deployment"));
        DeploymentReferenceCheckerImpl failingChecker = new DeploymentReferenceCheckerImpl(processDefinitionService,
                decisionDefinitionService, engineService, mock(Messages.class, invocation -> {
            throw new IllegalStateException("Messages failure");
        }));

        assertThatThrownBy(() -> failingChecker.check(List.of(bpmn)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Messages failure");
    }

    @Test
    void formByKeyIsNotCheckedInEngineWithoutWarning() {
        DeploymentResourceDescriptor bpmn = bpmn("order.bpmn", List.of("order"),
                reference(ReferenceKind.CAMUNDA_FORM, "approveForm", "latest"));

        DeploymentReferenceCheckResult result = checker.check(List.of(bpmn));

        assertThat(result.getReferences()).singleElement().satisfies(resolved -> {
            assertThat(resolved.location()).isEqualTo(ReferenceLocation.NOT_CHECKED);
            assertThat(resolved.severity()).isEqualTo(DeploymentResourceStatus.OK);
            assertThat(resolved.message()).isEqualTo("referenceNotCheckedInEngine");
        });
        assertThat(result.getWarnings()).isEmpty();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void dynamicReferenceHasWarning() {
        DeploymentResourceDescriptor bpmn = bpmn("order.bpmn", List.of("order"),
                reference(ReferenceKind.PROCESS, "${processKey}", "latest"));

        DeploymentReferenceCheckResult result = checker.check(List.of(bpmn));

        assertThat(result.getReferences()).singleElement().satisfies(resolved -> {
            assertThat(resolved.location()).isEqualTo(ReferenceLocation.NOT_CHECKED);
            assertThat(resolved.severity()).isEqualTo(DeploymentResourceStatus.WARNING);
        });
        assertThat(result.getWarnings("order.bpmn")).containsExactly("referenceDynamic");
        verify(processDefinitionService, never()).findAll(any());
    }

    @Test
    void pendingCheckDoesNotQueryEngine() {
        DeploymentResourceDescriptor first = bpmn("first.bpmn", List.of("order"),
                reference(ReferenceKind.PROCESS, "shipping", "latest"),
                reference(ReferenceKind.DECISION, "discount", "latest"),
                reference(ReferenceKind.PROCESS, "billing", "deployment"),
                reference(ReferenceKind.PROCESS, "invoice", "latest"));
        DeploymentResourceDescriptor second = bpmn("second.bpmn", List.of("order", "invoice"));

        DeploymentReferenceCheckResult result = checker.checkPending(List.of(first, second));

        assertThat(result.isPending()).isTrue();
        assertThat(result.getReferences())
                .extracting(ResolvedReference::location, ResolvedReference::severity)
                .containsExactly(
                        tuple(ReferenceLocation.CHECKING, DeploymentResourceStatus.OK),
                        tuple(ReferenceLocation.CHECKING, DeploymentResourceStatus.OK),
                        tuple(ReferenceLocation.MISSING, DeploymentResourceStatus.ERROR),
                        tuple(ReferenceLocation.THIS_DEPLOYMENT, DeploymentResourceStatus.OK));
        assertThat(result.getErrors("first.bpmn")).containsExactly("duplicateKey", "referenceMissingInDeployment");
        assertThat(result.getErrors("second.bpmn")).containsExactly("duplicateKey");
        assertThat(result.getWarnings()).isEmpty();
        verify(processDefinitionService, never()).findAll(any());
        verify(decisionDefinitionService, never()).findAllByKey(any());
        verify(engineService, never()).getSelectedEngine();
    }

    @Test
    void pendingCheckWithoutEngineReferencesIsNotPending() {
        DeploymentResourceDescriptor bpmn = bpmn("order.bpmn", List.of("order"),
                reference(ReferenceKind.PROCESS, "order", "latest"));

        DeploymentReferenceCheckResult result = checker.checkPending(List.of(bpmn));

        assertThat(result.isPending()).isFalse();
        assertThat(result.getReferences()).singleElement()
                .satisfies(resolved -> assertThat(resolved.location()).isEqualTo(ReferenceLocation.THIS_DEPLOYMENT));
    }

    @Test
    void engineUnavailableCheckDoesNotQueryEngine() {
        DeploymentResourceDescriptor bpmn = bpmn("order.bpmn", List.of("order"),
                reference(ReferenceKind.PROCESS, "shipping", "latest"),
                reference(ReferenceKind.DECISION, "discount", "latest"));

        DeploymentReferenceCheckResult result = checker.checkEngineUnavailable(List.of(bpmn));

        assertThat(result.isPending()).isFalse();
        assertThat(result.getReferences()).hasSize(2).allSatisfy(resolved -> {
            assertThat(resolved.location()).isEqualTo(ReferenceLocation.NOT_CHECKED);
            assertThat(resolved.severity()).isEqualTo(DeploymentResourceStatus.WARNING);
        });
        assertThat(result.getWarnings("order.bpmn"))
                .containsExactly("referenceEngineUnavailable", "referenceEngineUnavailable");
        verify(processDefinitionService, never()).findAll(any());
        verify(engineService, never()).getSelectedEngine();
    }

    @Test
    void decisionFoundInEngine() {
        when(decisionDefinitionService.findAllByKey("discount")).thenReturn(List.of(new DecisionDefinitionData()));
        DeploymentResourceDescriptor bpmn = bpmn("order.bpmn", List.of("order"),
                reference(ReferenceKind.DECISION, "discount", "latest"));

        DeploymentReferenceCheckResult result = checker.check(List.of(bpmn));

        assertThat(result.getReferences()).singleElement()
                .satisfies(resolved -> assertThat(resolved.location()).isEqualTo(ReferenceLocation.ENGINE));
    }

    /**
     * A form referenced with deployment binding but not uploaded blocks deploy. After the form is added,
     * the error disappears. The scenario goes through the content validator (reference extraction) and the checker.
     */
    @Test
    void formWithDeploymentBindingMissingThenAdded() {
        DeploymentResourceValidatorImpl validator = new DeploymentResourceValidatorImpl(
                checkerMessages(), new ResourceValidationProperties(64, 100000));
        String bpmn = """
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                             xmlns:camunda="http://camunda.org/schema/1.0/bpmn">
                  <process id="order" isExecutable="true">
                    <userTask id="approve" camunda:formRef="approveForm" camunda:formRefBinding="deployment"/>
                  </process>
                </definitions>
                """;
        DeploymentResourceDescriptor bpmnDescriptor = DeploymentResourceDescriptor.of("order.bpmn",
                validator.validate("order.bpmn", bpmn.getBytes(StandardCharsets.UTF_8)));

        // the form is not uploaded: error, deploy is blocked
        DeploymentReferenceCheckResult withoutForm = checker.check(List.of(bpmnDescriptor));
        assertThat(withoutForm.getReferences()).singleElement().satisfies(resolved -> {
            assertThat(resolved.reference().getKind()).isEqualTo(ReferenceKind.CAMUNDA_FORM);
            assertThat(resolved.location()).isEqualTo(ReferenceLocation.MISSING);
            assertThat(resolved.severity()).isEqualTo(DeploymentResourceStatus.ERROR);
        });
        assertThat(withoutForm.getErrors("order.bpmn")).containsExactly("referenceMissingInDeployment");

        // the form is added: the reference is found in this deployment, no errors
        String form = """
                {"id": "approveForm", "type": "default", "components": []}
                """;
        DeploymentResourceDescriptor formDescriptor = DeploymentResourceDescriptor.of("approveForm.form",
                validator.validate("approveForm.form", form.getBytes(StandardCharsets.UTF_8)));

        DeploymentReferenceCheckResult withForm = checker.check(List.of(bpmnDescriptor, formDescriptor));
        assertThat(withForm.getReferences()).singleElement().satisfies(resolved -> {
            assertThat(resolved.location()).isEqualTo(ReferenceLocation.THIS_DEPLOYMENT);
            assertThat(resolved.severity()).isEqualTo(DeploymentResourceStatus.OK);
        });
        assertThat(withForm.getErrors("order.bpmn")).isEmpty();
        assertThat(withForm.getErrors("approveForm.form")).isEmpty();
        verify(processDefinitionService, never()).findAll(any());
    }

    private static Messages checkerMessages() {
        return Mockito.mock(Messages.class, invocation ->
                invocation.getMethod().getReturnType() == String.class && invocation.getArguments().length >= 2
                        ? invocation.getArgument(1)
                        : "label");
    }

    private static DeploymentResourceDescriptor bpmn(String fileName, List<String> keys,
                                                     ResourceReference... references) {
        return new DeploymentResourceDescriptor(fileName, DeploymentResourceType.BPMN, keys, List.of(references));
    }

    private static ResourceReference reference(ReferenceKind kind, String key, String binding) {
        return ResourceReference.builder()
                .kind(kind)
                .key(key)
                .binding(binding)
                .deploymentBinding("deployment".equals(binding))
                .dynamic(ResourceReference.isExpression(key))
                .sourceElementId("element_" + key)
                .build();
    }
}
