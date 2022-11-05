package browser.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import browser.css.CSSStyle;
import browser.model.Box;
import browser.model.RenderNode;
import browser.model.Vector2;
import browser.parser.HTMLElements;

public class BoxLayoutCalculator {

    private final float screenWidth;
    
    private final Map<Integer, RenderNode> lastAddedChildMap;
    private final Map<Integer, RenderNode> parentNodeMap;
    private final TextSplitter textSplitter;
        
    public BoxLayoutCalculator(Map<Integer, RenderNode> parentNodeMap, float screenWidth) {
        this.screenWidth = screenWidth;
        this.parentNodeMap = parentNodeMap;
        this.lastAddedChildMap = new HashMap<>();
        textSplitter = new TextSplitter(parentNodeMap);
    }
    
    public void clearBoxBounds(RenderNode root) {
        root.box = new Box();
        for (RenderNode child : root.children) {
            clearBoxBounds(child);
        }
    }
    
    /**
     * Traverse the render tree and fill in all the boxes that have fixed sizes. The elements that have fixed sizes are:
     *  - text
     *  - images
     *  - any block or inline-block element with width or height CSS properties
     * @param root The render node to start on.
     */
    public void setBoxBounds(RenderNode root) {
        RenderNode parent = parentNodeMap.get(root.id);
        if (parent == null) {
            // If the parent is null, this is the root element, so it should fill the screen width and be positioned at
            // the absolute top left.
            root.box.fixedWidth = true;
            root.maxWidth = root.style.maxWidth == null ? screenWidth : root.style.maxWidth;
            root.box.width = root.maxWidth;
            root.box.x = 0;
            root.box.y = 0;
        } else {
            root.maxWidth = root.style.maxWidth == null ? parent.maxWidth : root.style.maxWidth;
        }

        if (parent != null) {
            if (root.text != null) {
                Vector2 textSize = TextDimensionCalculator.getTextDimension(root.text, root.style);
                root.box.width = textSize.x;
                root.box.height = textSize.y;
                if (parent.children.size() == 1 && parent.style.display.equals(CSSStyle.displayType.INLINE)) {
                    parent.box.width = textSize.x;
                    parent.box.height = textSize.y;
                }
            } else if (root.type.equals("img")) {
                root.box.width = root.attributes.containsKey("width") ? Float.parseFloat(root.attributes.get("width")) : 50;
                root.box.height = root.attributes.containsKey("height") ? Float.parseFloat(root.attributes.get("height")) : 50;
            }
            
            if (root.style.height != null && root.style.display != CSSStyle.displayType.INLINE) {
                root.box.fixedHeight = true;
                if (root.style.heightType.equals(CSSStyle.dimensionType.PIXEL)) root.box.height = root.style.height;
                if (root.style.heightType.equals(CSSStyle.dimensionType.PERCENTAGE) && parent.box.fixedHeight) {
                    float availableHeight = parent.box.height
                            - parent.style.paddingTop - parent.style.paddingBottom
                            - root.style.marginTop - root.style.marginBottom;
                    root.box.height = root.style.height / 100.0f * availableHeight;
                    root.style.heightType = CSSStyle.dimensionType.PIXEL;
                    // This might be wrong. What if the parent does not have a fixed height, but child has a percentage.
                    // Is that even possible?
                }
            }
            
            if (root.style.width != null && root.style.display != CSSStyle.displayType.INLINE) {
                root.box.fixedWidth = true;
                if (root.style.widthType.equals(CSSStyle.dimensionType.PIXEL)) root.box.width = root.style.width;
                if (root.style.widthType.equals(CSSStyle.dimensionType.PERCENTAGE) && parent.box.fixedWidth) {
                    float availableWidth = parent.box.width
                            - parent.style.paddingLeft - parent.style.paddingRight
                            - root.style.marginLeft - root.style.marginRight;
                    root.box.width = root.style.width / 100.0f * availableWidth;
                    root.style.widthType = CSSStyle.dimensionType.PIXEL;
                }
            }
            
        }

        for (RenderNode child : root.children) {
            setBoxBounds(child);
        }

    }
    
    public void printBoxes(RenderNode root) {
        System.out.print("BoxLayoutCalculator: printing boxes\n\n");
        System.out.printf("[%d:%s] (%.2f, %.2f), (%.2f, %.2f)\n", root.id, root.type, root.box.x, root.box.y, root.box.width, root.box.height);
        for (RenderNode child : root.children) {
            printBoxes(child, "\t");
        }
        System.out.print("\n");
    }
    
    private void printBoxes(RenderNode root, String pad) {
        System.out.printf("%s[%d:%s] (%.2f, %.2f), (%.2f, %.2f), max(%.2f, %.2f)\n", pad, root.id, root.type, root.box.x, root.box.y, root.box.width, root.box.height, root.maxWidth, root.maxHeight);
        for (RenderNode child : root.children) {
            printBoxes(child, pad+"\t");
        }
    }
    
    /**
     * Take the render node tree and fill in the x, y, width, and height of the boxes
     * Then, go from top to bottom; root node is as wide as screen, but height isn't known
     * need to propagate values upwards as height gets updated.
     * Updates lastAddedChild as it works down the tree
     * 
     * @param root        The root of the render tree to use
     */
    
    public void calculateBoxes(RenderNode root) {
        RenderNode parent = parentNodeMap.get(root.id);

        if (parent != null) {
            Vector2 nextPosition = nextPosition(root, parent);
            root.box.x = nextPosition.x;
            root.box.y = nextPosition.y;
            root.positioned = true;
            propagateSize(root);
            expandIfBlockElement(root);
            lastAddedChildMap.put(parent.id, root);
        } else {
            // Position the body element
            root.box.x = root.style.marginLeft;
            root.box.y = root.style.marginTop;
        }

        /* When splitting lines, more RenderNodes can be added to a node's children, meaning
         * root.children.size() can change over the course of the loop. Have to use for loop
         * over indices to avoid concurrent modification exception.
         */
        for (int i = 0; i < root.children.size(); i++) {
            calculateBoxes(root.children.get(i));
        }
    }
    
    /**
     * Set the maximum dimensions of each node based on the maximum size of their parent, and so on.
     * Use this after using setBoxBounds, since it depends on fixed-size boxes having their width/height set.
     * @param root The render node to start on.
     */
    public void propagateMaxSizes(RenderNode root) {
        RenderNode parent = parentNodeMap.get(root.id);
        if (parent == null) {
            root.maxWidth = this.screenWidth;
            root.maxHeight = null;
        } else {
            if (root.box.fixedWidth) {
                // Percentage based width depends on parent, only if parent specifies a width
                if (root.style.widthType.equals(CSSStyle.dimensionType.PERCENTAGE) && parent.maxWidth != null) {
                    float parentAvailableWidth = parent.maxWidth - parent.style.paddingLeft - parent.style.paddingRight - root.style.marginLeft - root.style.marginRight;
                    root.maxWidth = parentAvailableWidth * root.box.width / 100f;
                } else {
                    root.maxWidth = root.box.width + root.style.marginLeft + root.style.marginRight;
                }
            } else if (parent.maxWidth != null) {
                root.maxWidth = parent.maxWidth - parent.style.paddingLeft - parent.style.paddingRight - root.style.marginLeft - root.style.marginRight;
            }

            if (root.box.fixedHeight) {
                // Percentage based height depends on parent, only if parent specifies a height
                if (root.style.heightType.equals(CSSStyle.dimensionType.PERCENTAGE) && parent.maxHeight != null) {
                    float parentAvailableHeight = parent.maxHeight - parent.style.paddingTop - parent.style.paddingBottom - root.style.marginTop - root.style.marginBottom;
                    root.maxHeight = parentAvailableHeight * root.box.width / 100f;
                } else {
                    root.maxHeight = root.box.height + root.style.marginTop + root.style.marginBottom;
                }
            } else if (parent.maxHeight != null) {
                root.maxHeight = parent.maxHeight - parent.style.paddingTop - parent.style.paddingBottom - root.style.marginTop - root.style.marginBottom;
            }
        }

        for (RenderNode child : root.children) {
            propagateMaxSizes(child);
        }

    }
    
    /**
     * When a render node's dimensions are updated, this information must be sent upwards through
     * the tree, such that the higher nodes expand to accommodate the now larger child node. For
     * nodes with percentage based sizes, if the parent gets larger, these nodes must also get 
     * larger to make sure they occupy the correct percentage. These changes only apply to nodes
     * that do not already have their width fixed.
     * @param node      The node with a newly updated dimension.
     */
    public void propagateSize(RenderNode node) {
        RenderNode parent = parentNodeMap.get(node.id);
        if (parent == null) return;

        // Find width and height by finding difference between the farthest elements, vertically and horizontally.

        RenderNode leftMost = null;
        RenderNode rightMost = null;
        RenderNode topMost = null;
        RenderNode bottomMost = null;

        for (RenderNode child : parent.children) {
            if (child.positioned) {
                if (leftMost == null || child.box.x < leftMost.box.x) leftMost = child;
                if (rightMost == null || (child.box.x + child.box.width) > (rightMost.box.x + rightMost.box.width)) rightMost = child;
                if (topMost == null || child.box.y < topMost.box.y) topMost = child;
                if (bottomMost == null || (child.box.y + child.box.height) > (bottomMost.box.y + bottomMost.box.height)) bottomMost = child;
            }
        }

        float newWidth = 0;
        float newHeight = 0;

        if (parent.children.size() > 0) {
            // Get distance between left and right-most nodes plus actual width of right-most to get width of element
            // Then add in the padding and margins on each side. Margins on the "inside" don't matter for the overall size.
            newWidth = rightMost.box.x - leftMost.box.x + rightMost.box.width +
                    parent.style.paddingLeft + parent.style.paddingRight +
                    leftMost.style.marginLeft + rightMost.style.marginRight;
            newHeight = bottomMost.box.y - topMost.box.y + bottomMost.box.height +
                    parent.style.paddingBottom + parent.style.paddingTop +
                    topMost.style.marginTop + bottomMost.style.marginBottom;
        }

        if (!parent.box.fixedWidth && newWidth > parent.box.width) {
            parent.box.width = newWidth;
        }

        if (!parent.box.fixedHeight && newHeight > parent.box.height) {
            parent.box.height = newHeight;
        }

        propagateSize(parent);
    }
    
    /**
     * Calculates the next valid position for an element, in relation to its parent and the other
     * previously added elements. Does not consider text alignment; everything is left justified
     * here and later shifted by the function applyJustification.
     * @param node
     * @param parent
     * @return
     */
    public Vector2 nextPosition(RenderNode node, RenderNode parent) {
        CSSStyle.displayType displayType = node.style.display;
        RenderNode lastAddedChild = lastAddedChildMap.get(parent.id);
        if (lastAddedChild == null) {
            float availableWidth = parent.maxWidth - (parent.style.paddingLeft + node.style.marginLeft + node.style.marginRight + parent.style.paddingRight);

            if (node.type.equals("text") && availableWidth < node.box.width) {
                textSplitter.splitTextNode(node, parent, availableWidth, availableWidth);
            }

            // If this is the first child, then it gets added in the top right of parent
            return new Vector2(
                    parent.box.x + parent.style.paddingLeft + node.style.marginLeft,
                    parent.box.y + parent.style.paddingTop + node.style.marginTop
            );

        } else {
            switch (displayType) {
                case INLINE:
                case INLINE_BLOCK:
                    // TODO handle stacking padding
                    // TODO put new lines in a horizontal row NO MATTER WHAT, text splitter handles putting them on new lines
                    // Try in-line, but if it needs more space, continue to block case
                    float x = lastAddedChild.box.x + lastAddedChild.box.width + lastAddedChild.style.marginRight + node.style.marginLeft;
                    float boundary = parent.box.x + parent.maxWidth - parent.style.paddingRight - node.style.marginRight;
                    if (x + node.box.width <= boundary) {
                        return new Vector2(x, lastAddedChild.box.y);
                    } else if (node.type.equals("text")) {
                        float availableWidth = boundary - x;
                        if (textSplitter.canBreakText(node, availableWidth)) {
                            float fullWidth = parent.maxWidth - (parent.style.paddingLeft + node.style.marginLeft + node.style.marginRight + parent.style.paddingRight);
                            boolean firstUsed = textSplitter.splitTextNode(node, parent, availableWidth, fullWidth);
                            // Place first line of text on current line if possible; fall through to block if not
                            if (firstUsed) {
                                return new Vector2(x, lastAddedChild.box.y);
                            }
                        }
                    } else {
                        // Determine if this inline element can be split in order to fit the available width. This is possible
                        // if the element contains text that can itself be split.
                        // TODO: textSplitter.canBreakNode needs to be made more flexible. It expects a single text child node
                        // and does not support nested spans.
                        float availableWidth = boundary - x;
                        if (textSplitter.canBreakNode(node, availableWidth)) {
                            float fullWidth = parent.maxWidth - (parent.style.paddingLeft + node.style.marginLeft + node.style.marginRight + parent.style.paddingRight);
                            boolean firstUsed = textSplitter.splitContainingTextNode(node, parent, availableWidth, fullWidth);
                            if (firstUsed) {
                                return new Vector2(x, lastAddedChild.box.y);
                            }
                        }
                    }
                case BLOCK:
                default:
                    // Default block elements get put to the left, and right below all the other elements. The height gets updated later.
                    // Bottom padding no longer impacts the last added child, so have to subtract that when finding the new values
                    // TODO probably other sides' padding have a stacking effect that needs to be managed
                    float bottomPaddingCorrection = parent.style.paddingBottom;
                    return new Vector2(
                            parent.box.x + parent.style.paddingLeft + node.style.marginLeft,
                            parent.box.y + parent.box.height + node.style.marginTop - bottomPaddingCorrection);
            }
        }
    }

    /**
     * Elements with display = block should expand to fill all available width. This means the new box width should
     * simply be set to the box's max width.
     * @param node  The node to expand
     */
    public void expandIfBlockElement(RenderNode node) {
        if (node.style.display != CSSStyle.displayType.BLOCK) {
            return;
        }

        node.box.width = node.maxWidth;
    }

    /**
     * Convert percentage based dimensions into raw pixels.
     * Converts margin: auto to an actual margin value.
     * @param root The render node to start on.
     */
    public void finalizeDimensions(RenderNode root) {
        RenderNode parent = parentNodeMap.get(root.id);
        if (root.style.widthType.equals(CSSStyle.dimensionType.PERCENTAGE) && parent != null) {
            root.box.fixedWidth = true;
            root.box.width = (parent.box.width * root.style.width / 100.0f)
                    - root.style.marginLeft - root.style.marginRight
                    - parent.style.paddingLeft - parent.style.paddingRight;
        }

        if (root.style.heightType.equals(CSSStyle.dimensionType.PERCENTAGE)) {
            root.box.fixedHeight = true;
            root.box.height = (parent.box.height * root.style.height / 100.0f)
                    - root.style.marginTop - root.style.marginBottom
                    - parent.style.paddingTop - parent.style.paddingBottom;
        }

        if (root.style.marginType.equals(CSSStyle.marginSizeType.AUTO) && parent != null) {
            float availableWidth = parent.box.width
                    - root.box.width
                    - parent.style.paddingLeft - parent.style.paddingRight;
            root.style.marginLeft = (int) (availableWidth / 2f);
            root.style.marginRight = (int) (availableWidth / 2f);
        } else if (root.style.marginType.equals(CSSStyle.marginSizeType.AUTO) && root.style.width != null) {
            float width = root.style.width;
            if (root.style.widthType.equals(CSSStyle.dimensionType.PERCENTAGE)) {
                width = (float) (screenWidth * width / 100.0);
            }
            if (width > root.style.maxWidth) {
                width = root.style.maxWidth;
            }
            float availableWidth = screenWidth - width;
            root.style.marginLeft = (int) (availableWidth / 2f);
            root.style.marginRight = (int) (availableWidth / 2f);
        }

        for (RenderNode child : root.children) {
            finalizeDimensions(child);
        }
    }
    
    /**
     * When justifying content, the rows of nodes will be shifted by the same value horizontally.
     * This this value varyings between rows, this function looks at consecutive render nodes
     * that would be "pushed" if the previous node was moved horizontally, and adds all of those
     * into a row.
     * @param root
     * @return
     */
    public List<List<RenderNode>> getChildRows(RenderNode root) {
        List<List<RenderNode>> rows = new ArrayList<>();
        List<RenderNode> currentRow = new ArrayList<RenderNode>();
        RenderNode lastAddedNode = null;
        
        for (RenderNode child : root.children) {
            if (lastAddedNode == null) {
                currentRow.add(child);
            } else {
                // Check if current node overlaps vertically with previous node
                // TODO should margins be included in these vertical positions?
                float prevTop = lastAddedNode.box.y;
                float prevBottom = lastAddedNode.box.y + lastAddedNode.box.height;
                float currTop = child.box.y;
                float currBottom = child.box.y + child.box.height;
                if (prevBottom < currTop || prevTop > currBottom) {
                    rows.add(currentRow);
                    currentRow = new ArrayList<RenderNode>();
                    currentRow.add(child);
                } else {
                    currentRow.add(child);
                }
            }
            
            lastAddedNode = child;
        }
        
        rows.add(currentRow);
        
        return rows;
    }
    
    /**
     * Given a render node root with some text alignment justification and a subset of its 
     * children, find the amount to shift those children so that they obey the alignment.
     * This doesn't use root's actual set of children because they may be arranged such that
     * they all require different shifts. If its a vertical stack with different widths for each
     * node, each would need to be considered separately if we wanted to center them.
     * @param root
     * @param children
     * @return
     */
    public float calculateJustificationOffset(RenderNode root, List<RenderNode> children) {
        RenderNode leftMost = null;
        RenderNode rightMost = null;
        
        for (RenderNode child : children) {
            if (leftMost == null || child.box.x < leftMost.box.x) leftMost = child;
            if (rightMost == null || child.box.x > rightMost.box.x) rightMost = child;
        }
        
        float leftSpace = 0;
        float rightSpace = 0;
        
        float parentWidth = root.box.width;
        float parentX = root.box.x;
        int parentPaddingLeft = root.style.paddingLeft;
        int parentPaddingRight = root.style.paddingRight;
        
        if (leftMost != null && rightMost != null) {
            leftSpace = (leftMost.box.x - leftMost.style.marginLeft) - (parentX + parentPaddingLeft);
            rightSpace =  (parentX + parentWidth - parentPaddingRight) - (rightMost.box.x + rightMost.box.width + rightMost.style.marginRight);
        }
        
        float xShift = 0;
        
        if (root.style.textAlign.equals(CSSStyle.textAlignType.CENTER)) {
            xShift = (leftSpace + rightSpace) / 2.0f;
        } else if (root.style.textAlign.equals(CSSStyle.textAlignType.RIGHT)) {
            xShift = leftSpace + rightSpace;
        }
        
        return xShift;
    }
    
    /**
     * Apply shifts for the text-Align CSS property. Since this property doesn't actually change
     * the sizes of containing elements, it can be applied after the boxes are calculated and
     * work just fine. The logic here is to find the left most and right most elements and check
     * what is the most that they can be moved left and right respectively. We then shift every
     * child element by that amount, in the specified direction.
     * 
     * Needs to find the shift required to center each row of nodes, they will all require a different
     * shift to be centered in the parent.
     * 
     * TODO this is a costly function, should improve the runtime
     * @param root
     */
    public void applyJustification(RenderNode root) {
        CSSStyle.textAlignType alignment = root.style.textAlign;
        if (!alignment.equals(CSSStyle.textAlignType.LEFT) && root.children.size() > 0) {

            List<List<RenderNode>> rows = getChildRows(root);
            for (List<RenderNode> children : rows) {
                float xShift = calculateJustificationOffset(root, children);
                if (xShift > 0) {
                    for (RenderNode child : children) {
                        applyShift(child, xShift, 0f);
                    }
                }
            }

        }

        for (RenderNode child : root.children) {
            applyJustification(child);
        }

    }
    
    /**
     * Shifts every element in the root including the root by x and y.
     * @param root
     * @param x
     * @param y
     */
    public void applyShift(RenderNode root, float x, float y) {
        root.box.x += x;
        root.box.y += y;
        for (RenderNode child : root.children) {
            applyShift(child, x, y);
        }
    }
    
    /**
     * Find each table, and enlarge the sizes of each cell so that the columns line up.
     * @param root
     */
    public void setTableCellWidths(RenderNode root) {
        if (root.type.equals(HTMLElements.TABLE)) {
            // TODO check for thead, tbody, tfoot
            List<RenderNode> rows = root.getElementsInChildren(HTMLElements.TR);
            
            int maxColumns = 0;
            for (RenderNode row : rows) {
                int columns = row.getElementsInChildren(HTMLElements.TD).size();
                if (columns > maxColumns) maxColumns = columns;
            }
            
            Float[] widths = new Float[maxColumns];
            
            for (RenderNode row : rows) {
                List<RenderNode> cols = row.getElementsInChildren(HTMLElements.TD);
                for (int i = 0; i < maxColumns; i++) {
                    if (i < cols.size() && (widths[i] == null || widths[i] < cols.get(i).box.width)) {
                        widths[i] = cols.get(i).box.width;
                    }
                }
            }

            for (RenderNode row : rows) {
                List<RenderNode> cols = row.getElementsInChildren(HTMLElements.TD);
                for (int i = 0; i < cols.size(); i++) {
                    RenderNode cell = cols.get(i);
                    cell.box.width = widths[i] + cell.style.marginLeft + cell.style.marginRight;
                    cell.box.fixedWidth = true;
                }
            }
            
        } else {
            for (RenderNode child : root.children) {
                setTableCellWidths(child);
            }
        }
    }

}
