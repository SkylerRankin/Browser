package browser.network;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javafx.scene.image.Image;

import browser.constants.ErrorConstants;
import browser.constants.ResourceConstants;
import browser.exception.PageLoadException;

public class HTTPClient {

    // Save a reference to the site so we can build up the URLs for other local resources
    // TODO: have a better system than this
    private static String baseURL;
    private static String baseHost;
    
    public static String requestPage(String urlString) throws PageLoadException {
        urlString = HTTPClient.formatURL(urlString);
        
        try {
            URL url = new URL(urlString);
            baseHost = url.getHost();
            baseURL = urlString;
            if (urlString.endsWith("html")) {
                baseURL = urlString.substring(0, baseURL.lastIndexOf("/"));
            }

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setInstanceFollowRedirects(false);
            
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new PageLoadException(ErrorConstants.ErrorType.NETWORK_FILE_BAD_HTTP_CODE, Map.of(ErrorConstants.HTTP_CODE, responseCode));
            }

            String contentType = conn.getHeaderField(ResourceConstants.CONTENT_TYPE_KEY);
            if (!contentType.startsWith("text/html")) {
                throw new PageLoadException(ErrorConstants.ErrorType.NETWORK_FILE_NOT_HTML, Map.of(ErrorConstants.CONTENT_TYPE, contentType));
            }
            
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine).append("\n");
            }
            in.close();
            
            return response.toString();
        } catch (Exception e) {
            if (e instanceof PageLoadException) {
                throw (PageLoadException) e;
            } else {
                throw new PageLoadException(ErrorConstants.ErrorType.NETWORK_FILE_FAILED_TO_LOAD, Map.of(ErrorConstants.EXCEPTION, e));
            }
        }
    }
    
    public static String requestResource(String urlString) {
        if (!urlString.startsWith("http")) {
            urlString = String.format("%s//%s", baseURL, urlString);
        }

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setInstanceFollowRedirects(false);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine).append("\n");
            }
            in.close();

            return response.toString();
        } catch (Exception e) {
            System.err.printf("Failed to load resource %s: %s\n", urlString, e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public static Image downloadImage(String rawURL) {
        if (rawURL == null) {
            return null;
        }

        List<String> urls = List.of(
                rawURL,
                formatURL(String.format("%s//%s", baseURL, rawURL)),
                formatURL(String.format("%s//%s", baseHost, rawURL))
        );

        for (String urlString : urls) {
            try {
                URL url = new URL(formatURL(urlString));
                InputStream in = new BufferedInputStream(url.openStream());
                return new Image(in);
            } catch (IOException e) {
                System.err.printf("HTTPClient: IO error downloading image from %s\n", urlString);
            }
        }

        return null;
    }
    
    private static String formatURL(String url) {
        if (!url.startsWith("http")) {
            return "http://"+url;
        } else {
            return url;
        }
    }

}
