package browser.model;

import static browser.css.CSSStyle.DisplayType;

import java.util.ArrayList;
import java.util.List;

public class BoxNode {

    public int id;
    public List<BoxNode> children = new ArrayList<>();
    public BoxNode parent = null;
    public int renderNodeId = -1;
    public DisplayType outerDisplayType;
    public DisplayType innerDisplayType;
    public boolean isAnonymous = false;
    public boolean isTextNode = false;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BoxNode other)) return false;
        if (other.id != id || other.outerDisplayType != outerDisplayType ||
                other.innerDisplayType != innerDisplayType || other.isAnonymous != isAnonymous ||
                other.isTextNode != isTextNode || ((parent == null) != (other.parent == null)) ||
                (parent != null && (parent.id != other.parent.id)) || other.children.size() != children.size() ||
                renderNodeId != other.renderNodeId) {
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
        return String.format("id=%s, outer=%s, inner=%s, anonymous=%s, text=%s, parent id=%d, render node=%d",
                id, outerDisplayType, innerDisplayType, isAnonymous, isTextNode, parent == null ? -1 : parent.id,
                renderNodeId);
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

}
