package browser.css;

import static org.junit.Assert.*;

import java.util.List;

import browser.constants.CSSConstants;
import browser.model.CSSSelector;
import browser.model.CSSSelectorGroup;
import browser.model.CSSUnitSelector;

import org.junit.Test;

public class CSSSpecificityTest {

    @Test
    public void singleSelector() {
        CSSSelectorGroup group = new CSSSelectorGroup();
        group.selectors.add(new CSSSelector(List.of(
                new CSSUnitSelector(CSSConstants.SelectorType.UNIVERSAL, "*"),
                new CSSUnitSelector(CSSConstants.SelectorType.TYPE, "div"),
                new CSSUnitSelector(CSSConstants.SelectorType.ID, "id"),
                new CSSUnitSelector(CSSConstants.SelectorType.CLASS, "class"),
                new CSSUnitSelector(CSSConstants.SelectorType.ATTRIBUTE, "attr=\"value\""),
                new CSSUnitSelector(CSSConstants.SelectorType.PSEUDO, "hover")
        )));
        CSSSpecificity specificity = CSSSpecificity.fromSelectorGroup(group);
        assertEquals("0-1-2-1", specificity.toString());
    }

    @Test
    public void multipleSelector() {
        CSSSelectorGroup group = new CSSSelectorGroup();
        group.selectors.add(new CSSSelector(List.of(
                new CSSUnitSelector(CSSConstants.SelectorType.UNIVERSAL, "*")
        )));
        group.selectors.add(new CSSSelector(List.of(
                new CSSUnitSelector(CSSConstants.SelectorType.ID, "id1"),
                new CSSUnitSelector(CSSConstants.SelectorType.CLASS, "class 1")
        )));
        group.selectors.add(new CSSSelector(List.of(
                new CSSUnitSelector(CSSConstants.SelectorType.ID, "id"),
                new CSSUnitSelector(CSSConstants.SelectorType.ATTRIBUTE, "attribute"),
                new CSSUnitSelector(CSSConstants.SelectorType.CLASS, "class")
        )));
        CSSSpecificity specificity = CSSSpecificity.fromSelectorGroup(group);
        assertEquals("0-2-3-0", specificity.toString());
    }

    @Test
    public void strictlyHigherSpecificityComparison() {
        List<List<Integer>> specificityValues = List.of(
                List.of(0, 0, 0, 1), List.of(0, 0, 0, 0),
                List.of(0, 1, 0, 1), List.of(0, 0, 3, 1),
                List.of(0, 1, 3, 0), List.of(0, 1, 1, 1),
                List.of(0, 1, 0, 0), List.of(0, 0, 2, 0),
                List.of(1, 0, 0, 0), List.of(0, 9, 9, 9),
                List.of(2, 3, 4, 5), List.of(2, 3, 4, 4),
                List.of(2, 3, 4, 5), List.of(2, 3, 4, 4)
        );

        for (int i = 0; i < specificityValues.size(); i += 2){
            List<Integer> values1 = specificityValues.get(i);
            List<Integer> values2 = specificityValues.get(i + 1);
            CSSSpecificity specificity1 = new CSSSpecificity(values1.get(0), values1.get(1), values1.get(2), values1.get(3));
            CSSSpecificity specificity2 = new CSSSpecificity(values2.get(0), values2.get(1), values2.get(2), values2.get(3));
            assertTrue(specificity1.hasEqualOrGreaterSpecificityThan(specificity2));
            assertFalse(specificity2.hasEqualOrGreaterSpecificityThan(specificity1));
        }
    }

    @Test
    public void equalSpecificityComparison() {
        List<List<Integer>> specificityValues = List.of(
                List.of(0, 0, 0, 1), List.of(0, 0, 0, 1),
                List.of(0, 1, 0, 1), List.of(0, 1, 0, 1),
                List.of(2, 3, 4, 5), List.of(2, 3, 4, 5)
        );

        for (int i = 0; i < specificityValues.size(); i += 2){
            List<Integer> values1 = specificityValues.get(i);
            List<Integer> values2 = specificityValues.get(i + 1);
            CSSSpecificity specificity1 = new CSSSpecificity(values1.get(0), values1.get(1), values1.get(2), values1.get(3));
            CSSSpecificity specificity2 = new CSSSpecificity(values2.get(0), values2.get(1), values2.get(2), values2.get(3));
            assertTrue(specificity1.hasEqualOrGreaterSpecificityThan(specificity2));
            assertTrue(specificity2.hasEqualOrGreaterSpecificityThan(specificity1));
        }
    }
}
