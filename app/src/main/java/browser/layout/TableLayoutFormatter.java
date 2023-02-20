package browser.layout;

import static browser.css.CSSStyle.DisplayType.*;

import java.util.*;
import java.util.stream.IntStream;

import browser.css.CSSStyle;
import browser.model.BoxNode;
import browser.model.IntVector2;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TableLayoutFormatter {

    public enum TableLayoutFormatterFlag { SetHeightFromChildren, SetHeightForRow }

    private final BoxLayoutGenerator boxLayoutGenerator;

    // Public methods

    public List<TableLayoutFormatterFlag> placeBox(BoxNode boxNode, TableFormattingContext context) {
        switch (boxNode.innerDisplayType) {
            case TABLE_CAPTION -> { return placeCaption(boxNode, context); }
            case TABLE_HEADER_GROUP, TABLE_ROW_GROUP, TABLE_FOOTER_GROUP -> { return placeRowGroup(boxNode, context); }
            case TABLE_ROW -> { return placeRow(boxNode, context); }
            case TABLE_CELL -> { return placeCell(boxNode, context); }
        }
        return new ArrayList<>();
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
        // Organize the cells into rows
        initializeContext(boxNode, context);
        // Set the preferred sizes of each cell
        calculateCellWidths(context);
        // Set the minimum and maximum widths of each column.
        setColumnMinMaxWidths(context);
        // Set the width of the table box.
        setTableWidth(context, boxNode, availableWidth);
        // Set the width of each cell/column.
        setCellWidths(context);
    }

    public float getHeightFromChildren(BoxNode boxNode, TableFormattingContext context) {
        if (boxNode.children.size() == 0) {
            return boxNode.height == null ? 0 : boxNode.height;
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
     * @return      A list of flags used in box layout generator.
     */
    private List<TableLayoutFormatterFlag> placeCaption(BoxNode boxNode, TableFormattingContext context) {
        boxNode.width = context.tableBoxNode.width;
        boxNode.x = context.tableBoxNode.x + context.tableBoxNode.style.paddingLeft + boxNode.style.borderWidthLeft;

        if (context.lastPlacedCaption == null) {
            boxNode.y = context.tableBoxNode.y + context.tableBoxNode.style.paddingTop + boxNode.style.borderWidthTop;
        } else {
            boxNode.y = context.lastPlacedCaption.y + context.lastPlacedCaption.style.marginBottom + boxNode.style.borderWidthTop;
        }
        context.lastPlacedCaption = boxNode;
        return List.of(TableLayoutFormatterFlag.SetHeightFromChildren);
    }

    /**
     * Places a header, row, or footer group. These are stacked vertically beneath any caption boxes.
     * @param boxNode       The caption box.
     * @param context       The table formatting context.
     * @return      A list of flags used in box layout generator.
     */
    private List<TableLayoutFormatterFlag> placeRowGroup(BoxNode boxNode, TableFormattingContext context) {
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
        return List.of(TableLayoutFormatterFlag.SetHeightFromChildren);
    }

    /**
     * Places a row. These are stacked vertically beneath any caption box, and within any row groups.
     * @param boxNode       The caption box.
     * @param context       The table formatting context.
     * @return      A list of flags used in box layout generator.
     */
    private List<TableLayoutFormatterFlag> placeRow(BoxNode boxNode, TableFormattingContext context) {
        boxNode.width = context.tableBoxNode.width - context.borderSpacing.x * 2;

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
        return List.of(TableLayoutFormatterFlag.SetHeightFromChildren);
    }

    /**
     * Places a cell. These are stacked vertically beneath any caption box, and within any row groups.
     * TODO: remove the padding consideration. Rows should not have padding/margins.
     * @param boxNode       The caption box.
     * @param context       The table formatting context.
     * @return      A list of flags used in box layout generator.
     */
    private List<TableLayoutFormatterFlag> placeCell(BoxNode boxNode, TableFormattingContext context) {
        int colSpan = Integer.parseInt(boxNode.correspondingRenderNode.attributes.getOrDefault("colspan", "1"));

        int startingColumn = 0;
        for (BoxNode child : boxNode.parent.children) {
            if (child == boxNode) {
                break;
            }
            int span = Integer.parseInt(child.correspondingRenderNode.attributes.getOrDefault("colspan", "1"));
            startingColumn += span;
        }

        float width = 0;
        for (int i = startingColumn; i < startingColumn + colSpan; i++) {
            width += context.columnWidths.get(i);
        }

        boxNode.width = width;
        float x = context.lastPlacedRow.x + context.lastPlacedRow.style.paddingLeft;
        if (startingColumn > 0) {
            for (int i = 0; i < startingColumn; i++) {
                x += context.columnWidths.get(i) + context.borderSpacing.x;
            }
        }
        boxNode.x = x;
        boxNode.y = context.lastPlacedRow.y + context.lastPlacedRow.style.paddingTop + boxNode.style.borderWidthTop;

        return List.of(TableLayoutFormatterFlag.SetHeightFromChildren, TableLayoutFormatterFlag.SetHeightForRow);
    }

    /**
     * Initializes the table formatting context. This identifies each row and each column within each row by coordinate.
     * The number of rows and columns is saved in the context.
     * @param boxNode       The table box node.
     * @param context       The table formatting context to initialize.
     */
    private void initializeContext(BoxNode boxNode, TableFormattingContext context) {
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
            } else {
                // not a row group, not a row
                // could be a caption, but anything else is invalid
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
                    columnsInRow++;
                } else {
                    // invalid
                }
            }
            maxColumns = Math.max(maxColumns, columnsInRow);
        }

        int tableWidth = maxColumns;
        int tableHeight = rows.size();

        for (int x = 0; x < tableWidth; x++) {
            for (int y = 0; y < tableHeight; y++) {
                BoxNode cell = context.getCell(x, y).boxNode;
                IntVector2 span = context.getCell(x, y).span;
                if (span.x > 1) {
                    for (int i = 0; i < span.x - 1; i++) {
                        context.addSpannedCell(cell, y, x + 1);
                    }
                }

                if (span.y > 1) {
                    for (int i = 0; i < span.y - 1; i++) {
                        context.addSpannedCell(cell, y + i + 1, x);
                    }
                }
            }
        }

        context.width = tableWidth;
        context.height = tableHeight;
        context.minimumColumnWidths = new ArrayList<>(Collections.nCopies(tableWidth, Float.MAX_VALUE));
        context.maximumColumnWidths = new ArrayList<>(Collections.nCopies(tableWidth, 0.0f));
        context.columnWidths = new ArrayList<>(Collections.nCopies(tableWidth, 0.0f));
        context.fixedColumnWidths = new ArrayList<>(Collections.nCopies(tableWidth, false));

        int colSpan = Integer.parseInt(boxNode.correspondingRenderNode.attributes.getOrDefault("colspan", "1"));
        int rowSpan = Integer.parseInt(boxNode.correspondingRenderNode.attributes.getOrDefault("rowspan", "1"));
        context.borderSpacing = new IntVector2(colSpan, rowSpan);
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
                TableFormattingContext.TableCell cell = context.getCell(colIndex, rowIndex);
                if (cell.isSpannedCell) {
                    continue;
                }

                if (cell.boxNode.style.width == null || cell.boxNode.style.widthType.equals(CSSStyle.DimensionType.PERCENTAGE)) {
                    List<Float> widths = getCellWidths(cell.boxNode);
                    cell.minimumPreferredWidth = widths.get(0);
                    cell.maximumPreferredWidth = widths.get(1);
                } else {
                    cell.minimumPreferredWidth = cell.boxNode.width;
                    cell.maximumPreferredWidth = cell.boxNode.width;
                }
            }
        }
    }

    private void setColumnMinMaxWidths(TableFormattingContext context) {
        // Collect the column widths based on the single column spanning cells.
        for (int x = 0; x < context.width; x++) {
            Float fixedWidth = getSingleColumnFixedWidth(context, x, false);
            if (fixedWidth != null) {
                context.minimumColumnWidths.set(x, fixedWidth);
                context.maximumColumnWidths.set(x, fixedWidth);
                context.fixedColumnWidths.set(x, true);
            } else {
                List<Float> widths = getSingleSpanColumnAutoWidths(context, x);
                context.minimumColumnWidths.set(x, widths.get(0));
                context.maximumColumnWidths.set(x, widths.get(1));
            }
        }

        // Adjust the column widths based on multiple column spanning cells.
        // TODO add support for column groups having a fixed width
        increaseMinimumColumnWidthsForSpanningCells(context);
    }

    private void setTableWidth(TableFormattingContext context, BoxNode boxNode, float availableWidth) {
        float minimumRequiredWidth = context.minimumColumnWidths.stream().reduce(Float::sum).get();
        float maximumRequiredWidth = context.maximumColumnWidths.stream().reduce(Float::sum).get();
        float totalBorderSpacing = context.borderSpacing.x * (context.width + 1);
        float totalCellSpacing = 0;
        float totalBorderWidth = 0;
        float totalMinWidth = minimumRequiredWidth + totalCellSpacing + totalBorderWidth + totalBorderSpacing;
        float totalMaxWidth = maximumRequiredWidth + totalCellSpacing + totalBorderWidth + totalBorderSpacing;

        // Set the width of the table box.
        if (boxNode.style.width == null) {
            // Table does not have a fixed width, so set width between min and max, constrained by the available width.
            boxNode.width = Math.max(totalMinWidth, Math.min(availableWidth, totalMaxWidth));
        } else {
            float fixedWidth = boxNode.style.widthType.equals(CSSStyle.DimensionType.PERCENTAGE) ?
                    boxNode.parent.width * boxNode.style.width / 100.0f : boxNode.style.width;
            // The table width was not large enough for the columns. Fixed width is overwritten.
            boxNode.width = Math.max(fixedWidth, totalMinWidth);
        }
    }

    private void setCellWidths(TableFormattingContext context) {
        float totalFixedColumnWidth = 0;
        float totalNonFixedMinimumWidth = 0;

        // Set the width of any fixed single-column width columns
        for (int columnIndex = 0; columnIndex < context.width; columnIndex++) {
            // TODO include percentage cell widths in this calculation.
            Float fixedWidth = getSingleColumnFixedWidth(context, columnIndex, false);
            if (fixedWidth != null) {
                context.columnWidths.set(columnIndex, fixedWidth);
                totalFixedColumnWidth += fixedWidth;
            } else {
                totalNonFixedMinimumWidth += context.minimumColumnWidths.get(columnIndex);
            }
        }


        float tablePadding = 0;
        float totalBorderSpacing = context.borderSpacing.x * (context.width + 1);
        float remainingWidth = context.tableBoxNode.width - totalFixedColumnWidth - totalNonFixedMinimumWidth - tablePadding - totalBorderSpacing;
        int numAutomaticColumns = context.fixedColumnWidths.stream().filter(v -> !v).toList().size();
        if (numAutomaticColumns > 0) {
            float additionalWidthPerColumn = remainingWidth / numAutomaticColumns;

            // Set the width of columns width automatic widths
            for (int columnIndex = 0; columnIndex < context.width; columnIndex++) {
                if (context.fixedColumnWidths.get(columnIndex)) {
                    continue;
                }

                float columnWidth = context.minimumColumnWidths.get(columnIndex) + additionalWidthPerColumn;
                context.columnWidths.set(columnIndex, columnWidth);
            }
        }

        for (int columnIndex = 0; columnIndex < context.width; columnIndex++) {
            for (int rowIndex = 0; rowIndex < context.height; rowIndex++) {
                BoxNode cell = context.getCell(columnIndex, rowIndex).boxNode;
                cell.width = context.columnWidths.get(columnIndex);
            }
        }
    }

    /**
     * A column in the table has a fixed width if any of the cells of that column have fixed widths. If multiple have
     * fixed widths, the largest is used.
     * Only single column spanning cells and pixel width cells are considered in the pass.
     * @param context       The corresponding table formatting context.
     * @param columnIndex       The index of the column to check.
     * @return      Null if there is no fixed width, otherwise the width as a float.
     */
    private Float getSingleColumnFixedWidth(TableFormattingContext context, int columnIndex, boolean includePercentage) {
        boolean hasFixedWidth = false;
        float width = 0;
        for (int rowIndex = 0; rowIndex < context.height; rowIndex++) {
            BoxNode cell = context.getCell(columnIndex, rowIndex).boxNode;
            IntVector2 span = context.getCell(columnIndex, rowIndex).span;
            boolean fixedWidth = cell.style.width != null &&
                    (cell.style.widthType.equals(CSSStyle.DimensionType.PIXEL) ||
                            (cell.style.widthType.equals(CSSStyle.DimensionType.PERCENTAGE) && includePercentage));
            if (fixedWidth && span.x == 1) {
                hasFixedWidth = true;
                float newWidth = cell.style.width;
                if (cell.style.widthType.equals(CSSStyle.DimensionType.PERCENTAGE)) {
                    newWidth /= 100.0f * context.tableBoxNode.width;
                }
                width = Math.max(width, newWidth);
            }
        }

        return hasFixedWidth ? width : null;
    }

    /**
     * For a given column, calculates the minimum and maximum widths needed for the content of each cell in the column.
     * The largest minimum and maximum among all the cells is returned.
     * Only cells that span a single column are considered in this pass. Multi-column spanning cells are considered
     * afterwards.
     * @param context       The table formatting context.
     * @param columnIndex       The column to use.
     * @return      A list of the form [minimum width, maximum width].
     */
    private List<Float> getSingleSpanColumnAutoWidths(TableFormattingContext context, int columnIndex) {
        float minimumColumnWidth = Float.MIN_VALUE;
        float maximumColumnWidth = Float.MIN_VALUE;

        for (int y = 0; y < context.height; y++) {
            TableFormattingContext.TableCell cell = context.getCell(columnIndex, y);
            if (cell.span.x == 1) {
                if (minimumColumnWidth < cell.minimumPreferredWidth) {
                    minimumColumnWidth = cell.minimumPreferredWidth;
                }
                if (maximumColumnWidth < cell.maximumPreferredWidth) {
                    maximumColumnWidth = cell.maximumPreferredWidth;
                }
            }
        }

        return List.of(minimumColumnWidth, maximumColumnWidth);
    }

    private void increaseMinimumColumnWidthsForSpanningCells(TableFormattingContext context) {
        for (int rowIndex = 0; rowIndex < context.height; rowIndex++) {
            for (int colIndex = 0; colIndex < context.width; colIndex++) {
                TableFormattingContext.TableCell cell = context.getCell(colIndex, rowIndex);
                if (cell.isSpannedCell || cell.span.x == 1) {
                    continue;
                }
                float requiredWidth = cell.minimumPreferredWidth;
                float totalColumnWidth = context.minimumColumnWidths.subList(colIndex, colIndex + cell.span.x).stream().reduce(Float::sum).get();
                if (totalColumnWidth >= requiredWidth) {
                    continue;
                }

                float diff = requiredWidth - totalColumnWidth;
                int[] adjustableColumns = IntStream.range(colIndex, colIndex + cell.span.x).filter(context.fixedColumnWidths::get).toArray();
                if (adjustableColumns.length > 0) {
                    float additionalWidth = diff / adjustableColumns.length;
                    for (int i : adjustableColumns) {
                        float newMinimumWidth = context.minimumColumnWidths.get(i) + additionalWidth;
                        context.minimumColumnWidths.set(i, newMinimumWidth);
                    }
                }
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
