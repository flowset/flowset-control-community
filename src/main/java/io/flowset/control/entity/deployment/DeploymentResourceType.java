/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.deployment;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;

/**
 * Types of resources that can be uploaded to a deployment.
 */
public enum DeploymentResourceType implements EnumClass<String> {

    BPMN("bpmn", List.of(".bpmn", ".bpmn20.xml")),
    DMN("dmn", List.of(".dmn", ".dmn11.xml")),
    CMMN("cmmn", List.of(".cmmn", ".cmmn10.xml", ".cmmn11.xml")),
    CAMUNDA_FORM("camundaForm", List.of(".form")),
    HTML_FORM("htmlForm", List.of(".html", ".htm")),
    SCRIPT("script", List.of(".js", ".groovy", ".py", ".rb", ".kts", ".scala", ".jexl")),
    IMAGE("image", List.of(".png", ".jpg", ".jpeg", ".gif", ".svg", ".webp", ".bmp")),
    /**
     * Any other file: the engine stores it in the deployment as is.
     */
    OTHER("other", List.of());

    private final String id;
    private final List<String> extensions;

    DeploymentResourceType(String id, List<String> extensions) {
        this.id = id;
        this.extensions = extensions;
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * @return file extensions of this resource type: lowercase, with a leading dot
     */
    public List<String> getExtensions() {
        return extensions;
    }

    @Nullable
    public static DeploymentResourceType fromId(String id) {
        for (DeploymentResourceType type : DeploymentResourceType.values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Determines a resource type by the file name extension (case-insensitive).
     * The longest matching extension wins, e.g. {@code process.bpmn20.xml} is resolved as {@link #BPMN};
     * a file with an unknown extension is {@link #OTHER}.
     *
     * @param fileName file name
     * @return resource type
     */
    public static DeploymentResourceType fromFileName(@Nullable String fileName) {
        if (fileName == null) {
            return OTHER;
        }
        String lowerCaseName = fileName.toLowerCase(Locale.ROOT);
        DeploymentResourceType result = OTHER;
        int matchedLength = 0;
        for (DeploymentResourceType type : values()) {
            for (String extension : type.extensions) {
                if (extension.length() > matchedLength
                        && lowerCaseName.length() > extension.length()
                        && lowerCaseName.endsWith(extension)) {
                    result = type;
                    matchedLength = extension.length();
                }
            }
        }
        return result;
    }

}
