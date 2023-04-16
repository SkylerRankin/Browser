package browser.tasks;

import javafx.concurrent.Task;

import browser.app.ErrorPageHandler;
import browser.app.Pipeline;
import browser.constants.ErrorConstants;

public class LoadWebpageTask extends Task<Pipeline> {
    
    private final String url;
    private final Pipeline pipeline;
    private final float width;
    private final float height;
    
    public LoadWebpageTask(String url, float width, float height, Pipeline pipeline) {
        this.url = url;
        this.pipeline = pipeline;
        this.width = width;
        this.height = height;
    }

    @Override
    protected Pipeline call() {
        try {
            synchronized (pipeline) {
                pipeline.loadWebpage(url);
                pipeline.calculateLayout(width, height);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorPageHandler.previousException = e;
            try {
                pipeline.loadWebpage(ErrorConstants.ErrorPagePath);
                pipeline.calculateLayout(width, height);
            } catch (Exception e2) {
                System.out.println("LoadWebpageTask: error running pipeline on error page.");
                e2.printStackTrace();
            }
            
        }
        
        return pipeline;
    }

}
