package test;

import org.junit.jupiter.api.Test;

import network.HTTPClient;

public class HTTPClientTest {
    
    @Test
    public void testRequestPage() {
        String html = HTTPClient.requestPage("https://en.wikipedia.org/wiki/Siberian_accentor");
    }
    
    @Test
    public void testDownloadImage() {
    	HTTPClient.downloadImage("https://upload.wikimedia.org/wikipedia/en/9/90/ElderScrollsOblivionScreenshot11.jpg");
    }

}
