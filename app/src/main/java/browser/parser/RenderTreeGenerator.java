package browser.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import browser.app.ErrorPageHandler;
import browser.constants.PseudoElementConstants;
import browser.css.CSSStyle;
import browser.model.DOMNode;
import browser.model.RenderNode;

public class RenderTreeGenerator {
    private static int nodeID = 0;
    private final Map<Integer, RenderNode> parentRenderNodeMap = new HashMap<Integer, RenderNode>();

    public RenderNode generateRenderTree(DOMNode dom, Float screenWidth) {
        return domTreeToRenderTree(dom);
    }

    public static int getNextID() {
        return nodeID++;
    }

    public static void setNextID(int id) {
        nodeID = id;
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
        renderNode.parent = parent;
//        if (parentID != null) parentRenderNodeMap.put(parentID, renderNode);
        if (parent != null) parentRenderNodeMap.put(nodeID, parent);
        nodeID++;
        for (DOMNode child : dom.children) {
            renderNode.children.add(copyTree(child, renderNode, depth + 1));
        }
        return renderNode;
    }

    public void cleanupRenderNodeText(RenderNode renderNode) {
        removeDuplicateWhitespace(renderNode, false);
        trimTextWhitespace(renderNode);
    }

    /**
     * Remove all new lines, carriage returns, and extra spaces from text. The only place
     * that these should be left as-is is inside a 'pre' tag.
     * @param root  The render node to start on.
     * @param inPre True if the node is within a pre-formatted block.
     */
    public void removeDuplicateWhitespace(RenderNode root, boolean inPre) {
        if (root == null) return;
        if (root.text != null && !inPre) {
            root.text = root.text.replaceAll("[\n\r]", " ");
            root.text = root.text.replaceAll("\\s+", " ");
        }

        if (root.text != null) {
            root.text = SpecialSymbolHandler.insertSymbols(root.text);
        }

        for (RenderNode child : root.children) {
            removeDuplicateWhitespace(child, inPre || root.type.equals("pre"));
        }
    }

    /**
     * Whitespace around text should be removed, but whitespace within text or around tags that are themselves within
     * text (such as "some <b>bold</b> text") should be retained. The following steps decide if whitespace should
     * be trimmed or not.
     *
     * Should trim start of text?
     *      - If this the first child of the parent? If so, trim.
     *      - Does the previous child have text that ends with a space? If so, trim.
     *      - Otherwise, do not trim.
     *
     * Should trim end of text?
     *      - If this the last child of the parent? If so, trim.
     *      - Does the next child not have text? If so, trim.
     *      - Otherwise, do not trim.
     *
     * @param node  The node to start on.
     */
    public void trimTextWhitespace(RenderNode node) {
        if (node == null ) return;

        if (node.parent != null && node.text != null) {
            int indexInParent = node.parent.children.indexOf(node);

            // Trim the start of the text.
            boolean isFirstChild = indexInParent == 0;
            RenderNode previousTextNode = getPreviousInlineTextNode(node);
            if (isFirstChild || (previousTextNode != null && previousTextNode.text.endsWith(" "))) {
                node.text = node.text.stripLeading();
            }

            // Trim the end of the text.
            boolean isLastChild = indexInParent == node.parent.children.size() - 1;
            RenderNode nextTextNode = getNextInlineTextNode(node);
            if (isLastChild || (nextTextNode == null || nextTextNode.text == null)) {
                node.text = node.text.stripTrailing();
            }
        }

        for (RenderNode child : node.children) {
            trimTextWhitespace(child);
        }
    }

    public RenderNode getNextInlineTextNode(RenderNode node) {
        return getRelativeInlineTextNode(node, 1);
    }

    public RenderNode getPreviousInlineTextNode(RenderNode node) {
        return getRelativeInlineTextNode(node, -1);
    }

    private RenderNode getRelativeInlineTextNode(RenderNode node, int direction) {
        int indexInParent = node.parent.children.indexOf(node);

        // If node is the last child of the parent, there can be no next node.
        if (direction == 1 && indexInParent == node.parent.children.size() - 1) {
            return null;
        } else if (direction == -1 && indexInParent == 0) {
            return null;
        }

        RenderNode current = node.parent.children.get(indexInParent + direction);
        while (!current.type.equals(HTMLElements.TEXT)) {
            if (current.children.isEmpty()  || current.style.display == CSSStyle.displayType.BLOCK) {
                break;
            }
            current = current.children.get(0);
        }

        if (current.type.equals(HTMLElements.TEXT)) {
            return current;
        } else {
            return null;
        }
    }

    /**
     * Some elements need to be transformed into what actually gets rendered: for instance,
     * multiple lines need to get broken up, and list elements need to be assigned numbers.
     * @param root
     */
    public void transformNode(RenderNode root) {
        for (int i = 0; i < root.children.size(); i++) {
            RenderNode child = root.children.get(i);
            switch (child.type) {
                case HTMLElements.OL:
                case HTMLElements.UL:
                    transformList(child);
                    break;
            }
        }

        for (RenderNode child : root.children) {
            transformNode(child);
        }
    }

    /**
     * HTML lists require the addition of pseudo-elements to represent the
     * bullet points or numbers. Each <li></li> will have a corresponding
     * pseudo-element added before it, and the <li></li> style will be set
     * to display = inline.
     * @param root The <ul> or <ol> RenderNode to transform.
     */
    private void transformList(RenderNode root) {
        List<RenderNode> newChildren = new ArrayList<>();
        for (int i = 0; i < root.children.size(); i++) {
            RenderNode child = root.children.get(i);
            if (child.type.equals(HTMLElements.LI)) {
                // Create pseudo element for the list marker.
                RenderNode marker = new RenderNode(HTMLElements.PSEUDO_MARKER);
                marker.depth = child.depth;
                marker.id = getNextID();
                marker.parent = child.parent;
                marker.attributes.put(PseudoElementConstants.MARKER_TYPE_KEY, root.type);
                marker.attributes.put(PseudoElementConstants.MARKER_INDEX_KEY, String.valueOf(i));
                marker.style.display = PseudoElementConstants.MARKER_DISPLAY_TYPE;
                marker.style.width = PseudoElementConstants.MARKER_WIDTH;
                marker.style.height = PseudoElementConstants.MARKER_HEIGHT;

                // Update the root node to be inline-block. In needs to be inline with the marker element, but should
                // otherwise be a block element.
                child.style.display = CSSStyle.displayType.INLINE_BLOCK;

                // Save the parent reference for the new render node.
                parentRenderNodeMap.put(marker.id, marker.parent);

                newChildren.add(marker);
                newChildren.add(child);
            } else {
                newChildren.add(child);
            }
        }

        root.children.clear();
        root.children.addAll(newChildren);
    }

    public Map<Integer, RenderNode> getParentRenderNodeMap() {
        return this.parentRenderNodeMap;
    }

    public void reset() {
        nodeID = 0;
        parentRenderNodeMap.clear();
    }

}
