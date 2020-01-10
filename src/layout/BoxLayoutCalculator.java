package layout;

import java.util.HashMap;
import java.util.Map;

import model.RenderNode;
import model.Vector2;

public class BoxLayoutCalculator {
    
    private float screenWidth;
    private float height;
    
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
    		root.maxWidth = screenWidth;
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
    	System.out.printf("%s[%s] (%.2f, %.2f), (%.2f, %.2f)\n", pad, root.type, root.box.x, root.box.y, root.box.width, root.box.height);
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
    	System.out.printf("calculateBoxes: root=%s parent=%s\n", root.type, (parent == null ? null : parent.type));
    	
    	if (parent != null) {
    		Vector2 nextPosition = nextPosition(parent);
    		root.box.x = nextPosition.x;
    		root.box.y = nextPosition.y;
    		propagateSize(root);
    		lastAddedChildMap.put(parent.id, root);
    	}
    	
    	for (RenderNode child : root.children) {
    		calculateBoxes(child);
    	}
    }
    
    /**
     * When a render node's dimensions are updated, this information must be send upwards through
     * the tree, such that the higher nodes expand to accommodate the now larger child node.
     * @param node      The node with a newly updated dimension.
     */
    public void propagateSize(RenderNode node) {
    	RenderNode parent = parentNodeMap.get(node.id);
    	System.out.printf("propagateSize: node=%s, parent=%s\n", node.type, (parent == null ? null : parent.type));
    	if (parent == null) return;
    	
    	float newWidth = node.box.width;
    	float newHeight = node.box.height;
    	
    	parent.box.width = newWidth;
    	parent.box.height = newHeight;
    	
    	propagateSize(parent);
    }
    
    public Vector2 nextPosition(RenderNode parent) {
        RenderNode lastAddedChild = lastAddedChildMap.get(parent.id);
        if (lastAddedChild == null) {
        	// If this is the first child, then it gets added in the top right of parent
            return new Vector2(parent.box.x, parent.box.y);
        } else {
        	// If this is not first, add it relative to the last added child
        	// Below assumes all block level elements; need to handle in-line
            return new Vector2(parent.box.x, parent.box.y + parent.box.height);
        }
    }

}
