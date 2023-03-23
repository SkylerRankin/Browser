package browser.parser;

import java.util.HashMap;
import java.util.Map;

import browser.constants.HTMLConstants;
import browser.css.CSSStyle;
import browser.model.DOMNode;
import browser.model.RenderNode;

public class RenderTreeGenerator {
    private final Map<Integer, RenderNode> parentRenderNodeMap = new HashMap<>();

    // Public methods

    public RenderNode generateRenderTree(DOMNode dom) {
        return domTreeToRenderTree(dom);
    }

    public void cleanupRenderNodeText(RenderNode renderNode) {
        if (renderNode != null) {
            removeDuplicateWhitespace(renderNode, false);
            trimTextWhitespace(renderNode);
            removeDuplicateWhitespaceAfterSameNode(renderNode);
        }
    }

    public DOMNode getBodyNode(DOMNode dom) {
        if (dom.type.equals(HTMLElements.BODY)) {
            return dom;
        }

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
        return copyTree(body == null ? dom : body, null, 0);
    }

    private RenderNode copyTree(DOMNode dom, RenderNode parent, int depth) {
        RenderNode renderNode = new RenderNode(dom, RenderNode.nextId, depth);
        renderNode.attributes = dom.attributes;
        renderNode.parent = parent;
        if (parent != null) parentRenderNodeMap.put(RenderNode.nextId, parent);
        RenderNode.nextId++;
        for (DOMNode child : dom.children) {
            if (!HTMLConstants.elementsExcludedFromRender.contains(child.type)) {
                renderNode.children.add(copyTree(child, renderNode, depth + 1));
            }
        }
        renderNode.whiteSpaceAfter = dom.whiteSpaceAfter;
        return renderNode;
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
     * Sets the whiteSpaceAfter flag to false on all text nodes that have text that ends with a space.
     * @param root      The render node to process.
     */
    public void removeDuplicateWhitespaceAfterSameNode(RenderNode root) {
        if (root.type.equals(HTMLElements.TEXT) && root.text != null && root.text.endsWith(" ")) {
            // Text ends with space, so can remove white space after flag.
            removeWhiteSpaceAfterFlag(root);
        }

        for (RenderNode child : root.children) {
            removeDuplicateWhitespaceAfterSameNode(child);
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
            // Trim the start of the text.
            RenderNode previousTextNode = getPreviousInlineTextNode(node);
            if (previousTextNode == null || previousTextNode.text.endsWith(" ") || previousSiblingHasWhiteSpaceAfter(node)) {
                node.text = node.text.stripLeading();
            }

            // Trim the end of the text.
            RenderNode nextTextNode = getNextInlineTextNode(node);
            if ((nextTextNode == null || nextTextNode.text == null)) {
                node.text = node.text.stripTrailing();
                removeWhiteSpaceAfterFlag(node);
            }
        }

        for (RenderNode child : node.children) {
            trimTextWhitespace(child);
        }
    }

    /**
     * Want to find the next inline text that will be rendered for this inline formatting context.
     * @param node
     * @return
     */
    private RenderNode getNextInlineTextNode(RenderNode node) {
        Map<Integer, Integer> lastVisitedChildIndex = new HashMap<>();
        boolean reachedNonInlineNode = false;
        // Start at the parent
        RenderNode current = node.parent;
        lastVisitedChildIndex.put(current.id, node.parent.children.indexOf(node));

        while (!current.type.equals(HTMLElements.TEXT)) {
            if (current.style.outerDisplay != CSSStyle.DisplayType.INLINE) {
                if (reachedNonInlineNode) {
                    return null;
                } else {
                    reachedNonInlineNode = true;
                }
            }

            if (!lastVisitedChildIndex.containsKey(current.id)) {
                // New node, go to its first child if present, otherwise move to parent.
                if (current.children.size() > 0) {
                    lastVisitedChildIndex.put(current.id, 0);
                    current = current.children.get(0);
                } else {
                    lastVisitedChildIndex.put(current.parent.id, current.parent.children.indexOf(current));
                    current = current.parent;
                }
            } else if (lastVisitedChildIndex.get(current.id) < current.children.size() - 1) {
                // Follow the next available child path.
                current = current.children.get(lastVisitedChildIndex.get(current.id) + 1);
            } else if (current.parent != null) {
                // Move to the parent.
                lastVisitedChildIndex.put(current.parent.id, current.parent.children.indexOf(current));
                current = current.parent;
            } else {
                // No available children and no parent.
                return null;
            }
        }

        return current;
    }

    private RenderNode getPreviousInlineTextNode(RenderNode node) {
        Map<Integer, Integer> lastVisitedChildIndex = new HashMap<>();
        boolean reachedNonInlineNode = false;

        // Start at the parent
        RenderNode current = node.parent;
        lastVisitedChildIndex.put(current.id, node.parent.children.indexOf(node));

        while (!current.type.equals(HTMLElements.TEXT)) {
            if (current.style.outerDisplay != CSSStyle.DisplayType.INLINE) {
                if (reachedNonInlineNode) {
                    return null;
                } else {
                    reachedNonInlineNode = true;
                }
            }

            if (!lastVisitedChildIndex.containsKey(current.id)) {
                // New node, go to its last child if present, otherwise move to parent.
                if (current.children.size() > 0) {
                    lastVisitedChildIndex.put(current.id, current.children.size() - 1);
                    current = current.children.get(current.children.size() - 1);
                } else {
                    lastVisitedChildIndex.put(current.parent.id, current.parent.children.indexOf(current));
                    current = current.parent;
                }
            } else if (lastVisitedChildIndex.get(current.id) > 0) {
                // Follow the next available child path.
                current = current.children.get(lastVisitedChildIndex.get(current.id) - 1);
            } else if (current.parent != null) {
                // Move to the parent.
                lastVisitedChildIndex.put(current.parent.id, current.parent.children.indexOf(current));
                current = current.parent;
            } else {
                // No available children and no parent.
                return null;
            }
        }

        return current;
    }

    private boolean previousSiblingHasWhiteSpaceAfter(RenderNode node) {
        if (node.parent == null) {
            return false;
        }

        int indexInParent = node.parent.children.indexOf(node);
        if (indexInParent > 0) {
            return node.parent.children.get(indexInParent - 1).whiteSpaceAfter;
        } else {
            return false;
        }
    }

    private void removeWhiteSpaceAfterFlag(RenderNode node) {
        node.whiteSpaceAfter = false;
        RenderNode currentParent = node.parent;
        RenderNode currentChild = node;
        while (currentParent != null && currentParent.children.indexOf(currentChild) == currentParent.children.size() - 1) {
            currentParent.whiteSpaceAfter = false;
            currentChild = currentParent;
            currentParent = currentParent.parent;
        }
    }

}
