package browser.renderer;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import browser.css.CSSStyle.fontStyleType;
import browser.css.CSSStyle.fontWeightType;
import browser.layout.BoxUtils;
import browser.model.Box;
import browser.model.BoxNode;
import browser.model.CSSColor;
import browser.parser.HTMLElements;

public class HTMLRenderer {

    private static final float textOffsetScale = 0.75f;
    
    public static void render(GraphicsContext gc, BoxNode root) {
        // Draw box background
        if (!root.isAnonymous && !root.correspondingRenderNode.type.equals(HTMLElements.TEXT) && root.style.backgroundColor != null) {
            fillRect(gc, root.style.backgroundColor, root.x, root.y, root.width, root.height);
        }

        if (!root.isAnonymous) {
            switch (root.correspondingRenderNode.type) {
                case HTMLElements.IMG:
                    renderImage(gc, root);
                    break;
                case HTMLElements.HR:
                    fillRect(gc, root.style.color, root.x, root.y, root.width, root.height);
                    break;
                case HTMLElements.PSEUDO_MARKER:
                    renderPseudoMarker(gc, root);
                    break;
            }
        }

        if (root.style.borderWidthTop > 0) fillRect(gc, root.style.borderColorTop, root.x - root.style.borderWidthLeft, root.y - root.style.borderWidthTop, root.width + root.style.borderWidthLeft + root.style.borderWidthRight, root.style.borderWidthTop);
        if (root.style.borderWidthBottom > 0) fillRect(gc, root.style.borderColorBottom, root.x  - root.style.borderWidthLeft, root.y + root.height, root.width + root.style.borderWidthLeft + root.style.borderWidthRight, root.style.borderWidthTop);
        if (root.style.borderWidthLeft > 0) fillRect(gc, root.style.borderColorLeft, root.x - root.style.borderWidthLeft, root.y, root.style.borderWidthLeft, root.height);
        if (root.style.borderWidthRight > 0) fillRect(gc, root.style.borderColorRight, root.x + root.width, root.y, root.style.borderWidthRight, root.height);

        if (RenderSettings.renderPadding || root.id == RenderSettings.hoveredElementID) {
            CSSColor paddingColor = new CSSColor("rgba(183, 196, 127, 100)");
            fillRect(gc, paddingColor, root.x, root.y, root.width, root.style.paddingTop);
            fillRect(gc, paddingColor, root.x, root.y + root.height - root.style.paddingBottom, root.width, root.style.paddingBottom);
            fillRect(gc, paddingColor, root.x, root.y, root.style.paddingLeft, root.height);
            fillRect(gc, paddingColor, root.x + root.width - root.style.paddingRight, root.y, root.style.paddingRight, root.height);
        }

        if (RenderSettings.renderMargins || root.id == RenderSettings.hoveredElementID) {
            CSSColor marginColor = new CSSColor("rgba(227, 151, 73, 100)");
            fillRect(gc, marginColor, root.x, root.y - root.style.marginTop, root.width, root.style.marginTop);
            fillRect(gc, marginColor, root.x, root.y + root.height, root.width, root.style.marginBottom);
            fillRect(gc, marginColor, root.x - root.style.marginLeft, root.y, root.style.marginLeft, root.height);
            fillRect(gc, marginColor, root.x + root.width, root.y, root.style.marginRight, root.height);
        }

        if (RenderSettings.renderOutlines) {
            drawBoxOutline(gc, root);
        }

        if (root.isTextNode) drawText(gc, root);

        for (BoxNode child : root.children) {
            render(gc, child);
        }

        // Render the highlight after the children, so it appears on top.
        if (root.id == RenderSettings.hoveredElementID) {
            CSSColor highlightColor = new CSSColor("rgba(3, 152, 252, 100)");
            Box contentBox = BoxUtils.getBoxWithoutPadding(root);
            fillRect(gc, highlightColor, contentBox.x, contentBox.y, contentBox.width, contentBox.height);
        }
    }
    
    public static void renderImage(GraphicsContext gc, BoxNode root) {
        Image image = ImageCache.getImage(root.correspondingRenderNode.attributes.get("src"));
        gc.drawImage(image, root.x, root.y, root.width, root.height);
    }

    public static void renderPseudoMarker(GraphicsContext gc, BoxNode node) {
        gc.setFill(node.style.color.toPaint());
        gc.setFont(Font.font(node.style.fontFamily, FontWeight.NORMAL, FontPosture.REGULAR, node.style.fontSize));
        String text = node.correspondingRenderNode.text;
        gc.fillText(text, node.x, node.y + node.height * textOffsetScale);
    }
    
    public static void drawBoxOutline(GraphicsContext gc, BoxNode box) {
        gc.setStroke(Color.GRAY);
        gc.strokeRect(box.x, box.y, box.width, box.height);
    }
    
    public static void drawText(GraphicsContext gc, BoxNode node) {
        FontWeight fontWeight = FontWeight.NORMAL;
        FontPosture fontPosture = FontPosture.REGULAR;
        if (node.style.fontWeight == fontWeightType.BOLD) fontWeight = FontWeight.BOLD;
        if (node.style.fontStyle == fontStyleType.ITALICS) fontPosture = FontPosture.ITALIC;
        gc.setFill(node.style.color.toPaint());
        gc.setFont(Font.font(node.style.fontFamily, fontWeight, fontPosture, node.style.fontSize));
        String fullText = node.correspondingRenderNode.text;
        String subText = fullText.substring(node.textStartIndex, node.textEndIndex);
        gc.fillText(subText, node.x, node.y + node.height * textOffsetScale);
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
