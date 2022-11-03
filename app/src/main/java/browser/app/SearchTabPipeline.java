package browser.app;

import java.util.ArrayList;
import java.util.List;

import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tab;

import browser.app.ui.inspector.InspectorHandler;
import browser.interaction.InteractionHandler;
import browser.tasks.LoadWebpageTask;
import browser.tasks.RedrawWebpageTask;

public class SearchTabPipeline {
    
    private int tabID;
    private Pipeline pipeline;
    private Tab tab;
    private InteractionHandler interactionHandler;
    private float width;
    private Canvas canvas;
    private GraphicsContext gc;
    private final boolean debug = true;
    private List<RedrawWebpageTask> currentRedrawTasks;
    
    public SearchTabPipeline(int id, Canvas canvas, Tab tab, InteractionHandler interactionHandler) {
        tabID = id;
        pipeline = new Pipeline();
        this.canvas = canvas;
        this.width = (float) canvas.getWidth();
        this.gc = canvas.getGraphicsContext2D();
        this.tab = tab;
        this.interactionHandler = interactionHandler;
        currentRedrawTasks = new ArrayList<RedrawWebpageTask>();
    }
    
    public void updateScreenWidth(float width) {
        this.width = width;
    }
    
    public boolean loadedWebpage() {
        return pipeline.loadedWebpage();
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
                interactionHandler.setRootRenderNode(pipeline.getRootRenderNode());
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
    
    public void redrawWebpage() {
        if (debug) System.out.printf("SearchTabPipeline: Tab %d redrawing webpage webpage\n", tabID);
        RedrawWebpageTask crt = new RedrawWebpageTask(width, pipeline);
        for (RedrawWebpageTask task : currentRedrawTasks) {
            task.cancel();
        }
        currentRedrawTasks.clear();
        currentRedrawTasks.add(crt);
        crt.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                System.out.println("RedrawWebpageTask succeeded");
                canvas.setHeight(Math.max(pipeline.height, (float) gc.getCanvas().getHeight()));
                pipeline.render(gc);
                InspectorHandler.get().update(pipeline.getRootRenderNode());
                currentRedrawTasks.remove(crt);
            }
        });
        Thread thread = new Thread(crt);
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.err.printf("Uncaught exception: %s\n", e);
                currentRedrawTasks.remove(crt);
            }
        });
        thread.start();
    }

}
