package browser.layout;

import static browser.css.CSSStyle.DisplayType;

import java.util.*;

import browser.model.BoxNode;
import browser.model.RenderNode;
import browser.parser.HTMLElements;

public class BoxTreeGenerator {

    private final Map<Integer, BoxNode> renderNodeIdToBoxNode = new HashMap<>();
    private BoxNode rootBoxNode;

    // Public methods

    public BoxNode generate(RenderNode rootRenderNode) {
        renderNodeIdToBoxNode.clear();

        List<RenderNode> renderNodeList = new ArrayList<>();
        List<BoxNode> parentBoxNodeList = new ArrayList<>();
        renderNodeList.add(rootRenderNode);
        parentBoxNodeList.add(null);

        while (!renderNodeList.isEmpty()) {
            RenderNode renderNode = renderNodeList.remove(0);
            BoxNode parentBoxNode = parentBoxNodeList.remove(0);
            BoxNode boxNode = createBoxNode(renderNode, parentBoxNode);
            renderNodeIdToBoxNode.put(renderNode.id, boxNode);

            if (parentBoxNode == null) {
                rootBoxNode = boxNode;
            }

            for (RenderNode childRenderNode : renderNode.children) {
                renderNodeList.add(childRenderNode);
                parentBoxNodeList.add(boxNode);
            }
        }

        addAnonymousBoxes(rootBoxNode);

        return rootBoxNode;
    }

    public BoxNode getBoxNodeForRenderNodeId(int id) {
        return renderNodeIdToBoxNode.get(id);
    }

    // Private methods

    private BoxNode createBoxNode(RenderNode renderNode, BoxNode parentBoxNode) {
        BoxNode boxNode = new BoxNode();
        boxNode.id = BoxNode.nextId++;
        boxNode.renderNodeId = renderNode.id;
        renderNode.boxNode = boxNode;
        boxNode.correspondingRenderNode = renderNode;
        boxNode.style = renderNode.style.deepCopy();
        boxNode.parent = parentBoxNode;
        if (parentBoxNode != null) {
            parentBoxNode.children.add(boxNode);
        }
        boxNode.outerDisplayType = renderNode.style.outerDisplay;
        boxNode.innerDisplayType = renderNode.style.innerDisplay;
        boxNode.auxiliaryDisplayType = renderNode.style.auxiliaryDisplay;
        boxNode.isTextNode = renderNode.type.equals(HTMLElements.TEXT);
        boxNode.isPseudo = HTMLElements.isPseudoElement(renderNode.type);

        if (boxNode.isTextNode) {
            boxNode.isAnonymous = true;
            boxNode.textStartIndex = 0;
            boxNode.textEndIndex = boxNode.correspondingRenderNode.text.length();
        }

        return boxNode;
    }

    private void addAnonymousBoxes(BoxNode rootBoxNode) {
        if (rootBoxNode.children.size() == 0) {
            return;
        }

        List<BoxNode> queue = new ArrayList<>();
        queue.add(rootBoxNode);

        while (!queue.isEmpty()) {
            BoxNode boxNode = queue.remove(0);
            boolean validDisplayConfiguration = boxHasValidDisplayConfiguration(boxNode);
            if (!validDisplayConfiguration) {
                addAnonymousBlockBoxes(boxNode);

                // Adding anonymous boxes may have invalidated the box consistency in the parent box. Reset the queue
                // so that the parent is reprocessed.
                if (boxNode.parent != null) {
                    List<BoxNode> newQueueContent = queue.stream()
                            .filter(b -> b.isDescendantOf(boxNode.parent.id)).toList();
                    queue.clear();
                    queue.add(0, boxNode.parent);
                    queue.addAll(newQueueContent);
                }
            }
            queue.addAll(boxNode.children);
        }
    }

    private void addAnonymousBlockBoxes(BoxNode boxNode) {
        BoxNode baseBoxNode = boxNode;

        // If the root box is inline, it needs to be wrapped in an anonymous block box.
        if (boxNode.outerDisplayType.equals(DisplayType.INLINE)) {
            baseBoxNode = wrapInlineElementWithAnonymousBlockBox(boxNode);
        }

        List<BoxNode> currentInlineBoxes = new ArrayList<>();
        List<BoxNode> newChildren = new ArrayList<>();

        for (int i = 0; i < baseBoxNode.children.size(); i++) {
            BoxNode childNode = baseBoxNode.children.get(i);
            if (childNode.outerDisplayType.equals(DisplayType.BLOCK) || i == baseBoxNode.children.size() - 1) {

                // On the last iteration, add an inline child node to the list, so it can be wrapped in an anonymous block.
                if (i == baseBoxNode.children.size() - 1 && childNode.outerDisplayType.equals(DisplayType.INLINE)) {
                    currentInlineBoxes.add(childNode);
                }

                if (currentInlineBoxes.size() > 0) {
                    BoxNode anonymousBox = new BoxNode();
                    anonymousBox.id = BoxNode.nextId++;
                    anonymousBox.outerDisplayType = DisplayType.BLOCK;
                    anonymousBox.innerDisplayType = DisplayType.FLOW;
                    anonymousBox.isAnonymous = true;

                    for (BoxNode inlineBox : currentInlineBoxes) {
                        inlineBox.parent = anonymousBox;
                        anonymousBox.children.add(inlineBox);
                    }
                    currentInlineBoxes.clear();
                    newChildren.add(anonymousBox);
                }

                if (childNode.outerDisplayType.equals(DisplayType.BLOCK)) {
                    newChildren.add(childNode);
                }
            } else if (childNode.outerDisplayType.equals(DisplayType.INLINE)) {
                currentInlineBoxes.add(childNode);
            } else {
                // TODO: what if its neither inline nor block?
                newChildren.add(childNode);
            }
        }

        for (BoxNode child : newChildren) {
            child.parent = baseBoxNode;
        }
        baseBoxNode.children = newChildren;
    }

    private BoxNode wrapInlineElementWithAnonymousBlockBox(BoxNode inlineBox) {
        BoxNode containingAnonymousBox = new BoxNode();
        containingAnonymousBox.id = BoxNode.nextId++;
        containingAnonymousBox.outerDisplayType = DisplayType.BLOCK;
        containingAnonymousBox.innerDisplayType = DisplayType.FLOW;
        containingAnonymousBox.parent = inlineBox.parent;
        containingAnonymousBox.isAnonymous = true;

        // Add the anonymous containing box to the parent's children, and remove the inline box.
        BoxNode parent = inlineBox.parent;
        if (parent == null) {
            // If the parent is null, then the base box node is the root box node. Since the parent of this node
            // is being changed, the root itself is updated to the containing anonymous box, since this now
            // contains the previous root box node.
            rootBoxNode = containingAnonymousBox;
        } else {
            int indexInParent = parent.children.indexOf(inlineBox);
            parent.children.add(indexInParent, containingAnonymousBox);
            parent.children.remove(inlineBox);
        }

        // Add the inline box's children to the new containing box, cutting the inline box out of the box tree.
        containingAnonymousBox.children.addAll(inlineBox.children);
        for (BoxNode child : inlineBox.children) {
            child.parent = containingAnonymousBox;
        }

        return containingAnonymousBox;
    }

    private boolean boxHasValidDisplayConfiguration(BoxNode boxNode) {
        List<BoxNode> blockChildren = boxNode.children.stream().filter(node -> !node.isTextNode).filter(node -> node.outerDisplayType.equals(DisplayType.BLOCK)).toList();
        List<BoxNode> inlineChildren = boxNode.children.stream().filter(node -> !node.isTextNode).filter(node -> node.outerDisplayType.equals(DisplayType.INLINE)).toList();
        List<BoxNode> textChildren = boxNode.children.stream().filter(node -> node.isTextNode).toList();

        if (boxNode.outerDisplayType.equals(DisplayType.BLOCK)) {
            if (blockChildren.size() > 0 && (inlineChildren.size() > 0 || textChildren.size() > 0)) {
                // The block level element contains both inline (or text) and block elements.
                return false;
            }
        } else if (boxNode.outerDisplayType.equals(DisplayType.INLINE)) {
            if (blockChildren.size() > 0) {
                // The inline level element contains block level elements.
                return false;
            }
        }

        return true;
    }

}
