package browser.app;

import javafx.scene.canvas.GraphicsContext;

import browser.css.CSSLoader;
import browser.css.DefaultColors;
import browser.css.FontLoader;
import browser.layout.BoxLayoutCalculator;
import browser.model.DOMNode;
import browser.model.RenderNode;
import browser.network.ResourceLoader;
import browser.parser.HTMLElements;
import browser.parser.HTMLParser;
import browser.parser.RenderTreeGenerator;
import browser.parser.SpecialSymbolHandler;
import browser.renderer.HTMLRenderer;
import browser.renderer.ImageCache;

public class Pipeline {

    private static boolean initialized = false;

    private final ResourceLoader resourceLoader;
    private DOMNode domRoot;
    private RenderNode renderRoot;
    private boolean loaded;

    public String title;
    public float width;
    public float height;


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
        RenderTreeGenerator rtg = new RenderTreeGenerator();
        renderRoot = rtg.generateRenderTree(domRoot, screenWidth);
        CSSLoader cssLoader = new CSSLoader(domRoot, rtg.getParentRenderNodeMap(), resourceLoader.getExternalCSS());
        cssLoader.applyAllCSS(renderRoot);

        // Run text cleanup after CSS properties have been set.
        rtg.cleanupRenderNodeText(renderRoot);

        BoxLayoutCalculator blc = new BoxLayoutCalculator(rtg.getParentRenderNodeMap(), screenWidth);

        rtg.transformNode(renderRoot);

        blc.setBoxBounds(renderRoot);
        blc.propagateMaxSizes(renderRoot);
        blc.finalizeDimensions(renderRoot);
        blc.setTableCellWidths(renderRoot);
        blc.calculateBoxes(renderRoot);
        blc.applyJustification(renderRoot);
        height = renderRoot.box.height;
        width = screenWidth;
    }

    /**
     * Step 3 in the pipeline. Draws the render tree to a JavaFX canvas.
     * @param gc        An instance of GraphicsContext to render on.
     */
    public void render(GraphicsContext gc) {
        HTMLRenderer.setBackground(gc, renderRoot.style.backgroundColor, width, height);
        HTMLRenderer.render(gc, renderRoot);
    }

    // Expose the render tree for the inspector
    public RenderNode getRootRenderNode() {
        return renderRoot;
    }

    // Expose the DOM root for testing.
    public void setDomRoot(DOMNode domRoot) {
        this.domRoot = domRoot;
    }

    public boolean loadedWebpage() {
        return loaded;
    }

}
