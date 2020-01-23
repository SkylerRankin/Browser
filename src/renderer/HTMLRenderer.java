package renderer;

import css.CSSStyle.fontWeightType;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Box;
import model.Color8Bit;
import model.DOMNode;
import model.RenderNode;

public class HTMLRenderer {
    
    public static void render(GraphicsContext gc, RenderNode root) {
    	System.out.printf("rendering: %s %f %f\n", root.type, root.box.x, root.box.y);
    	
    	// Draw the padding
    	Color8Bit paddingColor = new Color8Bit("yellow");
    	fillRect(gc, paddingColor, root.box.x, root.box.y, root.box.width, root.style.paddingTop);
    	fillRect(gc, paddingColor, root.box.x, root.box.y + root.box.height - root.style.paddingBottom, root.box.width, root.style.paddingBottom);
    	fillRect(gc, paddingColor, root.box.x, root.box.y, root.style.paddingLeft, root.box.height);
    	fillRect(gc, paddingColor, root.box.x + root.box.width - root.style.paddingRight, root.box.y, root.style.paddingRight, root.box.height);
    	
    	// Draw the margins
    	Color8Bit marginColor = new Color8Bit("blue");
    	fillRect(gc, marginColor, root.box.x, root.box.y - root.style.marginTop, root.box.width, root.style.marginTop);
    	fillRect(gc, marginColor, root.box.x, root.box.y + root.box.height, root.box.width, root.style.marginBottom);
    	fillRect(gc, marginColor, root.box.x - root.style.marginLeft, root.box.y, root.style.marginLeft, root.box.height);
    	fillRect(gc, marginColor, root.box.x + root.box.width, root.box.y, root.style.marginRight, root.box.height);
    	
    	// Draw a box outline
    	drawBox(gc, root.box);
    	
    	if (root.text != null) drawText(gc, root);
    	for (RenderNode child : root.children) {
    		render(gc, child);
    	}
    }
    
    public static void drawBox(GraphicsContext gc, Box box) {
    	gc.setFill(Color.BLACK);
    	gc.strokeRect(box.x, box.y, box.width, box.height);
    }
    
    public static void drawText(GraphicsContext gc, RenderNode node) {
    	FontWeight fontWeight = FontWeight.NORMAL;
    	if (node.style.fontWeight == fontWeightType.BOLD) fontWeight = FontWeight.BOLD;
    	gc.setFill(Color.BLACK);
    	gc.setFont(Font.font(node.style.fontFamily, fontWeight, node.style.fontSize));
        gc.fillText(node.text, node.box.x, node.box.y + node.box.height);
    }
    
    public static void fillRect(GraphicsContext gc, Color8Bit color, float x, float y, float w, float h) {
    	if (w <= 0f || h <= 0f) return;
    	gc.setFill(Color.color(color.r() / 255f, color.g() / 255f, color.b() / 255f));
    	gc.fillRect(x, y, w, h);
    }
    
}
