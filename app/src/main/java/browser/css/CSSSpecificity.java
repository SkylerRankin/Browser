package browser.css;

import browser.model.CSSSelector;
import browser.model.CSSSelectorGroup;
import browser.model.CSSUnitSelector;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class CSSSpecificity {

    private int inlineValue = 0;
    private int idValue = 0;
    private int classValue = 0;
    private int typeValue = 0;

    public void incrementInlineValue() {
        inlineValue++;
    }

    public void decrementInlineValue() {
        if (inlineValue > 0) {
            inlineValue--;
        }
    }

    public void incrementIdValue() {
        idValue++;
    }

    public void incrementClassValue() {
        classValue++;
    }

    public void incrementTypeValue() {
        typeValue++;
    }

    public boolean hasEqualOrGreaterSpecificityThan(CSSSpecificity other) {
        return toString().compareTo(other.toString()) >= 0;
    }

    public String toString() {
        return String.format("%d-%d-%d-%d", inlineValue, idValue, classValue, typeValue);
    }

    public static CSSSpecificity fromSelectorGroup(CSSSelectorGroup selectorGroup) {
        CSSSpecificity specificity = new CSSSpecificity();
        for (CSSSelector selector : selectorGroup.selectors) {
            for (CSSUnitSelector unitSelector : selector.unitSelectors) {
                switch (unitSelector.type) {
                    case ID -> specificity.incrementIdValue();
                    case CLASS, ATTRIBUTE -> specificity.incrementClassValue();
                    case TYPE -> specificity.incrementTypeValue();
                }
            }
        }
        return specificity;
    }

    public CSSSpecificity deepCopy() {
        CSSSpecificity specificity = new CSSSpecificity();
        specificity.inlineValue = inlineValue;
        specificity.idValue = idValue;
        specificity.classValue = classValue;
        specificity.typeValue = typeValue;
        return specificity;
    }

}
