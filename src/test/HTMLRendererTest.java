package test;

import java.util.HashMap;

import org.junit.Test;
import javafx.application.Application;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import layout.BoxLayoutCalculator;
import model.RenderNode;
import renderer.HTMLRenderer;

public class HTMLRendererTest {
	
	private static RenderNode createSimpleRenderTree() {
				
		RenderNode root = new RenderNode("body");
		RenderNode h1 = new RenderNode("h1");
		RenderNode div = new RenderNode("div");
		RenderNode p1 = new RenderNode("p");
		RenderNode p2 = new RenderNode("p");
		
		root.box.x = 0;		root.box.y = 0;		root.box.width = 200f;		root.box.height = 100f;
		h1.box.x = 0;		h1.box.y = 0;		h1.box.width = 32.75f;			h1.box.height = 15.96f;
		div.box.x = 0;		div.box.y = 15.96f;	h1.box.width = 196.44f;			h1.box.height = 15.96f;
		p1.box.x = 0;		p1.box.y = 15.96f;	p1.box.width = 103.15f;			p1.box.height = 15.96f;
		p2.box.x = 0;		p2.box.y = 31.92f;	p2.box.width = 196.44f;			p2.box.height = 15.96f;
		
		root.id = 0;	root.depth = 0;
		h1.id = 1;		h1.depth = 1;
		div.id = 2;		div.depth = 1;
		p1.id = 3;		p1.depth = 2;
		p2.id = 4;		p2.depth = 2;
		
		h1.text = "A Test";
		p1.text = "The first paragraph.";
		p2.text = "The second, much longer, paragraph.";
		
		root.children.add(h1);
		root.children.add(div);
		div.children.add(p1);
		div.children.add(p2);
		
		return root;
	}
	
	@Test
	public void test() {
		String[] args = new String[] {};
		Application.launch(RenderTestCanvas.class, args);
	}
	
	public static void render(GraphicsContext gc, double width, double height) {
		RenderNode root = createSimpleRenderTree();
		BoxLayoutCalculator blc = new BoxLayoutCalculator(null, 0f);
		blc.printBoxes(root);
		HTMLRenderer.render(gc, root);
		
	}

}
