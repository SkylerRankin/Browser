package tasks;

import javafx.concurrent.Task;
import network.HTTPClient;

public class LoadWebpageTask extends Task<String> {
    
    private String url;
    
    public LoadWebpageTask(String url) {
        this.url = url;
        // "https://en.wikipedia.org/wiki/Siberian_accentor"
    }

    @Override
    protected String call() throws Exception {
        return HTTPClient.requestPage(url);
    }

}
