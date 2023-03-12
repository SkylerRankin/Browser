package browser.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import browser.parser.HTMLElements;

public class DOMNode {
    
    public String type;
    public String content;
    public Map<String, String> attributes;
    public List<DOMNode> children;
    public DOMNode parent;
    // True if the original tag was followed by some form of whitespace.
    public boolean whiteSpaceAfter;
    
    public DOMNode(String type) {
        this.type = type;
        children = new ArrayList<DOMNode>();
        attributes = new HashMap<String, String>();
    }
    
    public void addChild(DOMNode n) {
        children.add(n);
        n.parent = this;
    }

    public void addChildren(DOMNode... nodes) {
        for (DOMNode node : nodes) {
            addChild(node);
        }
    }
    
    public void print() {
        print("");
    }
    
    public void print(String pad) {
        if (this.type.equals((HTMLElements.TEXT))) {
            System.out.printf("%sTEXT: [%s]\n", pad, content);
        } else {
            System.out.print(pad + type + (whiteSpaceAfter ? " +w" : ""));
            for (Entry<String, String> entry : this.attributes.entrySet()) {
                System.out.print(String.format(" %s=%s", entry.getKey(), entry.getValue()));
            }
            System.out.println();
            for (DOMNode n : children) {
                n.print(pad+"    ");
            }
            System.out.println(pad + "/" + type);
        }
    }
    
    public boolean equalsIgnoreText(Object obj) {
        return equalsIgnoreText(obj, false);
    }
    
    public boolean equalsIgnoreText(Object obj, boolean printDifference) {
        if (!(obj instanceof DOMNode) || obj == null) return false;
        DOMNode n = (DOMNode) obj;
        if (!n.type.equals(this.type)) {
            if (printDifference) System.out.printf("DOMNode: type mismatch: %s != %s\n", n.type, this.type);
            return false;
        }
        
        for (Entry<String, String> e : attributes.entrySet()) {
            if (!n.attributes.containsKey(e.getKey())) {
                if (printDifference) System.out.printf("DOMNode: missing attribute %s\n", e.getKey());
                return false;
            }  else if (e.getValue() == null) {
                if (n.attributes.get(e.getKey()) != null) return false;
            } else {
                if (!e.getValue().equals(n.attributes.get(e.getKey()))) return false;
            }
        }

        if (n.whiteSpaceAfter != whiteSpaceAfter) {
            return false;
        }

        if (this.children.size() != n.children.size()) {
            if (printDifference) System.out.printf("DOMNode: children size mismatch: %d != %d\n", n.children.size(), this.children.size());
            return false;
        }
        
        for (DOMNode c : n.children) {
            int index = n.children.indexOf(c);
            if (this.children.size() < index) return false; 
            
            boolean foundChild = false;
            for (DOMNode child : this.children) {
                if (child.equalsIgnoreText(c)) {
                    foundChild = true;
                }
            }
            if (!foundChild) return false;
        }
        return true;
        
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DOMNode) || obj == null) return false;
        DOMNode n = (DOMNode) obj;
        if (!n.type.equals(this.type)) return false;
        if (n.content != null) {
            if (this.content == null || !n.content.equals(this.content)) return false;
        } else if (n.parent != null) {
            // Cannot check if parents are equal, will cause infinite loop.
            if (this.parent == null) return false;
        }

        if (n.whiteSpaceAfter != whiteSpaceAfter) {
            return false;
        }
        
        for (Entry<String, String> e : attributes.entrySet()) {
            if (!n.attributes.containsKey(e.getKey())) {
                return false;
            }  else if (e.getValue() == null) {
                if (n.attributes.get(e.getKey()) != null) return false;
            } else {
                if (!e.getValue().equals(n.attributes.get(e.getKey()))) return false;
            }
        }
        
        if (this.children.size() != n.children.size()) return false;
        
        for (DOMNode c : n.children) {
            int index = n.children.indexOf(c);
            if (this.children.size() < index) return false; 
            if (!this.children.get(n.children.indexOf(c)).equals((c))) return false;
        }
        return true;
    }

}
