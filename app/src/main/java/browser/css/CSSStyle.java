package browser.css;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import browser.constants.CSSConstants;
import browser.model.CSSColor;
import browser.model.Dimension;
import browser.parser.StringUtils;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import static browser.constants.CSSConstants.*;

@EqualsAndHashCode
@ToString
public class CSSStyle {

    private final Map<String, String> properties = new HashMap<>();
    private final Map<String, CSSSpecificity> propertySpecificity = new HashMap<>();
    private final Map<String, Boolean> propertyImportant = new HashMap<>();
    private final Set<String> inheritedProperties = new HashSet<>();

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

    public static enum fontStyleType {NORMAL, ITALIC, ITALICS}

    public static enum fontWeightType {NORMAL, BOLD, OTHER}

    public enum TextAlign {
        LEFT, CENTER, RIGHT,
        WEBKIT_LEFT, WEBKIT_CENTER, WEBKIT_RIGHT
    }

//    public static enum textDecorationType {NONE, OVERLINE, LINETHROUGH, UNDERLINE}

    public static enum wordWrapType {NORMAL, BREAKWORD}

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
        properties.put(property, value);
    }
    
    public Map<String, String> getAllProperties() {
        return properties;
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
            case PX -> fontSize = length;
            case PT -> fontSize = length * (1 + 1.0f / 3.0f);
            case EM -> fontSize = (parentStyle == null ? fontSize : parentStyle.fontSize) * length;
            case REM -> fontSize = BASE_FONT_SIZE * length;
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
                    case "top" -> borderStyleTop = borderStyle;
                    case "right" -> borderStyleRight = borderStyle;
                    case "bottom" -> borderStyleBottom = borderStyle;
                    case "left" -> borderStyleLeft = borderStyle;
                    default -> {
                        borderStyleTop = borderStyle;
                        borderStyleRight = borderStyle;
                        borderStyleBottom = borderStyle;
                        borderStyleLeft = borderStyle;
                    }
                }
            } else {
                // Check if the value is a length
                if (items[i].equals("0")) {
                    // Zero is a special case that does not require a unit.
                    switch (direction) {
                        case "top" -> borderWidthTop = 0;
                        case "right" -> borderWidthRight = 0;
                        case "bottom" -> borderWidthBottom = 0;
                        case "left" -> borderWidthLeft = 0;
                        default -> {
                            borderWidthTop = 0;
                            borderWidthRight = 0;
                            borderWidthBottom = 0;
                            borderWidthLeft = 0;
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
                                case "top" -> borderWidthTop = length;
                                case "right" -> borderWidthRight = length;
                                case "bottom" -> borderWidthBottom = length;
                                case "left" -> borderWidthLeft = length;
                                default -> {
                                    borderWidthTop = length;
                                    borderWidthRight = length;
                                    borderWidthBottom = length;
                                    borderWidthLeft = length;
                                }
                            }
                            continue;
                        }
                    }

                    // Check if the value is a color
                    CSSColor color = CSSColor.getColor(items[i]);
                    if (color != null) {
                        switch (direction) {
                            case "top" -> borderColorTop = color;
                            case "right" -> borderColorRight = color;
                            case "bottom" -> borderColorBottom = color;
                            case "left" -> borderColorLeft = color;
                            default -> {
                                borderColorTop = color;
                                borderColorRight = color;
                                borderColorBottom = color;
                                borderColorLeft = color;
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
                outerDisplay = DisplayType.NONE;
                innerDisplay = DisplayType.NONE;
                auxiliaryDisplay = DisplayType.NONE;
            } else if (CSSConstants.getDisplayTypeOverride(singleType) != null) {
                // Some display types have mappings to inner/outer display types that are not evident from their names.
                // For example, inline-block maps to inline flow-root.
                List<DisplayType> types = CSSConstants.getDisplayTypeOverride(singleType);
                outerDisplay = types.get(0);
                innerDisplay = types.get(1);
                if (types.size() == 3) {
                    auxiliaryDisplay = types.get(2);
                }
            } else if (CSSConstants.outerDisplayTypes.contains(singleType)) {
                outerDisplay = singleType;
                innerDisplay = DisplayType.FLOW;
            } else {
                outerDisplay = DisplayType.BLOCK;
                innerDisplay = singleType;
            }
        } else if (text.contains("-")) {
            outerDisplay = CSSConstants.getDisplayType(text.substring(0, text.indexOf("-")));
            innerDisplay = CSSConstants.getDisplayType(text.substring(text.indexOf("-") + 1));
        } else if (text.contains(" ")) {
            outerDisplay = CSSConstants.getDisplayType(text.substring(0, text.indexOf(" ")));
            innerDisplay = CSSConstants.getDisplayType(text.substring(text.indexOf(" ") + 1));
        } else {
            outerDisplay = DisplayType.BLOCK;
            innerDisplay = DisplayType.FLOW;
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
                marginTop = value;
                marginTopType = type;
                marginTopUnit = unit;
            }
            case "right" -> {
                marginRight = value;
                marginRightType = type;
                marginRightUnit = unit;
            }
            case "bottom" -> {
                marginBottom = value;
                marginBottomType = type;
                marginBottomUnit = unit;
            }
            case "left" -> {
                marginLeft = value;
                marginLeftType = type;
                marginLeftUnit = unit;
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
                paddingTop = value;
                paddingTopType = type;
                paddingTopUnit = unit;
            }
            case "right" -> {
                paddingRight = value;
                paddingRightType = type;
                paddingRightUnit = unit;
            }
            case "bottom" -> {
                paddingBottom = value;
                paddingBottomType = type;
                paddingBottomUnit = unit;
            }
            case "left" -> {
                paddingLeft = value;
                paddingLeftType = type;
                paddingLeftUnit = unit;
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
            position = PositionType.valueOf(text.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.printf("Invalid position %s, defaulting to relative.\n", text);
            position = PositionType.RELATIVE;
        }
    }

    private void parseBorderSpacing(String text) {
        if (text == null || text.isBlank()) {
            return;
        }

        String[] items = text.split("\\s");
        Dimension dimension = parseSingleDimension(items[0]);
        if (dimension.value != null) {
            borderSpacing = dimension.value.intValue();
        }

        if (items.length > 1) {
            System.err.printf("Ignoring multiple border spacing values in \"%s\".\n", text);
        }
    }

    private void parseTextAlign(String text) {
        TextAlign textAlignCandidate = StringUtils.toEnum(TextAlign.class, text.toUpperCase());
        if (textAlignCandidate != null) {
            textAlign = textAlignCandidate;
        } else if (CSSConstants.stringToNonStandardTextAlign.containsKey(text.toLowerCase())) {
            textAlign = CSSConstants.stringToNonStandardTextAlign.get(text.toLowerCase());
        }
    }

    private void parseBackground(String text) {
        text = text.trim();
        CSSColor color = CSSColor.getColor(text);
        if (color != null) {
            backgroundColor = color;
        } else {
            System.err.printf("Unsupported background type \"%s\".\n", text);
        }
    }

    /**
     * Convert the string properties and values to actual properties on this class
     */
    public void finalizeCSS() {
        for (Entry<String, String> e : properties.entrySet()) {
            String value = e.getValue().trim();
            switch (e.getKey()) {
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
            case "box-sizing":          boxSizing = parseBoxSizingType(value); break;
            case "border-spacing":      parseBorderSpacing(value); break;
            case "color":               color = new CSSColor(value); break;
            case "display":             parseDisplayType(value); break;
            case "font-family":         fontFamily = FontLoader.getValidFont(value.split(",")); break;
            case "font-size":           parseFontSizeValue(value.toLowerCase()); break;
            case "font-style":          fontStyleType fontStyleTypeCandidate = StringUtils.toEnum(fontStyleType.class, value.toUpperCase());
                                        if (fontStyleTypeCandidate != null) {
                                            fontStyle = fontStyleTypeCandidate;
                                        }
                                        break;
            case "font-weight":         fontWeightType fontWeightTypeCandidate = StringUtils.toEnum(fontWeightType.class, value.toUpperCase());
                                        if (fontWeightTypeCandidate != null) {
                                            fontWeight = fontWeightTypeCandidate;
                                        }
                                        break;
            case "height":              Dimension heightDimension = parseSingleDimension(value);
                                        height = heightDimension.value;
                                        heightType = heightDimension.type; break;
            case "margin":              parseMargin(value, null); break;
            case "margin-top":          parseMargin(value, "top");  break;
            case "margin-right":        parseMargin(value, "right");  break;
            case "margin-bottom":       parseMargin(value, "bottom");  break;
            case "margin-left":         parseMargin(value, "left");  break;
            case "max-width":           Dimension maxWidthDimension = parseSingleDimension(value);
                                        maxWidth = maxWidthDimension.value;
                                        maxWidthType = maxWidthDimension.type; break;
            case "max-height":          Dimension maxHeightDimension = parseSingleDimension(value);
                                        maxHeight = maxHeightDimension.value;
                                        maxHeightType = maxHeightDimension.type; break;
            case "padding":             parsePadding(value, null); break;
            case "padding-top":         parsePadding(value, "top"); break;
            case "padding-right":       parsePadding(value, "right"); break;
            case "padding-bottom":      parsePadding(value, "bottom"); break;
            case "padding-left":        parsePadding(value, "left"); break;
            case "position":            parsePosition(value); break;
            case "text-align":          parseTextAlign(value); break;
            case "width":               Dimension widthDimension = parseSingleDimension(value);
                                        width = widthDimension.value;
                                        widthType = widthDimension.type; break;
            }
        }
    }

    public CSSSpecificity getPropertySpecificity(String property) {
        return propertySpecificity.get(property);
    }
    
    public void apply(String property, String value, CSSSpecificity specificity) {
        boolean existingImportance = propertyImportant.containsKey(property) && propertyImportant.get(property);
        boolean important = false;
        if (property.endsWith(IMPORTANT)) {
            important = true;
            property = property.substring(0, property.length() - IMPORTANT.length()).trim();
        }

        boolean specificityOverride = !propertySpecificity.containsKey(property) ||
                specificity.hasEqualOrGreaterSpecificityThan(propertySpecificity.get(property)) ||
                inheritedProperties.contains(property);
        boolean inheritedOverride = inheritedProperties.contains(property);

        boolean applyRule = (existingImportance && important) || (!existingImportance && (specificityOverride || inheritedOverride || important));

        if (applyRule) {
            propertySpecificity.put(property, specificity);
            propertyImportant.put(property, important);
            properties.put(property, value);
            inheritedProperties.remove(property);
        }
    }

    public void applyInherited(String property, String value, CSSSpecificity specificity) {
        if (!properties.containsKey(property) || inheritedProperties.contains(property)) {
            properties.put(property, value);
            inheritedProperties.add(property);
            propertySpecificity.put(property, specificity);
        }
    }

    public void apply(Map<String, String> declarations, CSSSpecificity specificity) {
        for (Entry<String, String> e : declarations.entrySet()) {
            apply(e.getKey(), e.getValue(), specificity);
        }
    }

    public CSSStyle deepCopy() {
        CSSStyle style = new CSSStyle();

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

}
