package browser.layout;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import browser.app.Pipeline;
import browser.model.BoxNode;
import browser.model.Vector2;
import browser.util.TestDataLoader;

import org.junit.Before;
import org.junit.Test;

public class BoxLayoutGeneratorTest {

    private BoxLayoutGenerator boxLayoutGenerator;
    private TextDimensionCalculator textDimensionCalculator;

    @Before
    public void setup() {
        Pipeline.init();
        textDimensionCalculator = mock(TextDimensionCalculator.class);
        boxLayoutGenerator = new BoxLayoutGenerator(textDimensionCalculator);
    }

    private void setTextDimensionOverride(int width, int height) {
        when(textDimensionCalculator.getDimension(anyString(), any()))
                .thenAnswer(i -> {
                    float x = i.getArgument(0, String.class).length() * width;
                    return new Vector2(x, height);
                });
    }

    @Test
    public void simpleTextInPInDiv() {
        TestDataLoader.TestData testData = TestDataLoader.loadLayoutTrees("textInPInDiv");
        setTextDimensionOverride(testData.letterWidth, testData.letterHeight);
        BoxNode rootBoxNode = testData.rootBoxNode;
        boxLayoutGenerator.calculateLayout(rootBoxNode, testData.screenWidth);
        assertEquals(testData.rootBoxNodeAfterLayout, rootBoxNode);
    }

    @Test
    public void simpleTextInPInDivWithOverflow() {
        TestDataLoader.TestData testData = TestDataLoader.loadLayoutTrees("textInPInDivWithOverflow");
        setTextDimensionOverride(testData.letterWidth, testData.letterHeight);
        BoxNode rootBoxNode = testData.rootBoxNode;
        boxLayoutGenerator.calculateLayout(rootBoxNode, testData.screenWidth);
        assertEquals(testData.rootBoxNodeAfterLayout, rootBoxNode);
    }

    @Test
    public void simpleInlineSpans() {
        testLayout("singleLineSpansInDiv");
    }

    @Test
    public void moveInlineParentsToNextLine() {
        TestDataLoader.TestData testData = TestDataLoader.loadLayoutTrees("moveInlineParentsToNextLine");
        setTextDimensionOverride(testData.letterWidth, testData.letterHeight);
        BoxNode rootBoxNode = testData.rootBoxNode;
        boxLayoutGenerator.calculateLayout(rootBoxNode, testData.screenWidth);
        assertBoxesEqualIgnoreIds(testData.rootBoxNodeAfterLayout, rootBoxNode);
    }

    @Test
    public void simpleSplitTextSpans() {
        TestDataLoader.TestData testData = TestDataLoader.loadLayoutTrees("splitTextSpanInDiv");
        setTextDimensionOverride(testData.letterWidth, testData.letterHeight);
        BoxNode rootBoxNode = testData.rootBoxNode;
        boxLayoutGenerator.calculateLayout(rootBoxNode, testData.screenWidth);
        assertBoxesEqualIgnoreIds(testData.rootBoxNodeAfterLayout, rootBoxNode);
    }

    /**
     * <div id=0>
     *     <span1 id=1>
     *         <span2 id=2>
     *             A block formatting context (BFC) is a part of a visual CSS rendering of a web page.
     *         </span2>
     *         <span3 id=4>
     *             It's the region in which the layout of block boxes occurs and in which floats interact with other elements.
     *         </span3>
     *     </span1>
     *     <span4 id=6>
     *         Formatting contexts affect layout, but typically
     *     </span4>
     * </div>
     */
    @Test
    public void splitTextSpansWithSpacing() {
        testLayout("inlineTextSplitsWithSpacing");
    }

    /**
     * <div id=0, padding=2>
     *     <span1 id=1, marginLeft=2, borderWidthLeft=1, paddingLeft=1, paddingRight=1, borderWidthRight=1, marginRight=2, borderWidthTop=1, borderWidthBottom=1>
     *         <span2 id=2, marginLeft=1, borderWidthLeft=2, paddingLeft=2, paddingRight=2, marginRight=1>
     *             A block formatting context (BFC) is a part of a visual CSS rendering of a web page.
     *         </span2>
     *         <span3 id=4, marginLeft=1, paddingLeft=2, paddingRight=2, borderWidthRight=2, marginRight=4>
     *             It's the region in which the layout of block boxes occurs and in which floats interact with other elements.
     *         </span3>
     *     </span1>
     *     <span4 id=6, marginLeft=1, borderWidthLeft=3, paddingLeft=2, paddingRight=2, borderWidthRight=1, marginRight=1, borderWidthBottom=2>
     *         Formatting contexts affect layout, but typically
     *     </span4>
     * </div>
     */
    @Test
    public void splitTextSpansWithSpacingAndBorders() {
        testLayout("inlineTextSplitsWithSpacingAndBorders");
    }

    @Test
    public void newLineIfNoRoomForInlineMargin() {
        testLayout("marginForcesNewLine");
    }

    /**
     * <div padding=20>
     *     <div marginLeft=10, marginRight=20, paddingTop=5>
     *         <div height=5></div>
     *     </div>
     * </div>
     */
    @Test
    public void simpleBlockSpacing() {
        TestDataLoader.TestData testData = TestDataLoader.loadLayoutTrees("simpleBlockSpacing");
        setTextDimensionOverride(testData.letterWidth, testData.letterHeight);
        BoxNode rootBoxNode = testData.rootBoxNode;
        boxLayoutGenerator.calculateLayout(rootBoxNode, testData.screenWidth);
        assertBoxesEqualIgnoreIds(testData.rootBoxNodeAfterLayout, rootBoxNode);
    }

    /**
     * <div1 style="padding: 10">
     *     <div2 style="margin-bottom: 20, width: 20, height: 20">
     *         <div3 style="margin-left: 5, margin-right: 5">title</div>
     *         <div4>
     *             <span style="margin-left: 2">subtitle</span>
     *         </div>
     *     </div2>
     *     <div5>
     *         <span>
     *             Some text.
     *         </span>
     *     </div>
     *     <div6 style="padding: 20">
     *         Scientists seek a single description of reality. But <i>modern</i> physics allows for many different descriptions, many equivalent to one another, connected through a vast landscape of mathematical possibility.
     *     </div>
     * </div2>
     */
    @Test
    public void mixedInlineBlock() {
        TestDataLoader.TestData testData = TestDataLoader.loadLayoutTrees("mixedInlineBlock");
        setTextDimensionOverride(testData.letterWidth, testData.letterHeight);
        BoxNode rootBoxNode = testData.rootBoxNode;
        boxLayoutGenerator.calculateLayout(rootBoxNode, testData.screenWidth);
        assertBoxesEqualIgnoreIds(testData.rootBoxNodeAfterLayout, rootBoxNode);
    }

    /**
     * <div1>
     *      <div2 style="width: 70px; box-sizing: content-box; padding: 10px; borderWidth: 1px;">
     *          first div
     *      </div2>
     *      <div3 style="width: 70px; box-sizing: border-box; padding: 10px; borderWidth: 1px;">
     *          second div
     *      </div3>
     * </div1>
     */
    @Test
    public void boxSizing() {
        testLayout("boxSizing");
    }

    /**
     * <div style="width: 50px; background-color: bisque;">
     *      <div style="padding: 10px;">a</div>
     *      <div style="width: 100%; padding: 10px; box-sizing: content-box;">b</div>
     *      <div style="width: 100%; padding: 10px; box-sizing: border-box;">c</div>
     * </div>
     */
    @Test
    public void boxSizingBlockDefault() {
        testLayout("boxSizingBlockDefault");
    }

    /**
     * <div style="width: 100px;">
     *     <div style="box-sizing: content-box; width: 50px; height: 50px; border-color: aquamarine; border-style: solid; border-top-width: 10px; border-bottom-width: 10px; border-left-width: 5px; border-right-width: 2px;">
     *          div with border
     *     </div>
     *     <div style="box-sizing: border-box; width: 50px; height: 50px; border-color: aquamarine; border-style: solid; border-top-width: 10px; border-bottom-width: 10px; border-left-width: 5px; border-right-width: 2px;">
     *          div with border
     *     </div>
     * </div>
     */
    @Test
    public void simpleDivWithBorder() {
        testLayout("simpleDivWithBorder");
    }

    @Test
    public void horizontalInlineBlockDivs() {
        TestDataLoader.TestData testData = TestDataLoader.loadLayoutTrees("simpleInlineBlock");
        setTextDimensionOverride(testData.letterWidth, testData.letterHeight);
        BoxNode rootBoxNode = testData.rootBoxNode;
        boxLayoutGenerator.calculateLayout(rootBoxNode, testData.screenWidth);
        assertBoxesEqualIgnoreIds(testData.rootBoxNodeAfterLayout, rootBoxNode);
    }

    private void testLayout(String filename) {
        testLayout(filename, false);
    }

    private void testLayout(String filename, boolean log) {
        TestDataLoader.TestData testData = TestDataLoader.loadLayoutTrees(filename);
        setTextDimensionOverride(testData.letterWidth, testData.letterHeight);
        BoxNode rootBoxNode = testData.rootBoxNode;
        boxLayoutGenerator.calculateLayout(rootBoxNode, testData.screenWidth);
        if (log) {
            System.out.printf("Expected:\n%s\nActual:\n%s\n", testData.rootBoxNodeAfterLayout.toRecursiveString(), rootBoxNode.toRecursiveString());
        }
        assertBoxesEqualIgnoreIds(testData.rootBoxNodeAfterLayout, rootBoxNode);
    }

    private void assertBoxesEqualIgnoreIds(BoxNode box1, BoxNode box2) {
        assertEquals(box1.outerDisplayType, box2.outerDisplayType);
        assertEquals(box1.innerDisplayType, box2.innerDisplayType);
        assertEquals(box1.isAnonymous, box2.isAnonymous);
        assertEquals(box1.isTextNode, box2.isTextNode);
        assertEquals(box1.parent == null, box2.parent == null);
        assertEquals(box1.children.size(), box2.children.size());
        assertEquals(box1.renderNodeId, box2.renderNodeId);
        assertEquals(box1.x, box2.x);
        assertEquals(box1.y, box2.y);
        assertEquals(box1.width, box2.width);
        assertEquals(box1.height, box2.height);
        assertEquals(box1.textStartIndex, box2.textStartIndex);
        assertEquals(box1.textEndIndex, box2.textEndIndex);

        for (int i = 0; i < box1.children.size(); i++) {
            assertBoxesEqualIgnoreIds(box1.children.get(i), box2.children.get(i));
        }
    }

}
