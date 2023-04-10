package browser.parser;

import java.util.*;
import java.util.regex.Matcher;

import browser.constants.CSSConstants;
import browser.constants.CSSConstants.SelectorCombinator;
import browser.model.*;

public class CSSParser {

    public static Map<CSSSelectorGroup, Map<String, String>> parseRules(String css) {
        List<CSSToken> tokens = CSSLexer.getTokens(css);
        removeCommentTokens(tokens);
        ListIterator<CSSToken> tokenIterator = tokens.listIterator();

        Map<CSSSelectorGroup, Map<String, String>> rules = new HashMap<>();
        List<CSSSelectorGroup> currentSelectorGroups = new ArrayList<>();
        Map<String, String> currentRuleSet = new HashMap<>();
        String currentPropertyName = null;

        while (tokenIterator.hasNext()) {
            CSSToken nextToken = tokenIterator.next();

            switch (nextToken.type) {
                case SELECTOR -> {
                    currentSelectorGroups = parseSelectorsGroups(nextToken.value.trim());
                    currentRuleSet = new HashMap<>();
                    currentPropertyName = null;
                }
                case PROPERTY_NAME -> {
                    currentPropertyName = nextToken.value;
                }
                case PROPERTY_VALUE -> {
                    if (currentPropertyName != null) {
                        String propertyValue = nextToken.value;
                        currentRuleSet.put(currentPropertyName, propertyValue);
                        currentPropertyName = null;
                    }
                }
                case CLOSE_BRACKET -> {
                    for (CSSSelectorGroup selectorGroup : currentSelectorGroups) {
                        addRuleDeclarationSet(rules, selectorGroup, currentRuleSet);
                    }
                }
            }
        }

        return rules;
    }

    private static void removeCommentTokens(List<CSSToken> tokens) {
        List<CSSLexer.CSSTokenType> commentTypes = List.of(
                CSSLexer.CSSTokenType.COMMENT_START,
                CSSLexer.CSSTokenType.COMMENT,
                CSSLexer.CSSTokenType.COMMENT_END
        );

        for (int i = tokens.size() - 1; i >= 0; i--) {
            if (commentTypes.contains(tokens.get(i).type)) {
                tokens.remove(i);
            }
        }
    }

    private static List<CSSSelectorGroup> parseSelectorsGroups(String text) {
        String[] items = text.split(",");
        List<CSSSelectorGroup> selectors = new ArrayList<>();
        for (String item : items) {
            item = item.trim();
            CSSSelectorGroup selector = new CSSSelectorGroup();

            boolean expectingCombinator = false;
            int i = 0;
            while (i < item.length()) {
                boolean whitespace = Character.isWhitespace(item.charAt(i));
                while (Character.isWhitespace(item.charAt(i))) {
                    i++;
                }

                String itemText = getSelectorListString(item, i);
                if (expectingCombinator) {
                    boolean whitespaceCombinator = false;
                    if (!CSSConstants.CSS_COMBINATOR_CHARACTERS.contains(item.charAt(i)) && whitespace) {
                        whitespaceCombinator = true;
                        itemText = " ";
                        i--;
                    }
                    boolean validCombinator = parseSelectorCombinator(itemText, selector);
                    // TODO skip this selector if invalid combinator
                    expectingCombinator = false;
                    while (i < item.length() && Character.isWhitespace(text.charAt(i))) {
                        i++;
                    }

                    if (!whitespaceCombinator) {
                        i += itemText.length();
                    }
                } else {
                    parseSelector(itemText, selector);
                    expectingCombinator = true;
                    i += itemText.length();
                }
            }
            selectors.add(selector);
        }
        return selectors;
    }

    private static void parseSelector(String text, CSSSelectorGroup selectorGroup) {
        List<CSSUnitSelector> unitSelectors = new ArrayList<>();

        int i = 0;
        while (i < text.length()) {
            String c = text.substring(i, i + 1);
            Matcher matcher = CSSConstants.CSS_IDENTIFIER_PATTERN.matcher(text.substring(i));
            switch (c) {
                case "*" -> {
                    unitSelectors.add(new CSSUnitSelector(CSSConstants.SelectorType.UNIVERSAL, c));
                    i++;
                }
                case "." -> {
                    if (matcher.find()) {
                        String classString = matcher.group();
                        unitSelectors.add(new CSSUnitSelector(CSSConstants.SelectorType.CLASS, classString));
                        i += classString.length() + 1;
                    } else {
                        i++;
                    }
                }
                case "#" -> {
                    if (matcher.find()) {
                        String idString = matcher.group();
                        unitSelectors.add(new CSSUnitSelector(CSSConstants.SelectorType.ID, idString));
                        i += idString.length() + 1;
                    } else {
                        i++;
                    }
                }
                case "[" -> {
                    int attributeEnd = text.indexOf("]", i + 1);
                    if (attributeEnd != -1) {
                        String attributeText = text.substring(i + 1, attributeEnd);
                        CSSUnitAttributeSelector attributeSelector = parseAttributeSelector(attributeText);
                        unitSelectors.add(attributeSelector);
                        i += attributeText.length() + 2;
                    } else {
                        i++;
                    }
                }
                case ":" -> {
                    if (matcher.find()) {
                        String pseudoString = matcher.group();
                        unitSelectors.add(new CSSUnitSelector(CSSConstants.SelectorType.PSEUDO, pseudoString));
                        i += pseudoString.length() + 1;
                    } else {
                        i++;
                    }
                }
                default -> {
                    if (matcher.find()) {
                        String typeString = matcher.group();
                        unitSelectors.add(new CSSUnitSelector(CSSConstants.SelectorType.TYPE, typeString));
                        i += typeString.length();
                    } else {
                        i++;
                    }
                }
            }
        }

        selectorGroup.selectors.add(new CSSSelector(unitSelectors));
    }

    private static boolean parseSelectorCombinator(String text, CSSSelectorGroup selectorGroup) {
        SelectorCombinator combinator = CSSConstants.STRING_SELECTOR_COMBINATOR_MAP.get(text);
        if (combinator == null) {
            return false;
        } else {
            selectorGroup.combinators.add(combinator);
            return true;
        }
    }

    private static CSSUnitAttributeSelector parseAttributeSelector(String text) {
        String name;
        String value;
        CSSConstants.AttributeSelectorComparisonType comparisonType;
        boolean caseInsensitive = false;

        if (text.contains("=")) {
            int operatorIndex = text.indexOf("=") - 1;
            switch (text.charAt(operatorIndex)) {
                case '~' -> comparisonType = CSSConstants.AttributeSelectorComparisonType.MEMBER_IN_LIST;
                case '|' -> comparisonType = CSSConstants.AttributeSelectorComparisonType.HYPHEN;
                case '^' -> comparisonType = CSSConstants.AttributeSelectorComparisonType.PREFIX;
                case '$' -> comparisonType = CSSConstants.AttributeSelectorComparisonType.SUFFIX;
                case '*' -> comparisonType = CSSConstants.AttributeSelectorComparisonType.OCCURRENCE;
                default -> comparisonType = CSSConstants.AttributeSelectorComparisonType.EXACT;
            }

            name = comparisonType.equals(CSSConstants.AttributeSelectorComparisonType.EXACT) ?
                    text.substring(0, operatorIndex + 1) : text.substring(0, operatorIndex);
            value = StringUtils.substringUntilSpaceOrString(text, text.indexOf("=") + 1, List.of("]"));
            if (value.startsWith("\"") || value.startsWith("'")) {
                value = value.substring(1);
            }
            if (value.endsWith("\"") || value.endsWith("'")) {
                value = value.substring(0, value.length() - 1);
            }
            if (text.endsWith(" i")) {
                caseInsensitive = true;
            }
        } else {
            name = text;
            value = null;
            comparisonType = CSSConstants.AttributeSelectorComparisonType.NONE;
        }

        CSSUnitAttributeSelector selector = new CSSUnitAttributeSelector(name, value, comparisonType);
        selector.caseInsensitive = caseInsensitive;
        return selector;
    }

    private static String getSelectorListString(String fullSelectorText, int startIndex) {
        StringBuilder string = new StringBuilder();
        boolean inAttribute = false;
        for (int i = startIndex; i < fullSelectorText.length(); i++) {
            char c = fullSelectorText.charAt(i);
            if (c == '[') {
                inAttribute = true;
            } else if (c == ' ' && !inAttribute) {
                break;
            }

            string.append(c);
        }

        return string.toString();
    }

    private static void addRuleDeclarationSet(Map<CSSSelectorGroup, Map<String, String>> rules, CSSSelectorGroup group, Map<String, String> newRules) {
        if (rules.containsKey(group)) {
            Map<String, String> currentRules = rules.get(group);
            for (String key : newRules.keySet()) {
                currentRules.put(key, newRules.get(key));
            }
        } else {
            rules.put(group, new HashMap<>(newRules));
        }
    }

}
