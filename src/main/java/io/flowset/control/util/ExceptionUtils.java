package io.flowset.control.util;

import feign.FeignException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

public class ExceptionUtils {

    /**
     * Determines whether the specified exception is related to a connection error.
     *
     * @param exception the exception to be checked for a connection error.
     * @return true if the exception represents a connection error, otherwise - false.
     */
    public static boolean isConnectionError(Throwable exception) {
        return exception instanceof ConnectException || exception instanceof SocketTimeoutException;
    }

    /**
     * Determines whether the specified exception represents a 404 error.
     *
     * @param exception the exception to be checked for a 404 error.
     * @return true if the exception represents a 404 error, otherwise - false.
     */
    public static boolean isNotFoundError(Throwable exception) {
        return exception instanceof FeignException feignException && feignException.status() == 404;
    }
}
