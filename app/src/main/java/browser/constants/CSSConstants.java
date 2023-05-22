package browser.constants;

import java.util.*;
import java.util.regex.Pattern;

import browser.css.CSSStyle;
import browser.model.CSSColor;

public class CSSConstants {

    public static final String IMPORTANT = "!important";
    public static final String INHERIT = "inherit";
    public static final float BASE_FONT_SIZE = 16;

    public enum SelectorType {
        UNIVERSAL,
        TYPE,
        CLASS,
        ID,
        ATTRIBUTE,
        PSEUDO
    }

    public enum SelectorCombinator {
        ADJACENT_SIBLING,
        CHILD,
        DESCENDANT,
        SIBLING
    }

    public enum AttributeSelectorComparisonType {
        NONE, // [attr], matches regardless of attribute value
        EXACT, // [attr=value], matches if attribute value is exactly value
        MEMBER_IN_LIST, // [attr~=value], matches when the attribute value is a whitespace-separated list, containing value
        HYPHEN, // [attr|=value], matches value exactly, or if value is followed by a hyphen
        PREFIX, // [attr^=value], matches when attribute value is preceded by value
        SUFFIX, // [attr$=value], matches when attribute value is suffixed by value
        OCCURRENCE // [attr*=value], matches when value occurs somewhere in the attribute value
    }

    public enum MediaType {
        ALL,
        PRINT,
        SCREEN
    }

    public enum MediaFeature {
        HEIGHT,
        MAX_WIDTH,
        MIN_WIDTH,
        ORIENTATION,
        WIDTH
    }

    public enum MediaQueryOperator {
        NOT,
        AND,
        ONLY,
        OR
    }

    public enum LengthUnit {
        // Font relative units
        CH, EM, EX, IC, REM,
        // Viewport relative units
        VH, VW, VMAX, VMIN, VB, VI,
        // Container query units
        CQW, CQH, CQI, CQB, CQMIN, CQMAX,
        // Absolute units
        PX, CM, MM, Q, IN, PC, PT
    }

    public static final List<String> MEDIA_QUERY_OPERATOR_STRINGS = Arrays.stream(MediaQueryOperator.values()).map(Enum::name).toList();

    public static final Pattern CSS_MEDIA_KEY_VALUE_PATTERN = Pattern.compile("^\\(([^\s]+)\s*:\s*([^\s]+)\\)$");

    public static final Pattern CSS_IDENTIFIER_PATTERN = Pattern.compile("[0-9a-zA-Z\\-_]+");

    public static final Pattern CSS_LENGTH_PATTERN = Pattern.compile("^([0-9.]+)([a-zA-Z]{1,5})$");

    public static final Set<Character> CSS_COMBINATOR_CHARACTERS = Set.of('>', '~', '+');

    public static final Map<String, SelectorCombinator> STRING_SELECTOR_COMBINATOR_MAP = Map.ofEntries(
            Map.entry(" ", SelectorCombinator.DESCENDANT),
            Map.entry(">", SelectorCombinator.CHILD),
            Map.entry("~", SelectorCombinator.SIBLING),
            Map.entry("+", SelectorCombinator.ADJACENT_SIBLING)
    );

    public final static List<CSSStyle.DisplayType> outerDisplayTypes = List.of(
            CSSStyle.DisplayType.BLOCK,
            CSSStyle.DisplayType.INLINE,
            CSSStyle.DisplayType.RUN_IN
    );

    public static final List<CSSStyle.DisplayType> tableInnerDisplayTypes = List.of(
            CSSStyle.DisplayType.TABLE,
            CSSStyle.DisplayType.TABLE_HEADER_GROUP,
            CSSStyle.DisplayType.TABLE_ROW_GROUP,
            CSSStyle.DisplayType.TABLE_FOOTER_GROUP,
            CSSStyle.DisplayType.TABLE_COLUMN_GROUP,
            CSSStyle.DisplayType.TABLE_CAPTION,
            CSSStyle.DisplayType.TABLE_ROW,
            CSSStyle.DisplayType.TABLE_COLUMN,
            CSSStyle.DisplayType.TABLE_CELL
    );

    // Mappings from the CSS display type string to the corresponding display enum.
    private final static Map<String, CSSStyle.DisplayType> displayTypeStringToType = Map.ofEntries(
            Map.entry("block", CSSStyle.DisplayType.BLOCK),
            Map.entry("inline", CSSStyle.DisplayType.INLINE),
            Map.entry("run-in", CSSStyle.DisplayType.RUN_IN),
            Map.entry("flow", CSSStyle.DisplayType.FLOW),
            Map.entry("flow-root", CSSStyle.DisplayType.FLOW_ROOT),
            Map.entry("table", CSSStyle.DisplayType.TABLE),
            Map.entry("flex", CSSStyle.DisplayType.FLEX),
            Map.entry("grid", CSSStyle.DisplayType.GRID),
            Map.entry("ruby", CSSStyle.DisplayType.RUBY),
            Map.entry("list-item", CSSStyle.DisplayType.LIST_ITEM),
            Map.entry("table-row-group", CSSStyle.DisplayType.TABLE_ROW_GROUP),
            Map.entry("table-header-group", CSSStyle.DisplayType.TABLE_HEADER_GROUP),
            Map.entry("table-footer-group", CSSStyle.DisplayType.TABLE_FOOTER_GROUP),
            Map.entry("table-row", CSSStyle.DisplayType.TABLE_ROW),
            Map.entry("table-cell", CSSStyle.DisplayType.TABLE_CELL),
            Map.entry("table-column-group", CSSStyle.DisplayType.TABLE_COLUMN_GROUP),
            Map.entry("table-column", CSSStyle.DisplayType.TABLE_COLUMN),
            Map.entry("table-caption", CSSStyle.DisplayType.TABLE_CAPTION),
            Map.entry("ruby-base", CSSStyle.DisplayType.RUBY_BASE),
            Map.entry("ruby-text", CSSStyle.DisplayType.RUBY_TEXT),
            Map.entry("ruby-base-container", CSSStyle.DisplayType.RUBY_BASE_CONTAINER),
            Map.entry("ruby-text-container", CSSStyle.DisplayType.RUBY_TEXT_CONTAINER),
            Map.entry("contents", CSSStyle.DisplayType.CONTENTS),
            Map.entry("none", CSSStyle.DisplayType.NONE),
            Map.entry("inline-block", CSSStyle.DisplayType.INLINE_BLOCK),
            Map.entry("inline-table", CSSStyle.DisplayType.INLINE_TABLE),
            Map.entry("inline-flex", CSSStyle.DisplayType.INLINE_FLEX),
            Map.entry("inline-grid", CSSStyle.DisplayType.INLINE_GRID)
    );

    // Some legacy display types have hyphens but are not singular values, or have implied inner types not specified.
    // The inner and outer display types for these are set here.
    private final static Map<CSSStyle.DisplayType, List<CSSStyle.DisplayType>> displayTypeOverrides = Map.ofEntries(
            Map.entry(CSSStyle.DisplayType.INLINE_BLOCK, List.of(CSSStyle.DisplayType.INLINE, CSSStyle.DisplayType.FLOW_ROOT)),
            Map.entry(CSSStyle.DisplayType.INLINE_TABLE, List.of(CSSStyle.DisplayType.INLINE, CSSStyle.DisplayType.TABLE)),
            Map.entry(CSSStyle.DisplayType.INLINE_FLEX, List.of(CSSStyle.DisplayType.INLINE, CSSStyle.DisplayType.FLEX)),
            Map.entry(CSSStyle.DisplayType.INLINE_GRID, List.of(CSSStyle.DisplayType.INLINE, CSSStyle.DisplayType.GRID)),
            Map.entry(CSSStyle.DisplayType.LIST_ITEM, List.of(CSSStyle.DisplayType.BLOCK, CSSStyle.DisplayType.FLOW, CSSStyle.DisplayType.LIST_ITEM)),
            Map.entry(CSSStyle.DisplayType.NONE, List.of(CSSStyle.DisplayType.NONE, CSSStyle.DisplayType.NONE, CSSStyle.DisplayType.NONE))
            );

    public static final List<String> borderLineStyles = List.of(
            "none",
            "hidden",
            "dotted",
            "dashed",
            "solid",
            "double",
            "groove",
            "ridge",
            "inset",
            "outset"
    );

    public static Set<String> inheritedProperties = Set.of(
            "color",
            "font-family",
            "font-size",
            "font-style",
            "font-weight",
            "text-align"
    );

    public static Map<String, CSSStyle.TextAlign> stringToNonStandardTextAlign = Map.ofEntries(
            Map.entry("-webkit-left", CSSStyle.TextAlign.WEBKIT_LEFT),
            Map.entry("-webkit-center", CSSStyle.TextAlign.WEBKIT_CENTER),
            Map.entry("-webkit-right", CSSStyle.TextAlign.WEBKIT_RIGHT)
    );

    public static List<String> textNodeInheritedProperties = List.of(
            "font-size",
            "font-weight",
            "font-style",
            "font-family",
            "color"
    );

    /**
     * Convert a CSS display type string into the matching display type enum value.
     * @param text      A CSS display type string.
     * @return      The matching DisplayType value.
     */
    public static CSSStyle.DisplayType getDisplayType(String text) {
        return displayTypeStringToType.get(text.toLowerCase());
    }

    /**
     * There are some legacy CSS display types that require special handling. For instance, the "inline-block" display
     * type is not meant to be treated as an outer display type of "inline" and an inner display type of "block".
     * Instead. it is converted to "inline" and "flow-root" respectively. This method returns such an override for a
     * given display type, if any exists.
     * @param type      The display type
     * @return      A list of the format [outer display type, inner display type].
     */
    public static List<CSSStyle.DisplayType> getDisplayTypeOverride(CSSStyle.DisplayType type) {
        return displayTypeOverrides.getOrDefault(type, null);
    }

    public static Map<String, Object> getDefaultProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("background-color", new CSSColor("rgba(0, 0, 0, 0)"));
        properties.put("border-color-top", new CSSColor("Black"));
        properties.put("border-color-bottom", new CSSColor("Black"));
        properties.put("border-color-left", new CSSColor("Black"));
        properties.put("border-color-right", new CSSColor("Black"));
        properties.put("border-width-top", 0);
        properties.put("border-width-bottom", 0);
        properties.put("border-width-left", 0);
        properties.put("border-width-right", 0);
        properties.put("border-style-top", CSSStyle.BorderStyle.NONE);
        properties.put("border-style-bottom", CSSStyle.BorderStyle.NONE);
        properties.put("border-style-right", CSSStyle.BorderStyle.NONE);
        properties.put("border-style-left", CSSStyle.BorderStyle.NONE);
        properties.put("box-sizing", CSSStyle.BoxSizingType.CONTENT_BOX);
        properties.put("border-spacing", 2);
        properties.put("color", new CSSColor("Black"));
        properties.put("display", CSSStyle.DisplayType.BLOCK);
        properties.put("inner-display", CSSStyle.DisplayType.FLOW);
        properties.put("outer-display", CSSStyle.DisplayType.BLOCK);
        properties.put("auxiliary-display", CSSStyle.DisplayType.NONE);
        properties.put("position", CSSStyle.PositionType.RELATIVE);
        properties.put("font-family", "Times New Roman");
        properties.put("font-size", BASE_FONT_SIZE);
        properties.put("font-style", CSSStyle.fontStyleType.NORMAL);
        properties.put("font-weight", CSSStyle.fontWeightType.NORMAL);
        properties.put("height-type", CSSStyle.DimensionType.PIXEL);
        properties.put("height", null);
        properties.put("margin-top", 0);
        properties.put("margin-top-type", CSSStyle.MarginType.LENGTH);
        properties.put("margin-top-unit", LengthUnit.PX);
        properties.put("margin-bottom", 0);
        properties.put("margin-bottom-type", CSSStyle.MarginType.LENGTH);
        properties.put("margin-bottom-unit", LengthUnit.PX);
        properties.put("margin-left", 0);
        properties.put("margin-left-type", CSSStyle.MarginType.LENGTH);
        properties.put("margin-left-unit", LengthUnit.PX);
        properties.put("margin-right", 0);
        properties.put("margin-right-type", CSSStyle.MarginType.LENGTH);
        properties.put("margin-right-unit", LengthUnit.PX);
        properties.put("max-height", null);
        properties.put("max-height-type", CSSStyle.DimensionType.PIXEL);
        properties.put("max-width", null);
        properties.put("max-width-type", CSSStyle.DimensionType.PIXEL);
        properties.put("padding-top", 0);
        properties.put("padding-top-type", CSSStyle.PaddingType.LENGTH);
        properties.put("padding-top-unit", LengthUnit.PX);
        properties.put("padding-bottom", 0);
        properties.put("padding-bottom-type", CSSStyle.PaddingType.LENGTH);
        properties.put("padding-bottom-unit", LengthUnit.PX);
        properties.put("padding-left", 0);
        properties.put("padding-left-type", CSSStyle.PaddingType.LENGTH);
        properties.put("padding-left-unit", LengthUnit.PX);
        properties.put("padding-right", 0);
        properties.put("padding-right-type", CSSStyle.PaddingType.LENGTH);
        properties.put("padding-right-unit", LengthUnit.PX);
        properties.put("text-align", CSSStyle.TextAlign.LEFT);
        properties.put("width", null);
        properties.put("width-type", CSSStyle.DimensionType.PIXEL);
        properties.put("word-wrap", CSSStyle.wordWrapType.NORMAL);
        return properties;
    }


    /**
     * Maps each property name to the list of fields that the property will set within a CSSStyle object. For instance,
     * the "height" property will actually set "height" and "height-type".
     */
    public static Map<String, List<String>> propertyNameToSetFields = Map.ofEntries(
            Map.entry("background", List.of("background-color")),
            Map.entry("background-color", List.of("background-color")),
            Map.entry("border", List.of("border-style-top", "border-style-right", "border-style-bottom", "border-style-left", "border-width-top", "border-width-right", "border-width-bottom", "border-width-left", "border-color-top", "border-color-right", "border-color-bottom", "border-color-left")),
            Map.entry("border-color", List.of("border-color-top", "border-color-right", "border-color-bottom", "border-color-left")),
            Map.entry("border-width", List.of("border-width-top", "border-width-right", "border-width-bottom", "border-width-left")),
            Map.entry("border-top", List.of("border-style-top", "border-width-top", "border-color-top")),
            Map.entry("border-top-color", List.of("border-top-color")),
            Map.entry("border-top-width", List.of("border-top-width")),
            Map.entry("border-bottom", List.of("border-style-bottom", "border-width-bottom", "border-color-bottom")),
            Map.entry("border-bottom-color", List.of("border-bottom-color")),
            Map.entry("border-bottom-width", List.of("border-bottom-width")),
            Map.entry("border-left", List.of("border-style-left", "border-width-left", "border-color-left")),
            Map.entry("border-left-color", List.of("border-left-color")),
            Map.entry("border-left-width", List.of("border-left-width")),
            Map.entry("border-right", List.of("border-style-right", "border-width-right", "border-color-right")),
            Map.entry("border-right-color", List.of("border-right-color")),
            Map.entry("border-right-width", List.of("border-right-width")),
            Map.entry("box-sizing", List.of("box-sizing")),
            Map.entry("border-spacing", List.of("border-spacing")),
            Map.entry("color", List.of("color")),
            Map.entry("display", List.of("outer-display", "inner-display", "auxiliary-display")),
            Map.entry("font-family", List.of("font-family")),
            Map.entry("font-size", List.of("font-size")),
            Map.entry("font-style", List.of("font-style")),
            Map.entry("font-weight", List.of("font-weight")),
            Map.entry("height", List.of("height", "height-type")),
            Map.entry("margin", List.of("margin-top", "margin-left", "margin-bottom", "margin-right", "margin-top-type", "margin-left-type", "margin-bottom-type", "margin-right-type", "margin-top-unit", "margin-left-unit", "margin-bottom-unit", "margin-right-unit")),
            Map.entry("margin-top", List.of("margin-top", "margin-top-type", "margin-top-unit")),
            Map.entry("margin-right", List.of("margin-right", "margin-right-type", "margin-right-unit")),
            Map.entry("margin-bottom", List.of("margin-bottom", "margin-bottom-type", "margin-bottom-unit")),
            Map.entry("margin-left", List.of("margin-left", "margin-left-type", "margin-left-unit")),
            Map.entry("max-width", List.of("max-width", "max-width-type")),
            Map.entry("max-height", List.of("max-height", "max-height-type")),
            Map.entry("padding", List.of("padding-top", "padding-top-type", "padding-top-unit", "padding-right", "padding-right-type", "padding-right-unit", "padding-bottom", "padding-bottom-type", "padding-bottom-unit", "padding-left", "padding-left-type", "padding-left-unit")),
            Map.entry("padding-top", List.of("padding-top", "padding-top-type", "padding-top-unit")),
            Map.entry("padding-right", List.of("padding-right", "padding-right-type", "padding-right-unit")),
            Map.entry("padding-bottom", List.of("padding-bottom", "padding-bottom-type", "padding-bottom-unit")),
            Map.entry("padding-left", List.of("padding-left", "padding-left-type", "padding-left-unit")),
            Map.entry("position", List.of("position")),
            Map.entry("text-align", List.of("text-align")),
            Map.entry("width", List.of("width", "width-type"))
            );

}
