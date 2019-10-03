package test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import model.DOMNode;
import parser.HTMLParser;
import parser.HTMLElements;

public class HTMLParserTest {
    
    @Test
    public void testIsSingular() {
        HTMLParser parser = new HTMLParser();
        assertFalse(parser.isSingular("<h1>"));
        assertFalse(parser.isSingular("</h1>"));
        assertTrue(parser.isSingular("<input />"));
        assertTrue(parser.isSingular("<input/>"));
        assertTrue(parser.isSingular("<!doctype html>"));
    }
    
    @Test
    public void testGetAttributes_keyValues() {
        HTMLParser parser = new HTMLParser();
        Map<String, String> attributes = parser.getAttributes("id=\"title\" class=\"centered pointer\"");
        assertEquals(2, attributes.size());
        assertTrue(attributes.containsKey("id"));
        assertEquals(attributes.get("id"), "title");
        assertTrue(attributes.containsKey("class"));
        assertEquals(attributes.get("class"), "centered pointer");
    }
    
    @Test
    public void testGetAttributes_singleValues() {
        HTMLParser parser = new HTMLParser();
        Map<String, String> attributes = parser.getAttributes("disabled mixed clear-block");
        assertEquals(3, attributes.size());
        assertTrue(attributes.containsKey("disabled"));
        assertNull(attributes.get("disabled"));
        assertTrue(attributes.containsKey("mixed"));
        assertNull(attributes.get("disabled"));
        assertTrue(attributes.containsKey("clear-block"));
        assertNull(attributes.get("disabled"));
    }
    
    @Test
    public void testGetAttributes_mixed() {
        HTMLParser parser = new HTMLParser();
        Map<String, String> attributes = parser.getAttributes("disabled id=\"title\" mixed clear-block class=\"centered pointer\"");
        assertEquals(5, attributes.size());
        assertTrue(attributes.containsKey("disabled"));
        assertNull(attributes.get("disabled"));
        assertTrue(attributes.containsKey("id"));
        assertEquals("title", attributes.get("id"));
        assertTrue(attributes.containsKey("mixed"));
        assertNull(attributes.get("mixed"));
        assertTrue(attributes.containsKey("clear-block"));
        assertNull(attributes.get("clear-block"));
        assertTrue(attributes.containsKey("class"));
        assertEquals("centered pointer", attributes.get("class"));
    }
    
    @Test
    public void testGetAttributes_strangeSpacing() {
        HTMLParser parser = new HTMLParser();
        Map<String, String> attributes = parser.getAttributes("   disabled   id =  \"title    \"   ");
        assertEquals(2, attributes.size());
        assertTrue(attributes.containsKey("disabled"));
        assertNull(attributes.get("disabled"));
        assertTrue(attributes.containsKey("id"));
        assertEquals(attributes.get("id"), "title    ");
    }
    
    @Test
    public void testRemoveUselessSpaces() {
        HTMLParser parser = new HTMLParser();
        String result = parser.removeUselessSpaces("test");
        assertEquals("test", result);
        result = parser.removeUselessSpaces("  test   ");
        assertEquals("test", result);
        result = parser.removeUselessSpaces("  test=\"test\"");
        assertEquals("test=\"test\"", result);
        result = parser.removeUselessSpaces("  test=\" test  \"");
        assertEquals("test=\" test  \"", result);
        result = parser.removeUselessSpaces(" h1 required  id = \"1\" value=\"Small Header\"");
        assertEquals("h1 required id=\"1\" value=\"Small Header\"", result);
        result = parser.removeUselessSpaces(" h1 required  id =\"1\" value=  \"Small Header\"  value ");
        assertEquals("h1 required id=\"1\" value=\"Small Header\" value", result);
        result = parser.removeUselessSpaces(" h1 required  id = \"1\" value =  \"Small Header\"");
        assertEquals("h1 required id=\"1\" value=\"Small Header\"", result);
        result = parser.removeUselessSpaces("h1 required  id   =\"1\" value");
        assertEquals("h1 required id=\"1\" value", result);
    }
    
    @Test
    public void testSplitOnAttributes() {
        HTMLParser parser = new HTMLParser();
        String[] results = parser.splitOnAttributes("id=\"title\" value=\"time zone\"");
        String[] expected = {"id=\"title\"", "value=\"time zone\""};
        assertArrayEquals(expected, results);
        
        results = parser.splitOnAttributes("disabled");
        expected = new String[]{"disabled"};
        assertArrayEquals(expected, results);
        
        results = parser.splitOnAttributes("disabled required");
        expected = new String[]{"disabled", "required"};
        assertArrayEquals(expected, results);
        
        results = parser.splitOnAttributes("disabled id=\"title\"");
        expected = new String[]{"disabled", "id=\"title\""};
        assertArrayEquals(expected, results);
        
        results = parser.splitOnAttributes("disabled id=\"title\" required");
        expected = new String[]{"disabled", "id=\"title\"", "required"};
        assertArrayEquals(expected, results);
        
        results = parser.splitOnAttributes("id=\"title   \" value=\" time zone \" id=\"   title\"");
        expected = new String[]{"id=\"title   \"", "value=\" time zone \"", "id=\"   title\""};
        assertArrayEquals(expected, results);
    }
    
    @Test
    public void testGenerateDOMTree_Basic() {
        String htmlText = "<!doctype html><html><head><title>Watchmen</title></head><body><h1 id=\"title\">Rorschach's Journal</h1><div><input disabled/><h2 class=\"centered linked\">October 12th, 1985</h2><p>Tonight, a comedian died in New York.</p></div></body></html>";
        HTMLParser parser = new HTMLParser();
        DOMNode dom = parser.generateDOMTree(htmlText);
        
        DOMNode expected = new DOMNode("root");
        DOMNode doctype = new DOMNode(HTMLElements.DOCTYPE);
        doctype.attributes.put("html", null);
        DOMNode html = new DOMNode(HTMLElements.HTML);
        DOMNode head = new DOMNode(HTMLElements.HEAD);
        DOMNode title = new DOMNode(HTMLElements.TITLE);
        DOMNode titleText = new DOMNode(HTMLElements.TEXT);
        titleText.content = "Watchmen";
        DOMNode body = new DOMNode(HTMLElements.BODY);
        DOMNode h1 = new DOMNode(HTMLElements.H1);
        h1.attributes.put("id", "title");
        DOMNode h1Text = new DOMNode(HTMLElements.TEXT);
        h1Text.content = "Rorschach's Journal";
        DOMNode div = new DOMNode(HTMLElements.DIV);
        DOMNode input = new DOMNode(HTMLElements.INPUT);
        input.attributes.put("disabled", null);
        DOMNode h2 = new DOMNode(HTMLElements.H2);
        h2.attributes.put("class", "centered linked");
        DOMNode h2Text = new DOMNode(HTMLElements.TEXT);
        h2Text.content = "October 12th, 1985";
        DOMNode p = new DOMNode(HTMLElements.P);
        DOMNode pText = new DOMNode(HTMLElements.TEXT);
        pText.content = "Tonight, a comedian died in New York.";
        
        expected.addChild(doctype);
        expected.addChild(html);
        html.addChild(head);
        html.addChild(body);
        head.addChild(title);        
        title.addChild(titleText);
        body.addChild(h1);
        h1.addChild(h1Text);
        body.addChild(div);
        div.addChild(input);
        div.addChild(h2);
        h2.addChild(h2Text);
        div.addChild(p);
        p.addChild(pText);
        
        assertEquals(expected, dom);
    }
    
    @Test
    public void testGenerateDOMTree_weirdAttributeSpacing() {
        String htmlText = "<!doctype html><html><head><title  required   status=\"bold \">Watchmen</title></head><body><h1 id  =  \"2\"   class   =\"none\" res>Rorschach's Journal</h1></body></html>";
        HTMLParser parser = new HTMLParser();
        DOMNode dom = parser.generateDOMTree(htmlText);
        
        DOMNode expected = new DOMNode("root");
        DOMNode doctype = new DOMNode(HTMLElements.DOCTYPE);
        doctype.attributes.put("html", null);
        DOMNode html = new DOMNode(HTMLElements.HTML);
        DOMNode head = new DOMNode(HTMLElements.HEAD);
        DOMNode title = new DOMNode(HTMLElements.TITLE);
        title.attributes.put("required", null);
        title.attributes.put("status", "bold ");
        DOMNode titleText = new DOMNode(HTMLElements.TEXT);
        titleText.content = "Watchmen";
        DOMNode body = new DOMNode(HTMLElements.BODY);
        DOMNode h1 = new DOMNode(HTMLElements.H1);
        h1.attributes.put("id", "2");
        h1.attributes.put("class", "none");
        h1.attributes.put("res", null);
        DOMNode h1Text = new DOMNode(HTMLElements.TEXT);
        h1Text.content = "Rorschach's Journal";
        
        expected.addChild(doctype);
        expected.addChild(html);
        html.addChild(head);
        html.addChild(body);
        head.addChild(title);        
        title.addChild(titleText);
        body.addChild(h1);
        h1.addChild(h1Text);
        
        assertEquals(expected, dom);
    }
    
    @Test
    public void testGenerateDOMTree_weirdHTMLSpacing() {
        String htmlText = "<!doctype html>  <html><head>    <title required> Watchmen 2008</title></head> <body>  <h1>Rorschach's Journal</h1></body>  </html> ";
        HTMLParser parser = new HTMLParser();
        DOMNode dom = parser.generateDOMTree(htmlText);
        
        DOMNode expected = new DOMNode("root");
        DOMNode doctype = new DOMNode(HTMLElements.DOCTYPE);
        doctype.attributes.put("html", null);
        DOMNode html = new DOMNode(HTMLElements.HTML);
        DOMNode head = new DOMNode(HTMLElements.HEAD);
        DOMNode title = new DOMNode(HTMLElements.TITLE);
        title.attributes.put("required", null);
        DOMNode titleText = new DOMNode(HTMLElements.TEXT);
        titleText.content = " Watchmen 2008";
        DOMNode body = new DOMNode(HTMLElements.BODY);
        DOMNode h1 = new DOMNode(HTMLElements.H1);
        DOMNode h1Text = new DOMNode(HTMLElements.TEXT);
        h1Text.content = "Rorschach's Journal";
        
        expected.addChild(doctype);
        expected.addChild(html);
        html.addChild(head);
        html.addChild(body);
        head.addChild(title);        
        title.addChild(titleText);
        body.addChild(h1);
        h1.addChild(h1Text);
        
        assertEquals(expected, dom);
    }
    
    @Test
    public void testGenerateDOMTree_multipleTextElements() {
        String htmlText = "<html><body><div>before<p>middle</p>after</div></body></html>";
        HTMLParser parser = new HTMLParser();
        DOMNode dom = parser.generateDOMTree(htmlText);
        
        DOMNode expected = new DOMNode("root");
        DOMNode html = new DOMNode(HTMLElements.HTML);
        DOMNode body = new DOMNode(HTMLElements.BODY);
        DOMNode div = new DOMNode(HTMLElements.DIV);
        DOMNode beforeText = new DOMNode(HTMLElements.TEXT);
        beforeText.content = "before";
        DOMNode afterText = new DOMNode(HTMLElements.TEXT);
        afterText.content = "after";
        DOMNode p = new DOMNode(HTMLElements.P);
        DOMNode pText = new DOMNode(HTMLElements.TEXT);
        pText.content = "middle";
        
        expected.addChild(html);
        html.addChild(body);
        body.addChild(div);
        div.addChild(beforeText);
        div.addChild(p);
        div.addChild(afterText);
        p.addChild(pText);
        
        assertEquals(expected, dom);
    }
    
}
