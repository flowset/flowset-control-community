/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.deployment.validation;

import io.flowset.control.entity.deployment.DeploymentResourceType;
import io.flowset.control.property.ResourceValidationProperties;
import io.jmix.core.Messages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Unit test of {@link DeploymentResourceValidatorImpl}: no Spring context, {@link Messages} returns message keys.
 */
class DeploymentResourceValidatorImplTest {

    private static final byte[] PNG_SIGNATURE = {
            (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n',
            0, 0, 0, 0x0D, 'I', 'H', 'D', 'R', 0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0
    };

    private DeploymentResourceValidatorImpl validator;

    @BeforeEach
    void setUp() {
        // getMessage(group, key) and formatMessage(group, key, params) return the key
        Messages messages = Mockito.mock(Messages.class, invocation ->
                invocation.getMethod().getReturnType() == String.class && invocation.getArguments().length >= 2
                        ? invocation.getArgument(1)
                        : null);
        validator = new DeploymentResourceValidatorImpl(messages, new ResourceValidationProperties(64, 100000));
    }

    @Test
    void validBpmnWithTwoExecutableProcesses() {
        String bpmn = """
                <?xml version="1.0" encoding="UTF-8"?>
                <bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" id="defs">
                  <bpmn:process id="orderProcess" isExecutable="true"/>
                  <bpmn:process id="invoiceProcess"/>
                  <bpmn:process id="draftProcess" isExecutable="false"/>
                </bpmn:definitions>
                """;

        DeploymentResourceValidationResult result = validator.validate("order.bpmn", utf8(bpmn));

        assertThat(result.getType()).isEqualTo(DeploymentResourceType.BPMN);
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getWarnings()).isEmpty();
        assertThat(result.getKeys()).containsExactly("orderProcess", "invoiceProcess");
        assertThat(result.getStatus()).isEqualTo(DeploymentResourceStatus.OK);
    }

    @Test
    void nonBpmnXmlInBpmnFile() {
        String xml = "<?xml version=\"1.0\"?><project><name>not a process</name></project>";

        DeploymentResourceValidationResult result = validator.validate("process.bpmn", utf8(xml));

        assertThat(result.getErrors()).containsExactly("notBpmn");
        assertThat(result.getStatus()).isEqualTo(DeploymentResourceStatus.ERROR);
    }

    @Test
    void bpmnWithExternalEntityIsRejected() {
        String bpmn = """
                <?xml version="1.0"?>
                <!DOCTYPE definitions [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL">
                  <process id="p1" name="&xxe;"/>
                </definitions>
                """;

        DeploymentResourceValidationResult result = validator.validate("xxe.bpmn", utf8(bpmn));

        assertThat(result.getErrors()).containsExactly("invalidXml");
        assertThat(result.getStatus()).isEqualTo(DeploymentResourceStatus.ERROR);
    }

    @Test
    void dmnWithoutDecisionsHasWarning() {
        String dmn = "<definitions xmlns=\"https://www.omg.org/spec/DMN/20191111/MODEL/\" id=\"d\"/>";

        DeploymentResourceValidationResult result = validator.validate("rules.dmn", utf8(dmn));

        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getWarnings()).containsExactly("noDecisions");
        assertThat(result.getStatus()).isEqualTo(DeploymentResourceStatus.WARNING);
    }

    @Test
    void camundaFormKey() {
        String form = "﻿{\"id\": \"approveForm\", \"type\": \"default\", \"components\": []}";

        DeploymentResourceValidationResult result = validator.validate("approve.form", utf8(form));

        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getKeys()).containsExactly("approveForm");
        assertThat(result.getStatus()).isEqualTo(DeploymentResourceStatus.OK);
    }

    @Test
    void nulByteInScript() {
        byte[] content = {'v', 'a', 'r', ' ', 'x', 0, ';'};

        DeploymentResourceValidationResult result = validator.validate("script.js", content);

        assertThat(result.getErrors()).containsExactly("binaryContent");
        assertThat(result.getStatus()).isEqualTo(DeploymentResourceStatus.ERROR);
    }

    @Test
    void pngWithPngContent() {
        DeploymentResourceValidationResult result = validator.validate("diagram.png", PNG_SIGNATURE);

        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getStatus()).isEqualTo(DeploymentResourceStatus.OK);
    }

    @Test
    void pngWithTextContent() {
        DeploymentResourceValidationResult result = validator.validate("diagram.png", utf8("just some text"));

        assertThat(result.getErrors()).containsExactly("notImage");
        assertThat(result.getStatus()).isEqualTo(DeploymentResourceStatus.ERROR);
    }

    @Test
    void pngContentInJpgFile() {
        DeploymentResourceValidationResult result = validator.validate("photo.jpg", PNG_SIGNATURE);

        assertThat(result.getErrors()).containsExactly("imageTypeMismatch");
    }

    @Test
    void svgWithScriptIsRejected() {
        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"><script>alert(1)</script></svg>";

        DeploymentResourceValidationResult result = validator.validate("icon.svg", utf8(svg));

        assertThat(result.getErrors()).containsExactly("svgUnsafeContent");
        assertThat(result.getStatus()).isEqualTo(DeploymentResourceStatus.ERROR);
    }

    @Test
    void svgWithOnloadIsRejected() {
        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" onload=\"alert(1)\"><circle r=\"1\"/></svg>";

        DeploymentResourceValidationResult result = validator.validate("icon.svg", utf8(svg));

        assertThat(result.getErrors()).containsExactly("svgUnsafeContent");
        assertThat(result.getStatus()).isEqualTo(DeploymentResourceStatus.ERROR);
    }

    @Test
    void plainSvgIsAccepted() {
        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 10 10\"><circle r=\"1\"/></svg>";

        DeploymentResourceValidationResult result = validator.validate("icon.svg", utf8(svg));

        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getStatus()).isEqualTo(DeploymentResourceStatus.OK);
    }

    @Test
    void controlCharactersWithoutNulAreBinary() {
        byte[] content = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06};

        DeploymentResourceValidationResult result = validator.validate("script.groovy", content);

        assertThat(result.getErrors()).containsExactly("binaryContent");
        assertThat(result.getStatus()).isEqualTo(DeploymentResourceStatus.ERROR);
    }

    @Test
    void nonAsciiTextIsNotBinary() {
        DeploymentResourceValidationResult result = validator.validate("script.js",
                utf8("// Скрипт проверки заявки\nvar сумма = 1;\n"));

        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getStatus()).isEqualTo(DeploymentResourceStatus.OK);
    }

    @Test
    void xmlDepthLimitExceeded() {
        DeploymentResourceValidatorImpl limitedValidator = new DeploymentResourceValidatorImpl(
                messagesMock(), new ResourceValidationProperties(3, 100000));
        String bpmn = """
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL">
                  <process id="p1"><extensionElements><a/></extensionElements></process>
                </definitions>
                """;

        DeploymentResourceValidationResult result = limitedValidator.validate("deep.bpmn", utf8(bpmn));

        assertThat(result.getErrors()).containsExactly("xmlTooDeep");
    }

    @Test
    void xmlElementLimitExceeded() {
        DeploymentResourceValidatorImpl limitedValidator = new DeploymentResourceValidatorImpl(
                messagesMock(), new ResourceValidationProperties(64, 3));
        String bpmn = """
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL">
                  <process id="p1"/><process id="p2"/><process id="p3"/>
                </definitions>
                """;

        DeploymentResourceValidationResult result = limitedValidator.validate("many.bpmn", utf8(bpmn));

        assertThat(result.getErrors()).containsExactly("xmlTooManyElements");
    }

    @Test
    void htmlFormWithCamundaScriptIsNotFlagged() {
        String html = """
                <form role="form">
                  <input type="text" cam-variable-name="amount" cam-variable-type="Long"/>
                  <script cam-script type="text/form-script">
                    camForm.on('form-loaded', function() {});
                  </script>
                </form>
                """;

        DeploymentResourceValidationResult result = validator.validate("approve.html", utf8(html));

        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getWarnings()).isEmpty();
        assertThat(result.getStatus()).isEqualTo(DeploymentResourceStatus.OK);
    }

    @Test
    void htmlFormWithOnclickHasWarning() {
        String html = "<form><button onclick=\"steal()\">OK</button><a href=\" JavaScript:alert(1)\">x</a></form>";

        DeploymentResourceValidationResult result = validator.validate("approve.html", utf8(html));

        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getWarnings()).containsExactly("htmlUnsafeContent");
        assertThat(result.getStatus()).isEqualTo(DeploymentResourceStatus.WARNING);
    }

    @Test
    void bpmnReferencesAreExtracted() {
        String bpmn = """
                <?xml version="1.0" encoding="UTF-8"?>
                <bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                                  xmlns:camunda="http://camunda.org/schema/1.0/bpmn" id="defs">
                  <bpmn:process id="orderProcess" isExecutable="true">
                    <bpmn:startEvent id="start" camunda:formKey="embedded:deployment:forms/start.html"/>
                    <bpmn:callActivity id="callShipping" calledElement="shippingProcess"
                                       camunda:calledElementBinding="deployment"/>
                    <bpmn:callActivity id="callBilling" calledElement="billingProcess"/>
                    <bpmn:callActivity id="callCase" camunda:caseRef="someCase"/>
                    <bpmn:businessRuleTask id="rules" camunda:decisionRef="${decisionKey}"
                                           camunda:decisionRefBinding="version"/>
                    <bpmn:userTask id="approve" camunda:formRef="approveForm" camunda:formRefBinding="deployment">
                      <bpmn:extensionElements>
                        <camunda:taskListener event="create">
                          <camunda:script scriptFormat="groovy" resource="deployment://listener.groovy"/>
                        </camunda:taskListener>
                      </bpmn:extensionElements>
                    </bpmn:userTask>
                    <bpmn:userTask id="review" camunda:formKey="camunda-forms:deployment:review.form"/>
                    <bpmn:userTask id="external" camunda:formKey="embedded:app:forms/external.html"/>
                    <bpmn:scriptTask id="calc" scriptFormat="javascript" camunda:resource="deployment://calc.js"/>
                    <bpmn:scriptTask id="classpathScript" scriptFormat="groovy" camunda:resource="classpath://x.groovy"/>
                  </bpmn:process>
                </bpmn:definitions>
                """;

        DeploymentResourceValidationResult result = validator.validate("order.bpmn", utf8(bpmn));

        assertThat(result.getErrors()).isEmpty();
        List<ResourceReference> references = result.getReferences();
        assertThat(references)
                .extracting(ResourceReference::getKind, ResourceReference::getKey, ResourceReference::getBinding,
                        ResourceReference::isDeploymentBinding, ResourceReference::isDynamic,
                        ResourceReference::isByResourceName, ResourceReference::getSourceElementId)
                .containsExactly(
                        tuple(ReferenceKind.HTML_FORM, "forms/start.html", "deployment", true, false, true, "start"),
                        tuple(ReferenceKind.PROCESS, "shippingProcess", "deployment", true, false, false,
                                "callShipping"),
                        tuple(ReferenceKind.PROCESS, "billingProcess", "latest", false, false, false, "callBilling"),
                        tuple(ReferenceKind.DECISION, "${decisionKey}", "version", false, true, false, "rules"),
                        tuple(ReferenceKind.CAMUNDA_FORM, "approveForm", "deployment", true, false, false, "approve"),
                        tuple(ReferenceKind.SCRIPT, "listener.groovy", "deployment", true, false, true, "approve"),
                        tuple(ReferenceKind.CAMUNDA_FORM, "review.form", "deployment", true, false, true, "review"),
                        tuple(ReferenceKind.SCRIPT, "calc.js", "deployment", true, false, true, "calc"));
    }

    @Test
    void operatonBpmnReferencesAreExtracted() {
        String bpmn = """
                <?xml version="1.0" encoding="UTF-8"?>
                <bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                                  xmlns:operaton="http://operaton.org/schema/1.0/bpmn" id="defs">
                  <bpmn:process id="orderProcess" isExecutable="true">
                    <bpmn:startEvent id="start" operaton:formKey="embedded:deployment:forms/a.html"/>
                    <bpmn:businessRuleTask id="rules" operaton:decisionRef="discount"
                                           operaton:decisionRefBinding="latest"/>
                    <bpmn:callActivity id="callShipping" calledElement="shippingProcess"
                                       operaton:calledElementBinding="deployment"/>
                    <bpmn:userTask id="approve" operaton:formRef="approveForm">
                      <bpmn:extensionElements>
                        <operaton:taskListener event="create">
                          <operaton:script scriptFormat="groovy" resource="deployment://listener.groovy"/>
                        </operaton:taskListener>
                      </bpmn:extensionElements>
                    </bpmn:userTask>
                  </bpmn:process>
                </bpmn:definitions>
                """;

        DeploymentResourceValidationResult result = validator.validate("order.bpmn", utf8(bpmn));

        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getKeys()).containsExactly("orderProcess");
        assertThat(result.getReferences())
                .extracting(ResourceReference::getKind, ResourceReference::getKey, ResourceReference::getBinding,
                        ResourceReference::isDeploymentBinding, ResourceReference::isByResourceName,
                        ResourceReference::getSourceElementId)
                .containsExactly(
                        tuple(ReferenceKind.HTML_FORM, "forms/a.html", "deployment", true, true, "start"),
                        tuple(ReferenceKind.DECISION, "discount", "latest", false, false, "rules"),
                        tuple(ReferenceKind.PROCESS, "shippingProcess", "deployment", true, false, "callShipping"),
                        tuple(ReferenceKind.CAMUNDA_FORM, "approveForm", "latest", false, false, "approve"),
                        tuple(ReferenceKind.SCRIPT, "listener.groovy", "deployment", true, true, "approve"));
    }

    @Test
    void operatonDmnDecisionsAreExtracted() {
        String dmn = """
                <definitions xmlns="https://www.omg.org/spec/DMN/20191111/MODEL/"
                             xmlns:operaton="http://operaton.org/schema/1.0/dmn" id="defs">
                  <decision id="discount" operaton:historyTimeToLive="30"/>
                </definitions>
                """;

        DeploymentResourceValidationResult result = validator.validate("discount.dmn", utf8(dmn));

        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getKeys()).containsExactly("discount");
    }

    @Test
    void unsupportedAndEmptyFiles() {
        assertThat(validator.validate("notes.txt", utf8("text")).getErrors()).containsExactly("unsupportedType");
        assertThat(validator.validate("process.bpmn", new byte[0]).getErrors()).containsExactly("emptyFile");
    }

    private static Messages messagesMock() {
        return Mockito.mock(Messages.class, invocation ->
                invocation.getMethod().getReturnType() == String.class && invocation.getArguments().length >= 2
                        ? invocation.getArgument(1)
                        : null);
    }

    private static byte[] utf8(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }
}
