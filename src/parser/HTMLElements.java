package parser;

public class HTMLElements {
    
    public static final String TEXT = "text";
    
    public static final String DOCTYPE = "!doctype";
    
    public static final String HTML = "html";
    public static final String HEAD = "head";
    public static final String TITLE = "title";
    public static final String BODY = "body";
    
    public static final String H1 = "h1";
    public static final String H2 = "h2";
    public static final String H3 = "h3";
    public static final String H4 = "h4";
    public static final String H5 = "h5";
    public static final String H6 = "h6";
    public static final String DIV = "div";
    public static final String P = "p";
    public static final String INPUT = "input";
    
    public static final String[] RENDER_TREE_ELEMENTS = {BODY, TEXT, H1, H2, H3, H4, H5, H6, DIV, P, INPUT};
    public static final String[] BLOCK_LEVEL_ELEMENTS = {H1, H2, H3, H4, H5, H6, DIV, P};
    
    public boolean isValidElement(String element) {
        return false;
    }
    
    public static boolean usedInRenderTree(String element) {
        for (int i = 0; i < RENDER_TREE_ELEMENTS.length; ++i) {
            if (RENDER_TREE_ELEMENTS[i].equals(element)) return true;
        }
        return false;
    }

}
