package browser.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import browser.css.CSSStyle;

public class RenderNode {

    public static int nextId = 0;
    
    public int id;
    // Depth of 0 is the root body element.
    public int depth;
    public String type;
    public String text;
    public List<RenderNode> children;
    public RenderNode parent;
    public CSSStyle style;
    public Box box;
    public BoxNode boxNode;
    public Float maxWidth = null;
    public Float maxHeight = null;
    public String cssAttribute = null;
    // True if the original HTML tag had any whitespace after its closing tag.
    public boolean whiteSpaceAfter;
    // A map of the attributes appearing in the HTML tag, such as style.
    public Map<String, String> attributes;
    // A generic set of properties used during layout and rendering.
    public final Map<String, Object> properties = new HashMap<>();
    
    // False until the BoxLayoutCalculator sets this node's box object correctly
    // This way we don't consider nodes to be at position (0, 0) when they are 
    // actually just not yet positioned.
    public boolean positioned = false;
    
    public RenderNode(String type) {
        this.type = type;
        children = new ArrayList<RenderNode>();
        box = new Box();
        maxWidth = null;
        maxHeight = null;
        cssAttribute = null;
        style = new CSSStyle();
        attributes = new HashMap<String, String>();
    }
    
    public RenderNode(DOMNode dom, int id, int depth) {
        type = dom.type;
        text = dom.content;
        this.depth = depth;
        this.id = id;
        children = new ArrayList<RenderNode>();
        box = new Box();
        cssAttribute = dom.attributes.get("style");
        style = new CSSStyle();
        attributes = new HashMap<String, String>();
    }
    
    public RenderNode(RenderNode node) {
        type = node.type;
        text = node.text;
        depth = node.depth;
        id = nextId++;
        children = new ArrayList<RenderNode>();
        box = new Box();
        cssAttribute = node.cssAttribute;
        style = node.style;
        attributes = node.attributes;
    }
    
    public void render() {}
    
    public boolean childrenHasElement(String element) {
        for (RenderNode child : children) {
            if (child.type.equals(element)) return true;
        }
        return false;
    }
    
    public RenderNode getElementInChildren(String element) {
        for (RenderNode child : children) {
            if (child.type.equals(element)) return child;
        }
        return null;
    }
    
    public List<RenderNode> getElementsInChildren(String element) {
        List<RenderNode> nodes = new ArrayList<RenderNode>();
        for (RenderNode child : children) {
            if (child.type.equals(element)) nodes.add(child);
        }
        return nodes;
    }

    public void addChild(RenderNode n) {
        children.add(n);
        n.parent = this;
    }

    public void addChildren(RenderNode... nodes) {
        for (RenderNode node : nodes) {
            addChild(node);
        }
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof RenderNode node)) {
            return false;
        }

        if (this.id != node.id ||
                this.depth != node.depth ||
                !this.type.equals(node.type) ||
                !this.style.equals(node.style) ||
                !this.box.equals(node.box) || (maxWidth == null ? node.maxWidth != null : maxWidth.compareTo(node.maxWidth) != 0) ||
                (maxHeight == null ? node.maxHeight != null : maxHeight.compareTo(node.maxHeight) != 0)) {
            return false;
        }

        if (this.parent == null) {
            if (node.parent != null) {
                return false;
            }
        } else {
            if (parent.id != node.parent.id) {
                return false;
            }
        }

        if (cssAttribute == null) {
            if (node.cssAttribute != null) {
                return false;
            }
        } else if (!cssAttribute.equals(node.cssAttribute)) {
            return false;
        }

        if (whiteSpaceAfter != node.whiteSpaceAfter) {
            return false;
        }

        for (Map.Entry<String, String> e : attributes.entrySet()) {
            if (!node.attributes.containsKey(e.getKey())) {
                return false;
            }  else if (e.getValue() == null) {
                if (node.attributes.get(e.getKey()) != null) {
                    return false;
                }
            } else {
                if (!e.getValue().equals(node.attributes.get(e.getKey()))) {
                    return false;
                }
            }
        }

        if (node.attributes.size() != attributes.size()) {
            return false;
        }

        if (node.children.size() != children.size()) {
            return false;
        }

        for (int i = 0; i < children.size(); i++) {
            if (!children.get(i).equals(node.children.get(i))) {
                return false;
            }
        }

        if (text == null && node.text != null || (text != null && !text.equals(node.text))) {
            return false;
        }

        return true;
    }
    
    public void print() {
        print("");
    }
    
    public void print(String pad) {
        System.out.printf("%s%s [depth=%d id=%d]\n",pad, type, this.depth, this.id);
        if (text != null) System.out.printf("%s\t[%s]\n", pad, text);
        for (RenderNode n : children) {
            n.print(pad+"\t");
        }
        System.out.println(pad + "/" + type);
    }
    
    public void printStyle() {
        printStyle("");
    }
    
    public void printStyle(String pad) {
        System.out.printf("%s%s [depth=%d id=%d]\n",pad, type, this.depth, this.id);
        style.print(pad+" ");
        for (RenderNode n : children) {
            n.printStyle(pad+"\t");
        }
    }

    public String toString() {
        return String.format("%s, depth=%d, id=%d, %d children, text = %s", type, depth, id, children.size(), text);
    }

}
