package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import css.CSSStyle;
import parser.RenderTreeGenerator;

public class RenderNode {
    
    public int id;
    // Depth of 0 is the root body element.
    public int depth;
    public String type;
    public String text;
    public List<RenderNode> children;
    public RenderNode parent;
    public CSSStyle style;
    public Box box;
    public Float maxWidth = null;
    public Float maxHeight = null;
    public String cssAttribute = null;
    public Map<String, String> attributes;
    
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
    	id = RenderTreeGenerator.getNextID();
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
    
    public void print() {
        print("");
    }
    
    public void print(String pad) {
        System.out.printf("%s%s [depth=%d id=%d]\n",pad, type, this.depth, this.id);
        if (text != null) System.out.println(pad+"\t"+text);
        for (RenderNode n : children) {
            n.print(pad+"\t");
        }
        System.out.println(pad+type);
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

}
