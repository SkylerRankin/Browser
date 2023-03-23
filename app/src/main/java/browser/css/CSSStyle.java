package browser.css;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import browser.constants.CSSConstants;
import browser.model.CSSColor;
import browser.model.CSSRulePrecedent;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class CSSStyle {

    private final Set<String> setProperties = new HashSet<>();
    private final Map<String, String> properties = new HashMap<>();
    private final Map<String, CSSRulePrecedent> precedents = new HashMap<>();

    private final Pattern lengthPattern = Pattern.compile("^([0-9]+)([a-zA-Z]{1,5})$");

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

    public enum PositionType { STATIC, RELATIVE, ABSOLUTE, FIXED, STICKY }

    public enum BoxSizingType { CONTENT_BOX, BORDER_BOX }

    public static enum fontStyleType {NORMAL, ITALIC, ITALICS}

    public static enum fontWeightType {NORMAL, BOLD, OTHER}

    public static enum textAlignType {LEFT, CENTER, RIGHT}

//    public static enum textDecorationType {NONE, OVERLINE, LINETHROUGH, UNDERLINE}

    public static enum wordWrapType {NORMAL, BREAKWORD}

    public static enum MarginType {AUTO, LENGTH, PERCENTAGE}
    
    public CSSColor backgroundColor = new CSSColor("White");
    
    public CSSColor borderColorTop = new CSSColor("Black");
    public CSSColor borderColorBottom = new CSSColor("Black");
    public CSSColor borderColorLeft = new CSSColor("Black");
    public CSSColor borderColorRight = new CSSColor("Black");

    public int borderWidthTop = 0;
    public int borderWidthRight = 0;
    public int borderWidthBottom = 0;
    public int borderWidthLeft = 0;

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
    public int fontSize = 12;
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
    
    public int padding = 0;
    public int paddingTop = 0;
    public int paddingRight = 0;
    public int paddingBottom = 0;
    public int paddingLeft = 0;
    
    public textAlignType textAlign = textAlignType.LEFT;
    
    public DimensionType widthType = DimensionType.PIXEL;
    public Float width = null;
    
    public Float maxWidth = null;
    public DimensionType maxWidthType = DimensionType.PIXEL;
    public Float maxHeight = null;
    public DimensionType maxHeightType = DimensionType.PIXEL;
    
//    public textDecorationType textDecoration = textDecorationType.NONE;
    
    public wordWrapType wordWrap = wordWrapType.NORMAL;
    
    public static String[] inheritedProperties = {"color", "font-family", "font-size", "font-style", "font-weight", "text-align", "background-color"};
    
    public static boolean propagateAttribute(String attribute) {
        for (String s : inheritedProperties) {
            if (s.equals(attribute)) return true;
        }
        return false;
    }
    
    /**
     * Check if this property has already been set by some other CSS rule.
     * @param property
     * @return
     */
    public boolean hasPropertySet(String property) {
//        return properties.containsKey(property);
        return setProperties.contains(property);
    }
    
    public void setProperty(String property, String value) {
        properties.put(property, value);
        setProperties.add(property);
    }
    
    public Map<String, String> getAllProperties() {
        return properties;
    }
    
    public void resetSetProperties() {
        setProperties.clear();
    }
    
    private int parseDimension(String value) {
        value = value.trim();

        if (value.endsWith("em")) {
            return (int) (16 * Double.parseDouble(value.substring(0, value.length() - 2)));
        } else if (value.equals("auto")) {
            return 0;
        }

        String[] values = value.split(" ");
        if (values.length > 1) {
            System.out.printf("CSSStyle: ignoring multiple dimension values, %s\n", value);
            value = values[0];
        }

        int offset = 0;
        if (value.endsWith("px")) offset = 2;
        if (value.endsWith("%")) offset = 1;
        return Integer.parseInt(value.substring(0, value.length() - offset));
    }
    
    private void parseFontSizeValue(String value) {
        if (value.matches("\\d+")) {
            fontSize = Integer.parseInt(value);
        } else if (value.matches("(\\d+)px")) {
            fontSize = Integer.parseInt(value.substring(0, value.length() - 2));
        } else if (value.endsWith("rem")) {
            String remsString = value.substring(0, value.indexOf("rem"));
            if (remsString.matches("\\d+(\\.\\d+?)?")) {
                double rems = Double.parseDouble(remsString);
                fontSize = (int) (rems * 16.0);
            }
        } else if (value.endsWith("%") && value.substring(0, value.length()-1).matches("\\d+")) {
            double percent = Double.parseDouble(value.substring(0, value.length()-1));
            fontSize = (int) (16.0 * 100.0 / percent);
        } else {
            System.out.printf("CSSStyle.parseFontSizeValue: invalid font property: %s\n", value);
        }

    }
    
    private int parseBorderWidth(String value) {
        String[] values = value.split("\\s");
        return parseDimension(values[0]);
    }
    
    private CSSColor parseBorderColor(String value) {
        String[] values = value.split("\\s");
        return new CSSColor(values[2]);
    }

    /**
     * The border property can contain up to 3 values, in any order: line width, line style, and line color.
     * @param value The CSS value string to parse
     */
    private void parseBorder(String value) {
        value = value.trim();
        if (value.endsWith(";")) {
            value = value.substring(0, value.length() - 1);
        }
        String[] items = value.split("\s");
        for (int i = 0; i < items.length; i++) {
            if (CSSConstants.borderLineStyles.contains(items[i])) {
                System.out.printf("Border styles are not supported. Ignored '%s' within '%s'.\n", items[i], value);
            } else {
                // Check if the value is a length
                if (items[i].equals("0")) {
                    // Zero is a special case that does not require a unit.
                    borderWidthTop = 0;
                    borderWidthBottom = 0;
                    borderWidthLeft = 0;
                    borderWidthRight = 0;
                } else {
                    Matcher lengthMatcher = lengthPattern.matcher(items[i]);
                    if (lengthMatcher.find()) {
                        int length = Integer.parseInt(lengthMatcher.group(1));
                        String unitString = lengthMatcher.group(2);
                        LengthUnit unit = parseLengthUnit(unitString);
                        if (unit != null) {
                            if (!unit.equals(LengthUnit.PX)) {
                                System.out.printf("Unsupported border width unit %s, defaulting to 1px.\n", unitString);
                                length = 1;
                            }
                            borderWidthTop = length;
                            borderWidthBottom = length;
                            borderWidthLeft = length;
                            borderWidthRight = length;
                            continue;
                        }
                    }

                    // Check if the value is a color
                    CSSColor color = CSSColor.getColor(items[i]);
                    if (color != null) {
                        borderColorTop = color;
                        borderColorBottom = color;
                        borderColorLeft = color;
                        borderColorRight = color;
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
            if (CSSConstants.getDisplayTypeOverride(singleType) != null) {
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
        MarginType type = MarginType.LENGTH;
        LengthUnit unit = LengthUnit.PX;

        if (text.equalsIgnoreCase("auto")) {
            type = MarginType.AUTO;
        } else if (text.endsWith("%") && text.length() > 1) {
            String percentageText = text.substring(text.length() - 1);
            if (percentageText.matches("[0-9]+")) {
                type = MarginType.PERCENTAGE;
                value = Integer.parseInt(percentageText);
            }
        } else if (text.matches("[0-9]+")) {
            value = Integer.parseInt(text);
        } else {
            Matcher lengthMatcher = lengthPattern.matcher(text);
            if (lengthMatcher.find()) {
                int length = Integer.parseInt(lengthMatcher.group(1));
                String unitString = lengthMatcher.group(2);
                LengthUnit unitCandidate = parseLengthUnit(unitString);
                if (unitCandidate != null) {
                    // TODO resolve non-pixel units into pixels.
                    unit = unitCandidate;
                    value = length;
                }
            }
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
    
    /**
     * Convert the string properties and values to actual properties on this class
     */
    public void finalizeCSS() {
        for (Entry<String, String> e : properties.entrySet()) {
            String value = e.getValue().trim();
            switch (e.getKey()) {
            case "background-color":    backgroundColor = new CSSColor(value); break;
            case "border":              parseBorder(value); break;
            case "border-color":        borderColorTop = new CSSColor(value);
                                        borderColorBottom = new CSSColor(value);
                                        borderColorLeft = new CSSColor(value);
                                        borderColorRight = new CSSColor(value);break;
            case "border-width":        borderWidthTop = Integer.parseInt(value);
                                        borderWidthBottom = Integer.parseInt(value);
                                        borderWidthLeft = Integer.parseInt(value);
                                        borderWidthRight = Integer.parseInt(value); break;
            case "border-top":          borderColorTop = parseBorderColor(value);
                                        borderWidthTop = parseBorderWidth(value); break;
            case "border-bottom":       borderColorBottom = parseBorderColor(value);
                                        borderWidthBottom = parseBorderWidth(value); break;
            case "border-left":         borderColorLeft = parseBorderColor(value);
                                        borderWidthLeft = parseBorderWidth(value); break;
            case "border-right":        borderColorRight = parseBorderColor(value);
                                        borderWidthRight = parseBorderWidth(value); break;
            case "border-top-color":    borderColorTop = new CSSColor(value); break;
            case "border-bottom-color": borderColorBottom = new CSSColor(value); break;
            case "border-left-color":   borderColorLeft = new CSSColor(value); break;
            case "border-right-color":  borderColorRight = new CSSColor(value); break;
            case "border-top-width":    borderWidthTop = parseDimension(value); break;
            case "border-bottom-width": borderWidthBottom = parseDimension(value); break;
            case "border-left-width":   borderWidthLeft = parseDimension(value); break;
            case "border-right-width":  borderWidthRight = parseDimension(value); break;
            case "box-sizing":          boxSizing = parseBoxSizingType(value); break;
            case "border-spacing":      borderSpacing = parseDimension(value); break;
            case "color":               color = new CSSColor(value); break;
            case "display":             parseDisplayType(value); break;
            case "font-family":         fontFamily = FontLoader.getValidFont(value.split(",")); break;
            case "font-size":           parseFontSizeValue(value.toLowerCase()); break;
            case "font-style":          fontStyle = fontStyleType.valueOf(value.toUpperCase()); break;
            case "font-weight":         fontWeight = fontWeightType.valueOf(value.toUpperCase()); break;
            case "height":              height = (float) parseDimension(value);
                                        heightType = value.contains("%") ?
                                                DimensionType.PERCENTAGE :
                                                DimensionType.PIXEL; break;
            case "margin":              parseMargin(value, null); break;
            case "margin-top":          parseMargin(value, "top");  break;
            case "margin-right":        parseMargin(value, "right");  break;
            case "margin-bottom":       parseMargin(value, "bottom");  break;
            case "margin-left":         parseMargin(value, "left");  break;
            case "max-width":           maxWidth = (float) parseDimension(value);
                                        maxWidthType = value.contains("%") ?
                                            DimensionType.PERCENTAGE :
                                            DimensionType.PIXEL; break;
            case "max-height":          maxHeight = (float) parseDimension(value);
                                        maxHeightType = value.contains("%") ?
                                            DimensionType.PERCENTAGE :
                                            DimensionType.PIXEL; break;
            case "padding":             paddingTop = parseDimension(value);
                                        paddingRight = parseDimension(value);
                                        paddingBottom = parseDimension(value);
                                        paddingLeft = parseDimension(value); break;
            case "padding-top":         paddingTop = parseDimension(value);  break;
            case "padding-right":       paddingRight = parseDimension(value);  break;
            case "padding-bottom":      paddingBottom = parseDimension(value);  break;
            case "padding-left":        paddingLeft = parseDimension(value);  break;
            case "position":            position = PositionType.valueOf(value.toLowerCase()); break;
            case "text-align":          textAlign = textAlignType.valueOf(value.toUpperCase()); break;
            case "width":               width = (float) parseDimension(value);
                                        widthType = value.contains("%") ?
                                                DimensionType.PERCENTAGE :
                                                DimensionType.PIXEL; break;
            }
        }
    }
    
    public CSSRulePrecedent getPropertyPrecedent(String property) {
        return precedents.get(property);
    }
    
    public void apply(String property, String value, CSSRulePrecedent newPrecedent) {
        CSSRulePrecedent oldPrecedent = precedents.get(property);
        if (oldPrecedent == null || newPrecedent.hasPrecedentOver(oldPrecedent)) {
            properties.put(property, value);
            // TODO: dont need setProperties anymore since we have precedents
            setProperties.add(property);
            precedents.put(property, newPrecedent);
        }
    }
    
    /**
     * Apply some CSS rules if it has precedence over what is currently there
     * @param css    Map<String, String> of the CSS attribute and value
     */
    public void apply(Map<String, String> css, CSSRulePrecedent newPrecedent) {
        for (Entry<String, String> e : css.entrySet()) {
            apply(e.getKey(), e.getValue(), newPrecedent);
        }
    }
    
    public void print(String padding) {
        for (Entry<String, String> e : properties.entrySet()) {
            CSSRulePrecedent p = precedents.get(e.getKey());
            System.out.printf("%s%s %s: %s\n", padding, p.toString(), e.getKey(), e.getValue());
        }
    }

    public CSSStyle deepCopy() {
        CSSStyle style = new CSSStyle();

        // TODO: copy over the properties maps

        style.backgroundColor = backgroundColor;
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
        style.padding = padding;
        style.paddingTop = paddingTop;
        style.paddingRight = paddingRight;
        style.paddingBottom = paddingBottom;
        style.paddingLeft = paddingLeft;
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
    
}
