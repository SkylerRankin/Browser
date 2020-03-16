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
//		System.out.printf("splitTextNode: %.2f %.2f, text=%s\n", firstWidth, laterWidth, text.text);
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
			if (line.length() > 0) {
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
	}
	

	/**
	 * Splits a node that is not a text element, but just contains a single child that is
     * a text node.
	 * @param node         The node containing the text element.
	 * @param parent       The parent of the node.
	 * @param firstWidth   The width to fit the first line of text to
	 * @param laterWidth   The width to fit every other line to
	 */
	public void splitContainingTextNode(RenderNode node, RenderNode parent, float firstWidth, float laterWidth) {
	    if (node.children.size() != 1) {
	        System.err.printf("splitContainingTextNode: node must have a single child, had %d\n", node.children.size());
	        return;
	    }
	    RenderNode text = node.children.get(0);
//	    System.out.printf("splitContainingTextNode: %.2f %.2f, text=%s\n", firstWidth, laterWidth, text.text);
	    List<String> lines = splitToWidth(text.text, text.style, firstWidth, laterWidth);
	    if (lines == null || lines.size() == 0) {
	        System.err.println("TextSplitter.splitTextNode failed");
	        return;
	    }
	    int childIndex = parent.children.indexOf(node);
	        
	    text.text = lines.get(0);
	    Vector2 textSize = getTextDimension(text.text, text.style);
	    text.box.width = textSize.x;
	    text.box.height = textSize.y;
	    node.box.width = textSize.x;
	    node.box.height = textSize.y;
	        
	    for (int i = 1; i < lines.size(); i++) {
	        String line = lines.get(i);
	        if (line.length() > 0) {
	            RenderNode newLine = new RenderNode(node);
	            RenderNode newText = new RenderNode(text);
	            newText.text = line;
	            newLine.maxWidth = node.maxWidth;
	            newLine.maxHeight = node.maxHeight;
	            newText.maxWidth = text.maxWidth;
	            newText.maxHeight = text.maxHeight;
	            textSize = getTextDimension(line, newLine.style);
	            newLine.box.width = textSize.x;
	            newLine.box.height = textSize.y;
	            newText.box.width = textSize.x;
	            newText.box.height = textSize.y;
	            newLine.children.add(newText);
	            parent.children.add(++childIndex, newLine);
	            parentNodeMap.put(newLine.id, parent);
	            parentNodeMap.put(newText.id, newLine);
	        }  
	    }
	}
	
	public boolean canBreakNode(RenderNode node, float availableWidth) {
	    if (node.children.size() != 1) return false;
	    RenderNode child = node.children.get(0);
	    return canBreakText(child, availableWidth);
	}
	
	// TODO: replace indexOf(" ") with a regex for spaces. this fails with tabs, for example.
	public boolean canBreakText(RenderNode node, float availableWidth) {
	    int spaceIndex = node.text.indexOf(" ");
	    if (spaceIndex == -1) return false;
		float width = getTextDimension(node.text.substring(0, spaceIndex), node.style).x;
		return width <= availableWidth;
	}
	
	public List<String> splitToWidth(String s, CSSStyle style, float width) {
	    return splitToWidth(s, style, width, width);
	}
	
	
	
    /**
     * Split a string into segments such that each segment's width is as large as possible without
     * violating the maximum width. Splits on spaces.
     * @param s
     * @param style
     * @param firstMaxWidth		space available for the first line
     * @param laterMaxWidth		space available for all other lines
     * @return
     */
    public List<String> splitToWidth(String s, CSSStyle style, float firstMaxWidth, float laterMaxWidth) {
    	List<String> lines = new ArrayList<String>();
    	String[] tokens = s.split("\\s");
    	Float[] tokenWidths = new Float[tokens.length];
    	float spaceWidth = getTextDimension(" ", style).x;
    	float maxWidth = firstMaxWidth;
    	float totalWidth = getTextDimension(s, style).x;
//        System.out.printf("space width = %.2f, total width = %.2f, max width = %.2f, %d tokens\n", spaceWidth, totalWidth, maxWidth, tokens.length);
    	if (totalWidth <= maxWidth || tokens.length == 1) {
    		lines.add(s);
    		return lines;
    	}
    	
    	if (firstMaxWidth <= 0 || laterMaxWidth <= 0 || s.length() == 0 || style == null) {
    	    System.out.printf("TextSplitter.splitToWidth called with invalid arguments:\n");
    	    System.out.printf("\tfirstMaxWidth = %f, laterMaxWidth = %f, totalWidth = %f\n", firstMaxWidth, laterMaxWidth, totalWidth);
    	    System.out.printf("\ttext = %s\n", s);
    	}
    	
    	for (int i = 0; i < tokens.length; i++) {
    	    Vector2 size = TextDimensionCalculator.getTextDimension(tokens[i], style);
    	    tokenWidths[i] = size.x;
    	}
    	
    	// If first width is too small for the first token, just use the later width. If that is also too small, then ignore it
    	if (firstMaxWidth < tokenWidths[0] + spaceWidth) {
    	    firstMaxWidth = laterMaxWidth;
    	}
    	
    	boolean first = true;
    	float currentWidth = 0;
    	StringBuilder currentLine = new StringBuilder();
    	for (int i = 0; i < tokens.length; i++) {
    	    if (currentWidth + spaceWidth + tokenWidths[i] >= (first ? firstMaxWidth : laterMaxWidth)) {
    	        lines.add(currentLine.toString());
    	        currentLine = new StringBuilder();
    	        currentLine.append(tokens[i] + " ");
    	        currentWidth = tokenWidths[i] + spaceWidth;
    	        first = false;
    	        
    	        if (i == tokens.length - 1) {
    	            currentLine.append(" ");
    	            lines.add(currentLine.toString());
    	        }
    	        
    	    } else {
    	        currentWidth = currentWidth + spaceWidth + tokenWidths[i];
    	        currentLine.append(tokens[i]);
    	        if (i == tokens.length - 1) {
    	            currentLine.append(" ");
    	            lines.add(currentLine.toString());
    	        } else {
    	            currentLine.append(" ");
    	        }
    	    }
    	}
    	
    	return lines;
    }

}
