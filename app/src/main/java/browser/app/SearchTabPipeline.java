package browser.app;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tab;

import browser.interaction.InteractionHandler;
import browser.model.RenderNode;
import browser.tasks.LoadWebpageTask;
import browser.tasks.RedrawWebpageTask;
import browser.tasks.RenderCompleteCallback;

public class SearchTabPipeline {
    
    private int tabID;
    private final Pipeline pipeline;
    private final Tab tab;
    private final InteractionHandler interactionHandler;
    private final RenderCompleteCallback renderCompleteCallback;
    private float width;
    private final Canvas canvas;
    private final GraphicsContext gc;
    private RedrawWebpageTask currentRedrawTask;

    public SearchTabPipeline(int id, Canvas canvas, Tab tab, InteractionHandler interactionHandler, RenderCompleteCallback renderCompleteCallback) {
        tabID = id;
        pipeline = new Pipeline();
        this.canvas = canvas;
        this.width = (float) canvas.getWidth();
        this.gc = canvas.getGraphicsContext2D();
        this.tab = tab;
        this.interactionHandler = interactionHandler;
        this.renderCompleteCallback = renderCompleteCallback;
    }
    
    public void updateScreenWidth(float width) {
        this.width = width;
    }
    
    public boolean loadedWebpage() {
        return pipeline.loadedWebpage();
    }
    
    public void loadWebpage(String url) {
        LoadWebpageTask lwt = new LoadWebpageTask(url, width, pipeline);
        lwt.setOnSucceeded(event -> {
            System.out.printf("Setting canvas height to %.2f\n", Math.max(pipeline.getHeight(), (float) gc.getCanvas().getHeight()));
            canvas.setHeight(Math.max(pipeline.getHeight(), (float) gc.getCanvas().getHeight()));
            tab.setText(pipeline.getTitle() == null ? url : pipeline.getTitle());
            synchronized (pipeline) {
                pipeline.render(gc);
            }
            renderCompleteCallback.onRenderCompleted(pipeline.getRootRenderNode(), RenderCompleteCallback.RenderType.NewLayout);
            interactionHandler.setRootBoxNode(pipeline.getRootBoxNode());
        });
        Thread thread = new Thread(lwt);
        thread.setUncaughtExceptionHandler((t, e) -> System.err.printf("Uncaught exception: %s\n", e));
        thread.start();
    }

    public void redrawWebpage() {
        redrawWebpage(RenderCompleteCallback.RenderType.NewLayout);
    }
    
    public void redrawWebpage(RenderCompleteCallback.RenderType renderType) {
        RedrawWebpageTask crt = new RedrawWebpageTask(width, pipeline);
        if (currentRedrawTask != null) {
            currentRedrawTask.cancel(true);
        }
        currentRedrawTask = crt;
        crt.setOnSucceeded(event -> {
            canvas.setHeight(Math.max(pipeline.getHeight(), (float) gc.getCanvas().getHeight()));
            synchronized (pipeline) {
                pipeline.render(gc);
            }
            renderCompleteCallback.onRenderCompleted(pipeline.getRootRenderNode(), renderType);
            interactionHandler.setRootBoxNode(pipeline.getRootBoxNode());
            currentRedrawTask = null;
        });
        Thread thread = new Thread(crt);
        thread.setUncaughtExceptionHandler((t, e) -> {
            System.err.println("SearchTabPipeline:redrawWebpage() thread uncaught exception handler");
            e.printStackTrace();
            currentRedrawTask = null;
        });
        thread.start();
    }

    public RenderNode getRootRenderNode() {
        return pipeline.getRootRenderNode();
    }

}
