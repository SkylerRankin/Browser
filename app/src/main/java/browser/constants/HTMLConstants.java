package browser.constants;

import java.util.Set;

import browser.parser.HTMLElements;

public class HTMLConstants {

    public static final Set<String> voidElements = Set.of(
            HTMLElements.AREA,
            HTMLElements.BASE,
            HTMLElements.BR,
            HTMLElements.COL,
            HTMLElements.EMBED,
            HTMLElements.HR,
            HTMLElements.IMG,
            HTMLElements.INPUT,
            HTMLElements.LINK,
            HTMLElements.META,
            HTMLElements.SOURCE,
            HTMLElements.TRACK,
            HTMLElements.WBR
    );

    public static final Set<String> docTypeStrings = Set.of("!doctype", "doctype");

    public static final Set<String> elementsExcludedFromRender = Set.of(
            HTMLElements.HEAD,
            HTMLElements.LINK,
            HTMLElements.SCRIPT,
            HTMLElements.STYLE
    );

    public static int LIST_MARKER_GAP = 10;

}
