package browser.layout;

import static browser.css.CSSStyle.DisplayType;

import java.util.*;

import browser.constants.CSSConstants;
import browser.css.CSSStyle;
import browser.model.BoxNode;
import browser.model.RenderNode;
import browser.parser.HTMLElements;
import browser.parser.StringUtils;

public class BoxTreeGenerator {

    private final Map<Integer, BoxNode> renderNodeIdToBoxNode = new HashMap<>();
    private BoxNode rootBoxNode;

    // Public methods

    public BoxNode generate(RenderNode rootRenderNode) {
        renderNodeIdToBoxNode.clear();

        List<RenderNode> renderNodeList = new ArrayList<>();
        List<BoxNode> parentBoxNodeList = new ArrayList<>();
        renderNodeList.add(rootRenderNode);
        parentBoxNodeList.add(null);

        while (!renderNodeList.isEmpty()) {
            RenderNode renderNode = renderNodeList.remove(0);
            BoxNode parentBoxNode = parentBoxNodeList.remove(0);
            BoxNode boxNode = createBoxNode(renderNode, parentBoxNode);
            renderNodeIdToBoxNode.put(renderNode.id, boxNode);

            if (parentBoxNode == null) {
                rootBoxNode = boxNode;
            }

            for (RenderNode childRenderNode : renderNode.children) {
                renderNodeList.add(childRenderNode);
                parentBoxNodeList.add(boxNode);
            }
        }

        separatePreformattedTextLines(rootBoxNode, false);
        setDisplaysForUnknownElements(rootBoxNode);
        addAnonymousFlowBoxes(rootBoxNode);
        addAnonymousTableBoxes(rootBoxNode);

        return rootBoxNode;
    }

    public BoxNode getBoxNodeForRenderNodeId(int id) {
        return renderNodeIdToBoxNode.get(id);
    }

    // Private methods

    private BoxNode createBoxNode(RenderNode renderNode, BoxNode parentBoxNode) {
        BoxNode boxNode = new BoxNode();
        boxNode.id = BoxNode.nextId++;
        boxNode.renderNodeId = renderNode.id;
        renderNode.boxNode = boxNode;
        boxNode.correspondingRenderNode = renderNode;
        boxNode.style = renderNode.style.deepCopy();
        boxNode.parent = parentBoxNode;
        if (parentBoxNode != null) {
            parentBoxNode.children.add(boxNode);
        }
        boxNode.outerDisplayType = renderNode.style.outerDisplay;
        boxNode.innerDisplayType = renderNode.style.innerDisplay;
        boxNode.auxiliaryDisplayType = renderNode.style.auxiliaryDisplay;
        boxNode.isTextNode = renderNode.type.equals(HTMLElements.TEXT);
        boxNode.isPseudo = HTMLElements.isPseudoElement(renderNode.type);
        boxNode.whiteSpaceAfter = renderNode.whiteSpaceAfter;

        if (boxNode.isTextNode) {
            String text = boxNode.correspondingRenderNode.text;
            boxNode.isAnonymous = true;
            boxNode.textStartIndex = 0;
            boxNode.textEndIndex = text == null ? 0 : text.length();
        }

        return boxNode;
    }

    private void addAnonymousFlowBoxes(BoxNode rootBoxNode) {
        if (rootBoxNode.children.size() == 0) {
            return;
        }

        List<BoxNode> queue = new ArrayList<>();
        queue.add(rootBoxNode);

        while (!queue.isEmpty()) {
            BoxNode boxNode = queue.remove(0);
            boolean validDisplayConfiguration = boxHasValidDisplayConfiguration(boxNode);
            if (!validDisplayConfiguration) {
                addAnonymousBlockBoxes(boxNode);

                // Adding anonymous boxes may have invalidated the box consistency in the parent box. Reset the queue
                // so that the parent is reprocessed.
                if (boxNode.parent != null) {
                    List<BoxNode> newQueueContent = queue.stream()
                            .filter(b -> !b.isDescendantOf(boxNode.parent.id)).toList();
                    queue.clear();
                    queue.add(0, boxNode.parent);
                    queue.addAll(newQueueContent);
                }
            }
            queue.addAll(boxNode.children);
        }
    }

    private void addAnonymousTableBoxes(BoxNode rootBoxNode) {
        TableAnonymousBoxAdder anonymousBoxAdder = new TableAnonymousBoxAdder();
        anonymousBoxAdder.addAnonymousBoxes(rootBoxNode);
    }

    private void addAnonymousBlockBoxes(BoxNode boxNode) {
        BoxNode baseBoxNode = boxNode;

        // If the root box is inline, it needs to be wrapped in an anonymous block box.
        if (boxNode.outerDisplayType.equals(DisplayType.INLINE)) {
            baseBoxNode = wrapInlineElementWithAnonymousBlockBox(boxNode);
        }

        List<BoxNode> currentInlineBoxes = new ArrayList<>();
        List<BoxNode> newChildren = new ArrayList<>();

        for (int i = 0; i < baseBoxNode.children.size(); i++) {
            BoxNode childNode = baseBoxNode.children.get(i);
            if (childNode.isPseudo) {
                newChildren.add(childNode);
            } else if (childNode.outerDisplayType.equals(DisplayType.BLOCK) || i == baseBoxNode.children.size() - 1) {
                // On the last iteration, add an inline child node to the list, so it can be wrapped in an anonymous block.
                if (i == baseBoxNode.children.size() - 1 && childNode.outerDisplayType.equals(DisplayType.INLINE)) {
                    currentInlineBoxes.add(childNode);
                }

                if (currentInlineBoxes.size() > 0) {
                    BoxNode anonymousBox = new BoxNode();
                    anonymousBox.id = BoxNode.nextId++;
                    anonymousBox.outerDisplayType = DisplayType.BLOCK;
                    anonymousBox.innerDisplayType = DisplayType.FLOW;
                    anonymousBox.isAnonymous = true;
                    anonymousBox.style = baseBoxNode.style.deepCopy();

                    for (BoxNode inlineBox : currentInlineBoxes) {
                        inlineBox.parent = anonymousBox;
                        anonymousBox.children.add(inlineBox);
                    }
                    currentInlineBoxes.clear();
                    newChildren.add(anonymousBox);
                }

                if (childNode.outerDisplayType.equals(DisplayType.BLOCK)) {
                    newChildren.add(childNode);
                }
            } else if (childNode.outerDisplayType.equals(DisplayType.INLINE)) {
                currentInlineBoxes.add(childNode);
            } else {
                // TODO: what if its neither inline nor block?
                newChildren.add(childNode);
            }
        }

        for (BoxNode child : newChildren) {
            child.parent = baseBoxNode;
        }
        baseBoxNode.children = newChildren;
    }

    private BoxNode wrapInlineElementWithAnonymousBlockBox(BoxNode inlineBox) {
        BoxNode containingAnonymousBox = new BoxNode();
        containingAnonymousBox.id = BoxNode.nextId++;
        containingAnonymousBox.outerDisplayType = DisplayType.BLOCK;
        containingAnonymousBox.innerDisplayType = DisplayType.FLOW;
        containingAnonymousBox.parent = inlineBox.parent;
        containingAnonymousBox.isAnonymous = true;
        // TODO should this actually be the inline box's style? Is that style lost?
        containingAnonymousBox.style = inlineBox.parent == null ? new CSSStyle() : inlineBox.parent.style.deepCopy();

        // Add the anonymous containing box to the parent's children, and remove the inline box.
        BoxNode parent = inlineBox.parent;
        if (parent == null) {
            // If the parent is null, then the base box node is the root box node. Since the parent of this node
            // is being changed, the root itself is updated to the containing anonymous box, since this now
            // contains the previous root box node.
            rootBoxNode = containingAnonymousBox;
        } else {
            int indexInParent = parent.children.indexOf(inlineBox);
            parent.children.add(indexInParent, containingAnonymousBox);
            parent.children.remove(inlineBox);
        }

        // Add the inline box's children to the new containing box, cutting the inline box out of the box tree.
        containingAnonymousBox.children.addAll(inlineBox.children);
        for (BoxNode child : inlineBox.children) {
            child.parent = containingAnonymousBox;
        }

        return containingAnonymousBox;
    }

    private boolean boxHasValidDisplayConfiguration(BoxNode boxNode) {
        if (CSSConstants.tableInnerDisplayTypes.contains(boxNode.innerDisplayType) && !boxNode.innerDisplayType.equals(DisplayType.TABLE_CELL)) {
            // Table boxes do not abide by the flow display types. Only table cells may use flow layout children.
            return true;
        }

        List<BoxNode> blockChildren = boxNode.children.stream().filter(node -> !node.isTextNode && !node.isPseudo).filter(node -> node.outerDisplayType.equals(DisplayType.BLOCK)).toList();
        List<BoxNode> inlineChildren = boxNode.children.stream().filter(node -> !node.isTextNode && !node.isPseudo).filter(node -> node.outerDisplayType != null && node.outerDisplayType.equals(DisplayType.INLINE)).toList();
        List<BoxNode> textChildren = boxNode.children.stream().filter(node -> node.isTextNode).toList();

        if (boxNode.outerDisplayType != null && boxNode.outerDisplayType.equals(DisplayType.BLOCK)) {
            if (blockChildren.size() > 0 && (inlineChildren.size() > 0 || textChildren.size() > 0)) {
                // The block level element contains both inline (or text) and block elements.
                return false;
            }
        } else if (boxNode.outerDisplayType != null && boxNode.outerDisplayType.equals(DisplayType.INLINE) && !boxNode.innerDisplayType.equals(DisplayType.FLOW_ROOT)) {
            if (blockChildren.size() > 0) {
                // The inline level element contains block level elements.
                return false;
            }
        }

        return true;
    }

    /**
     * Inner and outer displays are required for adding anonymous boxes and for later computing layouts. If a box did
     * not have any display information from the page's styling, it will default to a block box.
     * @param boxNode
     */
    private void setDisplaysForUnknownElements(BoxNode boxNode) {
        if (boxNode.innerDisplayType == null) {
            boxNode.innerDisplayType = DisplayType.FLOW;
            System.out.printf("Setting default inner display for [%s] %s\n", boxNode.correspondingRenderNode.type, boxNode);
        }

        if (boxNode.outerDisplayType == null) {
            boxNode.outerDisplayType = DisplayType.BLOCK;
            System.out.printf("Setting default outer display for [%s] %s\n", boxNode.correspondingRenderNode.type, boxNode);
        }

        for (BoxNode child : boxNode.children) {
            setDisplaysForUnknownElements(child);
        }
    }

    private void separatePreformattedTextLines(BoxNode boxNode, boolean inPre) {
        if (inPre && boxNode.isTextNode) {
            List<BoxNode> newTextBoxes = new ArrayList<>();
            String text = boxNode.correspondingRenderNode.text.substring(boxNode.textStartIndex, boxNode.textEndIndex);
            String[] lines = text.split("\\r?\\n");
            if (lines.length > 1) {
                int index = 0;
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    if (!line.isEmpty()) {
                        BoxNode textLineBox = new BoxNode(boxNode);
                        textLineBox.isAnonymous = true;
                        textLineBox.isTextNode = true;
                        textLineBox.textStartIndex = index;
                        textLineBox.textEndIndex = index + line.length();
                        newTextBoxes.add(textLineBox);
                    }

                    if (i < lines.length - 1) {
                        RenderNode lineBreakRenderNode = new RenderNode(HTMLElements.BR);
                        lineBreakRenderNode.id = RenderNode.nextId++;

                        BoxNode lineBreakBox = new BoxNode(boxNode);
                        lineBreakBox.isAnonymous = true;
                        lineBreakBox.isTextNode = false;
                        lineBreakBox.textStartIndex = 0;
                        lineBreakBox.textEndIndex = 0;
                        lineBreakBox.correspondingRenderNode = lineBreakRenderNode;
                        lineBreakBox.renderNodeId = lineBreakRenderNode.id;

                        newTextBoxes.add(lineBreakBox);
                    }

                    int newlineLength = 1;
                    if (StringUtils.substringMatch(text, "\r\n", index + line.length())) {
                        newlineLength = 2;
                    }

                    index += line.length() + newlineLength;
                }

                // Remove the previous text box, and insert the new lines instead.
                int indexInParent = boxNode.parent.children.indexOf(boxNode);
                boxNode.parent.children.remove(indexInParent);
                boxNode.parent.children.addAll(indexInParent, newTextBoxes);
            }
        } else if (boxNode.correspondingRenderNode.type.equals(HTMLElements.PRE)) {
            inPre = true;
        }

        for (int i = 0; i < boxNode.children.size(); i++) {
            separatePreformattedTextLines(boxNode.children.get(i), inPre);
        }
    }
}
