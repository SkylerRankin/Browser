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
    
    public BoxLayoutCalculator(Map<Integer, RenderNode> parentNodeMap) {
    	this.parentNodeMap = parentNodeMap;
        this.lastAddedChildMap = new HashMap<Integer, RenderNode>();
    }
    
    /**
     * Take the render node tree and fill in the x, y, width, and height of the boxes
     * first pass should set all of the size requirements from the css, such as width=100px or height=50%
     * Then, go from top to bottom; root node is as wide as screen, but height isn't known
     * need to propagate values upwards as height gets updated
     * 
     * @param root		The root of the render tree to use
     */
    
    public void calculateBoxes(RenderNode root) {
        if (parentNodeMap.get(root.id) == null) {
            root.box.x = 0;
            root.box.y = 0;
            root.box.width = 0;
            root.box.height = 0;
        } else {
            Vector2 position = nextPosition(parentNodeMap.get(root.id));
            root.box.x = position.x;
            root.box.y = position.y;
            Vector2 size = calculateNodeSize(root);
            root.box.width = size.x;
            root.box.height = size.y;
            propagateSize(root);
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
    	if (node.parent == null) {
    		return;
    	}
    	
    	float newWidth = node.box.width;
    	float newHeight = node.box.height;
    	
    	// cannot just assume that if child is wider, then parent width is the child width, children could be side by side
    	
    }
    
    public Vector2 nextPosition(RenderNode parent) {
        RenderNode lastAddedChild = lastAddedChildMap.get(parent.id);
        if (lastAddedChild == null) {
            return new Vector2(parent.box.x, parent.box.y);
        } else {
            return new Vector2(parent.box.x, parent.box.y + parent.box.height);
        }
    }
    
    public Vector2 calculateNodeSize(RenderNode node) {
        Vector2 size = new Vector2();
        if (node.style.width != null) {
            
        }
        return size;
    }

}
