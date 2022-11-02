package browser.model;

public class CSSRulePrecedent {
    
    private enum RuleType { ALL, ELEMENT, CLASS, ID }

    private int levelsInherited;
    private RuleType ruleType;
    
    public static CSSRulePrecedent All() {
        return new CSSRulePrecedent(RuleType.ALL);
    }

    public static CSSRulePrecedent Element() {
        return new CSSRulePrecedent(RuleType.ELEMENT);
    }

    public static CSSRulePrecedent Class() {
        return new CSSRulePrecedent(RuleType.CLASS);
    }

    public static CSSRulePrecedent ID() {
        return new CSSRulePrecedent(RuleType.ID);
    }
    
    public CSSRulePrecedent(RuleType ruleType) {
        this.ruleType = ruleType;
        levelsInherited = 0;
    }
    
    public void incrementLevel() {
        levelsInherited++;
    }
    
    public boolean hasPrecedentOver(CSSRulePrecedent rule) {
        if (rule.ruleType.equals(this.ruleType)) {
            return levelsInherited <= rule.levelsInherited;
        } else {
            return rule.ruleType.ordinal() <= this.ruleType.ordinal();
        }
    }
    
    public String toString() {
        return String.format("[%s %d]", ruleType, levelsInherited);
    }

}
