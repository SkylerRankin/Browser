package model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import parser.HTMLElements;

public class CSSNode {
    
    public String type;
    public CSSNode parent;
    public Set<CSSNode> children;
    public Map<String, String> declarations;
    
    public CSSNode(String type) {
        this.type = type;
        children = new HashSet<CSSNode>();
        declarations = new HashMap<String, String>();
    }
    
    public boolean hasChild(String element) {
        for (CSSNode child : children) {
            if (child.type.equals(element)) return true;
        }
        return false;
    }
    
    public CSSNode getChild(String element) {
        for (CSSNode child : children) {
            if (child.type.equals(element)) return child;
        }
        return null;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CSSNode) || obj == null) return false;
        CSSNode n = (CSSNode) obj;
        if (!n.type.equals(type)) return false;
        if (n.declarations.size() != declarations.size()) return false;
        for (Entry<String, String> e : declarations.entrySet()) {
            if (!n.declarations.keySet().contains(e.getKey())) return false;
            if (!n.declarations.get(e.getKey()).equals(e.getValue())) return false;
        }
        if (n.children.size() != children.size()) return false;
        for (CSSNode child : children) {
            boolean containsChild = false;
            for (CSSNode node : n.children) {
                if (child.equals(node)) containsChild = true;
            }
            if (!containsChild) return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = type.hashCode();
        for (Entry<String, String> e : declarations.entrySet()) {
            hash += e.getKey().hashCode() * 17 + e.getValue().hashCode() * 31;
        }
        for (CSSNode child : children) {
            hash += child.hashCode();
        }
        return hash;
    }
    
    @Override
    public String toString() {
        String string = type + ", #c="+children.size()+", #d="+declarations.size()+": ";
        for (Entry<String, String> e : declarations.entrySet()) {
            string += String.format("[%s = %s] ", e.getKey(), e.getValue());
        }
        for (CSSNode child : children)
            string += "\n"+child.toString();
        string += "\nend "+type;
        return string;
    }
    
}
