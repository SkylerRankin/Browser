package browser.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import browser.constants.CSSConstants;
import browser.constants.CSSConstants.AttributeSelectorComparisonType;
import browser.constants.CSSConstants.SelectorCombinator;
import browser.constants.CSSConstants.SelectorType;
import browser.model.*;

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

    @Test
    public void singleMediaQuery() {
        String css = "@media screen and (min-width: 400px) { h1 { font-size: 40px; } }";
        Map<CSSSelectorGroup, Map<String, String>> rules = CSSParser.parseRules(css);

        for (CSSSelectorGroup group : rules.keySet()) {
            System.out.print(group.mediaExpression);
        }

        CSSSelectorGroup group = new CSSSelectorGroup();
        group.selectors.add(new CSSSelector(List.of(new CSSUnitSelector(SelectorType.TYPE, "h1"))));

        // screen
        CSSMediaExpression expression1 = new CSSMediaExpression();
        expression1.mediaType = CSSConstants.MediaType.SCREEN;

        // (min-width: 400px)
        CSSMediaExpression expression2 = new CSSMediaExpression();
        expression2.feature = CSSConstants.MediaFeature.MIN_WIDTH;
        expression2.featureValue = "400px";

        // screen and (min-width: 400px)
        CSSMediaExpression rootExpression = new CSSMediaExpression();
        rootExpression.leftHandExpression = expression1;
        rootExpression.operator = CSSConstants.MediaQueryOperator.AND;
        rootExpression.rightHandExpression = expression2;

        group.mediaExpression = rootExpression;

        Map<String, String> expectedDeclarations = Map.of(
                "font-size", "40px"
        );

        assertEquals(1, rules.size());
        assertTrue(rules.containsKey(group));
        assertEquals(expectedDeclarations, rules.get(group));
    }

    @Test
    public void singleNestedMediaQuery() {
        String css = "@media (orientation: portrait) and (not (max-width: 500px) or (min-width: 200px)) { h1 { font-size: 20px; } }";
        Map<CSSSelectorGroup, Map<String, String>> rules = CSSParser.parseRules(css);

        CSSSelectorGroup group = new CSSSelectorGroup();
        group.selectors.add(new CSSSelector(List.of(new CSSUnitSelector(SelectorType.TYPE, "h1"))));

        // (orientation: portrait)
        CSSMediaExpression subExpression1 = new CSSMediaExpression();
        subExpression1.feature = CSSConstants.MediaFeature.ORIENTATION;
        subExpression1.featureValue = "portrait";

        // (max-width: 500px)
        CSSMediaExpression subExpression2 = new CSSMediaExpression();
        subExpression2.feature = CSSConstants.MediaFeature.MAX_WIDTH;
        subExpression2.featureValue = "500px";

        // (min-width: 200px)
        CSSMediaExpression subExpression3 = new CSSMediaExpression();
        subExpression3.feature = CSSConstants.MediaFeature.MIN_WIDTH;
        subExpression3.featureValue = "200px";

        // (max-width: 500px) or (min-width: 200px)
        CSSMediaExpression subExpression4 = new CSSMediaExpression();
        subExpression4.leftHandExpression = subExpression2;
        subExpression4.operator = CSSConstants.MediaQueryOperator.OR;
        subExpression4.rightHandExpression = subExpression3;

        // not (max-width: 500px) or (min-width: 200px)
        CSSMediaExpression subExpression5 = new CSSMediaExpression();
        subExpression5.operator = CSSConstants.MediaQueryOperator.NOT;
        subExpression5.rightHandExpression = subExpression4;

        // (orientation: portrait) and (not (max-width: 500px) or (min-width: 200px))
        CSSMediaExpression subExpression6 = new CSSMediaExpression();
        subExpression6.leftHandExpression = subExpression1;
        subExpression6.operator = CSSConstants.MediaQueryOperator.AND;
        subExpression6.rightHandExpression = subExpression5;

        group.mediaExpression = subExpression6;

        Map<String, String> expectedDeclarations = Map.of(
                "font-size", "20px"
        );

        assertEquals(1, rules.size());
        assertTrue(rules.containsKey(group));
        assertEquals(expectedDeclarations, rules.get(group));
    }

    @Test
    public void multipleBasicQueries() {
        String css = "@media (orientation: portrait), (max-width: 500px) or (min-width: 200px) { h1 { font-size: 15px; } }";
        Map<CSSSelectorGroup, Map<String, String>> rules = CSSParser.parseRules(css);

        CSSSelectorGroup group = new CSSSelectorGroup();
        group.selectors.add(new CSSSelector(List.of(new CSSUnitSelector(SelectorType.TYPE, "h1"))));

        // (orientation: portrait)
        CSSMediaExpression subExpression1 = new CSSMediaExpression();
        subExpression1.feature = CSSConstants.MediaFeature.ORIENTATION;
        subExpression1.featureValue = "portrait";

        // (max-width: 500px)
        CSSMediaExpression subExpression2 = new CSSMediaExpression();
        subExpression2.feature = CSSConstants.MediaFeature.MAX_WIDTH;
        subExpression2.featureValue = "500px";

        // (min-width: 200px)
        CSSMediaExpression subExpression3 = new CSSMediaExpression();
        subExpression3.feature = CSSConstants.MediaFeature.MIN_WIDTH;
        subExpression3.featureValue = "200px";

        // (max-width: 500px) or (min-width: 200px)
        CSSMediaExpression subExpression4 = new CSSMediaExpression();
        subExpression4.leftHandExpression = subExpression2;
        subExpression4.operator = CSSConstants.MediaQueryOperator.OR;
        subExpression4.rightHandExpression = subExpression3;

        // (orientation: portrait) or ((max-width: 500px) or (min-width: 200px))
        CSSMediaExpression subExpression6 = new CSSMediaExpression();
        subExpression6.leftHandExpression = subExpression1;
        subExpression6.operator = CSSConstants.MediaQueryOperator.OR;
        subExpression6.rightHandExpression = subExpression4;

        group.mediaExpression = subExpression6;

        Map<String, String> expectedDeclarations = Map.of(
                "font-size", "15px"
        );

        assertEquals(1, rules.size());
        assertTrue(rules.containsKey(group));
        assertEquals(expectedDeclarations, rules.get(group));
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
