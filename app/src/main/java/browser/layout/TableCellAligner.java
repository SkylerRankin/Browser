package browser.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import browser.model.IntVector2;
import browser.model.TableCell;

public class TableCellAligner {

    public enum Mode { Min, Max }

    private Mode mode = Mode.Min;
    private List<List<TableCell>> cells;
    private boolean[] fixedSize;
    private float[] columnWidths;
    private IntVector2 borderSpacing;
    private int width, height;

    // Public methods

    public float[] alignColumnsMinWidth(TableFormattingContext context) {
        return alignColumns(context, Mode.Min);
    }

    public float[] alignColumnsMinWidth(int width, int height, IntVector2 borderSpacing, List<List<TableCell>> tableCells) {
        return alignColumns(width, height, borderSpacing, tableCells, Mode.Min);
    }

    public float[] alignColumnsMaxWidth(TableFormattingContext context) {
        return alignColumns(context, Mode.Max);
    }

    public float[] alignColumnsMaxWidth(int width, int height, IntVector2 borderSpacing, List<List<TableCell>> tableCells) {
        return alignColumns(width, height, borderSpacing, tableCells, Mode.Max);
    }

    public float[] alignRowsMinHeight(TableFormattingContext context) {
        return alignRows(context, Mode.Min);
    }

    public float[] alignRowsMinHeight(int width, int height, IntVector2 borderSpacing, List<List<TableCell>> tableCells) {
        return alignRows(width, height, borderSpacing, tableCells, Mode.Min);
    }

    public float[] alignRowsMaxHeight(TableFormattingContext context) {
        return alignRows(context, Mode.Max);
    }

    public float[] alignRowsMaxHeight(int width, int height, IntVector2 borderSpacing, List<List<TableCell>> tableCells) {
        return alignRows(width, height, borderSpacing, tableCells, Mode.Max);
    }

    // Private methods

    private float[] alignColumns(TableFormattingContext context, Mode mode) {
        List<List<TableCell>> tableCells = new ArrayList<>();
        for (int y = 0; y < context.height; y++) {
            List<TableCell> row = new ArrayList<>();
            for (int x = 0; x < context.width; x++) {
                TableCell cell = context.getCell(x, y);
                if (cell != null) {
                    row.add(cell);
                }
            }
            tableCells.add(row);
        }
        return alignColumns(context.width, context.height, context.borderSpacing, tableCells, mode);
    }

    private float[] alignColumns(int width, int height, IntVector2 borderSpacing, List<List<TableCell>> tableCells, Mode mode) {
        cells = tableCells;
        this.width = width;
        this.height = height;
        this.borderSpacing = borderSpacing;
        this.mode = mode;
        align();
        return columnWidths;
    }

    private float[] alignRows(TableFormattingContext context, Mode mode) {
        List<List<TableCell>> tableCells = new ArrayList<>();
        for (int y = 0; y < context.height; y++) {
            List<TableCell> row = new ArrayList<>();
            for (int x = 0; x < context.width; x++) {
                TableCell cell = context.getCell(x, y);
                if (cell != null) {
                    row.add(cell);
                }
            }
            tableCells.add(row);
        }
        return alignRows(context.width, context.height, context.borderSpacing, tableCells, mode);
    }

    private float[] alignRows(int width, int height, IntVector2 borderSpacing, List<List<TableCell>> tableCells, Mode mode) {
        // Create the cells grid, with a row for each column.
        cells = new ArrayList<>();
        for (int i = 0; i < width; i++) {
            cells.add(new ArrayList<>(Collections.nCopies(height, null)));
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (tableCells.get(y).size() <= x) {
                    continue;
                }

                TableCell cell = tableCells.get(y).get(x).deepCopy();

                // Swap the x and y properties of the cell so that column alignment logic will work for row alignment.
                cell.spannedCellOrigin = cell.spannedCellOrigin == null ? null : cell.spannedCellOrigin.transpose();
                cell.span = cell.span.transpose();

                boolean isSpannedX = cell.isSpannedX;
                cell.isSpannedX = cell.isSpannedY;
                cell.isSpannedY = isSpannedX;

                boolean fixedWidth = cell.fixedWidth;
                cell.fixedWidth = cell.fixedHeight;
                cell.fixedHeight = fixedWidth;

                Float minWidth = cell.minWidth;
                cell.minWidth = cell.minHeight;
                cell.minHeight = minWidth;

                Float maxWidth = cell.maxWidth;
                cell.maxWidth = cell.maxHeight;
                cell.maxHeight = maxWidth;

                cells.get(x).set(y, cell);
            }
        }

        this.width = height;
        this.height = width;
        this.borderSpacing = borderSpacing.transpose();
        this.mode = mode;
        align();
        return columnWidths;
    }

    private void align() {
        columnWidths = new float[width];
        fixedSize = new boolean[width];

        addEmptyCells();
        setSingleSpanFixedWidths();
        initializeColumnWidths();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (cellEndsAtColumn(x, y)) {
                    TableCell cell = cells.get(y).get(x);
                    int span = cell.isSpannedX ?
                            cells.get(cell.spannedCellOrigin.y).get(cell.spannedCellOrigin.x).span.x :
                            cell.span.x;
                    if (span == 1) {
                        float minWidth = mode.equals(Mode.Min) ? cells.get(y).get(x).minWidth : cells.get(y).get(x).maxWidth;
                        if (minWidth > columnWidths[x]) {
                            // The minimum size is larger than the current size of the row/column.
                            columnWidths[x] = minWidth;
                        }
                    } else {
                        // get the width of all spanned columns and compare to this spanning cell
                        TableCell originCell = cells.get(cell.spannedCellOrigin.y).get(cell.spannedCellOrigin.x);
                        int origin = cells.get(y).get(x).spannedCellOrigin.x;
                        float totalSize = getSpanTotalSize(origin, span);
                        float minWidth = mode.equals(Mode.Min) ? originCell.minWidth : originCell.maxWidth;
                        if (totalSize < minWidth) {
                            float diff = minWidth - totalSize;
                            List<Integer> adjustableIndices = getAdjustableIndices(origin, span);
                            if (adjustableIndices.size() == 0) {
                                adjustableIndices = IntStream.range(0, width).boxed().collect(Collectors.toList());
                            }
                            float additionalSize = diff / adjustableIndices.size();
                            for (int indexToAdjust : adjustableIndices) {
                                columnWidths[indexToAdjust] += additionalSize;
                            }
                        }
                    }
                }
            }
        }
    }

    private void addEmptyCells() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (cells.get(y).size() <= x) {
                    TableCell emptyCell = new TableCell();
                    emptyCell.minWidth = 0.0f;
                    emptyCell.minHeight = 0.0f;
                    emptyCell.maxWidth = 0.0f;
                    emptyCell.maxHeight = 0.0f;
                    cells.get(y).add(emptyCell);
                }
            }
        }
    }

    private boolean cellEndsAtColumn(int x, int y) {
        TableCell cell = cells.get(y).get(x);
        if (cell == null) {
            return false;
        } else if (cell.isSpannedX) {
            IntVector2 origin = cell.spannedCellOrigin;
            TableCell originCell = cells.get(origin.y).get(origin.x);
            return origin.x + originCell.span.x - 1 == x;
        } else {
            return cell.span.x == 1;
        }
    }

    private float getSpanTotalSize(int startColumn, int span) {
        float totalSize = 0;
        for (int x = startColumn; x < startColumn + span; x++) {
            if (x > startColumn) {
                totalSize += borderSpacing.x;
            }
            totalSize += columnWidths[x];
        }
        return totalSize;
    }

    private List<Integer> getAdjustableIndices(int startColumn, int length) {
        List<Integer> adjustableIndices = new ArrayList<>();
        for (int i = startColumn; i < startColumn + length; i++) {
            if (!fixedSize[i]) {
                adjustableIndices.add(i);
            }
        }
        return adjustableIndices;
    }

    /**
     * Sets the index for each row/column in fixedSize based on if the given row/column has a fixed size. Percentage
     * sizes and spans >1 are not included here. If there is a fixed size, the size is set in the size array as well.
     */
    private void setSingleSpanFixedWidths() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                TableCell cell = cells.get(y).get(x);
                if (cell == null || cell.span.x > 1 || !cell.fixedWidth) {
                    continue;
                }
                fixedSize[x] = true;
                float width = mode.equals(Mode.Min) ? cell.minWidth : cell.maxWidth;
                columnWidths[x] = Math.max(width, columnWidths[x]);
            }
        }
    }

    private void initializeColumnWidths() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                TableCell cell = cells.get(y).get(x);
                if (cell == null || cell.span.x > 1 || cell.isSpannedX) {
                    continue;
                }
                float width = mode.equals(Mode.Min) ? cell.minWidth : cell.maxWidth;
                columnWidths[x] = Math.max(width, columnWidths[x]);
            }
        }
    }

}
