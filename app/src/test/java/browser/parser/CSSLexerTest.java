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
                new CSSToken(CSSLexer.CSSTokenType.SELECTOR_OPEN_BRACKET, "{"),
                new CSSToken(CSSLexer.CSSTokenType.PROPERTY_NAME, "background-color"),
                new CSSToken(CSSLexer.CSSTokenType.COLON, ":"),
                new CSSToken(CSSLexer.CSSTokenType.PROPERTY_VALUE, "green"),
                new CSSToken(CSSLexer.CSSTokenType.SEMI_COLON, ";"),
                new CSSToken(CSSLexer.CSSTokenType.PROPERTY_NAME, "width"),
                new CSSToken(CSSLexer.CSSTokenType.COLON, ":"),
                new CSSToken(CSSLexer.CSSTokenType.PROPERTY_VALUE, "50%"),
                new CSSToken(CSSLexer.CSSTokenType.SEMI_COLON, ";"),
                new CSSToken(CSSLexer.CSSTokenType.SELECTOR_CLOSE_BRACKET, "}")
        );
        assertTokenListsEqual(expectedTokens, tokens);
    }

    @Test
    public void attributesValid() {
        String css = "[a=\"b\"]{ font-size: 12px; }\n[a*=\"c\" i]{ font-size: 14px; }";
        List<CSSToken> tokens = CSSLexer.getTokens(css);
        List<CSSToken> expectedTokens = List.of(
                new CSSToken(CSSLexer.CSSTokenType.SELECTOR, "[a=\"b\"]"),
                new CSSToken(CSSLexer.CSSTokenType.SELECTOR_OPEN_BRACKET, "{"),
                new CSSToken(CSSLexer.CSSTokenType.PROPERTY_NAME, "font-size"),
                new CSSToken(CSSLexer.CSSTokenType.COLON, ":"),
                new CSSToken(CSSLexer.CSSTokenType.PROPERTY_VALUE, "12px"),
                new CSSToken(CSSLexer.CSSTokenType.SEMI_COLON, ";"),
                new CSSToken(CSSLexer.CSSTokenType.SELECTOR_CLOSE_BRACKET, "}"),
                new CSSToken(CSSLexer.CSSTokenType.SELECTOR, "[a*=\"c\" i]"),
                new CSSToken(CSSLexer.CSSTokenType.SELECTOR_OPEN_BRACKET, "{"),
                new CSSToken(CSSLexer.CSSTokenType.PROPERTY_NAME, "font-size"),
                new CSSToken(CSSLexer.CSSTokenType.COLON, ":"),
                new CSSToken(CSSLexer.CSSTokenType.PROPERTY_VALUE, "14px"),
                new CSSToken(CSSLexer.CSSTokenType.SEMI_COLON, ";"),
                new CSSToken(CSSLexer.CSSTokenType.SELECTOR_CLOSE_BRACKET, "}")
        );
        assertTokenListsEqual(expectedTokens, tokens);
    }

    @Test
    public void missingDeclarationSemicolon() {
        String css = "div { font-size: 12px }";
        List<CSSToken> tokens = CSSLexer.getTokens(css);
        List<CSSToken> expectedTokens = List.of(
                new CSSToken(CSSLexer.CSSTokenType.SELECTOR, "div"),
                new CSSToken(CSSLexer.CSSTokenType.SELECTOR_OPEN_BRACKET, "{"),
                new CSSToken(CSSLexer.CSSTokenType.PROPERTY_NAME, "font-size"),
                new CSSToken(CSSLexer.CSSTokenType.COLON, ":"),
                new CSSToken(CSSLexer.CSSTokenType.PROPERTY_VALUE, "12px"),
                new CSSToken(CSSLexer.CSSTokenType.SELECTOR_CLOSE_BRACKET, "}")
        );
        assertTokenListsEqual(expectedTokens, tokens);
    }

    @Test
    public void repeatedComments() {
        String css = "/*\ncomment\n*/\n/*second comment*/div {}/**//*\n\n*/";
        List<CSSToken> tokens = CSSLexer.getTokens(css);
        List<CSSToken> expectedTokens = List.of(
                new CSSToken(CSSLexer.CSSTokenType.COMMENT_START, "/*"),
                new CSSToken(CSSLexer.CSSTokenType.COMMENT, "\ncomment\n"),
                new CSSToken(CSSLexer.CSSTokenType.COMMENT_END, "*/"),
                new CSSToken(CSSLexer.CSSTokenType.COMMENT_START, "/*"),
                new CSSToken(CSSLexer.CSSTokenType.COMMENT, "second comment"),
                new CSSToken(CSSLexer.CSSTokenType.COMMENT_END, "*/"),
                new CSSToken(CSSLexer.CSSTokenType.SELECTOR, "div"),
                new CSSToken(CSSLexer.CSSTokenType.SELECTOR_OPEN_BRACKET, "{"),
                new CSSToken(CSSLexer.CSSTokenType.SELECTOR_CLOSE_BRACKET, "}"),
                new CSSToken(CSSLexer.CSSTokenType.COMMENT_START, "/*"),
                new CSSToken(CSSLexer.CSSTokenType.COMMENT_END, "*/"),
                new CSSToken(CSSLexer.CSSTokenType.COMMENT_START, "/*"),
                new CSSToken(CSSLexer.CSSTokenType.COMMENT, "\n\n"),
                new CSSToken(CSSLexer.CSSTokenType.COMMENT_END, "*/")
                );
        assertTokenListsEqual(expectedTokens, tokens);
    }

    @Test
    public void internalComment() {
        String css = "div {/*a*/ font-size:/*b*/12px;/*c*/}";
        List<CSSToken> tokens = CSSLexer.getTokens(css);
        List<CSSToken> expectedTokens = List.of(
                new CSSToken(CSSLexer.CSSTokenType.SELECTOR, "div"),
                new CSSToken(CSSLexer.CSSTokenType.SELECTOR_OPEN_BRACKET, "{"),
                new CSSToken(CSSLexer.CSSTokenType.COMMENT_START, "/*"),
                new CSSToken(CSSLexer.CSSTokenType.COMMENT, "a"),
                new CSSToken(CSSLexer.CSSTokenType.COMMENT_END, "*/"),
                new CSSToken(CSSLexer.CSSTokenType.PROPERTY_NAME, "font-size"),
                new CSSToken(CSSLexer.CSSTokenType.COLON, ":"),
                new CSSToken(CSSLexer.CSSTokenType.COMMENT_START, "/*"),
                new CSSToken(CSSLexer.CSSTokenType.COMMENT, "b"),
                new CSSToken(CSSLexer.CSSTokenType.COMMENT_END, "*/"),
                new CSSToken(CSSLexer.CSSTokenType.PROPERTY_VALUE, "12px"),
                new CSSToken(CSSLexer.CSSTokenType.SEMI_COLON, ";"),
                new CSSToken(CSSLexer.CSSTokenType.COMMENT_START, "/*"),
                new CSSToken(CSSLexer.CSSTokenType.COMMENT, "c"),
                new CSSToken(CSSLexer.CSSTokenType.COMMENT_END, "*/"),
                new CSSToken(CSSLexer.CSSTokenType.SELECTOR_CLOSE_BRACKET, "}")
                );
        assertTokenListsEqual(expectedTokens, tokens);
    }

    @Test
    public void mediaQuery() {
        String css = "@media all and (max-width:1000px) {\n.label { font-size: 2em; } \n}";
        List<CSSToken> tokens = CSSLexer.getTokens(css);
        List<CSSToken> expectedTokens = List.of(
                new CSSToken(CSSLexer.CSSTokenType.AT_RULE, "@media all and (max-width:1000px)"),
                new CSSToken(CSSLexer.CSSTokenType.AT_RULE_OPEN_BRACKET, "{"),
                new CSSToken(CSSLexer.CSSTokenType.SELECTOR, ".label"),
                new CSSToken(CSSLexer.CSSTokenType.SELECTOR_OPEN_BRACKET, "{"),
                new CSSToken(CSSLexer.CSSTokenType.PROPERTY_NAME, "font-size"),
                new CSSToken(CSSLexer.CSSTokenType.COLON, ":"),
                new CSSToken(CSSLexer.CSSTokenType.PROPERTY_VALUE, "2em"),
                new CSSToken(CSSLexer.CSSTokenType.SEMI_COLON, ";"),
                new CSSToken(CSSLexer.CSSTokenType.SELECTOR_CLOSE_BRACKET, "}"),
                new CSSToken(CSSLexer.CSSTokenType.AT_RULE_CLOSE_BRACKET, "}")
        );
        assertTokenListsEqual(expectedTokens, tokens);
    }

    @Test
    public void fontAtRule() {
        String css = "h1 { width: 1px; }\n@font { font-family: 'font'; } h2 { width: 2px; }";
        List<CSSToken> tokens = CSSLexer.getTokens(css);
        List<CSSToken> expectedTokens = List.of(
                new CSSToken(CSSLexer.CSSTokenType.SELECTOR, "h1"),
                new CSSToken(CSSLexer.CSSTokenType.SELECTOR_OPEN_BRACKET, "{"),
                new CSSToken(CSSLexer.CSSTokenType.PROPERTY_NAME, "width"),
                new CSSToken(CSSLexer.CSSTokenType.COLON, ":"),
                new CSSToken(CSSLexer.CSSTokenType.PROPERTY_VALUE, "1px"),
                new CSSToken(CSSLexer.CSSTokenType.SEMI_COLON, ";"),
                new CSSToken(CSSLexer.CSSTokenType.SELECTOR_CLOSE_BRACKET, "}"),
                new CSSToken(CSSLexer.CSSTokenType.AT_RULE, "@font"),
                new CSSToken(CSSLexer.CSSTokenType.AT_RULE_OPEN_BRACKET, "{"),
                new CSSToken(CSSLexer.CSSTokenType.PROPERTY_NAME, "font-family"),
                new CSSToken(CSSLexer.CSSTokenType.COLON, ":"),
                new CSSToken(CSSLexer.CSSTokenType.PROPERTY_VALUE, "'font'"),
                new CSSToken(CSSLexer.CSSTokenType.SEMI_COLON, ";"),
                new CSSToken(CSSLexer.CSSTokenType.AT_RULE_CLOSE_BRACKET, "}"),
                new CSSToken(CSSLexer.CSSTokenType.SELECTOR, "h2"),
                new CSSToken(CSSLexer.CSSTokenType.SELECTOR_OPEN_BRACKET, "{"),
                new CSSToken(CSSLexer.CSSTokenType.PROPERTY_NAME, "width"),
                new CSSToken(CSSLexer.CSSTokenType.COLON, ":"),
                new CSSToken(CSSLexer.CSSTokenType.PROPERTY_VALUE, "2px"),
                new CSSToken(CSSLexer.CSSTokenType.SEMI_COLON, ";"),
                new CSSToken(CSSLexer.CSSTokenType.SELECTOR_CLOSE_BRACKET, "}")
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
