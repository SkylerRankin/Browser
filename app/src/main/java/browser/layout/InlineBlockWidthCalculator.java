package browser.layout;

import static browser.css.CSSStyle.DisplayType.*;

import java.util.ArrayList;
import java.util.List;

import browser.css.CSSStyle;
import browser.model.BoxNode;

public class InlineBlockWidthCalculator {

    private final BoxLayoutGenerator boxLayoutGenerator;

    public InlineBlockWidthCalculator(BoxLayoutGenerator boxLayoutGenerator) {
        this.boxLayoutGenerator = boxLayoutGenerator;
    }

    // Public methods

    /**
     * Calculates the width of an inline-block box by running layout on its children. If the width is already defined,
     * that value is returned. If the width is not defined, a shrink-to-fit algorithm is run that determines the width
     * based on the contents of the box.
     * @param boxNode       The inline-block box.
     * @param availableWidth        The available width in the containing box.
     * @return      The width of the inline-block box.
     */
    public float getWidth(BoxNode boxNode, float availableWidth) {
        if (boxNode.width != null) {
            return boxNode.width;
        }

        List<Float> widths = List.of(Float.MAX_VALUE, 1f);
        List<Float> results = new ArrayList<>();

        for (Float width : widths) {
            BoxNode copyBoxNode = boxNode.deepCopy();
            removePercentageWidthBlockBoxes(copyBoxNode);
            copyBoxNode.innerDisplayType = CSSStyle.DisplayType.FLOW;
            copyBoxNode.outerDisplayType = CSSStyle.DisplayType.BLOCK;
            copyBoxNode.style.width = width;
            copyBoxNode.style.widthType = CSSStyle.DimensionType.PIXEL;
            boxLayoutGenerator.calculateLayout(copyBoxNode, width);
            float maxX = 0;
            for (BoxNode child : copyBoxNode.children) {
                float childMaxX = child.x + child.width + copyBoxNode.style.borderWidthRight + copyBoxNode.style.paddingRight + child.style.marginRight;
                maxX = Math.max(childMaxX, maxX);
            }
            float preferredWidth = maxX - copyBoxNode.x;
            results.add(preferredWidth);
        }

        float preferredWidth = results.get(0);
        float preferredMinWidth = results.get(1);
        return Math.min(Math.max(preferredMinWidth, availableWidth), preferredWidth);
    }

    // Private methods

    /**
     * When calculating the width of an inline block box, child block boxes with percentage widths should shrink to
     * their contents during the algorithm's first pass. This method sets their widths to null to allow the shrinking
     * to happen.
     * The recursive search will end early if an inline box is found or if a block box is found with a defined pixel
     * width. This is because percentage width block boxes are impossible within inline boxes and are valid within
     * set-width block boxes, so nothing needs to be removed in either case.
     * @param boxNode       The box node to process.
     */
    private void removePercentageWidthBlockBoxes(BoxNode boxNode) {
        boolean isBlockBox = boxNode.outerDisplayType.equals(BLOCK);
        boolean isInlineBlockBox = boxNode.outerDisplayType.equals(INLINE) && boxNode.innerDisplayType.equals(FLOW_ROOT);

        if ((isBlockBox || isInlineBlockBox) && (boxNode.style.width == null || boxNode.style.widthType.equals(CSSStyle.DimensionType.PERCENTAGE))) {
            boxNode.shrinkBlockWidthToContent = true;
        }

        // Percentages that are contained within a block with a pixel width are valid, so no removals need to happen.
        if ((isBlockBox || isInlineBlockBox) && boxNode.style.width != null && boxNode.style.widthType.equals(CSSStyle.DimensionType.PIXEL)) {
            return;
        }

        // There shouldn't be any block boxes within an inline box, so no removals need to happen.
        if (!isBlockBox && !isInlineBlockBox) {
            return;
        }

        for (BoxNode child : boxNode.children) {
            removePercentageWidthBlockBoxes(child);
        }
    }

}
