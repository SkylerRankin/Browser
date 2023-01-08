package browser.layout;

import static org.junit.Assert.*;
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

    @Test
    public void fitNodeToWidth_notATextNode() {
        BoxNode boxNode = new BoxNode();
        boxNode.isTextNode = false;
        boxNode.textStartIndex = 0;
        boxNode.textEndIndex = 123;
        BoxNode result = textNodeSplitter.fitNodeToWidth(boxNode, 10);

        assertNull(result);
        assertEquals(0, boxNode.textStartIndex);
        assertEquals(123, boxNode.textEndIndex);
    }

    @Test
    public void fitNodeToWidth_textFits() {
        RenderNode renderNode = new RenderNode(HTMLElements.TEXT);
        renderNode.text = "some text";
        BoxNode boxNode = new BoxNode();
        boxNode.isTextNode = true;
        boxNode.correspondingRenderNode = renderNode;
        boxNode.textStartIndex = 0;
        boxNode.textEndIndex = 9;
        float width = 10;
        BoxNode result = textNodeSplitter.fitNodeToWidth(boxNode, width);

        assertNull(result);
        assertEquals(0, boxNode.textStartIndex);
        assertEquals(9, boxNode.textEndIndex);
    }

    @Test
    public void fitNodeToWidth_textTooLarge() {
        RenderNode renderNode = new RenderNode(HTMLElements.TEXT);
        renderNode.text = "some text that does not fit";
        BoxNode boxNode = new BoxNode();
        boxNode.isTextNode = true;
        boxNode.correspondingRenderNode = renderNode;
        boxNode.textStartIndex = 0;
        boxNode.textEndIndex = 27;
        float width = 10;
        BoxNode result = textNodeSplitter.fitNodeToWidth(boxNode, width);

        assertEquals(0, boxNode.textStartIndex);
        assertEquals(9, boxNode.textEndIndex);

        assertNotNull(result);
        assertEquals(10, result.textStartIndex);
        assertEquals(27, result.textEndIndex);
    }

    @Test
    public void fitNodeToWidth_largeWordAtBreak() {
        RenderNode renderNode = new RenderNode(HTMLElements.TEXT);
        renderNode.text = "this is-a-large-word-that-cant-be broken.";
        BoxNode boxNode = new BoxNode();
        boxNode.isTextNode = true;
        boxNode.correspondingRenderNode = renderNode;
        boxNode.textStartIndex = 0;
        boxNode.textEndIndex = 41;
        float width = 10;
        BoxNode result = textNodeSplitter.fitNodeToWidth(boxNode, width);

        assertEquals(0, boxNode.textStartIndex);
        assertEquals(4, boxNode.textEndIndex);

        assertNotNull(result);
        assertEquals(5, result.textStartIndex);
        assertEquals(41, result.textEndIndex);
    }

    @Test
    public void fitNodeToWidth_noSplitAndDoesNotFit() {
        RenderNode renderNode = new RenderNode(HTMLElements.TEXT);
        renderNode.text = "this-is-a-large-word-that-cant-be-broken.";
        BoxNode boxNode = new BoxNode();
        boxNode.isTextNode = true;
        boxNode.correspondingRenderNode = renderNode;
        boxNode.textStartIndex = 0;
        boxNode.textEndIndex = 41;
        float width = 10;
        BoxNode result = textNodeSplitter.fitNodeToWidth(boxNode, width);

        assertEquals(0, boxNode.textStartIndex);
        assertEquals(41, boxNode.textEndIndex);
        assertNull(result);
    }

    @Test
    public void fitNodeToWidth_splitWithSpacing() {
        RenderNode renderNode = new RenderNode(HTMLElements.TEXT);
        renderNode.text = " has extra spaces  ";
        BoxNode boxNode = new BoxNode();
        boxNode.isTextNode = true;
        boxNode.correspondingRenderNode = renderNode;
        boxNode.textStartIndex = 0;
        boxNode.textEndIndex = 19;
        float width = 13;
        BoxNode result = textNodeSplitter.fitNodeToWidth(boxNode, width);

        assertEquals(0, boxNode.textStartIndex);
        assertEquals(10, boxNode.textEndIndex);

        assertNotNull(result);
        assertEquals(11, result.textStartIndex);
        assertEquals(19, result.textEndIndex);
    }

    @Test
    public void gitNodeToWidth_overlapWithNonZeroStartIndex() {
        RenderNode renderNode = new RenderNode(HTMLElements.TEXT);
        renderNode.text = "fifty percent width";
        BoxNode boxNode = new BoxNode();
        boxNode.isTextNode = true;
        boxNode.correspondingRenderNode = renderNode;
        boxNode.textStartIndex = 6;
        boxNode.textEndIndex = 19;
        float width = 1;
        BoxNode result = textNodeSplitter.fitNodeToWidth(boxNode, width);

        assertEquals(6, boxNode.textStartIndex);
        assertEquals(13, boxNode.textEndIndex);

        assertNotNull(result);
        assertEquals(14, result.textStartIndex);
        assertEquals(19, result.textEndIndex);
    }

    @Test
    public void canSplitNodeToFitWidth() {
        RenderNode renderNode = new RenderNode(HTMLElements.TEXT);
        renderNode.text = "this is some text";
        BoxNode boxNode = new BoxNode();
        boxNode.isTextNode = true;
        boxNode.correspondingRenderNode = renderNode;
        boxNode.textStartIndex = 0;
        boxNode.textEndIndex = 17;

        assertTrue(textNodeSplitter.canSplitNodeToFitWidth(boxNode, 4));
        assertTrue(textNodeSplitter.canSplitNodeToFitWidth(boxNode, 10));
        assertTrue(textNodeSplitter.canSplitNodeToFitWidth(boxNode, 100));

        assertFalse(textNodeSplitter.canSplitNodeToFitWidth(boxNode, 1));
        assertFalse(textNodeSplitter.canSplitNodeToFitWidth(boxNode, 3));
    }

    @Test
    public void canSplitNodeToFitWidth_EdgeSpaces() {
        RenderNode renderNode = new RenderNode(HTMLElements.TEXT);
        renderNode.text = "  this is some text ";
        BoxNode boxNode = new BoxNode();
        boxNode.isTextNode = true;
        boxNode.correspondingRenderNode = renderNode;
        boxNode.textStartIndex = 0;
        boxNode.textEndIndex = 20;

        assertTrue(textNodeSplitter.canSplitNodeToFitWidth(boxNode, 7));
        assertTrue(textNodeSplitter.canSplitNodeToFitWidth(boxNode, 10));
        assertTrue(textNodeSplitter.canSplitNodeToFitWidth(boxNode, 100));

        assertFalse(textNodeSplitter.canSplitNodeToFitWidth(boxNode, 1));
        assertFalse(textNodeSplitter.canSplitNodeToFitWidth(boxNode, 2));
        assertFalse(textNodeSplitter.canSplitNodeToFitWidth(boxNode, 5));
    }

}
