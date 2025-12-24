package io.flowset.control.entity.job;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum JobState implements EnumClass<String> {

    ACTIVE("ACTIVE"),
    SUSPENDED("SUSPENDED");

    private final String id;

    JobState(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static JobState fromId(String id) {
        for (JobState at : JobState.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}