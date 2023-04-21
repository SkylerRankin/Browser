package browser.layout;

import static browser.css.CSSStyle.DisplayType;

import java.util.*;

import browser.constants.CSSConstants;
import browser.model.BoxNode;

public class TableAnonymousBoxAdder {

    // Maps a set of box inner displays to the set of valid parent types for that box.
    private final static Map<Set<DisplayType>, Set<DisplayType>> requiredParentType = Map.of(
            Set.of(DisplayType.TABLE_CELL), Set.of(DisplayType.TABLE_ROW),
            Set.of(DisplayType.TABLE_ROW), Set.of(DisplayType.TABLE, DisplayType.INLINE_TABLE, DisplayType.TABLE_HEADER_GROUP, DisplayType.TABLE_FOOTER_GROUP, DisplayType.TABLE_ROW_GROUP),
            Set.of(DisplayType.TABLE_COLUMN), Set.of(DisplayType.TABLE, DisplayType.INLINE_TABLE, DisplayType.TABLE_COLUMN_GROUP),
            Set.of(DisplayType.TABLE_ROW_GROUP, DisplayType.TABLE_HEADER_GROUP, DisplayType.TABLE_FOOTER_GROUP, DisplayType.TABLE_COLUMN_GROUP, DisplayType.TABLE_CAPTION), Set.of(DisplayType.TABLE, DisplayType.INLINE_TABLE)
    );

    // Maps a set of inner displays to the type of anonymous box to be added as the box's parent, given its parent does
    // not have a valid type, as specified by the requiredParentType map.
    private final static Map<Set<DisplayType>, DisplayType> addedAnonymousParentType = Map.of(
            Set.of(DisplayType.TABLE_CELL), DisplayType.TABLE_ROW,
            Set.of(DisplayType.TABLE_ROW), DisplayType.TABLE,
            Set.of(DisplayType.TABLE_COLUMN), DisplayType.TABLE,
            Set.of(DisplayType.TABLE_ROW_GROUP, DisplayType.TABLE_HEADER_GROUP, DisplayType.TABLE_FOOTER_GROUP, DisplayType.TABLE_COLUMN_GROUP, DisplayType.TABLE_CAPTION), DisplayType.TABLE
    );

    // Maps a set of box inner displays to the set of valid children types for that box.
    private final static Map<Set<DisplayType>, Set<DisplayType>> requiredChildType = Map.of(
            Set.of(DisplayType.TABLE, DisplayType.INLINE_TABLE), Set.of(DisplayType.TABLE_ROW_GROUP, DisplayType.TABLE_HEADER_GROUP, DisplayType.TABLE_FOOTER_GROUP, DisplayType.TABLE_CAPTION, DisplayType.TABLE_COLUMN, DisplayType.TABLE_COLUMN_GROUP, DisplayType.TABLE_ROW),
            Set.of(DisplayType.TABLE_ROW_GROUP, DisplayType.TABLE_HEADER_GROUP, DisplayType.TABLE_FOOTER_GROUP), Set.of(DisplayType.TABLE_ROW),
            Set.of(DisplayType.TABLE_ROW), Set.of(DisplayType.TABLE_CELL)
    );

    // Maps a set of inner displays to the type of anonymous box to be added as the box's parent, given the box does not
    // have a valid display type given its parent, as specified by the requiredChildType map.
    private final static Map<Set<DisplayType>, DisplayType> addedAnonymousChildType = Map.of(
            Set.of(DisplayType.TABLE, DisplayType.INLINE_TABLE), DisplayType.TABLE_ROW,
            Set.of(DisplayType.TABLE_ROW_GROUP, DisplayType.TABLE_HEADER_GROUP, DisplayType.TABLE_FOOTER_GROUP), DisplayType.TABLE_ROW,
            Set.of(DisplayType.TABLE_ROW), DisplayType.TABLE_CELL
    );

    private final static Set<DisplayType> validRootTableTypes = Set.of(DisplayType.TABLE, DisplayType.INLINE_TABLE);

    // Public methods

    /**
     * Tables use a set of table-related inner display types to control formatting. These types must be in a specific
     * hierarchy, and therefore missing intermediate boxes must be added as anonymous boxes with the correct inner
     * display type.
     *
     * @param boxNode       The box node to add anonymous boxes to.
     */
    public void addAnonymousBoxes(BoxNode boxNode) {
        List<BoxNode> queue = new ArrayList<>();
        queue.add(boxNode);

        while (!queue.isEmpty()) {
            BoxNode currentBoxNode = queue.remove(0);
            boolean isTableBox = CSSConstants.tableInnerDisplayTypes.contains(currentBoxNode.innerDisplayType);
            boolean validParentType = hasValidParentType(currentBoxNode);
            boolean validChildType = isValidChildType(currentBoxNode);

            if (isTableBox && (!validParentType || !validChildType)) {
                Map.Entry<Set<DisplayType>, DisplayType> addedParentEntry =
                        (!validParentType ? addedAnonymousParentType : addedAnonymousChildType).entrySet().stream()
                        .filter(e -> e.getKey().contains(currentBoxNode.innerDisplayType)).toList().get(0);
                DisplayType anonymousParentType = addedParentEntry.getValue();
                // If an anonymous table box is being added within an inline box, the added box should be an inline table.
                if (anonymousParentType.equals(DisplayType.TABLE) && currentBoxNode.parent != null && currentBoxNode.parent.outerDisplayType.equals(DisplayType.INLINE)) {
                    anonymousParentType = DisplayType.INLINE_TABLE;
                }
                replaceWithAnonymousBox(currentBoxNode, anonymousParentType, addedParentEntry.getKey());
                removeDescendantsOfBoxFromQueue(currentBoxNode.parent, queue);
                queue.add(currentBoxNode.parent);
            } else {
                queue.addAll(currentBoxNode.children);
            }
        }
    }

    // Private methods

    /**
     * Inserts a new anonymous box in the place of a given box. The new anonymous box will have the given inner display
     * type, and will take the given box as well as any other siblings that have an inner display type in the given list
     * as children. The siblings must be consecutive with the original box.
     * @param boxNode       The box to replace.
     * @param type      The inner display of the anonymous box.
     * @param addedSiblingTypes     The types of consecutive sibling boxes to remove as children of the anonymous box.
     */
    private void replaceWithAnonymousBox(BoxNode boxNode, DisplayType type, Set<DisplayType> addedSiblingTypes) {
        BoxNode anonymousBox = new BoxNode();
        anonymousBox.isAnonymous = true;
        anonymousBox.outerDisplayType = DisplayType.BLOCK;
        anonymousBox.innerDisplayType = type;
        anonymousBox.id = BoxNode.nextId++;
        BoxNode originalParent = boxNode.parent;
        int originalIndexInParent = boxNode.parent == null ? 0 : boxNode.parent.children.indexOf(boxNode);
        List<BoxNode> newChildren = new ArrayList<>();
        if (boxNode.parent == null) {
            boxNode.parent = anonymousBox;
            newChildren.add(boxNode);
        } else {
            anonymousBox.style = boxNode.parent.style.deepCopy();
            int indexInParent = boxNode.parent.children.indexOf(boxNode);
            for (int i = indexInParent; i < boxNode.parent.children.size(); i++) {
                BoxNode sibling = boxNode.parent.children.get(i);
                if (addedSiblingTypes.contains(sibling.innerDisplayType)) {
                    newChildren.add(sibling);
                } else {
                    break;
                }
            }
            for (BoxNode child : newChildren) {
                boxNode.parent.children.remove(child);
                child.parent = anonymousBox;
            }
        }

        anonymousBox.children = newChildren;
        anonymousBox.parent = originalParent;
        if (originalParent != null) {
            originalParent.children.add(originalIndexInParent, anonymousBox);
        }
    }

    /**
     * Checks if a given box has a valid parent display type within the table context. The following relationships
     * are checks for:
     * - table-cell is within a table-row
     * - table-row is within a table, inline-table, table-header-group, table-footer-group, or table-row-group
     * - table-column is within a table, inline-table, or table-column-group
     * - table-row-group is within a table-header-group, table-footer-group, table-column-group, table-caption, table,
     *   or inline-table
     * @param boxNode       The box node to check.
     * @return      True iff the box's parent has the right relationship to the box.
     */
    private boolean hasValidParentType(BoxNode boxNode) {
        if (boxNode.innerDisplayType.equals(DisplayType.TABLE)) {
            return true;
        } else if (boxNode.parent == null) {
            return validRootTableTypes.contains(boxNode.innerDisplayType);
        }
        Optional<Set<DisplayType>> requiredParentTypeOptional = requiredParentType.entrySet().stream()
                .filter(e -> e.getKey().contains(boxNode.innerDisplayType)).map(Map.Entry::getValue).findFirst();
        if (requiredParentTypeOptional.isPresent()) {
            Set<DisplayType> allowedParentTypes = requiredParentTypeOptional.get();
            return allowedParentTypes.contains(boxNode.parent.innerDisplayType);
        }
        return false;
    }

    /**
     * Checks if a given box has a valid inner display type given its parent's inner display type in the table context.
     * The following relationships are checked for:
     * - table and inline-table must contain table-row-group, table-header-group, table-footer-group, table-caption,
     *   table-column, table-column-group, or table-row children only.
     * - table-row-group, table-header-group, and table-footer-group must contain table-row children only.
     * - table-row must contain table-cell children only.
     * @param boxNode       The box node to check.
     * @return      True iff the box has a valid relationship given its parent.
     */
    private boolean isValidChildType(BoxNode boxNode) {
        if (boxNode.innerDisplayType.equals(DisplayType.TABLE)) {
            return true;
        } else if (boxNode.parent == null) {
            return validRootTableTypes.contains(boxNode.innerDisplayType);
        }
        Optional<Set<DisplayType>> requiredChildTypeOptional = requiredChildType.entrySet().stream()
                .filter(e -> e.getKey().contains(boxNode.parent.innerDisplayType)).map(Map.Entry::getValue).findFirst();
        if (requiredChildTypeOptional.isPresent()) {
            Set<DisplayType> allowedChildTypes = requiredChildTypeOptional.get();
            return allowedChildTypes.contains(boxNode.innerDisplayType);
        }
        return false;
    }

    private void removeDescendantsOfBoxFromQueue(BoxNode rootBoxNode, List<BoxNode> queue) {
        for (int i = queue.size() - 1; i >= 0; i--) {
            if (queue.get(i).isDescendantOf(rootBoxNode.id)) {
                queue.remove(i);
            }
        }
    }
}
