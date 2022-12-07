package browser.layout;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import browser.app.Pipeline;
import browser.css.CSSStyle;
import browser.model.BoxNode;
import browser.model.RenderNode;
import browser.model.Vector2;
import browser.parser.HTMLElements;
import browser.util.TestDataLoader;

import org.junit.Before;
import org.junit.Test;

public class InlineLayoutFormatterTest {

    private static final int TEST_LETTER_WIDTH = 1;

    private InlineLayoutFormatter inlineLayoutFormatter;

    @Before
    public void setup() {
        Pipeline.init();
        TextDimensionCalculator textDimensionCalculator = mock(TextDimensionCalculator.class);
        when(textDimensionCalculator.getDimension(anyString(), any()))
                .thenAnswer(i -> {
                    float x = i.getArgument(0, String.class).length() * TEST_LETTER_WIDTH;
                    return new Vector2(x, 1);
                });
        inlineLayoutFormatter = new InlineLayoutFormatter(textDimensionCalculator);
    }

    @Test
    public void singleLineSpansTest() {
        RenderNode root = new RenderNode(HTMLElements.DIV);
        root.style = new CSSStyle();
        RenderNode span1 = new RenderNode(HTMLElements.SPAN);
        span1.style = new CSSStyle();
        RenderNode text1 = new RenderNode(HTMLElements.TEXT);
        text1.text = "text1";
        text1.style = new CSSStyle();
        RenderNode span2 = new RenderNode(HTMLElements.SPAN);
        span2.style = new CSSStyle();
        RenderNode text2 = new RenderNode(HTMLElements.TEXT);
        text2.text = "text2";
        text2.style = new CSSStyle();
        root.children.addAll(List.of(span1, span2));
        span1.children.add(text1);
        span2.children.add(text2);

        BoxNode rootBox = new BoxNode();
        rootBox.id = 0;
        rootBox.correspondingRenderNode = root;
        rootBox.innerDisplayType = CSSStyle.DisplayType.FLOW;
        rootBox.x = 0f;
        rootBox.y = 0f;
        rootBox.width = 50f;
        BoxNode span1Box = new BoxNode();
        span1Box.id = 1;
        span1Box.correspondingRenderNode = span1;
        span1Box.parent = rootBox;
        span1Box.innerDisplayType = CSSStyle.DisplayType.FLOW;
        BoxNode span2Box = new BoxNode();
        span2Box.id = 2;
        span2Box.correspondingRenderNode = span2;
        span2Box.parent = rootBox;
        span2Box.innerDisplayType = CSSStyle.DisplayType.FLOW;
        BoxNode text1Box = new BoxNode();
        text1Box.id = 3;
        text1Box.isTextNode = true;
        text1Box.isAnonymous = true;
        text1Box.correspondingRenderNode = text1;
        text1Box.parent = span1Box;
        text1Box.innerDisplayType = CSSStyle.DisplayType.FLOW;
        BoxNode text2Box = new BoxNode();
        text2Box.id = 4;
        text2Box.isTextNode = true;
        text2Box.isAnonymous = true;
        text2Box.correspondingRenderNode = text2;
        text2Box.parent = span2Box;
        text2Box.innerDisplayType = CSSStyle.DisplayType.FLOW;

        rootBox.children.addAll(List.of(span1Box, span2Box));
        span1Box.children.add(text1Box);
        span2Box.children.add(text2Box);

        InlineFormattingContext context = new InlineFormattingContext(0, 50, 0);
        context.initialize(rootBox);

        System.out.println(rootBox.toRecursiveString());

        // Place the first span
        inlineLayoutFormatter.placeBox(span1Box, context);

        System.out.printf("position (%f, %f)\n", span1Box.x, span1Box.y);

        // Place the second span, given first is already placed
//        inlineLayoutFormatter.placeBox(span1Box, context);
    }

    @Test
    public void test() {
        TestDataLoader.TestData testData = TestDataLoader.loadLayoutTrees("singleLineSpans");
        RenderNode rootRenderNode = testData.rootRenderNode;
        BoxNode rootBoxNode = testData.rootBoxNode;

        rootRenderNode.print();
        System.out.println(rootBoxNode.toRecursiveString());
    }

}
