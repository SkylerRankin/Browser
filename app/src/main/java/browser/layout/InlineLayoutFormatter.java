package browser.layout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import browser.css.CSSStyle;
import browser.model.BoxNode;
import browser.model.Vector2;
import browser.parser.HTMLElements;

public class InlineLayoutFormatter {

    private final TextDimensionCalculator textDimensionCalculator;
    private final TextNodeSplitter textNodeSplitter;

    public InlineLayoutFormatter(TextDimensionCalculator textDimensionCalculator) {
        this.textDimensionCalculator = textDimensionCalculator;
        this.textNodeSplitter = new TextNodeSplitter(textDimensionCalculator);
    }

    public void placeBox(BoxNode boxNode, InlineFormattingContext context) {
        boolean lastInParent = boxNode.parent == null || boxNode.parent.children.indexOf(boxNode) == boxNode.parent.children.size() - 1;
        CSSStyle style = boxNode.correspondingRenderNode.style;
        float x = getBoxXPosition(boxNode);

        Vector2 preferredSize = getInlineBoxPreferredSize(boxNode);
        float rightSideSpacing = style.marginRight + (lastInParent ? boxNode.parent.correspondingRenderNode.style.paddingRight : 0);
        float availableWidth = context.endX - x - rightSideSpacing;
        if (preferredSize.x <= availableWidth) {
            // The box fits with its preferred width, it is added to the current line.
            boxNode.x = x;
            boxNode.y = context.getCurrentLineYStart();
            boxNode.width = preferredSize.x;
            boxNode.height = preferredSize.y;
            context.addBoxToCurrentLine(boxNode);
        } else if (canSplitInlineBox(boxNode, availableWidth)) {
            // The box does not fit in the current line, but can be split such that some of it will fit in the current
            // line, while the remaining portion is placed on subsequent lines.
            List<BoxNode> newBoxes = textNodeSplitter.split(boxNode, availableWidth, context.width);
            injectSplitBoxesIntoTree(boxNode, newBoxes);
            // TODO modify the box tree to split around the boxes on a new line
            BoxNode boxForCurrentLine = newBoxes.get(0);
            boxForCurrentLine.x = x;
            boxForCurrentLine.y = context.getCurrentLineYStart();
            boxForCurrentLine.width = preferredSize.x;
            boxForCurrentLine.height = preferredSize.y;
            context.addBoxToCurrentLine(boxForCurrentLine);
            context.moveToNextLine();
        } else {
            // The box is too large to fit on the current line and cannot be split to fit, even partially. The boxes
            // are placed on new lines. If the box is a text node, it is split since it may be too large for the next
            // line.
            BoxNode boxForCurrentLine = boxNode;
            if (boxNode.isTextNode) {
                List<BoxNode> newBoxes = textNodeSplitter.split(boxNode, availableWidth, context.width);
                if (newBoxes.size() > 0) {
                    injectSplitBoxesIntoTree(boxNode, newBoxes);
                    // TODO modify the box tree to split around the boxes on a new line
                    boxForCurrentLine = newBoxes.get(0);
                }
            }

            context.moveToNextLine();
            boxForCurrentLine.x = boxNode.parent.x;
            boxForCurrentLine.y = context.getCurrentLineYStart();
            boxForCurrentLine.width = preferredSize.x;
            boxForCurrentLine.height = preferredSize.y;
            context.addBoxToCurrentLine(boxForCurrentLine);
        }
    }

    private float getBoxXPosition(BoxNode boxNode) {
        boolean firstInParent = boxNode.parent == null || boxNode.parent.children.indexOf(boxNode) == 0;
        CSSStyle style = boxNode.correspondingRenderNode.style;
        if (boxNode.parent == null) {
            return style.marginLeft;
        } else if (firstInParent) {
            return boxNode.parent.x + boxNode.parent.correspondingRenderNode.style.paddingLeft + style.marginLeft;
        } else {
            int indexInParent = boxNode.parent.children.indexOf(boxNode);
            BoxNode previousBox = boxNode.parent.children.get(indexInParent - 1);
            return previousBox.x + previousBox.width + previousBox.correspondingRenderNode.style.marginRight + style.marginLeft;
        }
    }

    private Vector2 getInlineBoxPreferredSize(BoxNode boxNode) {
        if (boxNode.isTextNode) {
            return textDimensionCalculator.getDimension(boxNode.correspondingRenderNode.text, boxNode.correspondingRenderNode.style);
        } else if (boxNode.correspondingRenderNode.type.equals(HTMLElements.IMG)) {
            return new Vector2(boxNode.width, boxNode.height);
        } else if (boxNode.innerDisplayType.equals(CSSStyle.DisplayType.FLOW_ROOT)) {
            return new Vector2(boxNode.width, boxNode.height);
        }

        return new Vector2(0, 0);
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
