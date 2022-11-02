package browser.parser;

import java.util.HashMap;
import java.util.Map;

import browser.app.ErrorPageHandler;
import browser.model.DOMNode;
import browser.model.RenderNode;

public class RenderTreeGenerator {
    // has to be changed for testing, making it private later?
    public static int nodeID = 0;
    private Map<Integer, RenderNode> parentRenderNodeMap = new HashMap<Integer, RenderNode>();

    public RenderNode generateRenderTree(DOMNode dom, Float screenWidth) {
        RenderNode renderTree = domTreeToRenderTree(dom);
        return renderTree;
    }

    public static int getNextID() {
        return nodeID++;
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
            ErrorPageHandler.browserError = ErrorPageHandler.BrowserErrorType.NO_BODY;
            return null;
        }
        nodeID = 0;
        return copyTree(body, null, 0);
    }

    private RenderNode copyTree(DOMNode dom, RenderNode parent, int depth) {
        RenderNode renderNode = new RenderNode(dom, nodeID, depth);
        renderNode.attributes = dom.attributes;
//        if (parentID != null) parentRenderNodeMap.put(parentID, renderNode);
        if (parent != null) parentRenderNodeMap.put(nodeID, parent);
        nodeID++;
        for (DOMNode child : dom.children) {
            renderNode.children.add(copyTree(child, renderNode, depth + 1));
        }
        return renderNode;
    }

    /**
     * Remove all new lines, carriage returns, and extra spaces from text. The only place
     * that these should be left as is is inside a 'pre' tag.
     * @param root
     * @param inPre
     */
    public void cleanUpText(RenderNode root, boolean inPre) {
        if (root == null) return;
        if (root.text != null && !inPre) {
            root.text = root.text.replaceAll("[\n\r]", " ");
            root.text = root.text.replaceAll("\\s+", " ");
//            while (root.text.startsWith(" ") && root.text.length() > 1) root.text = root.text.substring(1);
        }

        if (root.text != null) {
            root.text = SpecialSymbolHandler.insertSymbols(root.text);
        }

        for (RenderNode child : root.children) {
            cleanUpText(child, inPre || root.type.equals("pre"));
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
                text.text = String.format("\t� %s", text.text);
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
