package browser.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import browser.model.CSSToken;

public class CSSLexer {

    public enum CSSTokenType {
        AT_RULE,
        AT_RULE_OPEN_BRACKET,
        AT_RULE_CLOSE_BRACKET,
        SELECTOR,
        SELECTOR_OPEN_BRACKET,
        PROPERTY_NAME,
        COLON,
        PROPERTY_VALUE,
        SEMI_COLON,
        SELECTOR_CLOSE_BRACKET,
        COMMENT_START,
        COMMENT,
        COMMENT_END,
        SKIP
    }

    public enum BlockContext {
        Selector,
        SingularAtRule,
        NestedAtRule
    }

    private static final Set<CSSTokenType> commentTokenTypes = Set.of(CSSTokenType.COMMENT_START, CSSTokenType.COMMENT, CSSTokenType.COMMENT_END);

    public static List<CSSToken> getTokens(String css) {
        List<BlockContext> contextStack = new ArrayList<>();
        css = css.trim();
        List<CSSToken> tokens = new ArrayList<>();
        CSSTokenType lastToken = CSSTokenType.AT_RULE_CLOSE_BRACKET;
        CSSTokenType lastNonCommentToken = CSSTokenType.AT_RULE_CLOSE_BRACKET;
        int index = 0;
        while (index < css.length()) {
            CSSToken token = getToken(css, index, lastToken, lastNonCommentToken, contextStack);
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

    private static CSSToken getToken(String css, int index, CSSTokenType lastToken, CSSTokenType lastNonCommentToken, List<BlockContext> contextStack) {
        if (StringUtils.substringMatch(css, "/*", index)) {
            return new CSSToken(CSSTokenType.COMMENT_START, "/*");
        }

        switch (lastToken) {
            case SELECTOR -> { return handleSelector(css, index, contextStack); }
            case AT_RULE -> { return handleAtRule(css, index, contextStack); }
            case AT_RULE_OPEN_BRACKET -> { return handleAtRuleOpenBracket(css, index, contextStack); }
            case AT_RULE_CLOSE_BRACKET -> { return handleAtRuleCloseBracket(css, index); }
            case SELECTOR_OPEN_BRACKET -> { return handleSelectorOpenBracket(css, index, contextStack); }
            case PROPERTY_NAME -> { return handlePropertyName(css, index); }
            case COLON -> { return handleColon(css, index); }
            case PROPERTY_VALUE -> { return handlePropertyValue(css, index, contextStack); }
            case SEMI_COLON -> { return handleSemiColon(css, index, contextStack); }
            case SELECTOR_CLOSE_BRACKET -> { return handleSelectorCloseBracket(css, index, contextStack); }
            case COMMENT_START -> { return handleCommentStart(css, index); }
            case COMMENT -> { return handleComment(css, index); }
            case COMMENT_END -> { return handleCommentEnd(css, index, lastNonCommentToken, contextStack); }
        }

        return null;
    }

    // Handling for each token type

    // SELECTOR: Handles characters after a selector: "div, span?"
    private static CSSToken handleSelector(String css, int index, List<BlockContext> contextStack) {
        if (css.charAt(index) == '{') {
            contextStack.add(BlockContext.Selector);
            return new CSSToken(CSSTokenType.SELECTOR_OPEN_BRACKET, "{");
        }

        return null;
    }

    // AT_RULE: Handles characters after an at rule: "@media?"
    private static CSSToken handleAtRule(String css, int index, List<BlockContext> contextStack) {
        char c = css.charAt(index);
        if (Character.isWhitespace(c)) {
            return new CSSToken(CSSTokenType.SKIP, StringUtils.whitespaceSubstring(css, index));
        } else if (css.charAt(index) == '{') {
            // Check if previous token started with @media, in that case use nested at rule
            int atIndex = StringUtils.prevIndexOf(css, "@", index);
            if (atIndex == -1) {
                return null;
            } else if (StringUtils.substringMatch(css, "@media", atIndex)) {
                contextStack.add(BlockContext.NestedAtRule);
                return new CSSToken(CSSTokenType.AT_RULE_OPEN_BRACKET, "{");
            } else {
                contextStack.add(BlockContext.SingularAtRule);
                return new CSSToken(CSSTokenType.AT_RULE_OPEN_BRACKET, "{");
            }
        }
        return null;
    }

    private static CSSToken handleAtRuleOpenBracket(String css, int index, List<BlockContext> contextStack) {
        char c = css.charAt(index);
        if (Character.isWhitespace(c)) {
            return new CSSToken(CSSTokenType.SKIP, StringUtils.whitespaceSubstring(css, index));
        } else if (c == '}') {
            contextStack.remove(contextStack.size() - 1);
            return new CSSToken(CSSTokenType.AT_RULE_CLOSE_BRACKET, '}');
        } else {
            int openIndex = css.indexOf("{", index);
            int closeIndex = css.indexOf("}", index);
            if (openIndex != -1 && openIndex < closeIndex) {
                // If a new open bracket occurs before a close, assume this is a nested @rule.
                String selector = StringUtils.substringUntil(css, index, "{");
                return new CSSToken(CSSTokenType.SELECTOR, selector);
            } else {
                // Assume this is not a nested @rule.
                return handleSelectorOpenBracket(css, index, contextStack);
            }
        }
    }

    private static CSSToken handleAtRuleCloseBracket(String css, int index) {
        char c = css.charAt(index);
        if (Character.isWhitespace(c)) {
            return new CSSToken(CSSTokenType.SKIP, StringUtils.whitespaceSubstring(css, index));
        } else if (c == '@') {
            String atRule = StringUtils.substringUntil(css, index, "{");
            return new CSSToken(CSSTokenType.AT_RULE, atRule);
        } else {
            String selector = StringUtils.substringUntil(css, index, "{");
            return new CSSToken(CSSTokenType.SELECTOR, selector);
        }
    }

    // OPEN_BRACKET: Handles characters after an opening bracket: "div, span {?"
    private static CSSToken handleSelectorOpenBracket(String css, int index, List<BlockContext> contextStack) {
        char c = css.charAt(index);
        if (Character.isWhitespace(c)) {
            return new CSSToken(CSSTokenType.SKIP, StringUtils.whitespaceSubstring(css, index));
        } else if (c == '}') {
            contextStack.remove(contextStack.size() - 1);
            return new CSSToken(CSSTokenType.SELECTOR_CLOSE_BRACKET, "}");
        } else if (StringUtils.substringMatch(css, "/*", index)) {
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
    private static CSSToken handlePropertyValue(String css, int index, List<BlockContext> contextStack) {
        if (css.charAt(index) == ';') {
            return new CSSToken(CSSTokenType.SEMI_COLON, ";");
        } else if (css.charAt(index) == '}') {
            contextStack.remove(contextStack.size() - 1);
            return new CSSToken(CSSTokenType.SELECTOR_CLOSE_BRACKET, '}');
        }
        return null;
    }

    // SEMI_COLON: Handles characters after a property semicolon: "div, span { font-size: 10px;?"
    private static CSSToken handleSemiColon(String css, int index, List<BlockContext> contextStack) {
        char c = css.charAt(index);
        if (Character.isWhitespace(c)) {
            return new CSSToken(CSSTokenType.SKIP, StringUtils.whitespaceSubstring(css, index));
        }

        if (c == '}') {
            BlockContext currentContext = contextStack.remove(contextStack.size() - 1);
            if (currentContext.equals(BlockContext.SingularAtRule)) {
                return new CSSToken(CSSTokenType.AT_RULE_CLOSE_BRACKET, '}');
            } else {
                return new CSSToken(CSSTokenType.SELECTOR_CLOSE_BRACKET, '}');
            }
        }

        // TODO check for next char being : or ;

        String name = StringUtils.substringUntil(css, index, ":");
        return new CSSToken(CSSTokenType.PROPERTY_NAME, name);
    }

    // CLOSE_BRACKET: Handles characters after a close bracket: "div, span { font-size: 10px; }?"
    private static CSSToken handleSelectorCloseBracket(String css, int index, List<BlockContext> contextStack) {
        char c = css.charAt(index);
        if (Character.isWhitespace(c)) {
            return new CSSToken(CSSTokenType.SKIP, StringUtils.whitespaceSubstring(css, index));
        } else if (c == '@') {
            String atRule = StringUtils.substringUntil(css, index, "{");
            return new CSSToken(CSSTokenType.AT_RULE, atRule.trim());
        } else if (c == '}') {
            contextStack.remove(contextStack.size() - 1);
            return new CSSToken(CSSTokenType.AT_RULE_CLOSE_BRACKET, '}');
        } else {
            String selector = StringUtils.substringUntil(css, index, "{");
            return new CSSToken(CSSTokenType.SELECTOR, selector);
        }
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
    private static CSSToken handleCommentEnd(String css, int index, CSSTokenType lastNonCommentToken, List<BlockContext> contextStack) {
        char c = css.charAt(index);
        if (Character.isWhitespace(c)) {
            String whitespace = StringUtils.whitespaceSubstring(css, index);
            return new CSSToken(CSSTokenType.SKIP, whitespace);
        } else {
            return getToken(css, index, lastNonCommentToken, null, contextStack);
        }
    }

}
