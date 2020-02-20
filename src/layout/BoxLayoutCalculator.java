package layout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import css.CSSStyle;
import model.Box;
import model.RenderNode;
import model.Vector2;
import renderer.ImageCache;

public class BoxLayoutCalculator {
    
    private float screenWidth;
    
    private Map<Integer, RenderNode> lastAddedChildMap;
    private Map<Integer, RenderNode> parentNodeMap;
    private TextSplitter textSplitter;
        
    public BoxLayoutCalculator(Map<Integer, RenderNode> parentNodeMap, float screenWidth) {
    	this.screenWidth = screenWidth;
    	this.parentNodeMap = parentNodeMap;
        this.lastAddedChildMap = new HashMap<Integer, RenderNode>();
        textSplitter = new TextSplitter(parentNodeMap);
    }
    
    public void clearBoxBounds(RenderNode root) {
    	root.box = new Box();
    	for (RenderNode child : root.children) {
    		clearBoxBounds(child);
    	}
    }
    
    /**
     * Traverse the render tree and fill in all the boxes that have fixed sizes
     * @param root
     */
    public void setBoxBounds(RenderNode root) {
    	RenderNode parent = parentNodeMap.get(root.id);
    	if (parent == null) {
    		root.box.fixedWidth = true;
    		root.box.width = screenWidth;
    		root.box.x = 0;
    		root.box.y = 0;
    	} else {
    		root.maxWidth = parent.maxWidth;
    	}
    	
    	/* Cases of elements that have set widths
    	 * 	- text
    	 * 	- image
    	 */
    	
    	if (root.text != null) {
    		Vector2 textSize = TextDimensionCalculator.getTextDimension(root.text, root.style);
    		root.box.width = textSize.x;
    		root.box.height = textSize.y;
    	} else if (root.type.equals("img")) {
    		root.box.width = root.attributes.containsKey("width") ? Float.parseFloat(root.attributes.get("width")) : 50;
    		root.box.height = root.attributes.containsKey("height") ? Float.parseFloat(root.attributes.get("height")) : 50;
    		ImageCache.loadImage(root.attributes.get("src"));
    	}
    	
    	if (root.style.height != null) root.box.height = root.style.height;
        if (root.style.width != null) root.box.width = root.style.width;
    	
    	for (RenderNode child : root.children) {
    		setBoxBounds(child);
    	}
    	
    }
    
    public void printBoxes(RenderNode root) {
    	System.out.printf("BoxLayoutCalculator: printing boxes\n\n");
    	System.out.printf("[%d:%s] (%.2f, %.2f), (%.2f, %.2f)\n", root.id, root.type, root.box.x, root.box.y, root.box.width, root.box.height);
    	for (RenderNode child : root.children) {
    		printBoxes(child, "\t");
    	}
    	System.out.printf("\n");
    }
    
    private void printBoxes(RenderNode root, String pad) {
    	System.out.printf("%s[%d:%s] (%.2f, %.2f), (%.2f, %.2f), max(%.2f, %.2f)\n", pad, root.id, root.type, root.box.x, root.box.y, root.box.width, root.box.height, root.maxWidth, root.maxHeight);
    	for (RenderNode child : root.children) {
    		printBoxes(child, pad+"\t");
    	}
    }
    
    /**
     * Take the render node tree and fill in the x, y, width, and height of the boxes
     * Then, go from top to bottom; root node is as wide as screen, but height isn't known
     * need to propagate values upwards as height gets updated.
     * Updates lastAddedChild as it works down the tree
     * 
     * @param root		The root of the render tree to use
     */
    
    public void calculateBoxes(RenderNode root) {
    	RenderNode parent = parentNodeMap.get(root.id);
//    	System.out.printf("\ncalculateBoxes: root=%s parent=%s\n", root.type, (parent == null ? null : parent.type));
    	
    	if (parent != null) {
    		Vector2 nextPosition = nextPosition(root, parent);
    		root.box.x = nextPosition.x;
    		root.box.y = nextPosition.y;
    		root.positioned = true;
    		propagateSize(root);
    		lastAddedChildMap.put(parent.id, root);
    	}
    	
    	/* When splitting lines, more RenderNodes can be added to a nodes children, meaning
    	 * root.childen.size() can change over the course of the loop. Have to use for loop
    	 * over indices to avoid concurrent modification exception.
    	 */
    	for (int i = 0; i < root.children.size(); i++) {
    		calculateBoxes(root.children.get(i));
    	}
    }
    
    /**
     * Set the maximum dimensions of each node based on the maximum size of their parent, and so on.
     * Use this after using setBoxBounds, since it depends on boxes having their widths set.
     * @param root
     */
    public void propagateMaxSizes(RenderNode root) {
    	RenderNode parent = parentNodeMap.get(root.id);
    	if (parent == null) {
    		root.maxWidth = this.screenWidth;
    		root.maxHeight = null;
    	} else {
    		
    		if (root.box.fixedWidth) {
    			// Percentage based width depends on parent, only if parent specifies a width
    			if (root.style.widthType.equals(CSSStyle.dimensionType.PERCENTAGE) && parent.maxWidth != null) {
    				float parentAvailableWidth = parent.maxWidth - parent.style.paddingLeft - parent.style.paddingRight - root.style.marginLeft - root.style.marginRight;
    				root.maxWidth = parentAvailableWidth * root.box.width / 100f;
    			} else {
    				root.maxWidth = root.box.width + root.style.marginLeft + root.style.marginRight;
    			}
    		} else if (parent.maxWidth != null) {
    			root.maxWidth = parent.maxWidth - parent.style.paddingLeft - parent.style.paddingRight - root.style.marginLeft - root.style.marginRight;
    		}
    		
    		if (root.box.fixedHeight) {
    			// Percentage based height depends on parent, only if parent specifies a height
    			if (root.style.heightType.equals(CSSStyle.dimensionType.PERCENTAGE) && parent.maxHeight != null) {
    				float parentAvailableHeight = parent.maxWidth - parent.style.paddingTop - parent.style.paddingBottom - root.style.marginTop - root.style.marginBottom;
    				root.maxHeight = parentAvailableHeight * root.box.width / 100f;
    			} else {
    				root.maxHeight = root.box.height + root.style.marginTop + root.style.marginBottom;
    			}
    		} else if (parent.maxHeight != null) {
    			root.maxHeight = parent.maxHeight - parent.style.paddingTop - parent.style.paddingBottom - root.style.marginTop - root.style.marginBottom;
    		}
    		
    	}
    	
    	for (RenderNode child : root.children) {
    		propagateMaxSizes(child);
    	}
    	
    }
    
    /**
     * When a render node's dimensions are updated, this information must be send upwards through
     * the tree, such that the higher nodes expand to accommodate the now larger child node. For
     * nodes with percentage based sizes, if the parent gets larger, these nodes must also get 
     * larger to make sure the occupy the correct percentage. These changes only apply to nodes
     * that do not already have their width fixed.
     * @param node      The node with a newly updated dimension.
     */
    public void propagateSize(RenderNode node) {
    	RenderNode parent = parentNodeMap.get(node.id);
    	if (parent == null) return;
    	
    	// Find width and height by finding difference between farthest elements, vertically and horizontally
    	
    	RenderNode leftMost = null;
    	RenderNode rightMost = null;
    	RenderNode topMost = null;
    	RenderNode bottomMost = null;
    	
    	for (RenderNode child : parent.children) {
    		if (child.positioned) {
    			if (leftMost == null || child.box.x < leftMost.box.x) leftMost = child;
        		if (rightMost == null || (child.box.x + child.box.width) > (rightMost.box.x + rightMost.box.width)) rightMost = child;
        		if (topMost == null || child.box.y < topMost.box.y) topMost = child;
        		if (bottomMost == null || (child.box.y + child.box.height) > (bottomMost.box.y + bottomMost.box.height)) bottomMost = child;
    		}
    	}
    	
    	float newWidth = 0;
    	float newHeight = 0;
    	
    	if (parent.children.size() > 0) {
    		// Get distance between left and right-most nodes plus actual width of right-most to get width of element
    		// Then add in the padding and margins on each side. Margins on the "inside" don't matter for the overall size.
    		newWidth = rightMost.box.x - leftMost.box.x + rightMost.box.width + 
    				parent.style.paddingLeft + parent.style.paddingRight +
    				leftMost.style.marginLeft + rightMost.style.marginRight;
    		newHeight = bottomMost.box.y - topMost.box.y + bottomMost.box.height +
    				parent.style.paddingBottom + parent.style.paddingTop +
    				topMost.style.marginTop + bottomMost.style.marginBottom;
    	}
    	
//    	System.out.printf("propagateSize: node=%s, parent=%s, (%.2f, %.2f)\n", node.type, (parent == null ? null : parent.type), newWidth, newHeight);
    	
    	if (!parent.box.fixedWidth) {
    		parent.box.width = newWidth;
    	}
    	
    	if (!parent.box.fixedHeight) {
    		parent.box.height = newHeight;
    	}
    	
    	propagateSize(parent);
    }
    
    /**
     * Calculates the next valid position for an element, in relation to its parent and the other
     * previously added elements. Does not consider text alignment; everything is left justified
     * here and later shifted by the function applyJustification.
     * @param node
     * @param parent
     * @return
     */
    public Vector2 nextPosition(RenderNode node, RenderNode parent) {
    	CSSStyle.displayType displayType = node.style.display;
        RenderNode lastAddedChild = lastAddedChildMap.get(parent.id);
        if (lastAddedChild == null) {
        	
        	float availableWidth = parent.maxWidth - (parent.style.paddingLeft + node.style.marginLeft + node.style.marginRight + parent.style.paddingRight);
        	
        	if (node.type.equals("text") && availableWidth < node.box.width) {
        		textSplitter.splitTextNode(node, parent, availableWidth, availableWidth);
        	}
        	
        	// If this is the first child, then it gets added in the top right of parent
            return new Vector2(
            		parent.box.x + parent.style.paddingLeft + node.style.marginLeft,
            		parent.box.y + parent.style.paddingTop + node.style.marginTop
            );
        	
        } else {
        	switch (displayType) {
        	case INLINE:
        		// TODO handle stacking padding
        		// TODO put new lines in a horizontal row NO MATTER WHAT, text splitter handles putting them on new lines
        		// Why check if parent is null? there would already have been a null pointer exception
        		// Try in-line, but if it needs more space, continue to block case
        		
        		float x = lastAddedChild.box.x + lastAddedChild.box.width + lastAddedChild.style.marginRight + node.style.marginLeft;
        		float boundary = parent.maxWidth - parent.style.paddingRight - node.style.marginRight;

        		if (parent != null && (x + node.box.width <= boundary)) {
        			return new Vector2(x, lastAddedChild.box.y);
        		}
        		else if (node.type.equals("text")) {
        			float availableWidth = boundary - x;
        			
        			if (textSplitter.canBreak(node, availableWidth)) {
        				float fullWidth = parent.maxWidth - (parent.style.paddingLeft + node.style.marginLeft + node.style.marginRight + parent.style.paddingRight);
                    	textSplitter.splitTextNode(node, parent, availableWidth, fullWidth);
            			return new Vector2(x, lastAddedChild.box.y);
        			}
        		}
        	case BLOCK:
        	default:
        		// Default block elements get put to the left, and right below all the other elements. The height gets updated later.
        		// Bottom padding no longer impacts the last added child, so have to subtract that when finding the new values
        		// TODO probably other sides' padding have a stacking effect that needs to be managed
        		float bottomPaddingCorrection = parent.style.paddingBottom;
        		return new Vector2(
        				parent.box.x + parent.style.paddingLeft + node.style.marginLeft,
        				parent.box.y + parent.box.height + node.style.marginTop - bottomPaddingCorrection);
        	}
        }
    }
    
    /**
     * Convert percentage based dimensions into raw pixels.
     * @param root
     */
    public void finalizeDimensions(RenderNode root) {
    	
    	RenderNode parent = parentNodeMap.get(root.id);
    	
    	if (root.style.widthType.equals(CSSStyle.dimensionType.PERCENTAGE) && parent != null) {
    		root.box.fixedWidth = true;
    		root.box.width = (parent.box.width * root.style.width / 100.0f)
    		        - root.style.marginLeft - root.style.marginRight
    		        - parent.style.paddingLeft - parent.style.paddingRight;
    	}
    	
		if (root.style.heightType.equals(CSSStyle.dimensionType.PERCENTAGE)) {
			root.box.fixedHeight = true;
    		root.box.height = (parent.box.height * root.style.height / 100.0f)
    		        - root.style.marginTop - root.style.marginBottom
    		        - parent.style.paddingTop - parent.style.paddingBottom;
    	}
    	
    	for (RenderNode child : root.children) {
    		finalizeDimensions(child);
    	}
    }
    
    /**
     * Apply shifts for the text-Align CSS property. Since this property doesn't actually change
     * the sizes of containing elements, it can be applied after the boxes are calculated and
     * work just fine. The logic here is to find the left most and right most elements and check
     * what is the most that they can be moved left and right respectively. We then shift every
     * child element by that amount, in the specified direction.
     * TODO this is a costly function, should improve the runtime
     * TODO this will run multiple times on nodes that are next to each other, only needs to calculate the shifts from one of them
     * @param root
     */
    public void applyJustification(RenderNode root) {
    	CSSStyle.textAlignType alignment = root.style.textAlign;
    	if (!alignment.equals(CSSStyle.textAlignType.LEFT) && root.children.size() > 0) {
    		RenderNode parent = parentNodeMap.get(root.id);
    		RenderNode leftMost = null;
    		RenderNode rightMost = null;
    		
    		for (RenderNode child : root.children) {
    			if (leftMost == null || child.box.x < leftMost.box.x) leftMost = child;
    			if (rightMost == null || child.box.x > rightMost.box.x) rightMost = child;
    		}
    		
    		float leftSpace = 0;
    		float rightSpace = 0;
    		
            float parentWidth = parent == null ? root.box.width : parent.box.width;
    		float parentX = parent == null ? 0 : parent.box.x;
            int parentPaddingLeft = parent == null ? 0 : parent.style.paddingLeft;
    		int parentPaddingRight = parent == null ? 0 : parent.style.paddingRight;
    		
    		if (leftMost != null && rightMost != null) {
    			leftSpace = (leftMost.box.x - leftMost.style.marginLeft) - (parentX + parentPaddingLeft);
    			rightSpace =  (parentX + parentWidth - parentPaddingRight) - (rightMost.box.x + rightMost.box.width + rightMost.style.marginRight);
    		}
    		
//    		System.out.printf("leftMost=%s rightMost=%s leftSpace=%f rightSpace=%f\n", leftMost.type, rightMost.type, leftSpace, rightSpace);
    		// Should left space always be 0? since calculateBoxes does left justification?
    		
    		float xShift = 0;
    		
    		if (alignment.equals(CSSStyle.textAlignType.CENTER)) {
    			xShift = (leftSpace + rightSpace) / 2.0f;
    		} else if (alignment.equals(CSSStyle.textAlignType.RIGHT)) {
    			xShift = leftSpace + rightSpace;
    		}
    		    		
    		for (RenderNode child : root.children) {
        		applyShift(child, xShift, 0f);
        	}
    		
    	}
    	
    	for (RenderNode child : root.children) {
    		applyJustification(child);
    	}
    }
    
    /**
     * Shifts every element in the root including the root by x and y.
     * @param root
     * @param x
     * @param y
     */
    public void applyShift(RenderNode root, float x, float y) {
    	root.box.x += x;
    	root.box.y += y;
    	for (RenderNode child : root.children) {
    		applyShift(child, x, y);
    	}
    }
    
    /**
     * Once all dimensions and max sizes are set, check if any lines of text are too large for their
     * containing element. For these, split them into multiple lines by making new render nodes.
     * @param root
     */
    public void breakLines(RenderNode root) {
    	
    }

}
