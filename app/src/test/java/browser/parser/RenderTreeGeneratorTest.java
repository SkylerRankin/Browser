package browser.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import browser.app.Pipeline;
import browser.constants.PseudoElementConstants;
import browser.css.CSSStyle;
import browser.model.DOMNode;
import browser.model.RenderNode;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RenderTreeGeneratorTest {

    private RenderTreeGenerator renderTreeGenerator;

    @BeforeClass
    public static void setup() {
        Pipeline.init();
    }

    @Before
    public void perTestSetup() {
        renderTreeGenerator = new RenderTreeGenerator();
        renderTreeGenerator.reset();
    }

    @Test
    public void domTreeToRenderTreeTest() {
        DOMNode domTree = new DOMNode("root");
        DOMNode doctype = new DOMNode(HTMLElements.DOCTYPE);
        doctype.attributes.put("html", null);
        DOMNode html = new DOMNode(HTMLElements.HTML);
        DOMNode head = new DOMNode(HTMLElements.HEAD);
        DOMNode title = new DOMNode(HTMLElements.TITLE);
        DOMNode titleText = new DOMNode(HTMLElements.TEXT);
        titleText.content = "Watchmen";
        DOMNode body = new DOMNode(HTMLElements.BODY);
        DOMNode h1 = new DOMNode(HTMLElements.H1);
        h1.attributes.put("id", "title");
        DOMNode h1Text = new DOMNode(HTMLElements.TEXT);
        h1Text.content = "Rorschach's Journal";
        DOMNode div = new DOMNode(HTMLElements.DIV);
        DOMNode input = new DOMNode(HTMLElements.INPUT);
        input.attributes.put("disabled", null);
        DOMNode h2 = new DOMNode(HTMLElements.H2);
        h2.attributes.put("class", "centered linked");
        DOMNode h2Text = new DOMNode(HTMLElements.TEXT);
        h2Text.content = "October 12th, 1985";
        DOMNode p = new DOMNode(HTMLElements.P);
        DOMNode pText = new DOMNode(HTMLElements.TEXT);
        pText.content = "Tonight, a comedian died in New York.";
        domTree.addChild(doctype);
        domTree.addChild(html);
        html.addChild(head);
        html.addChild(body);
        head.addChild(title);
        title.addChild(titleText);
        body.addChild(h1);
        h1.addChild(h1Text);
        body.addChild(div);
        div.addChild(input);
        div.addChild(h2);
        h2.addChild(h2Text);
        div.addChild(p);
        p.addChild(pText);

        RenderTreeGenerator rtg = new RenderTreeGenerator();
        RenderNode renderTree = rtg.domTreeToRenderTree(domTree);

    }

    @Test
    public void transformNode_BasicListTest() {
        int id = 0;
        RenderNode renderRoot = new RenderNode("root");
        renderRoot.id = id++;
        renderRoot.depth = 0;
        RenderNode body = new RenderNode(HTMLElements.BODY);
        body.id = id++;
        body.depth = 1;
        RenderNode ol = new RenderNode(HTMLElements.OL);
        ol.id = id++;
        ol.depth = 2;
        RenderNode li1 = new RenderNode(HTMLElements.LI);
        li1.id = id++;
        li1.depth = 3;
        RenderNode text1 = new RenderNode(HTMLElements.TEXT);
        text1.id = id++;
        text1.depth = 4;
        text1.text = "first list item";
        RenderNode li2 = new RenderNode(HTMLElements.LI);
        li2.id = id++;
        li2.depth = 3;
        RenderNode text2 = new RenderNode(HTMLElements.TEXT);
        text2.id = id++;
        text2.depth = 4;
        text2.text = "second list item";
        RenderNode li3 = new RenderNode(HTMLElements.LI);
        li3.id = id++;
        li3.depth = 3;
        RenderNode text3 = new RenderNode(HTMLElements.TEXT);
        text3.id = id++;
        text3.depth = 4;
        text3.text = "third list item";

        renderRoot.addChild(body);
        body.addChild(ol);
        ol.addChildren(li1, li2, li3);
        li1.addChild(text1);
        li2.addChild(text2);
        li3.addChild(text3);

        id = 0;

        RenderNode expectedRenderNode = new RenderNode("root");
        expectedRenderNode.id = id++;
        expectedRenderNode.depth = 0;
        RenderNode renderBody = new RenderNode(HTMLElements.BODY);
        renderBody.id = id++;
        renderBody.depth = 1;
        RenderNode renderOL = new RenderNode(HTMLElements.OL);
        renderOL.id = id++;
        renderOL.depth = 2;

        RenderNode renderMarker1 = new RenderNode(HTMLElements.PSEUDO_MARKER);
        renderMarker1.depth = 3;
        renderMarker1.style.width = PseudoElementConstants.MARKER_WIDTH;
        renderMarker1.style.height = PseudoElementConstants.MARKER_HEIGHT;
        renderMarker1.style.display = PseudoElementConstants.MARKER_DISPLAY_TYPE;
        renderMarker1.attributes.put(PseudoElementConstants.MARKER_INDEX_KEY, "0");
        renderMarker1.attributes.put(PseudoElementConstants.MARKER_TYPE_KEY, "ol");
        RenderNode renderLI1 = new RenderNode(HTMLElements.LI);
        renderLI1.id = id++;
        renderLI1.depth = 3;
        renderLI1.style.display = CSSStyle.DisplayType.INLINE;
        RenderNode renderText1 = new RenderNode(HTMLElements.TEXT);
        renderText1.id = id++;
        renderText1.depth = 4;
        renderText1.text = "first list item";

        RenderNode renderMarker2 = new RenderNode(HTMLElements.PSEUDO_MARKER);
        renderMarker2.depth = 3;
        renderMarker2.style.width = PseudoElementConstants.MARKER_WIDTH;
        renderMarker2.style.height = PseudoElementConstants.MARKER_HEIGHT;
        renderMarker2.style.display = PseudoElementConstants.MARKER_DISPLAY_TYPE;
        renderMarker2.attributes.put(PseudoElementConstants.MARKER_INDEX_KEY, "1");
        renderMarker2.attributes.put(PseudoElementConstants.MARKER_TYPE_KEY, "ol");
        RenderNode renderLI2 = new RenderNode(HTMLElements.LI);
        renderLI2.id = id++;
        renderLI2.depth = 3;
        renderLI2.style.display = CSSStyle.DisplayType.INLINE;
        RenderNode renderText2 = new RenderNode(HTMLElements.TEXT);
        renderText2.id = id++;
        renderText2.depth = 4;
        renderText2.text = "second list item";

        RenderNode renderMarker3 = new RenderNode(HTMLElements.PSEUDO_MARKER);
        renderMarker3.depth = 3;
        renderMarker3.style.width = PseudoElementConstants.MARKER_WIDTH;
        renderMarker3.style.height = PseudoElementConstants.MARKER_HEIGHT;
        renderMarker3.style.display = PseudoElementConstants.MARKER_DISPLAY_TYPE;
        renderMarker3.attributes.put(PseudoElementConstants.MARKER_INDEX_KEY, "2");
        renderMarker3.attributes.put(PseudoElementConstants.MARKER_TYPE_KEY, "ol");
        RenderNode renderLI3 = new RenderNode(HTMLElements.LI);
        renderLI3.id = id++;
        renderLI3.depth = 3;
        renderLI3.style.display = CSSStyle.DisplayType.INLINE;
        RenderNode renderText3 = new RenderNode(HTMLElements.TEXT);
        renderText3.id = id++;
        renderText3.depth = 4;
        renderText3.text = "third list item";

        renderMarker1.id = id;
        renderMarker2.id = id + 1;
        renderMarker3.id = id + 2;

        expectedRenderNode.addChild(renderBody);
        renderBody.addChild(renderOL);
        renderOL.addChildren(renderMarker1, renderLI1, renderMarker2, renderLI2, renderMarker3, renderLI3);
        renderLI1.addChild(renderText1);
        renderLI2.addChild(renderText2);
        renderLI3.addChild(renderText3);

        RenderNode.nextId = id;
        renderTreeGenerator.transformNode(renderRoot);

        assertEquals(expectedRenderNode, renderRoot);
    }

    @Test
    public void transformNode_NestedListTest() {
        int id = 0;
        RenderNode renderRoot = new RenderNode("root");
        renderRoot.id = id++;
        renderRoot.depth = 0;
        RenderNode body = new RenderNode(HTMLElements.BODY);
        body.id = id++;
        body.depth = 1;
        RenderNode ol = new RenderNode(HTMLElements.OL);
        ol.id = id++;
        ol.depth = 2;
        RenderNode li1 = new RenderNode(HTMLElements.LI);
        li1.id = id++;
        li1.depth = 3;
        RenderNode ul = new RenderNode(HTMLElements.UL);
        ul.id = id++;
        ul.depth = 4;
        RenderNode li2 = new RenderNode(HTMLElements.LI);
        li2.id = id++;
        li2.depth = 5;
        RenderNode text1 = new RenderNode(HTMLElements.TEXT);
        text1.id = id++;
        text1.depth = 6;
        text1.text = "first list item";
        RenderNode li3 = new RenderNode(HTMLElements.LI);
        li3.id = id++;
        li3.depth = 5;
        RenderNode text2 = new RenderNode(HTMLElements.TEXT);
        text2.id = id++;
        text2.depth = 6;
        text2.text = "second list item";

        renderRoot.addChild(body);
        body.addChild(ol);
        ol.addChildren(li1);
        li1.addChild(ul);
        ul.addChildren(li2, li3);
        li2.addChild(text1);
        li3.addChild(text2);

        id = 0;

        RenderNode expectedRenderNode = new RenderNode("root");
        expectedRenderNode.id = id++;
        expectedRenderNode.depth = 0;
        RenderNode renderBody = new RenderNode(HTMLElements.BODY);
        renderBody.id = id++;
        renderBody.depth = 1;
        RenderNode renderOL = new RenderNode(HTMLElements.OL);
        renderOL.id = id++;
        renderOL.depth = 2;

        RenderNode renderMarker1 = new RenderNode(HTMLElements.PSEUDO_MARKER);
        renderMarker1.depth = 3;
        renderMarker1.style.width = PseudoElementConstants.MARKER_WIDTH;
        renderMarker1.style.height = PseudoElementConstants.MARKER_HEIGHT;
        renderMarker1.style.display = PseudoElementConstants.MARKER_DISPLAY_TYPE;
        renderMarker1.attributes.put(PseudoElementConstants.MARKER_INDEX_KEY, "0");
        renderMarker1.attributes.put(PseudoElementConstants.MARKER_TYPE_KEY, "ol");
        RenderNode renderLI1 = new RenderNode(HTMLElements.LI);
        renderLI1.id = id++;
        renderLI1.depth = 3;
        renderLI1.style.display = CSSStyle.DisplayType.INLINE;

        RenderNode renderUL = new RenderNode(HTMLElements.UL);
        renderUL.id = id++;
        renderUL.depth = 4;

        RenderNode renderMarker2 = new RenderNode(HTMLElements.PSEUDO_MARKER);
        renderMarker2.depth = 5;
        renderMarker2.style.width = PseudoElementConstants.MARKER_WIDTH;
        renderMarker2.style.height = PseudoElementConstants.MARKER_HEIGHT;
        renderMarker2.style.display = PseudoElementConstants.MARKER_DISPLAY_TYPE;
        renderMarker2.attributes.put(PseudoElementConstants.MARKER_INDEX_KEY, "0");
        renderMarker2.attributes.put(PseudoElementConstants.MARKER_TYPE_KEY, "ul");
        RenderNode renderLI2 = new RenderNode(HTMLElements.LI);
        renderLI2.id = id++;
        renderLI2.depth = 5;
        renderLI2.style.display = CSSStyle.DisplayType.INLINE;
        RenderNode renderText1 = new RenderNode(HTMLElements.TEXT);
        renderText1.id = id++;
        renderText1.depth = 6;
        renderText1.text = "first list item";

        RenderNode renderMarker3 = new RenderNode(HTMLElements.PSEUDO_MARKER);
        renderMarker3.depth = 5;
        renderMarker3.style.width = PseudoElementConstants.MARKER_WIDTH;
        renderMarker3.style.height = PseudoElementConstants.MARKER_HEIGHT;
        renderMarker3.style.display = PseudoElementConstants.MARKER_DISPLAY_TYPE;
        renderMarker3.attributes.put(PseudoElementConstants.MARKER_INDEX_KEY, "1");
        renderMarker3.attributes.put(PseudoElementConstants.MARKER_TYPE_KEY, "ul");
        RenderNode renderLI3 = new RenderNode(HTMLElements.LI);
        renderLI3.id = id++;
        renderLI3.depth = 5;
        renderLI3.style.display = CSSStyle.DisplayType.INLINE;
        RenderNode renderText2 = new RenderNode(HTMLElements.TEXT);
        renderText2.id = id++;
        renderText2.depth = 6;
        renderText2.text = "first list item";

        renderMarker1.id = id;
        renderMarker2.id = id + 1;
        renderMarker3.id = id + 2;

        expectedRenderNode.addChild(renderBody);
        renderBody.addChild(renderOL);
        renderOL.addChildren(renderMarker1, renderLI1);
        renderLI1.addChild(renderUL);
        renderUL.addChildren(renderMarker2, renderLI2, renderMarker3, renderLI3);
        renderLI2.addChild(renderText1);
        renderLI3.addChild(renderText2);
    }

    @Test
    public void removeDuplicateWhitespace_SingleLineWhitespace() {
        RenderNode body = new RenderNode(HTMLElements.BODY);
        RenderNode text1 = new RenderNode(HTMLElements.TEXT);
        text1.text = "   This text is \n";
        RenderNode b = new RenderNode(HTMLElements.B);
        RenderNode text2 = new RenderNode(HTMLElements.TEXT);
        text2.text = "\t\tbold\n";
        RenderNode text3 = new RenderNode(HTMLElements.TEXT);
        text3.text = " text. \n";

        body.addChildren(text1, b, text3);
        b.addChild(text2);

        renderTreeGenerator.removeDuplicateWhitespace(body, false);

        assertEquals(" This text is ", text1.text);
        assertEquals(" bold ", text2.text);
        assertEquals(" text. ", text3.text);
    }

    @Test
    public void trimTextWhitespace_SingleLine() {
        // "  This text is <b>bold</b> for testing. "
        RenderNode div = new RenderNode(HTMLElements.DIV);
        RenderNode text1 = new RenderNode(HTMLElements.TEXT);
        text1.style.display = CSSStyle.DisplayType.INLINE;
        text1.text = "  This text is ";
        RenderNode b = new RenderNode(HTMLElements.B);
        b.style.display = CSSStyle.DisplayType.INLINE;
        RenderNode text2 = new RenderNode(HTMLElements.TEXT);
        text2.style.display = CSSStyle.DisplayType.INLINE;
        text2.text = "bold";
        RenderNode text3 = new RenderNode(HTMLElements.TEXT);
        text3.style.display = CSSStyle.DisplayType.INLINE;
        text3.text = " text for testing. ";

        div.addChildren(text1, b, text3);
        b.addChild(text2);

        renderTreeGenerator.trimTextWhitespace(div);

        assertEquals("This text is ", text1.text);
        assertEquals("bold", text2.text);
        assertEquals(" text for testing.", text3.text);
    }

    @Test
    public void trimTextWhitespace_MultipleBolds() {
        // "  Start <b>b1</b>, <b>b2</b>, and <b>b3</b>"
        RenderNode div = new RenderNode(HTMLElements.DIV);
        RenderNode text1 = new RenderNode(HTMLElements.TEXT);
        text1.style.display = CSSStyle.DisplayType.INLINE;
        text1.text = "  Start ";
        RenderNode b1 = new RenderNode(HTMLElements.B);
        b1.style.display = CSSStyle.DisplayType.INLINE;
        RenderNode text2 = new RenderNode(HTMLElements.TEXT);
        text2.style.display = CSSStyle.DisplayType.INLINE;
        text2.text = "b1";
        RenderNode text3 = new RenderNode(HTMLElements.TEXT);
        text3.style.display = CSSStyle.DisplayType.INLINE;
        text3.text = ", ";
        RenderNode b2 = new RenderNode(HTMLElements.B);
        b2.style.display = CSSStyle.DisplayType.INLINE;
        RenderNode text4 = new RenderNode(HTMLElements.TEXT);
        text4.style.display = CSSStyle.DisplayType.INLINE;
        text4.text = "b2";
        RenderNode text5 = new RenderNode(HTMLElements.TEXT);
        text5.style.display = CSSStyle.DisplayType.INLINE;
        text5.text = ", and ";
        RenderNode b3 = new RenderNode(HTMLElements.B);
        b3.style.display = CSSStyle.DisplayType.INLINE;
        RenderNode text6 = new RenderNode(HTMLElements.TEXT);
        text6.style.display = CSSStyle.DisplayType.INLINE;
        text6.text = "b3";

        div.addChildren(text1, b1, text3, b2, text5, b3);
        b1.addChild(text2);
        b2.addChild(text4);
        b3.addChild(text6);

        renderTreeGenerator.trimTextWhitespace(div);

        assertEquals("Start ", text1.text);
        assertEquals("b1", text2.text);
        assertEquals(", ", text3.text);
        assertEquals("b2", text4.text);
        assertEquals(", and ", text5.text);
        assertEquals("b3", text6.text);

    }

    @Test
    public void getNextInlineTextNode_TextInSpan() {
        RenderNode body = new RenderNode(HTMLElements.BODY);
        RenderNode div = new RenderNode(HTMLElements.DIV);
        RenderNode text1 = new RenderNode(HTMLElements.TEXT);
        text1.style.display = CSSStyle.DisplayType.INLINE;
        RenderNode span1 = new RenderNode(HTMLElements.SPAN);
        span1.style.display = CSSStyle.DisplayType.INLINE;
        RenderNode text2 = new RenderNode(HTMLElements.TEXT);
        text2.style.display = CSSStyle.DisplayType.INLINE;

        body.addChildren(div, text1, span1);
        span1.addChild(text2);

        RenderNode result = renderTreeGenerator.getNextInlineTextNode(div);
        assertEquals(text1, result);

        result = renderTreeGenerator.getNextInlineTextNode(text1);
        assertEquals(text2, result);

        result = renderTreeGenerator.getNextInlineTextNode(span1);
        assertNull(result);
    }

}
