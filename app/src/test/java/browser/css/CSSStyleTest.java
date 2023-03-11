package browser.css;

import static browser.css.CSSStyle.DisplayType;
import static org.junit.Assert.assertEquals;

import java.util.List;

import browser.app.Pipeline;
import browser.model.CSSColor;

import org.junit.BeforeClass;
import org.junit.Test;

public class CSSStyleTest {

    @BeforeClass
    public static void setup() {
        Pipeline.init();
    }

    @Test
    public void setPropertyTest() {
        CSSStyle style = new CSSStyle();
        style.setProperty("textAlign", "LEFT");
        assertEquals(CSSStyle.textAlignType.LEFT, style.textAlign);
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

}
