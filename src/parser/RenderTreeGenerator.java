package parser;

import java.util.HashMap;
import java.util.Map;

import layout.BoxLayoutCalculator;
import model.DOMNode;
import model.RenderNode;

public class RenderTreeGenerator {
	
	private int nodeID = 0;
	private Map<Integer, RenderNode> parentRenderNodeMap = new HashMap<Integer, RenderNode>();

	public RenderNode generateRenderTree(DOMNode dom, Float screenWidth) {
		RenderNode renderTree = domTreeToRenderTree(dom);
		transformNode(renderTree);
		BoxLayoutCalculator boxLayoutCalculator = new BoxLayoutCalculator(parentRenderNodeMap, screenWidth);
		boxLayoutCalculator.setBoxBounds(renderTree);
		boxLayoutCalculator.propagateMaxSizes(renderTree);
		boxLayoutCalculator.calculateBoxes(renderTree);
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
	
	private RenderNode copyTree(DOMNode dom, Integer parentID, int depth) {
		RenderNode renderNode = new RenderNode(dom, nodeID, depth);
		renderNode.attributes = dom.attributes;
		if (parentID != null) parentRenderNodeMap.put(parentID, renderNode);
		nodeID++;
		for (DOMNode child : dom.children) {
			renderNode.children.add(copyTree(child, nodeID - 1, depth + 1));
		}
		return renderNode;
	}
	
	public void splitLongText(RenderNode root) {
		
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
			item.text = String.format("\t• %s", item.text);
		}
	}
	
	private void transformOL(RenderNode root) {
		for (int i = 0; i < root.children.size(); i++) {
			RenderNode item = root.children.get(i);
			item.text = String.format("\t%d. %s", i + 1, item.text);
		}
	}
	
	
	
}
