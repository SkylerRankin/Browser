package browser.parser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class HTMLElements {
    
    public static final String TEXT = "text";
    
    public static final String DOCTYPE = "!doctype";
    public static final String HTML = "html";
    public static final String HEAD = "head";
    public static final String META = "meta";
    public static final String STYLE = "style";
    public static final String TITLE = "title";
    public static final String BODY = "body";
    
    public static final String H1 = "h1";
    public static final String H2 = "h2";
    public static final String H3 = "h3";
    public static final String H4 = "h4";
    public static final String H5 = "h5";
    public static final String H6 = "h6";
    public static final String HEADER = "header";
    public static final String DIV = "div";
    public static final String SPAN = "span";
    public static final String P = "p";
    public static final String INPUT = "input";
    public static final String UL = "ul";
    public static final String OL = "ol";
    public static final String LI = "li";
    public static final String HR = "hr";
    public static final String B = "b";
    public static final String IMG = "img";
    public static final String A = "a";
    public static final String TABLE = "table";
    public static final String THEAD = "thead";
    public static final String TBODY = "tbody";
    public static final String TFOOT = "tfoot";
    public static final String TR = "tr";
    public static final String TD = "td";
    public static final String COL = "col";
    public static final String COLGROUP = "colgroup";
    public static final String CAPTION = "caption";
    public static final String PRE = "pre";
    public static final String TT = "tt";
    public static final String BR = "br";
    public static final String LINK = "link";
    public static final String CODE = "code";
    public static final String EM = "em";
    public static final String STRONG = "strong";
    public static final String CENTER = "center";
    public static final String NOBR = "nobr";
    public static final String ASIDE = "aside";

    public static final String PSEUDO_MARKER = "marker";
    public static final String ANONYMOUS = "anonymous";
    
    private static final String[] VALID_ELEMENTS = {TEXT, HTML, HEAD, STYLE, TITLE, BODY, H1, H2, H3, H4, H5, H6, HEADER, DIV, SPAN, P, UL, OL, LI, HR, B, IMG, A, PRE, TT, CODE, EM, STRONG, CENTER, TABLE, THEAD, TBODY, TFOOT, TR, TD, COL, COLGROUP, CAPTION, ASIDE, BR};
    private static final String[] EMPTY_ELEMENTS = {DOCTYPE, IMG, BR, LINK, BR, INPUT, META, HR};
    public static final String[] NOT_IN_RENDER_TREE_ELEMENTS = {STYLE, HTML, HEAD, TITLE};

    private static Set<String> validElements;
    private static Set<String> emptyElements;
    private static Set<String> renderElements;
    private static Set<String> pseudoElements;
    
    public static void init() {
        validElements = new HashSet<String>();
        emptyElements = new HashSet<String>();
        renderElements = new HashSet<String>();
        pseudoElements = new HashSet<>();

        validElements.addAll(Arrays.asList(VALID_ELEMENTS));
        emptyElements.addAll(Arrays.asList(EMPTY_ELEMENTS));
        pseudoElements.add(PSEUDO_MARKER);
        renderElements.addAll(Arrays.asList(VALID_ELEMENTS));
        renderElements.removeAll(Arrays.asList(NOT_IN_RENDER_TREE_ELEMENTS));
    }
    
    public static boolean isValidElement(String element) {
        return element.equals(DOCTYPE) || validElements.contains(element);
    }
    
    public static boolean isEmptyElement(String element) {
        return emptyElements.contains(element);
    }

    public static boolean usedInRenderTree(String element) {
        return renderElements.contains(element);
    }

    public static boolean isPseudoElement(String element) {
        return pseudoElements.contains(element);
    }

    public static boolean isAnonymousElement(String element) {
        return element.equals(ANONYMOUS);
    }

}
