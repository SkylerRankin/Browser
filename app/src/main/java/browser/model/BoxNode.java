package browser.model;

import static browser.css.CSSStyle.DisplayType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import browser.css.CSSStyle;

public class BoxNode {

    public static int nextId = 0;

    public int id;
    public List<BoxNode> children = new ArrayList<>();
    public BoxNode parent = null;
    public int renderNodeId = -1;
    public int inlineFormattingContextId = -1;
    public int blockFormattingContextId = -1;
    public int tableFormattingContextId = -1;
    public RenderNode correspondingRenderNode = null;
    public CSSStyle style = new CSSStyle();
    public DisplayType outerDisplayType;
    public DisplayType innerDisplayType;
    public DisplayType auxiliaryDisplayType = null;
    public boolean isAnonymous = false;
    public boolean isTextNode = false;
    public boolean isPseudo = false;
    public Float x = null;
    public Float y = null;
    public Float width = null;
    public Float height = null;
    public boolean whiteSpaceAfter;

    // The range of indices for the text of this node: from textStartIndex up to (but not including) textEndIndex.
    public int textStartIndex;
    public int textEndIndex;

    // Block boxes generally have fixed widths. However, when inline block widths are calculated, there can be
    // instances where a block box's width should be shrunk to its content. This flag enables that shrinking.
    public boolean shrinkBlockWidthToContent = false;

    public BoxNode() {}

    public BoxNode(BoxNode other) {
        this.id = nextId++;
        this.parent = other.parent;
        this.renderNodeId = other.renderNodeId;
        this.inlineFormattingContextId = other.inlineFormattingContextId;
        this.blockFormattingContextId = other.blockFormattingContextId;
        this.correspondingRenderNode = other.correspondingRenderNode;
        this.style = other.style == null ? null : other.style.deepCopy();
        this.outerDisplayType = other.outerDisplayType;
        this.innerDisplayType = other.innerDisplayType;
        this.auxiliaryDisplayType = other.auxiliaryDisplayType;
        this.isAnonymous = other.isAnonymous;
        this.isTextNode = other.isTextNode;
        this.isPseudo = other.isPseudo;
        this.x = other.x;
        this.y = other.y;
        this.width = other.width;
        this.height = other.height;
        this.textStartIndex = other.textStartIndex;
        this.textEndIndex = other.textEndIndex;
        this.whiteSpaceAfter = other.whiteSpaceAfter;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BoxNode other)) return false;
        if (other.id != id || other.outerDisplayType != outerDisplayType ||
                other.innerDisplayType != innerDisplayType || other.isAnonymous != isAnonymous ||
                other.isTextNode != isTextNode || ((parent == null) != (other.parent == null)) ||
                (parent != null && (parent.id != other.parent.id)) || other.children.size() != children.size() ||
                renderNodeId != other.renderNodeId || !Objects.equals(x, other.x) || !Objects.equals(y, other.y) ||
                !Objects.equals(width, other.width) || !Objects.equals(height, other.height) ||
                !Objects.equals(textStartIndex, other.textStartIndex) || !Objects.equals(textEndIndex, other.textEndIndex) ||
                whiteSpaceAfter != other.whiteSpaceAfter) {
            return false;
        }

        for (int i = 0; i < children.size(); i++) {
            if (!children.get(i).equals(other.children.get(i))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        String positionSize = String.format("@(%.0f, %.0f), (%.0f x %.0f)", x, y, width, height);
        String flags = "" + (isAnonymous ? "a" : "") + (isTextNode ? "t" : "");
        if (isPseudo) flags += "p";
        String type = correspondingRenderNode == null ? "?" : correspondingRenderNode.type;
        if (isAnonymous) {
            type = "anon";
        }
        String textRange = isTextNode ? String.format(", tx:%d-%d", textStartIndex, textEndIndex) : "";
        return String.format("(%s) id=%s, outer=%s, inner=%s, parent=%d, rid=%d, %s, [%s], %s%s",
                type, id, outerDisplayType,
                innerDisplayType, parent == null ? -1 : parent.id, renderNodeId, positionSize, flags,
                children.stream().map(boxNode -> boxNode.id).toList(), textRange);
    }

    public String toRecursiveString() {
        return toRecursiveString("");
    }

    private String toRecursiveString(String padding) {
        StringBuilder string = new StringBuilder(padding + this + "\n");
        for (BoxNode child : children) {
            string.append(child.toRecursiveString(padding + "  "));
        }
        return string.toString();
    }

    public boolean isDescendantOf(int id) {
        if (parent == null) {
            return false;
        }

        if (parent.id == id) {
            return true;
        }

        return parent.isDescendantOf(id);
    }

    public BoxNode deepCopy() {
        BoxNode copy = new BoxNode(this);
        copy.parent = null;
        for (BoxNode child : children) {
            BoxNode childCopy = child.deepCopy();
            copy.children.add(childCopy);
            childCopy.parent = copy;
        }
        return copy;
    }

}
