package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import layout.BoxLayoutCalculator;
import layout.TextDimensionCalculator;
import model.DOMNode;
import model.RenderNode;
import model.Vector2;

public class RenderTreeGenerator {
	// has to be changed for testing, making it private later?
	public int nodeID = 0;
	private Map<Integer, RenderNode> parentRenderNodeMap = new HashMap<Integer, RenderNode>();

	public RenderNode generateRenderTree(DOMNode dom, Float screenWidth) {
		RenderNode renderTree = domTreeToRenderTree(dom);
		return renderTree;
    }
	
	public DOMNode getBodyNode(DOMNode dom) {
        if (dom.type.equals(HTMLElements.BODY)) return dom;
        DOMNode bodyCandidate = null;
        for (DOMNode child : dom.children) {
            DOMNode d = getBodyNode(child);
            if (d != null) bodyCandidate = d;
        }
        return bodyCandidate;
    }
	
	public RenderNode domTreeToRenderTree(DOMNode dom) {
		DOMNode body = getBodyNode(dom);
        if (body == null) {
            System.out.println("RenderTreeGenerator: no body element found");
            return null;
        }
        nodeID = 0;
        return copyTree(body, null, 0);
	}
	
	private RenderNode copyTree(DOMNode dom, RenderNode parent, int depth) {
		RenderNode renderNode = new RenderNode(dom, nodeID, depth);
		renderNode.attributes = dom.attributes;
//		if (parentID != null) parentRenderNodeMap.put(parentID, renderNode);
		if (parent != null) parentRenderNodeMap.put(nodeID, parent);
		nodeID++;
		for (DOMNode child : dom.children) {
			renderNode.children.add(copyTree(child, renderNode, depth + 1));
		}
		return renderNode;
	}
	
//	private RenderNode copyTree(DOMNode dom, Integer parentID, int depth) {
//		RenderNode renderNode = new RenderNode(dom, nodeID, depth);
//		renderNode.attributes = dom.attributes;
////		if (parentID != null) parentRenderNodeMap.put(parentID, renderNode);
//		if (parentID != null) parentRenderNodeMap.put(parentID, renderNode);
//		nodeID++;
//		for (DOMNode child : dom.children) {
//			renderNode.children.add(copyTree(child, nodeID - 1, depth + 1));
//		}
//		return renderNode;
//	}
	
	public void splitLongText(RenderNode root, Map<Integer, RenderNode> parentRenderNodeMap) {
		this.parentRenderNodeMap = parentRenderNodeMap;
		splitLongText(root);
	}
	
	/**
	 * Splits lines that go over their max width. Requires the parent map to be populated.
	 * @param root
	 */
	public void splitLongText(RenderNode root) {
		System.out.printf("splitLongText: root = %s\n", root.type);
		if (root.text != null && root.box.width > root.maxWidth) {
			System.out.printf("splitLongText: %s is too long\n", root.type);

			List<String> lines = TextDimensionCalculator.splitToWidth(root.text, root.style, root.maxWidth);
			System.out.printf("Splitting text into %d lines\n", lines.size());
			// Replace this node with new nodes, each containing one line of the text
			RenderNode parent = parentRenderNodeMap.get(root.id);
			List<RenderNode> newChildren = new ArrayList<RenderNode>();
			
			if (parent != null) {
				for (RenderNode child : parent.children) {
					if (child.id != root.id) {
						newChildren.add(child);
					} else {
						for (String line : lines) {
							RenderNode newNode = new RenderNode(root.type);
							newNode.style = root.style;
							newNode.id = ++nodeID;
							parentRenderNodeMap.put(newNode.id, parent);
							newNode.depth = root.depth;
							newNode.attributes = root.attributes;
							newNode.text = line;
							newNode.maxWidth = root.maxWidth;
							newNode.maxHeight = root.maxHeight;
							Vector2 size = TextDimensionCalculator.getTextDimension(line, root.style);
							newNode.box.fixedWidth = true;
							newNode.box.width = size.x;
							newNode.box.fixedHeight = true;
							newNode.box.height = size.y;
							newChildren.add(newNode);
						}
					}
				}
				parent.children = newChildren;
			}
		}
		
		for (RenderNode child : root.children) {
			splitLongText(child);
		}
	}
	
	
	public void cleanUpText(RenderNode root) {
		if (root.text != null) {
			root.text = root.text.replaceAll("[\n\r]", " ");
			root.text = root.text.replaceAll("\\s+", " ");
		}
		for (RenderNode child : root.children) {
			cleanUpText(child);
		}
	}
	
	/**
	 * Some elements need to be transformed into what actually gets rendered: for instance, 
	 * multiple lines need to get broken up, and list elements need to be assigned numbers.
	 * @param root
	 */
	public void transformNode(RenderNode root) {
		
		switch (root.type) {
		case "ol":
			transformOL(root);
			break;
		case "ul":
			transformUL(root);
			break;
		}
		
		for (RenderNode child : root.children) {
			transformNode(child);
		}
	}
	
	// Methods for transforming specific elements
	
	private void transformUL(RenderNode root) {
		for (RenderNode item : root.children) {
			if (item.type.equals("li")) {
				RenderNode text = item.children.get(0);
				text.text = String.format("\t• %s", text.text);
			}
			
		}
	}
	
	private void transformOL(RenderNode root) {
		for (int i = 0; i < root.children.size(); i++) {
			RenderNode item = root.children.get(i);
			item.text = String.format("\t%d. %s", i + 1, item.text);
		}
	}
	
	public Map<Integer, RenderNode> getParentRenderNodeMap() {
		return this.parentRenderNodeMap;
	}
	
	
	
}
