package browser.parser;

import static org.junit.Assert.assertEquals;

import java.util.List;

import browser.model.HTMLToken;
import browser.parser.HTMLLexer.HTMLTokenType;

import org.junit.Test;

public class HTMLLexerTest {

    @Test
    public void simpleValid() {
        String input = "<div>this<span>is</span>div</div>";
        List<HTMLToken> expectedTokens = List.of(
                new HTMLToken(HTMLTokenType.TAG_OPEN, '<'),
                new HTMLToken(HTMLTokenType.TAG_NAME, "div"),
                new HTMLToken(HTMLTokenType.TAG_CLOSE, '>'),
                new HTMLToken(HTMLTokenType.TEXT, "this"),
                new HTMLToken(HTMLTokenType.TAG_OPEN, '<'),
                new HTMLToken(HTMLTokenType.TAG_NAME, "span"),
                new HTMLToken(HTMLTokenType.TAG_CLOSE, '>'),
                new HTMLToken(HTMLTokenType.TEXT, "is"),
                new HTMLToken(HTMLTokenType.TAG_END_OPEN, "</"),
                new HTMLToken(HTMLTokenType.TAG_NAME, "span"),
                new HTMLToken(HTMLTokenType.TAG_CLOSE, '>'),
                new HTMLToken(HTMLTokenType.TEXT, "div"),
                new HTMLToken(HTMLTokenType.TAG_END_OPEN, "</"),
                new HTMLToken(HTMLTokenType.TAG_NAME, "div"),
                new HTMLToken(HTMLTokenType.TAG_CLOSE, '>')
        );
        HTMLLexer htmlLexer = new HTMLLexer(input);
        List<HTMLToken> tokens = htmlLexer.getTokens();
        assertTokenListsEqual(expectedTokens, tokens);
    }

    @Test
    public void simpleTextSpaces() {
        String input = "<div>  this\t<span>\n\ni\ns\t</span>div </div>";
        List<HTMLToken> expectedTokens = List.of(
                new HTMLToken(HTMLTokenType.TAG_OPEN, '<'),
                new HTMLToken(HTMLTokenType.TAG_NAME, "div"),
                new HTMLToken(HTMLTokenType.TAG_CLOSE, '>'),
                new HTMLToken(HTMLTokenType.TEXT, "  this\t"),
                new HTMLToken(HTMLTokenType.TAG_OPEN, '<'),
                new HTMLToken(HTMLTokenType.TAG_NAME, "span"),
                new HTMLToken(HTMLTokenType.TAG_CLOSE, '>'),
                new HTMLToken(HTMLTokenType.TEXT, "\n\ni\ns\t"),
                new HTMLToken(HTMLTokenType.TAG_END_OPEN, "</"),
                new HTMLToken(HTMLTokenType.TAG_NAME, "span"),
                new HTMLToken(HTMLTokenType.TAG_CLOSE, '>'),
                new HTMLToken(HTMLTokenType.TEXT, "div "),
                new HTMLToken(HTMLTokenType.TAG_END_OPEN, "</"),
                new HTMLToken(HTMLTokenType.TAG_NAME, "div"),
                new HTMLToken(HTMLTokenType.TAG_CLOSE, '>')
        );
        HTMLLexer htmlLexer = new HTMLLexer(input);
        List<HTMLToken> tokens = htmlLexer.getTokens();
        assertTokenListsEqual(expectedTokens, tokens);
    }

    @Test
    public void simpleAttributes() {
        String input = "<div class=\"box\"><span id=\"2\" style=\"width: 20px;\">";
        List<HTMLToken> expectedTokens = List.of(
                new HTMLToken(HTMLTokenType.TAG_OPEN, '<'),
                new HTMLToken(HTMLTokenType.TAG_NAME, "div"),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_NAME, "class"),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_EQUALS, "="),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_START_QUOTES, "\""),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_VALUE, "box"),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_END_QUOTES, "\""),
                new HTMLToken(HTMLTokenType.TAG_CLOSE, '>'),
                new HTMLToken(HTMLTokenType.TAG_OPEN, '<'),
                new HTMLToken(HTMLTokenType.TAG_NAME, "span"),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_NAME, "id"),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_EQUALS, "="),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_START_QUOTES, "\""),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_VALUE, "2"),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_END_QUOTES, "\""),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_NAME, "style"),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_EQUALS, "="),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_START_QUOTES, "\""),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_VALUE, "width: 20px;"),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_END_QUOTES, "\""),
                new HTMLToken(HTMLTokenType.TAG_CLOSE, '>')
        );
        HTMLLexer htmlLexer = new HTMLLexer(input);
        List<HTMLToken> tokens = htmlLexer.getTokens();
        assertTokenListsEqual(expectedTokens, tokens);
    }

    @Test
    public void mixedAttributes() {
        String input = "<meta active name-value=\"robot\" href=\"./dev\" disabled rel=\"search\">";
        List<HTMLToken> expectedTokens = List.of(
                new HTMLToken(HTMLTokenType.TAG_OPEN, '<'),
                new HTMLToken(HTMLTokenType.TAG_NAME, "meta"),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_NAME, "active"),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_NAME, "name-value"),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_EQUALS, "="),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_START_QUOTES, "\""),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_VALUE, "robot"),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_END_QUOTES, "\""),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_NAME, "href"),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_EQUALS, "="),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_START_QUOTES, "\""),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_VALUE, "./dev"),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_END_QUOTES, "\""),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_NAME, "disabled"),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_NAME, "rel"),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_EQUALS, "="),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_START_QUOTES, "\""),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_VALUE, "search"),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_END_QUOTES, "\""),
                new HTMLToken(HTMLTokenType.TAG_CLOSE, '>')
        );
        HTMLLexer htmlLexer = new HTMLLexer(input);
        List<HTMLToken> tokens = htmlLexer.getTokens();
        assertTokenListsEqual(expectedTokens, tokens);
    }

    @Test
    public void simpleComment() {
        String input = "<div><!--comment-->div</div>";
        List<HTMLToken> expectedTokens = List.of(
                new HTMLToken(HTMLTokenType.TAG_OPEN, '<'),
                new HTMLToken(HTMLTokenType.TAG_NAME, "div"),
                new HTMLToken(HTMLTokenType.TAG_CLOSE, '>'),
                new HTMLToken(HTMLTokenType.COMMENT_START, "<!--"),
                new HTMLToken(HTMLTokenType.TEXT, "comment"),
                new HTMLToken(HTMLTokenType.COMMENT_END, "-->"),
                new HTMLToken(HTMLTokenType.TEXT, "div"),
                new HTMLToken(HTMLTokenType.TAG_END_OPEN, "</"),
                new HTMLToken(HTMLTokenType.TAG_NAME, "div"),
                new HTMLToken(HTMLTokenType.TAG_CLOSE, '>')
        );
        HTMLLexer htmlLexer = new HTMLLexer(input);
        List<HTMLToken> tokens = htmlLexer.getTokens();
        assertTokenListsEqual(expectedTokens, tokens);
    }

    @Test
    public void simpleScript() {
        String input = "<script>const x = [0, 1, 2]; x.forEach(v => console.log(\"v\"));</script>";
        List<HTMLToken> expectedTokens = List.of(
                new HTMLToken(HTMLTokenType.TAG_OPEN, '<'),
                new HTMLToken(HTMLTokenType.TAG_NAME, "script"),
                new HTMLToken(HTMLTokenType.TAG_CLOSE, '>'),
                new HTMLToken(HTMLTokenType.TEXT, "const x = [0, 1, 2]; x.forEach(v => console.log(\"v\"));"),
                new HTMLToken(HTMLTokenType.TAG_END_OPEN, "</"),
                new HTMLToken(HTMLTokenType.TAG_NAME, "script"),
                new HTMLToken(HTMLTokenType.TAG_CLOSE, '>')
        );
        HTMLLexer htmlLexer = new HTMLLexer(input);
        List<HTMLToken> tokens = htmlLexer.getTokens();
        assertTokenListsEqual(expectedTokens, tokens);
    }

    @Test
    public void scriptWithTagText() {
        String input = "<script>console.log(\"<a></a>\")</script>";
        List<HTMLToken> expectedTokens = List.of(
                new HTMLToken(HTMLTokenType.TAG_OPEN, '<'),
                new HTMLToken(HTMLTokenType.TAG_NAME, "script"),
                new HTMLToken(HTMLTokenType.TAG_CLOSE, '>'),
                new HTMLToken(HTMLTokenType.TEXT, "console.log(\"<a></a>\")"),
                new HTMLToken(HTMLTokenType.TAG_END_OPEN, "</"),
                new HTMLToken(HTMLTokenType.TAG_NAME, "script"),
                new HTMLToken(HTMLTokenType.TAG_CLOSE, '>')
        );
        HTMLLexer htmlLexer = new HTMLLexer(input);
        List<HTMLToken> tokens = htmlLexer.getTokens();
        assertTokenListsEqual(expectedTokens, tokens);
    }

    /**
     * Any scripts that use the string "</script>" within the actual JavaScript will break tokenization, since the
     * script tag will be picked up.
     */
    @Test
    public void scriptWithScriptTagText() {
        String input = "<script>let x=\"</script>\"";
        List<HTMLToken> expectedTokens = List.of(
                new HTMLToken(HTMLTokenType.TAG_OPEN, '<'),
                new HTMLToken(HTMLTokenType.TAG_NAME, "script"),
                new HTMLToken(HTMLTokenType.TAG_CLOSE, '>'),
                new HTMLToken(HTMLTokenType.TEXT, "let x=\""),
                new HTMLToken(HTMLTokenType.TAG_END_OPEN, "</"),
                new HTMLToken(HTMLTokenType.TAG_NAME, "script"),
                new HTMLToken(HTMLTokenType.TAG_CLOSE, '>'),
                new HTMLToken(HTMLTokenType.TEXT, "\"")
        );
        HTMLLexer htmlLexer = new HTMLLexer(input);
        List<HTMLToken> tokens = htmlLexer.getTokens();
        assertTokenListsEqual(expectedTokens, tokens);
    }

    @Test
    public void scriptAndStyleText() {
        String input = "<script>let x = 0;</script>\n<style>\n#box: { display: inline; }\n</style>";
        List<HTMLToken> expectedTokens = List.of(
                new HTMLToken(HTMLTokenType.TAG_OPEN, '<'),
                new HTMLToken(HTMLTokenType.TAG_NAME, "script"),
                new HTMLToken(HTMLTokenType.TAG_CLOSE, '>'),
                new HTMLToken(HTMLTokenType.TEXT, "let x = 0;"),
                new HTMLToken(HTMLTokenType.TAG_END_OPEN, "</"),
                new HTMLToken(HTMLTokenType.TAG_NAME, "script"),
                new HTMLToken(HTMLTokenType.TAG_CLOSE, '>'),
                new HTMLToken(HTMLTokenType.TEXT, "\n"),
                new HTMLToken(HTMLTokenType.TAG_OPEN, '<'),
                new HTMLToken(HTMLTokenType.TAG_NAME, "style"),
                new HTMLToken(HTMLTokenType.TAG_CLOSE, '>'),
                new HTMLToken(HTMLTokenType.TEXT, "\n#box: { display: inline; }\n"),
                new HTMLToken(HTMLTokenType.TAG_END_OPEN, "</"),
                new HTMLToken(HTMLTokenType.TAG_NAME, "style"),
                new HTMLToken(HTMLTokenType.TAG_CLOSE, '>')
        );
        HTMLLexer htmlLexer = new HTMLLexer(input);
        List<HTMLToken> tokens = htmlLexer.getTokens();
        assertTokenListsEqual(expectedTokens, tokens);
    }

    @Test
    public void spaceInAttributes() {
        String input = "< div  id =  \"0\" >";
        List<HTMLToken> expectedTokens = List.of(
                new HTMLToken(HTMLTokenType.TAG_OPEN, '<'),
                new HTMLToken(HTMLTokenType.TAG_NAME, "div"),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_NAME, "id"),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_EQUALS, "="),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_START_QUOTES, "\""),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_VALUE, "0"),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_END_QUOTES, "\""),
                new HTMLToken(HTMLTokenType.TAG_CLOSE, '>')
        );
        HTMLLexer htmlLexer = new HTMLLexer(input);
        List<HTMLToken> tokens = htmlLexer.getTokens();
        assertTokenListsEqual(expectedTokens, tokens);
    }

    @Test
    public void singleAttributeWithoutQuotations() {
        String input = "<div id=test>";
        List<HTMLToken> expectedTokens = List.of(
                new HTMLToken(HTMLTokenType.TAG_OPEN, '<'),
                new HTMLToken(HTMLTokenType.TAG_NAME, "div"),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_NAME, "id"),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_EQUALS, "="),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_VALUE, "test"),
                new HTMLToken(HTMLTokenType.TAG_CLOSE, '>')
        );
        HTMLLexer htmlLexer = new HTMLLexer(input);
        List<HTMLToken> tokens = htmlLexer.getTokens();
        assertTokenListsEqual(expectedTokens, tokens);
    }

    @Test
    public void multipleAttributeWithoutQuotations() {
        String input = "<div id=test test>";
        List<HTMLToken> expectedTokens = List.of(
                new HTMLToken(HTMLTokenType.TAG_OPEN, '<'),
                new HTMLToken(HTMLTokenType.TAG_NAME, "div"),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_NAME, "id"),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_EQUALS, "="),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_VALUE, "test"),
                new HTMLToken(HTMLTokenType.ATTRIBUTE_NAME, "test"),
                new HTMLToken(HTMLTokenType.TAG_CLOSE, '>')
        );
        HTMLLexer htmlLexer = new HTMLLexer(input);
        List<HTMLToken> tokens = htmlLexer.getTokens();
        assertTokenListsEqual(expectedTokens, tokens);
    }

    private void assertTokenListsEqual(List<HTMLToken> expected, List<HTMLToken> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assert(expected.get(i).type.equals(actual.get(i).type));
            assert(expected.get(i).value.equals(actual.get(i).value));
        }
    }

    private void logTokenLists(List<HTMLToken> expected, List<HTMLToken> actual) {
        System.out.println("Expected:");
        for (HTMLToken token : expected) {
            System.out.printf("  %22s: %s\n", token.type.toString(), token.value);
        }
        System.out.println("Actual:");
        for (HTMLToken token : actual) {
            System.out.printf("  %22s: %s\n", token.type.toString(), token.value);
        }
    }

}
