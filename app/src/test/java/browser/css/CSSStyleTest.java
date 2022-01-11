package browser.css;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CSSStyleTest {

    @Test
    public void setPropertyTest() {
        CSSStyle style = new CSSStyle();
        style.setProperty("textAlign", "LEFT");
        assertEquals("FONT", style.fontFamily);
    }

}
