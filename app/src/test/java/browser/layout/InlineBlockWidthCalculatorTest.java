package browser.layout;

import static browser.constants.MathConstants.DELTA;
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

public class InlineBlockWidthCalculatorTest {

    private InlineBlockWidthCalculator inlineBlockWidthCalculator;
    private TextDimensionCalculator textDimensionCalculator;

    @Before
    public void setup() {
        Pipeline.init();
        textDimensionCalculator = mock(TextDimensionCalculator.class);
        BoxLayoutGenerator boxLayoutGenerator = new BoxLayoutGenerator(textDimensionCalculator);
        inlineBlockWidthCalculator = new InlineBlockWidthCalculator(boxLayoutGenerator);
    }

    private void setTextDimensionOverride(int width, int height) {
        when(textDimensionCalculator.getDimension(anyString(), any()))
                .thenAnswer(i -> {
                    float x = i.getArgument(0, String.class).length() * width;
                    return new Vector2(x, height);
                });
    }

    /**
     * <div style="display: inline-block; padding: 5;">
     *     <div style="margin-left: 10">inline block</div>
     * </div>
     */
    @Test
    public void smallInlineBlockWidth() {
        TestDataLoader.TestData testData = TestDataLoader.loadLayoutTrees("inlineBlockSimpleDiv");
        setTextDimensionOverride(testData.letterWidth, testData.letterHeight);
        BoxNode boxNode = testData.rootBoxNode;

        // Preferred width is less than available width, so the full preferred width is used.
        float result = inlineBlockWidthCalculator.getWidth(boxNode, 100);
        assertEquals(32, result, DELTA);

        // Preferred width > available width, and available width > min width, so available width is used.
        result = inlineBlockWidthCalculator.getWidth(boxNode, 30);
        assertEquals(30, result, DELTA);

        // Available width < min width, so min width is used.
        result = inlineBlockWidthCalculator.getWidth(boxNode, 20);
        assertEquals(26, result, DELTA);
    }

    /**
     * <div style="display: inline-block">
     *     <div style="width: 50%">fifty percent width</div>
     *     <div style="width: 80%">eighty percent width</div>
     * </div>
     */
    @Test
    public void inlineBlockPercentageChildWidth() {
        TestDataLoader.TestData testData = TestDataLoader.loadLayoutTrees("inlineBlockPercentageChildWidth");
        setTextDimensionOverride(testData.letterWidth, testData.letterHeight);
        BoxNode boxNode = testData.rootBoxNode;

        float result = inlineBlockWidthCalculator.getWidth(boxNode, 100);
        assertEquals(20, result, DELTA);

        result = inlineBlockWidthCalculator.getWidth(boxNode, 50);
        assertEquals(20, result, DELTA);

        result = inlineBlockWidthCalculator.getWidth(boxNode, 20);
        assertEquals(20, result, DELTA);

        result = inlineBlockWidthCalculator.getWidth(boxNode, 15);
        assertEquals(15, result, DELTA);
    }

}
