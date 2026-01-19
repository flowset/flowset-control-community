package io.flowset.control.util;

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
}
