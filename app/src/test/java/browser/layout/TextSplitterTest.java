package browser.layout;

import browser.css.CSSStyle;
import browser.model.RenderNode;

public class TextSplitterTest {

    public RenderNode singleLevelTree() {
        RenderNode p = new RenderNode("p");
        RenderNode text1 = new RenderNode("text");
        RenderNode b1 = new RenderNode("b");
        RenderNode text2 = new RenderNode("text");
        RenderNode b2 = new RenderNode("b");
        RenderNode span = new RenderNode("span");
        RenderNode text3 = new RenderNode("text");

        CSSStyle style = new CSSStyle();
        style.fontFamily = "Times New Roman";
        style.fontSize = 12;

        text1.style = style;
        b1.style = style;
        text2.style = style;
        b2.style = style;
        span.style = style;
        text3.style = style;

        p.maxWidth = 100f;

        text1.text = "Times New Roman";            // Width = 93.32
        b1.text = "is a font";                    // Width = 40.67
        text2.text = "Testing";                    // Width = 35.99
        b2.text = "some bold test";                // Width = 72.66
        span.text = "span";                        // Width = 22.00
        text3.text = "<end>";                    // Width = 30.86

        /*

         Expected line splits

         [Times New Roman (93.32)][i (3.33)]                    (96.65)
         [s a font (37.34)][Testing (35.99)][some (25.99)]        (99.32)
         [ bold test (46.67)][span (22.00)][<end> (30.86)]        (99.53)

         */

        p.id = 0;            p.depth = 0;
        text1.id = 1;        text1.depth = 1;
        b1.id = 2;            b1.depth = 1;
        text2.id = 3;        text2.depth = 1;
        b2.id = 4;            b2.depth = 1;
        span.id = 5;        span.depth = 1;
        text3.id = 6;        text3.depth = 1;

        p.children.add(text1);
        p.children.add(b1);
        p.children.add(text2);
        p.children.add(b2);
        p.children.add(span);
        p.children.add(text3);

        return p;
    }


}
