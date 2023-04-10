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
        BoxNode previousBoxInLine = context.getPreviousBoxInLine();

        boolean fitsInCurrentLine = preferredSize.x <= availableWidth;
        boolean canBeSplit = textNodeSplitter.canBeSplit(boxNode);
        boolean canBeSplitToFit = canBeSplit && textNodeSplitter.canSplitNodeToFitWidth(boxNode, availableWidth);
        boolean placeFullyOnCurrentLine = fitsInCurrentLine || (!canBeSplit && !context.currentRowHasTerminalBox());
        boolean placePartiallyOnCurrentLine = !placeFullyOnCurrentLine && (canBeSplitToFit || !context.currentRowHasTerminalBox());
        boolean lineBreak = previousBoxInLine != null && previousBoxInLine.correspondingRenderNode != null && previousBoxInLine.correspondingRenderNode.type.equals(HTMLElements.BR);
        boolean placeOnNextLine = (!placeFullyOnCurrentLine && !placePartiallyOnCurrentLine) || lineBreak;

        // The box is too large for the current line, but cannot be split to fit, and is the first terminal box in
        // the row. The layout moves to the next line in the context. In the case where the box is the first child in
        // the parent, partitioning on it will not result in any box tree changes, so the box is split and positioned
        // right away. If the partitioning does modify the tree, meaning the box has prior siblings that would remain,
        // the box is partitioned out fully.
        if (placeOnNextLine) {
            context.moveToNextLine();
            if (boxTreePartitioner.partitionAltersTree(boxNode, context)) {
                context.clearTentativeBoxes();
                boxTreePartitioner.partition(boxNode, context);
                return true;
            } else {
                x = getBoxXPosition(boxNode, context);
                availableWidth = context.endX - x - rightSideSpacing;
                fitsInCurrentLine = preferredSize.x <= availableWidth;
                placeFullyOnCurrentLine = fitsInCurrentLine || (!canBeSplit && !context.currentRowHasTerminalBox());
            }
        }

        if (placeFullyOnCurrentLine) {
            // 1. The box fits with its preferred width, so it is added to the current line.
            // 2. The box does not fit and cannot be split to fit, but there are no previous terminal boxes, so the box
            //    has to be put on this line, causing an overflow.
            placeBoxFullyOnCurrentLine(boxNode, context, x, preferredSize);
        } else {
            // The box does not fit as is on the current line, but can be split to partially fit.
            placeBoxPartiallyOnCurrentLine(boxNode, context, x, availableWidth);
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
            return boxNode.style.paddingLeft + boxNode.style.paddingRight + boxNode.style.borderWidthLeft + boxNode.style.borderWidthRight;
        }

        BoxNode rightMostChild = null;
        for (BoxNode child : boxNode.children) {
            if (rightMostChild == null || child.x > rightMostChild.x) {
                rightMostChild = child;
            }
        }
        float maxX = rightMostChild.x + rightMostChild.width +
                rightMostChild.style.marginRight + boxNode.style.paddingRight + boxNode.style.borderWidthRight;
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
                return previousBoxInLine.x + previousStyle.borderWidthLeft + previousStyle.paddingLeft + boxNode.style.marginLeft;
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
                // Whitespace in the HTML source after a previous sibling inline box will add one space character's worth
                // of horizontal space here. The line break element is an exception, which should not add space.
                boolean ignoreWhitespaceAfter = (siblingBox.correspondingRenderNode != null && siblingBox.correspondingRenderNode.type.equals(HTMLElements.BR)) ||
                        (boxNode.correspondingRenderNode != null && boxNode.correspondingRenderNode.type.equals(HTMLElements.BR));
                float inlineSpace = (siblingBox.whiteSpaceAfter && !ignoreWhitespaceAfter) ? textDimensionCalculator.getDimension(" ", boxNode.parent.style).x : 0;
                return siblingBox.x + siblingBox.width + previousStyle.marginRight + boxNode.style.marginLeft + inlineSpace;
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
        float widthDueToSpacing = boxNode.style.paddingLeft + boxNode.style.paddingRight;
        if (boxNode.isTextNode) {
            String text = boxNode.correspondingRenderNode.text;
            if (boxNode.textEndIndex > 0) {
                text = text.substring(boxNode.textStartIndex, boxNode.textEndIndex);
            }
            Vector2 size = textDimensionCalculator.getDimension(text, boxNode.style);
            return new Vector2(size.x + widthDueToSpacing, size.y);
        } else if (boxNode.correspondingRenderNode.type.equals(HTMLElements.IMG)) {
            return new Vector2(boxNode.width, boxNode.height);
        } else if (boxNode.correspondingRenderNode.type.equals(HTMLElements.BR)) {
            Vector2 size = textDimensionCalculator.getDimension(" ", boxNode.style);
            return new Vector2(0, size.y);
        } else if (boxNode.innerDisplayType.equals(CSSStyle.DisplayType.FLOW_ROOT)) {
            float width = boxNode.width == null ? 0 : boxNode.width;
            float height = boxNode.height == null ? 0 : boxNode.height;
            return new Vector2(width, height);
        } else if (boxNode.innerDisplayType.equals(CSSStyle.DisplayType.TABLE)) {
            return new Vector2(boxNode.width, 0);
        } else {
            return new Vector2(widthDueToSpacing, 0);
        }
    }

    private void placeBoxFullyOnCurrentLine(BoxNode boxNode, InlineFormattingContext context, float x, Vector2 preferredSize) {
        boxNode.x = x;
        boxNode.y = context.getCurrentLineYStart();

        // Normal inline boxes should have their size set here. Inline-block boxes will either have a set width/height,
        // or leave the value as null as it will be shrunk to the content later.
        boolean isInlineBlock = boxNode.outerDisplayType.equals(CSSStyle.DisplayType.INLINE) && boxNode.innerDisplayType.equals(CSSStyle.DisplayType.FLOW_ROOT);
        if (!isInlineBlock) {
            boxNode.width = preferredSize.x;
            boxNode.height = preferredSize.y;
        }
        context.addBoxToCurrentLine(boxNode);
    }

    private void placeBoxPartiallyOnCurrentLine(BoxNode boxNode, InlineFormattingContext context, float x, float availableWidth) {
        BoxNode remainingBox = textNodeSplitter.splitNodeAcrossLines(boxNode, availableWidth);
        // A null remaining box indicates that the split box contained only spaces, and thus is skipped from layout. The
        // logic skips to placing the box fully on the current line.
        if (remainingBox == null) {
            Vector2 preferredSize = getInlineBoxPreferredSize(boxNode);
            placeBoxFullyOnCurrentLine(boxNode, context, x, preferredSize);
        } else {
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
        }
        context.moveToNextLine();
    }

}
