package io.flowset.control.entity.engine;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum EnvironmentType implements EnumClass<String> {

    PROD("PROD"),
    PRE_PROD("PRE_PROD"),
    STAGE("STAGE"),
    QA("QA"),
    DEV("DEV"),
    LOCAL("LOCAL");

    private final String id;

    EnvironmentType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static EnvironmentType fromId(String id) {
        for (EnvironmentType at : EnvironmentType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}