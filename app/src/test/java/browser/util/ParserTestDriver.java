package browser.util;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import browser.model.DOMNode;
import browser.parser.HTMLParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class ParserTestDriver {

    private static final String testDataDirectory = "./src/test/resources/htmlParserTests";

    public void runParseTest(String filename) {
        runParseTest(filename, false);
    }

    public void runParseTest(String filename, boolean log) {
        try {
            Path inputPath = Paths.get(String.format("%s/%s.html", testDataDirectory, filename));
            String input = Files.readString(inputPath);
            DOMNode expectedRoot = createExpectedDOMTree(filename);
            HTMLParser parser = new HTMLParser();
            DOMNode actualRoot = parser.generateDOMTree(input);
            if (log) {
                System.out.printf("Expected:\n%s\nActual:\n%s\n", expectedRoot.toRecursiveString(), actualRoot.toRecursiveString());
            }
            assertDOMTreesEqual(expectedRoot, actualRoot);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
    }

    private DOMNode createExpectedDOMTree(String filename) throws IOException {
        Document document = Jsoup.parse(Paths.get(String.format("%s/%s_dom.html", testDataDirectory, filename)).toFile());
        if (document.children().size() != 1) {
            fail(String.format("Expected 1 root element for test file %s but got %d.", filename, document.body().children().size()));
        }

        DOMNode rootDOMNode = null;
        Map<Element, DOMNode> elementDOMNodeMap = new HashMap<>();
        List<Element> queue = new ArrayList<>();
        queue.add(document.child(0));

        while (!queue.isEmpty()) {
            Element currentElement = queue.remove(0);

            DOMNode domNode = createDOMNode(currentElement);
            if (elementDOMNodeMap.containsKey(currentElement.parent())) {
                domNode.parent = elementDOMNodeMap.get(currentElement.parent());
                domNode.parent.children.add(domNode);
            } else {
                rootDOMNode = domNode;
            }

            elementDOMNodeMap.put(currentElement, domNode);
            queue.addAll(currentElement.children());
        }

        return rootDOMNode;
    }

    private void assertDOMTreesEqual(DOMNode expected, DOMNode actual) {
        assertEquals(expected.type, actual.type);
        assertEquals(expected.content, actual.content);
        assertEquals(expected.attributes.size(), actual.attributes.size());
        for (String key : expected.attributes.keySet()) {
            assertEquals(expected.attributes.get(key), actual.attributes.get(key));
        }
        assertEquals(expected.children.size(), actual.children.size());
        assertEquals(expected.whiteSpaceAfter, actual.whiteSpaceAfter);
        assertEquals(expected.parent == null, actual.parent == null);
        if (expected.parent != null) {
            assertEquals(expected.parent.type, actual.parent.type);
        }
        for (int i = 0; i < expected.children.size(); i++) {
            assertDOMTreesEqual(expected.children.get(i), actual.children.get(i));
        }
    }

    private DOMNode createDOMNode(Element element) {
        DOMNode domNode = new DOMNode(element.tagName());

        if (element.tagName().equals("text")) {
            domNode.content = element.wholeOwnText();
        }

        for (Attribute attribute : element.attributes().asList()) {
            if (attribute.getKey().equals("nw")) {
                continue;
            }
            domNode.attributes.put(attribute.getKey(), attribute.getValue());
        }

        domNode.whiteSpaceAfter = !element.hasAttr("nw");

        // JSoup won't parse HTML within script or style tags, so the text nodes need to be added manually.
        if (domNode.type.equals("script") || domNode.type.equals("style")) {
            DOMNode text = new DOMNode("text");
            text.content = element.data();
            domNode.addChild(text);
        }

        return domNode;
    }
}
