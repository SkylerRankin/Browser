package browser.app;

import browser.util.LayoutIntegrationTestDriver;

import org.junit.Before;
import org.junit.Test;

/**
 * These tests take standard input resources (HTML files, CSS files, images, etc.) and run all steps from parsing to
 * layout. Only the rendering step is not done.
 *
 * Each test case provides the following two files:
 * 1. An HTML file that acts as the page to be laid out. This file may reference other resources.
 * 2. An HTML file that includes information on the expected layout. This file is assumed to have the same name as the
 *    input file, but with a _layout suffix.
 *
 * The expected layout file should define the expected HTML structure, and provide the size and positions of each box.
 * Anonymous boxes should be included in this file using an <anon> tag. Any text anonymous boxes should include the
 * textStartIndex and textEndIndex attributes. The actual text content of the HTML element is not used in testing, but
 * can be added to make the test case more clear than with indices alone.
 *
 * Example:
 * <div x="0" y=v0" width=v100" height="40">
 *     <div x="10" y="10" width="80" height="20">
 *         <anon x="10" y="10" width="23.44" height="13" textStartIndex="0" textEndIndex="10">
 *             anonymous block text
 *         </anon>
 *     </div>
 * </div>
 *
 * Additionally, any tag can include a key-value pair that will be interpreted as a styling value.
 *
 * Example:
 * <div x="0" y="0" width="100" height="40" color="red">
 *     <div x="10" y="10" width="80" height="20" widthType="PERCENTAGE">
 *         <anon x="10" y="10" width="23.44" height="13" textStartIndex="0" textEndIndex="10">
 *             anonymous block text
 *         </anon>
 *     </div>
 * </div>
 */
public class LayoutIntegrationTests {

    private LayoutIntegrationTestDriver driver;

    @Before
    public void setup() {
        Pipeline.init();
        driver = new LayoutIntegrationTestDriver(new Pipeline());
    }

    // Flow tests

    @Test
    public void stackedPaddingTest() {
        driver.runLayoutTest("stackedPadding", 1000);
    }

    @Test
    public void blockBoxNoChildren() {
        driver.runLayoutTest("blockBoxNoChildren", 500);
    }

    @Test
    public void simpleLineBreaks() {
        driver.runLayoutTest("simpleLineBreaks", 100);
    }

    @Test
    public void basicInferredInlineSpacing() {
        driver.runLayoutTest("basicInferredInlineSpacing", 100);
    }

    @Test
    public void preserveInlineSpaces() {
        driver.runLayoutTest("preserveInlineSpaces", 200);
    }

    @Test
    public void marginAuto() {
        driver.runLayoutTest("marginAuto", 1000);
    }

    @Test
    public void displayNone() {
        driver.runLayoutTest("displayNone", 1000);
    }

    @Test
    public void splitInlineTextEndingWithSpace() {
        driver.runLayoutTest("splitInlineTextEndingWithSpace", 1000);
    }

    @Test
    public void simplePre() {
        driver.runLayoutTest("simplePre", 1000);
    }

    @Test
    public void preWithInternalTags() {
        driver.runLayoutTest("preWithInternalTags", 1000);
    }

    @Test
    public void multiLevelInlinePartition() {
        driver.runLayoutTest("multiLevelInlinePartition", 1000);
    }

    // Text alignment

    @Test
    public void simpleTextAlignCenter() {
        driver.runLayoutTest("simpleTextAlignCenter", 500);
    }

    @Test
    public void mixedTextAlign() {
        driver.runLayoutTest("mixedTextAlign", 100);
    }

    @Test
    public void alignmentMultipleRows() {
        driver.runLayoutTest("alignmentMultipleRows", 100);
    }

    @Test
    public void alignmentAnonymousBlockBox() {
        driver.runLayoutTest("alignmentAnonymousBlockBox", 100);
    }

    @Test
    public void alignSplitText() {
        driver.runLayoutTest("alignSplitText", 1000);
    }

    // List tests

    @Test
    public void simpleOrderedList() {
        driver.runLayoutTest("simpleOrderedList", 400);
    }

    @Test
    public void nestedList() {
        driver.runLayoutTest("nestedList", 500);
    }

    @Test
    public void inlineBlockList() {
        driver.runLayoutTest("inlineBlockList", 500);
    }

    @Test
    public void listThatsActuallyJustADiv() {
        driver.runLayoutTest("listThatsActuallyJustADiv", 400);
    }

    @Test
    public void inlineListWithInlineItems() {
        driver.runLayoutTest("inlineListWithInlineItems", 500);
    }

    @Test
    public void nestedListWithoutItem() {
        driver.runLayoutTest("nestedListWithoutItem", 1000);
    }

    // Table tests

    @Test
    public void simplestTable() {
        driver.runLayoutTest("simplestTable", 500);
    }

    @Test
    public void tableFixedWidthAutoCells() {
        driver.runLayoutTest("tableFixedWidthAutoCells", 500);
    }

    @Test
    public void tableFixedWidthOneFixedColumn() {
        driver.runLayoutTest("tableFixedWidthOneFixedColumn", 500);
    }

    @Test
    public void tableVaryingCellHeights() {
        driver.runLayoutTest("tableVaryingCellHeights", 500);
    }

    @Test
    public void simpleMultiColumnSpanTable() {
        driver.runLayoutTest("simpleMultiColumnSpanTable", 500);
    }

    @Test
    public void simpleMultiRowSpanTable() {
        driver.runLayoutTest("simpleMultiRowSpanTable", 500);
    }

    @Test
    public void tableMixedSpans() {
        driver.runLayoutTest("tableMixedSpans", 500);
    }

    @Test
    public void tableCellSpanningRowAndCol() {
        driver.runLayoutTest("tableCellSpanningRowAndCol", 500);
    }

    @Test
    public void inlineTable() {
        driver.runLayoutTest("inlineTable", 200);
    }

    @Test
    public void tableInTable() {
        driver.runLayoutTest("tableInTable", 200);
    }

    @Test
    public void tableInlineBlockCells() {
        driver.runLayoutTest("tableInlineBlockCells", 500);
    }

    @Test
    public void tableCellMismatch() {
        driver.runLayoutTest("tableCellMismatch", 1000);
    }

    @Test
    public void tableBlockDisplay() {
        driver.runLayoutTest("tableBlockDisplay", 1000);
    }

    @Test
    public void tableEmptyRow() {
        driver.runLayoutTest("tableEmptyRow", 1000);
    }

    // Inline block tests

    @Test
    public void inlineBlockWithinInline() {
        driver.runLayoutTest("inlineBlockWithinInline", 500);
    }

    // CSS specific tests

    @Test
    public void basicCSSSelectors() {
        driver.runLayoutTest("basicCSSSelectors", 100);
    }

    @Test
    public void multipleTypeSelectors() {
        driver.runLayoutTest("multipleTypeSelectors", 1000);
    }

    @Test
    public void mediaQueryWidthRange() {
        driver.runLayoutTest("mediaQueryWidthRange", 500);
    }

}
