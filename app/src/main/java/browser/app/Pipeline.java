package browser.app;

import javafx.scene.canvas.GraphicsContext;

import browser.css.CSSLoader;
import browser.css.DefaultColors;
import browser.css.FontLoader;
import browser.exception.LayoutException;
import browser.exception.PageLoadException;
import browser.layout.BoxLayoutGenerator;
import browser.layout.BoxTreeGenerator;
import browser.layout.ListMarkerGenerator;
import browser.layout.TextDimensionCalculator;
import browser.model.BoxNode;
import browser.model.DOMNode;
import browser.model.RenderNode;
import browser.network.ResourceLoader;
import browser.parser.HTMLElements;
import browser.parser.RenderTreeGenerator;
import browser.parser.SpecialSymbolHandler;
import browser.renderer.HTMLRenderer;
import browser.renderer.ImageCache;

import lombok.Getter;
import lombok.Setter;

public class Pipeline {

    private static boolean initialized = false;

    private final ResourceLoader resourceLoader;
    private final TextDimensionCalculator textDimensionCalculator;
    @Getter
    @Setter
    private DOMNode domRoot;
    @Getter
    private RenderNode rootRenderNode;
    @Getter
    private BoxNode rootBoxNode;
    private boolean loaded;

    @Getter
    private String title;
    private float width;
    @Getter
    private float height;

    public static void init() {
        if (initialized) {
            return;
        }

        initialized = true;
        DefaultColors.init();
        ImageCache.loadDefaultImages();
        HTMLElements.init();
        FontLoader.init();
        ErrorPageHandler.init();
        SpecialSymbolHandler.init();
    }

    public Pipeline() {
        resourceLoader = new ResourceLoader();
        textDimensionCalculator = new TextDimensionCalculator();
    }

    /**
     * Step 1 in the pipeline. Downloads and parses the HTML.
     * @param url        URL to visit.
     */
    public void loadWebpage(String url) throws PageLoadException {
        resourceLoader.loadWebpage(url);
        domRoot = resourceLoader.getDom();
        title = "no title";
        loaded = true;
    }

    /**
     * Step 2 in the pipeline. Calculates the size and position of each render nodes.
     * @param screenWidth        Width in pixels of the screen.
     */
    public void calculateLayout(float screenWidth, float screenHeight) throws LayoutException {
        try {
            RenderTreeGenerator renderTreeGenerator = new RenderTreeGenerator();
            rootRenderNode = renderTreeGenerator.generateRenderTree(domRoot);
            CSSLoader cssLoader = new CSSLoader(domRoot, resourceLoader.getExternalCSS(), screenWidth, screenHeight);
            cssLoader.applyAllCSS(rootRenderNode);

            renderTreeGenerator.removeDisplayNoneNodes(rootRenderNode);
            renderTreeGenerator.cleanupRenderNodeText(rootRenderNode);

            // Insert list markers, propagate any CSS to them, and update their content.
            ListMarkerGenerator.addMarkers(rootRenderNode);
            // TODO apply styles to marker nodes.
            ListMarkerGenerator.setMarkerStyles(rootRenderNode);

            BoxTreeGenerator boxTreeGenerator = new BoxTreeGenerator();
            rootBoxNode = boxTreeGenerator.generate(rootRenderNode);
            BoxLayoutGenerator boxLayoutGenerator = new BoxLayoutGenerator(textDimensionCalculator);
            boxLayoutGenerator.calculateLayout(rootBoxNode, screenWidth);

            height = rootBoxNode.height;
            width = screenWidth;
        } catch (Exception e) {
            throw new LayoutException(e);
        }
    }

    /**
     * Step 3 in the pipeline. Draws the render tree to a JavaFX canvas.
     * @param gc        An instance of GraphicsContext to render on.
     */
    public void render(GraphicsContext gc) {
        HTMLRenderer.setBackground(gc, rootRenderNode.style.backgroundColor);
        HTMLRenderer.render(gc, rootBoxNode);
    }

    public boolean loadedWebpage() {
        return loaded;
    }

}
