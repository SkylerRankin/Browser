package model;

import java.util.ArrayList;
import java.util.List;

import css.CSSStyle;

public class RenderNode {
    
    public String type;
    public String content;
    public List<RenderNode> children;
    public DOMNode parent;
    public CSSStyle style;
    
    public Box box;
    
    public RenderNode(String type) {
        this.type = type;
        children = new ArrayList<RenderNode>();
    }
    
    public void render() {}
    
    public void print() {
        print("");
    }
    public void print(String pad) {
        System.out.println(pad+type);
        if (content != null) System.out.println(pad+"    "+content);
        for (RenderNode n : children) {
            n.print(pad+"    ");
        }
        System.out.println(pad+type);
    }

}
