package test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import css.DefaultCSSLoader;
import model.RenderNode;

public class DefaultCSSLoaderTest {
	
	@Test
	public void test() {
		RenderNode root = new RenderNode("h1");
		DefaultCSSLoader.loadDefaults(root);
		assertEquals(100, root.style.fontSize);
	}
	
	@Test
	public void IDTest() {
		RenderNode root = new RenderNode("body");
		RenderNode div1 = new RenderNode("div");
		RenderNode h1 = new RenderNode("h1");
		RenderNode div2 = new RenderNode("div");
		RenderNode p = new RenderNode("p");
		
		root.children.add(div1);
		div1.children.add(h1);
		div1.children.add(div2);
		div2.children.add(p);

	}

}
