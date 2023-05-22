package browser.parser;

import static org.junit.Assert.*;

import java.util.List;

import browser.css.CSSStyle;
import browser.layout.TextDimensionCalculator;
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

    @Test
    public void prevIndexOf() {
        assertEquals(0, StringUtils.prevIndexOf("abcde", "a", 4));
        assertEquals(0, StringUtils.prevIndexOf("abcde", "ab", 4));
        assertEquals(1, StringUtils.prevIndexOf("abcde", "bcd", 4));
        assertEquals(-1, StringUtils.prevIndexOf("abcde", "ac", 4));
        assertEquals(2, StringUtils.prevIndexOf("abcdefgh", "cde", 6));
    }

    @Test
    public void splitStringIncludeEmpty() {
        assertEquals(List.of("", "test", "test", ""), StringUtils.splitStringIncludeEmpty("\ntest\r\ntest\n", List.of("\n", "\r\n")));
        assertEquals(List.of("", "", "", "test"), StringUtils.splitStringIncludeEmpty("\n\r\n\ntest", List.of("\n", "\r\n")));
        assertEquals(List.of("", "", "e", "", ""), StringUtils.splitStringIncludeEmpty("abcdebcda", List.of("a", "bcd")));
        assertEquals(List.of("", "test"), StringUtils.splitStringIncludeEmpty("\ntest", List.of("\n")));
    }

    @Test
    public void hyphenatedToCamelCase() {
        assertEquals("fontSize", StringUtils.hyphenatedToCamelCase("font-size"));
        assertEquals("marginTopWidth", StringUtils.hyphenatedToCamelCase("margin-top-width"));
        assertEquals("fontSize", StringUtils.hyphenatedToCamelCase("font-size-"));
        assertEquals("fontSize", StringUtils.hyphenatedToCamelCase("font---size"));
        assertEquals("fontsize", StringUtils.hyphenatedToCamelCase("FontSize"));
    }

}
