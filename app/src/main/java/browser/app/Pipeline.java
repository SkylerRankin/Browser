package browser.app;

import javafx.scene.canvas.GraphicsContext;

import browser.css.CSSLoader;
import browser.css.DefaultColors;
import browser.css.FontLoader;
import browser.layout.BoxLayoutGenerator;
import browser.layout.BoxTreeGenerator;
import browser.layout.ListMarkerGenerator;
import browser.layout.TextDimensionCalculator;
import browser.model.BoxNode;
import browser.model.DOMNode;
import browser.model.RenderNode;
import browser.network.ResourceLoader;
import browser.parser.HTMLElements;
import browser.parser.HTMLParser;
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
    public void loadWebpage(String url) {
        resourceLoader.loadWebpage(url);
        domRoot = resourceLoader.getDOM();
        title = (new HTMLParser(null)).getTitle(domRoot);
        loaded = true;
    }

    /**
     * Step 2 in the pipeline. Calculates the size and position of each render nodes.
     * @param screenWidth        Width in pixels of the screen.
     */
    public void calculateLayout(float screenWidth) {
        RenderTreeGenerator renderTreeGenerator = new RenderTreeGenerator();
        rootRenderNode = renderTreeGenerator.generateRenderTree(domRoot);
        CSSLoader cssLoader = new CSSLoader(domRoot, renderTreeGenerator.getParentRenderNodeMap(), resourceLoader.getExternalCSS());
        cssLoader.applyAllCSS(rootRenderNode);

        // Insert list markers, propagate any CSS to them, and update their content.
        ListMarkerGenerator.addMarkers(rootRenderNode);
        cssLoader.applyAllCSS(rootRenderNode);
        ListMarkerGenerator.setMarkerStyles(rootRenderNode);

        BoxTreeGenerator boxTreeGenerator = new BoxTreeGenerator();
        rootBoxNode = boxTreeGenerator.generate(rootRenderNode);
        BoxLayoutGenerator boxLayoutGenerator = new BoxLayoutGenerator(textDimensionCalculator);
        boxLayoutGenerator.calculateLayout(rootBoxNode, screenWidth);

        height = rootBoxNode.height;
        width = screenWidth;
    }

    /**
     * Step 3 in the pipeline. Draws the render tree to a JavaFX canvas.
     * @param gc        An instance of GraphicsContext to render on.
     */
    public void render(GraphicsContext gc) {
        HTMLRenderer.setBackground(gc, rootRenderNode.style.backgroundColor, width, height);
        HTMLRenderer.render(gc, rootBoxNode);
    }

    public boolean loadedWebpage() {
        return loaded;
    }

}
