package browser.network;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.scene.image.Image;

import browser.constants.ErrorConstants;
import browser.constants.ResourceConstants;
import browser.exception.PageLoadException;
import browser.parser.SpecialSymbolHandler;

public class HTTPClient {

    // Save a reference to the site so we can build up the URLs for other local resources
    // TODO: have a better system than this
    private static String baseURL;
    private static URL pageURL;
    
    public static String requestPage(String urlString) throws PageLoadException {
        urlString = HTTPClient.formatURL(urlString);
        
        try {
            URL url = new URL(urlString);
            pageURL = url;
            baseURL = urlString;
            if (urlString.endsWith("html")) {
                baseURL = urlString.substring(0, baseURL.lastIndexOf("/"));
            }

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Host", pageURL.getHost());
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setInstanceFollowRedirects(false);
            
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new PageLoadException(ErrorConstants.ErrorType.NETWORK_FILE_BAD_HTTP_CODE, Map.of(ErrorConstants.HTTP_CODE, responseCode));
            }

            String contentType = conn.getHeaderField(ResourceConstants.CONTENT_TYPE_KEY);
            if (!contentType.startsWith(ResourceConstants.HTML_CONTENT_TYPE_PREFIX)) {
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
    
    public static String requestResource(String rawURL) {
        for (String urlString : getURLCandidateList(rawURL)) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(formatURL(urlString));
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Host", pageURL.getHost());
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setInstanceFollowRedirects(true);

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine).append("\n");
                }
                in.close();
                conn.disconnect();
                return response.toString();
            } catch (Exception e) {
                System.err.printf("Failed to load resource from %s\n", formatURL(urlString));
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
        return null;
    }
    
    public static Image downloadImage(String rawURL) {
        if (rawURL == null) {
            return null;
        }

        for (String urlString : getURLCandidateList(rawURL)) {
            try {
                URL url = new URL(formatURL(urlString));
                InputStream in = new BufferedInputStream(url.openStream());
                return new Image(in);
            } catch (IOException e) {
                System.err.printf("HTTPClient: IO error downloading image from %s\n", formatURL(urlString));
            }
        }

        return null;
    }
    
    private static String formatURL(String url) {
        url = SpecialSymbolHandler.insertSymbols(url);
        url = URLDecoder.decode(url, StandardCharsets.UTF_8);
        String protocol = pageURL == null ? "https" : pageURL.getProtocol();
        if (url.startsWith("//")) {
            url = String.format("%s:%s", protocol, url);
        } else if (!url.startsWith("http")) {
            url = String.format("%s://%s", protocol, url);
        }
        return url;
    }

    private static List<String> getURLCandidateList(String rawURL) {
        List<String> urls = new ArrayList<>();
        if (!rawURL.startsWith("/")) {
            urls.add(rawURL);
        }
        urls.add(formatURL(String.format("%s%s%s", baseURL, !(baseURL.endsWith("/") || rawURL.startsWith("/")) ? "/" : "", rawURL)));
        urls.add(formatURL(String.format("%s%s%s", pageURL.getHost(), !(pageURL.getHost().endsWith("/") || rawURL.startsWith("/")) ? "/" : "", rawURL)));
        return urls;
    }

}
