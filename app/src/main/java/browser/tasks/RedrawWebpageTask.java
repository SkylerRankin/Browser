package browser.tasks;

import javafx.concurrent.Task;

import browser.app.ErrorPageHandler;
import browser.app.Pipeline;


public class RedrawWebpageTask extends Task<Pipeline> {
    
    private final Pipeline pipeline;
    private float width;
    
    public RedrawWebpageTask(float width, Pipeline pipeline) {
        this.pipeline = pipeline;
        this.width = width;
    }

    @Override
    protected Pipeline call() throws Exception {
        try {
            if (!pipeline.loadedWebpage()) {
                System.out.println("RedrawWebpageTask: attempted redraw before first draw");
                return pipeline;
            }
            synchronized (pipeline) {
                pipeline.calculateLayout(width);
            }
        } catch (Exception e) {
            System.out.println("LoadWebpageTask: error running pipeline on page: " + e.getLocalizedMessage());
            e.printStackTrace();
            try {
                synchronized (pipeline) {
                    pipeline.loadWebpage(ErrorPageHandler.errorPagePath);
                    pipeline.calculateLayout(width);
                }
            } catch (Exception e2) {
                System.out.println("LoadWebpageTask: error running pipeline on error page: " + e2.getLocalizedMessage());
                e2.printStackTrace();
            }
        }
        
        return pipeline;
    }

}
