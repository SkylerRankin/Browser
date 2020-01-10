package network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPClient {
    
    public static String requestPage(String urlString) {
        urlString = HTTPClient.formatURL(urlString);
        
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setInstanceFollowRedirects(false);
            
            int responseCode = conn.getResponseCode();
            
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
    
    public static String formatURL(String url) {
        if (!url.startsWith("http")) {
            return "http://"+url;
        } else {
            return url;
        }
    }

}
