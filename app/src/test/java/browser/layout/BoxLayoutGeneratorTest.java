package browser.layout;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import browser.app.Pipeline;
import browser.model.BoxNode;
import browser.model.Vector2;
import browser.util.TestDataLoader;

import org.junit.Before;
import org.junit.Test;

public class BoxLayoutGeneratorTest {

    private BoxLayoutGenerator boxLayoutGenerator;
    private TextDimensionCalculator textDimensionCalculator;

    @Before
    public void setup() {
        Pipeline.init();
        textDimensionCalculator = mock(TextDimensionCalculator.class);
        boxLayoutGenerator = new BoxLayoutGenerator(textDimensionCalculator);
    }

    private void setTextDimensionOverride(int width, int height) {
        when(textDimensionCalculator.getDimension(anyString(), any()))
                .thenAnswer(i -> {
                    float x = i.getArgument(0, String.class).length() * width;
                    return new Vector2(x, height);
                });
    }

    @Test
    public void simpleTextInPInDiv() {
        TestDataLoader.TestData testData = TestDataLoader.loadLayoutTrees("textInPInDiv");
        setTextDimensionOverride(testData.letterWidth, testData.letterHeight);
        BoxNode rootBoxNode = testData.rootBoxNode;
        boxLayoutGenerator.calculateLayout(rootBoxNode, testData.screenWidth);
        assertEquals(testData.rootBoxNodeAfterLayout, rootBoxNode);
    }

    @Test
    public void simpleTextInPInDivWithOverflow() {
        TestDataLoader.TestData testData = TestDataLoader.loadLayoutTrees("textInPInDivWithOverflow");
        setTextDimensionOverride(testData.letterWidth, testData.letterHeight);
        BoxNode rootBoxNode = testData.rootBoxNode;
        System.out.println(rootBoxNode.toRecursiveString());
        boxLayoutGenerator.calculateLayout(rootBoxNode, testData.screenWidth);
        System.out.println(rootBoxNode.toRecursiveString());
        assertEquals(testData.rootBoxNodeAfterLayout, rootBoxNode);
    }

    @Test
    public void simpleInlineSpans() {
        TestDataLoader.TestData testData = TestDataLoader.loadLayoutTrees("singleLineSpansInDiv");
        setTextDimensionOverride(testData.letterWidth, testData.letterHeight);
        BoxNode rootBoxNode = testData.rootBoxNode;
        boxLayoutGenerator.calculateLayout(rootBoxNode, testData.screenWidth);
        assertEquals(testData.rootBoxNodeAfterLayout, rootBoxNode);
    }

    @Test
    public void moveInlineParentsToNextLine() {
        TestDataLoader.TestData testData = TestDataLoader.loadLayoutTrees("moveInlineParentsToNextLine");
        setTextDimensionOverride(testData.letterWidth, testData.letterHeight);
        BoxNode rootBoxNode = testData.rootBoxNode;
        boxLayoutGenerator.calculateLayout(rootBoxNode, testData.screenWidth);
        assertBoxesEqualIgnoreIds(testData.rootBoxNodeAfterLayout, rootBoxNode);
    }

    @Test
    public void simpleSplitTextSpans() {
        TestDataLoader.TestData testData = TestDataLoader.loadLayoutTrees("splitTextSpanInDiv");
        setTextDimensionOverride(testData.letterWidth, testData.letterHeight);
        BoxNode rootBoxNode = testData.rootBoxNode;
        boxLayoutGenerator.calculateLayout(rootBoxNode, testData.screenWidth);
        assertBoxesEqualIgnoreIds(testData.rootBoxNodeAfterLayout, rootBoxNode);
    }

    private void assertBoxesEqualIgnoreIds(BoxNode box1, BoxNode box2) {
        assertEquals(box1.outerDisplayType, box2.outerDisplayType);
        assertEquals(box1.innerDisplayType, box2.innerDisplayType);
        assertEquals(box1.isAnonymous, box2.isAnonymous);
        assertEquals(box1.isTextNode, box2.isTextNode);
        assertEquals(box1.parent == null, box2.parent == null);
        assertEquals(box1.children.size(), box2.children.size());
        assertEquals(box1.renderNodeId, box2.renderNodeId);
        assertEquals(box1.x, box2.x);
        assertEquals(box1.y, box2.y);
        assertEquals(box1.width, box2.width);
        assertEquals(box1.height, box2.height);
        assertEquals(box1.textStartIndex, box2.textStartIndex);
        assertEquals(box1.textEndIndex, box2.textEndIndex);

        for (int i = 0; i < box1.children.size(); i++) {
            assertBoxesEqualIgnoreIds(box1.children.get(i), box2.children.get(i));
        }
    }

}
