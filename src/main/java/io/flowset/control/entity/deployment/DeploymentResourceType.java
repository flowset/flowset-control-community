/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.deployment;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Types of resources that can be uploaded to a deployment.
 */
public enum DeploymentResourceType implements EnumClass<String> {

    BPMN("bpmn", List.of(".bpmn", ".bpmn20.xml")),
    DMN("dmn", List.of(".dmn", ".dmn11.xml")),
    CAMUNDA_FORM("camundaForm", List.of(".form")),
    HTML_FORM("htmlForm", List.of(".html", ".htm")),
    SCRIPT("script", List.of(".js", ".groovy", ".py", ".rb")),
    IMAGE("image", List.of(".png", ".jpg", ".jpeg", ".gif", ".svg"));

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
     * The longest matching extension wins, e.g. {@code process.bpmn20.xml} is resolved as {@link #BPMN}.
     *
     * @param fileName file name
     * @return resource type or {@code null} if the file type is not supported
     */
    @Nullable
    public static DeploymentResourceType fromFileName(@Nullable String fileName) {
        if (fileName == null) {
            return null;
        }
        String lowerCaseName = fileName.toLowerCase(Locale.ROOT);
        DeploymentResourceType result = null;
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

    /**
     * @return all supported file extensions, longest first
     */
    public static List<String> getAllExtensions() {
        return Arrays.stream(values())
                .flatMap(type -> type.extensions.stream())
                .sorted(Comparator.comparingInt(String::length).reversed())
                .toList();
    }

    /**
     * @return all supported file extensions as an array suitable for the {@code acceptedFileTypes} upload property
     */
    public static String[] getAcceptedFileTypes() {
        return getAllExtensions().toArray(String[]::new);
    }
}
