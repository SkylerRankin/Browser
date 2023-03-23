package browser.app;

import static browser.constants.MathConstants.DELTA;
import static org.junit.Assert.assertEquals;

import browser.app.Pipeline;
import browser.exception.LayoutException;
import browser.model.DOMNode;
import browser.model.RenderNode;
import browser.parser.HTMLElements;

import org.junit.Before;
import org.junit.Test;

public class PipelineTest {

    // Layout tests for lists

    @Before
    public void setup() {
        Pipeline.init();
    }

    @Test
    public void calculateLayoutTest_SimpleDiv() throws LayoutException {
        final int screenWidth = 100;
        final int divHeight = 50;
        final int bodyPadding = 10;

        DOMNode domRoot = new DOMNode("root");
        DOMNode domBody = new DOMNode(HTMLElements.BODY);
        domBody.attributes.put("style", String.format("padding: %s;", bodyPadding));
        DOMNode domDiv = new DOMNode(HTMLElements.DIV);
        domDiv.attributes.put("style", String.format("height: %dpx;", divHeight));

        domRoot.addChild(domBody);
        domBody.addChild(domDiv);

        Pipeline pipeline = new Pipeline();
        pipeline.setDomRoot(domRoot);
        pipeline.calculateLayout(screenWidth);

        RenderNode body = pipeline.getRootRenderNode();

        assertEquals(1, body.children.size());
        assertEquals(0, body.children.get(0).children.size());

        RenderNode div = body.children.get(0);

        // Check body position and size
        assertEquals(0, body.box.x, DELTA);
        assertEquals(0, body.box.y, DELTA);
        assertEquals(screenWidth, body.box.width, DELTA);
        assertEquals(divHeight + 2 * bodyPadding, body.box.height, DELTA);

        // Check div position and size
        assertEquals(bodyPadding, div.box.x, DELTA);
        assertEquals(bodyPadding, div.box.y, DELTA);
        assertEquals(screenWidth - 2 * bodyPadding, div.box.width, DELTA);
        assertEquals(divHeight, div.box.height, DELTA);
    }

    @Test
    public void calculateLayoutTest_NestedDivWithFixedWidth() throws LayoutException {
        final int screenWidth = 1000;
        final int div1Width = 600;
        final int div1Height = 600;
        final int div2Height = 100;
        final int div3Width = 300;
        final int div3Height = 300;
        final int bodyPadding = 100;

        DOMNode domRoot = new DOMNode("root");
        DOMNode domBody = new DOMNode(HTMLElements.BODY);
        domBody.attributes.put("style", String.format("padding: %d;", bodyPadding));
        DOMNode domDiv1 = new DOMNode(HTMLElements.DIV);
        domDiv1.attributes.put("style", String.format("width: %d; height: %dpx;", div1Width, div1Height));
        DOMNode domDiv2 = new DOMNode(HTMLElements.DIV);
        domDiv2.attributes.put("style", String.format("height: %dpx;", div2Height));
        DOMNode domDiv3 = new DOMNode(HTMLElements.DIV);
        domDiv3.attributes.put("style", String.format("width: %dpx; height: %dpx;", div3Width, div3Height));

        domRoot.addChild(domBody);
        domBody.addChildren(domDiv1, domDiv3);
        domDiv1.addChild(domDiv2);

        Pipeline pipeline = new Pipeline();
        pipeline.setDomRoot(domRoot);
        pipeline.calculateLayout(screenWidth);

        RenderNode body = pipeline.getRootRenderNode();

        assertEquals(2, body.children.size());
        assertEquals(1, body.children.get(0).children.size());
        assertEquals(0, body.children.get(0).children.get(0).children.size());
        assertEquals(0, body.children.get(1).children.size());

        RenderNode div1 = body.children.get(0);
        RenderNode div2 = div1.children.get(0);
        RenderNode div3 = body.children.get(1);

        // Check body position and size
        assertEquals(0, body.box.x, DELTA);
        assertEquals(0, body.box.y, DELTA);
        assertEquals(screenWidth, body.box.width, DELTA);
        assertEquals(div1Height + div3Height + 2 * bodyPadding, body.box.height, DELTA);

        // Check div1 position and size
        assertEquals(bodyPadding, div1.box.x, DELTA);
        assertEquals(bodyPadding, div1.box.y, DELTA);
        assertEquals(div1Width, div1.box.width, DELTA);
        assertEquals(div1Height, div1.box.height, DELTA);

        // Check div2 position and size
        assertEquals(bodyPadding, div2.box.x, DELTA);
        assertEquals(bodyPadding, div2.box.y, DELTA);
        assertEquals(div1Width, div2.box.width, DELTA);
        assertEquals(div2Height, div2.box.height, DELTA);

        // Check div3 position and size
        assertEquals(bodyPadding, div3.box.x, DELTA);
        assertEquals(bodyPadding + div1Height, div3.box.y, DELTA);
        assertEquals(div3Width, div3.box.width, DELTA);
        assertEquals(div3Height, div3.box.height, DELTA);

    }

    @Test
    public void calculateLayoutTest_SimpleList() throws LayoutException {
        final int screenWidth = 100;
        final int divHeight = 50;
        final int bodyPadding = 10;
        final int olLeftPadding = 40;

        DOMNode domRoot = new DOMNode("root");
        DOMNode domBody = new DOMNode(HTMLElements.BODY);
        domBody.attributes.put("style", String.format("padding: %s;", bodyPadding));
        DOMNode domOL = new DOMNode(HTMLElements.OL);
        domBody.attributes.put("style", String.format("padding-left: %d;", olLeftPadding));
        DOMNode domLI1 = new DOMNode(HTMLElements.LI);
        DOMNode domLI2 = new DOMNode(HTMLElements.LI);

        domRoot.addChild(domBody);
        domBody.addChild(domOL);
        domOL.addChildren(domLI1, domLI2);

        Pipeline pipeline = new Pipeline();
        pipeline.setDomRoot(domRoot);
        pipeline.calculateLayout(screenWidth);

        RenderNode body = pipeline.getRootRenderNode();

        assertEquals(1, body.children.size());
        assertEquals(0, body.children.get(0).children.size());

        RenderNode div = body.children.get(0);

        // Check body position and size
        assertEquals(0, body.box.x, DELTA);
        assertEquals(0, body.box.y, DELTA);
        assertEquals(screenWidth, body.box.width, DELTA);
        assertEquals(divHeight + 2 * bodyPadding, body.box.height, DELTA);

        // Check div position and size
        assertEquals(bodyPadding, div.box.x, DELTA);
        assertEquals(bodyPadding, div.box.y, DELTA);
        assertEquals(screenWidth - 2 * bodyPadding, div.box.width, DELTA);
        assertEquals(divHeight, div.box.height, DELTA);
    }

}
