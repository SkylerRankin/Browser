package browser.parser;

import java.util.*;
import java.util.regex.Matcher;

import browser.constants.CSSConstants;
import browser.constants.CSSConstants.SelectorCombinator;
import browser.model.CSSSelector;
import browser.model.CSSSelectorGroup;
import browser.model.CSSToken;

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
                        rules.put(selectorGroup, currentRuleSet);
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

                String itemText = StringUtils.substringUntilSpace(item, i);
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
        List<CSSConstants.SelectorType> types = new ArrayList<>();
        List<String> values = new ArrayList<>();

        int i = 0;
        while (i < text.length()) {
            String c = text.substring(i, i + 1);
            Matcher matcher = CSSConstants.CSS_IDENTIFIER_PATTERN.matcher(text.substring(i));
            switch (c) {
                case "*" -> {
                    types.add(CSSConstants.SelectorType.UNIVERSAL);
                    values.add(c);
                    i++;
                }
                case "." -> {
                    if (matcher.find()) {
                        String classString = matcher.group();
                        types.add(CSSConstants.SelectorType.CLASS);
                        values.add(classString);
                        i += classString.length() + 1;
                    } else {
                        i++;
                    }
                }
                case "#" -> {
                    if (matcher.find()) {
                        String idString = matcher.group();
                        types.add(CSSConstants.SelectorType.ID);
                        values.add(idString);
                        i += idString.length() + 1;
                    } else {
                        i++;
                    }
                }
                case "[" -> {
                    int attributeEnd = text.indexOf("]", i + 1);
                    if (attributeEnd != -1) {
                        String attributeText = text.substring(i + 1, attributeEnd);
                        // TODO parse attribute further
                        types.add(CSSConstants.SelectorType.ATTRIBUTE);
                        values.add(attributeText);
                        i += attributeText.length() + 2;
                    } else {
                        i++;
                    }
                }
                case ":" -> {
                    if (matcher.find()) {
                        String pseudoString = matcher.group();
                        types.add(CSSConstants.SelectorType.PSEUDO);
                        values.add(pseudoString);
                        i += pseudoString.length() + 1;
                    } else {
                        i++;
                    }
                }
                default -> {
                    if (matcher.find()) {
                        String typeString = matcher.group();
                        types.add(CSSConstants.SelectorType.TYPE);
                        values.add(typeString);
                        i += typeString.length();
                    } else {
                        i++;
                    }
                }
            }
        }

        selectorGroup.selectors.add(new CSSSelector(types, values));
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

}
