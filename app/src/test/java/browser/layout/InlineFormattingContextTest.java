package browser.layout;

import static browser.constants.MathConstants.DELTA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import browser.app.Pipeline;
import browser.css.CSSStyle;
import browser.model.BoxNode;
import browser.model.RenderNode;
import browser.parser.HTMLElements;

import org.junit.Before;
import org.junit.Test;

public class InlineFormattingContextTest {

    @Before
    public void setup() {
        Pipeline.init();
    }

    /**
     * This box node structure is created to test the right/left spacing is initialized correctly.
     * <root>
     *     <span1>
     *         <span2></span2>
     *     </span1>
     *     <span3>
     *         <span4></span4>
     *         <span5></span5>
     *         <div></div>
     *     </span3>
     * </root>
     */
    @Test
    public void initializeTest() {
        InlineFormattingContext context = new InlineFormattingContext(0, 10, 0);

        BoxNode root = createBoxNode(0, 0, 20, 10, 100, 200);
        root.x = 10f;
        root.y = 50f;
        root.width = 8f;
        root.height = 8f;
        BoxNode span1 = createBoxNode(1, 0, 5, 5, 15, 10);
        BoxNode span2 = createBoxNode(2,0, 10, 10, 3, 2);
        BoxNode span3 = createBoxNode(3,0, 5, 20, 5, 20);
        BoxNode span4 = createBoxNode(4,0, 1, 2, 3, 4);
        BoxNode span5 = createBoxNode(5,0, 10, 20, 20, 10);
        BoxNode div1 = createBoxNode(6,0, 1, 20, 15, 1);
        BoxNode div2 = createBoxNode(7,1, 1, 20, 10, 1);

        root.children.addAll(List.of(span1, span3));
        span1.children.add(span2);
        span1.parent = root;
        span2.parent = span1;
        span3.children.addAll(List.of(span4, span5, div1));
        span3.parent = root;
        span4.parent = span3;
        span5.parent = span3;
        div1.parent = span3;
        div1.children.add(div2);
        div2.parent = div1;

        context.initialize(root);

        assertEquals(10, context.width, DELTA);
        assertEquals(10, context.startX, DELTA);
        assertEquals(18, context.endX, DELTA);

        assertEquals(0, context.getLeftSpacingForBox(0), DELTA);
        assertEquals(0, context.getRightSpacingForBox(0), DELTA);
        assertEquals(35, context.getLeftSpacingForBox(1), DELTA);
        assertEquals(10, context.getRightSpacingForBox(1), DELTA);
        assertEquals(43, context.getLeftSpacingForBox(2), DELTA);
        assertEquals(17, context.getRightSpacingForBox(2), DELTA);
        assertEquals(5, context.getLeftSpacingForBox(3), DELTA);
        assertEquals(30, context.getRightSpacingForBox(3), DELTA);
        assertEquals(13, context.getLeftSpacingForBox(4), DELTA);
        assertEquals(4, context.getRightSpacingForBox(4), DELTA);
        assertEquals(20, context.getLeftSpacingForBox(5), DELTA);
        assertEquals(10, context.getRightSpacingForBox(5), DELTA);
        assertEquals(15, context.getLeftSpacingForBox(6), DELTA);
        assertEquals(51, context.getRightSpacingForBox(6), DELTA);
        assertEquals(0, context.getLeftSpacingForBox(7), DELTA);
        assertEquals(0, context.getRightSpacingForBox(7), DELTA);
    }

    private BoxNode createBoxNode(int id, int contextId, int paddingLeft, int paddingRight, int marginLeft, int marginRight) {
        RenderNode renderNode = new RenderNode(HTMLElements.DIV);
        CSSStyle style = new CSSStyle();
        style.paddingLeft = paddingLeft;
        style.paddingRight = paddingRight;
        style.marginLeft = marginLeft;
        style.marginRight = marginRight;
        renderNode.style = style;
        BoxNode boxNode = new BoxNode();
        boxNode.id = id;
        boxNode.correspondingRenderNode = renderNode;
        boxNode.inlineFormattingContextId = contextId;
        return boxNode;
    }

}
