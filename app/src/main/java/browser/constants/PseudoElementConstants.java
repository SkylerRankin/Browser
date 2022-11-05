package browser.constants;

import browser.css.CSSStyle;

public class PseudoElementConstants {

    // Style constants for pseudo-elements. These have to be manually set since they are auto-generated elements.
    public static float MARKER_WIDTH = 30f;
    public static float MARKER_HEIGHT = 20f;
    public static CSSStyle.displayType MARKER_DISPLAY_TYPE = CSSStyle.displayType.BLOCK;

    // Keys for accessing marker information in a render node's attribute map.
    public static String MARKER_TYPE_KEY = "LIST_MARKER_TYPE";
    public static String MARKER_INDEX_KEY = "LIST_MARKER_INDEX";

}
