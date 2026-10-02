/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.deployment.validation;

import io.flowset.control.entity.deployment.DeploymentResourceType;
import io.flowset.control.property.ResourceValidationProperties;
import io.jmix.core.Messages;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.function.Predicate;

@Service("control_DeploymentResourceValidator")
@Slf4j
public class DeploymentResourceValidatorImpl implements DeploymentResourceValidator {

    protected static final String MESSAGE_GROUP = "io.flowset.control.service.deployment.validation";

    protected static final String BPMN_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/MODEL";
    /**
     * Namespaces of the BPMN extension attributes and elements of Camunda 7 and its forks: Camunda, the legacy
     * Activiti one and Operaton. All of them use the same attribute and element names.
     */
    protected static final List<String> EXTENSION_NAMESPACES = List.of(
            "http://camunda.org/schema/1.0/bpmn",
            "http://activiti.org/bpmn",
            "http://operaton.org/schema/1.0/bpmn");
    protected static final List<String> DMN_NAMESPACE_PREFIXES = List.of(
            "http://www.omg.org/spec/DMN/", "https://www.omg.org/spec/DMN/");

    protected static final char BOM = '﻿';

    protected static final String EMBEDDED_DEPLOYMENT_FORM_PREFIX = "embedded:deployment:";
    protected static final String CAMUNDA_FORMS_DEPLOYMENT_PREFIX = "camunda-forms:deployment:";
    protected static final String DEPLOYMENT_RESOURCE_PREFIX = "deployment://";

    /**
     * HTML elements that embed active content and are reported as unsafe in HTML forms.
     */
    protected static final List<String> UNSAFE_HTML_ELEMENTS = List.of("iframe", "object", "embed");
    /**
     * Attributes that contain URLs and are checked for the {@code javascript:} scheme.
     */
    protected static final Set<String> URL_ATTRIBUTES = Set.of("href", "src", "action", "formaction", "xlink:href");
    protected static final String JAVASCRIPT_URL = "javascript: URL";

    /**
     * Media types expected for the image file extensions.
     */
    protected static final Map<String, String> IMAGE_MEDIA_TYPES = Map.of(
            "png", "image/png",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "gif", "image/gif",
            "svg", "image/svg+xml"
    );

    protected static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
    protected static final byte[] JPEG_SIGNATURE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    protected static final byte[] GIF87_SIGNATURE = "GIF87a".getBytes(StandardCharsets.US_ASCII);
    protected static final byte[] GIF89_SIGNATURE = "GIF89a".getBytes(StandardCharsets.US_ASCII);

    /**
     * An SVG document: optional XML declaration, comments and DOCTYPE followed by the {@code svg} root element.
     */
    protected static final Pattern SVG_PROLOG_PATTERN = Pattern.compile(
            "^\\s*(<\\?xml[^>]*\\?>\\s*)?(<!--.*?-->\\s*)*(<!DOCTYPE[^>]*>\\s*)?<(?:[A-Za-z_][\\w.-]*:)?svg[\\s>/]",
            Pattern.DOTALL);

    /**
     * The number of leading bytes inspected for control characters to detect binary content.
     */
    protected static final int TEXT_DETECTION_WINDOW = 8192;

    protected final Messages messages;
    protected final ResourceValidationProperties properties;
    protected final JsonMapper jsonMapper = JsonMapper.builder().build();
    protected final XMLInputFactory xmlInputFactory = createXmlInputFactory();

    public DeploymentResourceValidatorImpl(Messages messages, ResourceValidationProperties properties) {
        this.messages = messages;
        this.properties = properties;
    }

    @Override
    public DeploymentResourceValidationResult validate(String fileName, byte[] content) {
        DeploymentResourceType type = DeploymentResourceType.fromFileName(fileName);
        DeploymentResourceValidationResult result = new DeploymentResourceValidationResult(type);
        try {
            if (type == null) {
                result.addError(message("unsupportedType"));
                return result;
            }
            if (content == null || content.length == 0) {
                result.addError(message("emptyFile"));
                return result;
            }

            switch (type) {
                case BPMN -> validateBpmn(content, result);
                case DMN -> validateDmn(content, result);
                case CAMUNDA_FORM -> validateCamundaForm(content, result);
                case HTML_FORM -> validateHtmlForm(content, result);
                case SCRIPT -> decodeText(content, result);
                case IMAGE -> validateImage(fileName, content, result);
            }
        } catch (Exception e) {
            log.warn("Unable to validate deployment resource '{}'", fileName, e);
            result.addError(formatMessage("validationFailed", Objects.toString(e.getMessage(),
                    e.getClass().getSimpleName())));
        }
        return result;
    }

    protected void validateBpmn(byte[] content, DeploymentResourceValidationResult result) {
        String text = decodeText(content, result);
        if (text == null) {
            return;
        }
        // ids of the BPMN elements on the current path: index = depth - 1, null for non-BPMN elements or without id
        List<String> elementIds = new ArrayList<>();
        boolean valid = scanXml(text, result,
                reader -> "definitions".equals(reader.getLocalName())
                        && BPMN_NAMESPACE.equals(reader.getNamespaceURI()),
                "notBpmn",
                (reader, depth) -> {
                    boolean bpmnElement = BPMN_NAMESPACE.equals(reader.getNamespaceURI());
                    elementIds.subList(Math.min(depth - 1, elementIds.size()), elementIds.size()).clear();
                    elementIds.add(bpmnElement ? StringUtils.trimToNull(reader.getAttributeValue(null, "id")) : null);

                    if (bpmnElement && "process".equals(reader.getLocalName())
                            && !"false".equalsIgnoreCase(StringUtils.trim(reader.getAttributeValue(null, "isExecutable")))) {
                        addKey(reader, result);
                    }
                    extractReferences(reader, getNearestElementId(elementIds), result);
                });
        if (valid && result.getKeys().isEmpty()) {
            result.addWarning(message("noExecutableProcess"));
        }
    }

    protected void validateDmn(byte[] content, DeploymentResourceValidationResult result) {
        String text = decodeText(content, result);
        if (text == null) {
            return;
        }
        boolean valid = scanXml(text, result,
                reader -> "definitions".equals(reader.getLocalName()) && isDmnNamespace(reader.getNamespaceURI()),
                "notDmn",
                (reader, depth) -> {
                    if ("decision".equals(reader.getLocalName()) && isDmnNamespace(reader.getNamespaceURI())) {
                        addKey(reader, result);
                    }
                });
        if (valid && result.getKeys().isEmpty()) {
            result.addWarning(message("noDecisions"));
        }
    }

    protected void validateCamundaForm(byte[] content, DeploymentResourceValidationResult result) {
        String text = decodeText(content, result);
        if (text == null) {
            return;
        }
        JsonNode root;
        try {
            root = jsonMapper.readTree(text);
        } catch (JacksonException e) {
            result.addError(formatMessage("invalidJson", e.getOriginalMessage()));
            return;
        }
        if (root == null || !root.isObject() || !root.path("components").isArray()) {
            result.addError(message("notCamundaForm"));
            return;
        }
        JsonNode id = root.get("id");
        if (id != null && id.isString() && StringUtils.isNotBlank(id.stringValue())) {
            result.addKey(id.stringValue());
        } else {
            result.addWarning(message("formIdMissing"));
        }
    }

    protected void validateHtmlForm(byte[] content, DeploymentResourceValidationResult result) {
        String text = decodeText(content, result);
        if (text == null) {
            return;
        }
        if (text.indexOf('<') < 0) {
            result.addWarning(message("htmlNoMarkup"));
            return;
        }
        Set<String> unsafeConstructs = findUnsafeHtmlConstructs(Jsoup.parse(text));
        if (!unsafeConstructs.isEmpty()) {
            // a warning only: Camunda embedded forms may legitimately contain active content
            result.addWarning(formatMessage("htmlUnsafeContent", String.join(", ", unsafeConstructs)));
        }
    }

    /**
     * Finds the constructs of an HTML form that can execute code: scripts (except the Camunda embedded form scripts
     * {@code <script cam-script type="text/form-script">}), event handler attributes, {@code javascript:} URLs and
     * the elements that embed active content.
     *
     * @return names of the found constructs, in the order of their first occurrence
     */
    protected Set<String> findUnsafeHtmlConstructs(Document document) {
        Set<String> result = new LinkedHashSet<>();
        for (Element element : document.getAllElements()) {
            String name = element.normalName();
            if ("script".equals(name) && !isCamundaFormScript(element)) {
                result.add("script");
            } else if (UNSAFE_HTML_ELEMENTS.contains(name)) {
                result.add(name);
            }
            collectUnsafeAttributes(element, result);
        }
        return result;
    }

    protected boolean isCamundaFormScript(Element script) {
        return script.hasAttr("cam-script") && "text/form-script".equalsIgnoreCase(script.attr("type").trim());
    }

    protected void collectUnsafeAttributes(Element element, Set<String> result) {
        for (Attribute attribute : element.attributes()) {
            String name = attribute.getKey().toLowerCase(Locale.ROOT);
            if (isEventHandlerAttribute(name)) {
                result.add(name);
            } else if (URL_ATTRIBUTES.contains(name) && isJavascriptUrl(attribute.getValue())) {
                result.add(JAVASCRIPT_URL);
            }
        }
    }

    protected boolean isEventHandlerAttribute(String lowerCaseName) {
        return lowerCaseName.length() > 2 && lowerCaseName.startsWith("on");
    }

    protected boolean isJavascriptUrl(@Nullable String value) {
        if (value == null) {
            return false;
        }
        // browsers ignore whitespace and control characters in the scheme
        StringBuilder normalized = new StringBuilder();
        for (int i = 0; i < value.length() && normalized.length() < 11; i++) {
            char c = value.charAt(i);
            if (c > ' ') {
                normalized.append(Character.toLowerCase(c));
            }
        }
        return normalized.toString().startsWith("javascript:");
    }

    protected void validateImage(String fileName, byte[] content, DeploymentResourceValidationResult result) {
        String detected = detectImageType(content);
        if (detected == null) {
            result.addError(message("notImage"));
            return;
        }

        String extension = getExtension(fileName);
        String expectedType = IMAGE_MEDIA_TYPES.get(extension);
        if (expectedType != null && !expectedType.equals(detected)) {
            result.addError(formatMessage("imageTypeMismatch", detected, expectedType));
            return;
        }

        if ("svg".equals(extension)) {
            validateSvg(content, result);
        }
    }

    protected void validateSvg(byte[] content, DeploymentResourceValidationResult result) {
        String text = decodeText(content, result);
        if (text == null) {
            return;
        }
        boolean valid = scanXml(text, result, reader -> "svg".equals(reader.getLocalName()), "notSvg", null);
        if (valid) {
            Set<String> unsafeConstructs = findUnsafeSvgConstructs(Jsoup.parse(text, "", Parser.xmlParser()));
            if (!unsafeConstructs.isEmpty()) {
                result.addError(formatMessage("svgUnsafeContent", String.join(", ", unsafeConstructs)));
            }
        }
    }

    /**
     * Finds the constructs of an SVG image that can execute code: scripts, event handler attributes,
     * {@code javascript:} URLs and {@code foreignObject} elements (embedded HTML).
     *
     * @return names of the found constructs, in the order of their first occurrence
     */
    protected Set<String> findUnsafeSvgConstructs(Document document) {
        Set<String> result = new LinkedHashSet<>();
        for (Element element : document.getAllElements()) {
            // the XML parser preserves prefixes and case: "svg:script", "foreignObject"
            String name = StringUtils.substringAfterLast(":" + element.tagName(), ":").toLowerCase(Locale.ROOT);
            if ("script".equals(name)) {
                result.add("script");
            } else if ("foreignobject".equals(name)) {
                result.add("foreignObject");
            }
            collectUnsafeAttributes(element, result);
        }
        return result;
    }

    /**
     * Checks that the content is a UTF-8 text: no NUL bytes, no control characters other than whitespace
     * in the leading bytes, strictly valid UTF-8.
     *
     * @return decoded text without a leading BOM or {@code null} if the content is not a text (an error is added)
     */
    @Nullable
    protected String decodeText(byte[] content, DeploymentResourceValidationResult result) {
        for (byte b : content) {
            if (b == 0) {
                result.addError(message("binaryContent"));
                return null;
            }
        }
        if (isBinary(content)) {
            result.addError(message("binaryContent"));
            return null;
        }
        String text;
        try {
            text = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(content))
                    .toString();
        } catch (CharacterCodingException e) {
            result.addError(message("invalidEncoding"));
            return null;
        }
        return !text.isEmpty() && text.charAt(0) == BOM ? text.substring(1) : text;
    }

    /**
     * Detects binary content the same way as the {@code file} utility does for text: the leading bytes must not
     * contain ASCII control characters except TAB, LF, FF, CR and ESC.
     *
     * @return whether the content (its first bytes) is binary
     */
    protected boolean isBinary(byte[] content) {
        int length = Math.min(content.length, TEXT_DETECTION_WINDOW);
        for (int i = 0; i < length; i++) {
            int b = content[i] & 0xFF;
            if (b < 0x20 && b != 0x09 && b != 0x0A && b != 0x0C && b != 0x0D && b != 0x1B) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses the XML document completely, checks the root element and passes each non-root element
     * to the handler. The nesting depth and the total number of elements are limited by
     * {@link ResourceValidationProperties}.
     *
     * @return {@code true} if the document is well-formed, has the expected root element and does not exceed
     * the limits
     */
    protected boolean scanXml(String text, DeploymentResourceValidationResult result,
                              Predicate<XMLStreamReader> rootMatcher, String rootErrorKey,
                              @Nullable XmlElementHandler elementHandler) {
        int maxDepth = properties.getXmlMaxDepth();
        int maxElements = properties.getXmlMaxElements();
        XMLStreamReader reader = null;
        try {
            reader = xmlInputFactory.createXMLStreamReader(new StringReader(text));
            boolean rootFound = false;
            int depth = 0;
            int elementCount = 0;
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.END_ELEMENT) {
                    depth--;
                    continue;
                }
                if (event != XMLStreamConstants.START_ELEMENT) {
                    continue;
                }
                depth++;
                elementCount++;
                if (depth > maxDepth) {
                    result.addError(formatMessage("xmlTooDeep", maxDepth));
                    return false;
                }
                if (elementCount > maxElements) {
                    result.addError(formatMessage("xmlTooManyElements", maxElements));
                    return false;
                }
                if (!rootFound) {
                    rootFound = true;
                    if (!rootMatcher.test(reader)) {
                        result.addError(message(rootErrorKey));
                        return false;
                    }
                } else if (elementHandler != null) {
                    elementHandler.onStartElement(reader, depth);
                }
            }
            if (!rootFound) {
                result.addError(message(rootErrorKey));
                return false;
            }
            return true;
        } catch (XMLStreamException e) {
            result.addError(formatMessage("invalidXml", getXmlErrorMessage(e)));
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException e) {
                    log.debug("Unable to close XML reader", e);
                }
            }
        }
    }

    protected String getXmlErrorMessage(XMLStreamException e) {
        String message = Objects.toString(e.getMessage(), e.getClass().getSimpleName());
        return StringUtils.normalizeSpace(message);
    }

    protected void addKey(XMLStreamReader reader, DeploymentResourceValidationResult result) {
        String id = reader.getAttributeValue(null, "id");
        if (StringUtils.isNotBlank(id)) {
            result.addKey(id.trim());
        }
    }

    /**
     * Extracts the references to other resources from a BPMN element (Camunda 7 extensions; the {@code camunda:} prefix
     * stands for any of the {@link #EXTENSION_NAMESPACES}, e.g. {@code operaton:}):
     * <ul>
     *     <li>{@code callActivity calledElement} + {@code camunda:calledElementBinding}: a process
     *     (CMMN {@code camunda:caseRef} is ignored)</li>
     *     <li>{@code businessRuleTask camunda:decisionRef} + {@code camunda:decisionRefBinding}: a decision</li>
     *     <li>{@code userTask}/{@code startEvent camunda:formRef} + {@code camunda:formRefBinding}: a Camunda Form by key;
     *     {@code camunda:formKey="embedded:deployment:<name>"}: an HTML form resource;
     *     {@code camunda:formKey="camunda-forms:deployment:<name>"}: a Camunda Form resource. Other form keys
     *     ({@code embedded:app:}, {@code app:}, {@code camunda-forms:app:}, custom keys) point outside the deployment
     *     and the engine and are not extracted</li>
     *     <li>{@code scriptTask camunda:resource} and {@code camunda:script resource} with the {@code deployment://}
     *     prefix: a script resource. Classpath and other resources are not extracted</li>
     * </ul>
     */
    protected void extractReferences(XMLStreamReader reader, @Nullable String sourceElementId,
                                     DeploymentResourceValidationResult result) {
        String namespace = reader.getNamespaceURI();
        String name = reader.getLocalName();
        if (BPMN_NAMESPACE.equals(namespace)) {
            switch (name) {
                case "callActivity" -> addDefinitionReference(result, ReferenceKind.PROCESS,
                        reader.getAttributeValue(null, "calledElement"),
                        getExtensionAttribute(reader, "calledElementBinding"), sourceElementId);
                case "businessRuleTask" -> addDefinitionReference(result, ReferenceKind.DECISION,
                        getExtensionAttribute(reader, "decisionRef"),
                        getExtensionAttribute(reader, "decisionRefBinding"), sourceElementId);
                case "userTask", "startEvent" -> {
                    addDefinitionReference(result, ReferenceKind.CAMUNDA_FORM,
                            getExtensionAttribute(reader, "formRef"),
                            getExtensionAttribute(reader, "formRefBinding"), sourceElementId);
                    addFormKeyReference(result, getExtensionAttribute(reader, "formKey"), sourceElementId);
                }
                case "scriptTask" ->
                        addScriptReference(result, getExtensionAttribute(reader, "resource"), sourceElementId);
                default -> {
                }
            }
        } else if (namespace != null && EXTENSION_NAMESPACES.contains(namespace) && "script".equals(name)) {
            addScriptReference(result, reader.getAttributeValue(null, "resource"), sourceElementId);
        }
    }

    protected void addDefinitionReference(DeploymentResourceValidationResult result, ReferenceKind kind,
                                          @Nullable String key, @Nullable String binding,
                                          @Nullable String sourceElementId) {
        String trimmedKey = StringUtils.trimToNull(key);
        if (trimmedKey == null) {
            return;
        }
        String normalizedBinding = StringUtils.defaultIfBlank(StringUtils.trim(binding),
                ResourceReference.BINDING_LATEST);
        result.addReference(ResourceReference.builder()
                .kind(kind)
                .key(trimmedKey)
                .binding(normalizedBinding)
                .deploymentBinding(ResourceReference.BINDING_DEPLOYMENT.equals(normalizedBinding))
                .dynamic(ResourceReference.isExpression(trimmedKey))
                .sourceElementId(sourceElementId)
                .build());
    }

    protected void addFormKeyReference(DeploymentResourceValidationResult result, @Nullable String formKey,
                                       @Nullable String sourceElementId) {
        String trimmedFormKey = StringUtils.trimToNull(formKey);
        if (trimmedFormKey == null) {
            return;
        }
        if (trimmedFormKey.startsWith(EMBEDDED_DEPLOYMENT_FORM_PREFIX)) {
            addResourceReference(result, ReferenceKind.HTML_FORM,
                    trimmedFormKey.substring(EMBEDDED_DEPLOYMENT_FORM_PREFIX.length()), sourceElementId);
        } else if (trimmedFormKey.startsWith(CAMUNDA_FORMS_DEPLOYMENT_PREFIX)) {
            addResourceReference(result, ReferenceKind.CAMUNDA_FORM,
                    trimmedFormKey.substring(CAMUNDA_FORMS_DEPLOYMENT_PREFIX.length()), sourceElementId);
        }
    }

    protected void addScriptReference(DeploymentResourceValidationResult result, @Nullable String resource,
                                      @Nullable String sourceElementId) {
        String trimmedResource = StringUtils.trimToNull(resource);
        if (trimmedResource != null && trimmedResource.startsWith(DEPLOYMENT_RESOURCE_PREFIX)) {
            addResourceReference(result, ReferenceKind.SCRIPT,
                    trimmedResource.substring(DEPLOYMENT_RESOURCE_PREFIX.length()), sourceElementId);
        }
    }

    /**
     * Adds a reference to a resource of the same deployment by the resource name.
     */
    protected void addResourceReference(DeploymentResourceValidationResult result, ReferenceKind kind,
                                        String resourceName, @Nullable String sourceElementId) {
        String trimmedName = StringUtils.trimToNull(resourceName);
        if (trimmedName == null) {
            return;
        }
        result.addReference(ResourceReference.builder()
                .kind(kind)
                .key(trimmedName)
                .binding(ResourceReference.BINDING_DEPLOYMENT)
                .deploymentBinding(true)
                .byResourceName(true)
                .dynamic(ResourceReference.isExpression(trimmedName))
                .sourceElementId(sourceElementId)
                .build());
    }

    @Nullable
    protected String getExtensionAttribute(XMLStreamReader reader, String localName) {
        for (String namespace : EXTENSION_NAMESPACES) {
            String value = reader.getAttributeValue(namespace, localName);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * @return the id of the innermost BPMN element on the current path that has an id
     */
    @Nullable
    protected String getNearestElementId(List<String> elementIds) {
        for (int i = elementIds.size() - 1; i >= 0; i--) {
            if (elementIds.get(i) != null) {
                return elementIds.get(i);
            }
        }
        return null;
    }

    protected boolean isDmnNamespace(@Nullable String namespace) {
        return namespace != null && DMN_NAMESPACE_PREFIXES.stream().anyMatch(namespace::startsWith);
    }

    /**
     * Detects the image type by the file signature (magic bytes). SVG is detected by the {@code svg} root element
     * of a UTF-8 XML text.
     *
     * @return the detected media type or {@code null} if the content is not a supported image
     */
    @Nullable
    protected String detectImageType(byte[] content) {
        if (startsWith(content, PNG_SIGNATURE)) {
            return "image/png";
        }
        if (startsWith(content, JPEG_SIGNATURE)) {
            return "image/jpeg";
        }
        if (startsWith(content, GIF87_SIGNATURE) || startsWith(content, GIF89_SIGNATURE)) {
            return "image/gif";
        }
        if (isSvg(content)) {
            return "image/svg+xml";
        }
        return null;
    }

    protected boolean isSvg(byte[] content) {
        if (isBinary(content)) {
            return false;
        }
        String text;
        try {
            text = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(content))
                    .toString();
        } catch (CharacterCodingException e) {
            return false;
        }
        if (!text.isEmpty() && text.charAt(0) == BOM) {
            text = text.substring(1);
        }
        return SVG_PROLOG_PATTERN.matcher(text).lookingAt();
    }

    protected static boolean startsWith(byte[] content, byte[] signature) {
        if (content.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if (content[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }

    protected String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex >= 0 ? fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT) : "";
    }

    /**
     * Creates a StAX factory hardened against XXE: DTDs and external entities are not supported.
     */
    protected static XMLInputFactory createXmlInputFactory() {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        setPropertyIfSupported(factory, XMLConstants.ACCESS_EXTERNAL_DTD, "");
        setPropertyIfSupported(factory, XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory;
    }

    protected static void setPropertyIfSupported(XMLInputFactory factory, String name, Object value) {
        try {
            factory.setProperty(name, value);
        } catch (IllegalArgumentException e) {
            log.debug("XML input factory property '{}' is not supported", name);
        }
    }

    protected String message(String key) {
        return messages.getMessage(MESSAGE_GROUP, key);
    }

    protected String formatMessage(String key, Object... params) {
        return messages.formatMessage(MESSAGE_GROUP, key, params);
    }

    /**
     * Handles a start element of a scanned XML document.
     */
    @FunctionalInterface
    protected interface XmlElementHandler {

        /**
         * @param reader reader positioned at the start element
         * @param depth  nesting depth of the element, the root element has depth 1
         */
        void onStartElement(XMLStreamReader reader, int depth);
    }
}
