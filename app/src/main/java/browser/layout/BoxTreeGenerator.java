package browser.layout;

import static browser.css.CSSStyle.DisplayType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import browser.model.BoxNode;
import browser.model.RenderNode;
import browser.parser.HTMLElements;

public class BoxTreeGenerator {

    private final Map<Integer, BoxNode> renderNodeIdToBoxNode = new HashMap<>();
    private final Map<Integer, Integer> boxNodeIdToRenderNodeId = new HashMap<>();
    private BoxNode rootBoxNode;
    private int nextBoxNodeId = 0;

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
            boxNodeIdToRenderNodeId.put(boxNode.id, renderNode.id);

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
        boxNode.id = nextBoxNodeId++;
        boxNode.renderNodeId = renderNode.id;
        boxNode.parent = parentBoxNode;
        if (parentBoxNode != null) {
            parentBoxNode.children.add(boxNode);
        }
        boxNode.outerDisplayType = renderNode.style.outerDisplay;
        boxNode.innerDisplayType = renderNode.style.innerDisplay;
        boxNode.isTextNode = renderNode.type.equals(HTMLElements.TEXT);

        if (boxNode.isTextNode) {
            boxNode.isAnonymous = true;
        }

        return boxNode;
    }

    private void addAnonymousBoxes(BoxNode boxNode) {
        if (boxNode.children.size() == 0) {
            return;
        }

        List<BoxNode> blockChildren = boxNode.children.stream().filter(node -> !node.isTextNode).filter(node -> node.outerDisplayType.equals(DisplayType.BLOCK)).toList();
        List<BoxNode> inlineChildren = boxNode.children.stream().filter(node -> !node.isTextNode).filter(node -> node.outerDisplayType.equals(DisplayType.INLINE)).toList();
        List<BoxNode> textChildren = boxNode.children.stream().filter(node -> node.isTextNode).toList();

        if (boxNode.outerDisplayType.equals(DisplayType.BLOCK)) {
            if (blockChildren.size() > 0 && (inlineChildren.size() > 0 || textChildren.size() > 0)) {
                // The block level element contains both inline (or text) and block elements.
                addAnonymousBlockBoxes(boxNode);
            }
        } else if (boxNode.outerDisplayType.equals(DisplayType.INLINE)) {
            if (blockChildren.size() > 0) {
                // The inline level element contains block level elements.
                addAnonymousBlockBoxes(boxNode);
            }
        }

        for (BoxNode childBoxNode : boxNode.children) {
            addAnonymousBoxes(childBoxNode);
        }
    }

    private void addAnonymousBlockBoxes(BoxNode boxNode) {
        System.out.printf("\naddAnonymousBlockBoxes, box node %d\n", boxNode.id);
        BoxNode baseBoxNode = boxNode;

        // If the root box is inline, it needs to be wrapped in an anonymous block box.
        if (boxNode.outerDisplayType.equals(DisplayType.INLINE)) {
            BoxNode containingAnonymousBox;

            containingAnonymousBox = new BoxNode();
            containingAnonymousBox.id = nextBoxNodeId++;
            containingAnonymousBox.outerDisplayType = DisplayType.BLOCK;
            containingAnonymousBox.innerDisplayType = DisplayType.FLOW;
            containingAnonymousBox.parent = boxNode.parent;
            containingAnonymousBox.isAnonymous = true;
            System.out.printf("Adding anon box since root is inline. id=%d\n", containingAnonymousBox.id);

            // Add the anonymous containing box to the parent's children, and remove the inline box.
            BoxNode parent = boxNode.parent;
            if (parent == null) {
                // If the parent is null, then the base box node is the root box node. Since the parent of this node
                // is being changed, the root itself is updated to the containing anonymous box, since this now
                // contains the previous root box node.
                rootBoxNode = containingAnonymousBox;
            } else {
                int indexInParent = parent.children.indexOf(boxNode);
                parent.children.add(indexInParent, containingAnonymousBox);
                parent.children.remove(boxNode);
            }

            // Add the inline box's children to the new containing box, cutting the inline box out of the box tree.
            containingAnonymousBox.children.addAll(boxNode.children);
            for (BoxNode child : boxNode.children) {
                child.parent = containingAnonymousBox;
            }

            baseBoxNode = containingAnonymousBox;
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
                    anonymousBox.id = nextBoxNodeId++;
                    anonymousBox.outerDisplayType = DisplayType.BLOCK;
                    anonymousBox.innerDisplayType = DisplayType.FLOW;
                    anonymousBox.isAnonymous = true;
                    System.out.printf("Adding anon box to hold inline boxes. id=%d\n", anonymousBox.id);

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
                // TODO what if its neither inline no block?
                newChildren.add(childNode);
            }
        }

        for (BoxNode child : newChildren) {
            child.parent = baseBoxNode;
        }
        baseBoxNode.children = newChildren;
    }

}
