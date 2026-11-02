package io.flowset.control.util;


import io.flowset.control.entity.engine.EngineType;
import io.flowset.control.entity.variable.CamundaVariableType;
import io.flowset.control.entity.variable.ProcessVariableType;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ProcessVariableUtils {

    /**
     * Resolve the process variable type based on the engine type and the variable type string.
     *
     * @param engineType an engine type
     * @param typeString the variable type string received from BPM engine
     * @return instance of child ProcessVariableType
     */
    @Nullable
    public static ProcessVariableType resolveVariableType(@Nullable EngineType engineType, String typeString) {
        if (engineType == null || engineType.isCamunda7Compatible()) {
            return CamundaVariableType.fromId(typeString);
        }
        return null;
    }

    /**
     * Returns all process variable types supported by the given engine type.
     *
     * @param engineType an engine type
     * @return list of process variable types
     */
    public static List<ProcessVariableType> getAllVariableTypes(@Nullable EngineType engineType) {
        if (engineType == null || engineType.isCamunda7Compatible()) {
            return Arrays.stream(CamundaVariableType.values())
                    .map(ProcessVariableType.class::cast)
                    .toList();
        }

        return Collections.emptyList();
    }

    /**
     * Returns the primitive process variable types supported by the given engine type.
     *
     * @param engineType an engine type, {@code null} if no engine is selected
     * @return list of primitive process variable types
     */
    public static List<ProcessVariableType> getPrimitiveVariableTypes(@Nullable EngineType engineType) {
        return getAllVariableTypes(engineType).stream()
                .filter(ProcessVariableType::isPrimitive)
                .toList();
    }

    /**
     * Checks whether a variable of the given type keeps its value as binary content (a file or a byte array), which
     * is loaded and saved separately from the other variable values.
     *
     * @param engineType an engine type, {@code null} if no engine is selected
     * @param typeId     the variable type string received from BPM engine
     * @return true if the variable type is binary
     */
    public static boolean isBinaryType(@Nullable EngineType engineType, String typeId) {
        if (engineType == null || engineType.isCamunda7Compatible()) {
            CamundaVariableType type = CamundaVariableType.fromId(typeId);
            return type == CamundaVariableType.FILE || type == CamundaVariableType.BYTES;
        }
        return false;
    }

}
