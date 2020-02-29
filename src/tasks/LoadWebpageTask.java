package tasks;

import app.Pipeline;
import javafx.concurrent.Task;
import network.HTTPClient;

public class LoadWebpageTask extends Task<Pipeline> {
    
    private String url;
    private Pipeline pipeline;
    private float width;
    
    public LoadWebpageTask(String url, float width, Pipeline pipeline) {
        this.url = url;
        this.pipeline = pipeline;
        this.width = width;
    }

    @Override
    protected Pipeline call() throws Exception {
        try {
            pipeline.loadWebpage(url);
            pipeline.calculateLayout(width);
        } catch (Exception e) {
            e.printStackTrace();
            pipeline.loadWebpage("file://res/html/error_page.html");
            pipeline.calculateLayout(width);
        }
        
        return pipeline;
    }

}
