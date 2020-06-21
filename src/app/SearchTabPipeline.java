package app;

import app.ui.inspector.InspectorHandler;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tab;
import model.RenderNode;
import tasks.LoadWebpageTask;

public class SearchTabPipeline {
    
    private int tabID;
    private Pipeline pipeline;
    private Tab tab;
    private float width;
    private Canvas canvas;
    private GraphicsContext gc;
    private final boolean debug = true;
    
    public SearchTabPipeline(int id, Canvas canvas, Tab tab) {
        tabID = id;
        pipeline = new Pipeline();
        this.canvas = canvas;
        this.width = (float) canvas.getWidth();
        this.gc = canvas.getGraphicsContext2D();
        this.tab = tab;
    }
    
    public void updateScreenWidth(float width) {
        this.width = width;
    }
    
    public void loadWebpage(String url) {
        if (debug) System.out.printf("SearchTabPipeline: Tab %d loading webpage, %s\n", tabID, url);
        LoadWebpageTask lwt = new LoadWebpageTask(url, width, pipeline);
        lwt.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                System.out.println("LoadWebpageTask succeeded");
                canvas.setHeight(Math.max(pipeline.height, (float) gc.getCanvas().getHeight()));
                pipeline.render(gc);
                tab.setText(pipeline.title == null ? url : pipeline.title);
                InspectorHandler.get().update(pipeline.getRootRenderNode());
            }
        });
        Thread thread = new Thread(lwt);
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.err.printf("Uncaught exception: %s\n", e);
            }
        });
        thread.start();
    }

}
