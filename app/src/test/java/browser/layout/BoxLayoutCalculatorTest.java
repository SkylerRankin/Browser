package browser.layout;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import browser.css.CSSStyle;
//import javafx.embed.swing.JFXPanel;
import browser.layout.BoxLayoutCalculator;
import browser.model.RenderNode;

public class BoxLayoutCalculatorTest {

    private RenderNode root;
    private Map<Integer, RenderNode> parentMap;

//	@Before
//	public void before() {
//		JFXPanel jfxPanel = new JFXPanel();
//	}

    private void createTree2() {

        // 5 in-line spans in a row; should show two per line, since each is 40 wide and total width is set to 100

        parentMap = new HashMap<Integer, RenderNode>();

        root = new RenderNode("body");
        RenderNode span1 = new RenderNode("span");
        RenderNode span2 = new RenderNode("span");
        RenderNode span3 = new RenderNode("span");
        RenderNode span4 = new RenderNode("span");
        RenderNode span5 = new RenderNode("span");

        span1.style.display = CSSStyle.displayType.INLINE;
        span2.style.display = CSSStyle.displayType.INLINE;
        span3.style.display = CSSStyle.displayType.INLINE;
        span4.style.display = CSSStyle.displayType.INLINE;
        span5.style.display = CSSStyle.displayType.INLINE;

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

        D.style.display = CSSStyle.displayType.INLINE;
        E.style.display = CSSStyle.displayType.INLINE;
        F.style.display = CSSStyle.displayType.INLINE;

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

    private void createTree4() {
        // Two nested divs with padding, and some text in the middle
        // Outer div has fixed size, inner doesn't

        parentMap = new HashMap<Integer, RenderNode>();

        root = new RenderNode("body");
        RenderNode A = new RenderNode("div");
        RenderNode B = new RenderNode("div");
        RenderNode C = new RenderNode("text");

        A.box.fixedWidth = true;
        A.box.fixedHeight = true;

        A.box.width = 50;
        A.box.height = 50;

        A.style.paddingTop = 5;
        A.style.paddingBottom = 5;
        A.style.paddingLeft = 10;
        A.style.paddingRight = 10;

        B.style.paddingTop = 2;
        B.style.paddingBottom = 2;
        B.style.paddingLeft = 2;
        B.style.paddingRight = 2;

        root.id = 0;	root.depth = 0;
        A.id = 1;		A.depth = 1;
        B.id = 2;		B.depth = 2;
        C.id = 3;		C.depth = 3;

        root.children.add(A);
        A.children.add(B);
        B.children.add(C);

        parentMap.put(1, root);
        parentMap.put(2, A);
        parentMap.put(3, B);
    }

    private void createTree5() {
        // Has some relative sizes, three nested divs

        parentMap = new HashMap<Integer, RenderNode>();

        root = new RenderNode("body");
        RenderNode A = new RenderNode("div");
        RenderNode B = new RenderNode("div");
        RenderNode C = new RenderNode("div");

        A.box.fixedWidth = true;
        A.box.width = 210;

        B.box.fixedWidth = true;
        B.box.fixedHeight = true;
        B.box.width = 50;
        B.style.widthType = CSSStyle.dimensionType.PERCENTAGE;
        B.box.height = 100;

        C.box.fixedWidth = true;
        C.box.width = 20;
        C.style.widthType = CSSStyle.dimensionType.PERCENTAGE;
        C.box.fixedHeight = true;
        C.box.height = 10;
        C.style.heightType = CSSStyle.dimensionType.PERCENTAGE;

        A.style.paddingTop = 5;
        A.style.paddingBottom = 5;
        A.style.paddingLeft = 5;
        A.style.paddingRight = 5;

        B.style.paddingTop = 5;
        B.style.paddingBottom = 5;
        B.style.paddingLeft = 10;
        B.style.paddingRight = 10;

        C.style.marginTop = 5;
        C.style.marginBottom = 5;

        root.id = 0;	root.depth = 0;
        A.id = 1;		A.depth = 1;
        B.id = 2;		B.depth = 2;
        C.id = 3;		C.depth = 3;

        root.children.add(A);
        A.children.add(B);
        B.children.add(C);

        parentMap.put(1, root);
        parentMap.put(2, A);
        parentMap.put(3, B);
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
    public void calculateBoxesTestTree4() {
        createTree4();
        BoxLayoutCalculator blc = new BoxLayoutCalculator(parentMap, 100f);
        blc.setBoxBounds(root);
        blc.propagateMaxSizes(root);
        blc.calculateBoxes(root);

        RenderNode A = findRenderNode(1, root);
        RenderNode B = findRenderNode(2, root);
        RenderNode C = findRenderNode(3, root);

        assertEquals((Float) 0f, (Float) A.box.x);
        assertEquals((Float) 0f, (Float) A.box.y);
        assertEquals((Float) 10f, (Float) B.box.x);
        assertEquals((Float) 5f, (Float) B.box.y);
        assertEquals((Float) 12f, (Float) C.box.x);
        assertEquals((Float) 7f, (Float) C.box.y);

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
    public void propagateMaxSizesTrestTree5() {
        createTree5();
        BoxLayoutCalculator blc = new BoxLayoutCalculator(parentMap, 300f);
        blc.propagateMaxSizes(root);
        RenderNode A = findRenderNode(1, root);
        RenderNode B = findRenderNode(2, root);
        RenderNode C = findRenderNode(3, root);
        assertEquals((Float) 210f, A.maxWidth);
        assertEquals((Float) 100f, B.maxWidth);
        assertEquals((Float) 100f, B.maxHeight);
        assertEquals((Float) 16f, C.maxWidth);
        assertEquals((Float) 16f, C.maxHeight);

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
