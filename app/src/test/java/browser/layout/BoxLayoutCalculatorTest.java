package browser.layout;

import static browser.constants.MathConstants.DELTA;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import browser.app.Pipeline;
import browser.css.CSSStyle;
import browser.model.RenderNode;
import browser.parser.HTMLElements;

import org.junit.Before;
import org.junit.Test;

public class BoxLayoutCalculatorTest {

    private RenderNode root;
    private Map<Integer, RenderNode> parentMap;

    @Before
    public void setup() {
        parentMap = new HashMap<>();
        Pipeline.init();
    }

    @Test
    public void setBoxBoundsTest_SimpleFixedSizeDiv() {
        float screenWidth = 100;
        BoxLayoutCalculator calculator = new BoxLayoutCalculator(parentMap, screenWidth);

        RenderNode body = new RenderNode(HTMLElements.BODY);
        body.id = 0;
        RenderNode div = new RenderNode(HTMLElements.DIV);
        div.id = 1;
        div.style.width = 50f;
        div.style.height = 60f;

        body.addChild(div);
        parentMap.put(div.id, body);

        calculator.setBoxBounds(body);

        assertTrue(body.box.fixedWidth);
        assertFalse(body.box.fixedHeight);
        assertEquals(screenWidth, body.box.width, DELTA);
        assertEquals(screenWidth, body.maxWidth, DELTA);
        assertNull(body.maxHeight);

        assertTrue(div.box.fixedWidth);
        assertTrue(div.box.fixedHeight);
        assertEquals(50f, div.box.width, DELTA);
        assertEquals(screenWidth, div.maxWidth, DELTA);
        assertEquals(60f, div.box.height, DELTA);
        assertNull(div.maxHeight);
    }

    @Test
    public void setBoxBoundsTest_InlineFixedSizeDiv() {
        float screenWidth = 100;
        BoxLayoutCalculator calculator = new BoxLayoutCalculator(parentMap, screenWidth);

        RenderNode body = new RenderNode(HTMLElements.BODY);
        body.id = 0;
        RenderNode div = new RenderNode(HTMLElements.DIV);
        div.id = 1;
        div.style.width = 50f;
        div.style.height = 60f;
        div.style.display = CSSStyle.DisplayType.INLINE;

        body.addChild(div);
        parentMap.put(div.id, body);

        calculator.setBoxBounds(body);

        assertTrue(body.box.fixedWidth);
        assertFalse(body.box.fixedHeight);
        assertEquals(screenWidth, body.box.width, DELTA);
        assertEquals(screenWidth, body.maxWidth, DELTA);
        assertNull(body.maxHeight);

        assertFalse(div.box.fixedWidth);
        assertFalse(div.box.fixedHeight);
        assertEquals(0, div.box.width, DELTA);
        assertEquals(screenWidth, div.maxWidth, DELTA);
        assertEquals(0, div.box.height, DELTA);
        assertNull(div.maxHeight);
    }

    @Test
    public void calculateBoxesTest_ExpandDivs() {
        final float screenWidth = 100;
        final float div1Width = 50;
        BoxLayoutCalculator calculator = new BoxLayoutCalculator(parentMap, screenWidth);

        RenderNode body = new RenderNode(HTMLElements.BODY);
        body.id = 0;
        body.box.fixedWidth = true;
        body.box.width = screenWidth;
        body.maxWidth = screenWidth;
        RenderNode div1 = new RenderNode(HTMLElements.DIV);
        div1.id = 1;
        div1.box.fixedWidth = true;
        div1.box.width = div1Width;
        div1.maxWidth = div1Width;
        RenderNode div2 = new RenderNode(HTMLElements.DIV);
        div2.id = 2;
        div2.maxWidth = div1Width;
        RenderNode div3 = new RenderNode(HTMLElements.DIV);
        div3.id = 3;
        div3.maxWidth = div1Width;

        body.addChild(div1);
        div1.addChild(div2);
        div2.addChild(div3);

        parentMap.put(div1.id, body);
        parentMap.put(div2.id, div1);
        parentMap.put(div3.id, div2);

        calculator.calculateBoxes(body);

        assertEquals(0, body.box.x, DELTA);
        assertEquals(0, body.box.y, DELTA);
        assertEquals(screenWidth, body.box.width, DELTA);
        assertEquals(0, body.box.height, DELTA);

        assertEquals(0, div1.box.x, DELTA);
        assertEquals(0, div1.box.y, DELTA);
        assertEquals(div1Width, div1.box.width, DELTA);
        assertEquals(0, div1.box.height, DELTA);

        assertEquals(0, div2.box.x, DELTA);
        assertEquals(0, div2.box.y, DELTA);
        assertEquals(div1Width, div2.box.width, DELTA);
        assertEquals(0, div2.box.height, DELTA);

        assertEquals(0, div3.box.x, DELTA);
        assertEquals(0, div3.box.y, DELTA);
        assertEquals(div1Width, div3.box.width, DELTA);
        assertEquals(0, div3.box.height, DELTA);
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

        span1.style.display = CSSStyle.DisplayType.INLINE;
        span2.style.display = CSSStyle.DisplayType.INLINE;
        span3.style.display = CSSStyle.DisplayType.INLINE;
        span4.style.display = CSSStyle.DisplayType.INLINE;
        span5.style.display = CSSStyle.DisplayType.INLINE;

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

        root.id = 0;    root.depth = 0;
        span1.id = 1;    span1.depth = 1;
        span2.id = 2;    span2.depth = 1;
        span3.id = 3;    span3.depth = 1;
        span4.id = 4;    span3.depth = 1;
        span5.id = 5;    span3.depth = 1;


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
        RenderNode nodeA = new RenderNode("div");
        RenderNode nodeB = new RenderNode("div");
        RenderNode nodeC = new RenderNode("div");
        RenderNode nodeD = new RenderNode("div");
        RenderNode nodeE = new RenderNode("div");
        RenderNode nodeF = new RenderNode("div");

        nodeD.style.display = CSSStyle.DisplayType.INLINE;
        nodeE.style.display = CSSStyle.DisplayType.INLINE;
        nodeF.style.display = CSSStyle.DisplayType.INLINE;

        nodeA.box.fixedWidth = true;
        nodeB.box.fixedWidth = true;
        nodeD.box.fixedWidth = true;
        nodeE.box.fixedWidth = true;
        nodeF.box.fixedWidth = true;

        nodeA.box.fixedHeight = true;
        nodeD.box.fixedHeight = true;
        nodeE.box.fixedHeight = true;
        nodeF.box.fixedHeight = true;

        nodeA.box.width = 50;
        nodeA.box.height = 50;
        nodeB.box.width = 90;
        nodeD.box.width = 31;
        nodeD.box.height = 5;
        nodeE.box.width = 31;
        nodeE.box.height = 5;
        nodeF.box.width = 31;
        nodeF.box.height = 5;

        root.id = 0;    root.depth = 0;
        nodeA.id = 1;    nodeA.depth = 1;
        nodeB.id = 2;    nodeB.depth = 1;
        nodeC.id = 3;    nodeC.depth = 2;
        nodeD.id = 4;    nodeD.depth = 3;
        nodeE.id = 5;    nodeE.depth = 3;
        nodeF.id = 6;    nodeF.depth = 3;

        root.children.add(nodeA);
        root.children.add(nodeB);
        nodeB.children.add(nodeC);
        nodeC.children.add(nodeD);
        nodeC.children.add(nodeE);
        nodeC.children.add(nodeF);

        parentMap.put(1, root);
        parentMap.put(2, root);
        parentMap.put(3, nodeB);
        parentMap.put(4, nodeC);
        parentMap.put(5, nodeC);
        parentMap.put(6, nodeC);
    }

    private void createTree4() {
        // Two nested divs with padding, and some text in the middle
        // Outer div has fixed size, inner doesn't

        parentMap = new HashMap<Integer, RenderNode>();

        root = new RenderNode("body");
        RenderNode nodeA = new RenderNode("div");
        RenderNode nodeB = new RenderNode("div");
        RenderNode nodeC = new RenderNode("text");

        nodeA.box.fixedWidth = true;
        nodeA.box.fixedHeight = true;

        nodeA.box.width = 50;
        nodeA.box.height = 50;

        nodeA.style.paddingTop = 5;
        nodeA.style.paddingBottom = 5;
        nodeA.style.paddingLeft = 10;
        nodeA.style.paddingRight = 10;

        nodeB.style.paddingTop = 2;
        nodeB.style.paddingBottom = 2;
        nodeB.style.paddingLeft = 2;
        nodeB.style.paddingRight = 2;

        root.id = 0;    root.depth = 0;
        nodeA.id = 1;        nodeA.depth = 1;
        nodeB.id = 2;        nodeB.depth = 2;
        nodeC.id = 3;        nodeC.depth = 3;

        root.children.add(nodeA);
        nodeA.children.add(nodeB);
        nodeB.children.add(nodeC);

        parentMap.put(1, root);
        parentMap.put(2, nodeA);
        parentMap.put(3, nodeB);
    }

    private void createTree5() {
        // Has some relative sizes, three nested divs

        parentMap = new HashMap<Integer, RenderNode>();

        root = new RenderNode("body");
        RenderNode nodeA = new RenderNode("div");
        RenderNode nodeB = new RenderNode("div");
        RenderNode nodeC = new RenderNode("div");

        nodeA.box.fixedWidth = true;
        nodeA.box.width = 210;

        nodeB.box.fixedWidth = true;
        nodeB.box.fixedHeight = true;
        nodeB.box.width = 50;
        nodeB.style.widthType = CSSStyle.DimensionType.PERCENTAGE;
        nodeB.box.height = 100;

        nodeC.box.fixedWidth = true;
        nodeC.box.width = 20;
        nodeC.style.widthType = CSSStyle.DimensionType.PERCENTAGE;
        nodeC.box.fixedHeight = true;
        nodeC.box.height = 10;
        nodeC.style.heightType = CSSStyle.DimensionType.PERCENTAGE;

        nodeA.style.paddingTop = 5;
        nodeA.style.paddingBottom = 5;
        nodeA.style.paddingLeft = 5;
        nodeA.style.paddingRight = 5;

        nodeB.style.paddingTop = 5;
        nodeB.style.paddingBottom = 5;
        nodeB.style.paddingLeft = 10;
        nodeB.style.paddingRight = 10;

        nodeC.style.marginTop = 5;
        nodeC.style.marginBottom = 5;

        root.id = 0;    root.depth = 0;
        nodeA.id = 1;        nodeA.depth = 1;
        nodeB.id = 2;        nodeB.depth = 2;
        nodeC.id = 3;        nodeC.depth = 3;

        root.children.add(nodeA);
        nodeA.children.add(nodeB);
        nodeB.children.add(nodeC);

        parentMap.put(1, root);
        parentMap.put(2, nodeA);
        parentMap.put(3, nodeB);
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

        RenderNode nodeA = findRenderNode(1, root);
        RenderNode nodeB = findRenderNode(2, root);
        RenderNode nodeC = findRenderNode(3, root);
        RenderNode nodeD = findRenderNode(4, root);
        RenderNode nodeE = findRenderNode(5, root);
        RenderNode nodeF = findRenderNode(6, root);

        assertEquals((Float) 0f, (Float) nodeA.box.x);
        assertEquals((Float) 0f, (Float) nodeA.box.y);
        assertEquals((Float) 50f, (Float) nodeA.box.width);
        assertEquals((Float) 50f, (Float) nodeA.box.height);

        assertEquals((Float) 0f, (Float) nodeB.box.x);
        assertEquals((Float) 50f, (Float) nodeB.box.y);
        assertEquals((Float) 90f, (Float) nodeB.box.width);
        assertEquals((Float) 10f, (Float) nodeB.box.height);

        assertEquals((Float) 0f, (Float) nodeC.box.x);
        assertEquals((Float) 50f, (Float) nodeC.box.y);
        assertEquals((Float) 62f, (Float) nodeC.box.width);
        assertEquals((Float) 10f, (Float) nodeC.box.height);

        assertEquals((Float) 0f, (Float) nodeD.box.x);
        assertEquals((Float) 50f, (Float) nodeD.box.y);
        assertEquals((Float) 31f, (Float) nodeD.box.width);
        assertEquals((Float) 5f, (Float) nodeD.box.height);

        assertEquals((Float) 31f, (Float) nodeE.box.x);
        assertEquals((Float) 50f, (Float) nodeE.box.y);
        assertEquals((Float) 31f, (Float) nodeE.box.width);
        assertEquals((Float) 5f, (Float) nodeE.box.height);

        assertEquals((Float) 0f, (Float) nodeF.box.x);
        assertEquals((Float) 55f, (Float) nodeF.box.y);
        assertEquals((Float) 31f, (Float) nodeF.box.width);
        assertEquals((Float) 5f, (Float) nodeF.box.height);
    }

    @Test
    public void calculateBoxesTestTree4() {
        createTree4();
        BoxLayoutCalculator blc = new BoxLayoutCalculator(parentMap, 100f);
        blc.setBoxBounds(root);
        blc.propagateMaxSizes(root);
        blc.calculateBoxes(root);

        RenderNode nodeA = findRenderNode(1, root);
        RenderNode nodeB = findRenderNode(2, root);
        RenderNode nodeC = findRenderNode(3, root);

        assertEquals((Float) 0f, (Float) nodeA.box.x);
        assertEquals((Float) 0f, (Float) nodeA.box.y);
        assertEquals((Float) 10f, (Float) nodeB.box.x);
        assertEquals((Float) 5f, (Float) nodeB.box.y);
        assertEquals((Float) 12f, (Float) nodeC.box.x);
        assertEquals((Float) 7f, (Float) nodeC.box.y);

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
        RenderNode nodeC = findRenderNode(3, root);
        assertNull(nodeC.maxHeight);
        assertEquals((Float) 90f, nodeC.maxWidth);

    }

    @Test
    public void propagateMaxSizesTestTree5() {
        createTree5();
        BoxLayoutCalculator blc = new BoxLayoutCalculator(parentMap, 300f);
        blc.propagateMaxSizes(root);
        RenderNode nodeA = findRenderNode(1, root);
        RenderNode nodeB = findRenderNode(2, root);
        RenderNode nodeC = findRenderNode(3, root);
        assertEquals((Float) 210f, nodeA.maxWidth);
        assertEquals((Float) 100f, nodeB.maxWidth);
        assertEquals((Float) 100f, nodeB.maxHeight);
        assertEquals((Float) 16f, nodeC.maxWidth);
        assertEquals((Float) 16f, nodeC.maxHeight);

    }

    @Test
    public void propagateMaxSizesTest_small() {
        root = new RenderNode("body");
        RenderNode nodeA = new RenderNode("div");
        RenderNode nodeB = new RenderNode("div");
        RenderNode nodeC = new RenderNode("div");
        RenderNode nodeD = new RenderNode("div");

        float screenWidth = 100;

        nodeA.box.fixedWidth = true;
        nodeA.box.fixedHeight = true;
        nodeA.box.width = 50;
        nodeA.box.height = 50;

        nodeB.box.fixedWidth = true;
        nodeB.box.fixedHeight = true;
        nodeB.box.width = 60;
        nodeB.box.height = 20;

        nodeC.box.fixedWidth = true;
        nodeC.box.width = 30f;

        nodeD.box.fixedHeight = true;
        nodeD.box.height = 40;

        root.children.add(nodeA);
        root.children.add(nodeB);
        nodeA.children.add(nodeD);
        nodeB.children.add(nodeC);

        root.id = 0;        root.depth = 0;
        nodeA.id = 1;            nodeA.depth = 1;
        nodeB.id = 2;            nodeB.depth = 1;
        nodeC.id = 3;            nodeC.depth = 2;
        nodeD.id = 4;            nodeD.depth = 2;

        parentMap = new HashMap<Integer, RenderNode>();
        parentMap.put(1, root);
        parentMap.put(2, root);
        parentMap.put(3, nodeB);
        parentMap.put(4, nodeA);

        BoxLayoutCalculator blc = new BoxLayoutCalculator(parentMap, screenWidth);
//        blc.printBoxes(root);
        blc.propagateMaxSizes(root);
//        blc.printBoxes(root);

        assertEquals((Float) 20f, nodeC.maxHeight);
        assertEquals((Float) 30f, nodeC.maxWidth);
        assertEquals((Float) 50f, nodeD.maxWidth);
        assertEquals((Float) 40f, nodeD.maxHeight);

    }

    @Test
    public void propagateMaxSizesTest_simple() {
        root = new RenderNode("body");
        RenderNode nodeA = new RenderNode("div");
        RenderNode nodeB = new RenderNode("div");
        RenderNode nodeC = new RenderNode("div");
        RenderNode nodeD = new RenderNode("div");
        RenderNode nodeE = new RenderNode("div");
        RenderNode nodeF = new RenderNode("div");
        RenderNode nodeG = new RenderNode("div");

        float screenWidth = 100;

        nodeA.box.fixedWidth = true;
        nodeA.box.width = 80;

        nodeB.box.fixedWidth = true;
        nodeB.box.fixedHeight = true;
        nodeB.box.width = 90;
        nodeB.box.height = 90;

        nodeC.box.fixedWidth = true;
        nodeC.box.width = 50;

        nodeF.box.fixedWidth = true;
        nodeF.box.fixedHeight = true;
        nodeF.box.width = 50;
        nodeF.box.height = 10;

        root.children.add(nodeA);
        root.children.add(nodeB);
        nodeA.children.add(nodeF);
        nodeB.children.add(nodeC);
        nodeB.children.add(nodeD);
        nodeD.children.add(nodeE);
        nodeF.children.add(nodeG);

        root.id = 0;        root.depth = 0;
        nodeA.id = 1;            nodeA.depth = 1;
        nodeB.id = 2;            nodeB.depth = 1;
        nodeC.id = 3;            nodeC.depth = 2;
        nodeD.id = 4;            nodeD.depth = 2;
        nodeE.id = 5;            nodeE.depth = 3;
        nodeF.id = 6;            nodeF.depth = 2;
        nodeG.id = 7;            nodeG.depth = 3;

        parentMap = new HashMap<Integer, RenderNode>();
        parentMap.put(1, root);
        parentMap.put(2, root);
        parentMap.put(3, nodeB);
        parentMap.put(4, nodeB);
        parentMap.put(5, nodeD);
        parentMap.put(6, nodeA);
        parentMap.put(7, nodeF);

        BoxLayoutCalculator blc = new BoxLayoutCalculator(parentMap, screenWidth);
//        blc.printBoxes(root);
        blc.propagateMaxSizes(root);

        assertEquals((Float) 80f, nodeA.maxWidth);
        assertNull(nodeA.maxHeight);
        assertEquals((Float) 90f, nodeB.maxWidth);
        assertEquals((Float) 90f, nodeB.maxHeight);
        assertEquals((Float) 50f, nodeC.maxWidth);
        assertEquals((Float) 90f, nodeC.maxHeight);
        assertEquals((Float) 90f, nodeD.maxWidth);
        assertEquals((Float) 90f, nodeD.maxHeight);
        assertEquals((Float) 90f, nodeE.maxWidth);
        assertEquals((Float) 90f, nodeE.maxHeight);
        assertEquals((Float) 50f, nodeF.maxWidth);
        assertEquals((Float) 10f, nodeF.maxHeight);
        assertEquals((Float) 50f, nodeG.maxWidth);
        assertEquals((Float) 10f, nodeG.maxHeight);

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
