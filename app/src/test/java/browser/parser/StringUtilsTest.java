package browser.parser;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;


public class StringUtilsTest {

    @Test
    public void substringMatch() {
        assertFalse(StringUtils.substringMatch("abc", "b", 0));
        assertTrue(StringUtils.substringMatch("abc", "b", 1));
        assertFalse(StringUtils.substringMatch("abc", "b", 2));

        assertTrue(StringUtils.substringMatch("div span img", "div", 0));
        assertTrue(StringUtils.substringMatch("div span img", "span", 4));
        assertTrue(StringUtils.substringMatch("div span img", "img", 9));

        assertFalse(StringUtils.substringMatch("div span img", "div", 1));
        assertFalse(StringUtils.substringMatch("div span img", "span", 20));
        assertFalse(StringUtils.substringMatch("div span img", "image", 9));
    }

    @Test
    public void substringUntilSpaceOrString() {
        assertEquals("test", StringUtils.substringUntilSpaceOrString("test test", 0, List.of()));
        assertEquals("t", StringUtils.substringUntilSpaceOrString("test test", 3, List.of()));
        assertEquals("te", StringUtils.substringUntilSpaceOrString("test test", 0, List.of("s")));
        assertEquals("t", StringUtils.substringUntilSpaceOrString("test test", 3, List.of("s")));
        assertEquals("te", StringUtils.substringUntilSpaceOrString("test test", 5, List.of("s")));
        assertEquals("", StringUtils.substringUntilSpaceOrString("test test", 1, List.of("est")));
        assertEquals("st", StringUtils.substringUntilSpaceOrString("test test", 2, List.of("est")));

        assertEquals("abcde", StringUtils.substringUntilSpaceOrString("abcdefg", 0, List.of("g", "f")));
    }

}
