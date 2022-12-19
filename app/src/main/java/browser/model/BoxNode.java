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
    public RenderNode correspondingRenderNode = null;
    public CSSStyle style;
    public DisplayType outerDisplayType;
    public DisplayType innerDisplayType;
    public boolean isAnonymous = false;
    public boolean isTextNode = false;
    public Float x = null;
    public Float y = null;
    public Float width = null;
    public Float height = null;
    public Float maxWidth = null;
    public Float maxHeight = null;

    // The range of indices for the text of this node: from textStartIndex up to (but not including) textEndIndex.
    public int textStartIndex;
    public int textEndIndex;

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
        this.isAnonymous = other.isAnonymous;
        this.isTextNode = other.isTextNode;
        this.x = other.x;
        this.y = other.y;
        this.width = other.width;
        this.height = other.height;
        this.maxWidth = other.maxWidth;
        this.maxHeight = other.maxHeight;
        this.textStartIndex = other.textStartIndex;
        this.textEndIndex = other.textEndIndex;
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
                !Objects.equals(textStartIndex, other.textStartIndex) || !Objects.equals(textEndIndex, other.textEndIndex)) {
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
        String textRange = isTextNode ? String.format(", tx:%d-%d", textStartIndex, textEndIndex) : "";
        return String.format("id=%s, outer=%s, inner=%s, parent=%d, rid=%d, %s, [%s], %s%s",
                id, outerDisplayType, innerDisplayType, parent == null ? -1 : parent.id, renderNodeId,
                positionSize, flags, children.stream().map(boxNode -> boxNode.id).toList(), textRange);
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

    public BoxNode getRootAncestor() {
        BoxNode current = this;
        while (current.parent != null) {
            current = current.parent;
        }
        return current;
    }

    public boolean layoutEquals(BoxNode other) {
        if (other == null || !Objects.equals(x, other.x) || !Objects.equals(y, other.y) ||
                !Objects.equals(width, other.width) || !Objects.equals(height, other.height) || other.id != id ||
                (parent == null) != (other.parent == null) || (parent != null && parent.id != other.parent.id)) {
            return false;
        }

        for (int i = 0; i < children.size(); i++) {
            if (!children.get(i).layoutEquals(other.children.get(i))) {
                return false;
            }
        }

        return true;
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
