package browser.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import browser.constants.CSSConstants.AttributeSelectorComparisonType;
import browser.constants.CSSConstants.SelectorCombinator;
import browser.constants.CSSConstants.SelectorType;
import browser.model.CSSSelector;
import browser.model.CSSSelectorGroup;
import browser.model.CSSUnitAttributeSelector;
import browser.model.CSSUnitSelector;

import org.junit.Test;

public class CSSParserTest {

    @Test
    public void basicSelectors() {
        String css = "span.class-1 > span, #id div { background-color: red; }";
        Map<CSSSelectorGroup, Map<String, String>> rules = CSSParser.parseRules(css);

        CSSSelectorGroup group1 = new CSSSelectorGroup();
        group1.selectors.add(new CSSSelector(List.of(
                new CSSUnitSelector(SelectorType.TYPE, "span"),
                new CSSUnitSelector(SelectorType.CLASS, "class-1"))));
        group1.combinators.add(SelectorCombinator.CHILD);
        group1.selectors.add(new CSSSelector(List.of(
                new CSSUnitSelector(SelectorType.TYPE, "span")
        )));

        CSSSelectorGroup group2 = new CSSSelectorGroup();
        group2.selectors.add(new CSSSelector(List.of(new CSSUnitSelector(SelectorType.ID, "id"))));
        group2.combinators.add(SelectorCombinator.DESCENDANT);
        group2.selectors.add(new CSSSelector(List.of(new CSSUnitSelector(SelectorType.TYPE, "div"))));

        Map<String, String> expectedDeclarations = Map.of("background-color", "red");

        assertEquals(2, rules.size());
        assertTrue(rules.containsKey(group1));
        assertTrue(rules.containsKey(group2));
        assertEquals(expectedDeclarations, rules.get(group1));
        assertEquals(expectedDeclarations, rules.get(group2));
    }

    @Test
    public void allCombinators() {
        String css = ".class1 .class2 > .class3 ~ .class4 + .class5 { background-color: green; }";
        Map<CSSSelectorGroup, Map<String, String>> rules = CSSParser.parseRules(css);

        CSSSelectorGroup group1 = new CSSSelectorGroup();
        group1.selectors.add(new CSSSelector(List.of(new CSSUnitSelector(SelectorType.CLASS, "class1"))));
        group1.combinators.add(SelectorCombinator.DESCENDANT);
        group1.selectors.add(new CSSSelector(List.of(new CSSUnitSelector(SelectorType.CLASS, "class2"))));
        group1.combinators.add(SelectorCombinator.CHILD);
        group1.selectors.add(new CSSSelector(List.of(new CSSUnitSelector(SelectorType.CLASS, "class3"))));
        group1.combinators.add(SelectorCombinator.SIBLING);
        group1.selectors.add(new CSSSelector(List.of(new CSSUnitSelector(SelectorType.CLASS, "class4"))));
        group1.combinators.add(SelectorCombinator.ADJACENT_SIBLING);
        group1.selectors.add(new CSSSelector(List.of(new CSSUnitSelector(SelectorType.CLASS, "class5"))));

        Map<String, String> expectedDeclarations = Map.of("background-color", "green");

        assertEquals(1, rules.size());
        assertTrue(rules.containsKey(group1));
        assertEquals(expectedDeclarations, rules.get(group1));

    }

    @Test
    public void allSelectors() {
        String css = "*div#id.class[attr=\"value\"]:hover, div:hover[attr2=\"value2\"]#id*.class { background-color: yellow; }";
        Map<CSSSelectorGroup, Map<String, String>> rules = CSSParser.parseRules(css);

        CSSSelectorGroup group1 = new CSSSelectorGroup();
        group1.selectors.add(new CSSSelector(List.of(
                new CSSUnitSelector(SelectorType.UNIVERSAL, "*"),
                new CSSUnitSelector(SelectorType.TYPE, "div"),
                new CSSUnitSelector(SelectorType.ID, "id"),
                new CSSUnitSelector(SelectorType.CLASS, "class"),
                new CSSUnitAttributeSelector("attr", "value", AttributeSelectorComparisonType.EXACT),
                new CSSUnitSelector(SelectorType.PSEUDO, "hover")
        )));

        CSSSelectorGroup group2 = new CSSSelectorGroup();
        group2.selectors.add(new CSSSelector(List.of(
                new CSSUnitSelector(SelectorType.TYPE, "div"),
                new CSSUnitSelector(SelectorType.PSEUDO, "hover"),
                new CSSUnitAttributeSelector("attr2", "value2", AttributeSelectorComparisonType.EXACT),
                new CSSUnitSelector(SelectorType.ID, "id"),
                new CSSUnitSelector(SelectorType.UNIVERSAL, "*"),
                new CSSUnitSelector(SelectorType.CLASS, "class")
        )));

        Map<String, String> expectedDeclarations = Map.of("background-color", "yellow");

        assertEquals(2, rules.size());
        assertTrue(rules.containsKey(group1));
        assertTrue(rules.containsKey(group2));
        assertEquals(expectedDeclarations, rules.get(group1));
        assertEquals(expectedDeclarations, rules.get(group2));
    }

    @Test
    public void attributeSelectors() {
        String css = "[attr][attr=\"value\" i][attr~=\"value\"][attr|=\"value\"][attr^=\"value\"][attr$=\"value\" i][attr*=\"value\"] { background-color: black; }";
        Map<CSSSelectorGroup, Map<String, String>> rules = CSSParser.parseRules(css);

        CSSSelectorGroup group = new CSSSelectorGroup();
        group.selectors.add(new CSSSelector(List.of(
                new CSSUnitAttributeSelector("attr", null, AttributeSelectorComparisonType.NONE),
                new CSSUnitAttributeSelector("attr", "value", AttributeSelectorComparisonType.EXACT, true),
                new CSSUnitAttributeSelector("attr", "value", AttributeSelectorComparisonType.MEMBER_IN_LIST),
                new CSSUnitAttributeSelector("attr", "value", AttributeSelectorComparisonType.HYPHEN),
                new CSSUnitAttributeSelector("attr", "value", AttributeSelectorComparisonType.PREFIX),
                new CSSUnitAttributeSelector("attr", "value", AttributeSelectorComparisonType.SUFFIX, true),
                new CSSUnitAttributeSelector("attr", "value", AttributeSelectorComparisonType.OCCURRENCE)
        )));
        Map<String, String> expectedDeclarations = Map.of("background-color", "black");

        assertEquals(1, rules.size());
        assertTrue(rules.containsKey(group));
        assertEquals(expectedDeclarations, rules.get(group));
    }

    @Test
    public void missingFinalDeclarationSemicolon() {
        String css = "div { font-size: 12px }";
        Map<CSSSelectorGroup, Map<String, String>> rules = CSSParser.parseRules(css);

        CSSSelectorGroup group = new CSSSelectorGroup();
        group.selectors.add(new CSSSelector(List.of(new CSSUnitSelector(SelectorType.TYPE, "div"))));
        Map<String, String> expectedDeclarations = Map.of("font-size", "12px");

        assertEquals(1, rules.size());
        assertTrue(rules.containsKey(group));
        assertEquals(expectedDeclarations, rules.get(group));
    }

    @Test
    public void mergeRepeatedSelectors() {
        String css = "h1, h2 { color: red; }\nh1 { font-size: 10px; }\nh1, h2 { background-color: white; }";
        Map<CSSSelectorGroup, Map<String, String>> rules = CSSParser.parseRules(css);

        CSSSelectorGroup group1 = new CSSSelectorGroup();
        group1.selectors.add(new CSSSelector(List.of(new CSSUnitSelector(SelectorType.TYPE, "h1"))));
        Map<String, String> expectedDeclarations1 = Map.of(
                "color", "red",
                "font-size", "10px",
                "background-color", "white"
        );

        CSSSelectorGroup group2 = new CSSSelectorGroup();
        group2.selectors.add(new CSSSelector(List.of(new CSSUnitSelector(SelectorType.TYPE, "h2"))));
        Map<String, String> expectedDeclarations2 = Map.of(
                "color", "red",
                "background-color", "white"
        );

        assertEquals(2, rules.size());
        assertTrue(rules.containsKey(group1));
        assertEquals(expectedDeclarations1, rules.get(group1));
        assertTrue(rules.containsKey(group2));
        assertEquals(expectedDeclarations2, rules.get(group2));
    }

    private String rulesToString(Map<CSSSelectorGroup, Map<String, String>> rules) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("Rules (%d):\n", rules.size()));
        for (Map.Entry<CSSSelectorGroup, Map<String, String>> e : rules.entrySet()) {
            stringBuilder.append(String.format("%s\n", e.getKey().toString()));
            for (Map.Entry<String, String> e2 : e.getValue().entrySet()) {
                stringBuilder.append(String.format("\t%s: %s\n", e2.getKey(), e2.getValue()));
            }
        }
        return stringBuilder.toString();
    }

}
