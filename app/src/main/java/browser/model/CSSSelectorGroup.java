package browser.model;

import java.util.ArrayList;
import java.util.List;

import browser.constants.CSSConstants;

import lombok.EqualsAndHashCode;

/**
 * The CSSSelectorGroup class represents one selector, including the basic selectors and combinators that it is composed
 * of. For example, consider the following CSS.
 * <code>
 *     div.outlined > span, #id { background-color: aquamarine; }
 * </code>
 * There will be two selector groups parsed from this code. The first, `div.outlined > span`, contains two selectors and one
 * combinator. The second, `#id`, contains just one selector.
 */
@EqualsAndHashCode
public class CSSSelectorGroup {

    public final List<CSSConstants.SelectorCombinator> combinators;
    public final List<CSSSelector> selectors;

    public CSSSelectorGroup() {
        combinators = new ArrayList<>();
        selectors = new ArrayList<>();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("CSS Selector Group (%d): ", selectors.size()));
        for (int i = 0; i < selectors.size(); i++) {
            if (i > 0) {
                stringBuilder.append(String.format(" [%s] ", i - 1 < combinators.size() ? combinators.get(i - 1).name() : "Missing Combinator"));
            }
            stringBuilder.append(String.format("[%s]", selectors.get(i)));
        }
        return stringBuilder.toString();
    }

}
