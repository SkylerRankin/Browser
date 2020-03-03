package network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import app.ErrorPageHandler;
import javafx.scene.image.Image;

public class HTTPClient {
	
	// Save a reference to the site so we can build up the URLs for other local resources
	// TODO have a better system than this
	private static String baseURL;
    
    public static String requestPage(String urlString) {
        urlString = HTTPClient.formatURL(urlString);
        
        try {
            URL url = new URL(urlString);
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
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
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
    
    public static Image downloadImage(String urlString) {
    	if (urlString == null) return null;
    	Image image = null;
    	URL url = null;
    	
    	// Try to download from url directly
    	try {
			url = new URL(formatURL(urlString));
			InputStream in = new BufferedInputStream(url.openStream());
			image = new Image(in);
		} catch (IOException e) {
			System.err.printf("HTTPClient: error downloading image from %s\n", url.toString());
		}
    	
    	// Assume urlString is a relative resource; try downloading with base URL prepended
    	try {
			url = new URL(formatURL(String.format("%s//%s", baseURL, urlString)));
			InputStream in = new BufferedInputStream(url.openStream());
			image = new Image(in);
		} catch (IOException e) {
			System.err.printf("HTTPClient: error downloading image from %s\n", url.toString());
		}
    	
    	return image;
    }
    
    public static String formatURL(String url) {
        if (!url.startsWith("http")) {
            return "http://"+url;
        } else {
            return url;
        }
    }

}
