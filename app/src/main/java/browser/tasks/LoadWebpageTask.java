package browser.tasks;

import javafx.concurrent.Task;

import browser.app.ErrorPageHandler;
import browser.app.Pipeline;

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
            try {
                pipeline.loadWebpage(ErrorPageHandler.errorPagePath);
                pipeline.calculateLayout(width);
            } catch (Exception e2) {
                System.out.println("LoadWebpageTask: error running pipeline on error page.");
                e2.printStackTrace();
            }
            
        }
        
        return pipeline;
    }

}
