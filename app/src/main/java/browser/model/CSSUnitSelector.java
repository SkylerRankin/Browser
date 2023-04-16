package browser.model;

import browser.constants.CSSConstants;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class CSSUnitSelector {

    public CSSConstants.SelectorType type;
    public String value;

    public CSSUnitSelector(CSSConstants.SelectorType type, String value) {
        this.type = type;
        this.value = value;
    }

    public String toString() {
        return String.format("%s: %s", type.name(), value);
    }

    public CSSUnitSelector deepCopy() {
        return new CSSUnitSelector(type, value);
    }
}
