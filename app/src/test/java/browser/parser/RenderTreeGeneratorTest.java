package browser.parser;

import static org.junit.Assert.assertEquals;

import browser.app.Pipeline;
import browser.css.CSSStyle;
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
        div.id = RenderNode.nextId++;

        RenderNode text1 = new RenderNode(HTMLElements.TEXT);
        text1.style.outerDisplay = CSSStyle.DisplayType.INLINE;
        text1.text = "  This text is ";
        text1.id = RenderNode.nextId++;

        RenderNode b = new RenderNode(HTMLElements.B);
        b.style.outerDisplay = CSSStyle.DisplayType.INLINE;
        b.id = RenderNode.nextId++;

        RenderNode text2 = new RenderNode(HTMLElements.TEXT);
        text2.style.outerDisplay = CSSStyle.DisplayType.INLINE;
        text2.text = "bold";
        text2.id = RenderNode.nextId++;

        RenderNode text3 = new RenderNode(HTMLElements.TEXT);
        text3.style.outerDisplay = CSSStyle.DisplayType.INLINE;
        text3.text = " text for testing. ";
        text3.id = RenderNode.nextId++;

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
        div.id = RenderNode.nextId++;

        RenderNode text1 = new RenderNode(HTMLElements.TEXT);
        text1.style.outerDisplay = CSSStyle.DisplayType.INLINE;
        text1.text = "  Start ";
        text1.id = RenderNode.nextId++;

        RenderNode b1 = new RenderNode(HTMLElements.B);
        b1.style.outerDisplay = CSSStyle.DisplayType.INLINE;
        b1.id = RenderNode.nextId++;

        RenderNode text2 = new RenderNode(HTMLElements.TEXT);
        text2.style.outerDisplay = CSSStyle.DisplayType.INLINE;
        text2.text = "b1";
        text2.id = RenderNode.nextId++;

        RenderNode text3 = new RenderNode(HTMLElements.TEXT);
        text3.style.outerDisplay = CSSStyle.DisplayType.INLINE;
        text3.text = ", ";
        text3.id = RenderNode.nextId++;

        RenderNode b2 = new RenderNode(HTMLElements.B);
        b2.style.outerDisplay = CSSStyle.DisplayType.INLINE;
        b2.id = RenderNode.nextId++;

        RenderNode text4 = new RenderNode(HTMLElements.TEXT);
        text4.style.outerDisplay = CSSStyle.DisplayType.INLINE;
        text4.text = "b2";
        text4.id = RenderNode.nextId++;

        RenderNode text5 = new RenderNode(HTMLElements.TEXT);
        text5.style.outerDisplay = CSSStyle.DisplayType.INLINE;
        text5.text = ", and ";
        text5.id = RenderNode.nextId++;

        RenderNode b3 = new RenderNode(HTMLElements.B);
        b3.style.outerDisplay = CSSStyle.DisplayType.INLINE;
        b3.id = RenderNode.nextId++;

        RenderNode text6 = new RenderNode(HTMLElements.TEXT);
        text6.style.outerDisplay = CSSStyle.DisplayType.INLINE;
        text6.text = "b3";
        text6.id = RenderNode.nextId++;

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
    public void trimTextWhitespace_spaceAfterText() {
        // "<div> a <code> b  </code></div>"

        RenderNode div = new RenderNode(HTMLElements.DIV);
        div.id = RenderNode.nextId++;

        RenderNode text1 = new RenderNode(HTMLElements.TEXT);
        text1.id = RenderNode.nextId++;
        text1.style.outerDisplay = CSSStyle.DisplayType.INLINE;
        text1.text = " a ";

        RenderNode code = new RenderNode(HTMLElements.CODE);
        code.id = RenderNode.nextId++;
        code.style.outerDisplay = CSSStyle.DisplayType.INLINE;

        RenderNode text2 = new RenderNode(HTMLElements.TEXT);
        text2.id = RenderNode.nextId++;
        text2.style.outerDisplay = CSSStyle.DisplayType.INLINE;
        text2.text = "b";

        div.addChildren(text1, code);
        code.addChild(text2);

        renderTreeGenerator.trimTextWhitespace(div);

        assertEquals("a ", text1.text);
        assertEquals("b", text2.text);
    }

}
