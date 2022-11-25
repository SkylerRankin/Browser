package browser.layout;

import static browser.css.CSSStyle.DisplayType;
import static browser.css.CSSStyle.PositionType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import browser.css.CSSStyle;
import browser.model.BoxNode;
import browser.parser.HTMLElements;

public class BoxLayoutGenerator {

    private float screenWidth;
    private final Map<Integer, InlineFormattingContext> inlineFormattingContexts = new HashMap<>();
    private final Map<Integer, BlockFormattingContext> blockFormattingContexts = new HashMap<>();
    private final TextDimensionCalculator textDimensionCalculator = new TextDimensionCalculator();
    private final InlineLayoutFormatter inlineLayoutFormatter = new InlineLayoutFormatter(textDimensionCalculator);

    // Public methods

    public void calculateLayout(BoxNode rootBoxNode, float screenWidth) {
        this.screenWidth = screenWidth;

        // Set all fixed heights/widths.
        setFixedSizes(rootBoxNode);
        setImageSizes(rootBoxNode);

        // Set the max width of each inline box, based on the closest parent block box.
        setInlineMaxWidths(rootBoxNode);

        // Set the formatting context ids for each box.
        setInlineFormattingContexts(rootBoxNode);
        setBlockFormattingContexts(rootBoxNode);

        // Set the position and width of the root node.
        rootBoxNode.x = 0f;
        rootBoxNode.y = 0f;
        rootBoxNode.width = screenWidth;

        // Layout all box nodes within the root.
        setBoxLayout(rootBoxNode);
    }

    // Private methods

    /**
     * Elements may use the CSS width and height properties to set a fixed size, either by a pixel value or as a
     * percentage of the parent. This method sets all those fixed sizes in the corresponding box node. All block
     * boxes should have a fixed size, and no inline elements should have a fixed size. Since the method proceeds
     * recursively from parent to child, any box node (besides the root) can always reference its parent for a
     * percentage based width.
     * A percentage height when the parent has no fixed height will be ignored.
     * @param boxNode       The box to set the fixed size of.
     */
    private void setFixedSizes(BoxNode boxNode) {
        // Only allow fixed sizes on block boxes.
        boolean fixedSizeAllowed = boxNode.outerDisplayType.equals(CSSStyle.DisplayType.BLOCK);

        if (fixedSizeAllowed) {
            CSSStyle style = boxNode.correspondingRenderNode.style;

            // Block boxes should be 100% width by default, if this was not set in the stylesheets. For anonymous block
            // boxes, for instance, the width property would not have been set.
            if (style.width == null) {
                style.width = 100f;
                style.widthType = CSSStyle.DimensionType.PERCENTAGE;
            }

            if (style.widthType.equals(CSSStyle.DimensionType.PERCENTAGE)) {
                float parentWidth = boxNode.parent == null ? screenWidth : boxNode.parent.width;
                boxNode.width = parentWidth * style.width / 100;
            } else {
                // TODO should the margin and padding play a role here?
                boxNode.width = style.width;
            }

            // The max-width property overrides the width property.
            if (style.maxWidth != null) {
                if (style.maxWidthType.equals(CSSStyle.DimensionType.PERCENTAGE)) {
                    float parentWidth = boxNode.parent == null ? screenWidth : boxNode.parent.width;
                    float maxWidthValue = parentWidth * style.maxWidth / 100;
                    boxNode.width = Math.min(boxNode.width, maxWidthValue);
                } else if (style.maxWidthType.equals(CSSStyle.DimensionType.PIXEL)) {
                    boxNode.width = Math.min(boxNode.width, style.maxWidth);
                }
            }

            if (style.height != null) {
                Float height;
                if (style.widthType.equals(CSSStyle.DimensionType.PERCENTAGE)) {
                    if (boxNode.parent == null || boxNode.parent.height == null) {
                        height = null;
                    } else {
                        height = boxNode.parent.height * style.height / 100;
                    }
                } else {
                    height = style.height;
                }
                boxNode.height = height;
            }

            // The max-height property overrides the height property.
            if (style.maxHeight != null) {
                if (style.maxHeightType.equals(CSSStyle.DimensionType.PERCENTAGE)) {
                    if (boxNode.parent != null && boxNode.parent.height != null) {
                        float maxHeightValue = boxNode.parent.height * style.maxHeight / 100;
                        boxNode.height = Math.min(boxNode.height, maxHeightValue);
                    }
                } else if (style.maxHeightType.equals(CSSStyle.DimensionType.PIXEL)) {
                    boxNode.height = Math.min(boxNode.height, style.maxHeight);
                }
            }
        }

        for (BoxNode child : boxNode.children) {
            setFixedSizes(child);
        }
    }

    /**
     * Images are a special case of elements that have fixed sizes. They are inline by default but have a fixed size
     * that can be overwritten by the width/height attributes or by CSS properties.
     * @param boxNode       The box to set the fixed size of, if it's an image.
     */
    private void setImageSizes(BoxNode boxNode) {
        if (boxNode.correspondingRenderNode.type.equals(HTMLElements.IMG)) {
            Map<String, String> attributes = boxNode.correspondingRenderNode.attributes;
            CSSStyle style = boxNode.correspondingRenderNode.style;
            // TODO the default size should come from the actual image dimensions. have to download the image first
            float width = 50;
            float height = 50;
            if (attributes.containsKey("width")) {
                try {
                    width = Float.parseFloat(attributes.get("width"));
                } catch (Exception ignored) {}
            }

            if (style.width != null) {
                width = style.width;
            }

            if (attributes.containsKey("height")) {
                try {
                    height = Float.parseFloat(attributes.get("height"));
                } catch (Exception ignored) {}
            }

            if (style.height != null) {
                height = style.height;
            }

            boxNode.width = width;
            boxNode.height = height;
        }

        for (BoxNode child : boxNode.children) {
            setImageSizes(child);
        }
    }

    /**
     * Inline elements are laid out according to an inline formatting context, which maintains the state of line boxes,
     * which contain the rows of inline boxes. This method sets the inline formatting context id on each inline box.
     * An inline box either defines a new formatting context, or shares the context of its parent, given the parent
     * is also an inline box.
     * @param boxNode       The box node to set the inline formatting context id of.
     */
    private void setInlineFormattingContexts(BoxNode boxNode) {
        List<BoxNode> childrenInNormalFlow = boxNode.children.stream()
                .filter(b -> b.correspondingRenderNode.style.position == PositionType.STATIC ||
                        b.correspondingRenderNode.style.position == PositionType.RELATIVE)
                .toList();
        DisplayType childrenDisplayType = childrenInNormalFlow.get(0).outerDisplayType;
        if (childrenDisplayType.equals(DisplayType.INLINE)) {
            int id;
            if (boxNode.parent != null && boxNode.parent.outerDisplayType.equals(DisplayType.INLINE)) {
                id = boxNode.parent.inlineFormattingContextId;
            } else {
                id = inlineFormattingContexts.size();
                inlineFormattingContexts.put(id, new InlineFormattingContext(boxNode.maxWidth, boxNode.id));
            }
            boxNode.inlineFormattingContextId = id;
        }

        for (BoxNode child : boxNode.children) {
            setInlineFormattingContexts(child);
        }
    }

    /**
     * Block elements are laid out according to a block formatting context. The root element creates an initial
     * block formatting context. Other elements establish their own new contexts in a few specific cases, such as
     * having an inner display type of 'flow-root', having display of 'inline-block', being a table cell element, etc.
     * Full list is <a href="https://developer.mozilla.org/en-US/docs/Web/Guide/CSS/Block_formatting_context">here</a>.
     * @param boxNode       The box node to set the block formatting context of.
     */
    private void setBlockFormattingContexts(BoxNode boxNode) {
        int id;
        // TODO add in all other cases of new BFCs.
        if (boxNode.innerDisplayType.equals(DisplayType.FLOW_ROOT) || boxNode.parent == null) {
            id = inlineFormattingContexts.size();
            blockFormattingContexts.put(id, new BlockFormattingContext());
        } else {
            id = boxNode.parent.blockFormattingContextId;
        }
        boxNode.blockFormattingContextId = id;

        for (BoxNode child : boxNode.children) {
            setBlockFormattingContexts(child);
        }
    }

    /**
     * Inline boxes do not have set widths, but they are placed horizontally in a line box that extends the full
     * available width. This method sets the max width property such that an inline element can know when to wrap
     * the contained inline boxes into a new line.
     * @param boxNode       The box node to set the max width of.
     */
    private void setInlineMaxWidths(BoxNode boxNode) {
        if (boxNode.outerDisplayType.equals(DisplayType.INLINE)) {
            if (boxNode.parent == null) {
                boxNode.maxWidth = screenWidth;
            } else if (boxNode.parent.outerDisplayType.equals(DisplayType.BLOCK)) {
                boxNode.maxWidth = boxNode.parent.width;
            } else {
                boxNode.maxWidth = boxNode.parent.maxWidth;
            }
        }

        for (BoxNode child : boxNode.children) {
            setInlineMaxWidths(child);
        }
    }

    /**
     * Sets the position and size of the given box node's children.
     * @param boxNode       The box node to layout.
     */
    private void setBoxLayout(BoxNode boxNode) {
        if (boxNode.children.size() == 0) {
            return;
        }

        if (boxNode.innerDisplayType.equals(CSSStyle.DisplayType.FLOW)) {
            List<BoxNode> childrenInNormalFlow = boxNode.children.stream()
                    .filter(b -> b.correspondingRenderNode.style.position == PositionType.STATIC ||
                            b.correspondingRenderNode.style.position == PositionType.RELATIVE)
                    .toList();
            DisplayType childrenDisplayType = childrenInNormalFlow.get(0).outerDisplayType;
            if (childrenDisplayType.equals(DisplayType.BLOCK)) {
                // Assume all children are block. Use the block formatting algorithm.
                layoutBlockBoxes(boxNode);
            } else {
                // Assume all children are inline. Use the inline formatting algorithm.
                layoutInlineBoxes(boxNode);
            }
        } else {
            System.err.printf("BoxLayoutGenerator.setBoxLayout: unsupported inner display type: %s\n", boxNode.innerDisplayType);
        }
    }

    private void layoutBlockBoxes(BoxNode parentBox) {
        BlockFormattingContext context = blockFormattingContexts.get(parentBox.id);

        for (BoxNode childBox : parentBox.children) {
            BoxNode lastPlacedBox = context.getLastPlacedBoxForId(parentBox.id);
            childBox.x = parentBox.x;
            if (lastPlacedBox == null) {
                childBox.y = parentBox.y;
            } else {
                // TODO add margin and padding and border offsets
                childBox.y = lastPlacedBox.y + lastPlacedBox.height;
            }

            // Recursively run layout on the child after placement. This will determine its height.
            setBoxLayout(childBox);
            context.setLastPlacedBoxForId(parentBox.id, childBox);
        }

        float maxY = 0;
        for (BoxNode childBox : parentBox.children) {
            maxY = Math.max(maxY, childBox.y);
        }
        // TODO include padding, margins
        parentBox.height = maxY - parentBox.y;
    }

    private void layoutInlineBoxes(BoxNode parentBox) {
        InlineFormattingContext context = inlineFormattingContexts.get(parentBox.id);
        if (parentBox.id == context.contextRootId) {
            context.initialize(parentBox);
        }

        for (BoxNode childBox : parentBox.children) {
            inlineLayoutFormatter.placeBox(childBox, context);
            setBoxLayout(childBox);
        }

        // At this point, all the children should be placed, so can determine the size of the box?
        // TODO set the height based on the line boxes
    }

}
