package browser.css;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import browser.constants.CSSConstants;
import browser.model.*;
import browser.parser.CSSParser;
import browser.parser.HTMLElements;

public class CSSLoader {

    private final List<String> externalCSS;
    private final List<String> styleTagCSS;
    private final float screenWidth;
    private final float screenHeight;

    public CSSLoader(DOMNode dom, List<String> externalCSS, float screenWidth, float screenHeight) {
        this.externalCSS = externalCSS;
        styleTagCSS = new ArrayList<>();
        extractStyleTagsCSS(dom);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    // Public methods

    public void applyAllCSS(RenderNode root) {
        SelectorMatcher.clearCache();
        loadDefaults(root);
        loadExternalCSS(root);
        loadStyleTags(root);
        applyInline(root);
        propagateCSS(root);
        finalizeCSS(root);
    }

    // Private methods

    /**
     * Searches through the DOM tree for all style elements and adds their contents to the
     * styleTagCSS list. Assumes that the first and only child of a style element is a text
     * element containing the CSS.
     * @param root
     */
    private void extractStyleTagsCSS(DOMNode root) {
        if (root == null) return;
        if (root.type.equals(HTMLElements.STYLE)) {
            DOMNode text = root.children.get(0);
            if (text != null) {
                styleTagCSS.add(text.content);
            }
        } else {
            for (DOMNode child : root.children) {
                extractStyleTagsCSS(child);
            }
        }
    }

    private void loadDefaults(RenderNode root) {
        try {
            String cssString = new String(Files.readAllBytes(Paths.get("./src/main/resources/css/default.css")));
            Map<CSSSelectorGroup, Map<String, String>> rules = CSSParser.parseRules(cssString);
            filterSelectorsByMediaQuery(rules);
            applyRules(root, rules, true, false);
        } catch (IOException e) {
            System.err.println("CSSLoader: failed to load default.css, " + e.getLocalizedMessage());
        }
    }

    private void loadExternalCSS(RenderNode root) {
        for (String cssString : externalCSS) {
            Map<CSSSelectorGroup, Map<String, String>> rules = CSSParser.parseRules(cssString);
            filterSelectorsByMediaQuery(rules);
            applyRules(root, rules, true, false);
        }
    }

    private void loadStyleTags(RenderNode root) {
        for (String cssString : styleTagCSS) {
            try {
                Map<CSSSelectorGroup, Map<String, String>> rules = CSSParser.parseRules(cssString);
                filterSelectorsByMediaQuery(rules);
                applyRules(root, rules, true, false);
            } catch (Exception e) {
                System.out.printf("Failed to parse and apply css:\n\"%s\"\n", cssString);
                e.printStackTrace();
            }
        }
    }

    /**
     * Parse the inline style attribute and apply that style.
     * @param root
     */
    private void applyInline(RenderNode root) {
        if (root.attributes.containsKey("style")) {
            String style = root.attributes.get("style");
            if (style != null && !style.isBlank()) {
                String cssString = String.format("%s { %s }", root.type, style);
                Map<CSSSelectorGroup, Map<String, String>> rules = CSSParser.parseRules(cssString);
                filterSelectorsByMediaQuery(rules);
                applyRules(root, rules, false, true);
            }
        }

        String legacyAttributesCSS = LegacyCSSLoader.getCSSFromAttributes(root);
        if (legacyAttributesCSS != null) {
            Map<CSSSelectorGroup, Map<String, String>> rules = CSSParser.parseRules(legacyAttributesCSS);
            filterSelectorsByMediaQuery(rules);
            applyRules(root, rules, false, true);
        }

        for (RenderNode child : root.children) {
            applyInline(child);
        }
    }

    /**
     * Applies a set of CSS declarations to the provided render node, given the selector group matches. The global
     * application flag enables recursively applying the rules to child render nodes.
     * @param node      The render node to apply the CSS to.
     * @param rules     The set of CSS declarations to apply.
     * @param globalApplication     True if the rules should be recursively applied to the node's children.
     */
    private void applyRules(RenderNode node, Map<CSSSelectorGroup, Map<String, String>> rules, boolean globalApplication, boolean inline) {
        for (CSSSelectorGroup selectorGroup : rules.keySet()) {
            if (SelectorMatcher.selectorGroupMatchesNode(selectorGroup, node)) {
                CSSSpecificity specificity = CSSSpecificity.fromSelectorGroup(selectorGroup);
                if (inline) {
                    specificity.incrementInlineValue();
                }
                node.style.apply(rules.get(selectorGroup), specificity);
            }
        }

        if (globalApplication) {
            for (RenderNode child : node.children) {
                applyRules(child, rules, true, false);
            }
        }
    }

    private void filterSelectorsByMediaQuery(Map<CSSSelectorGroup, Map<String, String>> rules) {
        List<CSSSelectorGroup> toRemove = new ArrayList<>();
        for (CSSSelectorGroup selectorGroup : rules.keySet()) {
            if (!MediaQueryMatcher.matches(selectorGroup.mediaExpression, screenWidth, screenHeight)) {
                toRemove.add(selectorGroup);
            }
        }

        for (CSSSelectorGroup group : toRemove) {
            rules.remove(group);
        }
    }

    /**
     * Apply most of the rules of the parent to the child. In some cases, the CSS is not supposed
     * to be inherited, such as with dimensions. Also, if the child has already set this property,
     * then the parent should not override it.
     * @param root
     */
    private void propagateCSS(RenderNode root) {
        if (root.parent != null) {
            for (Entry<String, String> e : root.parent.style.getAllProperties().entrySet()) {
                if (CSSConstants.inheritedProperties.contains(e.getKey())) {
                    String property = e.getKey();
                    CSSSpecificity parentSpecificity = root.parent.style.getPropertySpecificity(property).deepCopy();
                    parentSpecificity.decrementInlineValue();
                    if (root.type.equals(HTMLElements.TEXT)) {
                        // Text nodes always take the parent's styling and are not involved with inheritance.
                        root.style.apply(property, e.getValue(), parentSpecificity);
                    } else {
                        root.style.applyInherited(property, e.getValue(), parentSpecificity);
                    }
                }
            }
        }

        for (RenderNode child : root.children) {
            propagateCSS(child);
        }
    }

    private void finalizeCSS(RenderNode root) {
        root.style.finalizeCSS();
        for (RenderNode child : root.children) {
            finalizeCSS(child);
        }
    }

}
