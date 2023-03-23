package browser.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import browser.app.Pipeline;
import browser.model.BoxNode;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class LayoutIntegrationTestDriver {

    private static final String testDataDirectory = "./src/test/resources/layoutIntegrationTests";
    private static final float layoutTolerance = 0.01f;

    private final Pipeline pipeline;

    // Public methods

    public LayoutIntegrationTestDriver(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public void runLayoutTest(String filename, int screenWidth) {
        runLayoutTest(filename, screenWidth, false);
    }

    public void runLayoutTest(String filename, int screenWidth, boolean log) {
        try {
            String inputFilePath = String.format("file://%s/html/%s.html", testDataDirectory, filename);
            pipeline.loadWebpage(inputFilePath);
            pipeline.calculateLayout(screenWidth);
            BoxNode rootBoxNode = pipeline.getRootRenderNode().boxNode;
            BoxNode expectedRootBoxNode = createExpectedBoxTree(filename);
            if (log) {
                System.out.printf("Expected:\n%s\nActual:\n%s\n", expectedRootBoxNode.toRecursiveString(), rootBoxNode.toRecursiveString());
            }
            assertBoxNodesEqual(expectedRootBoxNode, rootBoxNode);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    // Private methods

    private BoxNode createExpectedBoxTree(String filename) throws IOException {
        Document document = Jsoup.parse(Paths.get(String.format("%s/html/%s_layout.html", testDataDirectory, filename)).toFile());
        if (document.children().size() != 1) {
            fail(String.format("Expected 1 root element for test file %s but got %d.", filename, document.body().children().size()));
        }

        BoxNode rootBoxNode = null;
        List<Element> queue = new ArrayList<>();
        Map<Element, BoxNode> elementBoxNodeMap = new HashMap<>();
        queue.add(document.child(0));
        while (!queue.isEmpty()) {
            Element currentElement = queue.remove(0);

            // Jsoup injects HTML and HEAD tags automatically. They are not used in layout checking.
            if (currentElement.tagName().equals("head")) {
                continue;
            } else if (currentElement.tagName().equals("html")) {
                queue.addAll(currentElement.children());
                continue;
            }

            BoxNode boxNode = createBoxNodeFromElement(currentElement);

            if (elementBoxNodeMap.containsKey(currentElement.parent())) {
                boxNode.parent = elementBoxNodeMap.get(currentElement.parent());
                boxNode.parent.children.add(boxNode);
            } else {
                rootBoxNode = boxNode;
            }

            elementBoxNodeMap.put(currentElement, boxNode);
            queue.addAll(currentElement.children());
        }

        return rootBoxNode;
    }

    private BoxNode createBoxNodeFromElement(Element element) {
        BoxNode boxNode = new BoxNode();

        boxNode.x = Float.parseFloat(element.attr("x"));
        boxNode.y = Float.parseFloat(element.attr("y"));
        boxNode.width = Float.parseFloat(element.attr("width"));
        boxNode.height = Float.parseFloat(element.attr("height"));
        if (element.hasAttr("textStartIndex")) {
            boxNode.textStartIndex = Integer.parseInt(element.attr("textStartIndex"));
            boxNode.textEndIndex = Integer.parseInt(element.attr("textEndIndex"));
        }
        List<String> layoutAttributes = List.of("x", "y", "width", "height", "textStartIndex", "textEndIndex");
        List<Attribute> styleAttributes = element.attributes().asList().stream().filter(attr -> !layoutAttributes.contains(attr.getKey())).toList();
        for (Attribute styleAttribute : styleAttributes) {
            // TODO use reflection to check if the boxNode.style has a matching field, and set the value if so.
        }
        return boxNode;
    }

    private void assertBoxNodesEqual(BoxNode expected, BoxNode actual) {
        assertEquals(expected.x, actual.x, layoutTolerance);
        assertEquals(expected.y, actual.y, layoutTolerance);
        assertEquals(expected.width, actual.width, layoutTolerance);
        assertEquals(expected.height, actual.height, layoutTolerance);
        assertEquals(expected.textStartIndex, actual.textStartIndex);
        assertEquals(expected.textEndIndex, actual.textEndIndex);
        assertEquals(expected.children.size(), actual.children.size());
        for (int i = 0; i < expected.children.size(); i++) {
            assertBoxNodesEqual(expected.children.get(i), actual.children.get(i));
        }
    }

}
