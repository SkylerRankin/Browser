package app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ErrorPageHandler {
    
    public static int responseCode = 0;
    public static BrowserErrorType browserError = BrowserErrorType.NONE;
    public static final String errorPagePath = "file://res/html/error_page.html";
    private static Map<Integer, String> httpResponseMessages;
    private static Map<BrowserErrorType, String> browserErrorMessages;
    public static enum BrowserErrorType {NONE, NO_BODY}
    
    public static void init() {
        httpResponseMessages = new HashMap<Integer, String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("./res/data/httpResponseMessages.txt"));
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
        
        browserErrorMessages = new HashMap<BrowserErrorType, String>();
        browserErrorMessages.put(BrowserErrorType.NO_BODY, "No body tag found.");
        
    }
    
    public static String populateHTML(String html) {
        html = html.replace("$ERROR_CODE$", ""+responseCode);
        if (responseCode != 200) {
            html = html.replace("$MESSAGE$", httpResponseMessages.get(responseCode));
        } else if (!browserError.equals(BrowserErrorType.NONE)) {
            html = html.replace("$MESSAGE$", browserErrorMessages.get(browserError));
        }
        return html;
    }

}
