package browser.app;

import static browser.constants.MathConstants.DELTA;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import browser.css.CSSStyle;
import browser.layout.TextDimensionCalculator;
import browser.model.BoxNode;

import browser.model.Vector2;
import browser.util.LayoutIntegrationTestDriver;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
}
