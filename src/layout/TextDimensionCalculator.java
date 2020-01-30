package layout;

import java.util.ArrayList;
import java.util.List;

import css.CSSStyle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import model.Vector2;

public class TextDimensionCalculator {
    
    /**
     * Determine the width and height of the box containing some text based on its CSS styling.
     * @param text
     * @param style
     * @return
     */
    public static Vector2 getTextDimension(String s, CSSStyle style) {
    	Text text = new Text(s);
    	FontWeight fontWeight = FontWeight.NORMAL;
    	if (style.fontWeight == CSSStyle.fontWeightType.BOLD) {
    		fontWeight = FontWeight.BOLD;
    	}
    	text.setFont(Font.font(style.fontFamily, fontWeight, style.fontSize));
    	float width = (float) text.getBoundsInLocal().getWidth();
    	float height = (float) text.getBoundsInLocal().getHeight();
        return new Vector2(width, height);
    }
    
    /**
     * Split a string into segments such that each segment's width is as large as possible without
     * violating the maximum width.
     * @param s
     * @param style
     * @param maxWidth
     * @return
     */
    public static List<String> splitToWidth(String s, CSSStyle style, float maxWidth) {
    	List<String> lines = new ArrayList<String>();
    	float totalWidth = getTextDimension(s, style).x;
    	if (totalWidth <= maxWidth) {
    		lines.add(s);
    		return lines;
    	}
//    	System.out.printf("max=%f, total=%f stringlength=%d\n", maxWidth, totalWidth, s.length());
    	int count = 0;
    	
    	// Search for the largest prefix that fits the width
    	
    	int lineStart = 0;
    	int startIndex = 0;
    	int endIndex = s.length() - 1;
    	// Make initial guess based on a mono-spaced font
    	int currentIndex = Math.round(s.length() / totalWidth * maxWidth);
    	
//    	System.out.printf("Initial guess = %d\n", currentIndex);
    	
    	while (startIndex < s.length()) {
    		// Binary search for largest substring that fits in width and starts at startIndex
//    		System.out.printf("\nlineStart=%d, start=%d, curr=%d end=%d\n", lineStart, startIndex, currentIndex, endIndex);
    		
    		while (startIndex < endIndex && count++ < 20) {
    			float currentWidth = getTextDimension(s.substring(lineStart, currentIndex), style).x;
//    			System.out.printf("Search iteration: currentWidth = %f\n", currentWidth);
    			float nextWidth = -1;
    			
    			if (currentWidth <= maxWidth && currentIndex == s.length() - 1) {
        			// Handle case for the last segment
//        			System.out.printf("Found last segment of string\n");
        			break;
        		}
    			
    			// Check if this index is correct: fits within width, but adding one more letter goes beyond max width for a line
        		if (currentWidth <= maxWidth && currentIndex < s.length()) {
//        			System.out.printf("Current width passed\n");
            		nextWidth = getTextDimension(s.substring(lineStart, currentIndex + 1), style).x;
            		if (nextWidth > maxWidth) {
//            			System.out.printf("Next Width fails\nFound correct index\n");
            			break;
            		}
        		}
        		
//        		System.out.printf("max=%f current=%f next=%f\n", maxWidth, currentWidth, nextWidth);
        		if (currentWidth <= maxWidth && nextWidth <= maxWidth) {
        			// If both the current string and one more letter fit, then continue searching from the current index
        			startIndex = currentIndex;
        			currentIndex = startIndex + (endIndex - startIndex) / 2;
//        			System.out.printf("Both fit more; updating start to %d, current to %d\n", startIndex, currentIndex);
        		} else if (currentWidth > maxWidth) {
        			// If neither fit, reduce the upper bound. If currentWidth is too large, nextWidth is definitely too large
        			endIndex = currentIndex;
        			currentIndex = startIndex + (endIndex - startIndex) / 2;
//        			System.out.printf("Neither fit more; updating end to %d, current to %d\n", endIndex, currentIndex);
//        			System.out.printf("start=%d curr=%d end=%d\n", startIndex, currentIndex, endIndex);
        		}
    		}
    		
//    		System.out.printf("lineStart = %d, start = %d, curr = %d, end = %d\n", lineStart, startIndex, currentIndex, endIndex);
//    		System.out.printf("Found index %d, adding segment [%s]\n", currentIndex, s.substring(lineStart, currentIndex + 1));
    		lines.add(s.substring(lineStart, currentIndex + 1));
    		startIndex = currentIndex + 1;
    		lineStart = startIndex;
    		currentIndex = Math.min((lines.size() + 1) * lines.get(0).length(), s.length() - 1);
    		endIndex = s.length() - 1;
    		
    	}
    	
    	return lines;
    }
    
}
