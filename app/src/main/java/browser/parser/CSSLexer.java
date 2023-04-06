package browser.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import browser.model.CSSToken;

public class CSSLexer {

    public enum CSSTokenType {
        SELECTOR,
        OPEN_BRACKET,
        PROPERTY_NAME,
        COLON,
        PROPERTY_VALUE,
        SEMI_COLON,
        CLOSE_BRACKET,
        COMMENT_START,
        COMMENT,
        COMMENT_END,
        SKIP
    }

    private static Set<CSSTokenType> commentTokenTypes = Set.of(CSSTokenType.COMMENT_START, CSSTokenType.COMMENT, CSSTokenType.COMMENT_END);

    public static List<CSSToken> getTokens(String css) {
        css = css.trim();
        List<CSSToken> tokens = new ArrayList<>();
        CSSTokenType lastToken = CSSTokenType.CLOSE_BRACKET;
        CSSTokenType lastNonCommentToken = CSSTokenType.CLOSE_BRACKET;
        int index = 0;
        while (index < css.length()) {
            CSSToken token = getToken(css, index, lastToken, lastNonCommentToken);
            if (token == null) {
                System.err.printf("Lexing failure: given last token %s, failed to find token at \"%s...\".\n", lastToken.name(), css.substring(index, Math.min(css.length(), index + 10)));
                index++;
            } else {
                index += token.value.length();
                if (!token.type.equals(CSSTokenType.SKIP)) {
                    if (!token.type.equals(CSSTokenType.COMMENT)) {
                        token.value = token.value.trim();
                    }
                    tokens.add(token);
                    lastToken = token.type;
                    if (!commentTokenTypes.contains(token.type)) {
                        lastNonCommentToken = token.type;
                    }
                }
            }
        }

        return tokens;
    }

    private static CSSToken getToken(String css, int index, CSSTokenType lastToken, CSSTokenType lastNonCommentToken) {
        if (StringUtils.substringMatch(css, "/*", index)) {
            return new CSSToken(CSSTokenType.COMMENT_START, "/*");
        }

        switch (lastToken) {
            case SELECTOR -> { return handleSelector(css, index); }
            case OPEN_BRACKET -> { return handleOpenBracket(css, index); }
            case PROPERTY_NAME -> { return handlePropertyName(css, index); }
            case COLON -> { return handleColon(css, index); }
            case PROPERTY_VALUE -> { return handlePropertyValue(css, index); }
            case SEMI_COLON -> { return handleSemiColon(css, index); }
            case CLOSE_BRACKET -> { return handleCloseBracket(css, index); }
            case COMMENT_START -> { return handleCommentStart(css, index); }
            case COMMENT -> { return handleComment(css, index); }
            case COMMENT_END -> { return handleCommentEnd(css, index, lastNonCommentToken); }
        }

        return null;
    }

    // Handling for each token type

    // SELECTOR: Handles characters after a selector: "div, span?"
    private static CSSToken handleSelector(String css, int index) {
        if (css.charAt(index) == '{') {
            return new CSSToken(CSSTokenType.OPEN_BRACKET, "{");
        }

        return null;
    }

    // OPEN_BRACKET: Handles characters after an opening bracket: "div, span {?"
    private static CSSToken handleOpenBracket(String css, int index) {
        String whitespace = StringUtils.whitespaceSubstring(css, index);
        if (StringUtils.substringMatch(css, "}", index + whitespace.length())) {
            return new CSSToken(CSSTokenType.CLOSE_BRACKET, "}");
        } else if (StringUtils.substringMatch(css, "/*", index + whitespace.length())) {
            return new CSSToken(CSSTokenType.COMMENT_START, "/*");
        }
        String propertyName = StringUtils.substringUntil(css, index, ":");
        return new CSSToken(CSSTokenType.PROPERTY_NAME, propertyName);
    }

    // PROPERTY_NAME: Handles characters after a property name: "div, span { font-size?"
    private static CSSToken handlePropertyName(String css, int index) {
        if (css.charAt(index) == ':') {
            return new CSSToken(CSSTokenType.COLON, ":");
        }
        return null;
    }

    // COLON: Handles characters after a property colon: "div, span { font-size:?"
    private static CSSToken handleColon(String css, int index) {
        String value = StringUtils.substringUntil(css, index, List.of(";", "}"));
        return new CSSToken(CSSTokenType.PROPERTY_VALUE, value);
    }

    // PROPERTY_VALUE: Handles characters after a property value: "div, span { font-size: 10px?"
    private static CSSToken handlePropertyValue(String css, int index) {
        if (css.charAt(index) == ';') {
            return new CSSToken(CSSTokenType.SEMI_COLON, ";");
        } else if (css.charAt(index) == '}') {
            return new CSSToken(CSSTokenType.CLOSE_BRACKET, '}');
        }
        return null;
    }

    // SEMI_COLON: Handles characters after a property semicolon: "div, span { font-size: 10px;?"
    private static CSSToken handleSemiColon(String css, int index) {
        char c = css.charAt(index);
        if (Character.isWhitespace(c)) {
            return new CSSToken(CSSTokenType.SKIP, StringUtils.whitespaceSubstring(css, index));
        }

        if (c == '}') {
            return new CSSToken(CSSTokenType.CLOSE_BRACKET, '}');
        }

        // TODO check for next char being : or ;

        String name = StringUtils.substringUntil(css, index, ":");
        return new CSSToken(CSSTokenType.PROPERTY_NAME, name);
    }

    // CLOSE_BRACKET: Handles characters after a close bracket: "div, span { font-size: 10px; }?"
    private static CSSToken handleCloseBracket(String css, int index) {
        char c = css.charAt(index);
        if (Character.isWhitespace(c)) {
            return new CSSToken(CSSTokenType.SKIP, StringUtils.whitespaceSubstring(css, index));
        }

        String selector = StringUtils.substringUntil(css, index, "{");
        return new CSSToken(CSSTokenType.SELECTOR, selector);
    }

    // COMMENT_START: Handles characters after a comment start: "/*?"
    private static CSSToken handleCommentStart(String css, int index) {
        if (StringUtils.substringMatch(css, "*/", index)) {
            return new CSSToken(CSSTokenType.COMMENT_END, "*/");
        } else {
            String commentText = StringUtils.substringUntil(css, index, "*/");
            return new CSSToken(CSSTokenType.COMMENT, commentText);
        }
    }

    // COMMENT: Handles characters after a comment text: "/*comment?"
    private static CSSToken handleComment(String css, int index) {
        return new CSSToken(CSSTokenType.COMMENT_END, "*/");
    }

    // COMMENT_END Handles characters after a comment end: "/*comment*/?"
    private static CSSToken handleCommentEnd(String css, int index, CSSTokenType lastNonCommentToken) {
        char c = css.charAt(index);
        if (Character.isWhitespace(c)) {
            String whitespace = StringUtils.whitespaceSubstring(css, index);
            return new CSSToken(CSSTokenType.SKIP, whitespace);
        } else {
            return getToken(css, index, lastNonCommentToken, null);
        }
    }

}
