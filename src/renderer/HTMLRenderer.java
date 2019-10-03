package renderer;

import java.util.Stack;

import model.DOMNode;
import model.RenderNode;

public class HTMLRenderer {
    
    public RenderNode generateRenderTree(DOMNode dom) {
        RenderNode root = new RenderNode("body");
        
        return root;
    }
    
    public void calculateLayouts(DOMNode root, int width) {
        
        Stack<DOMNode> nodes = new Stack<DOMNode>();
        nodes.add(root);
        while(!nodes.isEmpty()) {
            DOMNode current = nodes.pop();
        }
        
    }
    
    public void render(DOMNode root) {
        
    }
    
}
