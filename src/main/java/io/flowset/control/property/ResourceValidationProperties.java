/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.property;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Settings of the content validation of the resources uploaded to a deployment.
 */
@ConfigurationProperties(prefix = "flowset.control.resource-validation")
@ConfigurationPropertiesBinding
public class ResourceValidationProperties {

    /**
     * A maximum nesting depth of the elements of an uploaded XML resource (BPMN, DMN, SVG).
     * A deeper document is rejected.
     */
    @Positive
    private final int xmlMaxDepth;

    /**
     * A maximum total number of the elements of an uploaded XML resource (BPMN, DMN, SVG).
     * A document with more elements is rejected.
     */
    @Positive
    private final int xmlMaxElements;

    public ResourceValidationProperties(@DefaultValue("64") int xmlMaxDepth,
                                        @DefaultValue("100000") int xmlMaxElements) {
        this.xmlMaxDepth = xmlMaxDepth;
        this.xmlMaxElements = xmlMaxElements;
    }

    /**
     * @return a maximum nesting depth of the elements of an uploaded XML resource
     */
    public int getXmlMaxDepth() {
        return xmlMaxDepth;
    }

    /**
     * @return a maximum total number of the elements of an uploaded XML resource
     */
    public int getXmlMaxElements() {
        return xmlMaxElements;
    }
}
