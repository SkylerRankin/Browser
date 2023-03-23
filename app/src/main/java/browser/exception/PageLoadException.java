package browser.exception;

import java.util.Map;

import browser.constants.ErrorConstants.ErrorType;

import lombok.Getter;

public class PageLoadException extends Exception {

    @Getter
    private final ErrorType errorType;
    @Getter
    private final Map<String, Object> context;

    public PageLoadException(ErrorType errorType) {
        this(errorType, null);
    }

    public PageLoadException(ErrorType errorType, Map<String, Object> context) {
        this.errorType = errorType;
        this.context = context;
    }

}
