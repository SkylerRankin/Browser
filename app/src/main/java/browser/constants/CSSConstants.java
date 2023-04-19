package browser.constants;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import browser.css.CSSStyle;

public class CSSConstants {

    public static final String IMPORTANT = "!important";

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

    public static final Pattern CSS_MEDIA_KEY_VALUE_PATTERN = Pattern.compile("^\\(([^\s]+):\s*([^\s]+)\\)$");

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
            "background-color",
            "color",
            "font-family",
            "font-size",
            "font-style",
            "font-weight",
            "text-align"
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

}
