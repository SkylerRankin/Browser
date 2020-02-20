package app;

import css.CSSLoader;
import css.DefaultColors;
import javafx.scene.canvas.GraphicsContext;
import layout.BoxLayoutCalculator;
import model.DOMNode;
import model.RenderNode;
import network.HTTPClient;
import network.ResourceLoader;
import parser.HTMLElements;
import parser.HTMLParser;
import parser.RenderTreeGenerator;
import renderer.HTMLRenderer;
import renderer.ImageCache;

public class Pipeline {
	
	private String url;
	private ResourceLoader resourceLoader;
	private DOMNode domRoot;
	private RenderNode renderRoot;
	
	public static void init() {
		DefaultColors.init();
		ImageCache.loadDefaultImages();
		HTMLElements.init();
	}
	
	/**
	 * Step 1 in the pipeline. Downloads and parses the HTML.
	 * @param url		URL to visit.
	 */
	public void loadWebpage(String url) {
	    resourceLoader = new ResourceLoader();
	    resourceLoader.loadWebpage(url);
		domRoot = resourceLoader.getDOM();
	}
	
	/**
	 * Step 2 in the pipeline. Calculates sizes and positions all render nodes.
	 * @param screenWidth		Width in pixels of the screen.
	 */
	public void calculateLayout(float screenWidth) {
		RenderTreeGenerator rtg = new RenderTreeGenerator();
		renderRoot = rtg.generateRenderTree(domRoot, screenWidth);
//		renderRoot.print();
		rtg.cleanUpText(renderRoot, false);
		CSSLoader cssLoader = new CSSLoader(domRoot, rtg.getParentRenderNodeMap(), resourceLoader.getExternalCSS());
		cssLoader.applyAllCSS(renderRoot);
		BoxLayoutCalculator blc = new BoxLayoutCalculator(rtg.getParentRenderNodeMap(), screenWidth);
		rtg.transformNode(renderRoot);
		blc.setBoxBounds(renderRoot);
		blc.propagateMaxSizes(renderRoot);
		blc.finalizeDimensions(renderRoot);
		blc.calculateBoxes(renderRoot);
		blc.applyJustification(renderRoot);
	    blc.printBoxes(renderRoot);
//		rtg.splitLongText(renderRoot);
	}
	
	/**
	 * Step 3 in the pipeline. Draws the render tree to a JavaFX canvas.
	 * @param gc		An instance of GraphicsContext to render on.
	 */
	public void render(GraphicsContext gc) {
		HTMLRenderer.render(gc, renderRoot);
	}

}
