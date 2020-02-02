package css;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import model.CSSColor;

public class CSSStyle {
	
	private Set<String> setProperties = new HashSet<String>();
	private Map<String, String> properties = new HashMap<String, String>();
	
	private static enum overridePrecedent {
		DEFAULT_ALL, DEFAULT_ELEMENT, DEFAULT_CLASS, DEFAULT_ID,
		TAG_ALL, TAG_ELEMENT, TAG_CLASS, TAG_ID,
		INLINE};
	
    public static enum dimensionType {PIXEL, PERCENTAGE};
    public static enum displayType {BLOCK, INLINE, NONE};
    public static enum fontStyleType {NORMAL, ITALICS};
    public static enum fontWeightType {NORMAL, BOLD, OTHER};
    public static enum textAlignType {LEFT, CENTER, RIGHT};
//    public static enum textDecorationType {NONE, OVERLINE, LINETHROUGH, UNDERLINE};
    public static enum wordWrapType {NORMAL, BREAKWORD};
    
    public CSSColor backgroundColor = new CSSColor("White");
    
    public CSSColor borderColor = new CSSColor("White");
    public int borderWidth = 1;
    public int borderWidthTop = 1;
    public int borderWidthRight = 1;
    public int borderWidthBottom = 1;
    public int borderWidthLeft = 1;
    
    public CSSColor color = new CSSColor("Black");
    
    public displayType display = displayType.BLOCK;
    
    public String fontFamily = "Arial";
    public int fontSize = 15;
    public fontStyleType fontStyle = fontStyleType.NORMAL;
    public fontWeightType fontWeight = fontWeightType.NORMAL;
    
    public dimensionType heightType = dimensionType.PIXEL;
    public Float height = null;
    
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
    
//    public textDecorationType textDecoration = textDecorationType.NONE;
    
    public wordWrapType wordWrap = wordWrapType.NORMAL;
    
    public static boolean propagateAttribute(String attribute) {
    	return (
    				!attribute.equals("width") &&
    				!attribute.equals("height") &&
    				!attribute.equals("widthType") &&
    				!attribute.equals("heightType") &&
    				!attribute.equals("display")
    			);
    }
    
    /**
     * Check if this property has already been set by some other CSS rule.
     * @param property
     * @return
     */
    public boolean hasPropertySet(String property) {
//    	return setProperties.contains(property);
    	return false;
    }
    
    public void setProperty(String property, String value) {
    	properties.put(property, value);
//    	setProperties.add(property);
    }
    
    public Map<String, String> getAllProperties() {
    	return properties;
    }
    
    public void resetSetProperties() {
    	setProperties.clear();
    }
    
    public void setProperty2(String property, String value) {
    	Class<? extends CSSStyle> c = this.getClass();
    	try {
			Field field = c.getDeclaredField(property);
			System.out.println(field.isEnumConstant());
			field.setAccessible(true);
			field.set(this, value);
			field.setAccessible(false);
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private int parseDimension(String value) {
    	value = value.trim();
    	int offset = 0;
    	if (value.endsWith("px")) offset = 2;
    	if (value.endsWith("%")) offset = 1;
    	return Integer.parseInt(value.substring(0, value.length() - offset));
    }
    
    /**
     * Convert the string properties and values to actual properties on this class
     * TODO handle margin and padding where 1 value sets all 4
     */
    public void finalizeCSS() {
    	for (Entry<String, String> e : properties.entrySet()) {
    		String value = e.getValue().trim();
    		switch (e.getKey()) {
    		case "background-color": 	backgroundColor = new CSSColor(value); break;
    		case "color": 				color = new CSSColor(value); break;
    		case "display":				display = displayType.valueOf(value.toUpperCase()); break;
    		case "font-family":			fontFamily = value; break;
    		case "font-size":			fontSize = Integer.parseInt(value); break;
    		case "font-style":			fontStyle = fontStyleType.valueOf(value.toUpperCase()); break;
    		case "font-weight":			fontWeight = fontWeightType.valueOf(value.toUpperCase()); break;
    		case "height":				height = Float.parseFloat(value.endsWith("%") ? value.substring(0, value.length() - 1) : value);
							    		heightType = value.contains("%") ? 
												dimensionType.PERCENTAGE : 
												dimensionType.PIXEL; break;
    		case "margin-top":			marginTop = parseDimension(value);  break;
    		case "margin-right":		marginRight = parseDimension(value);  break;
    		case "margin-bottom":		marginBottom = parseDimension(value);  break;
    		case "margin-left":			marginLeft = parseDimension(value);  break;
    		case "padding-top":			paddingTop = parseDimension(value);  break;
    		case "padding-right":		paddingRight = parseDimension(value);  break;
    		case "padding-bottom":		paddingBottom = parseDimension(value);  break;
    		case "padding-left":		paddingLeft = parseDimension(value);  break;
    		case "text-align":			textAlign = textAlignType.valueOf(value.toUpperCase()); break;
    		case "width":				width = Float.parseFloat(value.endsWith("%") ? value.substring(0, value.length() - 1) : value);
    									widthType = value.contains("%") ? 
    											dimensionType.PERCENTAGE : 
    											dimensionType.PIXEL; break;
    		}
    	}
    }
    
    /**
     * Apply some CSS rules
     * @param css	Map<String, String> of the CSS attribute and value
     */
    public void apply(Map<String, String> css) {
    	for (Entry<String, String> e : css.entrySet()) {
    		properties.put(e.getKey(), e.getValue());
    	}
    }
    
}
