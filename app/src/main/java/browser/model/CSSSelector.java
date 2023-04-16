package browser.model;

import java.util.List;

import lombok.EqualsAndHashCode;

/**
 * Represents a chain of basic CSS selectors such as <code>span.code-block</code>. These selectors can be formed into
 * more complex selectors by using combinations, as represented by the CSSSelectorGroup class.
 */
@EqualsAndHashCode
public class CSSSelector {

    public final List<CSSUnitSelector> unitSelectors;

    public CSSSelector(List<CSSUnitSelector> unitSelectors) {
        this.unitSelectors = unitSelectors;
    }

    public String toString() {
        if (unitSelectors.size() == 0) {
            return "Empty CSS selector";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (CSSUnitSelector unitSelector : unitSelectors) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(unitSelector);
        }
        return stringBuilder.toString();
    }

    public CSSSelector deepCopy() {
        return new CSSSelector(unitSelectors == null ? null : unitSelectors.stream().map(CSSUnitSelector::deepCopy).toList());
    }

}
