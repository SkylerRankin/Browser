package browser.model;

import browser.constants.CSSConstants.AttributeSelectorComparisonType;
import browser.constants.CSSConstants.SelectorType;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class CSSUnitAttributeSelector extends CSSUnitSelector {

    public String attributeName;
    public AttributeSelectorComparisonType comparisonType;
    public boolean caseInsensitive = false;

    public CSSUnitAttributeSelector(String attributeName, String attributeValue, AttributeSelectorComparisonType comparisonType) {
        this(attributeName, attributeValue, comparisonType, false);
    }

    public CSSUnitAttributeSelector(String attributeName, String attributeValue, AttributeSelectorComparisonType comparisonType, boolean caseInsensitive) {
        super(SelectorType.ATTRIBUTE, attributeValue);
        this.attributeName = attributeName;
        this.comparisonType = comparisonType;
        this.caseInsensitive = caseInsensitive;
    }

    public String toString() {
        if (comparisonType.equals(AttributeSelectorComparisonType.NONE)) {
            return String.format("attr [%s]", attributeName);
        } else {
            return String.format("attr [%s (%s) %s%s]", attributeName, comparisonType.name(), value, caseInsensitive ? " i" : "");
        }
    }

}
