package browser.exception;

import lombok.Getter;

public class LayoutException extends Exception {

    @Getter
    private final Exception cause;

    public LayoutException(Exception cause) {
        this.cause = cause;
    }

}
