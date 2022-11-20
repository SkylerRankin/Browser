package browser.layout;

import static browser.css.CSSStyle.DisplayType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import browser.app.Pipeline;
import browser.model.BoxNode;
import browser.model.RenderNode;
import browser.parser.HTMLElements;

import org.junit.BeforeClass;
import org.junit.Test;

public class BoxTreeGeneratorTest {

    @BeforeClass
    public static void setup() {
        Pipeline.init();
    }

    @Test
    public void generatesOneBoxForEachRenderNode() {
        RenderNode renderNode1 = new RenderNode(HTMLElements.DIV);
        renderNode1.id = 0;
        RenderNode renderNode2 = new RenderNode(HTMLElements.P);
        renderNode2.id = 1;
        RenderNode renderNode3 = new RenderNode(HTMLElements.SPAN);
        renderNode3.id = 2;
        RenderNode renderNode4 = new RenderNode(HTMLElements.SPAN);
        renderNode4.id = 3;

        renderNode1.children.add(renderNode2);
        renderNode2.children.add(renderNode3);
        renderNode2.children.add(renderNode4);

        BoxTreeGenerator boxTreeGenerator = new BoxTreeGenerator();
        boxTreeGenerator.generate(renderNode1);

        assertNotNull(boxTreeGenerator.getBoxNodeForRenderNodeId(renderNode1.id));
        assertNotNull(boxTreeGenerator.getBoxNodeForRenderNodeId(renderNode2.id));
        assertNotNull(boxTreeGenerator.getBoxNodeForRenderNodeId(renderNode3.id));
        assertNotNull(boxTreeGenerator.getBoxNodeForRenderNodeId(renderNode4.id));

        assertEquals(0, boxTreeGenerator.getBoxNodeForRenderNodeId(renderNode1.id).id);
        assertEquals(1, boxTreeGenerator.getBoxNodeForRenderNodeId(renderNode2.id).id);
        assertEquals(2, boxTreeGenerator.getBoxNodeForRenderNodeId(renderNode3.id).id);
        assertEquals(3, boxTreeGenerator.getBoxNodeForRenderNodeId(renderNode4.id).id);
    }

    @Test
    public void generateOnlyBlockInsideBlock() {
        RenderNode div = new RenderNode(HTMLElements.DIV);
        div.id = 0;
        div.style.outerDisplay = DisplayType.BLOCK;
        div.style.innerDisplay = DisplayType.FLOW;

        RenderNode h3 = new RenderNode(HTMLElements.H3);
        h3.id = 1;
        h3.style.outerDisplay = DisplayType.BLOCK;
        h3.style.innerDisplay = DisplayType.FLOW;

        RenderNode div1 = new RenderNode(HTMLElements.DIV);
        div1.id = 2;
        div1.style.outerDisplay = DisplayType.BLOCK;
        div1.style.innerDisplay = DisplayType.FLOW;

        RenderNode p = new RenderNode(HTMLElements.P);
        p.id = 3;
        p.style.outerDisplay = DisplayType.BLOCK;
        p.style.innerDisplay = DisplayType.FLOW;

        div.children.addAll(List.of(h3, div1, p));

        BoxTreeGenerator boxTreeGenerator = new BoxTreeGenerator();
        BoxNode rootBoxNode = boxTreeGenerator.generate(div);

        BoxNode divBox = new BoxNode();
        divBox.id = 0;
        divBox.renderNodeId = div.id;
        divBox.outerDisplayType = DisplayType.BLOCK;
        divBox.innerDisplayType = DisplayType.FLOW;

        BoxNode h3Box = new BoxNode();
        h3Box.id = 1;
        h3Box.renderNodeId = h3.id;
        h3Box.parent = divBox;
        h3Box.outerDisplayType = DisplayType.BLOCK;
        h3Box.innerDisplayType = DisplayType.FLOW;

        BoxNode div1Box = new BoxNode();
        div1Box.id = 2;
        div1Box.renderNodeId = div1.id;
        div1Box.parent = divBox;
        div1Box.outerDisplayType = DisplayType.BLOCK;
        div1Box.innerDisplayType = DisplayType.FLOW;

        BoxNode pBox = new BoxNode();
        pBox.id = 3;
        pBox.renderNodeId = p.id;
        pBox.parent = divBox;
        pBox.outerDisplayType = DisplayType.BLOCK;
        pBox.innerDisplayType = DisplayType.FLOW;

        divBox.children.addAll(List.of(h3Box, div1Box, pBox));

        assertEquals(divBox, rootBoxNode);
    }

    @Test
    public void generateOnlyInlineInsideBlock() {
        RenderNode div = new RenderNode(HTMLElements.DIV);
        div.id = 0;
        div.style.outerDisplay = DisplayType.BLOCK;
        div.style.innerDisplay = DisplayType.FLOW;

        RenderNode span1 = new RenderNode(HTMLElements.SPAN);
        span1.id = 1;
        span1.style.outerDisplay = DisplayType.INLINE;
        span1.style.innerDisplay = DisplayType.FLOW;

        RenderNode span2 = new RenderNode(HTMLElements.SPAN);
        span2.id = 2;
        span2.style.outerDisplay = DisplayType.INLINE;
        span2.style.innerDisplay = DisplayType.FLOW;

        RenderNode text = new RenderNode(HTMLElements.TEXT);
        text.id = 3;
        text.style.outerDisplay = DisplayType.INLINE;
        text.style.innerDisplay = DisplayType.FLOW;

        div.children.addAll(List.of(span1, span2, text));

        BoxTreeGenerator boxTreeGenerator = new BoxTreeGenerator();
        BoxNode rootBoxNode = boxTreeGenerator.generate(div);

        BoxNode divBox = new BoxNode();
        divBox.id = 0;
        divBox.renderNodeId = div.id;
        divBox.outerDisplayType = DisplayType.BLOCK;
        divBox.innerDisplayType = DisplayType.FLOW;

        BoxNode span1Box = new BoxNode();
        span1Box.id = 1;
        span1Box.renderNodeId = span1.id;
        span1Box.parent = divBox;
        span1Box.outerDisplayType = DisplayType.INLINE;
        span1Box.innerDisplayType = DisplayType.FLOW;

        BoxNode span2Box = new BoxNode();
        span2Box.id = 2;
        span2Box.renderNodeId = span2.id;
        span2Box.parent = divBox;
        span2Box.outerDisplayType = DisplayType.INLINE;
        span2Box.innerDisplayType = DisplayType.FLOW;

        BoxNode textBox = new BoxNode();
        textBox.id = 3;
        textBox.renderNodeId = text.id;
        textBox.parent = divBox;
        textBox.outerDisplayType = DisplayType.INLINE;
        textBox.innerDisplayType = DisplayType.FLOW;
        textBox.isAnonymous = true;
        textBox.isTextNode = true;

        divBox.children.addAll(List.of(span1Box, span2Box, textBox));

        assertEquals(divBox, rootBoxNode);
    }

    @Test
    public void generateMixedInlineAndBlockChildrenInsideBlock() {
        // This tests adding the correct anonymous block boxes when a block box contains both inline and block boxes.

        RenderNode div = new RenderNode(HTMLElements.DIV);
        div.id = 0;
        div.style.outerDisplay = DisplayType.BLOCK;
        div.style.innerDisplay = DisplayType.FLOW;

        RenderNode span1 = new RenderNode(HTMLElements.SPAN);
        span1.id = 1;
        span1.style.outerDisplay = DisplayType.INLINE;
        span1.style.innerDisplay = DisplayType.FLOW;
        span1.parent = div;

        RenderNode div1 = new RenderNode(HTMLElements.DIV);
        div1.id = 2;
        div1.style.outerDisplay = DisplayType.BLOCK;
        div1.style.innerDisplay = DisplayType.FLOW;
        div1.parent = div;

        RenderNode span2 = new RenderNode(HTMLElements.SPAN);
        span2.id = 3;
        span2.style.outerDisplay = DisplayType.INLINE;
        span2.style.innerDisplay = DisplayType.FLOW;
        span2.parent = div;

        div.children.addAll(List.of(span1, div1, span2));

        BoxTreeGenerator boxTreeGenerator = new BoxTreeGenerator();
        BoxNode rootBoxNode = boxTreeGenerator.generate(div);

        BoxNode divBox = new BoxNode();
        divBox.id = 0;
        divBox.renderNodeId = div.id;
        divBox.outerDisplayType = DisplayType.BLOCK;
        divBox.innerDisplayType = DisplayType.FLOW;

        BoxNode span1Box = new BoxNode();
        span1Box.id = 1;
        span1Box.renderNodeId = span1.id;
        span1Box.outerDisplayType = DisplayType.INLINE;
        span1Box.innerDisplayType = DisplayType.FLOW;

        BoxNode div1Box = new BoxNode();
        div1Box.id = 2;
        div1Box.renderNodeId = div1.id;
        div1Box.outerDisplayType = DisplayType.BLOCK;
        div1Box.innerDisplayType = DisplayType.FLOW;
        div1Box.parent = divBox;

        BoxNode span2Box = new BoxNode();
        span2Box.id = 3;
        span2Box.renderNodeId = span2.id;
        span2Box.outerDisplayType = DisplayType.INLINE;
        span2Box.innerDisplayType = DisplayType.FLOW;

        BoxNode anonymousBox1 = new BoxNode();
        anonymousBox1.id = 4;
        anonymousBox1.outerDisplayType = DisplayType.BLOCK;
        anonymousBox1.innerDisplayType = DisplayType.FLOW;
        anonymousBox1.isAnonymous = true;
        anonymousBox1.parent = divBox;

        BoxNode anonymousBox2 = new BoxNode();
        anonymousBox2.id = 5;
        anonymousBox2.outerDisplayType = DisplayType.BLOCK;
        anonymousBox2.innerDisplayType = DisplayType.FLOW;
        anonymousBox2.isAnonymous = true;
        anonymousBox2.parent = divBox;

        span1Box.parent = anonymousBox1;
        span2Box.parent = anonymousBox2;

        divBox.children.addAll(List.of(anonymousBox1, div1Box, anonymousBox2));
        anonymousBox1.children.add(span1Box);
        anonymousBox2.children.add(span2Box);

        assertEquals(divBox, rootBoxNode);
    }

    @Test
    public void generateBlockInsideInline() {
        // This tests adding the correct anonymous block boxes when an inline box contains a block box.

        RenderNode span = new RenderNode(HTMLElements.SPAN);
        span.id = 0;
        span.style.outerDisplay = DisplayType.INLINE;
        span.style.innerDisplay = DisplayType.FLOW;

        RenderNode span1 = new RenderNode(HTMLElements.SPAN);
        span1.id = 1;
        span1.style.outerDisplay = DisplayType.INLINE;
        span1.style.innerDisplay = DisplayType.FLOW;
        span1.parent = span;

        RenderNode div = new RenderNode(HTMLElements.DIV);
        div.id = 2;
        div.style.outerDisplay = DisplayType.BLOCK;
        div.style.innerDisplay = DisplayType.FLOW;
        div.parent = span;

        RenderNode text = new RenderNode(HTMLElements.TEXT);
        text.id = 3;
        text.style.outerDisplay = DisplayType.INLINE;
        text.style.innerDisplay = DisplayType.FLOW;
        text.parent = span;

        span.children.addAll(List.of(span1, div, text));

        BoxTreeGenerator boxTreeGenerator = new BoxTreeGenerator();
        BoxNode rootBoxNode = boxTreeGenerator.generate(span);

        BoxNode spanBox = new BoxNode();
        spanBox.id = 0;
        spanBox.renderNodeId = span.id;
        spanBox.outerDisplayType = DisplayType.INLINE;
        spanBox.innerDisplayType = DisplayType.FLOW;

        BoxNode span1Box = new BoxNode();
        span1Box.id = 1;
        span1Box.renderNodeId = span1.id;
        span1Box.outerDisplayType = DisplayType.INLINE;
        span1Box.innerDisplayType = DisplayType.FLOW;

        BoxNode divBox = new BoxNode();
        divBox.id = 2;
        divBox.renderNodeId = div.id;
        divBox.outerDisplayType = DisplayType.BLOCK;
        divBox.innerDisplayType = DisplayType.FLOW;

        BoxNode textBox = new BoxNode();
        textBox.id = 3;
        textBox.renderNodeId = text.id;
        textBox.outerDisplayType = DisplayType.INLINE;
        textBox.innerDisplayType = DisplayType.FLOW;
        textBox.isTextNode = true;
        textBox.isAnonymous = true;

        // The containing anonymous box
        BoxNode anonymousBox1 = new BoxNode();
        anonymousBox1.id = 4;
        anonymousBox1.outerDisplayType = DisplayType.BLOCK;
        anonymousBox1.innerDisplayType = DisplayType.FLOW;
        anonymousBox1.isAnonymous = true;

        // The anonymous box containing span1
        BoxNode anonymousBox2 = new BoxNode();
        anonymousBox2.id = 5;
        anonymousBox2.outerDisplayType = DisplayType.BLOCK;
        anonymousBox2.innerDisplayType = DisplayType.FLOW;
        anonymousBox2.isAnonymous = true;
        anonymousBox2.parent = anonymousBox1;

        // The anonymous box containing text
        BoxNode anonymousBox3 = new BoxNode();
        anonymousBox3.id = 6;
        anonymousBox3.outerDisplayType = DisplayType.BLOCK;
        anonymousBox3.innerDisplayType = DisplayType.FLOW;
        anonymousBox3.isAnonymous = true;
        anonymousBox3.parent = anonymousBox1;

        anonymousBox1.children.addAll(List.of(anonymousBox2, divBox, anonymousBox3));
        anonymousBox2.children.add(span1Box);
        anonymousBox3.children.add(textBox);

        span1Box.parent = anonymousBox2;
        divBox.parent = anonymousBox1;
        textBox.parent = anonymousBox3;

        assertEquals(anonymousBox1, rootBoxNode);
    }

    /**
     * This test tests a messier situation of both mixed display types and block elements inside inline elements.
     * <div1>
     *     <span1>
     *         <div2></div>
     *         <span2></span>
     *     </span>
     *     <div3>
     *         <span3></span>
     *         <div4></div>
     *     </div>
     * </div>
     *
     * should be transformed into:
     *
     * <div1>
     *     <anonymous1>
     *         <anonymous3>
     *             <div2></div2>
     *             <anonymous4>
     *                 <span2></span2>
     *             </anonymous4>
     *         </anonymous3>
     *     </anonymous1>
     *     <div3>
     *         <anonymous2>
     *             <span3></span3>
     *         </anonymous2>
     *         <div4></div4>
     *     </div3>
     * </div1>
     */
    @Test
    public void generateNestedMixedBoxTypes() {
        RenderNode div1 = new RenderNode(HTMLElements.DIV);
        div1.id = 0;
        div1.style.outerDisplay = DisplayType.BLOCK;
        div1.style.innerDisplay = DisplayType.FLOW;

        RenderNode span1 = new RenderNode(HTMLElements.SPAN);
        span1.id = 1;
        span1.style.outerDisplay = DisplayType.INLINE;
        span1.style.innerDisplay = DisplayType.FLOW;
        span1.parent = div1;

        RenderNode div3 = new RenderNode(HTMLElements.DIV);
        div3.id = 2;
        div3.style.outerDisplay = DisplayType.BLOCK;
        div3.style.innerDisplay = DisplayType.FLOW;
        div3.parent = div1;

        RenderNode div2 = new RenderNode(HTMLElements.DIV);
        div2.id = 3;
        div2.style.outerDisplay = DisplayType.BLOCK;
        div2.style.innerDisplay = DisplayType.FLOW;
        div2.parent = span1;

        RenderNode span2 = new RenderNode(HTMLElements.SPAN);
        span2.id = 4;
        span2.style.outerDisplay = DisplayType.INLINE;
        span2.style.innerDisplay = DisplayType.FLOW;
        span2.parent = span1;

        RenderNode span3 = new RenderNode(HTMLElements.SPAN);
        span3.id = 5;
        span3.style.outerDisplay = DisplayType.INLINE;
        span3.style.innerDisplay = DisplayType.FLOW;
        span3.parent = div3;

        RenderNode div4 = new RenderNode(HTMLElements.DIV);
        div4.id = 6;
        div4.style.outerDisplay = DisplayType.BLOCK;
        div4.style.innerDisplay = DisplayType.FLOW;
        div4.parent = div3;

        div1.children.addAll(List.of(span1, div3));
        span1.children.addAll(List.of(div2, span2));
        div3.children.addAll(List.of(span3, div4));

        BoxTreeGenerator boxTreeGenerator = new BoxTreeGenerator();
        BoxNode rootBoxNode = boxTreeGenerator.generate(div1);

        BoxNode div1Box = new BoxNode();
        div1Box.id = 0;
        div1Box.renderNodeId = div1.id;
        div1Box.outerDisplayType = DisplayType.BLOCK;
        div1Box.innerDisplayType = DisplayType.FLOW;

        // Skip span1Box, it is replaced by anonymous box 2

        BoxNode div3Box = new BoxNode();
        div3Box.id = 2;
        div3Box.renderNodeId = div3.id;
        div3Box.outerDisplayType = DisplayType.BLOCK;
        div3Box.innerDisplayType = DisplayType.FLOW;
        div3Box.parent = div1Box;

        BoxNode div2Box = new BoxNode();
        div2Box.id = 3;
        div2Box.renderNodeId = div2.id;
        div2Box.outerDisplayType = DisplayType.BLOCK;
        div2Box.innerDisplayType = DisplayType.FLOW;

        BoxNode span2Box = new BoxNode();
        span2Box.id = 4;
        span2Box.renderNodeId = span2.id;
        span2Box.outerDisplayType = DisplayType.INLINE;
        span2Box.innerDisplayType = DisplayType.FLOW;

        BoxNode span3Box = new BoxNode();
        span3Box.id = 5;
        span3Box.renderNodeId = span3.id;
        span3Box.outerDisplayType = DisplayType.INLINE;
        span3Box.innerDisplayType = DisplayType.FLOW;

        BoxNode div4Box = new BoxNode();
        div4Box.id = 6;
        div4Box.renderNodeId = div4.id;
        div4Box.outerDisplayType = DisplayType.BLOCK;
        div4Box.innerDisplayType = DisplayType.FLOW;
        div4Box.parent = div3Box;

        BoxNode anonymousBox1 = new BoxNode();
        anonymousBox1.id = 7;
        anonymousBox1.outerDisplayType = DisplayType.BLOCK;
        anonymousBox1.innerDisplayType = DisplayType.FLOW;
        anonymousBox1.isAnonymous = true;
        anonymousBox1.parent = div1Box;

        BoxNode anonymousBox2 = new BoxNode();
        anonymousBox2.id = 8;
        anonymousBox2.outerDisplayType = DisplayType.BLOCK;
        anonymousBox2.innerDisplayType = DisplayType.FLOW;
        anonymousBox2.isAnonymous = true;
        anonymousBox2.parent = div3Box;

        BoxNode anonymousBox3 = new BoxNode();
        anonymousBox3.id = 9;
        anonymousBox3.outerDisplayType = DisplayType.BLOCK;
        anonymousBox3.innerDisplayType = DisplayType.FLOW;
        anonymousBox3.isAnonymous = true;
        anonymousBox3.parent = anonymousBox1;

        BoxNode anonymousBox4 = new BoxNode();
        anonymousBox4.id = 10;
        anonymousBox4.outerDisplayType = DisplayType.BLOCK;
        anonymousBox4.innerDisplayType = DisplayType.FLOW;
        anonymousBox4.isAnonymous = true;
        anonymousBox4.parent = anonymousBox3;

        div2Box.parent = anonymousBox3;
        span2Box.parent = anonymousBox4;
        span3Box.parent = anonymousBox2;

        div1Box.children.addAll(List.of(anonymousBox1, div3Box));
        anonymousBox1.children.add(anonymousBox3);
        anonymousBox3.children.addAll(List.of(div2Box, anonymousBox4));
        anonymousBox4.children.add(span2Box);
        div3Box.children.addAll(List.of(anonymousBox2, div4Box));
        anonymousBox2.children.add(span3Box);

        assertEquals(div1Box, rootBoxNode);
    }

    /**
     * This test covers the situation of an inline box containing a block box, but the inline is itself contained
     * in several inline boxes.
     *
     * <div1>
     *     <span1>
     *         <span2>
     *             some text
     *             <span3>
     *                 <div2></div>
     *             </span3>
     *         </span2>
     *     </span1>
     * </div1>
     *
     * -->
     *
     * <div1>
     *     <anon4>
     *         <anon3>
     *             <anon2>
     *                 some text
     *             </anon2>
     *             <anon1>
     *                 <div2></div2>
     *             </anon1>
     *         </anon3>
     *     </anon4>
     * </div1>
     *
     */
    @Test
    public void generatedNestedInlinesContainingBlock() {
        RenderNode div1 = new RenderNode(HTMLElements.DIV);
        div1.id = 0;
        div1.style.outerDisplay = DisplayType.BLOCK;
        div1.style.innerDisplay = DisplayType.FLOW;

        RenderNode span1 = new RenderNode(HTMLElements.SPAN);
        span1.id = 1;
        span1.style.outerDisplay = DisplayType.INLINE;
        span1.style.innerDisplay = DisplayType.FLOW;
        span1.parent = div1;

        RenderNode span2 = new RenderNode(HTMLElements.SPAN);
        span2.id = 2;
        span2.style.outerDisplay = DisplayType.INLINE;
        span2.style.innerDisplay = DisplayType.FLOW;
        span2.parent = span1;

        RenderNode text = new RenderNode(HTMLElements.TEXT);
        text.id = 3;
        text.style.outerDisplay = DisplayType.INLINE;
        text.style.innerDisplay = DisplayType.FLOW;
        text.parent = span2;

        RenderNode span3 = new RenderNode(HTMLElements.SPAN);
        span3.id = 4;
        span3.style.outerDisplay = DisplayType.INLINE;
        span3.style.innerDisplay = DisplayType.FLOW;
        span3.parent = span2;

        RenderNode div2 = new RenderNode(HTMLElements.DIV);
        div2.id = 5;
        div2.style.outerDisplay = DisplayType.BLOCK;
        div2.style.innerDisplay = DisplayType.FLOW;
        div2.parent = span3;

        div1.children.add(span1);
        span1.children.add(span2);
        span2.children.addAll(List.of(text, span3));
        span3.children.add(div2);

        BoxTreeGenerator boxTreeGenerator = new BoxTreeGenerator();
        BoxNode rootBoxNode = boxTreeGenerator.generate(div1);

        BoxNode div1Box = new BoxNode();
        div1Box.id = 0;
        div1Box.renderNodeId = div1.id;
        div1Box.outerDisplayType = DisplayType.BLOCK;
        div1Box.innerDisplayType = DisplayType.FLOW;

        BoxNode textBox = new BoxNode();
        textBox.id = text.id;
        textBox.renderNodeId = text.id;
        textBox.outerDisplayType = DisplayType.INLINE;
        textBox.innerDisplayType = DisplayType.FLOW;
        textBox.isAnonymous = true;
        textBox.isTextNode = true;

        BoxNode div2Box = new BoxNode();
        div2Box.id = div2.id;
        div2Box.renderNodeId = div2.id;
        div2Box.outerDisplayType = DisplayType.BLOCK;
        div2Box.innerDisplayType = DisplayType.FLOW;

        BoxNode anonymousBox1 = new BoxNode();
        anonymousBox1.id = 6;
        anonymousBox1.outerDisplayType = DisplayType.BLOCK;
        anonymousBox1.innerDisplayType = DisplayType.FLOW;
        anonymousBox1.isAnonymous = true;

        BoxNode anonymousBox3 = new BoxNode();
        anonymousBox3.id = 7;
        anonymousBox3.outerDisplayType = DisplayType.BLOCK;
        anonymousBox3.innerDisplayType = DisplayType.FLOW;
        anonymousBox3.isAnonymous = true;

        BoxNode anonymousBox2 = new BoxNode();
        anonymousBox2.id = 8;
        anonymousBox2.outerDisplayType = DisplayType.BLOCK;
        anonymousBox2.innerDisplayType = DisplayType.FLOW;
        anonymousBox2.isAnonymous = true;

        BoxNode anonymousBox4 = new BoxNode();
        anonymousBox4.id = 9;
        anonymousBox4.outerDisplayType = DisplayType.BLOCK;
        anonymousBox4.innerDisplayType = DisplayType.FLOW;
        anonymousBox4.isAnonymous = true;

        div2Box.parent = anonymousBox1;
        textBox.parent = anonymousBox2;
        anonymousBox1.parent = anonymousBox3;
        anonymousBox2.parent = anonymousBox3;
        anonymousBox3.parent = anonymousBox4;
        anonymousBox4.parent = div1Box;

        anonymousBox1.children.add(div2Box);
        anonymousBox2.children.add(textBox);
        anonymousBox3.children.addAll(List.of(anonymousBox2, anonymousBox1));
        anonymousBox4.children.add(anonymousBox3);
        div1Box.children.add(anonymousBox4);

        assertEquals(div1Box, rootBoxNode);
    }

}
