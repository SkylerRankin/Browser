package css;

import java.util.Map;
import java.util.Map.Entry;

import model.CSSColor;

public class CSSStyle {
	
    public static enum dimensionType {PIXEL, PERCENTAGE};
    public static enum displayType {BLOCK, INLINE};
    public static enum fontStyleType {NORMAL, ITALICS, OBLIQUE};
    public static enum fontWeightType {NORMAL, BOLD, OTHER};
    public static enum textAlignType {LEFT, CENTER, RIGHT};
    public static enum textDecorationType {NONE, OVERLINE, LINETHROUGH, UNDERLINE};
    public static enum wordWrapType {NORMAL, BREAKWORD};
    
    public CSSColor backgroundColor = new CSSColor("white");
    
    public CSSColor borderColor = new CSSColor("white");
    public int borderWidth = 1;
    public int borderWidthTop = 1;
    public int borderWidthRight = 1;
    public int borderWidthBottom = 1;
    public int borderWidthLeft = 1;
    
    public CSSColor color = new CSSColor("black");
    
    public displayType diplay = displayType.BLOCK;
    
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
    
    public textDecorationType textDecoration = textDecorationType.NONE;
    
    public wordWrapType wordWrap = wordWrapType.NORMAL;
    
    /**
     * Apply some CSS rules
     * @param css	Map<String, String> of the CSS attribute and value
     */
    public void apply(Map<String, String> css) {
    	for (Entry<String, String> e : css.entrySet()) {
    		switch (e.getKey()) {
    		case "font-size":
    			fontSize = Integer.parseInt(e.getValue());
    			break;
    		case "font-weight":
    			if (e.getValue().equals("bold")) fontWeight = fontWeightType.BOLD;
    			break;
    		}
    	}
    }
    
}
