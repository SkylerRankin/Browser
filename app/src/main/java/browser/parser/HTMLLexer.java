package browser.parser;

import java.util.ArrayList;
import java.util.List;

import browser.model.HTMLToken;

public class HTMLLexer {

    public enum HTMLTokenType {
        TAG_OPEN, // "<"
        TAG_END_OPEN, // "</"
        TAG_CLOSE, // ">"
        TAG_END_CLOSE, // "/>"
        TAG_NAME,
        ATTRIBUTE_NAME,
        ATTRIBUTE_EQUALS,
        ATTRIBUTE_VALUE,
        ATTRIBUTE_START_QUOTES,
        ATTRIBUTE_END_QUOTES,
        WHITE_SPACE,
        TEXT,
        COMMENT_START,
        COMMENT,
        COMMENT_END,
        // Meta tags
        SKIP
    }

    private final String input;

    private String currentTagName = "none";
    private boolean inScript;
    private boolean inComment;
    private HTMLTokenType lastOpeningTagType = HTMLTokenType.TAG_END_OPEN;

    public HTMLLexer(String input) {
        this.input = input;
    }

    public List<HTMLToken> getTokens() {
        List<HTMLToken> tokens = new ArrayList<>();

        int index = 0;
        HTMLTokenType lastToken = HTMLTokenType.TAG_CLOSE;

        while (index < input.length()) {
            HTMLToken token = getToken(index, lastToken);
            if (token.type.equals(HTMLTokenType.TAG_NAME)) {
                currentTagName = token.value;
                if (lastToken.equals(HTMLTokenType.TAG_END_OPEN) && token.value.equalsIgnoreCase("script")) {
                    inScript = false;
                }
            } else if (lastOpeningTagType.equals(HTMLTokenType.TAG_OPEN) &&
                    token.type.equals(HTMLTokenType.TAG_CLOSE) &&
                    currentTagName.equalsIgnoreCase("script")) {
                inScript = true;
            }

            if (!token.type.equals(HTMLTokenType.SKIP)) {
                tokens.add(token);
                lastToken = token.type;
            }

            if (token.type.equals(HTMLTokenType.TAG_OPEN)) {
                lastOpeningTagType = HTMLTokenType.TAG_OPEN;
            } else if (token.type.equals(HTMLTokenType.TAG_END_OPEN)) {
                lastOpeningTagType = HTMLTokenType.TAG_END_OPEN;
            }

            index += token.value.length();
        }

        return tokens;
    }

    private HTMLToken getToken(int index, HTMLTokenType lastToken) {
        char c = input.charAt(index);

        // Comment starts can be detected independent of previous token type.
        if (StringUtils.substringMatch(input, "<!--", index)) {
            return new HTMLToken(HTMLTokenType.COMMENT_START, "<!--");
        }

        switch (lastToken) {
            case TAG_OPEN, TAG_END_OPEN -> { return handleTagOpen(index, c); }
            case TAG_CLOSE, TAG_END_CLOSE -> { return handleTagClose(index, c); }
            case TAG_NAME -> { return handleTagName(index, c); }
            case ATTRIBUTE_NAME -> { return handleAttributeName(index, c); }
            case ATTRIBUTE_EQUALS -> { return handleAttributeEquals(index, c); }
            case ATTRIBUTE_VALUE -> { return handleAttributeValue(index, c); }
            case ATTRIBUTE_START_QUOTES -> { return handleAttributeStartQuotes(index, c); }
            case ATTRIBUTE_END_QUOTES -> { return handleAttributeEndQuotes(index, c); }
            case TEXT -> { return handleText(index, c); }
            case COMMENT_START -> { return handleCommentStart(index); }
            case COMMENT -> { return handleComment(index); }
            case COMMENT_END -> { return handleCommentEnd(index, c); }
        }

        return null;
    }

    // Handling for each token type

    // TAG_OPEN/TAG_END_OPEN: Handles characters occurring after a tag opening: "<?"
    private HTMLToken handleTagOpen(int index, char c) {
        // </
        if (StringUtils.substringMatch(input, "</", index)) {
            return new HTMLToken(HTMLTokenType.TAG_END_OPEN, "</");
        }

        // <
        if (c == '<') {
            return new HTMLToken(HTMLTokenType.TAG_OPEN, c);
        }

        // "<>"
        if (c == '>') {
            return new HTMLToken(HTMLTokenType.TAG_CLOSE, c);
        }

        // "</>"
        if (StringUtils.substringMatch(input, "/>", index)) {
            return new HTMLToken(HTMLTokenType.TAG_END_CLOSE, "/>");
        }

        // Skip whitespace here, its meaningless
        if (Character.isWhitespace(c)) {
            return new HTMLToken(HTMLTokenType.SKIP, StringUtils.whitespaceSubstring(input, index));
        }

        String name = StringUtils.substringUntilSpaceOrString(input, index, List.of(">", "/>"));
        return new HTMLToken(HTMLTokenType.TAG_NAME, name);
    }

    // TAG_CLOSE/TAG_END_CLOSE: Handles characters occurring after a tag closing: ">?"
    private HTMLToken handleTagClose(int index, char c) {
        // A new tag opening: "></"
        if (StringUtils.substringMatch(input, "</", index)) {
            return new HTMLToken(HTMLTokenType.TAG_END_OPEN, "</");
        }

        // A new tag opening: "><"
        if (c == '<') {
            return new HTMLToken(HTMLTokenType.TAG_OPEN, c);
        }

        String endString = inScript ? "</script" : "<";
        String text = StringUtils.substringUntil(input, index, List.of(endString));
        return new HTMLToken(HTMLTokenType.TEXT, text);
    }

    // TAG_NAME: Handles characters occurring after a tag name: "<div?"
    private HTMLToken handleTagName(int index, char c) {
        // Whitespace: "<div "
        // this whitespace is skipped
        if (Character.isWhitespace(c)) {
            return new HTMLToken(HTMLTokenType.SKIP, StringUtils.whitespaceSubstring(input, index));
        }

        if (c == '>') {
            return new HTMLToken(HTMLTokenType.TAG_CLOSE, '>');
        }

        if (StringUtils.substringMatch(input, "/>", index)) {
            return new HTMLToken(HTMLTokenType.TAG_END_CLOSE, "/>");
        }

        // Whatever text is there will be the attribute name. Name stops at next space or >, />, =.
        String attribute = StringUtils.substringUntilSpaceOrString(input, index, List.of(">", "/>", "="));
        return new HTMLToken(HTMLTokenType.ATTRIBUTE_NAME, attribute);
    }

    // ATTRIBUTE_NAME: Handles characters occurring after an attribute name: "<div class?"
    private HTMLToken handleAttributeName(int index, char c) {
        // Whitespace: "<div disabled "
        if (Character.isWhitespace(c)) {
            return new HTMLToken(HTMLTokenType.SKIP, StringUtils.whitespaceSubstring(input, index));
        }

        // "<div class="
        if (c == '=') {
            return new HTMLToken(HTMLTokenType.ATTRIBUTE_EQUALS, '=');
        }

        // "<div disabled>
        if (c == '>') {
            return new HTMLToken(HTMLTokenType.TAG_CLOSE, '>');
        }

        // "<button disabled/>
        if (StringUtils.substringMatch(input, "/>", index)) {
            return new HTMLToken(HTMLTokenType.TAG_END_CLOSE, "/>");
        }

        // previous attribute was singular, new attribute
        String attribute = StringUtils.substringUntilSpaceOrString(input, index, List.of(">", "/>", "="));
        return new HTMLToken(HTMLTokenType.ATTRIBUTE_NAME, attribute);
    }

    // ATTRIBUTE_EQUALS: Handles characters occurring after an attribute equals sign: "<div class=?"
    private HTMLToken handleAttributeEquals(int index, char c) {
        // Whitespace: "<div disabled= "
        // this whitespace is skipped
        if (Character.isWhitespace(c)) {
            return new HTMLToken(HTMLTokenType.SKIP, StringUtils.whitespaceSubstring(input, index));
        }

        // "<div disabled=>
        if (c == '>') {
            return new HTMLToken(HTMLTokenType.TAG_CLOSE, '>');
        }

        // "<button disabled=/>
        if (StringUtils.substringMatch(input, "/>", index)) {
            return new HTMLToken(HTMLTokenType.TAG_END_CLOSE, "/>");
        }

        if (c == '"' || c == '\'') {
            return new HTMLToken(HTMLTokenType.ATTRIBUTE_START_QUOTES, c);
        }

        // Attribute value not wrapped in quotes: "<td rowspan=2"
        String attributeValue = StringUtils.substringUntilSpaceOrString(input, index, List.of(">", "/>"));
        return new HTMLToken(HTMLTokenType.ATTRIBUTE_VALUE, attributeValue);
    }

    // ATTRIBUTE_VALUE: Handles characters occurring after an attribute value: "<rd rowspan="1"
    private HTMLToken handleAttributeValue(int index, char c) {
        // Ending quotation
        if (c == '"' || c == '\'') {
            return new HTMLToken(HTMLTokenType.ATTRIBUTE_END_QUOTES, c);
        }

        // "<div disabled=>
        if (c == '>') {
            return new HTMLToken(HTMLTokenType.TAG_CLOSE, '>');
        }

        // "<button disabled=/>
        if (StringUtils.substringMatch(input, "/>", index)) {
            return new HTMLToken(HTMLTokenType.TAG_END_CLOSE, "/>");
        }

        // attributes not wrapped in quotes are split on spaces
        if (Character.isWhitespace(c)) {
            return new HTMLToken(HTMLTokenType.SKIP, StringUtils.whitespaceSubstring(input, index));
        }

        // Attribute value not wrapped in quotes: "<td rowspan=2"
        String attributeValue = StringUtils.substringUntilSpaceOrString(input, index, List.of(">", "/>"));
        return new HTMLToken(HTMLTokenType.ATTRIBUTE_NAME, attributeValue);
    }

    // ATTRIBUTE_START_QUOTES: Handles characters occurring after an attribute's starting quotation: "<a href=""
    private HTMLToken handleAttributeStartQuotes(int index, char c) {
        if (c == '>') {
            return new HTMLToken(HTMLTokenType.TAG_CLOSE, '>');
        }

        if (StringUtils.substringMatch(input, "/>", index)) {
            return new HTMLToken(HTMLTokenType.TAG_END_CLOSE, "/>");
        }

        if (c == '"' || c == '\'') {
            return new HTMLToken(HTMLTokenType.ATTRIBUTE_END_QUOTES, c);
        }

        // All the text until the next quote
        String attributeValue = StringUtils.substringUntil(input, index, List.of("'", "\""));
        return new HTMLToken(HTMLTokenType.ATTRIBUTE_VALUE, attributeValue);
    }

    // ATTRIBUTE_END_QUOTES: Handles characters occurring after an attribute's starting quotation: "<a href="www""
    private HTMLToken handleAttributeEndQuotes(int index, char c) {
        if (Character.isWhitespace(c)) {
            return new HTMLToken(HTMLTokenType.SKIP, StringUtils.whitespaceSubstring(input, index));
        }

        if (c == '>') {
            return new HTMLToken(HTMLTokenType.TAG_CLOSE, '>');
        }

        if (StringUtils.substringMatch(input, "/>", index)) {
            return new HTMLToken(HTMLTokenType.TAG_END_CLOSE, "/>");
        }

        // Whatever text is there will be the attribute name. Name stops at next space or >, />, =.
        String attribute = StringUtils.substringUntilSpaceOrString(input, index, List.of(">", "/>", "="));
        return new HTMLToken(HTMLTokenType.ATTRIBUTE_NAME, attribute);
    }

    private HTMLToken handleText(int index, char c) {
        if (StringUtils.substringMatch(input, "</", index)) {
            return new HTMLToken(HTMLTokenType.TAG_END_OPEN, "</");
        }

        if (c == '<') {
            return new HTMLToken(HTMLTokenType.TAG_OPEN, c);
        }

        return null;
    }

    private HTMLToken handleCommentStart(int index) {
        inComment = true;
        String commentText = StringUtils.substringUntil(input, index, "-->");
        return new HTMLToken(HTMLTokenType.COMMENT, commentText);
    }

    private HTMLToken handleComment(int index) {
        inComment = false;
        return new HTMLToken(HTMLTokenType.COMMENT_END, "-->");
    }

    private HTMLToken handleCommentEnd(int index, char c) {
        // A new tag opening: "></"
        if (StringUtils.substringMatch(input, "</", index)) {
            return new HTMLToken(HTMLTokenType.TAG_END_OPEN, "</");
        }

        // A new tag opening: "><"
        if (c == '<') {
            return new HTMLToken(HTMLTokenType.TAG_OPEN, c);
        }

        String text = StringUtils.substringUntil(input, index, List.of("<"));
        return new HTMLToken(HTMLTokenType.TEXT, text);
    }

}
