package renderer;

import java.util.Stack;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import model.Box;
import model.DOMNode;
import model.RenderNode;

public class HTMLRenderer {
    
    public static void render(GraphicsContext gc, RenderNode root) {
    	System.out.printf("rendering: %s\n", root.type);
    	drawBox(gc, root.box);
    	if (root.text != null) drawText(gc, root);
    	for (RenderNode child : root.children) {
    		render(gc, child);
    	}
    }
    
    public static void drawBox(GraphicsContext gc, Box box) {
    	gc.strokeRect(box.x, box.y, box.width, box.height);
    }
    
    public static void drawText(GraphicsContext gc, RenderNode node) {
    	System.out.printf("drawing text %s at (%.2f, %.2f)\n", node.text, node.box.x, node.box.y + node.box.height);
        gc.fillText(node.text, node.box.x, node.box.y + node.box.height);
    }
    
}
