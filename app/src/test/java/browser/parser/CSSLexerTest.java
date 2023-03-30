package browser.parser;

import static org.junit.Assert.assertEquals;

import java.util.List;

import browser.model.CSSToken;

import org.junit.Test;

public class CSSLexerTest {

    @Test
    public void simpleValid() {
        String css = "div { background-color: green; width: 50%; }";
        List<CSSToken> tokens = CSSLexer.getTokens(css);
        List<CSSToken> expectedTokens = List.of(
                new CSSToken(CSSLexer.CSSTokenType.SELECTOR, "div"),
                new CSSToken(CSSLexer.CSSTokenType.OPEN_BRACKET, "{"),
                new CSSToken(CSSLexer.CSSTokenType.PROPERTY_NAME, "background-color"),
                new CSSToken(CSSLexer.CSSTokenType.COLON, ":"),
                new CSSToken(CSSLexer.CSSTokenType.PROPERTY_VALUE, "green"),
                new CSSToken(CSSLexer.CSSTokenType.SEMI_COLON, ";"),
                new CSSToken(CSSLexer.CSSTokenType.PROPERTY_NAME, "width"),
                new CSSToken(CSSLexer.CSSTokenType.COLON, ":"),
                new CSSToken(CSSLexer.CSSTokenType.PROPERTY_VALUE, "50%"),
                new CSSToken(CSSLexer.CSSTokenType.SEMI_COLON, ";"),
                new CSSToken(CSSLexer.CSSTokenType.CLOSE_BRACKET, "}")
        );
        assertTokenListsEqual(expectedTokens, tokens);
    }

    private void assertTokenListsEqual(List<CSSToken> expected, List<CSSToken> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assert(expected.get(i).type.equals(actual.get(i).type));
            assert(expected.get(i).value.equals(actual.get(i).value));
        }
    }

    private void logTokenLists(List<CSSToken> expected, List<CSSToken> actual) {
        System.out.println("Expected:");
        for (CSSToken token : expected) {
            System.out.printf("  %22s: %s\n", token.type.toString(), token.value);
        }
        System.out.println("Actual:");
        for (CSSToken token : actual) {
            System.out.printf("  %22s: %s\n", token.type.toString(), token.value);
        }
    }

}
