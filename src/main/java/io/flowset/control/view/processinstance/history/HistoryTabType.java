/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processinstance.history;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.jspecify.annotations.Nullable;

/**
 * Tabs of the history section in the process instance detail view. {@link HistoryTabFragment} keeps the tabs shown
 * for the selected engine type as an ordered list of these values, so a value's position in the list is the index
 * of its tab in the tab sheet.
 */
public enum HistoryTabType implements EnumClass<String> {

    ACTIVITIES("historicActivityInstancesTab"),
    USER_TASKS("historyTasksTab"),
    VARIABLES("historyVariablesTab"),
    INCIDENTS("historyIncidentsTab"),
    DECISIONS("historyDecisionsTab");

    private final String id;

    HistoryTabType(String id) {
        this.id = id;
    }

    /**
     * Returns the identifier of the tab type.
     *
     * @return the tab type identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Finds the tab type by its identifier.
     *
     * @param id the tab type identifier
     * @return the tab type with the given identifier, or {@code null} if there is none
     */
    @Nullable
    public static HistoryTabType fromId(String id) {
        for (HistoryTabType at : HistoryTabType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
