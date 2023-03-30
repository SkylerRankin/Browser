package browser.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import browser.constants.CSSConstants.SelectorCombinator;
import browser.constants.CSSConstants.SelectorType;
import browser.model.CSSSelector;
import browser.model.CSSSelectorGroup;

import org.junit.Test;


public class CSSParserTest {

    @Test
    public void basicSelectors() {
        String css = "span.class-1 > span, #id div { background-color: red; }";
        Map<CSSSelectorGroup, Map<String, String>> rules = CSSParser.parseRules(css);

        CSSSelectorGroup group1 = new CSSSelectorGroup();
        group1.selectors.add(new CSSSelector(List.of(SelectorType.TYPE, SelectorType.CLASS), List.of("span", "class-1")));
        group1.combinators.add(SelectorCombinator.CHILD);
        group1.selectors.add(new CSSSelector(List.of(SelectorType.TYPE), List.of("span")));

        CSSSelectorGroup group2 = new CSSSelectorGroup();
        group2.selectors.add(new CSSSelector(List.of(SelectorType.ID), List.of("id")));
        group2.combinators.add(SelectorCombinator.DESCENDANT);
        group2.selectors.add(new CSSSelector(List.of(SelectorType.TYPE), List.of("div")));

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
        group1.selectors.add(new CSSSelector(List.of(SelectorType.CLASS), List.of("class1")));
        group1.combinators.add(SelectorCombinator.DESCENDANT);
        group1.selectors.add(new CSSSelector(List.of(SelectorType.CLASS), List.of("class2")));
        group1.combinators.add(SelectorCombinator.CHILD);
        group1.selectors.add(new CSSSelector(List.of(SelectorType.CLASS), List.of("class3")));
        group1.combinators.add(SelectorCombinator.SIBLING);
        group1.selectors.add(new CSSSelector(List.of(SelectorType.CLASS), List.of("class4")));
        group1.combinators.add(SelectorCombinator.ADJACENT_SIBLING);
        group1.selectors.add(new CSSSelector(List.of(SelectorType.CLASS), List.of("class5")));

        Map<String, String> expectedDeclarations = Map.of("background-color", "green");

        assertEquals(1, rules.size());
        assertTrue(rules.containsKey(group1));
        assertEquals(expectedDeclarations, rules.get(group1));

    }

    @Test
    public void allSelectors() {
        String css = "*div#id.class[attr=\"value\"]:hover, div:hover[attr=\"value\"]#id*.class { background-color: yellow; }";
        Map<CSSSelectorGroup, Map<String, String>> rules = CSSParser.parseRules(css);

        CSSSelectorGroup group1 = new CSSSelectorGroup();
        group1.selectors.add(new CSSSelector(
                List.of(SelectorType.UNIVERSAL, SelectorType.TYPE, SelectorType.ID, SelectorType.CLASS, SelectorType.ATTRIBUTE, SelectorType.PSEUDO),
                List.of("*", "div", "id", "class", "attr=\"value\"", "hover")));

        CSSSelectorGroup group2 = new CSSSelectorGroup();
        group2.selectors.add(new CSSSelector(
                List.of(SelectorType.TYPE, SelectorType.PSEUDO, SelectorType.ATTRIBUTE, SelectorType.ID, SelectorType.UNIVERSAL, SelectorType.CLASS),
                List.of("div", "hover", "attr=\"value\"", "id", "*", "class")));

        Map<String, String> expectedDeclarations = Map.of("background-color", "yellow");

        assertEquals(2, rules.size());
        assertTrue(rules.containsKey(group1));
        assertTrue(rules.containsKey(group2));
        assertEquals(expectedDeclarations, rules.get(group1));
        assertEquals(expectedDeclarations, rules.get(group2));
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
