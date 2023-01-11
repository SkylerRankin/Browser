package browser.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import browser.app.ErrorPageHandler;
import browser.constants.PseudoElementConstants;
import browser.css.CSSStyle;
import browser.model.DOMNode;
import browser.model.RenderNode;

public class RenderTreeGenerator {
    private final Map<Integer, RenderNode> parentRenderNodeMap = new HashMap<Integer, RenderNode>();

    // Public methods

    public RenderNode generateRenderTree(DOMNode dom, Float screenWidth) {
        RenderNode root = domTreeToRenderTree(dom);

        if (root != null) {
            addListMarkers(root);
            cleanupRenderNodeText(root);
        }

        return root;
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

    public Map<Integer, RenderNode> getParentRenderNodeMap() {
        return this.parentRenderNodeMap;
    }

    public void reset() {
        parentRenderNodeMap.clear();
    }

    // Private methods

    private RenderNode domTreeToRenderTree(DOMNode dom) {
        DOMNode body = getBodyNode(dom);
        if (body == null) {
            ErrorPageHandler.browserError = ErrorPageHandler.BrowserErrorType.NO_BODY;
            return null;
        }
        return copyTree(body, null, 0);
    }

    private RenderNode copyTree(DOMNode dom, RenderNode parent, int depth) {
        RenderNode renderNode = new RenderNode(dom, RenderNode.nextId, depth);
        renderNode.attributes = dom.attributes;
        renderNode.parent = parent;
        if (parent != null) parentRenderNodeMap.put(RenderNode.nextId, parent);
        RenderNode.nextId++;
        for (DOMNode child : dom.children) {
            renderNode.children.add(copyTree(child, renderNode, depth + 1));
        }
        return renderNode;
    }

    private void cleanupRenderNodeText(RenderNode renderNode) {
        removeDuplicateWhitespace(renderNode, false);
        trimTextWhitespace(renderNode);
    }

    /**
     * Remove all new lines, carriage returns, and extra spaces from text. The only place
     * that these should be left as-is is inside a 'pre' tag.
     * @param root  The render node to start on.
     * @param inPre True if the node is within a pre-formatted block.
     */
    // TODO make this private
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
    // TODO make this private
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

    private RenderNode getNextInlineTextNode(RenderNode node) {
        return getRelativeInlineTextNode(node, 1);
    }

    private RenderNode getPreviousInlineTextNode(RenderNode node) {
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
            if (current.children.isEmpty()  || current.style.display == CSSStyle.DisplayType.BLOCK) {
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
     * Lists require the insertion of pseudo-elements to render the list bullet points, numbers, or other character.
     * Each list item will have a corresponding marker added after it.
     * @param renderNode        The render node to potentially add list markers to.
     */
    private void addListMarkers(RenderNode renderNode) {
        if (renderNode.style.auxiliaryDisplay != null && renderNode.style.auxiliaryDisplay.equals(CSSStyle.DisplayType.LIST_ITEM)) {
            RenderNode marker = new RenderNode(HTMLElements.PSEUDO_MARKER);
            if (renderNode.parent.type.equals(HTMLElements.OL)) {
                List<RenderNode> nonMarkerChildren = renderNode.parent.children.stream().filter(node -> !node.type.equals(HTMLElements.PSEUDO_MARKER)).toList();
                int indexInParent = nonMarkerChildren.indexOf(renderNode);
                marker.properties.put(PseudoElementConstants.MARKER_INDEX_KEY, indexInParent);
            }
            int indexInParent = renderNode.parent.children.indexOf(renderNode);
            renderNode.parent.children.add(indexInParent + 1, marker);
        }

        for (RenderNode child : renderNode.children) {
            addListMarkers(child);
        }
    }

}
