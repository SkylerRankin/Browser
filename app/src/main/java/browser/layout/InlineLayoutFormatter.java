package browser.layout;

import browser.css.CSSStyle;
import browser.model.BoxNode;
import browser.model.Vector2;
import browser.parser.HTMLElements;

public class InlineLayoutFormatter {

    private final TextDimensionCalculator textDimensionCalculator;
    private final TextNodeSplitter textNodeSplitter;
    private final BoxTreePartitioner boxTreePartitioner;

    public InlineLayoutFormatter(TextDimensionCalculator textDimensionCalculator) {
        this.textDimensionCalculator = textDimensionCalculator;
        this.textNodeSplitter = new TextNodeSplitter(textDimensionCalculator);
        this.boxTreePartitioner = new BoxTreePartitioner();
    }

    // Public methods

    public boolean placeBox(BoxNode boxNode, InlineFormattingContext context) {
        float x = getBoxXPosition(boxNode, context);
        Vector2 preferredSize = getInlineBoxPreferredSize(boxNode);
        float rightSideSpacing = context.getRightSpacingForBox(boxNode.id);
        float availableWidth = context.endX - x - rightSideSpacing;

        boolean fitsInCurrentLine = preferredSize.x <= availableWidth;
        boolean canBeSplitToFit = textNodeSplitter.canSplitNodeToFitWidth(boxNode, availableWidth);
        boolean placeOnCurrentLine = fitsInCurrentLine || (!canBeSplitToFit && !context.currentRowHasTerminalBox());

        // This box will be placed on a new line, so it is partitioned and will be laid out later in the tree.
        if (!fitsInCurrentLine && !canBeSplitToFit && context.currentRowHasTerminalBox()) {
            context.moveToNextLine();
            if (boxTreePartitioner.partitionAltersTree(boxNode, context)) {
                context.clearTentativeBoxes();
                boxTreePartitioner.partition(boxNode, context);
                return true;
            } else {
                x = getBoxXPosition(boxNode, context);
                availableWidth = context.endX - x - rightSideSpacing;
                fitsInCurrentLine = preferredSize.x <= availableWidth;
                canBeSplitToFit = textNodeSplitter.canSplitNodeToFitWidth(boxNode, availableWidth);
                placeOnCurrentLine = fitsInCurrentLine || (!canBeSplitToFit && !context.currentRowHasTerminalBox());
            }
        }

        if (placeOnCurrentLine) {
            // 1. The box fits with its preferred width, so it is added to the current line.
            // 2. The box does not fit and cannot be split to fit, but there are no previous terminal boxes, so the box
            //    has to be put on this line, causing an overflow.

            boxNode.x = x;
            boxNode.y = context.getCurrentLineYStart();
            boxNode.width = preferredSize.x;
            boxNode.height = preferredSize.y;
            context.addBoxToCurrentLine(boxNode);
        } else if (canBeSplitToFit) {
            // The box does not fit as is on the current line, but can be split to partially fit.

            BoxNode remainingBox = textNodeSplitter.fitNodeToWidth(boxNode, availableWidth);
            int indexInParent = boxNode.parent.children.indexOf(boxNode);
            boxNode.parent.children.add(indexInParent + 1, remainingBox);
            boxTreePartitioner.partition(remainingBox, context);

            // The newly generated box will take on whatever right side spacing the original box had.
            context.setRightSpacingForBox(remainingBox.id, context.getRightSpacingForBox(boxNode.id));
            context.setRightSpacingForBox(boxNode.id, 0);

            String substring = boxNode.correspondingRenderNode.text.substring(boxNode.textStartIndex, boxNode.textEndIndex);
            Vector2 newDimensions = textDimensionCalculator.getDimension(substring, boxNode.style);
            boxNode.x = x;
            boxNode.y = context.getCurrentLineYStart();
            boxNode.width = newDimensions.x;
            boxNode.height = newDimensions.y;

            context.addBoxToCurrentLine(boxNode);
            context.moveToNextLine();
        }

        return false;
    }

    /**
     * Returns the width of an inline box. This width does not include the box's own margins.
     * @param boxNode       The inline box.
     * @return      The box's width.
     */
    public float getWidth(BoxNode boxNode) {
        if (boxNode.children.size() == 0) {
            return boxNode.style.paddingLeft + boxNode.style.paddingRight;
        }

        BoxNode rightMostChild = null;
        for (BoxNode child : boxNode.children) {
            if (rightMostChild == null || child.x > rightMostChild.x) {
                rightMostChild = child;
            }
        }
        float maxX = rightMostChild.x + rightMostChild.width +
                rightMostChild.style.marginRight + boxNode.style.paddingRight;
        return maxX - boxNode.x;
    }

    // Private methods

    private float getBoxXPosition(BoxNode boxNode, InlineFormattingContext context) {
        BoxNode previousBoxInLine = context.getPreviousBoxInLine();
        if (previousBoxInLine == null) {
            return context.startX + context.getLeftSpacingForBox(boxNode.id);
        } else {
            if (previousBoxInLine.children.contains(boxNode)) {
                // The previous box is the parent of the current box.
                CSSStyle previousStyle = previousBoxInLine.style;
                return previousBoxInLine.x + previousStyle.paddingLeft + boxNode.style.marginLeft;
            } else {
                // The previous box is some sibling, potentially higher up in the tree, of the current box.
                BoxNode siblingBox = getAncestorSiblingInLine(boxNode, previousBoxInLine);
                if (siblingBox == null) {
                    System.err.println("InlineLayoutFormatter:getBoxXPosition: failed to find sibling box.");
                    System.err.printf("BoxNode: %s\n", boxNode);
                    System.err.printf("Previous BoxNode in line: %s\n", previousBoxInLine);
                    return 0;
                }
                CSSStyle previousStyle = siblingBox.style;
                return siblingBox.x + siblingBox.width + previousStyle.marginRight + boxNode.style.marginLeft;
            }
        }
    }

    private BoxNode getAncestorSiblingInLine(BoxNode boxNode, BoxNode previousBoxInLine) {
        BoxNode current = previousBoxInLine;
        while (current != null) {
            boolean sameParent = current.parent.id == boxNode.parent.id;
            boolean siblings = current.parent.children.indexOf(current) == boxNode.parent.children.indexOf(boxNode) - 1;
            if (current.parent != null && sameParent && siblings) {
                return current;
            }
            current = current.parent;
        }
        return null;
    }

    private Vector2 getInlineBoxPreferredSize(BoxNode boxNode) {
        Vector2 size = new Vector2(0, 0);
        if (boxNode.isTextNode) {
            String text = boxNode.correspondingRenderNode.text;
            if (boxNode.textEndIndex > 0) {
                text = text.substring(boxNode.textStartIndex, boxNode.textEndIndex);
            }
            size = textDimensionCalculator.getDimension(text, boxNode.style);
        } else if (boxNode.correspondingRenderNode.type.equals(HTMLElements.IMG)) {
            size = new Vector2(boxNode.width, boxNode.height);
        } else if (boxNode.innerDisplayType.equals(CSSStyle.DisplayType.FLOW_ROOT)) {
            size = new Vector2(boxNode.width, boxNode.height);
        }

        float widthDueToSpacing = boxNode.style.paddingLeft + boxNode.style.paddingRight;

        return new Vector2(size.x + widthDueToSpacing, size.y);
    }

}
