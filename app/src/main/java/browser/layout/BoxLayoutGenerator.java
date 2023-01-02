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
    private final InlineLayoutFormatter inlineLayoutFormatter;
    private final InlineBlockWidthCalculator inlineBlockWidthCalculator;

    public BoxLayoutGenerator(final TextDimensionCalculator textDimensionCalculator) {
        inlineLayoutFormatter = new InlineLayoutFormatter(textDimensionCalculator);
        inlineBlockWidthCalculator = new InlineBlockWidthCalculator(this);
    }

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

        // Set the position of the root node.
        rootBoxNode.x = 0f;
        rootBoxNode.y = 0f;

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
        // Only allow fixed sizes on block boxes or boxes that establish new block context.
        boolean fixedSizeAllowed = boxNode.outerDisplayType.equals(CSSStyle.DisplayType.BLOCK) ||
                boxNode.innerDisplayType.equals(DisplayType.FLOW_ROOT);

        // Inline block boxes have optional fixed widths. If a width/height is not provided, the box fits to its contents.
        boolean fixedWidthOptional = boxNode.innerDisplayType.equals(DisplayType.FLOW_ROOT);

        if (fixedSizeAllowed) {
            CSSStyle style = boxNode.style;

            // Block boxes should be 100% width by default, if this was not set in the stylesheets. For anonymous block
            // boxes, for instance, the width property would not have been set.
            boolean noSetWidth = false;
            if (style.width == null && !fixedWidthOptional) {
                style.width = 100f;
                style.widthType = CSSStyle.DimensionType.PERCENTAGE;
                noSetWidth = true;
            }

            if (style.width != null) {
                float widthSpacing = (boxNode.parent == null ? 0 : boxNode.parent.style.paddingLeft + boxNode.parent.style.paddingRight) +
                        boxNode.style.marginLeft + boxNode.style.marginRight;
                if (style.widthType.equals(CSSStyle.DimensionType.PERCENTAGE)) {
                    float parentWidth = boxNode.parent == null ? screenWidth : boxNode.parent.width;
                    boxNode.width = (parentWidth * style.width / 100) - widthSpacing;
                } else {
                    boxNode.width = style.width;
                }
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
                if (style.heightType.equals(CSSStyle.DimensionType.PERCENTAGE)) {
                    if (boxNode.parent == null || boxNode.parent.height == null) {
                        height = null;
                    } else {
                        float heightSpacing = boxNode.parent.style.paddingTop + boxNode.parent.style.paddingBottom +
                                boxNode.style.marginTop + boxNode.style.marginBottom;
                        height = (boxNode.parent.height * style.height / 100) - heightSpacing;
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

            // If using content box sizing, the original width and height is reserved for the box content, so padding
            // and border values are added.
            // For width, block boxes are automatically set to 100% as default. In the case of 100% width and content-box
            // box sizing, the box should not extend past the bounds of its parent, so the padding/border addition is
            // skipped.
            if (boxNode.style.boxSizing.equals(CSSStyle.BoxSizingType.CONTENT_BOX)) {
                if (boxNode.width != null && !noSetWidth) {
                    boxNode.width += style.paddingLeft + style.paddingRight + style.borderWidthLeft + style.borderWidthRight;
                }

                if (boxNode.height != null) {
                    boxNode.height += style.paddingTop + style.paddingBottom + style.borderWidthTop + style.borderWidthBottom;
                }
            }
        }

        boolean isInlineBlock = boxNode.outerDisplayType.equals(DisplayType.BLOCK) && boxNode.innerDisplayType.equals(DisplayType.FLOW_ROOT);
        if (isInlineBlock && boxNode.width == null) {
            float availableWidth = boxNode.parent == null ? screenWidth : boxNode.parent.width -
                    boxNode.parent.style.borderWidthLeft - boxNode.parent.style.paddingLeft - boxNode.style.marginLeft -
                    boxNode.parent.style.borderWidthRight - boxNode.parent.style.paddingRight - boxNode.style.marginRight;
            boxNode.width = inlineBlockWidthCalculator.getWidth(boxNode, availableWidth);
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
            CSSStyle style = boxNode.style;
            // TODO: the default size should come from the actual image dimensions. have to download the image first
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
                .filter(b -> b.style.position == PositionType.STATIC ||
                        b.style.position == PositionType.RELATIVE)
                .toList();

        if (childrenInNormalFlow.size() == 0) {
            boxNode.inlineFormattingContextId = boxNode.parent == null ? -1 : boxNode.parent.inlineFormattingContextId;
        } else {
            DisplayType childrenDisplayType = childrenInNormalFlow.get(0).outerDisplayType;
            if (childrenDisplayType.equals(DisplayType.INLINE)) {
                int id;
                if (boxNode.parent != null && boxNode.parent.inlineFormattingContextId != -1
                        && !boxNode.innerDisplayType.equals(DisplayType.FLOW_ROOT)) {
                    id = boxNode.parent.inlineFormattingContextId;
                } else {
                    id = inlineFormattingContexts.size();
                    inlineFormattingContexts.put(id, new InlineFormattingContext(id, boxNode.width, boxNode.id));
                }
                boxNode.inlineFormattingContextId = id;
            }
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
        // TODO: add in all other cases of new BFCs.
        if (boxNode.innerDisplayType.equals(DisplayType.FLOW_ROOT) || boxNode.parent == null) {
            id = blockFormattingContexts.size();
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
                    .filter(b -> b.style.position == PositionType.STATIC ||
                            b.style.position == PositionType.RELATIVE)
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
        BlockFormattingContext context = blockFormattingContexts.get(parentBox.blockFormattingContextId);

        for (BoxNode childBox : parentBox.children) {
            BoxNode lastPlacedBox = context.getLastPlacedBoxForId(parentBox.id);
            childBox.x = parentBox.x + parentBox.style.borderWidthLeft + parentBox.style.paddingLeft + childBox.style.marginLeft;
            if (lastPlacedBox == null) {
                childBox.y = parentBox.y + parentBox.style.borderWidthTop + parentBox.style.paddingTop + childBox.style.marginTop;
            } else {
                childBox.y = lastPlacedBox.y + lastPlacedBox.height + lastPlacedBox.style.marginBottom + childBox.style.marginTop;
            }

            // Recursively run layout on the child after placement. This will determine its height.
            setBoxLayout(childBox);
            context.setLastPlacedBoxForId(parentBox.id, childBox);
        }

        if (parentBox.height == null) {
            parentBox.height = getHeightFromChildren(parentBox);
        }
    }

    private void layoutInlineBoxes(BoxNode parentBox) {
        InlineFormattingContext context = inlineFormattingContexts.get(parentBox.inlineFormattingContextId);
        if (parentBox.id == context.contextRootId) {
            context.initialize(parentBox);
        }

        for (int i = 0; i < parentBox.children.size(); i++) {
            BoxNode childBox = parentBox.children.get(i);
            boolean earlyExit = inlineLayoutFormatter.placeBox(childBox, context);
            if (earlyExit) {
                // This child box (and its subsequent siblings) have been moved to a new branch, so the loop can stop
                // early. If there were previous children, then the width/height calculations on the parent still need
                // to happen.
                if (i == 0) {
                    return;
                }
                break;
            }
            setBoxLayout(parentBox.children.get(i));
        }

        // If this is an inline element without a predefined width, its width is the necessary size to contain its
        // children.
        if (parentBox.outerDisplayType.equals(DisplayType.INLINE) && !parentBox.innerDisplayType.equals(DisplayType.FLOW_ROOT)) {
            parentBox.width = inlineLayoutFormatter.getWidth(parentBox);
        }

        // Inline boxes will have their height derived from their children. If this was a block box containing inline
        // boxes, then it will either have a predefined height or will need to derive the height from its children as
        // well.
        if (parentBox.style.outerDisplay.equals(DisplayType.INLINE) || parentBox.height == null) {
            parentBox.height = getHeightFromChildren(parentBox);
        }
    }

    private float getHeightFromChildren(BoxNode boxNode) {
        if (boxNode.children.size() == 0) {
            return boxNode.height == null ? 0 : boxNode.height;
        }

        float maxY = 0;
        for (BoxNode childBox : boxNode.children) {
            float childHeight = childBox.height == null ? 0 : childBox.height;
            float newY = childBox.y + childHeight + childBox.style.marginBottom;
            maxY = Math.max(maxY, newY);
        }

        // Vertical borders and padding do not contribute to the layout of inline boxes. Inline-block is the one exception.
        float bottomSpacing = !boxNode.outerDisplayType.equals(DisplayType.INLINE) || boxNode.innerDisplayType.equals(DisplayType.FLOW_ROOT) ?
                boxNode.style.borderWidthBottom + boxNode.style.paddingBottom : 0;

        return maxY - boxNode.y + bottomSpacing;
    }

}
