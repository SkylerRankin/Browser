package browser.layout;

import java.util.ArrayList;
import java.util.List;

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

    public void placeBox(BoxNode boxNode, InlineFormattingContext context) {
        System.out.printf("placeBox for %s\n", boxNode);
        System.out.println(boxNode.getRootAncestor().toRecursiveString());

        CSSStyle style = boxNode.correspondingRenderNode.style;
        float x = getBoxXPosition(boxNode, context);
        Vector2 preferredSize = getInlineBoxPreferredSize(boxNode);
        float rightSideSpacing = context.getRightSpacingForBox(boxNode.id);
        float availableWidth = context.endX - x - rightSideSpacing;

        System.out.printf("preferredSize: %s, availableWidth: %.0f, right spacing: %.0f, x: %.0f\n", preferredSize, availableWidth, rightSideSpacing, x);

        if (preferredSize.x <= availableWidth) {
            // The box fits with its preferred width, it is added to the current line.
            boxNode.x = x;
            boxNode.y = context.getCurrentLineYStart();
            boxNode.width = preferredSize.x;
            boxNode.height = preferredSize.y;
            context.addBoxToCurrentLine(boxNode);
        } else if (textNodeSplitter.canSplitNodeToFitWidth(boxNode, availableWidth)) {
            // The box does not fit on the current line, but some substring of it can be split to fit.
            // TODO can use the available width plus the right spacing here, since the spacing is moved to the next line.
            BoxNode remainingBox = textNodeSplitter.fitNodeToWidth(boxNode, availableWidth);
            int indexInParent = boxNode.parent.children.indexOf(boxNode);
            boxNode.parent.children.add(indexInParent + 1, remainingBox);
            boxTreePartitioner.partition(remainingBox, context);

            // The newly generated box will take on whatever right side spacing the original box had.
            context.setRightSpacingForBox(remainingBox.id, context.getRightSpacingForBox(boxNode.id));
            context.setRightSpacingForBox(boxNode.id, 0);

            String substring = boxNode.correspondingRenderNode.text.substring(boxNode.textStartIndex, boxNode.textEndIndex);
            Vector2 newDimensions = textDimensionCalculator.getDimension(substring, style);
            boxNode.x = x;
            boxNode.y = context.getCurrentLineYStart();
            boxNode.width = newDimensions.x;
            boxNode.height = newDimensions.y;

            context.addBoxToCurrentLine(boxNode);
            context.moveToNextLine();
        } else {
            System.out.println("Box does not fit and cannot be split to fit.");
            // The box does not fit and no prefix of it can be made to fit. It is moved to the next line.

            // Move to the next line if there is already a terminal box on the line.
            if (context.currentRowHasTerminalBox()) {
                System.out.println("moving to new row");
                context.moveToNextLine();
            }

            x = getBoxXPosition(boxNode, context);
            availableWidth = context.endX - x - rightSideSpacing;

            System.out.printf("New x = %.0f, new available width = %.0f\n", x, availableWidth);

            if (preferredSize.x <= availableWidth) {
                // The box fits with its preferred width on the next line.
                boxNode.x = x;
                boxNode.y = context.getCurrentLineYStart();
                boxNode.width = preferredSize.x;
                boxNode.height = preferredSize.y;
                context.addBoxToCurrentLine(boxNode);
            } else {
                // The box still doesn't fit on the next line. It is broken on spaces regardless of any overlaps.
                BoxNode remainingBox = textNodeSplitter.fitNodeToWidth(boxNode, availableWidth);
                System.out.printf("After splitting node text, remaining box: %s\n", remainingBox);
                if (remainingBox != null) {
                    int indexInParent = boxNode.parent.children.indexOf(boxNode);
                    boxNode.parent.children.add(indexInParent + 1, remainingBox);
                    boxTreePartitioner.partition(remainingBox, context);

                    // The newly generated box will take on whatever right side spacing the original box had.
                    context.setRightSpacingForBox(remainingBox.id, context.getRightSpacingForBox(boxNode.id));
                    context.setRightSpacingForBox(boxNode.id, 0);
                }

                String substring = boxNode.correspondingRenderNode.text.substring(boxNode.textStartIndex, boxNode.textEndIndex);
                Vector2 newDimensions = textDimensionCalculator.getDimension(substring, style);
                boxNode.x = x;
                boxNode.y = context.getCurrentLineYStart();
                boxNode.width = newDimensions.x;
                boxNode.height = newDimensions.y;

                context.addBoxToCurrentLine(boxNode);
                context.moveToNextLine();
            }

        }

        System.out.printf("end placeBox for %s\n", boxNode);
        System.out.println(boxNode.getRootAncestor().toRecursiveString());
    }

    /**
     * Returns the width of an inline box. This width does not include the box's own margins.
     * @param boxNode       The inline box.
     * @return      The box's width.
     */
    public float getWidth(BoxNode boxNode) {
        CSSStyle style = boxNode.correspondingRenderNode.style;

        if (boxNode.children.size() == 0) {
            return style.paddingLeft + style.paddingRight;
        }

        BoxNode rightMostChild = null;
        for (BoxNode child : boxNode.children) {
            if (rightMostChild == null || child.x > rightMostChild.x) {
                rightMostChild = child;
            }
        }
        float maxX = rightMostChild.x + rightMostChild.width +
                rightMostChild.correspondingRenderNode.style.marginRight + style.paddingRight;
        return maxX - boxNode.x;
    }

    // Private methods

    private float getBoxXPosition(BoxNode boxNode, InlineFormattingContext context) {
        BoxNode previousBoxInLine = context.getPreviousBoxInLine();
        if (previousBoxInLine == null) {
            float rootPadding = context.rootBox.correspondingRenderNode.style.paddingLeft;
            float margin = boxNode.correspondingRenderNode.style.marginLeft;
            return context.startX + rootPadding + margin;
        } else {
            if (previousBoxInLine.children.contains(boxNode)) {
                // The previous box is the parent of the current box.
                CSSStyle previousStyle = previousBoxInLine.correspondingRenderNode.style;
                CSSStyle style = boxNode.correspondingRenderNode.style;
                return previousBoxInLine.x + previousStyle.paddingLeft + style.marginLeft;
            } else {
                // The previous box is some sibling, potentially higher up in the tree, of the current box.
                BoxNode siblingBox = getAncestorSiblingInLine(boxNode, previousBoxInLine);
                if (siblingBox == null) {
                    System.err.println("InlineLayoutFormatter:getBoxXPosition: failed to find sibling box.");
                    System.err.printf("BoxNode: %s\n", boxNode);
                    System.err.printf("Previous BoxNode in line: %s\n", previousBoxInLine);
                    return 0;
                }
                CSSStyle previousStyle = siblingBox.correspondingRenderNode.style;
                CSSStyle style = boxNode.correspondingRenderNode.style;
                return siblingBox.x + siblingBox.width + previousStyle.marginRight + style.marginLeft;
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
            size = textDimensionCalculator.getDimension(text, boxNode.correspondingRenderNode.style);
        } else if (boxNode.correspondingRenderNode.type.equals(HTMLElements.IMG)) {
            size = new Vector2(boxNode.width, boxNode.height);
        } else if (boxNode.innerDisplayType.equals(CSSStyle.DisplayType.FLOW_ROOT)) {
            size = new Vector2(boxNode.width, boxNode.height);
        }

        CSSStyle style = boxNode.correspondingRenderNode.style;
        float widthDueToSpacing = style.marginLeft + style.paddingLeft + style.paddingRight + style.marginRight;

        return new Vector2(size.x + widthDueToSpacing, size.y);
    }

    private boolean canSplitInlineBox(BoxNode boxNode, float availableWidth) {
        if (boxNode.isTextNode) {
            String[] words = boxNode.correspondingRenderNode.text.split("\s");
            String firstWord = words[0];
            float width = textDimensionCalculator.getDimension(firstWord, boxNode.correspondingRenderNode.style).x;
            return width <= availableWidth;
        } else {
            return false;
        }
    }

    private void injectSplitBoxesIntoTree(BoxNode original, List<BoxNode> newBoxes) {
        int indexInParent = original.parent.children.indexOf(original);
        List<BoxNode> newChildren = new ArrayList<>();
        for (int i = 0; i < original.parent.children.size(); i++) {
            if (i == indexInParent) {
                newChildren.addAll(newBoxes);
            } else {
                newChildren.add(original.parent.children.get(i));
            }
        }
        original.parent.children = newChildren;
    }

}
