package browser.parser;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import browser.model.DOMNode;

import org.junit.Before;
import org.junit.Test;

public class HTMLParserTest {

    @Before
    public void before() {
        HTMLElements.init();
    }

    @Test
    public void testIsSingular() {
        HTMLParser parser = new HTMLParser(null);
        assertFalse(parser.isSingular("<h1>"));
        assertFalse(parser.isSingular("</h1>"));
        assertTrue(parser.isSingular("<input />"));
        assertTrue(parser.isSingular("<input/>"));
        assertTrue(parser.isSingular("<!doctype html>"));
    }

    @Test
    public void testGetAttributes_keyValues() {
        HTMLParser parser = new HTMLParser(null);
        Map<String, String> attributes = parser.getAttributes("id=\"title\" class=\"centered pointer\"");
        assertEquals(2, attributes.size());
        assertTrue(attributes.containsKey("id"));
        assertEquals(attributes.get("id"), "title");
        assertTrue(attributes.containsKey("class"));
        assertEquals(attributes.get("class"), "centered pointer");
    }

    @Test
    public void testGetAttributes_singleValues() {
        HTMLParser parser = new HTMLParser(null);
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
        HTMLParser parser = new HTMLParser(null);
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
        HTMLParser parser = new HTMLParser(null);
        Map<String, String> attributes = parser.getAttributes("   disabled   id =  \"title    \"   ");
        assertEquals(2, attributes.size());
        assertTrue(attributes.containsKey("disabled"));
        assertNull(attributes.get("disabled"));
        assertTrue(attributes.containsKey("id"));
        assertEquals(attributes.get("id"), "title    ");
    }

    @Test
    public void testGetAttributes_realworld1() {
        HTMLParser parser = new HTMLParser(null);
        Map<String, String> attributes = parser.getAttributes("width=\"180\" alt=\"Photo by Lionel Pottier\" src=\"petit-menhir.jpg\" height=\"240\"");
        assertEquals(4, attributes.size());
        assertTrue(attributes.containsKey("width"));
        assertEquals(attributes.get("width"), "180");
        assertTrue(attributes.containsKey("alt"));
        assertEquals(attributes.get("alt"), "Photo by Lionel Pottier");
        assertTrue(attributes.containsKey("src"));
        assertEquals(attributes.get("src"), "petit-menhir.jpg");
        assertTrue(attributes.containsKey("height"));
        assertEquals(attributes.get("height"), "240");
    }

    @Test
    public void testGetAttributes_realworld2() {
        HTMLParser parser = new HTMLParser(null);
        Map<String, String> attributes = parser.getAttributes("style=\"text-align: center;\"");
        assertEquals(1, attributes.size());
        assertTrue(attributes.containsKey("style"));
        assertEquals(attributes.get("style"), "text-align: center;");
    }

    @Test
    public void testRemoveUselessSpaces() {
        HTMLParser parser = new HTMLParser(null);
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
        HTMLParser parser = new HTMLParser(null);
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

        results = parser.splitOnAttributes("content=\"Search the world's information, including webpages, images, videos and more. Google has many special features to help you find exactly what you're looking for.\" name=\"description\"");
        expected = new String[]{"content=\"Search the world's information, including webpages, images, videos and more. Google has many special features to help you find exactly what you're looking for.\"", "name=\"description\""};
        assertArrayEquals(expected, results);

    }

    @Test
    public void testGenerateDOMTree_Basic() {
        String htmlText = "<!doctype html><html><head><title>Watchmen</title></head><body><h1 id=\"title\">Rorschach's Journal</h1><div><input disabled/><h2 class=\"centered linked\">October 12th, 1985</h2><p>Tonight, a comedian died in New York.</p></div></body></html>";
        HTMLParser parser = new HTMLParser(null);
        DOMNode dom = parser.generateDOMTree(htmlText);

        DOMNode expected = new DOMNode("root");
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
        HTMLParser parser = new HTMLParser(null);
        DOMNode dom = parser.generateDOMTree(htmlText);

        DOMNode expected = new DOMNode("root");
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
        HTMLParser parser = new HTMLParser(null);
        DOMNode dom = parser.generateDOMTree(htmlText);

        DOMNode expected = new DOMNode("root");
        DOMNode html = new DOMNode(HTMLElements.HTML);
        DOMNode head = new DOMNode(HTMLElements.HEAD);
        head.whiteSpaceAfter = true;
        DOMNode title = new DOMNode(HTMLElements.TITLE);
        title.attributes.put("required", null);
        DOMNode titleText = new DOMNode(HTMLElements.TEXT);
        titleText.content = " Watchmen 2008";
        DOMNode body = new DOMNode(HTMLElements.BODY);
        body.whiteSpaceAfter = true;
        DOMNode h1 = new DOMNode(HTMLElements.H1);
        DOMNode h1Text = new DOMNode(HTMLElements.TEXT);
        h1Text.content = "Rorschach's Journal";

        expected.addChild(html);
        html.addChild(head);
        html.addChild(body);
        head.addChild(title);
        title.addChild(titleText);
        body.addChild(h1);
        h1.addChild(h1Text);

        System.out.println("Expected:");
        expected.print();
        System.out.println("\nActual:");
        dom.print();

        assertEquals(expected, dom);
    }

    @Test
    public void testGenerateDOMTree_multipleTextElements() {
        String htmlText = "<html><body><div>before<p>middle</p>after</div></body></html>";
        HTMLParser parser = new HTMLParser(null);
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

    @Test
    public void testGenerateDOMTree_empty_tags() {
        String htmlText = "<html><link href=\"sd\"><body><link href=0/><div><p>paragraph<br></p>after</div><img></body></html>";
        HTMLParser parser = new HTMLParser(null);
        DOMNode dom = parser.generateDOMTree(htmlText);

        DOMNode expected = new DOMNode("root");
        DOMNode html = new DOMNode(HTMLElements.HTML);
        DOMNode body = new DOMNode(HTMLElements.BODY);
        DOMNode link1 = new DOMNode(HTMLElements.LINK);
        DOMNode link2 = new DOMNode(HTMLElements.LINK);
        DOMNode div = new DOMNode(HTMLElements.DIV);
        DOMNode p = new DOMNode(HTMLElements.P);
        DOMNode text1 = new DOMNode(HTMLElements.TEXT);
        text1.content = "paragraph";
        DOMNode br = new DOMNode(HTMLElements.BR);
        DOMNode text2 = new DOMNode(HTMLElements.TEXT);
        text2.content = "after";
        DOMNode img = new DOMNode(HTMLElements.IMG);

        expected.addChild(html);
        html.addChild(link1);
        html.addChild(body);
        body.addChild(link2);
        body.addChild(div);
        body.addChild(img);
        div.addChild(p);
        div.addChild(text2);
        p.addChild(text1);
        p.addChild(br);

        assertEquals(expected, dom);
    }

    @Test
    public void testGetBodyNode() {
        DOMNode dom = new DOMNode("root");
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

        dom.addChild(html);
        html.addChild(body);
        body.addChild(div);
        div.addChild(beforeText);
        div.addChild(p);
        div.addChild(afterText);
        p.addChild(pText);

        HTMLParser parser = new HTMLParser(null);
        DOMNode bodyNode = parser.getBodyNode(dom);
        assertEquals(body, bodyNode);
    }

    @Test
    public void testGetBodyNode_null() {
        DOMNode dom = new DOMNode("root");
        DOMNode html = new DOMNode(HTMLElements.HTML);
        DOMNode div = new DOMNode(HTMLElements.DIV);
        DOMNode beforeText = new DOMNode(HTMLElements.TEXT);
        beforeText.content = "before";
        DOMNode afterText = new DOMNode(HTMLElements.TEXT);
        afterText.content = "after";
        DOMNode p = new DOMNode(HTMLElements.P);
        DOMNode pText = new DOMNode(HTMLElements.TEXT);
        pText.content = "middle";

        dom.addChild(html);
        html.addChild(div);
        div.addChild(beforeText);
        div.addChild(p);
        div.addChild(afterText);
        p.addChild(pText);

        HTMLParser parser = new HTMLParser(null);
        DOMNode bodyNode = parser.getBodyNode(dom);
        assertEquals(null, bodyNode);
    }

    @Test
    public void testRemoveComments() {
        String html1 = "<div>testing<a>a link <!-- comment! --> </a> <!--closing   the div--></div>";
        String expected1 = "<div>testing<a>a link  </a> </div>";
        String actual1 = new HTMLParser(null).removeComments(html1);

        String html2 = "<div><<<a>a link <!-- -- ><&& --><!----> </a>test";
        String expected2 = "<div><<<a>a link  </a>test";
        String actual2 = new HTMLParser(null).removeComments(html2);

        assertEquals(expected1, actual1);
        assertEquals(expected2, actual2);

    }

    @Test
    public void testRemoveUnknownElements() {
        DOMNode dom = new DOMNode("root");
        DOMNode html = new DOMNode(HTMLElements.HTML);
        DOMNode head = new DOMNode(HTMLElements.HEAD);
        DOMNode script = new DOMNode("script");
        DOMNode title = new DOMNode(HTMLElements.TITLE);
        DOMNode table = new DOMNode(HTMLElements.TITLE);
        DOMNode tr = new DOMNode(HTMLElements.TITLE);
        DOMNode media = new DOMNode("media");
        DOMNode td = new DOMNode(HTMLElements.TITLE);
        td.content = "a table cell";

        dom.addChild(html);
        html.addChild(head);
        head.addChild(script);
        head.addChild(title);
        html.addChild(table);
        table.addChild(tr);
        tr.addChild(media);
        tr.addChild(td);

        DOMNode expectedDOM = new DOMNode("root");
        DOMNode expectedHTML = new DOMNode(HTMLElements.HTML);
        DOMNode expectedHead = new DOMNode(HTMLElements.HEAD);
        DOMNode expectedScript = new DOMNode("script");
        DOMNode expectedTitle = new DOMNode(HTMLElements.TITLE);
        DOMNode expectedTable = new DOMNode(HTMLElements.TITLE);
        DOMNode expectedTR = new DOMNode(HTMLElements.TITLE);
        DOMNode expectedMedia = new DOMNode("media");
        DOMNode expectedTD = new DOMNode(HTMLElements.TITLE);
        expectedTD.content = "a table cell";

        expectedDOM.addChild(html);
        expectedHTML.addChild(head);
        expectedHead.addChild(script);
        expectedHead.addChild(title);
        expectedHTML.addChild(table);
        expectedTable.addChild(tr);
        expectedTR.addChild(media);
        expectedTR.addChild(td);

        HTMLParser parser = new HTMLParser(null);
        parser.removeUnknownElements(dom);

        assertEquals(expectedDOM, dom);
    }

    @Test
    public void testRemoveDoctypeUppercase() {
        HTMLParser parser = new HTMLParser(null);
        String html1 = "<!DOCTYPE html  PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"><head><style type=\"text/css\">img {border-width: 0}</style><title>Menhir</title>";
        String expected1 = "<head><style type=\"text/css\">img {border-width: 0}</style><title>Menhir</title>";
        String actual1 = parser.removeDoctype(html1);

        assertEquals(expected1, actual1);
    }

    @Test
    public void testRemoveDoctypeLowercase() {
        HTMLParser parser = new HTMLParser(null);
        String html1 = "<!doctype html  PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"><head><style type=\"text/css\">img {border-width: 0}</style><title>Menhir</title>";
        String expected1 = "<head><style type=\"text/css\">img {border-width: 0}</style><title>Menhir</title>";
        String actual1 = parser.removeDoctype(html1);

        assertEquals(expected1, actual1);
    }

    @Test
    public void testGoogleHead() {
        HTMLParser parser = new HTMLParser(null);
        String html = "<head> <meta content=\"Search the world's information, including webpages, images, videos and more. Google has many special features to help you find exactly what you're looking for.\" name=\"description\"> <meta content=\"noodp\" name=\"robots\"> <meta content=\"text/html; charset=UTF-8\" http-equiv=\"Content-Type\"> <meta content=\"/images/branding/googleg/1x/googleg_standard_color_128dp.png\" itemprop=\"image\"> <title>Google</title> <script nonce=\"F9ODBlC0ILATNLFjZsdiKA==\">(function () { window.google = { kEI: 'CHj1XpvuLunm_QadnqKYBg', kEXPI: '0,202123,3,4,32,1151584,5663,730,224,3657,1448,206,3204,10,1226,364,1118,381,576,127,114,383,246,5,1128,226,196,211,146,608,533,273,1911,141,80,13,185,111,6,3,339,180,343,188,91,180,1122028,1197735,414,329118,1294,12383,4855,32691,15248,867,17444,1953,9287,9188,8384,4859,1361,9290,3021,4747,8000,3033,1808,4020,978,4788,1,3144,5295,2054,920,873,1217,8366,315,728,1138,7861,2303,3221,4519,2776,919,2277,8,85,2711,219,1374,1165,114,2212,530,149,1103,842,515,1137,278,51,57,157,4100,109,203,1135,1,3,2669,1839,184,1733,43,144,377,1947,244,786,1,1198,93,328,1284,16,2927,2247,473,1339,1787,3227,1989,856,7,4805,794,469,6286,4454,642,2449,2459,1226,1743,3653,1275,108,1710,1697,908,2,941,1064,1550,2392,5,9,5410,2049,1,841,1337,666,432,3,1546,865,1,377,3540,706,338,466,2371,476,502,1,240,1745,28,130,1,2061,32,4119,866,105,643,4,498,1030,17,417,861,1009,1236,271,874,405,1259,580,21,177,69,1849,331,40,9,7,729,56,244,673,9,43,214,1202,1424,460,43,73,765,438,23,15,198,1140,1424,1503,97,1655,513,86,3,1493,877,910,1264,162,69,2,22,69,212,628,388,346,436,512,1305,209,245,40,724,6,282,501,438,4,3,898,593,6,981,433,128,25,401,2,237,6,32,126,195,91,234,2,2,82,108,15,76,412,272,97,184,136,2,81,1153,44,72,144,356,72,330,356,1,353,1,399,67,175,36,4,282,255,24,4,186,78,138,297,49,89,27,1658,5796659,1873,1503,8798540,549,333,444,1,2,80,1,900,896,1,8,1,2,2551,1,748,141,59,736,563,1,4265,1,1,1,1,137,1,879,9,305,1356,97,1078,224,3,6,14,6,24,2,22,8,2,39,17,3,11,2,80,8,1,23961401,2693708', kBL: 'Qj-y' }; google.sn = 'webhp'; google.kHL = 'en'; })(); (function () { google.lc = []; google.li = 0; google.getEI = function (a) { for (var c; a && (!a.getAttribute || !(c = a.getAttribute(\"eid\")));)a=a.parentNode;return c||google.kEI};google.getLEI=function(a){for(var c=null;a&&(!a.getAttribute||!(c=a.getAttribute(\"leid\")));)a=a.parentNode;return c};google.ml=function(){return null};google.time=function(){return Date.now()};google.log=function(a,c,b,d,g){if(b=google.logUrl(a,c,b,d,g)){a=new Image;var e=google.lc,f=google.li;e[f]=a;a.onerror=a.onload=a.onabort=function(){delete e[f]};google.vel&&google.vel.lu&&google.vel.lu(b);a.src=b;google.li=f+1}};google.logUrl=function(a,c,b,d,g){var e=\"\",f=google.ls||\"\";b||-1!=c.search(\"&ei=\")||(e=\"&ei=\"+google.getEI(d),-1==c.search(\"&lei=\")&&(d=google.getLEI(d))&&(e+=\"&lei=\"+d));d=\"\";!b&&google.cshid&&-1==c.search(\"&cshid=\")&&\"slh\"!=a&&(d=\"&cshid=\"+google.cshid);b=b||\"/\"+(g||\"gen_204\")+\"?atyp=i&ct=\"+a+\"&cad=\"+c+e+f+\"&zx=\"+google.time()+d;/^http:/i.test(b)&&\"https:\"==window.location.protocol&&(google.ml(Error(\"a\"),!1,{src:b,glmm:1}),b=\"\");return b};}).call(this);(function(){google.y={};google.x=function(a,b){if(a)var c=a.id;else{do c=Math.random();while(google.y[c])}google.y[c]=[a,b];return!1};google.lm=[];google.plm=function(a){google.lm.push.apply(google.lm,a)};google.lq=[];google.load=function(a,b,c){google.lq.push([[a],b,c])};google.loadAll=function(a,b){google.lq.push([a,b])};}).call(this);google.f={};(function(){document.documentElement.addEventListener(\"submit\",function(b){var a;if(a=b.target){var c=a.getAttribute(\"data-submitfalse\");a=\"1\"==c||\"q\"==c&&!a.elements.q.value?!0:!1}else a=!1;a&&(b.preventDefault(),b.stopPropagation())},!0);document.documentElement.addEventListener(\"click\",function(b){var a;a:{for(a=b.target;a&&a!=document.documentElement;a=a.parentElement)if(\"A\"==a.tagName){a=\"1\"==a.getAttribute(\"data-nohref\");break a}a=!1}a&&b.preventDefault()},!0);}).call(this);var a=window.location,b=a.href.indexOf(\"#\");if(0<=b){var c=a.href.substring(b+1);/(^|&)q=/.test(c)&&-1==c.indexOf(\"#\")&&a.replace(\"/search?\"+c.replace(/(^|&)fp=[^&]*/g,\"\")+\"&cad=h\")};</script> <style> #gbar, #guser { font-size: 13px; padding-top: 1px !important; } #gbar { height: 22px } #guser { padding-bottom: 7px !important; text-align: right } .gbh, .gbd { border-top: 1px solid #c9d7f1; font-size: 1px } .gbh { height: 0; position: absolute; top: 24px; width: 100% } @media all { .gb1 { height: 22px; margin-right: .5em; vertical-align: top } #gbar { float: left } } a.gb1, a.gb4 { text-decoration: underline !important } a.gb1, a.gb4 { color: #00c !important } .gbi .gb4 { color: #dd8e27 !important } .gbf .gb4 { color: #900 !important } </style> <style> body, td, a, p, .h { font-family: arial, sans-serif } body { margin: 0; overflow-y: scroll } #gog { padding: 3px 8px 0 } td { line-height: .8em } .gac_m td { line-height: 17px } form { margin-bottom: 20px } .h { color: #36c } .q { color: #00c } .ts td { padding: 0 } .ts { border-collapse: collapse } em { font-weight: bold; font-style: normal } .lst { height: 25px; width: 496px } .gsfi, .lst { font: 18px arial, sans-serif } .gsfs { font: 17px arial, sans-serif } .ds { display: inline-box; display: inline-block; margin: 3px 0 4px; margin-left: 4px } input { font-family: inherit } body { background: #fff; color: #000 } a { color: #11c; text-decoration: none } a:hover, a:active { text-decoration: underline } .fl a { color: #36c } a:visited { color: #551a8b } .sblc { padding-top: 5px } .sblc a { display: block; margin: 2px 0; margin-left: 13px; font-size: 11px } .lsbb { background: #eee; border: solid 1px; border-color: #ccc #999 #999 #ccc; height: 30px } .lsbb { display: block } #fll a { display: inline-block; margin: 0 12px } .lsb { background: url(/images/nav_logo229.png) 0 -261px repeat-x; border: none; color: #000; cursor: pointer; height: 30px; margin: 0; outline: 0; font: 15px arial, sans-serif; vertical-align: top } .lsb:active { background: #ccc } .lst:focus { outline: none } </style> <script nonce=\"F9ODBlC0ILATNLFjZsdiKA==\"></script> </head>";
        DOMNode actual = parser.generateDOMTree(html);

        DOMNode expected = new DOMNode("root");

        DOMNode head = new DOMNode("head");

        DOMNode meta1 = new DOMNode("meta");
        meta1.attributes.put("content", "Search the world's information, including webpages, images, videos and more. Google has many special features to help you find exactly what you're looking for.");
        meta1.attributes.put("name", "description");

        DOMNode meta2 = new DOMNode("meta");
        meta2.attributes.put("content", "noodp");
        meta2.attributes.put("name", "robots");

        DOMNode meta3 = new DOMNode("meta");
        meta3.attributes.put("content", "text/html; charset=UTF-8");
        meta3.attributes.put("http-equiv", "Content-Type");

        DOMNode meta4 = new DOMNode("meta");
        meta4.attributes.put("content", "/images/branding/googleg/1x/googleg_standard_color_128dp.png");
        meta4.attributes.put("itemprop", "image");

        DOMNode title = new DOMNode("title");
        title.whiteSpaceAfter = true;
        DOMNode titleText = new DOMNode("text");
        title.children.add(titleText);

        DOMNode script1 = new DOMNode("script");
        script1.attributes.put("nonce", "F9ODBlC0ILATNLFjZsdiKA==");
        script1.whiteSpaceAfter = true;
        DOMNode script1Text = new DOMNode("text");
        script1.children.add(script1Text);

        DOMNode style1 = new DOMNode("style");
        style1.whiteSpaceAfter = true;
        DOMNode style1Text = new DOMNode("text");
        style1.children.add(style1Text);

        DOMNode style2 = new DOMNode("style");
        style2.whiteSpaceAfter = true;
        DOMNode style2Text = new DOMNode("text");
        style2.children.add(style2Text);

        DOMNode script2 = new DOMNode("script");
        script2.attributes.put("nonce", "F9ODBlC0ILATNLFjZsdiKA==");
        script2.whiteSpaceAfter = true;

        expected.children.add(head);
        head.children.add(meta1);
        head.children.add(meta2);
        head.children.add(meta3);
        head.children.add(meta4);
        head.children.add(title);
        head.children.add(script1);
        head.children.add(style1);
        head.children.add(style2);
        head.children.add(script2);

        assertTrue(expected.equalsIgnoreText(actual, true));
    }

    @Test
    public void testWhiteSpaceBetweenSpans() {
        HTMLParser parser = new HTMLParser(null);
        List<String> htmlStrings = List.of(
                "<span>a</span><span>b</span>",
                "<span>a</span> <span>b</span>",
                "<span>a</span>\n<span>b</span>"
        );
        List<Boolean> span1WhiteSpaceAfter = List.of(false, true, true);

        for (int i = 0; i < htmlStrings.size(); i++) {
            DOMNode dom = parser.generateDOMTree(htmlStrings.get(i));

            DOMNode expectedRoot = new DOMNode("root");
            DOMNode span1 = new DOMNode("span");
            span1.whiteSpaceAfter = span1WhiteSpaceAfter.get(i);
            DOMNode text1 = new DOMNode("text");
            text1.content = "a";
            span1.addChild(text1);

            DOMNode span2 = new DOMNode("span");
            DOMNode text2 = new DOMNode("text");
            text2.content = "b";
            span2.addChild(text2);

            expectedRoot.addChildren(span1, span2);

            assertEquals(expectedRoot, dom);
        }
    }

    @Test
    public void testWhiteSpaceInTextBeforeInline() {
        HTMLParser parser = new HTMLParser(null);
        String html = "<p>some text <code>c</code></p>";

        DOMNode expectedRoot = new DOMNode("root");
        DOMNode p = new DOMNode("p");
        DOMNode text1 = new DOMNode("text");
        text1.content = "some text ";
        DOMNode code = new DOMNode("code");
        DOMNode text2 = new DOMNode("text");
        text2.content = "c";

        p.addChildren(text1, code);
        code.addChild(text2);
        expectedRoot.addChildren(p);

        DOMNode actual = parser.generateDOMTree(html);

        assertEquals(expectedRoot, actual);
    }

}
