package browser.layout;

import static browser.css.CSSStyle.DisplayType.*;

import java.util.*;

import browser.css.CSSStyle;
import browser.model.BoxNode;
import browser.model.IntVector2;
import browser.model.TableCell;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TableLayoutFormatter {

    private final BoxLayoutGenerator boxLayoutGenerator;

    // Public methods

    public void placeBox(BoxNode boxNode, TableFormattingContext context) {
        switch (boxNode.innerDisplayType) {
            case TABLE_CAPTION -> { placeCaption(boxNode, context); }
            case TABLE_HEADER_GROUP, TABLE_ROW_GROUP, TABLE_FOOTER_GROUP -> { placeRowGroup(boxNode, context); }
            case TABLE_ROW -> { placeRow(boxNode, context); }
            case TABLE_CELL -> { placeCell(boxNode, context); }
        }
    }

    /**
     * Sets the widths of the table box well as each column within the table. Column and row information is stored
     * in the table formatting context.
     * @param boxNode     The table box node.
     * @param availableWidth        The available width of the parent box.
     * @param context       The formatting context for the table box.
     */
    public void setTableWidths(BoxNode boxNode, float availableWidth, TableFormattingContext context) {
        if (!boxNode.innerDisplayType.equals(CSSStyle.DisplayType.TABLE)) {
            return;
        }

        // Save the caption, if any, in the context.
        updateContextWithCaption(boxNode, context);
        // Organize the cells into rows.
        initializeContext(boxNode, availableWidth, context);
        // Set the widths of each cell, column, and the table.
        calculateCellWidths(context);
        setHorizontalWidths(context);
        // Set the heights of each cell, column, and the table.
        calculateCellHeights(context);
        setVerticalHeights(context);
        setTableBoxSizes(context);
    }

    public float getHeightFromChildren(BoxNode boxNode, TableFormattingContext context) {
        if (boxNode.children.size() == 0) {
            return boxNode.height == null ? 0 : boxNode.height;
        }

        // TODO calculate cell, row group, and table sizes based on the context as well, not based on children sizes.
        if (boxNode.innerDisplayType.equals(TABLE_ROW)) {
            int rowIndex = boxNode.parent.children.indexOf(boxNode);
            return context.rowHeights.get(rowIndex);
        }

        float maxY = 0;
        for (BoxNode childBox : boxNode.children) {
            float childHeight = childBox.height == null ? 0 : childBox.height;
            float newY = childBox.y + childHeight;
            maxY = Math.max(maxY, newY);
        }

        // The table box needs to account for the bottom border spacing.
        float bottomSpacing = boxNode.innerDisplayType.equals(TABLE) ? context.borderSpacing.y : 0;
        return maxY - boxNode.y + bottomSpacing;
    }

    // Private methods

    /**
     * Places a table caption box. There can be multiple captions, and all are stacked vertically at the top of the
     * table. Their width is set to the full width of the table.
     * @param boxNode       The caption box.
     * @param context       The table formatting context.
     */
    private void placeCaption(BoxNode boxNode, TableFormattingContext context) {
        boxNode.width = context.tableBoxNode.width;
        boxNode.x = context.tableBoxNode.x + context.tableBoxNode.style.paddingLeft + boxNode.style.borderWidthLeft;

        if (context.lastPlacedCaption == null) {
            boxNode.y = context.tableBoxNode.y + context.tableBoxNode.style.paddingTop + boxNode.style.borderWidthTop;
        } else {
            boxNode.y = context.lastPlacedCaption.y + context.lastPlacedCaption.style.marginBottom + boxNode.style.borderWidthTop;
        }
        context.lastPlacedCaption = boxNode;
    }

    /**
     * Places a header, row, or footer group. These are stacked vertically beneath any caption boxes.
     * @param boxNode       The caption box.
     * @param context       The table formatting context.
     */
    private void placeRowGroup(BoxNode boxNode, TableFormattingContext context) {
        boxNode.width = context.tableBoxNode.width - context.borderSpacing.x * 2;
        boxNode.x = context.tableBoxNode.x + context.tableBoxNode.style.paddingLeft + boxNode.style.borderWidthLeft + context.borderSpacing.x;

        if (context.lastPlacedRowGroup == null) {
            if (context.lastPlacedCaption == null) {
                boxNode.y = context.tableBoxNode.y + context.tableBoxNode.style.paddingTop + boxNode.style.borderWidthTop + context.borderSpacing.y;
            } else {
                boxNode.y = context.lastPlacedCaption.y + context.lastPlacedCaption.style.marginBottom + boxNode.style.borderWidthTop;
            }
        } else {
            boxNode.y = context.lastPlacedRowGroup.y + context.lastPlacedRowGroup.style.marginBottom + boxNode.style.borderWidthTop;
        }

        context.lastPlacedRowGroup = boxNode;
    }

    /**
     * Places a row. These are stacked vertically beneath any caption box, and within any row groups.
     * @param boxNode       The caption box.
     * @param context       The table formatting context.
     */
    private void placeRow(BoxNode boxNode, TableFormattingContext context) {
        if (context.lastPlacedRowGroup == null) {
            boxNode.x = context.tableBoxNode.x + boxNode.style.borderWidthLeft + context.borderSpacing.x;
        } else {
            boxNode.x = context.lastPlacedRowGroup.x + boxNode.style.borderWidthLeft;
        }

        if (context.lastPlacedRow == null) {
            if (context.lastPlacedRowGroup == null) {
                // No row group for this row.
                if (context.lastPlacedCaption == null) {
                    // Place at top of table
                    boxNode.y = context.tableBoxNode.y + context.tableBoxNode.style.paddingTop + boxNode.style.borderWidthTop + context.borderSpacing.y;
                } else {
                    // Place beneath caption
                    boxNode.y = context.lastPlacedCaption.y + context.lastPlacedCaption.style.marginBottom + boxNode.style.borderWidthTop + context.borderSpacing.y;
                }
            } else {
                // Place within row group
                boxNode.y = context.lastPlacedRowGroup.y + context.lastPlacedRowGroup.style.paddingTop + boxNode.style.borderWidthTop;
            }
        } else {
            boxNode.y = context.lastPlacedRow.y + context.lastPlacedRow.height + context.borderSpacing.y;
        }

        context.lastPlacedRow = boxNode;
    }

    /**
     * Places a cell. These are stacked vertically beneath any caption box, and within any row groups.
     * TODO: remove the padding consideration. Rows should not have padding/margins.
     * @param boxNode       The caption box.
     * @param context       The table formatting context.
     */
    private void placeCell(BoxNode boxNode, TableFormattingContext context) {
        int rowIndex = boxNode.parent.parent.children.indexOf(boxNode.parent);
        List<Integer> rowCellIds = context.rows.get(rowIndex).cells.stream().map(cell -> cell.boxNode.id).toList();
        int startingColumn = rowCellIds.indexOf(boxNode.id);

        float x = context.lastPlacedRow.x + context.lastPlacedRow.style.paddingLeft;
        if (startingColumn > 0) {
            for (int i = 0; i < startingColumn; i++) {
                x += context.columnWidths.get(i) + context.borderSpacing.x;
            }
        }
        boxNode.x = x;
        boxNode.y = context.lastPlacedRow.y + context.lastPlacedRow.style.paddingTop + boxNode.style.borderWidthTop;
    }

    /**
     * Initializes the table formatting context. This identifies each row and each column within each row by coordinate.
     * The number of rows and columns is saved in the context.
     * @param boxNode       The table box node.
     * @param context       The table formatting context to initialize.
     */
    private void initializeContext(BoxNode boxNode, float availableWidth, TableFormattingContext context) {
        Set<CSSStyle.DisplayType> tableRowDisplayTypes = Set.of(
                CSSStyle.DisplayType.TABLE_HEADER_GROUP,
                CSSStyle.DisplayType.TABLE_ROW_GROUP,
                CSSStyle.DisplayType.TABLE_FOOTER_GROUP
        );

        List<BoxNode> rows = new ArrayList<>();
        for (BoxNode child : boxNode.children) {
            if (tableRowDisplayTypes.contains(child.innerDisplayType)) {
                rows.addAll(child.children);
            } else if (child.innerDisplayType.equals(CSSStyle.DisplayType.TABLE_ROW)) {
                rows.add(child);
            }
        }

        int maxColumns = 0;
        for (int i = 0; i < rows.size(); i++) {
            BoxNode rowBoxNode  = rows.get(i);
            context.addRow(rowBoxNode);
            int columnsInRow = 0;
            for (int j = 0; j < rowBoxNode.children.size(); j++) {
                BoxNode child = rowBoxNode.children.get(j);
                if (child.innerDisplayType.equals(CSSStyle.DisplayType.TABLE_CELL)) {
                    context.addCell(child, i);
                    columnsInRow += context.getCell(j, i).span.x;
                }
            }
            maxColumns = Math.max(maxColumns, columnsInRow);
        }

        int tableWidth = maxColumns;
        int tableHeight = rows.size();

        for (int x = 0; x < tableWidth; x++) {
            for (int y = 0; y < tableHeight; y++) {
                TableCell tableCell = context.getCell(x, y);
                if (tableCell == null || tableCell.isSpannedX || tableCell.isSpannedY) {
                    continue;
                }
                IntVector2 span = tableCell.span;
                if (span.x > 1 && span.y == 1) {
                    for (int i = 0; i < span.x - 1; i++) {
                        context.addSpannedCell(tableCell.boxNode, y, x + i + 1, new IntVector2(x, y));
                    }
                } else if (span.y > 1 && span.x == 1) {
                    for (int i = 0; i < span.y - 1; i++) {
                        context.addSpannedCell(tableCell.boxNode, y + i + 1, x, new IntVector2(x, y));
                    }
                } else if (span.x > 1 && span.y > 1) {
                    for (int i = 0; i < span.x; i++) {
                        for (int j = 0; j < span.y; j++) {
                            if (i == 0 && j == 0) continue;
                            context.addSpannedCell(tableCell.boxNode, y + j, x + i, new IntVector2(x, y));
                        }
                    }
                }
            }
        }

        // Set the fixed width columns list
        context.fixedColumnWidths.addAll(Collections.nCopies(tableWidth, false));
        for (int x = 0; x < tableWidth; x++) {
            for (int y = 0; y < tableHeight; y++) {
                TableCell tableCell = context.getCell(x, y);
                if (tableCell == null) {
                    continue;
                }
                BoxNode cell = tableCell.boxNode;
                if (cell.style.width != null && cell.style.widthType.equals(CSSStyle.DimensionType.PIXEL)) {
                    context.fixedColumnWidths.set(x, true);
                }
            }
        }

        // Set the fixed height rows list
        context.fixedRowHeights.addAll(Collections.nCopies(tableHeight, false));
        for (int y = 0; y < tableHeight; y++) {
            for (int x = 0; x < tableWidth; x++) {
                TableCell tableCell = context.getCell(x, y);
                if (tableCell == null) {
                    continue;
                }
                BoxNode cell = tableCell.boxNode;
                if (cell.style.height != null && cell.style.heightType.equals(CSSStyle.DimensionType.PIXEL)) {
                    context.fixedRowHeights.set(y, true);
                }
            }
        }

        context.width = tableWidth;
        context.height = tableHeight;
        context.availableWidth = availableWidth;
        if (boxNode.width != null) {
            context.fixedWidth = boxNode.width;
            context.hasFixedWidth = true;
        }

        removeFullySpannedColumns(context);
        removeFullySpannedRows(context);

        context.borderSpacing = new IntVector2(boxNode.style.borderSpacing, boxNode.style.borderSpacing);
    }

    private void updateContextWithCaption(BoxNode table, TableFormattingContext context) {
        for (BoxNode child : table.children) {
            if (child.innerDisplayType.equals(TABLE_CAPTION)) {
                context.hasCaption = true;
                context.captions.add(child);
            }
        }
    }

    /**
     * For each cell stored in the context, this function sets the minimum and maximum preferred widths.
     * @param context       The table formatting context.
     */
    private void calculateCellWidths(TableFormattingContext context) {
        for (int rowIndex = 0; rowIndex < context.height; rowIndex++) {
            for (int colIndex = 0; colIndex < context.width; colIndex++) {
                TableCell cell = context.getCell(colIndex, rowIndex);
                if (cell == null || cell.isSpannedX) {
                    continue;
                }

                if (cell.boxNode.style.width == null || cell.boxNode.style.widthType.equals(CSSStyle.DimensionType.PERCENTAGE)) {
                    List<Float> widths = getCellWidths(cell.boxNode);
                    cell.minWidth = widths.get(0);
                    cell.maxWidth = widths.get(1);
                } else {
                    cell.minWidth = cell.boxNode.style.width;
                    cell.maxWidth = cell.boxNode.style.width;
                    cell.fixedWidth = true;
                }
            }
        }
    }

    private void calculateCellHeights(TableFormattingContext context) {
        for (int rowIndex = 0; rowIndex < context.height; rowIndex++) {
            for (int colIndex = 0; colIndex < context.width; colIndex++) {
                TableCell cell = context.getCell(colIndex, rowIndex);
                if (cell == null || cell.isSpannedY) {
                    continue;
                }

                if (cell.boxNode.style.height == null || cell.boxNode.style.heightType.equals(CSSStyle.DimensionType.PERCENTAGE)) {
                    cell.minHeight = getCellHeight(cell.boxNode);
                } else {
                    cell.minHeight = cell.boxNode.style.height;
                    cell.fixedHeight = true;
                }
            }
        }
    }

    private void setHorizontalWidths(TableFormattingContext context) {
        // Get the list of minimum working column widths
        TableCellAligner aligner = new TableCellAligner();
        List<Float> columnMinimumWidths = new ArrayList<>();
        float totalMinWidth = (context.width + 1) * context.borderSpacing.x;
        for (float width : aligner.alignColumnsMinWidth(context)) {
            columnMinimumWidths.add(width);
            totalMinWidth += width;
        }

        // Find the maximum width the cells would utilize.
        float totalMaxWidth = (context.width + 1) * context.borderSpacing.x;
        List<Float> columnMaxWidths = new ArrayList<>();
        for (float width : aligner.alignColumnsMaxWidth(context)) {
            columnMaxWidths.add(width);
            totalMaxWidth += width;
        }

        if (context.tableBoxNode.width != null) {
            // The table has fixed width, update cells or table width.
            float width = context.tableBoxNode.width;
            context.columnWidths = columnMinimumWidths;
            if (width <= totalMinWidth) {
                // Table set width is too small for the contents. Table is expanded.
                context.tableBoxNode.width = totalMinWidth;
            } else {
                // There is extra space to be distributed between the columns.
                float diff = width - totalMinWidth;
                distributeSpaceBetweenSections(diff, context.columnWidths, context.fixedColumnWidths, null);
            }
        } else {
            // Table has no fixed width. Width is constrained by available space and contents.
            float width = Math.max(totalMinWidth, Math.min(context.availableWidth, totalMaxWidth));
            context.tableBoxNode.width = width;
            context.columnWidths = columnMinimumWidths;
            if (width > totalMinWidth) {
                float diff = width - totalMinWidth;
                distributeSpaceBetweenSections(diff, context.columnWidths, context.fixedColumnWidths, columnMaxWidths);
            }
        }

        for (int x = 0; x < context.width; x++) {
            for (int y = 0; y < context.height; y++) {
                TableCell cell = context.getCell(x, y);
                if (cell == null || cell.isSpannedX || cell.isSpannedY) continue;
                float width = 0;
                for (int column = x; column < x + cell.span.x; column++) {
                    width += context.columnWidths.get(column);
                    if (column > x) {
                        width += context.borderSpacing.x;
                    }
                }

                cell.boxNode.width = width;
            }
        }

        for (int y = 0; y < context.height; y++) {
            BoxNode rowBoxNode = context.rows.get(y).rowBoxNode;
            rowBoxNode.width = context.tableBoxNode.width - context.borderSpacing.x * 2;
        }
    }

    private void setVerticalHeights(TableFormattingContext context) {
        // Get the list of minimum working row heights
        TableCellAligner aligner = new TableCellAligner();
        List<Float> rowMinimumHeights = new ArrayList<>();
        float totalMinHeight = (context.height - 1) * context.borderSpacing.y;
        float[] alignedRowHeights = aligner.alignRowsMinHeight(context);
        for (int rowIndex = 0; rowIndex < context.height; rowIndex++) {
            float height;
            BoxNode rowBoxNode = context.rows.get(rowIndex).rowBoxNode;
            if (rowBoxNode.style.height != null && rowBoxNode.style.heightType.equals(CSSStyle.DimensionType.PIXEL)) {
                height = rowBoxNode.style.height;
            } else {
                height = alignedRowHeights[rowIndex];
            }
            rowMinimumHeights.add(height);
            totalMinHeight += height;
        }

        if (context.tableBoxNode.height != null) {
            // The table has fixed height, update cells or table height.
            float height = context.tableBoxNode.height;
            context.rowHeights = rowMinimumHeights;
            if (height <= totalMinHeight) {
                // Table set height is too small for the contents. Table is expanded.
                context.tableBoxNode.height = totalMinHeight;
            } else {
                // There is extra space to be distributed between the rows.
                float diff = height - totalMinHeight;
                distributeSpaceBetweenSections(diff, context.rowHeights, context.fixedRowHeights, null);
            }
        } else {
            // Table has no fixed height. The height is set to the height needed by the rows.
            context.tableBoxNode.height = totalMinHeight;
            context.rowHeights = rowMinimumHeights;
        }

        // Set the height of each cell based on the row heights.
        for (int x = 0; x < context.width; x++) {
            for (int y = 0; y < context.height; y++) {
                TableCell cell = context.getCell(x, y);
                if (cell == null || cell.isSpannedX || cell.isSpannedY) continue;
                float height = 0;
                for (int row = y; row < y + cell.span.y; row++) {
                    height += context.rowHeights.get(row);
                    if (row > y) {
                        height += context.borderSpacing.y;
                    }
                }
                cell.boxNode.height = height;
            }
        }

        // Set the height of each row box
        for (int y = 0; y < context.height; y++) {
            BoxNode rowBoxNode = context.rows.get(y).rowBoxNode;
            rowBoxNode.height = context.rowHeights.get(y);
        }
    }

    private void setTableBoxSizes(TableFormattingContext context) {
        // Set the width and height of each cell based on its span.
        for (int x = 0; x < context.width; x++) {
            for (int y = 0; y < context.height; y++) {
                TableCell cell = context.getCell(x, y);
                if (cell == null || cell.isSpannedX || cell.isSpannedY) continue;
                float width = 0, height = 0;
                for (int column = x; column < x + cell.span.x; column++) {
                    width += context.columnWidths.get(column);
                    if (column > x) {
                        width += context.borderSpacing.x;
                    }
                }
                for (int row = y; row < y + cell.span.y; row++) {
                    height += context.rowHeights.get(row);
                    if (row > y) {
                        height += context.borderSpacing.y;
                    }
                }

                cell.boxNode.width = width;
                cell.boxNode.height = height;
            }
        }

        // Set the width and height of each row box
        for (int y = 0; y < context.height; y++) {
            BoxNode rowBoxNode = context.rows.get(y).rowBoxNode;
            rowBoxNode.width = context.tableBoxNode.width - context.borderSpacing.x * 2;
            rowBoxNode.height = context.rowHeights.get(y);
        }
    }

    private void distributeSpaceBetweenSections(float spaceToDistribute, List<Float> sizes, List<Boolean> fixed, List<Float> maxSizes) {
        if (maxSizes == null) {
            // maxSizes being null indicates that there are no enforced max sizes.
            maxSizes = Collections.nCopies(sizes.size(), Float.MAX_VALUE);
        }

        List<Boolean> adjustableSections = new ArrayList<>();
        for (int i = 0; i < sizes.size(); i++) {
            adjustableSections.add(!fixed.get(i) && sizes.get(i) < maxSizes.get(i));
        }

        if (!adjustableSections.contains(true)) {
            // All sections are either fixed or already at their maximum size.
            return;
        }

        int numAdjustableSections = adjustableSections.stream().filter(Boolean::booleanValue).toList().size();
        float spacePerSection = spaceToDistribute / numAdjustableSections;

        // Evenly distribute the available space to each section, respecting max section sizes.
        while (spaceToDistribute > 0) {
            for (int i = 0; i < sizes.size(); i++) {
                if (adjustableSections.get(i)) {
                    float maxAddition = maxSizes.get(i) - sizes.get(i);
                    float addition = Math.min(maxAddition, spacePerSection);
                    float newSize = sizes.get(i) + addition;
                    sizes.set(i, newSize);
                    if (newSize >= maxSizes.get(i)) {
                        adjustableSections.set(i, false);
                    }
                    spaceToDistribute -= addition;
                }
            }

            numAdjustableSections = adjustableSections.stream().filter(Boolean::booleanValue).toList().size();
            if (numAdjustableSections == 0) {
                break;
            } else {
                spacePerSection = spaceToDistribute / numAdjustableSections;
            }
        }
    }

    private List<Float> getCellWidths(BoxNode boxNode) {
        if (boxNode.style.width != null && boxNode.style.widthType.equals(CSSStyle.DimensionType.PIXEL)) {
            return List.of(boxNode.width, boxNode.width);
        }

        List<Float> widths = List.of(1f, Float.MAX_VALUE);
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

        return results;
    }

    private Float getCellHeight(BoxNode boxNode) {
        if (boxNode.style.height != null && boxNode.style.heightType.equals(CSSStyle.DimensionType.PIXEL)) {
            return boxNode.style.height;
        }

        BoxNode copyBoxNode = boxNode.deepCopy();
        removePercentageWidthBlockBoxes(copyBoxNode);
        copyBoxNode.innerDisplayType = CSSStyle.DisplayType.FLOW;
        copyBoxNode.outerDisplayType = CSSStyle.DisplayType.BLOCK;
        copyBoxNode.style.width = boxNode.width;
        copyBoxNode.style.widthType = CSSStyle.DimensionType.PIXEL;
        boxLayoutGenerator.calculateLayout(copyBoxNode, boxNode.width);
        float maxY = 0;
        for (BoxNode child : copyBoxNode.children) {
            float childMaxY = child.y + child.height + child.style.marginBottom + copyBoxNode.style.paddingBottom + copyBoxNode.style.borderWidthBottom;
            maxY = Math.max(childMaxY, maxY);
        }
        return maxY - copyBoxNode.y;
    }

    private void removeFullySpannedColumns(TableFormattingContext context) {
        for (int x = context.width - 1; x >= 0; x--) {
            boolean containsNonSpanningCell = false;
            for (int y = 0; y < context.height; y++) {
                if (context.getCell(x, y) == null) {
                    continue;
                }

                if (!context.getCell(x, y).isSpannedX) {
                    containsNonSpanningCell = true;
                    break;
                }
            }

            if (!containsNonSpanningCell) {
                // Entire column is x-spanning cells, so it can be removed.
                Set<IntVector2> reducedCells = new HashSet<>();
                for (int y = 0; y < context.height; y++) {
                    TableCell spannedCell = context.getCell(x, y);
                    if (!reducedCells.contains(spannedCell.spannedCellOrigin)) {
                        TableCell originalCell = context.getCell(spannedCell.spannedCellOrigin.x, spannedCell.spannedCellOrigin.y);
                        originalCell.span.x--;
                        reducedCells.add(spannedCell.spannedCellOrigin);
                    }
                    context.rows.get(y).cells.remove(x);
                }
                context.fixedColumnWidths.remove(x);
                context.width--;
                x--;
            }
        }
    }

    private void removeFullySpannedRows(TableFormattingContext context) {
        for (int y = context.height - 1; y >= 0; y--) {
            boolean containsNonSpanningCell = false;
            boolean containsNonNullCell = false;
            for (int x = 0; x < context.width; x++) {
                if (context.getCell(x, y) == null) {
                    continue;
                }
                containsNonNullCell = true;
                if (!context.getCell(x, y).isSpannedY) {
                    containsNonSpanningCell = true;
                    break;
                }
            }

            if (containsNonNullCell && !containsNonSpanningCell) {
                // Entire row is y-spanning cells, so it can be removed.
                Set<IntVector2> reducedCells = new HashSet<>();
                for (int x = 0; x < context.width; x++) {
                    TableCell spannedCell = context.getCell(x, y);
                    if (spannedCell != null && !reducedCells.contains(spannedCell.spannedCellOrigin)) {
                        TableCell originalCell = context.getCell(spannedCell.spannedCellOrigin.x, spannedCell.spannedCellOrigin.y);
                        originalCell.span.y--;
                        reducedCells.add(spannedCell.spannedCellOrigin);
                    }
                    context.rows.remove(y);
                }
                context.fixedRowHeights.remove(y);
                context.height--;
                y--;
            }
        }
    }

    /**
     *
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
