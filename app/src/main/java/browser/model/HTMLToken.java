package browser.model;

import browser.parser.HTMLLexer.HTMLTokenType;

public class HTMLToken {
    public HTMLTokenType type;
    public String value;

    public HTMLToken(HTMLTokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    public HTMLToken(HTMLTokenType type, char value) {
        this.type = type;
        this.value = String.valueOf(value);
    }
}
