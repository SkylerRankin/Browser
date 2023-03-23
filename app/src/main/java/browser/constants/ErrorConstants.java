package browser.constants;

public class ErrorConstants {

    public static final String ErrorPagePath = "file://src/main/resources/html/error_page.html";

    public enum ErrorType {
        LOCAL_FILE_DOES_NOT_EXIST,
        LOCAL_FILE_FAILED_TO_LOAD,
        LOCAL_FILE_IS_DIRECTORY,
        LOCAL_FILE_IS_NOT_HTML,
        NETWORK_FILE_BAD_HTTP_CODE,
        NETWORK_FILE_FAILED_TO_LOAD,
        NETWORK_FILE_NOT_HTML
    }

    // Strings use for exception context maps
    public static final String CONTENT_TYPE = "ContentType";
    public static final String HTTP_CODE = "HTTPCode";
    public static final String EXCEPTION = "Exception";
    public static final String PATH = "path";

    // Strings used for templating the error page html
    public static final String ERROR_PAGE_SUBTITLE = "$SUBTITLE$";
    public static final String ERROR_PAGE_MESSAGE = "$MESSAGE$";

}
