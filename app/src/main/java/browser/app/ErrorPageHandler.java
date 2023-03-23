package browser.app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import browser.constants.ErrorConstants;
import browser.constants.ResourceConstants;
import browser.exception.LayoutException;
import browser.exception.PageLoadException;

public class ErrorPageHandler {

    public static Exception previousException;

    private static Map<Integer, String> httpResponseMessages;

    public static void init() {
        httpResponseMessages = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("./src/main/resources/data/httpResponseMessages.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                int code = Integer.parseInt(line.substring(0, line.indexOf(' ')));
                String message = line.substring(line.indexOf(' '));
                httpResponseMessages.put(code, message);
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("ErrorPageHandler: error reading httpResponseMessages.txt");
            e.printStackTrace();
        }
    }
    
    public static String populateHTML(String html) {
        if (previousException == null) {
            return html;
        }

        if (previousException instanceof PageLoadException) {
            return processPageLoadException(html, (PageLoadException) previousException);
        } else if (previousException instanceof LayoutException) {
            return processLayoutException(html, (LayoutException) previousException);
        } else {
            return processUnknownException(html, previousException);
        }
    }

    private static String processPageLoadException(String html, PageLoadException e) {
        String subtitle = "", message = "";
        Map<String, Object> context = e.getContext();
        switch (e.getErrorType()) {
            case LOCAL_FILE_DOES_NOT_EXIST -> {
                String filePath = ((String) context.get(ErrorConstants.PATH)).substring(ResourceConstants.FILE_PREFIX.length());
                subtitle = "Local file not found.";
                message = String.format("The file <code>%s</code> does not exist in your local filesystem.", filePath);
            }
            case LOCAL_FILE_FAILED_TO_LOAD -> {
                Exception exception = (Exception) context.get(ErrorConstants.EXCEPTION);
                subtitle = "Local filesystem failure.";
                message = exception.toString();
            }
            case LOCAL_FILE_IS_DIRECTORY -> {
                subtitle = "Provided path is a directory.";
                message = "The provided local path points to a directory, not an HTML file.";
            }
            case LOCAL_FILE_IS_NOT_HTML -> {
                subtitle = "Local file is not HTML.";
                message = "HTML files are expected to use the <code>.html</code> file extension, but the provided file path does not.";
            }
            case NETWORK_FILE_BAD_HTTP_CODE -> {
                int httpCode = (int) context.get(ErrorConstants.HTTP_CODE);
                subtitle = String.format("%d - %s", httpCode, httpResponseMessages.get(httpCode));
                message = "";
            }
            case NETWORK_FILE_FAILED_TO_LOAD -> {
                Exception exception = (Exception) context.get(ErrorConstants.EXCEPTION);
                subtitle = "Network failure.";
                message = exception.toString();
            }
            case NETWORK_FILE_NOT_HTML -> {
                String contentType = (String) context.get(ErrorConstants.CONTENT_TYPE);
                subtitle = "The requested resource is not HTML.";
                message = String.format("URLs should point to paths that respond with HTML text, as identified by the <code>Content-Type: text/html</code> header. This resource has <code>Content-Type: %s</code>.", contentType);
            }
        }
        html = html.replace(ErrorConstants.ERROR_PAGE_SUBTITLE, subtitle);
        html = html.replace(ErrorConstants.ERROR_PAGE_MESSAGE, message);
        return html;
    }

    private static String processLayoutException(String html, LayoutException e) {
        String subtitle = "A layout failure occurred.";
        String message = e.getCause().getMessage();
        html = html.replace(ErrorConstants.ERROR_PAGE_SUBTITLE, subtitle);
        html = html.replace(ErrorConstants.ERROR_PAGE_MESSAGE, message);
        return html;
    }

    private static String processUnknownException(String html, Exception e) {
        String subtitle = "An unknown issue occurred.";
        String message = e.getMessage() == null ? "" : e.getMessage();
        html = html.replace(ErrorConstants.ERROR_PAGE_SUBTITLE, subtitle);
        html = html.replace(ErrorConstants.ERROR_PAGE_MESSAGE, message);
        return html;
    }

}
