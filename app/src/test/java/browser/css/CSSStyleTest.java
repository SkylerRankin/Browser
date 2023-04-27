package browser.css;

import static browser.constants.MathConstants.DELTA;
import static browser.css.CSSStyle.DisplayType;
import static org.junit.Assert.assertEquals;

import java.util.List;

import browser.app.Pipeline;
import browser.constants.CSSConstants;
import browser.model.CSSColor;

import org.junit.BeforeClass;
import org.junit.Test;

public class CSSStyleTest {

    @BeforeClass
    public static void setup() {
        Pipeline.init();
    }

    @Test
    public void displayTypeTest() {
        List<List<String>> properties = List.of(
                List.of("display", "block"),
                List.of("display", "inline"),
                List.of("display", "inline-block"),
                List.of("display", "block flow"),
                List.of("display", "table"),
                List.of("display", "inline-table"),
                List.of("display", "flex"),
                List.of("display", "inline flow"),
                List.of("display", "block table"),
                List.of("display", "block flex"),
                List.of("display", "run-in")
        );

        List<List<DisplayType>> displayTypes = List.of(
                List.of(DisplayType.BLOCK, DisplayType.FLOW),
                List.of(DisplayType.INLINE, DisplayType.FLOW),
                List.of(DisplayType.INLINE, DisplayType.FLOW_ROOT),
                List.of(DisplayType.BLOCK, DisplayType.FLOW),
                List.of(DisplayType.BLOCK, DisplayType.TABLE),
                List.of(DisplayType.INLINE, DisplayType.TABLE),
                List.of(DisplayType.BLOCK, DisplayType.FLEX),
                List.of(DisplayType.INLINE, DisplayType.FLOW),
                List.of(DisplayType.BLOCK, DisplayType.TABLE),
                List.of(DisplayType.BLOCK, DisplayType.FLEX),
                List.of(DisplayType.RUN_IN, DisplayType.FLOW)
        );

        for (int i = 0; i < properties.size(); i++) {
            CSSStyle style = new CSSStyle();
            style.setProperty(properties.get(i).get(0), properties.get(i).get(1));
            style.finalizeCSS();
            assertEquals(displayTypes.get(i).get(0), style.outerDisplay);
            assertEquals(displayTypes.get(i).get(1), style.innerDisplay);
        }
    }

    @Test
    public void auxiliaryDisplayTypesTest() {
        List<List<String>> properties = List.of(
                List.of("display", "list-item"),
                List.of("display", "none")
        );

        List<List<DisplayType>> displayTypes = List.of(
                List.of(DisplayType.BLOCK, DisplayType.FLOW, DisplayType.LIST_ITEM),
                List.of(DisplayType.NONE, DisplayType.NONE, DisplayType.NONE)
        );

        for (int i = 0; i < properties.size(); i++) {
            CSSStyle style = new CSSStyle();
            style.setProperty(properties.get(i).get(0), properties.get(i).get(1));
            style.finalizeCSS();
            assertEquals(displayTypes.get(i).get(0), style.outerDisplay);
            assertEquals(displayTypes.get(i).get(1), style.innerDisplay);
            assertEquals(displayTypes.get(i).get(2), style.auxiliaryDisplay);
        }
    }

    @Test
    public void parseBorderCombinedTest() {
        List<String> permutations = List.of(
                "solid 1px #121212;",
                "solid #121212 1px;",
                "1px solid #121212;",
                "1px #121212 solid;",
                "#121212 1px solid;",
                "#121212 solid 1px;"
        );
        for (String borderValue : permutations) {
            CSSStyle style = new CSSStyle();
            style.setProperty("border", borderValue);
            style.finalizeCSS();
            assertEquals(1, style.borderWidthLeft);
            assertEquals(1, style.borderWidthRight);
            assertEquals(1, style.borderWidthTop);
            assertEquals(1, style.borderWidthBottom);
            assertEquals(new CSSColor("#121212"), style.borderColorLeft);
            assertEquals(new CSSColor("#121212"), style.borderColorRight);
            assertEquals(new CSSColor("#121212"), style.borderColorTop);
            assertEquals(new CSSColor("#121212"), style.borderColorBottom);
        }
    }

    @Test
    public void parseFlexShorthandOneValue() {
        // Single unit-less number sets flex-grow
        CSSStyle style = new CSSStyle();
        style.setProperty("flex", "2");
        style.finalizeCSS();
        assertEquals(2, style.flexGrow, DELTA);
        assertEquals(1, style.flexShrink, DELTA);
        assertEquals(CSSStyle.FlexBasis.LENGTH, style.flexBasis);
        assertEquals(0, style.flexBasisValue, DELTA);
        assertEquals(CSSConstants.LengthUnit.PX, style.flexBasisUnit);

        style = new CSSStyle();
        style.setProperty("flex", "0");
        style.finalizeCSS();
        assertEquals(0, style.flexGrow, DELTA);
        assertEquals(1, style.flexShrink, DELTA);
        assertEquals(CSSStyle.FlexBasis.LENGTH, style.flexBasis);
        assertEquals(0, style.flexBasisValue, DELTA);
        assertEquals(CSSConstants.LengthUnit.PX, style.flexBasisUnit);

        // Single value with unit sets flex-basis
        style = new CSSStyle();
        style.setProperty("flex", "10em");
        style.finalizeCSS();
        assertEquals(1, style.flexGrow, DELTA);
        assertEquals(1, style.flexShrink, DELTA);
        assertEquals(CSSStyle.FlexBasis.LENGTH, style.flexBasis);
        assertEquals(10, style.flexBasisValue, DELTA);
        assertEquals(CSSConstants.LengthUnit.EM, style.flexBasisUnit);

        // Single flex-basis keyword
        style = new CSSStyle();
        style.setProperty("flex", "min-content");
        style.finalizeCSS();
        assertEquals(1, style.flexGrow, DELTA);
        assertEquals(1, style.flexShrink, DELTA);
        assertEquals(CSSStyle.FlexBasis.MIN_CONTENT, style.flexBasis);
    }

    @Test
    public void parseFlexShorthandTwoValues() {
        // Flex grow and flex shrink
        CSSStyle style = new CSSStyle();
        style.setProperty("flex", "2 3");
        style.finalizeCSS();
        assertEquals(2, style.flexGrow, DELTA);
        assertEquals(3, style.flexShrink, DELTA);
        assertEquals(CSSStyle.FlexBasis.LENGTH, style.flexBasis);
        assertEquals(0, style.flexBasisValue, DELTA);
        assertEquals(CSSConstants.LengthUnit.PX, style.flexBasisUnit);

        // Flex grow and flex basis
        style = new CSSStyle();
        style.setProperty("flex", "2 30px");
        style.finalizeCSS();
        assertEquals(2, style.flexGrow, DELTA);
        assertEquals(1, style.flexShrink, DELTA);
        assertEquals(CSSStyle.FlexBasis.LENGTH, style.flexBasis);
        assertEquals(30, style.flexBasisValue, DELTA);
        assertEquals(CSSConstants.LengthUnit.PX, style.flexBasisUnit);
    }

    @Test
    public void parseFlexShorthandThreeValues() {
        // Flex grow, flex shrink, flex basis
        CSSStyle style = new CSSStyle();
        style.setProperty("flex", "2 3 10%");
        style.finalizeCSS();
        assertEquals(2, style.flexGrow, DELTA);
        assertEquals(3, style.flexShrink, DELTA);
        assertEquals(CSSStyle.FlexBasis.PERCENTAGE, style.flexBasis);
        assertEquals(10, style.flexBasisValue, DELTA);

        // Flex grow, flex shrink, flex basis keyword
        style = new CSSStyle();
        style.setProperty("flex", "1 0 max-content");
        style.finalizeCSS();
        assertEquals(1, style.flexGrow, DELTA);
        assertEquals(0, style.flexShrink, DELTA);
        assertEquals(CSSStyle.FlexBasis.MAX_CONTENT, style.flexBasis);

        // Extra space
        style = new CSSStyle();
        style.setProperty("flex", " 1   0   max-content ");
        style.finalizeCSS();
        assertEquals(1, style.flexGrow, DELTA);
        assertEquals(0, style.flexShrink, DELTA);
        assertEquals(CSSStyle.FlexBasis.MAX_CONTENT, style.flexBasis);
    }

}
