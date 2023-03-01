package browser.layout;

import static browser.constants.MathConstants.DELTA;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import browser.model.IntVector2;
import browser.model.TableCell;

import org.junit.Before;
import org.junit.Test;


public class TableCellAlignerTest {

    private static final String X = "x";
    private static final String Y = "y";

    private TableCellAligner aligner;

    @Before
    public void setup() {
        aligner = new TableCellAligner();
    }

    /**
     * 2x2 table
     * [10] [15]
     * [15] [12]
     * ->
     * [15] [15]
     */
    @Test
    public void alignColumns_basic2x2() {
        float[] expectedWiths = {15, 15};

        TableCell cell11 = makeNormalCell(10, X);
        TableCell cell12 = makeNormalCell(15, X);
        TableCell cell21 = makeNormalCell(15, X);
        TableCell cell22 = makeNormalCell(12, X);

        List<TableCell> row1 = List.of(cell11, cell12);
        List<TableCell> row2 = List.of(cell21, cell22);
        List<List<TableCell>> cells = new ArrayList<>(List.of(row1, row2));

        float[] widths = aligner.alignColumnsMinWidth(2, 2, new IntVector2(0, 0), cells);
        assertFloatArrayEqual(widths, expectedWiths);
    }

    /**
     * 2x2 table
     * [10] [15]
     * [  36  ]
     * ->
     * [15] [20]
     */
    @Test
    public void alignColumns_basicColumnSpan() {
        float[] expectedWiths = {15, 20};

        TableCell cell11 = makeNormalCell(10, X);
        TableCell cell12 = makeNormalCell(15, X);
        TableCell cell21 = makeSpanningCell(36, 2, X);
        TableCell cell22 = makeSpannedCell(0, 1, X);

        List<TableCell> row1 = List.of(cell11, cell12);
        List<TableCell> row2 = List.of(cell21, cell22);
        List<List<TableCell>> cells = new ArrayList<>(List.of(row1, row2));

        float[] widths = aligner.alignColumnsMinWidth(2, 2, new IntVector2(1, 1), cells);
        assertFloatArrayEqual(expectedWiths, widths);
    }

    /**
     * 3x3 table
     * [10] [20] [15]
     * [   20  ] [10]
     * [5] [   30   ]
     * ->
     * [15] [20] [20]
     * :
     * [10] [20] [15]
     * [   30  ] [15]
     * [10] [   35  ]
     */
    @Test
    public void alignColumns_multipleColumnSpan() {
        float[] expectedWiths = {10, 20, 15};

        TableCell cell11 = makeNormalCell(10, X);
        TableCell cell12 = makeNormalCell(20, X);
        TableCell cell13 = makeNormalCell(15, X);

        TableCell cell21 = makeSpanningCell(20, 2, X);
        TableCell cell22 = makeSpannedCell(0, 1, X);
        TableCell cell23 = makeNormalCell(10, X);

        TableCell cell31 = makeNormalCell(5, X);
        TableCell cell32 = makeSpanningCell(30, 2, X);
        TableCell cell33 = makeSpannedCell(1, 2, X);

        List<TableCell> row1 = List.of(cell11, cell12, cell13);
        List<TableCell> row2 = List.of(cell21, cell22, cell23);
        List<TableCell> row3 = List.of(cell31, cell32, cell33);
        List<List<TableCell>> cells = new ArrayList<>(List.of(row1, row2, row3));

        float[] widths = aligner.alignColumnsMinWidth(3, 3, new IntVector2(0, 0), cells);
        assertFloatArrayEqual(expectedWiths, widths);
    }

    /**
     * 3x3 table
     * [10] [20] [15]
     * [   20  ] [10]
     * [5] [   41   ]
     * ->
     * [15] [20] [20]
     * :
     * [10] [23] [18]
     * [   33  ] [18]
     * [10] [   41  ]
     */
    @Test
    public void alignColumns_multipleColumnSpan_expandingFromSpan() {
        float[] expectedWiths = {10, 23, 18};

        TableCell cell11 = makeNormalCell(10, X);
        TableCell cell12 = makeNormalCell(20, X);
        TableCell cell13 = makeNormalCell(15, X);

        TableCell cell21 = makeSpanningCell(20, 2, X);
        TableCell cell22 = makeSpannedCell(0, 1, X);
        TableCell cell23 = makeNormalCell(10, X);

        TableCell cell31 = makeNormalCell(5, X);
        TableCell cell32 = makeSpanningCell(41, 2, X);
        TableCell cell33 = makeSpannedCell(1, 2, X);

        List<TableCell> row1 = List.of(cell11, cell12, cell13);
        List<TableCell> row2 = List.of(cell21, cell22, cell23);
        List<TableCell> row3 = List.of(cell31, cell32, cell33);
        List<List<TableCell>> cells = new ArrayList<>(List.of(row1, row2, row3));

        float[] widths = aligner.alignColumnsMinWidth(3, 3, new IntVector2(0, 0), cells);
        assertFloatArrayEqual(expectedWiths, widths);
    }

    /**
     * 3x3 table
     * [10*] [15] [10]
     * [   30   ] [20]
     * [      60     ]
     * ->
     * [10] [25] [25]
     * :
     * [10] [25] [25]
     * [   35  ] [25]
     * [     60     ]
     */
    @Test
    public void alignColumns_fixedWithSpan() {
        float[] expectedWiths = {10, 25, 25};

        TableCell cell11 = makeNormalFixedCell(10, X);
        TableCell cell12 = makeNormalCell(15, X);
        TableCell cell13 = makeNormalCell(10, X);

        TableCell cell21 = makeSpanningCell(30, 2, X);
        TableCell cell22 = makeSpannedCell(0, 1, X);
        TableCell cell23 = makeNormalCell(20, X);

        TableCell cell31 = makeSpanningCell(60, 3, X);
        TableCell cell32 = makeSpannedCell(0, 2, X);
        TableCell cell33 = makeSpannedCell(0, 2, X);

        List<TableCell> row1 = List.of(cell11, cell12, cell13);
        List<TableCell> row2 = List.of(cell21, cell22, cell23);
        List<TableCell> row3 = List.of(cell31, cell32, cell33);
        List<List<TableCell>> cells = new ArrayList<>(List.of(row1, row2, row3));

        float[] widths = aligner.alignColumnsMinWidth(3, 3, new IntVector2(0, 0), cells);
        assertFloatArrayEqual(expectedWiths, widths);
    }

    /**
     * 3x3 table
     * [      60     ]
     * [   30   ] [20]
     * [10*] [15] [10]
     * ->
     * [10] [25] [25]
     * :
     * [     60     ]
     * [   35  ] [25]
     * [10] [25] [25]
     */
    @Test
    public void alignColumns_fixedWithSpan_spansOnTop() {
        float[] expectedWiths = {10, 25, 25};

        TableCell cell11 = makeSpanningCell(60, 3, X);
        TableCell cell12 = makeSpannedCell(0, 0, X);
        TableCell cell13 = makeSpannedCell(0, 0, X);

        TableCell cell21 = makeSpanningCell(30, 2, X);
        TableCell cell22 = makeSpannedCell(0, 1, X);
        TableCell cell23 = makeNormalCell(20, X);

        TableCell cell31 = makeNormalFixedCell(10, X);
        TableCell cell32 = makeNormalCell(15, X);
        TableCell cell33 = makeNormalCell(10, X);

        List<TableCell> row1 = List.of(cell11, cell12, cell13);
        List<TableCell> row2 = List.of(cell21, cell22, cell23);
        List<TableCell> row3 = List.of(cell31, cell32, cell33);
        List<List<TableCell>> cells = new ArrayList<>(List.of(row1, row2, row3));

        float[] widths = aligner.alignColumnsMinWidth(3, 3, new IntVector2(0, 0), cells);
        assertFloatArrayEqual(expectedWiths, widths);
    }

    /**
     * 2x2 table
     * [10] [15]
     * [15] [12]
     * ->
     * [15]
     * [15]
     */
    @Test
    public void alignRows_basic2x2() {
        float[] expectedHeights = {15, 15};

        TableCell cell11 = makeNormalCell(10, Y);
        TableCell cell12 = makeNormalCell(15, Y);
        TableCell cell21 = makeNormalCell(15, Y);
        TableCell cell22 = makeNormalCell(12, Y);

        List<TableCell> row1 = List.of(cell11, cell12);
        List<TableCell> row2 = List.of(cell21, cell22);
        List<List<TableCell>> cells = new ArrayList<>(List.of(row1, row2));

        float[] heights = aligner.alignRowsMinHeight(2, 2, new IntVector2(0, 0), cells);
        assertFloatArrayEqual(heights, expectedHeights);
    }

    /**
     * 2x2 table
     * [10] [35]
     * [15]  ^^
     * ->
     * [15]
     * [20]
     */
    @Test
    public void alignRows_singleRowSpan() {
        float[] expectedHeights = {15, 20};

        TableCell cell11 = makeNormalCell(10, Y);
        TableCell cell12 = makeSpanningCell(35, 2, Y);
        TableCell cell21 = makeNormalCell(15, Y);
        TableCell cell22 = makeSpannedCell(1, 0, Y);

        List<TableCell> row1 = List.of(cell11, cell12);
        List<TableCell> row2 = List.of(cell21, cell22);
        List<List<TableCell>> cells = new ArrayList<>(List.of(row1, row2));

        float[] heights = aligner.alignRowsMinHeight(2, 2, new IntVector2(0, 0), cells);
        assertFloatArrayEqual(heights, expectedHeights);
    }

    private void assertFloatArrayEqual(float[] expected, float[] actual) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i], DELTA);
        }
    }

    private TableCell makeNormalCell(float minSize, String direction) {
        TableCell cell = new TableCell();
        cell.span = new IntVector2(1, 1);
        if (direction.equals(X)) cell.minWidth = minSize;
        if (direction.equals(Y)) cell.minHeight = minSize;
        return cell;
    }

    private TableCell makeNormalFixedCell(float size, String direction) {
        TableCell cell = new TableCell();
        cell.fixedWidth = true;
        cell.span = new IntVector2(1, 1);
        if (direction.equals(X)) {
            cell.minWidth = size;
            cell.maxWidth = size;
        } else if (direction.equals(Y)) {
            cell.minHeight = size;
            cell.maxHeight = size;
        }

        return cell;
    }

    private TableCell makeSpanningCell(float size, int span, String direction) {
        TableCell cell = new TableCell();
        if (direction.equals(X)) {
            cell.span = new IntVector2(span, 1);
            cell.minWidth = size;
        } else {
            cell.span = new IntVector2(1, span);
            cell.minHeight = size;
        }

        return cell;
    }

    private TableCell makeSpannedCell(int originX, int originY, String direction) {
        TableCell cell = new TableCell();
        if (direction.equals(X)) {
            cell.isSpannedX = true;
        } else if (direction.equals(Y)) {
            cell.isSpannedY = true;
        }
        cell.spannedCellOrigin = new IntVector2(originX, originY);
        return cell;
    }

}
