package io.flowset.control.util;

import org.camunda.community.rest.client.model.CountResultDto;
import org.jspecify.annotations.Nullable;

public class EngineRestUtils {

    public static long getCountResult(@Nullable CountResultDto countResultDto) {
        if (countResultDto == null) {
            return 0;
        }
        Long count = countResultDto.getCount();
        return count != null ? count : 0;
    }
}