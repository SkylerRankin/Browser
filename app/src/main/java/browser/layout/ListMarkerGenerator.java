package browser.layout;

import java.util.List;

import browser.constants.PseudoElementConstants;
import browser.css.CSSStyle;
import browser.model.BoxNode;
import browser.model.CSSRulePrecedent;
import browser.model.RenderNode;
import browser.model.Vector2;
import browser.parser.HTMLElements;

public class ListMarkerGenerator {

    private static final String bulletPointUnicode = "\u2022";
    private static final TextDimensionCalculator textDimensionCalculator = new TextDimensionCalculator();

    /**
     * Since list markers are not present in the original markup, render nodes are added into the tree. Each list item
     * has its corresponding marker added as its next sibling. No styling is computed here since styles have not yet
     * been calculated.
     * @param renderNode        The render node to add markers to.
     */
    public static void addMarkers(RenderNode renderNode) {
        if (renderNode.style.auxiliaryDisplay != null && renderNode.style.auxiliaryDisplay.equals(CSSStyle.DisplayType.LIST_ITEM)) {
            RenderNode marker = new RenderNode(HTMLElements.PSEUDO_MARKER);
            // TODO Set the marker display properties from the user agent CSS.
            marker.style.outerDisplay = CSSStyle.DisplayType.BLOCK;
            marker.style.innerDisplay = CSSStyle.DisplayType.FLOW;
            // Apply the font size property rather than setting the value directly. Using apply sets the precedent
            // value correctly so that the property is not inherited later.
//            marker.style.apply("font-size", String.valueOf(renderNode.style.fontSize), CSSRulePrecedent.ID());
            if (renderNode.parent.type.equals(HTMLElements.OL)) {
                List<RenderNode> nonMarkerChildren = renderNode.parent.children.stream().filter(node -> !node.type.equals(HTMLElements.PSEUDO_MARKER)).toList();
                int indexInParent = nonMarkerChildren.indexOf(renderNode);
                marker.properties.put(PseudoElementConstants.MARKER_INDEX_KEY, indexInParent);
            }
            int indexInParent = renderNode.parent.children.indexOf(renderNode);
            renderNode.parent.children.add(indexInParent + 1, marker);
            marker.parent = renderNode.parent;
        }

        for (int i = 0; i < renderNode.children.size(); i++) {
            RenderNode child = renderNode.children.get(i);
            if (!HTMLElements.isPseudoElement(child.type)) {
                addMarkers(child);
            }
        }
    }

    /**
     * Markers can appear as bullets, numbers, or other text/images based on their CSS. After the render tree is
     * created and CSS is computed on it, this method sets the styles and text for each list marker in the tree.
     * @param renderNode        The render node to process.
     */
    public static void setMarkerStyles(RenderNode renderNode) {
        if (renderNode.type.equals(HTMLElements.PSEUDO_MARKER)) {
            boolean insideOrderedList = renderNode.parent.type.equals(HTMLElements.OL);
            String markerText = bulletPointUnicode;
            if (insideOrderedList && renderNode.properties.containsKey(PseudoElementConstants.MARKER_INDEX_KEY)) {
                markerText = String.format("%d.", (int) renderNode.properties.get(PseudoElementConstants.MARKER_INDEX_KEY) + 1);
            }
            // TODO marker should have same font styling as its corresponding list item.
            // TODO support marker style types, as set by the list-style-type CSS property.
            renderNode.text = markerText;
        }

        for (RenderNode child : renderNode.children) {
            setMarkerStyles(child);
        }
    }

    public static void setMarkerSize(BoxNode boxNode) {
        Vector2 dimensions = textDimensionCalculator.getDimension(boxNode.correspondingRenderNode.text, boxNode.style);
        boxNode.width = dimensions.x;
        boxNode.height = dimensions.y;
    }

}
