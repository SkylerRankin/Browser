package browser.layout;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import browser.app.Pipeline;
import browser.model.BoxNode;
import browser.model.RenderNode;
import browser.model.Vector2;
import browser.parser.HTMLElements;

import org.junit.Before;
import org.junit.Test;

public class TextNodeSplitterTest {

    private static final int TEST_LETTER_WIDTH = 1;

    private TextNodeSplitter textNodeSplitter;

    @Before()
    public void setup() {
        Pipeline.init();
        TextDimensionCalculator textDimensionCalculator = mock(TextDimensionCalculator.class);
        when(textDimensionCalculator.getDimension(anyString(), any()))
                .thenAnswer(i -> {
                    float x = i.getArgument(0, String.class).length() * TEST_LETTER_WIDTH;
                    return new Vector2(x, 1);
                });
        textNodeSplitter = new TextNodeSplitter(textDimensionCalculator);
    }

    @Test
    public void noBreaksRequiredTest() {
        RenderNode renderNode = new RenderNode(HTMLElements.TEXT);
        renderNode.text = "1 22 3 4";
        BoxNode boxNode = new BoxNode();
        boxNode.isTextNode = true;
        boxNode.correspondingRenderNode = renderNode;
        float width = 10;
        List<BoxNode> results = textNodeSplitter.split(boxNode, width, width);
        assertEquals(1, results.size());
        assertEquals(0, results.get(0).textStartIndex);
        assertEquals(8, results.get(0).textEndIndex);
    }

    @Test
    public void requiresBreaksTest() {
        RenderNode renderNode = new RenderNode(HTMLElements.TEXT);
        renderNode.text = "first word second word third word";
        BoxNode boxNode = new BoxNode();
        boxNode.isTextNode = true;
        boxNode.correspondingRenderNode = renderNode;
        float width = 10;
        List<BoxNode> results = textNodeSplitter.split(boxNode, width, width);
        assertEquals(4, results.size());
        assertEquals("first word", renderNode.text.substring(results.get(0).textStartIndex, results.get(0).textEndIndex));
        assertEquals("second", renderNode.text.substring(results.get(1).textStartIndex, results.get(1).textEndIndex));
        assertEquals("word third", renderNode.text.substring(results.get(2).textStartIndex, results.get(2).textEndIndex));
        assertEquals("word", renderNode.text.substring(results.get(3).textStartIndex, results.get(3).textEndIndex));
    }

    @Test
    public void longWordsTest() {
        RenderNode renderNode = new RenderNode(HTMLElements.TEXT);
        renderNode.text = "first word very-long-second-word third word very-long-fourth-and-final-word";
        BoxNode boxNode = new BoxNode();
        boxNode.isTextNode = true;
        boxNode.correspondingRenderNode = renderNode;
        float width = 15;
        List<BoxNode> results = textNodeSplitter.split(boxNode, width, width);
        assertEquals(4, results.size());
        assertEquals("first word", renderNode.text.substring(results.get(0).textStartIndex, results.get(0).textEndIndex));
        assertEquals("very-long-second-word", renderNode.text.substring(results.get(1).textStartIndex, results.get(1).textEndIndex));
        assertEquals("third word", renderNode.text.substring(results.get(2).textStartIndex, results.get(2).textEndIndex));
        assertEquals("very-long-fourth-and-final-word", renderNode.text.substring(results.get(3).textStartIndex, results.get(3).textEndIndex));
    }

    @Test
    public void shortFirstLineTest() {
        RenderNode renderNode = new RenderNode(HTMLElements.TEXT);
        renderNode.text = "first word second word third word";
        BoxNode boxNode = new BoxNode();
        boxNode.isTextNode = true;
        boxNode.correspondingRenderNode = renderNode;
        List<BoxNode> results = textNodeSplitter.split(boxNode, 5, 10);
        assertEquals(5, results.size());
        assertEquals("first", renderNode.text.substring(results.get(0).textStartIndex, results.get(0).textEndIndex));
        assertEquals("word", renderNode.text.substring(results.get(1).textStartIndex, results.get(1).textEndIndex));
        assertEquals("second", renderNode.text.substring(results.get(2).textStartIndex, results.get(2).textEndIndex));
        assertEquals("word third", renderNode.text.substring(results.get(3).textStartIndex, results.get(3).textEndIndex));
        assertEquals("word", renderNode.text.substring(results.get(4).textStartIndex, results.get(4).textEndIndex));
    }

}
