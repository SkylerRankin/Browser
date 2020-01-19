package layout;

import java.util.HashMap;
import java.util.Map;

import css.CSSStyle;
import model.RenderNode;
import model.Vector2;

public class BoxLayoutCalculator {
    
    private float screenWidth;
    
    private Map<Integer, RenderNode> lastAddedChildMap;
    private Map<Integer, RenderNode> parentNodeMap;
        
    public BoxLayoutCalculator(Map<Integer, RenderNode> parentNodeMap, float screenWidth) {
    	this.screenWidth = screenWidth;
    	this.parentNodeMap = parentNodeMap;
        this.lastAddedChildMap = new HashMap<Integer, RenderNode>();
    }
    
    /**
     * Traverse the render tree and fill in all the boxes that have fixed sizes, as well as the 
     * max height and width for each node, based on the parent size.
     * @param root
     */
    public void setBoxBounds(RenderNode root) {
    	RenderNode parent = parentNodeMap.get(root.id);
    	if (parent == null) {
    		root.box.fixedWidth = true;
    		root.box.width = screenWidth;
//    		root.maxWidth = screenWidth;
    		root.box.x = 0;
    		root.box.y = 0;
    	} else {
    		root.maxWidth = parent.maxWidth;
    	}
    	
    	if (root.text != null) {
    		Vector2 textSize = TextDimensionCalculator.getTextDimension(root.text, root.style);
    		root.box.width = textSize.x;
    		root.box.height = textSize.y;
    	}
    	
    	for (RenderNode child : root.children) {
    		setBoxBounds(child);
    	}
    	
    }
    
    public void printBoxes(RenderNode root) {
    	System.out.printf("BoxLayoutCalculator: printing boxes\n\n");
    	System.out.printf("[%s] (%.2f, %.2f), (%.2f, %.2f)\n", root.type, root.box.x, root.box.y, root.box.width, root.box.height);
    	for (RenderNode child : root.children) {
    		printBoxes(child, "\t");
    	}
    	System.out.printf("\n");
    }
    
    private void printBoxes(RenderNode root, String pad) {
    	System.out.printf("%s[%s] (%.2f, %.2f), (%.2f, %.2f), max(%.2f, %.2f)\n", pad, root.type, root.box.x, root.box.y, root.box.width, root.box.height, root.maxWidth, root.maxHeight);
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
//    		System.out.printf("nextPosition = %s\n", nextPosition.toString());
    		root.box.x = nextPosition.x;
    		root.box.y = nextPosition.y;
    		root.positioned = true;
    		propagateSize(root);
    		lastAddedChildMap.put(parent.id, root);
    	}
    	
    	for (RenderNode child : root.children) {
    		calculateBoxes(child);
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
    		root.maxWidth = root.box.fixedWidth ? new Float(root.box.width) : parent.maxWidth;
    		root.maxHeight = root.box.fixedHeight ? new Float(root.box.height) : parent.maxHeight;
    	}
    	
    	for (RenderNode child : root.children) {
    		propagateMaxSizes(child);
    	}
    }
    
    /**
     * When a render node's dimensions are updated, this information must be send upwards through
     * the tree, such that the higher nodes expand to accommodate the now larger child node.
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
    		newWidth = rightMost.box.x - leftMost.box.x + rightMost.box.width;
    		newHeight = bottomMost.box.y - topMost.box.y + bottomMost.box.height;
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
    
    public Vector2 nextPosition(RenderNode node, RenderNode parent) {
    	CSSStyle.displayType displayType = node.style.diplay;
        RenderNode lastAddedChild = lastAddedChildMap.get(parent.id);
        if (lastAddedChild == null) {
        	// If this is the first child, then it gets added in the top right of parent
            return new Vector2(parent.box.x, parent.box.y);
        } else {
        	switch (displayType) {
        	case INLINE:
        		// Try in-line, but if it needs more space, continue to block case
        		float x = lastAddedChild.box.x + lastAddedChild.box.width;
        		if (parent != null && x + node.box.width <= parent.maxWidth) {
        			return new Vector2(x, lastAddedChild.box.y);
        		}
        	case BLOCK:
        	default:
        		return new Vector2(parent.box.x, parent.box.y + parent.box.height);
        	}
        }
    }

}
