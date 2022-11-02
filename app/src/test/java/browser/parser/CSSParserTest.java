package browser.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import browser.model.CSSNode;
import browser.parser.CSSParser.Selector;

import org.junit.Test;

public class CSSParserTest {

    @Test
    public void testGenerateCSSOM_Body_Only() {
        final String css = "/* test styles */\r\n\r\n" +
                "* {\r\n" +
                "    font-size: 120%;\r\n" +
                "    margin: 10px;\r\n" +
                "}\r\n\r\n" +
                "/* \r\n" +
                "    Some more test styles\r\n" +
                "*/\r\n\r\n" +
                "* { color: 0x003300; margin: 12px;}";
        CSSParser parser = new CSSParser();
        parser.parse(css);
        CSSNode actual = parser.getCSSOM();

        CSSNode expected = new CSSNode("default");
        CSSNode body = new CSSNode(HTMLElements.BODY);
        body.declarations.put("font-size", "120%");
        body.declarations.put("margin", "12px");
        body.declarations.put("color", "0x003300");
        body.parent = expected;
        expected.children.add(body);

        assertEquals(expected, actual);
    }

    /* Maybe the CSSOM shouldn't contain the classes and ids, just the element tree
        use two other maps for classes and ids
    */

    @Test
    public void testGenerateCSSOM_Elements() {
        final String css = "* {\r\n" +
                "    font-size: 60%;\r\n" +
                "}\r\n" +
                "\r\n" +
                "h1 {\r\n" +
                "    margin: 0;\r\n" +
                "}\r\n" +
                "\r\n" +
                "h2 h3 { font-size: 200%; }\r\n" +
                "p, div { font-family: Roboto; }";
        CSSParser parser = new CSSParser();
        parser.parse(css);
        CSSNode actual = parser.getCSSOM();

        CSSNode expected = new CSSNode("default");
        CSSNode body = new CSSNode(HTMLElements.BODY);
        body.declarations.put("font-size", "60%");
        body.parent = expected;
        expected.children.add(body);

        CSSNode h1 = new CSSNode(HTMLElements.H1);
        h1.parent = body;
        h1.declarations.put("margin", "0");
        body.children.add(h1);

        CSSNode h2 = new CSSNode(HTMLElements.H2);
        h2.parent = body;
        body.children.add(h2);

        CSSNode h3 = new CSSNode(HTMLElements.H3);
        h3.declarations.put("font-size", "200%");
        h3.parent = h2;
        h2.children.add(h3);

        CSSNode p = new CSSNode(HTMLElements.P);
        p.declarations.put("font-family", "Roboto");
        p.parent = body;
        body.children.add(p);

        CSSNode div = new CSSNode(HTMLElements.DIV);
        div.declarations.put("font-family", "Roboto");
        body.children.add(div);

        assertEquals(expected, actual);
    }

    @Test
    public void testRemoveComments() {
        final String css = "/* comment comment */ body { color: black } /* comment comment */ /* comment comment */ #title { font-size: 10; }";
        final String expected = " body { color: black }   #title { font-size: 10; }";
        final String actual = new CSSParser().removeComments(css);
        assertEquals(expected, actual);
    }

    @Test
    public void testParseRules_Elements() {
        final String css =
                "/* Test CSS file\r\nfor Java browser. */" +
                        "h1 {     color: black;     font-size: 120%; }" +
                        "h2, h3 {    float: right;}" +
                        "p { display: none; }";
        CSSParser parser = new CSSParser();
        parser.parse(css);

        Map<Selector, Map<String, String>> actual = parser.getRules();
        Map<Selector, Map> expected = new HashMap<Selector, Map>();

        Selector s1 = parser.new Selector(CSSParser.SelectorType.ELEMENT);
        s1.values.add("h1");
        Map<String, String> m1 = new HashMap<String, String>();
        m1.put("color", "black");
        m1.put("font-size", "120%");
        expected.put(s1, m1);

        Selector s2 = parser.new Selector(CSSParser.SelectorType.ELEMENT);
        Selector s4 = parser.new Selector(CSSParser.SelectorType.ELEMENT);
        s2.values.add("h2");
        s4.values.add("h3");
        Map<String, String> m2 = new HashMap<String, String>();
        m2.put("float", "right");
        expected.put(s2, m2);
        expected.put(s4, m2);

        Selector s3 = parser.new Selector(CSSParser.SelectorType.ELEMENT);
        s3.values.add("p");
        Map<String, String> m3 = new HashMap<String, String>();
        m3.put("display", "none");
        expected.put(s3, m3);

        for (Entry<Selector, Map> e : expected.entrySet()) {
            assertTrue(actual.keySet().contains(e.getKey()));
            assertTrue(actual.get(e.getKey()).equals(e.getValue()));
        }
    }

    @Test
    public void testGetRules_Repeated_Elements() {
        final String css =
                "h1 { color: black; font-size: 120%; }" +
                        "h2, h3 {float: right;}" +
                        "h1 { color: blue; }" +
                        "h2, h3 {float: left;}";

        CSSParser parser = new CSSParser();
        parser.parse(css);
        Map<Selector, Map<String, String>> actual = parser.getRules();
        Map<Selector, Map> expected = new HashMap<Selector, Map>();

        Selector s1 = parser.new Selector(CSSParser.SelectorType.ELEMENT);
        s1.values.add("h1");
        Map<String, String> m1 = new HashMap<String, String>();
        m1.put("color", "blue");
        m1.put("font-size", "120%");
        expected.put(s1, m1);

        Selector s2 = parser.new Selector(CSSParser.SelectorType.ELEMENT);
        Selector s3 = parser.new Selector(CSSParser.SelectorType.ELEMENT);
        s2.values.add("h2");
        s3.values.add("h3");
        Map<String, String> m2 = new HashMap<String, String>();
        m2.put("float", "left");
        expected.put(s2, m2);
        expected.put(s3, m2);

        for (Entry<Selector, Map> e : expected.entrySet()) {
            assertTrue(actual.keySet().contains(e.getKey()));
            assertTrue(actual.get(e.getKey()).equals(e.getValue()));
        }
    }

    @Test
    public void testGetRules_no_last_semicolon() {
        final String css =
                "h1 { color: black; font-size: 120% }" +
                        "h2, h3 {float: right}";

        CSSParser parser = new CSSParser();
        parser.parse(css);
        parser.printRules();
        Map<Selector, Map<String, String>> actual = parser.getRules();
        Map<Selector, Map> expected = new HashMap<Selector, Map>();

        Selector s1 = parser.new Selector(CSSParser.SelectorType.ELEMENT);
        s1.values.add("h1");
        Map<String, String> m1 = new HashMap<String, String>();
        m1.put("color", "black");
        m1.put("font-size", "120%");
        expected.put(s1, m1);

        Selector s2 = parser.new Selector(CSSParser.SelectorType.ELEMENT);
        Selector s3 = parser.new Selector(CSSParser.SelectorType.ELEMENT);
        s2.values.add("h2");
        s3.values.add("h3");
        Map<String, String> m2 = new HashMap<String, String>();
        m2.put("float", "right");
        expected.put(s2, m2);
        expected.put(s3, m2);

        for (Entry<Selector, Map> e : expected.entrySet()) {
            assertTrue(actual.keySet().contains(e.getKey()));
            assertTrue(actual.get(e.getKey()).equals(e.getValue()));
        }
    }

    @Test
    public void testGenerateIDMap_Empty() {
        final String css =
                "h1 { color: black; font-size: 120%; }" +
                        "h1 { color: blue; }";

        CSSParser parser = new CSSParser();
        parser.parse(css);

        Map<String, Map<String, String>> actual = parser.generateIDMap();
        assertEquals(0, actual.size());
    }

    @Test
    public void testGenerateIDMap() {
        final String css =
                "h1 { color: black; font-size: 120%; }" +
                        "#title {color: black; font-size: 120%;}" +
                        "h1 { color: blue; }" +
                        "#subtitle {float: left;}";

        CSSParser parser = new CSSParser();
        parser.parse(css);

        Map<String, Map<String, String>> actual = parser.generateIDMap();
        Map<String, Map<String, String>> expected = new HashMap<String, Map<String, String>>();
        HashMap<String, String> m1 = new HashMap<String, String>();
        m1.put("color", "black");
        m1.put("font-size", "120%");
        expected.put("title", m1);
        HashMap<String, String> m2 = new HashMap<String, String>();
        m2.put("float", "left");
        expected.put("subtitle", m2);

        assertEquals(expected.size(), actual.size());
        for (Entry<String, Map<String, String>> e : expected.entrySet()) {
            assertTrue(actual.keySet().contains(e.getKey()));
            assertTrue(actual.get(e.getKey()).equals(e.getValue()));
        }
    }

    @Test
    public void testParseSelector_CLASS() {
        CSSParser parser = new CSSParser();

        // Single class
        CSSParser.Selector actual = parser.parseSelector(".title");
        CSSParser.Selector expected = parser.new Selector(CSSParser.SelectorType.CLASS);
        expected.values.add("title");
        assertEquals(expected, actual);

        // Multiple classes
        actual = parser.parseSelector(".title.pointer.row-4");
        expected = parser.new Selector(CSSParser.SelectorType.CLASS);
        expected.values.add("title");
        expected.values.add("pointer");
        expected.values.add("row-4");
        assertEquals(expected, actual);
    }

    @Test
    public void testParseSelector_NESTED_CLASS() {
        CSSParser parser = new CSSParser();
        CSSParser.Selector actual = parser.parseSelector(".title .primary .col");
        CSSParser.Selector expected = parser.new Selector(CSSParser.SelectorType.NESTED_CLASS);
        expected.values.add("title");
        expected.values.add("primary");
        expected.values.add("col");
        assertEquals(expected, actual);
    }

    @Test
    public void testParseSelector_ID() {
        CSSParser parser = new CSSParser();
        CSSParser.Selector actual = parser.parseSelector("#title");
        CSSParser.Selector expected = parser.new Selector(CSSParser.SelectorType.ID);
        expected.values.add("title");
        assertEquals(expected, actual);
    }

    @Test
    public void testParseSelector_ALL() {
        CSSParser parser = new CSSParser();
        CSSParser.Selector actual = parser.parseSelector("*");
        CSSParser.Selector expected = parser.new Selector(CSSParser.SelectorType.ALL);
        assertEquals(expected, actual);
    }

    @Test
    public void testParseSelector_ELEMENT() {
        CSSParser parser = new CSSParser();

        // Single element
        CSSParser.Selector actual = parser.parseSelector("div");
        CSSParser.Selector expected = parser.new Selector(CSSParser.SelectorType.ELEMENT);
        expected.values.add("div");
        assertEquals(expected, actual);

        // Multiple classes
        actual = parser.parseSelector("div, p,span");
        expected = parser.new Selector(CSSParser.SelectorType.ELEMENT);
        expected.values.add("div");
        expected.values.add("p");
        expected.values.add("span");
        assertEquals(expected, actual);
    }

    @Test
    public void testParseSelector_NESTED_ELEMENT() {
        CSSParser parser = new CSSParser();
        CSSParser.Selector actual = parser.parseSelector("div p span");
        CSSParser.Selector expected = parser.new Selector(CSSParser.SelectorType.NESTED_ELEMENT);
        expected.values.add("div");
        expected.values.add("p");
        expected.values.add("span");
        assertEquals(expected, actual);
    }

    @Test
    public void testParseSelector_ELEMENT_CLASS() {
        CSSParser parser = new CSSParser();
        CSSParser.Selector actual = parser.parseSelector("h1.title");
        CSSParser.Selector expected = parser.new Selector(CSSParser.SelectorType.ELEMENT_CLASS);
        expected.values.add("h1");
        expected.values.add("title");
        assertEquals(expected, actual);
    }

    @Test
    public void testParser_multiple_mixed() {
        String css = "h2, h3, h4, h5, h6, div.h7, div.h8, div.h9 {\r\n" +
                "  font-size: 1.75rem;\r\n" +
                "  border: 1px solid #000;}";
        CSSParser parser = new CSSParser();
        parser.parse(css);
        parser.printRules();
    }

    @Test
    public void testParser_borders() {
        String css = "div { border: 5px solid #c22817;} p { border: 5px solid rgb(1, 1, 1);}";
        CSSParser parser = new CSSParser();
        parser.parse(css);
        parser.printRules();
    }

    @Test
    public void longParsingTest1() throws IOException {
        String css = new String(Files.readAllBytes(Paths.get("src/test/resources/cssTest1.css")));
        CSSParser parser = new CSSParser();
        parser.parse(css);
        parser.printRules();
    }

}
