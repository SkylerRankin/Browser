package app;

import css.CSSLoader;
import css.DefaultColors;
import javafx.scene.canvas.GraphicsContext;
import layout.BoxLayoutCalculator;
import model.DOMNode;
import model.RenderNode;
import network.HTTPClient;
import parser.HTMLParser;
import parser.RenderTreeGenerator;
import renderer.HTMLRenderer;
import renderer.ImageCache;

public class Pipeline {
	
	private String url;
	private DOMNode domRoot;
	private RenderNode renderRoot;
	
	public static void init() {
		DefaultColors.init();
		ImageCache.loadDefaultImages();
	}
	
	/**
	 * Step 1 in the pipeline. Downloads and parses the HTML.
	 * @param url		URL to visit.
	 */
	public void loadWebpage(String url) {
		String html = HTTPClient.requestPage(url);
		HTMLParser parser = new HTMLParser();
		domRoot = parser.generateDOMTree(html);
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
		CSSLoader cssLoader = new CSSLoader(rtg.getParentRenderNodeMap());
		cssLoader.applyAllCSS(renderRoot);
		BoxLayoutCalculator blc = new BoxLayoutCalculator(rtg.getParentRenderNodeMap(), screenWidth);
		rtg.transformNode(renderRoot);
		blc.setBoxBounds(renderRoot);
		blc.propagateMaxSizes(renderRoot);
		blc.finalizeDimensions(renderRoot);
//		blc.printBoxes(renderRoot);
		blc.calculateBoxes(renderRoot);
		blc.applyJustification(renderRoot);
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
