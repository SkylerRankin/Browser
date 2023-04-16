package browser.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import browser.constants.CSSConstants;
import browser.model.CSSMediaExpression;

public class MediaQueryParser {

    private static final String MEDIA_PREFIX = "@media";

    public static CSSMediaExpression getExpression(String text) {
        text = text.substring(MEDIA_PREFIX.length()).trim();
        String[] rules = text.split(",");
        List<CSSMediaExpression> expressions = new ArrayList<>();
        for (String rule : rules) {
            expressions.add(parseMediaExpression(rule.trim()));
        }

        if (expressions.size() == 0) {
            return null;
        } else if (expressions.size() == 1) {
            return expressions.get(0);
        } else {
            // Combine multiple comma-separated expressions into a single OR'd expression.
            CSSMediaExpression rootExpression = new CSSMediaExpression();
            CSSMediaExpression currentExpression = rootExpression;
            for (int i = 0; i < expressions.size(); i++) {
                currentExpression.leftHandExpression = expressions.get(i);
                currentExpression.operator = CSSConstants.MediaQueryOperator.OR;
                if (i == expressions.size() - 2) {
                    currentExpression.rightHandExpression = expressions.get(i + 1);
                    break;
                } else {
                    CSSMediaExpression nextExpression = new CSSMediaExpression();
                    currentExpression.rightHandExpression = nextExpression;
                    currentExpression = nextExpression;
                }
            }
            return rootExpression;
        }
    }

    private static CSSMediaExpression parseMediaExpression(String text) {
        CSSMediaExpression singleExpression = getSingleExpression(text);
        if (singleExpression != null) {
            return singleExpression;
        }

        // Check if text is not a compound expression
        if (!text.contains("\s")) {
            System.err.printf("Unknown expression \"%s\". Ignoring.\n", text);
            return null;
        }

        CSSMediaExpression expression = new CSSMediaExpression();
        int i = 0;
        while (i < text.length()) {
            if (Character.isWhitespace(text.charAt(i))) {
                i++;
            } else if (text.charAt(i) == '(') {
                String substring = getParenthesesMatchedSubstring(text.substring(i));
                if (substring != null) {
                    CSSMediaExpression keyValueExpression = getSingleExpression(substring);
                    if (keyValueExpression != null) {
                        expression.leftHandExpression = keyValueExpression;
                    } else {
                        expression.leftHandExpression = parseMediaExpression(substring.substring(1, substring.length() - 1).trim());
                    }
                    i += substring.length();
                } else {
                    System.err.printf("Unmatched parentheses, \"%s\".\n", text.substring(i));
                    return null;
                }
            } else {
                String nextWord = StringUtils.substringUntilSpace(text, i);
                CSSMediaExpression subExpression = getSingleExpression(nextWord);
                CSSConstants.MediaQueryOperator operator = StringUtils.toEnum(CSSConstants.MediaQueryOperator.class, nextWord.toUpperCase());

                if (subExpression != null) {
                    expression.leftHandExpression = subExpression;
                    i += nextWord.length();
                } else if (operator != null) {
                    expression.operator = operator;
                    String remainingText = text.substring(i + nextWord.length()).trim();
                    CSSMediaExpression remainingExpression = getSingleExpression(remainingText);
                    if (remainingExpression != null) {
                        expression.rightHandExpression = remainingExpression;
                    } else {
                        if (remainingText.startsWith("(") && stringIsFullyWithinParentheses(remainingText)) {
                            remainingText = remainingText.substring(1, remainingText.length() - 1);
                        }
                        expression.rightHandExpression = parseMediaExpression(remainingText);
                    }
                    return expression;
                } else {
                    System.err.printf("Unknown expression \"%s\" in \"%s\". Ignoring.\n", nextWord, text);
                    return null;
                }
            }
        }

        return expression;
    }

    private static CSSMediaExpression getSingleExpression(String text) {
        // Check if text is a key-value pair
        Matcher keyValueMatcher = CSSConstants.CSS_MEDIA_KEY_VALUE_PATTERN.matcher(text);
        if (keyValueMatcher.find()) {
            String key = keyValueMatcher.group(1);
            String value = keyValueMatcher.group(2);
            try {
                CSSMediaExpression expression = new CSSMediaExpression();
                expression.feature = CSSConstants.MediaFeature.valueOf(key.toUpperCase().replace("-", "_"));
                expression.featureValue = value;
                return expression;
            } catch (IllegalArgumentException e) {
                System.err.printf("Unsupported media query feature \"%s\". Ignoring.\n", key);
                return null;
            }
        }

        // Check if text is a media type
        CSSConstants.MediaType mediaType;
        if ((mediaType = StringUtils.toEnum(CSSConstants.MediaType.class, text.toUpperCase())) != null) {
            CSSMediaExpression expression = new CSSMediaExpression();
            expression.mediaType = mediaType;
            return expression;
        }

        return null;
    }

    private static String getParenthesesMatchedSubstring(String text) {
        if (!text.startsWith("(")) {
            return null;
        } else {
            int openParentheses = 1;
            int closeParentheses = 0;
            int j = 1;
            while (openParentheses > closeParentheses && j < text.length()) {
                if (text.charAt(j) == '(') {
                    openParentheses++;
                } else if (text.charAt(j) == ')') {
                    closeParentheses++;
                }
                j++;
            }

            if (openParentheses != closeParentheses) {
                System.err.printf("Unmatched parenthesis: \"%s\".\n", text);
                return null;
            }

            return text.substring(0, j).trim();
        }
    }

    private static boolean stringIsFullyWithinParentheses(String text) {
        String substring = getParenthesesMatchedSubstring(text);
        return substring != null && substring.equals(text);
    }

}
