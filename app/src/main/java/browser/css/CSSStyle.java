package browser.css;

import java.util.*;
import java.util.Map.Entry;

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

    public static enum dimensionType {PIXEL, PERCENTAGE}

    // Reference for all display type combinations: https://developer.mozilla.org/en-US/docs/Web/CSS/display.
    public enum DisplayType {
        // display-outside values
        BLOCK, INLINE,
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

    public static enum fontStyleType {NORMAL, ITALIC, ITALICS}

    public static enum fontWeightType {NORMAL, BOLD, OTHER}

    public static enum textAlignType {LEFT, CENTER, RIGHT}

//    public static enum textDecorationType {NONE, OVERLINE, LINETHROUGH, UNDERLINE}

    public static enum wordWrapType {NORMAL, BREAKWORD}

    public static enum marginSizeType {AUTO, PIXEL}
    
    public CSSColor backgroundColor = new CSSColor("White");
    
    public CSSColor borderColorTop = new CSSColor("Black");
    public CSSColor borderColorBottom = new CSSColor("Black");
    public CSSColor borderColorLeft = new CSSColor("Black");
    public CSSColor borderColorRight = new CSSColor("Black");

    public int borderWidthTop = 0;
    public int borderWidthRight = 0;
    public int borderWidthBottom = 0;
    public int borderWidthLeft = 0;
    
    public CSSColor color = new CSSColor("Black");
    
    public DisplayType display = DisplayType.BLOCK;
    public DisplayType innerDisplay;
    public DisplayType outerDisplay = DisplayType.BLOCK;
    // Some display types, such as list-item, set a third value in addition to the inner and outer display types. This
    // auxiliary display type captures that information.
    public DisplayType auxiliaryDisplay;
    
    public String fontFamily = "Times New Roman";
    public int fontSize = 12;
    public fontStyleType fontStyle = fontStyleType.NORMAL;
    public fontWeightType fontWeight = fontWeightType.NORMAL;
    
    public dimensionType heightType = dimensionType.PIXEL;
    public Float height = null;
    
    public marginSizeType marginType = marginSizeType.PIXEL;
    public int margin = 0;
    public int marginTop = 0;
    public int marginRight = 0;
    public int marginBottom = 0;
    public int marginLeft = 0;
    
    public int padding = 0;
    public int paddingTop = 0;
    public int paddingRight = 0;
    public int paddingBottom = 0;
    public int paddingLeft = 0;
    
    public textAlignType textAlign = textAlignType.LEFT;
    
    public dimensionType widthType = dimensionType.PIXEL;
    public Float width = null;
    
    public Float maxWidth = null;
    
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
    
    private int parseFontSizeValue(String value) {
        if (value.matches("\\d+")) return Integer.parseInt(value);
        if (value.endsWith("rem")) {
            String remsString = value.substring(0, value.indexOf("rem"));
            if (remsString.matches("\\d+(\\.\\d+?)?")) {
                double rems = Double.parseDouble(remsString);
                return (int) (rems * 16.0);
                
            }
        }
        if (value.endsWith("%") && value.substring(0, value.length()-1).matches("\\d+")) {
            double percent = Double.parseDouble(value.substring(0, value.length()-1));
            return (int) (16.0 * 100.0 / percent);
        }
        return 16;
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

        // TODO should the basic display even be used anymore?
        // TODO hwo to set the default inner. not sure if "flow" is the correct choice here.
    }
    
    /**
     * Convert the string properties and values to actual properties on this class
     */
    public void finalizeCSS() {
        for (Entry<String, String> e : properties.entrySet()) {
            String value = e.getValue().trim();
            switch (e.getKey()) {
            case "background-color":     backgroundColor = new CSSColor(value); break;
            case "border":              borderWidthTop = parseBorderWidth(value);
                                        borderWidthBottom = parseBorderWidth(value);
                                        borderWidthLeft = parseBorderWidth(value);
                                        borderWidthRight = parseBorderWidth(value);
                                        borderColorTop = parseBorderColor(value);
                                        borderColorBottom = parseBorderColor(value);
                                        borderColorLeft = parseBorderColor(value);
                                        borderColorRight = parseBorderColor(value); break;
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
            case "color":               color = new CSSColor(value); break;
            case "display":             parseDisplayType(value); break;
            case "font-family":         fontFamily = FontLoader.getValidFont(value.split(",")); break;
            case "font-size":           fontSize = parseFontSizeValue(value.toLowerCase()); break;
            case "font-style":          fontStyle = fontStyleType.valueOf(value.toUpperCase()); break;
            case "font-weight":         fontWeight = fontWeightType.valueOf(value.toUpperCase()); break;
            case "height":              height = (float) parseDimension(value);
                                        heightType = value.contains("%") ?
                                                dimensionType.PERCENTAGE :
                                                dimensionType.PIXEL; break;
            case "margin":              marginType = value.equals("auto") ?
                                                marginSizeType.AUTO :
                                                marginSizeType.PIXEL;
                                        if (marginType.equals(marginSizeType.PIXEL)) {
                                            marginTop = parseDimension(value);
                                            marginRight = parseDimension(value);
                                            marginBottom = parseDimension(value);
                                            marginLeft = parseDimension(value);
                                        } break;
            case "margin-top":          marginTop = parseDimension(value);  break;
            case "margin-right":        marginRight = parseDimension(value);  break;
            case "margin-bottom":       marginBottom = parseDimension(value);  break;
            case "margin-left":         marginLeft = parseDimension(value);  break;
            case "max-width":           maxWidth = (float) parseDimension(value); break;
            case "padding":             paddingTop = parseDimension(value);
                                        paddingRight = parseDimension(value);
                                        paddingBottom = parseDimension(value);
                                        paddingLeft = parseDimension(value); break;
            case "padding-top":         paddingTop = parseDimension(value);  break;
            case "padding-right":       paddingRight = parseDimension(value);  break;
            case "padding-bottom":      paddingBottom = parseDimension(value);  break;
            case "padding-left":        paddingLeft = parseDimension(value);  break;
            case "text-align":          textAlign = textAlignType.valueOf(value.toUpperCase()); break;
            case "width":               width = (float) parseDimension(value);
                                        widthType = value.contains("%") ?
                                                dimensionType.PERCENTAGE :
                                                dimensionType.PIXEL; break;
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
            // TODO dont need setProperties anymore since we have precedents
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
    
}
