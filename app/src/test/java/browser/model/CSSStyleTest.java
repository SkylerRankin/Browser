package browser.model;

import static org.junit.Assert.*;

import browser.app.Pipeline;
import browser.css.CSSStyle;

import org.junit.BeforeClass;
import org.junit.Test;

public class CSSStyleTest {

    @BeforeClass
    public static void setup() {
        Pipeline.init();
    }

    @Test
    public void equalsTest() {
        CSSStyle style1 = new CSSStyle();
        CSSStyle style2 = new CSSStyle();

        assertEquals(style1, style2);

        style1.display = CSSStyle.DisplayType.INLINE;
        style2.display = CSSStyle.DisplayType.BLOCK;

        assertNotEquals(style1, style2);

        style2.display = CSSStyle.DisplayType.INLINE;
        style2.setProperty("property", "value2");
        style1.setProperty("property", "value1");

        assertNotEquals(style1, style2);
        style1.setProperty("property", "value2");
        assertEquals(style1, style2);
    }

}
