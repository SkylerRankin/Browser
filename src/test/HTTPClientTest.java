package test;

import org.junit.jupiter.api.Test;

import network.HTTPClient;

public class HTTPClientTest {
    
    @Test
    public void test() {
        HTTPClient.requestPage("https://en.wikipedia.org/wiki/Siberian_accentor");
    }

}
