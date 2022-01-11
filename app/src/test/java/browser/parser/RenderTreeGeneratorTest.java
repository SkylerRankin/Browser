package browser.parser;

import org.junit.Test;

import browser.model.DOMNode;
import browser.model.RenderNode;
import browser.parser.HTMLElements;
import browser.parser.RenderTreeGenerator;

public class RenderTreeGeneratorTest {

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



}
