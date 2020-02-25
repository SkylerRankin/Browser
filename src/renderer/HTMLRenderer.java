package renderer;

import java.io.File;

import css.CSSStyle.fontStyleType;
import css.CSSStyle.fontWeightType;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import model.Box;
import model.CSSColor;
import model.DOMNode;
import model.RenderNode;
import parser.HTMLElements;

public class HTMLRenderer {
    
    private static final boolean drawOutlines = false; //true false
    private static final boolean drawPadding = false;
    private static final boolean drawMargins = false;
    
    public static void render(GraphicsContext gc, RenderNode root) {
//    	System.out.printf("rendering: %s %f %f\n", root.type, root.box.x, root.box.y);
    	switch (root.type) {
    	case "img":
    		renderImage(gc, root);
    		return;
    	case "hr":
    		fillRect(gc, new CSSColor("black"), root.box.x, root.box.y, root.box.width, root.box.height);
    		return;
    	}
    	
    	// Draw box background
    	if (!root.type.equals(HTMLElements.TEXT)) fillRect(gc, root.style.backgroundColor, root.box.x, root.box.y, root.box.width, root.box.height);
    	
    	// Draw borders
    	// TODO: this leaves out the corners
    	if (root.style.borderWidthTop > 0) fillRect(gc, root.style.borderColorTop, root.box.x, root.box.y - root.style.borderWidthTop, root.box.width, root.style.borderWidthTop);
        if (root.style.borderWidthBottom > 0) fillRect(gc, root.style.borderColorBottom, root.box.x, root.box.y + root.box.height, root.box.width, root.style.borderWidthTop);
        if (root.style.borderWidthLeft > 0) fillRect(gc, root.style.borderColorLeft, root.box.x - root.style.borderWidthLeft, root.box.y, root.style.borderWidthLeft, root.box.height);
        if (root.style.borderWidthRight > 0) fillRect(gc, root.style.borderColorRight, root.box.x + root.box.width, root.box.y, root.style.borderWidthRight, root.box.height);

    	
    	if (drawPadding) {
    	    CSSColor paddingColor = new CSSColor("SteelBlue");
    	    fillRect(gc, paddingColor, root.box.x, root.box.y, root.box.width, root.style.paddingTop);
    	    fillRect(gc, paddingColor, root.box.x, root.box.y + root.box.height - root.style.paddingBottom, root.box.width, root.style.paddingBottom);
    	    fillRect(gc, paddingColor, root.box.x, root.box.y, root.style.paddingLeft, root.box.height);
    	    fillRect(gc, paddingColor, root.box.x + root.box.width - root.style.paddingRight, root.box.y, root.style.paddingRight, root.box.height);
    	}
    	
    	if (drawMargins) {
    	    CSSColor marginColor = new CSSColor("Gold");
    	    fillRect(gc, marginColor, root.box.x, root.box.y - root.style.marginTop, root.box.width, root.style.marginTop);
    	    fillRect(gc, marginColor, root.box.x, root.box.y + root.box.height, root.box.width, root.style.marginBottom);
    	    fillRect(gc, marginColor, root.box.x - root.style.marginLeft, root.box.y, root.style.marginLeft, root.box.height);
    	    fillRect(gc, marginColor, root.box.x + root.box.width, root.box.y, root.style.marginRight, root.box.height);
    	}
  	
    	if (drawOutlines) {
    	    drawBoxOutline(gc, root.box);
    	}
    	
    	if (root.text != null) drawText(gc, root);
    	for (RenderNode child : root.children) {
    		render(gc, child);
    	}
    }
    
    public static void renderImage(GraphicsContext gc, RenderNode root) {
    	Image image = ImageCache.getImage(root.attributes.get("src"));
    	gc.drawImage(image, root.box.x, root.box.y, root.box.width, root.box.height);
    }
    
    public static void renderList(GraphicsContext gc, RenderNode root) {
    	for (RenderNode child : root.children) {
    		child.text = String.format("\t• %s", child.text);
    		drawText(gc, child);
    	}
    }
    
    public static void drawBoxOutline(GraphicsContext gc, Box box) {
    	gc.setStroke(Color.GRAY);
    	gc.strokeRect(box.x, box.y, box.width, box.height);
    }
    
    public static void drawText(GraphicsContext gc, RenderNode node) {
    	FontWeight fontWeight = FontWeight.NORMAL;
    	FontPosture fontPosture = FontPosture.REGULAR;
    	if (node.style.fontWeight == fontWeightType.BOLD) fontWeight = FontWeight.BOLD;
    	if (node.style.fontStyle == fontStyleType.ITALICS) fontPosture = FontPosture.ITALIC;
    	gc.setFill(node.style.color.toPaint());
    	gc.setFont(Font.font(node.style.fontFamily, fontWeight, fontPosture, node.style.fontSize));
        gc.fillText(node.text, node.box.x, node.box.y + node.box.height);
    }
    
    public static void fillRect(GraphicsContext gc, CSSColor color, float x, float y, float w, float h) {
        if (w <= 0f || h <= 0f) return;
    	gc.setFill(color.toPaint());
    	gc.fillRect(x, y, w, h);
    }
    
}
