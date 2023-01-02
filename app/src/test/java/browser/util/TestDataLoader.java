package browser.util;

import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import browser.css.CSSStyle;
import browser.model.BoxNode;
import browser.model.RenderNode;
import browser.parser.HTMLElements;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * This class builds render node and box node trees for testing, so that the tests do not have to write lots of code
 * to generate the correct inputs and expected outputs for each test. The JSON files in src/test/resources/layoutTrees
 * are used as configurations.
 */

public class TestDataLoader {

    private static final JSONParser parser = new JSONParser();
    private static final Map<String, RenderNode> renderNodeReferenceMap = new HashMap<>();

    public static TestData loadLayoutTrees(String name) {
        try (Reader reader = new FileReader(String.format("./src/test/resources/layoutTrees/%s.json", name))) {
            JSONObject object = (JSONObject) parser.parse(reader);
            TestData testData = new TestData();
            if (object.containsKey("config")) {
                JSONObject config = (JSONObject) object.get("config");
                testData.screenWidth = getFloat(config, "screenWidth");
                testData.letterWidth = getInt(config, "letterWidth");
                testData.letterHeight = getInt(config, "letterHeight");
            }
            if (object.containsKey("renderTree")) {
                JSONObject rootObject = (JSONObject) ((JSONObject) object.get("renderTree")).get("root");
                RenderNode.nextId = 0;
                testData.rootRenderNode = buildRenderTree(rootObject);
            }
            if (object.containsKey("boxTree")) {
                JSONObject rootObject = (JSONObject) ((JSONObject) object.get("boxTree")).get("root");
                BoxNode.nextId = 0;
                testData.rootBoxNode = buildBoxTree(rootObject);
            }
            if (object.containsKey("boxTreeAfterLayout") && ((JSONObject) object.get("boxTreeAfterLayout")).containsKey("root")) {
                JSONObject rootObject = (JSONObject) ((JSONObject) object.get("boxTreeAfterLayout")).get("root");
                BoxNode.nextId = 0;
                testData.rootBoxNodeAfterLayout = buildBoxTree(rootObject);
            }
            return testData;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static RenderNode buildRenderTree(JSONObject root) {
        RenderNode renderNode = new RenderNode(HTMLElements.DIV);
        if (root.containsKey("template")) {
            String template = (String) root.get("template");
            renderNode = getTemplateRenderNode(template);
            if (renderNode == null) {
                throw new RuntimeException(String.format("Invalid template %s.", template));
            }
        }
        renderNode.id = RenderNode.nextId++;

        if (root.containsKey("style")) {
            setRenderNodeStyle(renderNode, (JSONObject) root.get("style"));
        }

        if (root.containsKey("text")) {
            renderNode.text = (String) root.get("text");
        }

        if (root.containsKey("children")) {
            JSONArray children = (JSONArray) root.get("children");
            for (Object object : children) {
                JSONObject child = (JSONObject) object;
                RenderNode childRenderNode = buildRenderTree(child);
                renderNode.children.add(childRenderNode);
                childRenderNode.parent = renderNode;
                childRenderNode.depth = renderNode.depth + 1;
            }
        }

        if (root.containsKey("refName")) {
            renderNodeReferenceMap.put((String) root.get("refName"), renderNode);
        }

        return renderNode;
    }

    private static RenderNode getTemplateRenderNode(String template) {
        switch (template) {
            case "div": {
                RenderNode renderNode = new RenderNode(HTMLElements.DIV);
                renderNode.style.display = CSSStyle.DisplayType.BLOCK;
                renderNode.style.outerDisplay = CSSStyle.DisplayType.BLOCK;
                renderNode.style.innerDisplay = CSSStyle.DisplayType.FLOW;
                return renderNode;
            }
            case "span": {
                RenderNode renderNode = new RenderNode(HTMLElements.SPAN);
                renderNode.style.display = CSSStyle.DisplayType.INLINE;
                renderNode.style.outerDisplay = CSSStyle.DisplayType.INLINE;
                renderNode.style.innerDisplay = CSSStyle.DisplayType.FLOW;
                return renderNode;
            }
            case "text": {
                RenderNode renderNode = new RenderNode(HTMLElements.TEXT);
                renderNode.style.display = CSSStyle.DisplayType.INLINE;
                renderNode.style.outerDisplay = CSSStyle.DisplayType.INLINE;
                renderNode.style.innerDisplay = CSSStyle.DisplayType.FLOW;
                return renderNode;
            }
            case "inlineBlock": {
                RenderNode renderNode = new RenderNode(HTMLElements.DIV);
                renderNode.style.display = CSSStyle.DisplayType.INLINE_BLOCK;
                renderNode.style.outerDisplay = CSSStyle.DisplayType.INLINE;
                renderNode.style.innerDisplay = CSSStyle.DisplayType.FLOW_ROOT;
                return renderNode;
            }
        }
        return null;
    }

    private static void setRenderNodeStyle(RenderNode renderNode, JSONObject style) {
        if (style.containsKey("marginLeft")) renderNode.style.marginLeft = getInt(style,"marginLeft");
        if (style.containsKey("marginRight")) renderNode.style.marginRight = getInt(style, "marginRight");
        if (style.containsKey("marginTop")) renderNode.style.marginTop = getInt(style, "marginTop");
        if (style.containsKey("marginBottom")) renderNode.style.marginBottom = getInt(style, "marginBottom");

        if (style.containsKey("padding")) {
            renderNode.style.paddingLeft = getInt(style, "padding");
            renderNode.style.paddingRight = getInt(style, "padding");
            renderNode.style.paddingTop = getInt(style, "padding");
            renderNode.style.paddingBottom = getInt(style, "padding");
        }
        if (style.containsKey("paddingLeft")) renderNode.style.paddingLeft = getInt(style, "paddingLeft");
        if (style.containsKey("paddingRight")) renderNode.style.paddingRight = getInt(style, "paddingRight");
        if (style.containsKey("paddingTop")) renderNode.style.paddingTop = getInt(style, "paddingTop");
        if (style.containsKey("paddingBottom")) renderNode.style.paddingBottom = getInt(style, "paddingBottom");

        if (style.containsKey("width")) renderNode.style.width = getFloat(style, "width");
        if (style.containsKey("widthType")) renderNode.style.widthType = CSSStyle.DimensionType.valueOf((String) style.get("widthType"));
        if (style.containsKey("height")) renderNode.style.height = getFloat(style, "height");
        if (style.containsKey("heightType")) renderNode.style.heightType = CSSStyle.DimensionType.valueOf((String) style.get("heightType"));

        if (style.containsKey("boxSizing")) {
            String value = (String) style.get("boxSizing");
            renderNode.style.boxSizing = value.equals("borderBox") ? CSSStyle.BoxSizingType.BORDER_BOX : CSSStyle.BoxSizingType.CONTENT_BOX;
        }

        if (style.containsKey("borderWidth")) {
            int width = getInt(style, "borderWidth");
            renderNode.style.borderWidthTop = width;
            renderNode.style.borderWidthBottom = width;
            renderNode.style.borderWidthLeft = width;
            renderNode.style.borderWidthRight = width;
        }

        if (style.containsKey("borderWidthTop")) renderNode.style.borderWidthTop = getInt(style, "borderWidthTop");
        if (style.containsKey("borderWidthBottom")) renderNode.style.borderWidthBottom = getInt(style, "borderWidthBottom");
        if (style.containsKey("borderWidthLeft")) renderNode.style.borderWidthLeft = getInt(style, "borderWidthLeft");
        if (style.containsKey("borderWidthRight")) renderNode.style.borderWidthRight = getInt(style, "borderWidthRight");
    }

    private static BoxNode buildBoxTree(JSONObject root) {
        BoxNode boxNode = new BoxNode();
        if (root.containsKey("template")) {
            String template = (String) root.get("template");
            boxNode = getTemplateBoxNode(template);
            if (boxNode == null) {
                throw new RuntimeException(String.format("Invalid template %s.", template));
            }
        }
        boxNode.id = BoxNode.nextId++;

        if (root.containsKey("correspondingRenderNode")) {
            String referenceName = (String) root.get("correspondingRenderNode");
            boxNode.correspondingRenderNode = renderNodeReferenceMap.get(referenceName);
            if (boxNode.correspondingRenderNode != null) {
                boxNode.renderNodeId = boxNode.correspondingRenderNode.id;
                if (boxNode.correspondingRenderNode.style != null) {
                    boxNode.style = boxNode.correspondingRenderNode.style.deepCopy();
                }
            }
        }

        if (root.containsKey("inlineFormattingContextId")) {
            boxNode.inlineFormattingContextId = (int) root.get("inlineFormattingContextId");
        }

        if (root.containsKey("blockFormattingContextId")) {
            boxNode.inlineFormattingContextId = (int) root.get("blockFormattingContextId");
        }

        if (root.containsKey("x")) boxNode.x = getFloat(root, "x");
        if (root.containsKey("y")) boxNode.y = getFloat(root, "y");
        if (root.containsKey("width")) boxNode.width = getFloat(root, "width");
        if (root.containsKey("height")) boxNode.height = getFloat(root, "height");

        if (root.containsKey("textStartIndex")) boxNode.textStartIndex = getInt(root, "textStartIndex");
        if (root.containsKey("textEndIndex")) boxNode.textEndIndex = getInt(root, "textEndIndex");

        if (root.containsKey("children")) {
            JSONArray children = (JSONArray) root.get("children");
            for (Object object : children) {
                JSONObject child = (JSONObject) object;
                BoxNode childBoxNode = buildBoxTree(child);
                boxNode.children.add(childBoxNode);
                childBoxNode.parent = boxNode;
            }
        }
        return boxNode;
    }

    private static BoxNode getTemplateBoxNode(String template) {
        switch (template) {
            case "block": {
                BoxNode boxNode = new BoxNode();
                boxNode.outerDisplayType = CSSStyle.DisplayType.BLOCK;
                boxNode.innerDisplayType = CSSStyle.DisplayType.FLOW;
                return boxNode;
            }
            case "inline": {
                BoxNode boxNode = new BoxNode();
                boxNode.outerDisplayType = CSSStyle.DisplayType.INLINE;
                boxNode.innerDisplayType = CSSStyle.DisplayType.FLOW;
                return boxNode;
            }
            case "text": {
                BoxNode boxNode = new BoxNode();
                boxNode.outerDisplayType = CSSStyle.DisplayType.INLINE;
                boxNode.innerDisplayType = CSSStyle.DisplayType.FLOW;
                boxNode.isTextNode = true;
                boxNode.isAnonymous = true;
                return boxNode;
            }
            case "inlineBlock": {
                BoxNode boxNode = new BoxNode();
                boxNode.outerDisplayType = CSSStyle.DisplayType.INLINE;
                boxNode.innerDisplayType = CSSStyle.DisplayType.FLOW_ROOT;
                return boxNode;
            }
        }
        return null;
    }

    private static float getFloat(JSONObject object, String name) {
        return ((Long) object.get(name)).floatValue();
    }

    private static int getInt(JSONObject object, String name) {
        return ((Long) object.get(name)).intValue();
    }

    public static class TestData {
        public RenderNode rootRenderNode;
        public BoxNode rootBoxNode;
        public BoxNode rootBoxNodeAfterLayout;
        public float screenWidth;
        public int letterWidth;
        public int letterHeight;
    }

}
