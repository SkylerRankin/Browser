package browser.app;

import java.io.IOException;

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

    @Test
    public void stackedPaddingTest() throws IOException {
        driver.runLayoutTest("stackedPadding", 1000);
    }

    // List tests

    @Test
    public void simpleOrderedList() throws IOException {
        driver.runLayoutTest("simpleOrderedList", 400);
    }

    @Test
    public void nestedList() throws IOException {
        driver.runLayoutTest("nestedList", 500);
    }

    @Test
    public void inlineBlockList() throws IOException {
        driver.runLayoutTest("inlineBlockList", 500);
    }

    @Test
    public void listThatsActuallyJustADiv() throws IOException {
        driver.runLayoutTest("listThatsActuallyJustADiv", 400);
    }

    @Test
    public void inlineListWithInlineItems() throws IOException {
        driver.runLayoutTest("inlineListWithInlineItems", 500);
    }

    // Table tests

    @Test
    public void simplestTable() throws IOException {
        driver.runLayoutTest("simplestTable", 500);
    }

    @Test
    public void tableFixedWidthAutoCells() throws IOException {
        driver.runLayoutTest("tableFixedWidthAutoCells", 500);
    }

    @Test
    public void tableFixedWidthOneFixedColumn() throws IOException {
        driver.runLayoutTest("tableFixedWidthOneFixedColumn", 500);
    }

    @Test
    public void tableVaryingCellHeights() throws IOException {
        driver.runLayoutTest("tableVaryingCellHeights", 500);
    }

    @Test
    public void simpleMultiColumnSpanTable() throws IOException {
        driver.runLayoutTest("simpleMultiColumnSpanTable", 500);
    }

    @Test
    public void simpleMultiRowSpanTable() throws IOException {
        driver.runLayoutTest("simpleMultiRowSpanTable", 500);
    }

    @Test
    public void tableMixedSpans() throws IOException {
        driver.runLayoutTest("tableMixedSpans", 500);
    }

    @Test
    public void tableCellSpanningRowAndCol() throws IOException {
        driver.runLayoutTest("tableCellSpanningRowAndCol", 500);
    }

    @Test
    public void inlineTable() throws IOException {
        driver.runLayoutTest("inlineTable", 200);
    }

    @Test
    public void tableInTable() throws IOException {
        driver.runLayoutTest("tableInTable", 200);
    }

}
