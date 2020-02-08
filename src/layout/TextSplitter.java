package layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import css.CSSStyle;
import model.RenderNode;
import model.Vector2;

import static layout.TextDimensionCalculator.getTextDimension;

public class TextSplitter {
	
	Map<Integer, RenderNode> parentNodeMap;
	
	public TextSplitter(Map<Integer, RenderNode> parentNodeMap) {
		this.parentNodeMap = parentNodeMap;
	}
	
	/**
	 * If a text node is too wide, split it up into multiple text nodes. These are added as 
	 * children of the parent.
	 * @param text		The node of text to split up
	 * @param parent	RenderNode parent of text
	 * @param width		Width to fit to
	 */
	public void splitTextNode(RenderNode text, RenderNode parent, float firstWidth, float laterWidth) {
		System.out.printf("splitTextNode: %.2f %.2f\n", firstWidth, laterWidth);
		List<String> lines = splitToWidth(text.text, text.style, firstWidth, laterWidth);
		if (lines == null || lines.size() == 0) {
			System.err.println("TextSplitter.splitTextNode failed");
			return;
		}
		int childIndex = parent.children.indexOf(text);
		
		text.text = lines.get(0);
		Vector2 textSize = getTextDimension(text.text, text.style);
		text.box.width = textSize.x;
		text.box.height = textSize.y;
		
		for (int i = 1; i < lines.size(); i++) {
			String line = lines.get(i);
			RenderNode newLine = new RenderNode(text);
			newLine.text = line;
			newLine.maxWidth = text.maxWidth;
			newLine.maxHeight = text.maxHeight;
			textSize = getTextDimension(line, newLine.style);
			newLine.box.width = textSize.x;
			newLine.box.height = textSize.y;
			parent.children.add(++childIndex, newLine);
			parentNodeMap.put(newLine.id, parent);
		}
	}
	
	
	public boolean canBreak(RenderNode node, float availableWidth) {
		float width = getTextDimension(node.text.substring(0, 1), node.style).x;
		return width <= availableWidth;
	}
	
    /**
     * Split a string into segments such that each segment's width is as large as possible without
     * violating the maximum width.
     * @param s
     * @param style
     * @param firstMaxWidth		space available for the first line
     * @param laterMaxWidth		space available for all other lines
     * @return
     */
    public List<String> splitToWidth(String s, CSSStyle style, float firstMaxWidth, float laterMaxWidth) {
    	List<String> lines = new ArrayList<String>();
    	float maxWidth = firstMaxWidth;
    	float totalWidth = getTextDimension(s, style).x;
    	if (totalWidth <= maxWidth) {
    		lines.add(s);
    		return lines;
    	}

    	int count = 0;
    	
    	// Search for the largest prefix that fits the width
    	
    	int lineStart = 0;
    	int startIndex = 0;
    	int endIndex = s.length();
    	// Make initial guess based on a mono-spaced font
    	int currentIndex = Math.round(s.length() / totalWidth * maxWidth) - 1;
    	
//    	System.out.printf("Initial guess = %d\n", currentIndex);
    	
    	while (startIndex < s.length()) {
    		// Binary search for largest substring that fits in width and starts at startIndex
//    		System.out.printf("\nlineStart=%d, start=%d, curr=%d end=%d\n", lineStart, startIndex, currentIndex, endIndex);
    		
    		while (startIndex < endIndex && ++count < 20) {
    			float currentWidth = getTextDimension(s.substring(lineStart, currentIndex + 1), style).x;
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
            		nextWidth = getTextDimension(s.substring(lineStart, currentIndex + 2), style).x;
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
    		endIndex = s.length();
    		if (maxWidth == firstMaxWidth) {
    			maxWidth = laterMaxWidth;
    		}
    		
//    		System.out.printf("%d %d\n", lineStart, endIndex);
    		if (lineStart >= endIndex) break;
    		
    	}
    	
    	return lines;
    }

}
