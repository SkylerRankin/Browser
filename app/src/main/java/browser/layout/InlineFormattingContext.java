package browser.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import browser.css.CSSStyle;
import browser.model.BoxNode;
import browser.parser.HTMLElements;

public class InlineFormattingContext {

    public final int id;
    public final int contextRootId;
    public float startX;
    public float endX;
    public BoxNode rootBox;

    private final List<LineBox> lineBoxes;
    private final List<BoxNode> tentativeBoxesForLine;
    private final List<Float> maxHeightPerLine;
    private final List<Float> yStartPerLine;
    private final List<Boolean> terminalBoxInLine;
    private final Map<Integer, Float> rightSpacingPerBox;
    private final Map<Integer, Float> leftSpacingPerBox;

    public InlineFormattingContext(int id, int rootId) {
        this.id = id;
        this.contextRootId = rootId;
        lineBoxes = new ArrayList<>();
        maxHeightPerLine = new ArrayList<>();
        yStartPerLine = new ArrayList<>();
        terminalBoxInLine = new ArrayList<>();
        tentativeBoxesForLine = new ArrayList<>();
        rightSpacingPerBox = new HashMap<>();
        leftSpacingPerBox = new HashMap<>();
    }

    // Public methods

    public void initialize(BoxNode rootBox) {
        this.rootBox = rootBox;
        startX = rootBox.x + rootBox.style.borderWidthLeft + rootBox.style.paddingLeft;
        endX = startX + rootBox.width - rootBox.style.paddingLeft - rootBox.style.paddingRight - rootBox.style.borderWidthLeft - rootBox.style.borderWidthRight;
        yStartPerLine.add(rootBox.y + rootBox.style.borderWidthTop + rootBox.style.paddingTop);
        maxHeightPerLine.add(0f);
        lineBoxes.add(new LineBox());
        terminalBoxInLine.add(false);

        initializeSpacingValues(rootBox);
    }

    public float getCurrentLineYStart() {
        return yStartPerLine.get(yStartPerLine.size() - 1);
    }

    public void addBoxToCurrentLine(BoxNode boxNode) {
        tentativeBoxesForLine.add(boxNode);

        if (isTerminalInContext(boxNode)) {
            terminalBoxInLine.set(terminalBoxInLine.size() - 1, true);
            for (BoxNode tentativeBoxNode : tentativeBoxesForLine) {
                lastLineBox().boxes.add(tentativeBoxNode);
                float tentativeBoxHeight = tentativeBoxNode.height == null ? 0 : tentativeBoxNode.height;
                if (maxHeightPerLine.get(maxHeightPerLine.size() - 1) < tentativeBoxHeight) {
                    maxHeightPerLine.set(maxHeightPerLine.size() - 1, tentativeBoxNode.height);
                }
            }
            tentativeBoxesForLine.clear();
        }
    }

    public void moveToNextLine() {
        float lastYStart = yStartPerLine.get(yStartPerLine.size() - 1);
        float lastMaxHeight = maxHeightPerLine.get(maxHeightPerLine.size() - 1);
        yStartPerLine.add(lastYStart + lastMaxHeight);
        maxHeightPerLine.add(0f);
        lineBoxes.add(new LineBox());
        terminalBoxInLine.add(false);

        // If there are current tentative boxes, they need to be moved to the next line by updating their positions.
        if (tentativeBoxesForLine.size() > 0) {
            float parentX = tentativeBoxesForLine.get(0).parent.x;
            float parentLeftPadding = tentativeBoxesForLine.get(0).parent.style.paddingLeft;
            float parentLeftBorder = tentativeBoxesForLine.get(0).parent.style.borderWidthLeft;
            float previousX = parentX + (tentativeBoxesForLine.get(0).id == contextRootId ? parentLeftBorder + parentLeftPadding : 0);
            float y = yStartPerLine.get(yStartPerLine.size() - 1);
            for (BoxNode box : tentativeBoxesForLine) {
                float marginLeft = box.style.marginLeft;
                box.x = previousX + marginLeft;
                box.y = y;

                previousX += box.x + box.style.borderWidthRight + box.style.paddingRight;
            }
        }
    }

    public BoxNode getPreviousBoxInLine() {
        if (tentativeBoxesForLine.size() > 0) {
            return tentativeBoxesForLine.get(tentativeBoxesForLine.size() - 1);
        }

        LineBox lastLineBox = lastLineBox();
        if (lastLineBox.boxes.size() == 0) {
            return null;
        } else {
            return lastLineBox.boxes.get(lastLineBox.boxes.size() - 1);
        }
    }

    public void clearTentativeBoxes() {
        for (BoxNode tentativeBox : tentativeBoxesForLine) {
            tentativeBox.x = null;
            tentativeBox.y = null;
        }
        tentativeBoxesForLine.clear();
    }

    public boolean currentRowHasTerminalBox() {
        return terminalBoxInLine.get(terminalBoxInLine.size() - 1);
    }

    public void setRightSpacingForBox(int id, float value) {
        rightSpacingPerBox.put(id, value);
    }

    public void setLeftSpacingForBox(int id, float value) {
        leftSpacingPerBox.put(id, value);
    }

    /**
     * Gets the spacing on the right side of a box from the next terminal inline box.
     * @param id        The id of the box.
     * @return      The right spacing value.
     */
    public float getRightSpacingForBox(int id) {
        if (!rightSpacingPerBox.containsKey(id)) {
            return 0;
        }
        return rightSpacingPerBox.get(id);
    }

    /**
     * Gets the spacing on the left side of a box from the next terminal inline box.
     * @param id        The id of the box.
     * @return      The right spacing value.
     */
    public float getLeftSpacingForBox(int id) {
        if (!leftSpacingPerBox.containsKey(id)) {
            return 0;
        }
        return leftSpacingPerBox.get(id);
    }

    // Private methods

    private LineBox lastLineBox() {
        return lineBoxes.get(lineBoxes.size() - 1);
    }

    /**
     * Determines if the box is a terminal node for this inline context. This means it contains no children that belong
     * to the context, and is not a container for other elements (such as a span with no children).
     * @param boxNode       The box node.
     * @return      True if the box node is terminal.
     */
    private boolean isTerminalInContext(BoxNode boxNode) {
        String type = boxNode.correspondingRenderNode.type;
        return type.equals(HTMLElements.TEXT) || type.equals(HTMLElements.IMG) ||
                boxNode.innerDisplayType.equals(CSSStyle.DisplayType.FLOW_ROOT) || boxNode.children.size() == 0;
    }

    /**
     * Initializes the right and left spacing maps based on the stacked margins and paddings of the boxes. Only
     * boxes within this context are added. Spacing is initialized based on each box in the context being in the
     * same line. The spacing values represent the distance between the given side of a box and the next terminal box.
     * As such, boxes that are the first or last children in their parent will include the spacing inherited from the
     * parent, while other boxes will just have spacing based on their own margins.
     * The padding of the root box is not included. This is because the root is always a block box, and its padding
     * impacts all child boxes, unlike the inline paddings which only impact the next adjacent box.
     * As text lines are split and inline boxes are moved to new lines during layout, these spacing values may be
     * updated.
     * @param rootBox       The box to start with.
     */
    private void initializeSpacingValues(BoxNode rootBox) {
        if (rootBox.inlineFormattingContextId != id) {
            return;
        }

        if (rootBox.id != contextRootId) {
            float rightMargin = rootBox.style.marginRight;
            float leftMargin = rootBox.style.marginLeft;
            float parentRightPadding = 0;
            float parentLeftPadding = 0;
            float parentRightBorder = 0;
            float parentLeftBorder = 0;
            float inheritedRightSpacing = 0;
            float inheritedLeftSpacing = 0;

            boolean firstInParent = rootBox.parent.children.indexOf(rootBox) == 0;
            boolean lastInParent = rootBox.parent.children.indexOf(rootBox) == rootBox.parent.children.size() - 1;

            if (firstInParent) {
                parentLeftPadding = rootBox.parent.id == contextRootId ? 0 : rootBox.parent.style.paddingLeft;
                parentLeftBorder = rootBox.parent.id == contextRootId ? 0 : rootBox.parent.style.borderWidthLeft;

                if (leftSpacingPerBox.containsKey(rootBox.parent.id)) {
                    inheritedLeftSpacing = leftSpacingPerBox.get(rootBox.parent.id);
                }
            }

            if (lastInParent) {
                parentRightPadding = rootBox.parent.id == contextRootId ? 0 : rootBox.parent.style.paddingRight;
                parentRightBorder = rootBox.parent.id == contextRootId ? 0 : rootBox.parent.style.borderWidthRight;

                if (rightSpacingPerBox.containsKey(rootBox.parent.id)) {
                    inheritedRightSpacing = rightSpacingPerBox.get(rootBox.parent.id);
                }
            }

            rightSpacingPerBox.put(rootBox.id, parentRightBorder + parentRightPadding + rightMargin + inheritedRightSpacing);
            leftSpacingPerBox.put(rootBox.id, parentLeftBorder + parentLeftPadding + leftMargin + inheritedLeftSpacing);
        }

        for (BoxNode child : rootBox.children) {
            initializeSpacingValues(child);
        }
    }

    public static class LineBox {
        public final List<BoxNode> boxes = new ArrayList<>();
    }

}
