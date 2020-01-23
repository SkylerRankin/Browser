package test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import css.DefaultCSSLoader;
import javafx.application.Application;
import javafx.scene.canvas.GraphicsContext;
import layout.BoxLayoutCalculator;
import model.RenderNode;
import parser.RenderTreeGenerator;
import renderer.HTMLRenderer;

public class HTMLRendererTest {
	
	private static Map<Integer, RenderNode> parentNodeMap;
	
	private static RenderNode createSimpleRenderTree() {
		
		RenderNode root = new RenderNode("body");
		RenderNode h1 = new RenderNode("h1");
		RenderNode div = new RenderNode("div");
		RenderNode h2 = new RenderNode("h2");
		RenderNode p1 = new RenderNode("p");
		RenderNode p2 = new RenderNode("p");
		
		parentNodeMap = new HashMap<Integer, RenderNode>();
		parentNodeMap.put(1, root);
		parentNodeMap.put(2, root);
		parentNodeMap.put(3, div);
		parentNodeMap.put(4, div);
		parentNodeMap.put(5, div);
		
		div.style.paddingTop = 5;
		div.style.paddingBottom = 5;
		div.style.paddingRight = 5;
		div.style.paddingLeft = 5;
		
		h2.style.marginTop = 5;
		p1.style.marginTop = 15;
		p1.style.marginBottom = 15;
		p2.style.marginTop = 15;
		p2.style.marginBottom = 5;
		
//		root.box.x = 0;		root.box.y = 0;		root.box.width = 200f;			root.box.height = 100f;
//		h1.box.x = 0;		h1.box.y = 0;		h1.box.width = 32.75f;			h1.box.height = 15.96f;
//		div.box.x = 0;		div.box.y = 15.96f;	h1.box.width = 196.44f;			h1.box.height = 15.96f;
//		p1.box.x = 0;		p1.box.y = 15.96f;	p1.box.width = 103.15f;			p1.box.height = 15.96f;
//		p2.box.x = 0;		p2.box.y = 31.92f;	p2.box.width = 196.44f;			p2.box.height = 15.96f;
		
		root.id = 0;		root.depth = 0;
		h1.id = 1;			h1.depth = 1;
		div.id = 2;			div.depth = 1;
		h2.id = 3;			h2.depth = 2;
		p1.id = 4;			p1.depth = 2;
		p2.id = 5;			p2.depth = 2;
		
		h1.text = "A Title";
		h2.text = "A subtitle.";
		p1.text = "The second, much longer, paragraph.";
		p2.text = "Some other text, later on, second paragraph.";
		
		root.children.add(h1);
		root.children.add(div);
		div.children.add(h2);
		div.children.add(p1);
		div.children.add(p2);
		
		return root;
	}
	
	private static RenderNode createTree4() {
		// Two nested divs with padding, and some text in the middle
		// Outer div has fixed size, inner doesn't
		
		parentNodeMap = new HashMap<Integer, RenderNode>();
		
		RenderNode root = new RenderNode("body");
		RenderNode A = new RenderNode("div");
		RenderNode B = new RenderNode("div");
		RenderNode C = new RenderNode("text");
		
		C.text = "some text";
		
		A.box.fixedWidth = true;
		A.box.fixedHeight = true;
		
		A.box.width = 100;
		A.box.height = 50;
		
		A.style.paddingTop = 5;
		A.style.paddingBottom = 5;
		A.style.paddingLeft = 10;
		A.style.paddingRight = 10;
		
		B.style.paddingTop = 5;
		B.style.paddingBottom = 5;
		B.style.paddingLeft = 5;
		B.style.paddingRight = 5;
		
		root.id = 0;	root.depth = 0;
		A.id = 1;		A.depth = 1;
		B.id = 2;		B.depth = 2;
		C.id = 3;		C.depth = 3;
		
		root.children.add(A);
		A.children.add(B);
		B.children.add(C);
		
		parentNodeMap.put(1, root);
		parentNodeMap.put(2, A);
		parentNodeMap.put(3, B);
		return root;
	}
	
	@Test
	public void test() {
		String[] args = new String[] {};
		Application.launch(RenderTestCanvas.class, args);
	}
	
	public static void render(GraphicsContext gc, double width, double height) {
		RenderNode root = createSimpleRenderTree();
//		RenderNode root = createTree4();
		DefaultCSSLoader.loadDefaults(root);
		BoxLayoutCalculator blc = new BoxLayoutCalculator(parentNodeMap, 500f);
		blc.setBoxBounds(root);
		blc.propagateMaxSizes(root);
		blc.calculateBoxes(root);
		blc.printBoxes(root);
		HTMLRenderer.render(gc, root);
		
	}

}
