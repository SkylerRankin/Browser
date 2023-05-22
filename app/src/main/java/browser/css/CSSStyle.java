package browser.css;

import static browser.constants.CSSConstants.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import browser.constants.CSSConstants;
import browser.model.CSSColor;
import browser.model.Dimension;
import browser.parser.StringUtils;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class CSSStyle {

    private final Map<String, Boolean> propertyImportant = new HashMap<>();
    private final Set<String> inheritedProperties = new HashSet<>();
    private final Set<String> propertiesToInherit = new HashSet<>(CSSConstants.inheritedProperties);

    public final Map<String, String> propertyStrings = new HashMap<>();
    public final Map<String, Object> properties = CSSConstants.getDefaultProperties();
    public final Map<String, CSSSpecificity> propertySpecificity = new HashMap<>();

    public enum DimensionType { PIXEL, PERCENTAGE }

    // Reference for all display type combinations: https://developer.mozilla.org/en-US/docs/Web/CSS/display.
    public enum DisplayType {
        // display-outside values
        BLOCK, INLINE, RUN_IN,
        // display-inside values
        FLOW, FLOW_ROOT, TABLE, FLEX, GRID, RUBY,
        // display-list-item values
        LIST_ITEM,
        // display-internal values
        TABLE_ROW_GROUP, TABLE_HEADER_GROUP, TABLE_FOOTER_GROUP,
        TABLE_ROW, TABLE_CELL, TABLE_COLUMN_GROUP, TABLE_COLUMN,
        TABLE_CAPTION, RUBY_BASE, RUBY_TEXT, RUBY_BASE_CONTAINER,
        RUBY_TEXT_CONTAINER,
        // display-box values
        CONTENTS, NONE,
        // display-legacy values
        INLINE_BLOCK, INLINE_TABLE, INLINE_FLEX, INLINE_GRID;
    }

    public enum PositionType { STATIC, RELATIVE, ABSOLUTE, FIXED, STICKY }

    public enum BoxSizingType { CONTENT_BOX, BORDER_BOX }

    public enum fontStyleType {NORMAL, ITALIC, ITALICS}

    public enum fontWeightType {NORMAL, BOLD, OTHER}

    public enum TextAlign {
        LEFT, CENTER, RIGHT,
        WEBKIT_LEFT, WEBKIT_CENTER, WEBKIT_RIGHT
    }

//    public static enum textDecorationType {NONE, OVERLINE, LINETHROUGH, UNDERLINE}

    public enum wordWrapType {NORMAL, BREAKWORD}

    public enum MarginType {AUTO, LENGTH, PERCENTAGE}

    public enum PaddingType {LENGTH, PERCENTAGE}

    public enum BorderStyle { NONE, SOLID }

    public CSSStyle parentStyle = null;

    public CSSColor backgroundColor = new CSSColor("rgba(0, 0, 0, 0)");
    
    public CSSColor borderColorTop = new CSSColor("Black");
    public CSSColor borderColorBottom = new CSSColor("Black");
    public CSSColor borderColorLeft = new CSSColor("Black");
    public CSSColor borderColorRight = new CSSColor("Black");

    public float borderWidthTop = 0;
    public float borderWidthRight = 0;
    public float borderWidthBottom = 0;
    public float borderWidthLeft = 0;

    public BorderStyle borderStyleTop = BorderStyle.NONE;
    public BorderStyle borderStyleRight = BorderStyle.NONE;
    public BorderStyle borderStyleBottom = BorderStyle.NONE;
    public BorderStyle borderStyleLeft = BorderStyle.NONE;

    public BoxSizingType boxSizing = BoxSizingType.CONTENT_BOX;

    public int borderSpacing = 2;
    
    public CSSColor color = new CSSColor("Black");
    
    public DisplayType display = DisplayType.BLOCK;
    public DisplayType innerDisplay;
    public DisplayType outerDisplay;
    // Some display types, such as list-item, set a third value in addition to the inner and outer display types. This
    // auxiliary display type captures that information.
    public DisplayType auxiliaryDisplay;

    public PositionType position = PositionType.STATIC;
    
    public String fontFamily = "Times New Roman";
    public float fontSize = BASE_FONT_SIZE;
    public fontStyleType fontStyle = fontStyleType.NORMAL;
    public fontWeightType fontWeight = fontWeightType.NORMAL;
    
    public DimensionType heightType = DimensionType.PIXEL;
    public Float height = null;
    
    public int marginTop = 0;
    public MarginType marginTopType = MarginType.LENGTH;
    public LengthUnit marginTopUnit = LengthUnit.PX;

    public int marginRight = 0;
    public MarginType marginRightType = MarginType.LENGTH;
    public LengthUnit marginRightUnit = LengthUnit.PX;

    public int marginBottom = 0;
    public MarginType marginBottomType = MarginType.LENGTH;
    public LengthUnit marginBottomUnit = LengthUnit.PX;

    public int marginLeft = 0;
    public MarginType marginLeftType = MarginType.LENGTH;
    public LengthUnit marginLeftUnit = LengthUnit.PX;

    public int paddingTop = 0;
    public PaddingType paddingTopType = PaddingType.LENGTH;
    public LengthUnit paddingTopUnit = LengthUnit.PX;

    public int paddingRight = 0;
    public PaddingType paddingRightType = PaddingType.LENGTH;
    public LengthUnit paddingRightUnit = LengthUnit.PX;

    public int paddingBottom = 0;
    public PaddingType paddingBottomType = PaddingType.LENGTH;
    public LengthUnit paddingBottomUnit = LengthUnit.PX;

    public int paddingLeft = 0;
    public PaddingType paddingLeftType = PaddingType.LENGTH;
    public LengthUnit paddingLeftUnit = LengthUnit.PX;
    
    public TextAlign textAlign = TextAlign.LEFT;
    
    public DimensionType widthType = DimensionType.PIXEL;
    public Float width = null;
    
    public Float maxWidth = null;
    public DimensionType maxWidthType = DimensionType.PIXEL;
    public Float maxHeight = null;
    public DimensionType maxHeightType = DimensionType.PIXEL;
    
    public wordWrapType wordWrap = wordWrapType.NORMAL;
    
    public void setProperty(String property, String value) {
        propertyStrings.put(property, value);
    }

    public boolean shouldInheritProperty(String property) {
        return propertiesToInherit.contains(property);
    }

    private Dimension parseSingleDimension(String text) {
        Dimension dimension = new Dimension();
        if (text.equalsIgnoreCase("none")) {
            dimension.value = null;
        } else if (text.endsWith("%") && text.length() > 1) {
            String percentageText = text.substring(0, text.length() - 1);
            if (percentageText.matches("[0-9]+")) {
                dimension.value = Float.parseFloat(percentageText);
                dimension.type = DimensionType.PERCENTAGE;
            }
        } else if (text.matches("[0-9]+")) {
            dimension.value = Float.parseFloat(text);
            dimension.type = DimensionType.PIXEL;
        } else {
            Matcher lengthMatcher = CSS_LENGTH_PATTERN.matcher(text);
            if (lengthMatcher.find()) {
                float length = Float.parseFloat(lengthMatcher.group(1));
                String unitString = lengthMatcher.group(2);
                LengthUnit unitCandidate = parseLengthUnit(unitString);
                if (unitCandidate != null) {
                    switch (unitCandidate) {
                        case PX -> dimension.value = length;
                        case EM -> {
                            float parentFontSize = parentStyle == null ? fontSize : parentStyle.fontSize;
                            dimension.value = length * parentFontSize;
                        }
                        case REM -> {
                            // TODO use the root font size, not the parent's.
                            float parentFontSize = parentStyle == null ? fontSize : parentStyle.fontSize;
                            dimension.value = length * parentFontSize;
                        }
                        default -> {
                            System.err.printf("Unsupported dimension type %s. Defaulting to pixel.\n", unitCandidate.name());
                            dimension.value = length;
                        }
                    }
                    dimension.type = DimensionType.PIXEL;
                }
            }
        }

        return dimension;
    }
    
    private void parseFontSizeValue(String value) {
        Float length = null;
        LengthUnit unit = null;
        if (value.endsWith("%")) {
            String lengthString = value.substring(0, value.length() - 1);
            if (lengthString.matches("[\\d.]+")) {
                length = Float.parseFloat(lengthString) / 100 * BASE_FONT_SIZE;
                unit = LengthUnit.PX;
            }
        } else {
            Matcher lengthMatcher = CSS_LENGTH_PATTERN.matcher(value);
            if (lengthMatcher.find()) {
                length = Float.parseFloat(lengthMatcher.group(1));
                String unitString = lengthMatcher.group(2);
                unit = parseLengthUnit(unitString);
                if (unit == null) {
                    unit = LengthUnit.PX;
                }
            }
        }

        if (length == null) {
            return;
        }

        switch (unit) {
            case PX -> properties.put("font-size", length);
            case PT -> properties.put("font-size", length * (1 + 1.0f / 3.0f));
            case EM -> properties.put("font-size", (parentStyle == null ? (float) properties.get("font-size") : (float) parentStyle.properties.get("font-size")) * length);
            case REM -> properties.put("font-size", BASE_FONT_SIZE * length);
        }
    }

    /**
     * The border property can contain up to 3 values, in any order: line width, line style, and line color.
     * @param value The CSS value string to parse
     */
    private void parseBorder(String value, String direction) {
        value = value.trim();
        if (value.endsWith(";")) {
            value = value.substring(0, value.length() - 1);
        }
        String[] items = value.split("\s");
        for (int i = 0; i < items.length; i++) {
            if (CSSConstants.borderLineStyles.contains(items[i])) {
                BorderStyle borderStyle;
                try {
                    borderStyle = BorderStyle.valueOf(items[i].toUpperCase());
                } catch (IllegalArgumentException e) {
                    System.out.printf("Unsupported border style \"%s\". Reverting to \"solid\".\n", items[i]);
                    borderStyle = BorderStyle.SOLID;
                }
                switch (direction) {
                    case "top" -> {
                        properties.put("border-style-top", borderStyle);
                    }
                    case "right" -> {
                        properties.put("border-style-right", borderStyle);
                    }
                    case "bottom" -> {
                        properties.put("border-style-bottom", borderStyle);
                    }
                    case "left" -> {
                        properties.put("border-style-left", borderStyle);
                    }
                    default -> {
                        properties.put("border-style-top", borderStyle);
                        properties.put("border-style-right", borderStyle);
                        properties.put("border-style-bottom", borderStyle);
                        properties.put("border-style-left", borderStyle);
                    }
                }
            } else {
                // Check if the value is a length
                if (items[i].equals("0")) {
                    // Zero is a special case that does not require a unit.
                    switch (direction) {
                        case "top" -> {
                            properties.put("border-width-top", 0);
                        }
                        case "right" -> {
                            properties.put("border-width-right", 0);
                        }
                        case "bottom" -> {
                            properties.put("border-width-bottom", 0);
                        }
                        case "left" -> {
                            properties.put("border-width-left", 0);
                        }
                        default -> {
                            properties.put("border-width-top", 0);
                            properties.put("border-width-right", 0);
                            properties.put("border-width-bottom", 0);
                            properties.put("border-width-left", 0);
                        }
                    }
                } else {
                    Matcher lengthMatcher = CSS_LENGTH_PATTERN.matcher(items[i]);
                    if (lengthMatcher.find()) {
                        float length = Float.parseFloat(lengthMatcher.group(1));
                        String unitString = lengthMatcher.group(2);
                        LengthUnit unit = parseLengthUnit(unitString);
                        if (unit != null) {
                            if (!unit.equals(LengthUnit.PX)) {
                                System.out.printf("Unsupported border width unit %s, defaulting to 1px.\n", unitString);
                                length = 1;
                            }
                            switch (direction) {
                                case "top" -> {
                                    properties.put("border-width-top", length);
                                }
                                case "right" -> {
                                    properties.put("border-width-right", length);
                                }
                                case "bottom" -> {
                                    properties.put("border-width-bottom", length);
                                }
                                case "left" -> {
                                    properties.put("border-width-left", length);
                                }
                                default -> {
                                    properties.put("border-width-top", length);
                                    properties.put("border-width-right", length);
                                    properties.put("border-width-bottom", length);
                                    properties.put("border-width-left", length);
                                }
                            }
                            continue;
                        }
                    }

                    // Check if the value is a color
                    CSSColor color = CSSColor.getColor(items[i]);
                    if (color != null) {
                        switch (direction) {
                            case "top" -> {
                                properties.put("border-color-top", color);
                            }
                            case "right" -> {
                                properties.put("border-color-right", color);
                            }
                            case "bottom" -> {
                                properties.put("border-color-bottom", color);
                            }
                            case "left" -> {
                                properties.put("border-color-left", color);
                            }
                            default -> {
                                properties.put("border-color-top", color);
                                properties.put("border-color-right", color);
                                properties.put("border-color-bottom", color);
                                properties.put("border-color-left", color);
                            }
                        }
                    }
                }
            }
        }
    }

    private LengthUnit parseLengthUnit(String value) {
        value = value.toUpperCase();
        try {
            return LengthUnit.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Populates the inner and outer display types based on a display CSS string. There are a few configurations
     * for this property.
     *   - Single value: This can be an inner or outer value. Depending on which it is, the remaining display type is
     *     set to a default value (block for outer, and flow for inner).
     *   - Hyphenated values: The first is the inner, second is the outer.
     *   - Spaced values: The first is the inner, second is the outer.
     * @param text      The CSS display property text to parse.
     */
    private void parseDisplayType(String text) {
        if (CSSConstants.getDisplayType(text) != null) {
            DisplayType singleType = CSSConstants.getDisplayType(text);
            if (singleType.equals(DisplayType.NONE)) {
                properties.put("outer-display", DisplayType.NONE);
                properties.put("inner-display", DisplayType.NONE);
                properties.put("auxiliary-display", DisplayType.NONE);
            } else if (CSSConstants.getDisplayTypeOverride(singleType) != null) {
                // Some display types have mappings to inner/outer display types that are not evident from their names.
                // For example, inline-block maps to inline flow-root.
                List<DisplayType> types = CSSConstants.getDisplayTypeOverride(singleType);
                properties.put("outer-display", types.get(0));
                properties.put("inner-display", types.get(1));
                if (types.size() == 3) {
                    properties.put("auxiliary-display", types.get(2));
                }
            } else if (CSSConstants.outerDisplayTypes.contains(singleType)) {
                properties.put("outer-display", singleType);
                properties.put("inner-display", DisplayType.FLOW);
            } else {
                properties.put("outer-display", DisplayType.BLOCK);
                properties.put("inner-display", singleType);
            }
        } else if (text.contains("-")) {
            properties.put("outer-display", CSSConstants.getDisplayType(text.substring(0, text.indexOf("-"))));
            properties.put("inner-display", CSSConstants.getDisplayType(text.substring(text.indexOf("-") + 1)));
        } else if (text.contains(" ")) {
            properties.put("outer-display", CSSConstants.getDisplayType(text.substring(0, text.indexOf(" "))));
            properties.put("inner-display", CSSConstants.getDisplayType(text.substring(text.indexOf(" ") + 1)));
        } else {
            properties.put("outer-display", DisplayType.BLOCK);
            properties.put("inner-display", DisplayType.FLOW);
            System.out.printf("CSSStyle.parseDisplayType: unknown display type %s, reverting to block.\n", text);
        }

        // TODO: should the basic display even be used anymore?
        // TODO: how to set the default inner. not sure if "flow" is the correct choice here.
    }

    private BoxSizingType parseBoxSizingType(String text) {
        switch (text) {
            case "content-box": return BoxSizingType.CONTENT_BOX;
            case "border-box": return BoxSizingType.BORDER_BOX;
            default: return BoxSizingType.CONTENT_BOX;
        }
    }

    private void parseIndividualMargin(String text, String direction) {
        int value = 0;
        MarginType type;
        LengthUnit unit = LengthUnit.PX;

        if (text.equalsIgnoreCase("auto")) {
            type = MarginType.AUTO;
        } else {
            Dimension dimension = parseSingleDimension(text);
            if (dimension.value == null) {
                return;
            }
            value = dimension.value.intValue();
            type = dimension.type.equals(DimensionType.PIXEL) ? MarginType.LENGTH : MarginType.PERCENTAGE;
        }

        switch (direction) {
            case "top" -> {
                properties.put("margin-top", value);
                properties.put("margin-top-type", type);
                properties.put("margin-top-unit", unit);
            }
            case "right" -> {
                properties.put("margin-right", value);
                properties.put("margin-right-type", type);
                properties.put("margin-right-unit", unit);
            }
            case "bottom" -> {
                properties.put("margin-bottom", value);
                properties.put("margin-bottom-type", type);
                properties.put("margin-bottom-unit", unit);
            }
            case "left" -> {
                properties.put("margin-left", value);
                properties.put("margin-left-type", type);
                properties.put("margin-left-unit", unit);
            }
        }
    }

    private void parseMargin(String text, String direction) {
        if (direction != null) {
            parseIndividualMargin(text, direction);
        } else {
            String[] items = text.split("\\s");
            switch (items.length) {
                case 1 -> {
                    parseIndividualMargin(text, "top");
                    parseIndividualMargin(text, "bottom");
                    parseIndividualMargin(text, "left");
                    parseIndividualMargin(text, "right");
                }
                case 2 -> {
                    parseIndividualMargin(items[0], "top");
                    parseIndividualMargin(items[0], "bottom");
                    parseIndividualMargin(items[1], "left");
                    parseIndividualMargin(items[1], "right");
                }
                case 3 -> {
                    parseIndividualMargin(items[0], "top");
                    parseIndividualMargin(items[2], "bottom");
                    parseIndividualMargin(items[1], "left");
                    parseIndividualMargin(items[1], "right");
                }
                case 4 -> {
                    parseIndividualMargin(items[0], "top");
                    parseIndividualMargin(items[2], "bottom");
                    parseIndividualMargin(items[3], "left");
                    parseIndividualMargin(items[1], "right");
                }
            }
        }
    }

    private void parseIndividualPadding(String text, String direction) {
        int value;
        PaddingType type;
        LengthUnit unit = LengthUnit.PX;

        Dimension dimension = parseSingleDimension(text);
        if (dimension.value == null) {
            return;
        }
        value = dimension.value.intValue();
        type =  dimension.type.equals(DimensionType.PIXEL) ? PaddingType.LENGTH : PaddingType.PERCENTAGE;

        switch (direction) {
            case "top" -> {
                properties.put("padding-top", value);
                properties.put("padding-top-type", type);
                properties.put("padding-top-unit", unit);
            }
            case "right" -> {
                properties.put("padding-right", value);
                properties.put("padding-right-type", type);
                properties.put("padding-right-unit", unit);
            }
            case "bottom" -> {
                properties.put("padding-bottom", value);
                properties.put("padding-bottom-type", type);
                properties.put("padding-bottom-unit", unit);
            }
            case "left" -> {
                properties.put("padding-left", value);
                properties.put("padding-left-type", type);
                properties.put("padding-left-unit", unit);
            }
        }
    }

    private void parsePadding(String text, String direction) {
        if (direction != null) {
            parseIndividualPadding(text, direction);
        } else {
            String[] items = text.split("\\s");
            switch (items.length) {
                case 1 -> {
                    parseIndividualPadding(text, "top");
                    parseIndividualPadding(text, "bottom");
                    parseIndividualPadding(text, "left");
                    parseIndividualPadding(text, "right");
                }
                case 2 -> {
                    parseIndividualPadding(items[0], "top");
                    parseIndividualPadding(items[0], "bottom");
                    parseIndividualPadding(items[1], "left");
                    parseIndividualPadding(items[1], "right");
                }
                case 3 -> {
                    parseIndividualPadding(items[0], "top");
                    parseIndividualPadding(items[2], "bottom");
                    parseIndividualPadding(items[1], "left");
                    parseIndividualPadding(items[1], "right");
                }
                case 4 -> {
                    parseIndividualPadding(items[0], "top");
                    parseIndividualPadding(items[2], "bottom");
                    parseIndividualPadding(items[3], "left");
                    parseIndividualPadding(items[1], "right");
                }
            }
        }
    }

    private void parsePosition(String text) {
        try {
            properties.put("position", PositionType.valueOf(text.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            System.err.printf("Invalid position %s, defaulting to relative.\n", text);
            properties.put("position", PositionType.RELATIVE);
        }
    }

    private void parseBorderSpacing(String text) {
        if (text == null || text.isBlank()) {
            return;
        }

        String[] items = text.split("\\s");
        Dimension dimension = parseSingleDimension(items[0]);
        if (dimension.value != null) {
            properties.put("border-spacing", dimension.value.intValue());
        }

        if (items.length > 1) {
            System.err.printf("Ignoring multiple border spacing values in \"%s\".\n", text);
        }
    }

    private void parseTextAlign(String text) {
        TextAlign textAlignCandidate = StringUtils.toEnum(TextAlign.class, text.toUpperCase());
        if (textAlignCandidate != null) {
            properties.put("text-align", textAlignCandidate);
        } else if (CSSConstants.stringToNonStandardTextAlign.containsKey(text.toLowerCase())) {
            properties.put("text-align", CSSConstants.stringToNonStandardTextAlign.get(text.toLowerCase()));
        }
    }

    private void parseBackground(String text) {
        text = text.trim();
        CSSColor color = CSSColor.getColor(text);
        if (color != null) {
            properties.put("background-color", color);
        } else {
            System.err.printf("Unsupported background type \"%s\".\n", text);
        }
    }

    /**
     * Convert the string properties and values to actual properties on this class
     */
    public void setComputedValues() {
        for (Entry<String, String> e : propertyStrings.entrySet()) {
            String property = e.getKey();
            String value = e.getValue().trim();
            if (handleValueKeyword(property, value)) {
                continue;
            }
            switch (property) {
            case "background":
            case "background-color":    parseBackground(value); break;
            case "border":
            case "border-color":
            case "border-width":        parseBorder(value, "all"); break;
            case "border-top":
            case "border-top-color":
            case "border-top-width":    parseBorder(value, "top"); break;
            case "border-bottom":
            case "border-bottom-color":
            case "border-bottom-width": parseBorder(value, "bottom"); break;
            case "border-left":
            case "border-left-color":
            case "border-left-width":   parseBorder(value, "left"); break;
            case "border-right":
            case "border-right-color":
            case "border-right-width":  parseBorder(value, "right"); break;
            case "box-sizing":          properties.put("box-sizing", parseBoxSizingType(value)); break;
            case "border-spacing":      parseBorderSpacing(value); break;
            case "color":               properties.put("color", new CSSColor(value)); break;
            case "display":             parseDisplayType(value); break;
            case "font-family":         properties.put("font-family", FontLoader.getValidFont(value.split(","))); break;
            case "font-size":           parseFontSizeValue(value.toLowerCase()); break;
            case "font-style":          fontStyleType fontStyleTypeCandidate = StringUtils.toEnum(fontStyleType.class, value.toUpperCase());
                                        if (fontStyleTypeCandidate != null) {
                                            properties.put("font-style", fontStyleTypeCandidate);
                                        }
                                        break;
            case "font-weight":         fontWeightType fontWeightTypeCandidate = StringUtils.toEnum(fontWeightType.class, value.toUpperCase());
                                        if (fontWeightTypeCandidate != null) {
                                            properties.put("font-weight", fontWeightTypeCandidate);
                                        }
                                        break;
            case "height":              Dimension heightDimension = parseSingleDimension(value);
                                        properties.put("height", heightDimension.value);
                                        properties.put("height-type", heightDimension.type); break;
            case "margin":              parseMargin(value, null); break;
            case "margin-top":          parseMargin(value, "top");  break;
            case "margin-right":        parseMargin(value, "right");  break;
            case "margin-bottom":       parseMargin(value, "bottom");  break;
            case "margin-left":         parseMargin(value, "left");  break;
            case "max-width":           Dimension maxWidthDimension = parseSingleDimension(value);
                                        properties.put("max-width", maxWidthDimension.value);
                                        properties.put("max-width-type", maxWidthDimension.type); break;
            case "max-height":          Dimension maxHeightDimension = parseSingleDimension(value);
                                        properties.put("max-height", maxHeightDimension.value);
                                        properties.put("max-height-type", maxHeightDimension.type); break;
            case "padding":             parsePadding(value, null); break;
            case "padding-top":         parsePadding(value, "top"); break;
            case "padding-right":       parsePadding(value, "right"); break;
            case "padding-bottom":      parsePadding(value, "bottom"); break;
            case "padding-left":        parsePadding(value, "left"); break;
            case "position":            parsePosition(value); break;
            case "text-align":          parseTextAlign(value); break;
            case "width":               Dimension widthDimension = parseSingleDimension(value);
                                        properties.put("width", widthDimension.value);
                                        properties.put("width-type", widthDimension.type); break;
            }
        }
    }

    /**
     * Some CSS values are keywords that require special handling regardless of the property.
     * @param value     The value to handle, if a keyword.
     * @return      True if a keyword was handled.
     */
    private boolean handleValueKeyword(String property, String value) {
        switch (value.toLowerCase()) {
            case INHERIT -> {
                List<String> fieldNames = propertyNameToSetFields.get(property);
                if (fieldNames != null) {
                    propertiesToInherit.addAll(fieldNames);
                } else {
                    System.err.printf("Failed to handle inherit for property %s.\n", property);
                }
                return true;
            }
            case "initial", "revert", "revert-layer", "unset" -> {
                System.err.printf("Unsupported CSS property keyword %s, ignoring.\n", value);
                return true;
            }
        }
        return false;
    }
    
    public void apply(String property, String value, CSSSpecificity specificity) {
        boolean existingImportance = propertyImportant.containsKey(property) && propertyImportant.get(property);
        boolean important = false;
        if (value.endsWith(IMPORTANT)) {
            important = true;
            value = value.substring(0, value.length() - IMPORTANT.length()).trim();
        }

        boolean specificityOverride = !propertySpecificity.containsKey(property) ||
                specificity.hasEqualOrGreaterSpecificityThan(propertySpecificity.get(property));

        boolean applyRule = (existingImportance && important) || (!existingImportance && (specificityOverride || important));

        if (applyRule) {
            propertySpecificity.put(property, specificity);
            propertyImportant.put(property, important);
            propertyStrings.put(property, value);
            propertiesToInherit.remove(property);
        }
    }

    /**
     * Attempts to apply a computed CSS property inherited from a parent's styling. This inheritance may be the default
     * CSS behavior, such as with font colors, or the result of using the `inherit` keyword on a CSS property. Any CSS
     * declaration that applies directly to a node will override an inherited value on that same property.
     * @param property      The property name.
     * @param computedValue     The computed value object.
     * @param override      When true, property will always override the existing value.
     */
    public void applyInheritedComputed(String property, Object computedValue, boolean override) {
        boolean propertyAlreadySet = propertyStrings.containsKey(property) && !propertyStrings.get(property).equals(INHERIT);
        boolean propertyWasInherited = inheritedProperties.contains(property);
        if (!propertyAlreadySet || propertyWasInherited || override) {
            properties.put(property, computedValue);
            inheritedProperties.add(property);
        }
    }

    public void apply(Map<String, String> declarations, CSSSpecificity specificity) {
        for (Entry<String, String> e : declarations.entrySet()) {
            apply(e.getKey(), e.getValue(), specificity);
        }
    }

    public CSSStyle deepCopy() {
        CSSStyle style = new CSSStyle();

        style.propertyStrings.putAll(propertyStrings);
        style.properties.putAll(properties);

        // TODO: copy over the properties maps
        style.parentStyle = parentStyle;
        style.backgroundColor = backgroundColor;
        style.borderStyleTop = borderStyleTop;
        style.borderStyleLeft = borderStyleLeft;
        style.borderStyleRight = borderStyleRight;
        style.borderStyleBottom = borderStyleBottom;
        style.borderWidthTop = borderWidthTop;
        style.borderWidthBottom = borderWidthBottom;
        style.borderWidthLeft = borderWidthLeft;
        style.borderWidthRight = borderWidthRight;
        style.borderColorTop = borderColorTop;
        style.borderColorBottom = borderColorBottom;
        style.borderColorLeft = borderColorLeft;
        style.borderColorRight = borderColorRight;
        style.boxSizing = boxSizing;
        style.borderSpacing = borderSpacing;
        style.color = color;
        style.display = display;
        style.innerDisplay = innerDisplay;
        style.outerDisplay = outerDisplay;
        style.auxiliaryDisplay = auxiliaryDisplay;
        style.position = position;
        style.fontFamily = fontFamily;
        style.fontSize = fontSize;
        style.fontStyle = fontStyle;
        style.fontWeight = fontWeight;
        style.heightType = heightType;
        style.height = height;
        style.marginTop = marginTop;
        style.marginTopType = marginTopType;
        style.marginTopUnit = marginTopUnit;
        style.marginRight = marginRight;
        style.marginRightType = marginRightType;
        style.marginRightUnit = marginRightUnit;
        style.marginBottom = marginBottom;
        style.marginBottomType = marginBottomType;
        style.marginBottomUnit = marginBottomUnit;
        style.marginLeft = marginLeft;
        style.marginLeftType = marginLeftType;
        style.marginLeftUnit = marginLeftUnit;
        style.paddingTop = paddingTop;
        style.paddingTopType = paddingTopType;
        style.paddingTopUnit = paddingTopUnit;
        style.paddingRight = paddingRight;
        style.paddingRightType = paddingRightType;
        style.paddingRightUnit = paddingRightUnit;
        style.paddingBottom = paddingBottom;
        style.paddingBottomType = paddingBottomType;
        style.paddingBottomUnit = paddingBottomUnit;
        style.paddingLeft = paddingLeft;
        style.paddingLeftType = paddingLeftType;
        style.paddingLeftUnit = paddingLeftUnit;
        style.textAlign = textAlign;
        style.widthType = widthType;
        style.width = width;
        style.maxWidth = maxWidth;
        style.maxWidthType = maxWidthType;
        style.maxHeight = maxHeight;
        style.maxHeightType = maxHeightType;
        style.wordWrap = wordWrap;

        return style;
    }

    public CSSStyle inheritedOnlyDeepCopy() {
        CSSStyle style = new CSSStyle();

        style.color = color;
        style.fontFamily = fontFamily;
        style.fontSize = fontSize;
        style.fontStyle = fontStyle;
        style.fontWeight = fontWeight;
        style.textAlign = textAlign;

        return style;
    }

    public void setClassProperties() {
        for (String property : properties.keySet()) {
            String fieldName = StringUtils.hyphenatedToCamelCase(property);
            try {
                Field field = getClass().getDeclaredField(fieldName);
                field.set(this, properties.get(property));
            } catch (Exception e) {
                System.err.printf("Failed to set field %s to %s.\n", fieldName, properties.get(property));
                e.printStackTrace();
            }
        }
    }

    public String computedPropertiesToString() {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> sortedProperties = new ArrayList<>(properties.keySet()).stream().sorted().toList();

        stringBuilder.append(String.format("Computed properties map (%d):\n", sortedProperties.size()));
        for (String property : sortedProperties) {
            stringBuilder.append(String.format("  %s: %s\n", property, properties.get(property)));
        }

        return stringBuilder.toString();
    }

}
