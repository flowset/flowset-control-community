package io.flowset.control.exception;

import io.jmix.flowui.view.View;
import lombok.Getter;
import lombok.Setter;

/**
 * An exception occurs when loading data from the BPM engine in a view (for example, in a data loader delegate) due to a connection error.
 */
@Getter
@Setter
public class ViewEngineConnectionFailedException extends RuntimeException {
    private View<?> source;

    public ViewEngineConnectionFailedException(View<?> source) {
        this.source = source;
    }

    public ViewEngineConnectionFailedException(Throwable cause, View<?> source) {
        super(cause);
        this.source = source;
    }

    public ViewEngineConnectionFailedException(String message, View<?> source) {
        super(message);
        this.source = source;
    }

    public ViewEngineConnectionFailedException(String message, Throwable cause, View<?> source) {
        super(message, cause);
        this.source = source;
    }
}
