package browser.model;

import browser.parser.CSSLexer;

public class CSSToken {

    public CSSLexer.CSSTokenType type;
    public String value;

    public CSSToken(CSSLexer.CSSTokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    public CSSToken(CSSLexer.CSSTokenType type, char value) {
        this.type = type;
        this.value = String.valueOf(value);
    }
}
