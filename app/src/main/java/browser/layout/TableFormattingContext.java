package browser.layout;

import java.util.*;

import browser.model.BoxNode;
import browser.model.IntVector2;

public class TableFormattingContext {

    public int width, height;
    public IntVector2 borderSpacing;
    public BoxNode tableBoxNode;
    public List<Float> minimumColumnWidths, maximumColumnWidths, columnWidths;
    public List<Boolean> fixedColumnWidths;
    public boolean hasCaption = false;
    public List<BoxNode> captions = new ArrayList<>();

    public BoxNode lastPlacedCaption = null;
    public BoxNode lastPlacedRowGroup = null;
    public BoxNode lastPlacedRow = null;

    private final List<TableRow> rows = new ArrayList<>();

    public void addRow(BoxNode boxNode) {
        TableRow row = new TableRow();
        row.rowBoxNode = boxNode;
        rows.add(row);
    }

    public void addCell(BoxNode boxNode, int rowIndex) {
        TableCell cell = new TableCell();
        cell.boxNode = boxNode;
        cell.span = new IntVector2(
                Integer.parseInt(boxNode.correspondingRenderNode.attributes.getOrDefault("colspan", "1")),
                Integer.parseInt(boxNode.correspondingRenderNode.attributes.getOrDefault("rowspan", "1")));
        rows.get(rowIndex).cells.add(cell);
    }

    public void addSpannedCell(BoxNode boxNode, int rowIndex, int columnIndex) {
        TableCell cell = new TableCell();
        cell.isSpannedCell = true;
        cell.boxNode = boxNode;
        // Cells spanning outside the table rows are ignored.
        if (rows.size() <= rowIndex) {
            return;
        }
        // Cells spanning outside the table columns are ignored.
        if (rows.get(rowIndex).cells.size() <= columnIndex) {
            return;
        }
        rows.get(rowIndex).cells.add(columnIndex, cell);
    }

    public TableCell getCell(int x, int y) {
        return rows.get(y).cells.get(x);
    }

    private static class TableRow {
        BoxNode rowBoxNode;
        List<TableCell> cells = new ArrayList<>();
    }

    public static class TableCell {
        boolean isSpannedCell = false;
        BoxNode boxNode;
        IntVector2 span;
        Float minimumPreferredWidth;
        float maximumPreferredWidth;
    }

}
