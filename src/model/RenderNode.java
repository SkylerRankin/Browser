package model;

import java.util.ArrayList;
import java.util.List;

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
    public Float maxWidth;
    public Float maxHeight;
    public String cssAttribute;
    
    public RenderNode(String type) {
        this.type = type;
        children = new ArrayList<RenderNode>();
        box = new Box();
        maxWidth = null;
        maxHeight = null;
        cssAttribute = null;
        style = new CSSStyle();
    }
    
    public RenderNode(DOMNode dom, int id, int depth) {
        type = dom.type;
        text = dom.content;
        this.depth = depth;
        this.id = id;
        children = new ArrayList<RenderNode>();
        cssAttribute = dom.attributes.get("style");
        style = new CSSStyle();
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
