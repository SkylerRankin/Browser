package browser.app.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import browser.app.Pipeline;
import browser.interaction.InteractionHandler;
import browser.tasks.LoadWebpageTask;
import browser.tasks.RedrawWebpageTask;
import browser.tasks.RenderCompleteCallback;

public class CanvasRenderer {

    private final Pipeline pipeline;
    private final Canvas canvas;
    private final GraphicsContext graphicsContext;
    private final InteractionHandler interactionHandler;
    private final RenderCompleteCallback renderCompleteCallback;
    private final double heightOffset;

    private float width;
    private RedrawWebpageTask redrawWebpageTask;

    public CanvasRenderer(Canvas canvas, InteractionHandler interactionHandler, RenderCompleteCallback renderCompleteCallback, double heightOffset) {
        pipeline = new Pipeline();
        this.canvas = canvas;
        this.width = (float) canvas.getWidth();
        this.graphicsContext = canvas.getGraphicsContext2D();
        this.interactionHandler = interactionHandler;
        this.renderCompleteCallback = renderCompleteCallback;
        this.heightOffset = heightOffset;
    }

    public void updateScreenWidth(float width) {
        this.width = width;
    }

    public float getRenderedHeight() {
        return pipeline.getHeight();
    }

    public void refresh() {
        if (pipeline.loadedWebpage()) {
            redrawWebpage();
        }
    }

    public void renderPage(String url) {
        LoadWebpageTask lwt = new LoadWebpageTask(url, width, pipeline);
        lwt.setOnSucceeded(event -> {
            canvas.setHeight(Math.max(pipeline.getHeight(), canvas.getScene().getHeight() - heightOffset));
//            tab.setText(pipeline.getTitle() == null ? url : pipeline.getTitle());
            synchronized (pipeline) {
                pipeline.render(graphicsContext);
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
        if (redrawWebpageTask != null) {
            redrawWebpageTask.cancel(true);
        }
        redrawWebpageTask = new RedrawWebpageTask(width, pipeline);
        redrawWebpageTask.setOnSucceeded(event -> {
            canvas.setHeight(Math.max(pipeline.getHeight(), canvas.getScene().getHeight() - heightOffset));
            synchronized (pipeline) {
                pipeline.render(graphicsContext);
            }
            renderCompleteCallback.onRenderCompleted(pipeline.getRootRenderNode(), renderType);
            interactionHandler.setRootBoxNode(pipeline.getRootBoxNode());
            redrawWebpageTask = null;
        });
        Thread thread = new Thread(redrawWebpageTask);
        thread.setUncaughtExceptionHandler((t, e) -> {
            System.err.println("SearchTabPipeline:redrawWebpage() thread uncaught exception handler");
            e.printStackTrace();
            redrawWebpageTask = null;
        });
        thread.start();
    }

}
