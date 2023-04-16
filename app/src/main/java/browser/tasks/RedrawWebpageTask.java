package browser.tasks;

import javafx.concurrent.Task;

import browser.app.ErrorPageHandler;
import browser.app.Pipeline;
import browser.constants.ErrorConstants;

public class RedrawWebpageTask extends Task<Pipeline> {
    
    private final Pipeline pipeline;
    private final float width;
    private final float height;
    
    public RedrawWebpageTask(float width, float height, Pipeline pipeline) {
        this.pipeline = pipeline;
        this.width = width;
        this.height = height;
    }

    @Override
    protected Pipeline call() {
        try {
            if (!pipeline.loadedWebpage()) {
                System.out.println("RedrawWebpageTask: attempted redraw before first draw");
                return pipeline;
            }
            synchronized (pipeline) {
                pipeline.calculateLayout(width, height);
            }
        } catch (Exception e) {
            ErrorPageHandler.previousException = e;
            System.out.println("LoadWebpageTask: error running pipeline on page: " + e.getLocalizedMessage());
            e.printStackTrace();
            try {
                synchronized (pipeline) {
                    pipeline.loadWebpage(ErrorConstants.ErrorPagePath);
                    pipeline.calculateLayout(width, height);
                }
            } catch (Exception e2) {
                System.out.println("LoadWebpageTask: error running pipeline on error page: " + e2.getLocalizedMessage());
                e2.printStackTrace();
            }
        }
        
        return pipeline;
    }

}
