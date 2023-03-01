package browser.model;

public class TableCell {

    public IntVector2 spannedCellOrigin;
    public BoxNode boxNode;
    public IntVector2 span = new IntVector2(1, 1);
    public boolean isSpannedX = false;
    public boolean isSpannedY = false;
    public boolean fixedWidth = false;
    public boolean fixedHeight = false;
    public Float minWidth, minHeight;
    public Float maxWidth, maxHeight;

    public TableCell deepCopy() {
        TableCell cell = new TableCell();
        cell.spannedCellOrigin = spannedCellOrigin == null ? null : new IntVector2(spannedCellOrigin.x, spannedCellOrigin.y);
        cell.boxNode = boxNode;
        cell.span = new IntVector2(span.x, span.y);
        cell.isSpannedX = isSpannedX;
        cell.isSpannedY = isSpannedY;
        cell.fixedWidth = fixedWidth;
        cell.fixedHeight = fixedHeight;
        cell.minWidth = minWidth == null ? null : Float.valueOf(minWidth.floatValue());;
        cell.minHeight = minHeight == null ? null : Float.valueOf(minHeight.floatValue());
        cell.maxWidth = maxWidth == null ? null : Float.valueOf(maxWidth.floatValue());;
        cell.maxHeight = maxHeight == null ? null : Float.valueOf(maxHeight.floatValue());
        return cell;
    }

}
