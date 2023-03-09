package browser.layout;

import java.util.*;

import browser.model.BoxNode;
import browser.model.IntVector2;
import browser.model.TableCell;

public class TableFormattingContext {

    public int width, height;
    public float availableWidth, fixedWidth;
    public boolean hasFixedWidth = false;
    public IntVector2 borderSpacing;
    public BoxNode tableBoxNode;
    public List<Float> columnWidths;
    public List<Float> rowHeights;
    public List<Boolean> fixedColumnWidths = new ArrayList<>();
    public List<Boolean> fixedRowHeights = new ArrayList<>();
    public boolean hasCaption = false;
    public List<BoxNode> captions = new ArrayList<>();
    public final List<TableRow> rows = new ArrayList<>();

    public BoxNode lastPlacedCaption = null;
    public BoxNode lastPlacedRowGroup = null;
    public BoxNode lastPlacedRow = null;


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

    public void addSpannedCell(BoxNode boxNode, int rowIndex, int columnIndex, IntVector2 origin) {
        TableCell cell = new TableCell();
        cell.isSpannedX = columnIndex != origin.x;
        cell.isSpannedY = rowIndex != origin.y;
        cell.boxNode = boxNode;
        cell.spannedCellOrigin = origin;
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

    public static class TableRow {
        BoxNode rowBoxNode;
        List<TableCell> cells = new ArrayList<>();
    }

}
