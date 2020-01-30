package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import css.CSSStyle;

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
    
    public void render() {}
    
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

}
