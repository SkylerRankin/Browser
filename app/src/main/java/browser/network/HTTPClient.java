package browser.network;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javafx.scene.image.Image;

import browser.app.ErrorPageHandler;

public class HTTPClient {

    // Save a reference to the site so we can build up the URLs for other local resources
    // TODO: have a better system than this
    private static String baseURL;
    private static String baseHost;
    
    public static String requestPage(String urlString) {
        urlString = HTTPClient.formatURL(urlString);
        
        try {
            URL url = new URL(urlString);
            baseHost = url.getHost();
            System.out.println(baseHost);
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
            ErrorPageHandler.responseCode = responseCode;
            System.out.printf("HTTPClient Response Code %d\n", responseCode);
            
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine).append("\n");
            }
            in.close();
            
            return response.toString();
        } catch (Exception e) {
            System.out.println("HTTPClient: error while fetching webpage.");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    public static String requestResource(String url) {
        if (url.startsWith("http")) {
            return requestPage(url);
        } else {
            return requestPage(String.format("%s//%s", baseURL, url));
        }
    }
    
    public static Image downloadImage(String rawURL) {
        if (rawURL == null) return null;

        List<String> urls = List.of(
                rawURL,
                formatURL(String.format("%s//%s", baseURL, rawURL)),
                formatURL(String.format("%s//%s", baseHost, rawURL))
        );

        for (String urlString : urls) {
            try {
                URL url = new URL(formatURL(urlString));
                InputStream in = new BufferedInputStream(url.openStream());
                Image image = new Image(in);
                return image;
            } catch (IOException e) {
                System.err.printf("HTTPClient: IO error downloading image from %s\n", urlString);
            }
        }

        return null;
    }
    
    public static String formatURL(String url) {
        if (!url.startsWith("http")) {
            return "http://"+url;
        } else {
            return url;
        }
    }

}
