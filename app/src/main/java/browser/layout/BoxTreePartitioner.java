package browser.layout;

import java.util.ArrayList;
import java.util.List;

import browser.model.BoxNode;

public class BoxTreePartitioner {

    /**
     * Inline boxes should not span multiple rows in the inline context. This method duplicates boxes such that each
     * child of the root box of the inline context is isolated to its row.
     * @param boxNode       The leaf box node to put on a separate path.
     */
    public void partition(BoxNode boxNode, InlineFormattingContext context) {
        if (boxNode.parent == null || boxNode.parent.id == context.contextRootId) {
            return;
        }

        BoxNode currentBox = boxNode;

        while (currentBox.parent.id != context.contextRootId) {
            int indexInParent = currentBox.parent.children.indexOf(currentBox);
            boolean previousSiblings = indexInParent > 0;
            if (previousSiblings && currentBox.parent.id != context.contextRootId) {
                // If there are sibling boxes before the current box, separate them from the boxes that will be split.
                List<List<BoxNode>> partitions = partitionChildren(currentBox.parent, indexInParent);
                currentBox.parent.children = partitions.get(0);

                int parentIndex = currentBox.parent.parent.children.indexOf(currentBox.parent);
                BoxNode newBoxNode = new BoxNode(currentBox.parent);
                newBoxNode.children = partitions.get(1);
                for (BoxNode childBoxNode : newBoxNode.children) {
                    childBoxNode.parent = newBoxNode;
                }
                newBoxNode.parent = currentBox.parent.parent;
                newBoxNode.x = null;
                newBoxNode.y = null;
                newBoxNode.width = null;
                newBoxNode.height = null;
                currentBox.parent.parent.children.add(parentIndex + 1, newBoxNode);

                currentBox = newBoxNode;
            } else {
                currentBox = currentBox.parent;
            }
        }
    }

    private List<List<BoxNode>> partitionChildren(BoxNode parent, int partitionIndex) {
        List<BoxNode> leftBoxNodes = new ArrayList<>();
        List<BoxNode> rightBoxNodes = new ArrayList<>();
        for (int i = 0; i < parent.children.size(); i++) {
            if (i < partitionIndex) {
                leftBoxNodes.add(parent.children.get(i));
            } else {
                rightBoxNodes.add(parent.children.get(i));
            }
        }
        return List.of(leftBoxNodes, rightBoxNodes);
    }

}
