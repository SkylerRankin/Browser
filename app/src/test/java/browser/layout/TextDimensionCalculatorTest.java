package browser.layout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import browser.css.CSSStyle;
//import browser.javafx.embed.swing.JFXPanel;
import browser.layout.TextDimensionCalculator;
import browser.layout.TextSplitter;
import browser.model.Vector2;

public class TextDimensionCalculatorTest {

//	@Before
//	public void before() {
//		JFXPanel jfxPanel = new JFXPanel();
//	}

    @Test
    public void test() {
//		JFXPanel jfxPanel = new JFXPanel();
        CSSStyle style = new CSSStyle();
        style.fontFamily = "Times New Roman";
        style.fontSize = 12;
//		style.fontWeight = CSSStyle.fontWeightType.BOLD;
        Vector2 dimensions = TextDimensionCalculator.getTextDimension("<end>", style);
        System.out.printf("%s\n", dimensions.toString());
    }

    //	@Test
    public void splitToWidthTest_simple() {
        String testString = "onetwothree";
        float maxWidth = 20f;
        CSSStyle style = new CSSStyle();
        style.fontFamily = "Consolas";
        style.fontSize = 12;
        float letterWidth = TextDimensionCalculator.getTextDimension("a", style).x;
        System.out.printf("letterWidth: %f\n", letterWidth);
        List<String> lines = new TextSplitter(null).splitToWidth(testString, style, maxWidth);
        String total = "";
        for (String line : lines) {
            total += line;
            float width = TextDimensionCalculator.getTextDimension(line, style).x;
            assertTrue(width <= maxWidth);
            if (lines.indexOf(line) < lines.size() - 1) {
                assertTrue(width + letterWidth > maxWidth);
            }
        }
        assertEquals(total, testString);

    }

}
