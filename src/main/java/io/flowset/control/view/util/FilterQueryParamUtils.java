package io.flowset.control.view.util;

import com.vaadin.flow.router.QueryParameters;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

public class FilterQueryParamUtils {

    /**
     * Converts a given {@link LocalDateTime} value to its string representation.
     *
     * @param value the {@link LocalDateTime} value to be converted
     * @return the formatted string of the provided {@link LocalDateTime} value, or null if the input value is null
     */
    public static String convertLocalDateTimeParamValue(@Nullable LocalDateTime value) {
        return value != null ? value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
    }

    /**
     * Converts a given {@link OffsetDateTime} value to its string representation.
     *
     * @param value the {@link OffsetDateTime} value to be converted
     * @return the formatted string of the provided {@link OffsetDateTime} value, or null if the input value is null
     */
    public static String convertOffsetDateTimeParamValue(@Nullable OffsetDateTime value) {
        return value != null ? value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null;
    }

    /**
     * Parses a string value into an {@link OffsetDateTime} object.
     *
     * @param value the string value to be parsed into an {@link OffsetDateTime}
     * @return the parsed {@link OffsetDateTime} object, or null if the input value is null
     */
    public static OffsetDateTime parseOffsetDateTimeParamValue(@Nullable String value) {
        return value != null ? OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null;
    }

    /**
     * Parses a string value into a {@link LocalDateTime} object.
     *
     * @param value the string value to be parsed into a {@link LocalDateTime}
     * @return the parsed {@link LocalDateTime} object, or null if the input value is null.
     */
    public static LocalDateTime parseLocalDateTimeParamValue(@Nullable String value) {
        return value != null ? LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
    }

    /**
     * Retrieves the value of a specified parameter from the query parameters as a {@link LocalDateTime} object.
     *
     * @param queryParameters the URL query parameters
     * @param paramName       the name of the parameter to retrieve as {@link LocalDateTime}
     * @return the {@link LocalDateTime} instance parsed from the parameter's value, or null if not found
     */
    @Nullable
    public static LocalDateTime getLocalDateTimeParam(QueryParameters queryParameters, String paramName) {
        return queryParameters.getSingleParameter(paramName)
                .map(FilterQueryParamUtils::parseLocalDateTimeParamValue)
                .orElse(null);
    }

    /**
     * Retrieves the value of a specified parameter from the query parameters as an {@link OffsetDateTime} object.
     *
     * @param queryParameters the URL query parameters
     * @param paramName       the name of the parameter to retrieve as {@link OffsetDateTime}
     * @return the {@link OffsetDateTime} instance parsed from the parameter's value, or null if not found
     */
    @Nullable
    public static OffsetDateTime getOffsetDateTimeParam(QueryParameters queryParameters, String paramName) {
        return queryParameters.getSingleParameter(paramName)
                .map(FilterQueryParamUtils::parseOffsetDateTimeParamValue)
                .orElse(null);
    }

    /**
     * Retrieves the value of a specified query parameter as a string.
     *
     * @param queryParameters the URL query parameters
     * @param paramName       the name of the parameter whose value is to be retrieved
     * @return the value of the query parameter as a string, or null if not found
     */
    @Nullable
    public static String getStringParam(QueryParameters queryParameters, String paramName) {
        return queryParameters.getSingleParameter(paramName)
                .orElse(null);
    }

    /**
     * Retrieves the value of a specified parameter from the query parameters as a {@link Boolean}.
     *
     * @param queryParameters the URL query parameters
     * @param paramName       the name of the parameter to retrieve as a {@link Boolean}
     * @return the {@link Boolean} value of the specified parameter, or null if the parameter is not found
     */
    @Nullable
    public static Boolean getBooleanParam(QueryParameters queryParameters, String paramName) {
        return queryParameters.getSingleParameter(paramName)
                .map(Boolean::parseBoolean)
                .orElse(null);
    }

    /**
     * Retrieves the value of a specified query parameter, transforms it using the provided parser function,
     * and returns the parsed value.
     *
     * @param queryParameters the URL query parameters
     * @param paramName       the name of the parameter to retrieve
     * @param valueParser     a function to parse the parameter's value from a string into the required type
     * @return the parsed value of the parameter, or null if the parameter is not found
     */
    @Nullable
    public static <V> V getSingleParam(QueryParameters queryParameters, String paramName, Function<String, V> valueParser) {
        return queryParameters.getSingleParameter(paramName)
                .map(valueParser)
                .orElse(null);
    }
}
