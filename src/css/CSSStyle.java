package css;

public class CSSStyle {
    public static enum dimensionType {PIXEL, PERCENTAGE};
    public static enum displayType {BLOCK, INLINE};
    public static enum fontStyleType {NORMAL, ITALICS, BOLD};
    
    public dimensionType widthType = dimensionType.PIXEL;
    public Float width = null;
    
    public dimensionType heightType = dimensionType.PIXEL;
    public Float height = null;
    
    public displayType diplay = displayType.BLOCK;
    
    public int fontSize = 50;
    public fontStyleType fontStyle = fontStyleType.NORMAL;
}
