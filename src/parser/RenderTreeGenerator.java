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
		if (parentID != null) parentRenderNodeMap.put(parentID, renderNode);
		nodeID++;
		for (DOMNode child : dom.children) {
			renderNode.children.add(copyTree(child, nodeID - 1, depth + 1));
		}
		return renderNode;
	}
	
}
