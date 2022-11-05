package browser.renderer;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import browser.constants.PseudoElementConstants;
import browser.css.CSSStyle.fontStyleType;
import browser.css.CSSStyle.fontWeightType;
import browser.model.Box;
import browser.model.CSSColor;
import browser.model.RenderNode;
import browser.parser.HTMLElements;

public class HTMLRenderer {
    
    private static final boolean drawOutlines = false; //true false
    private static final boolean drawPadding = false;
    private static final boolean drawMargins = false;
    private static final float textOffsetScale = 0.75f;
    
    public static void render(GraphicsContext gc, RenderNode root) {
        // Draw box background
        if (!root.type.equals(HTMLElements.TEXT) && root.style.backgroundColor != null) {
            fillRect(gc, root.style.backgroundColor, root.box.x, root.box.y, root.box.width, root.box.height);
        }

        switch (root.type) {
            case HTMLElements.IMG:
                renderImage(gc, root);
                break;
            case HTMLElements.HR:
                fillRect(gc, root.style.color, root.box.x, root.box.y, root.box.width, root.box.height);
                break;
            case HTMLElements.PSEUDO_MARKER:
                renderPseudoMarker(gc, root);
                break;
        }

        // Draw borders
        if (root.style.borderWidthTop > 0) fillRect(gc, root.style.borderColorTop, root.box.x - root.style.borderWidthLeft, root.box.y - root.style.borderWidthTop, root.box.width + root.style.borderWidthLeft + root.style.borderWidthRight, root.style.borderWidthTop);
        if (root.style.borderWidthBottom > 0) fillRect(gc, root.style.borderColorBottom, root.box.x  - root.style.borderWidthLeft, root.box.y + root.box.height, root.box.width + root.style.borderWidthLeft + root.style.borderWidthRight, root.style.borderWidthTop);
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

    public static void renderPseudoMarker(GraphicsContext gc, RenderNode node) {
        gc.setFill(node.style.color.toPaint());
        String markerType = node.attributes.get(PseudoElementConstants.MARKER_TYPE_KEY);
        String markerIndex = node.attributes.get(PseudoElementConstants.MARKER_INDEX_KEY);
        String text = markerType.equals(HTMLElements.UL) ? "\u2022" : String.format("%d.", Integer.parseInt(markerIndex) + 1);
        gc.fillText(text, node.box.x, node.box.y + node.box.height * textOffsetScale);
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
        gc.fillText(node.text, node.box.x, node.box.y + node.box.height * textOffsetScale);
    }
    
    public static void fillRect(GraphicsContext gc, CSSColor color, float x, float y, float w, float h) {
        if (w <= 0f || h <= 0f) return;
        gc.setFill(color.toPaint());
        gc.fillRect(x, y, w, h);
    }
    
    public static void setBackground(GraphicsContext gc, CSSColor color, float w, float h) {
        fillRect(gc, color, 0, 0, w, Math.max(h, (float) gc.getCanvas().getHeight()));
    }
    
}
