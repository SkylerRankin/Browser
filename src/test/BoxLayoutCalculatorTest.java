package test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import css.CSSStyle;
import javafx.embed.swing.JFXPanel;
import layout.BoxLayoutCalculator;
import model.RenderNode;

public class BoxLayoutCalculatorTest {
	
	private RenderNode root;
	private Map<Integer, RenderNode> parentMap;
	
	@Before
	public void before() {
		JFXPanel jfxPanel = new JFXPanel();
	}
	
	private void createSimpleRenderTree() {
		parentMap = new HashMap<Integer, RenderNode>();
		
		root = new RenderNode("body");
		RenderNode h1 = new RenderNode("h1");
		RenderNode div = new RenderNode("div");
		RenderNode p1 = new RenderNode("p");
		RenderNode p2 = new RenderNode("p");
		
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
		
		parentMap.put(1, root);
		parentMap.put(2, root);
		parentMap.put(3, div);
		parentMap.put(4, div);
	}
	
	private void createRenderTreeWithInline() {
		parentMap = new HashMap<Integer, RenderNode>();
		
		root = new RenderNode("body");
		RenderNode h1 = new RenderNode("h1");
		RenderNode div = new RenderNode("div");
		RenderNode span1 = new RenderNode("span");
		RenderNode span2 = new RenderNode("span");
		RenderNode p = new RenderNode("p");
		RenderNode span3 = new RenderNode("span");
		
		span1.style.diplay = CSSStyle.displayType.INLINE;
		span2.style.diplay = CSSStyle.displayType.INLINE;
		span3.style.diplay = CSSStyle.displayType.INLINE;

		
		root.id = 0;	root.depth = 0;
		h1.id = 1;		h1.depth = 1;
		div.id = 2;		div.depth = 1;
		span1.id = 3;	span1.depth = 2;
		span2.id = 4;	span2.depth = 2;
		p.id = 5;		p.depth = 2;
		span3.id = 6;	span3.depth = 2;

		
		h1.text = "A Test";
		span1.text = "Search";
		span2.text = "Cancel";
		p.text = "A paragraph of information for testing.";
		span3.text = "Author Unknown";
		
		root.children.add(h1);
		root.children.add(div);
		div.children.add(span1);
		div.children.add(span2);
		div.children.add(p);
		div.children.add(span3);
		
		parentMap.put(1, root);
		parentMap.put(2, root);
		parentMap.put(3, div);
		parentMap.put(4, div);
		parentMap.put(5, div);
		parentMap.put(6, div);

	}

	private void createTree2() {
		
		// 5 in-line spans in a row; should show two per line, since each is 40 wide and total width is set to 100
		
		parentMap = new HashMap<Integer, RenderNode>();
		
		root = new RenderNode("body");
		RenderNode span1 = new RenderNode("span");
		RenderNode span2 = new RenderNode("span");
		RenderNode span3 = new RenderNode("span");
		RenderNode span4 = new RenderNode("span");
		RenderNode span5 = new RenderNode("span");
		
		span1.style.diplay = CSSStyle.displayType.INLINE;
		span2.style.diplay = CSSStyle.displayType.INLINE;
		span3.style.diplay = CSSStyle.displayType.INLINE;
		span4.style.diplay = CSSStyle.displayType.INLINE;
		span5.style.diplay = CSSStyle.displayType.INLINE;
		
		span1.box.fixedWidth = true;
		span2.box.fixedWidth = true;
		span3.box.fixedWidth = true;
		span4.box.fixedWidth = true;
		span5.box.fixedWidth = true;
		
		span1.box.fixedHeight = true;
		span2.box.fixedHeight = true;
		span3.box.fixedHeight = true;
		span4.box.fixedHeight = true;
		span5.box.fixedHeight = true;
		
		span1.box.width = 40;
		span1.box.height = 10;
		span2.box.width = 40;
		span2.box.height = 10;
		span3.box.width = 40;
		span3.box.height = 10;
		span4.box.width = 40;
		span4.box.height = 10;
		span5.box.width = 40;
		span5.box.height = 10;
		
		root.id = 0;	root.depth = 0;
		span1.id = 1;	span1.depth = 1;
		span2.id = 2;	span2.depth = 1;
		span3.id = 3;	span3.depth = 1;
		span4.id = 4;	span3.depth = 1;
		span5.id = 5;	span3.depth = 1;

		
		root.children.add(span1);
		root.children.add(span2);
		root.children.add(span3);
		root.children.add(span4);
		root.children.add(span5);
		
		parentMap.put(1, root);
		parentMap.put(2, root);
		parentMap.put(3, root);
		parentMap.put(4, root);
		parentMap.put(5, root);
	}
	
	private void createTree3() {		
		parentMap = new HashMap<Integer, RenderNode>();
		
		root = new RenderNode("body");
		RenderNode A = new RenderNode("div");
		RenderNode B = new RenderNode("div");
		RenderNode C = new RenderNode("div");
		RenderNode D = new RenderNode("div");
		RenderNode E = new RenderNode("div");
		RenderNode F = new RenderNode("div");
		
		D.style.diplay = CSSStyle.displayType.INLINE;
		E.style.diplay = CSSStyle.displayType.INLINE;
		F.style.diplay = CSSStyle.displayType.INLINE;
		
		A.box.fixedWidth = true;
		B.box.fixedWidth = true;
		D.box.fixedWidth = true;
		E.box.fixedWidth = true;
		F.box.fixedWidth = true;
		
		A.box.fixedHeight = true;
		D.box.fixedHeight = true;
		E.box.fixedHeight = true;
		F.box.fixedHeight = true;
		
		A.box.width = 50;
		A.box.height = 50;
		B.box.width = 90;
		D.box.width = 31;
		D.box.height = 5;
		E.box.width = 31;
		E.box.height = 5;
		F.box.width = 31;
		F.box.height = 5;
		
		root.id = 0;	root.depth = 0;
		A.id = 1;	A.depth = 1;
		B.id = 2;	B.depth = 1;
		C.id = 3;	C.depth = 2;
		D.id = 4;	D.depth = 3;
		E.id = 5;	E.depth = 3;
		F.id = 6;	F.depth = 3;
		
		root.children.add(A);
		root.children.add(B);
		B.children.add(C);
		C.children.add(D);
		C.children.add(E);
		C.children.add(F);
		
		parentMap.put(1, root);
		parentMap.put(2, root);
		parentMap.put(3, B);
		parentMap.put(4, C);
		parentMap.put(5, C);
		parentMap.put(6, C);
	}
	
	private void createRenderTree_widths_and_inline() {
		parentMap = new HashMap<Integer, RenderNode>();
		
		root = new RenderNode("body");
		RenderNode A = new RenderNode("div");
		RenderNode B = new RenderNode("div");
		RenderNode C = new RenderNode("div");
		RenderNode D = new RenderNode("div");
		RenderNode E = new RenderNode("div");
		RenderNode F = new RenderNode("div");
		RenderNode G = new RenderNode("div");
		RenderNode H = new RenderNode("div");
		RenderNode I = new RenderNode("div");
		RenderNode J = new RenderNode("div");

		A.box.fixedWidth = true;
		A.box.fixedHeight = true;
		A.box.width = 50;
		A.box.height = 20;
		
		B.box.fixedWidth = true;
		B.box.fixedHeight = true;
		B.box.width = 40;
		B.box.height = 10;
		
		D.box.fixedWidth = true;
		D.box.fixedHeight = true;
		D.box.width = 40;
		D.box.height = 5;
		
		E.box.fixedWidth = true;
		E.box.fixedHeight = true;
		E.box.width = 40;
		E.box.height = 5;
		
		F.box.fixedWidth = true;
		F.box.fixedHeight = true;
		F.box.width = 40;
		F.box.height = 5;
		
		H.box.fixedWidth = true;
		H.box.fixedHeight = true;
		H.box.width = 10;
		H.box.height = 40;
		
		I.box.fixedWidth = true;
		I.box.fixedHeight = true;
		I.box.width = 30;
		I.box.height = 10;
		
		J.box.fixedWidth = true;
		J.box.fixedHeight = true;
		J.box.width = 30;
		J.box.height = 10;
		
		
		B.style.diplay = CSSStyle.displayType.INLINE;
		D.style.diplay = CSSStyle.displayType.INLINE;
		E.style.diplay = CSSStyle.displayType.INLINE;
		F.style.diplay = CSSStyle.displayType.INLINE;
		J.style.diplay = CSSStyle.displayType.INLINE;

		
		root.id = 0;	root.depth = 0;
		A.id = 1;		A.depth = 1;
		B.id = 2;		B.depth = 1;
		C.id = 3;		C.depth = 1;
		D.id = 4;		D.depth = 2;
		E.id = 5;		E.depth = 2;
		F.id = 6;		F.depth = 2;
		G.id = 7;		G.depth = 2;
		H.id = 8;		H.depth = 3;
		I.id = 9;		I.depth = 3;
		J.id = 10;		J.depth = 3;
		
		root.children.add(A);
		root.children.add(B);
		root.children.add(C);
		C.children.add(D);
		C.children.add(E);		
		C.children.add(F);		
		C.children.add(G);
		G.children.add(H);
		G.children.add(I);
		G.children.add(J);
		
		parentMap.put(1, root);
		parentMap.put(2, root);
		parentMap.put(3, root);
		parentMap.put(4, C);
		parentMap.put(5, C);
		parentMap.put(6, C);
		parentMap.put(7, C);
		parentMap.put(8, G);
		parentMap.put(9, G);
		parentMap.put(10, G);
		
	}
	
//	@Test
	public void setBoxBoundsTest_simple() {
		createSimpleRenderTree();
		BoxLayoutCalculator blc = new BoxLayoutCalculator(parentMap, 50f);
		blc.setBoxBounds(root);
		blc.printBoxes(root);
	}
	
//	@Test
	public void calculateBoxesTest_simple() {
		createSimpleRenderTree();
		BoxLayoutCalculator blc = new BoxLayoutCalculator(parentMap, 50f);
		blc.setBoxBounds(root);
		blc.printBoxes(root);
		blc.calculateBoxes(root);
		blc.printBoxes(root);
	}
	
//	@Test
	public void calculateBoxesText_inline() {
		createRenderTreeWithInline();
		BoxLayoutCalculator blc = new BoxLayoutCalculator(parentMap, 50f);
		blc.setBoxBounds(root);
		blc.printBoxes(root);
		blc.calculateBoxes(root);
		blc.printBoxes(root);
	}
	
//	@Test
	public void calculateBoxesText_inline2() {
		createRenderTree_widths_and_inline();
		BoxLayoutCalculator blc = new BoxLayoutCalculator(parentMap, 50f);
		blc.printBoxes(root);
		blc.calculateBoxes(root);
		blc.printBoxes(root);
		
		Float[] x = {0f, 50f, 0f, 0f, 40f, 0f, 0f, 0f, 0f, 30f};
		Float[] y = {0f, 0f, 20f, 20f, 20f, 25f, 30f, 30f, 70f, 70f};
		Float[] width = {100f, 40f, 80f, 40f, 40f, 40f, 60f, 10f, 30f, 30f};
		Float[] height = {80f, 10f, 60f, 5f, 5f, 5f, 50f, 40f, 10f, 10f};
		
		for (int i = 0; i < 11; ++i) {
			RenderNode node = findRenderNode(i, root);
			assertNotNull(node);
			assertEquals(x[i], (Float) node.box.x);
			assertEquals(y[i], (Float) node.box.y);
			assertEquals(width[i], (Float) node.box.width);
			assertEquals(height[i], (Float) node.box.height);
		}
		
	}
	
	@Test
	public void calculateBoxesTestTree2() {
		createTree2();
		BoxLayoutCalculator blc = new BoxLayoutCalculator(parentMap, 100f);
		blc.setBoxBounds(root);
		blc.propagateMaxSizes(root);
		blc.calculateBoxes(root);
		
		RenderNode span1 = findRenderNode(1, root);
		RenderNode span2 = findRenderNode(2, root);
		RenderNode span3 = findRenderNode(3, root);
		RenderNode span4 = findRenderNode(4, root);
		RenderNode span5 = findRenderNode(5, root);
		
		assertEquals((Float) 100f, (Float) root.box.width);
		assertEquals((Float) 30f, (Float) root.box.height);
		assertEquals((Float) 0f, (Float) span1.box.x);
		assertEquals((Float) 0f, (Float) span1.box.y);
		assertEquals((Float) 40f, (Float) span2.box.x);
		assertEquals((Float) 0f, (Float) span2.box.y);
		assertEquals((Float) 0f, (Float) span3.box.x);
		assertEquals((Float) 10f, (Float) span3.box.y);
		assertEquals((Float) 40f, (Float) span4.box.x);
		assertEquals((Float) 10f, (Float) span4.box.y);
		assertEquals((Float) 0f, (Float) span5.box.x);
		assertEquals((Float) 20f, (Float) span5.box.y);
	}
	
	@Test
	public void calculateBoxesTestTree3() {
		createTree3();
		BoxLayoutCalculator blc = new BoxLayoutCalculator(parentMap, 100f);
		blc.setBoxBounds(root);
		blc.propagateMaxSizes(root);
		blc.calculateBoxes(root);
		
		RenderNode A = findRenderNode(1, root);
		RenderNode B = findRenderNode(2, root);
		RenderNode C = findRenderNode(3, root);
		RenderNode D = findRenderNode(4, root);
		RenderNode E = findRenderNode(5, root);
		RenderNode F = findRenderNode(6, root);
		
		assertEquals((Float) 0f, (Float) A.box.x);
		assertEquals((Float) 0f, (Float) A.box.y);
		assertEquals((Float) 50f, (Float) A.box.width);
		assertEquals((Float) 50f, (Float) A.box.height);

		assertEquals((Float) 0f, (Float) B.box.x);
		assertEquals((Float) 50f, (Float) B.box.y);
		assertEquals((Float) 90f, (Float) B.box.width);
		assertEquals((Float) 10f, (Float) B.box.height);
		
		assertEquals((Float) 0f, (Float) C.box.x);
		assertEquals((Float) 50f, (Float) C.box.y);
		assertEquals((Float) 62f, (Float) C.box.width);
		assertEquals((Float) 10f, (Float) C.box.height);
		
		assertEquals((Float) 0f, (Float) D.box.x);
		assertEquals((Float) 50f, (Float) D.box.y);
		assertEquals((Float) 31f, (Float) D.box.width);
		assertEquals((Float) 5f, (Float) D.box.height);
		
		assertEquals((Float) 31f, (Float) E.box.x);
		assertEquals((Float) 50f, (Float) E.box.y);
		assertEquals((Float) 31f, (Float) E.box.width);
		assertEquals((Float) 5f, (Float) E.box.height);
		
		assertEquals((Float) 0f, (Float) F.box.x);
		assertEquals((Float) 55f, (Float) F.box.y);
		assertEquals((Float) 31f, (Float) F.box.width);
		assertEquals((Float) 5f, (Float) F.box.height);
	}
	
	@Test
	public void propagateMaxSizesTestTree2() {
		createTree2();
		BoxLayoutCalculator blc = new BoxLayoutCalculator(parentMap, 50f);
		blc.propagateMaxSizes(root);
		for (int i = 1; i <= 5; ++i) {
			RenderNode node = findRenderNode(i, root);
			assertEquals((Float) 40f, node.maxWidth);
			assertEquals((Float) 10f, node.maxHeight);
		}
	}
	
	@Test
	public void propagateMaxSizesTestTree3() {
		createTree3();
		BoxLayoutCalculator blc = new BoxLayoutCalculator(parentMap, 100f);
		blc.propagateMaxSizes(root);
		RenderNode C = findRenderNode(3, root);
		assertNull(C.maxHeight);
		assertEquals((Float) 90f, C.maxWidth);

	}
	
	@Test
	public void propagateMaxSizesTest_small() {
		root = new RenderNode("body");
		RenderNode A = new RenderNode("div");
		RenderNode B = new RenderNode("div");
		RenderNode C = new RenderNode("div");
		RenderNode D = new RenderNode("div");

		float screenWidth = 100;
		
		A.box.fixedWidth = true;
		A.box.fixedHeight = true;
		A.box.width = 50;
		A.box.height = 50;
		
		B.box.fixedWidth = true;
		B.box.fixedHeight = true;
		B.box.width = 60;
		B.box.height = 20;
		
		C.box.fixedWidth = true;
		C.box.width = 30f;
		
		D.box.fixedHeight = true;
		D.box.height = 40;
		
		root.children.add(A);
		root.children.add(B);
		A.children.add(D);
		B.children.add(C);
		
		root.id = 0;		root.depth = 0;
		A.id = 1;			A.depth = 1;
		B.id = 2;			B.depth = 1;
		C.id = 3;			C.depth = 2;
		D.id = 4;			D.depth = 2;
		
		parentMap = new HashMap<Integer, RenderNode>();
		parentMap.put(1, root);
		parentMap.put(2, root);
		parentMap.put(3, B);
		parentMap.put(4, A);
		
		BoxLayoutCalculator blc = new BoxLayoutCalculator(parentMap, screenWidth);
//		blc.printBoxes(root);
		blc.propagateMaxSizes(root);
//		blc.printBoxes(root);
		
		assertEquals((Float) 20f, C.maxHeight);
		assertEquals((Float) 30f, C.maxWidth);
		assertEquals((Float) 50f, D.maxWidth);
		assertEquals((Float) 40f, D.maxHeight);
		
	}
	
	@Test
	public void propagateMaxSizesTest_simple() {
		root = new RenderNode("body");
		RenderNode A = new RenderNode("div");
		RenderNode B = new RenderNode("div");
		RenderNode C = new RenderNode("div");
		RenderNode D = new RenderNode("div");
		RenderNode E = new RenderNode("div");
		RenderNode F = new RenderNode("div");
		RenderNode G = new RenderNode("div");

		float screenWidth = 100;
		
		A.box.fixedWidth = true;
		A.box.width = 80;
		
		B.box.fixedWidth = true;
		B.box.fixedHeight = true;
		B.box.width = 90;
		B.box.height = 90;
		
		C.box.fixedWidth = true;
		C.box.width = 50;
		
		F.box.fixedWidth = true;
		F.box.fixedHeight = true;
		F.box.width = 50;
		F.box.height = 10;
		
		root.children.add(A);
		root.children.add(B);
		A.children.add(F);
		B.children.add(C);
		B.children.add(D);
		D.children.add(E);
		F.children.add(G);
		
		root.id = 0;		root.depth = 0;
		A.id = 1;			A.depth = 1;
		B.id = 2;			B.depth = 1;
		C.id = 3;			C.depth = 2;
		D.id = 4;			D.depth = 2;
		E.id = 5;			E.depth = 3;
		F.id = 6;			F.depth = 2;
		G.id = 7;			G.depth = 3;
		
		parentMap = new HashMap<Integer, RenderNode>();
		parentMap.put(1, root);
		parentMap.put(2, root);
		parentMap.put(3, B);
		parentMap.put(4, B);
		parentMap.put(5, D);
		parentMap.put(6, A);
		parentMap.put(7, F);
		
		BoxLayoutCalculator blc = new BoxLayoutCalculator(parentMap, screenWidth);
//		blc.printBoxes(root);
		blc.propagateMaxSizes(root);
		
		assertEquals((Float) 80f, A.maxWidth);
		assertNull(A.maxHeight);
		assertEquals((Float) 90f, B.maxWidth);
		assertEquals((Float) 90f, B.maxHeight);
		assertEquals((Float) 50f, C.maxWidth);
		assertEquals((Float) 90f, C.maxHeight);
		assertEquals((Float) 90f, D.maxWidth);
		assertEquals((Float) 90f, D.maxHeight);
		assertEquals((Float) 90f, E.maxWidth);
		assertEquals((Float) 90f, E.maxHeight);
		assertEquals((Float) 50f, F.maxWidth);
		assertEquals((Float) 10f, F.maxHeight);
		assertEquals((Float) 50f, G.maxWidth);
		assertEquals((Float) 10f, G.maxHeight);
		
	}
	
	private RenderNode findRenderNode(int id, RenderNode root) {
		if (root.id == id) return root;
		for (RenderNode child : root.children) {
			RenderNode found = findRenderNode(id, child);
			if (found != null) return found;
		}
		return null;
	}

}
