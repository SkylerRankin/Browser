package browser.model;

import java.util.List;

import browser.constants.CSSConstants.SelectorType;

import lombok.EqualsAndHashCode;

/**
 * Represents a chain of basic CSS selectors such as <code>span.code-block</code>. These selectors can be formed into
 * more complex selectors by using combinations, as represented by the CSSSelectorGroup class.
 */
@EqualsAndHashCode
public class CSSSelector {

    public final List<SelectorType> types;
    public final List<String> values;

    // TODO attribute selectors will need more than 1 string. They have up to 3 distinct pieces of information.

    public CSSSelector(List<SelectorType> types, List<String> values) {
        this.types = types;
        this.values = values;
    }

    public String toString() {
        if (types.size() == 0) {
            return "Empty CSS selector";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < types.size(); i++) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(String.format("%s=%s", types.get(i).toString(), values.get(i)));
        }
        return stringBuilder.toString();
    }

}
