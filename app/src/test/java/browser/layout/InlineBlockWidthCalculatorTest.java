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
     * This should use the preferred width of the content.
     * <div style="display: inline-block; padding: 5;">
     *     <div style="margin-left: 5">inline block</div>
     * </div>
     */
    @Test
    public void smallInlineBlockWidth() {
        TestDataLoader.TestData testData = TestDataLoader.loadLayoutTrees("inlineBlockSimpleDiv");
        setTextDimensionOverride(testData.letterWidth, testData.letterHeight);
        BoxNode boxNode = testData.rootBoxNode;

        // Available width = 100. Text width = 12, other spacing = 5 + 5 + 5. Preferred width = 27
        float result = inlineBlockWidthCalculator.getWidth(boxNode, 100);
        assertEquals(27, result, DELTA);
    }

}
