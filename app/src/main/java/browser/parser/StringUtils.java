package browser.parser;

import java.util.List;

public class StringUtils {

    public static int getLengthOfWhitespace(int startIndex, String string) {
        int endIndex = startIndex;
        while (Character.isWhitespace(string.charAt(startIndex))) {
            if (startIndex == string.length() - 1) {
                return startIndex;
            }
            startIndex++;
        }
        return endIndex - startIndex + 1;
    }

    public static String substringUntil(String string, int startIndex, String endString) {
        int endIndex = string.indexOf(endString, startIndex);
        if (endIndex == -1) {
            return string.substring(startIndex);
        } else if (endIndex == startIndex) {
            return "";
        } else {
            return string.substring(startIndex, endIndex);
        }
    }

    public static String substringUntil(String string, int startIndex, List<String> endStrings) {
        int endIndex = -1;
        for (String endString : endStrings) {
            int newIndex = string.indexOf(endString, startIndex);
            if (endIndex == -1 || newIndex < endIndex) {
                endIndex = newIndex;
            }
        }
        if (endIndex == -1) {
            return string.substring(startIndex);
        } else if (endIndex == startIndex) {
            return "";
        } else {
            return string.substring(startIndex, endIndex);
        }
    }

    public static String substringUntilSpace(String string, int startIndex) {
        return substringUntilSpaceOrString(string, startIndex, List.of());
    }

    public static String substringUntilSpaceOrString(String string, int startIndex, List<String> endStrings) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = startIndex; i < string.length(); i++) {
            if (Character.isWhitespace(string.charAt(i))) {
                break;
            }

            boolean endStringMatched = false;
            for (String endString : endStrings) {
                if (substringMatch(string, endString, i)) {
                    endStringMatched = true;
                    break;
                }
            }

            if (endStringMatched) {
                break;
            }

            stringBuilder.append(string.charAt(i));
        }
        return stringBuilder.toString();
    }

    public static String alphaNumericSubstring(String string, int startIndex) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = startIndex; i < string.length(); i++) {
            char c = string.charAt(i);
            if (Character.isAlphabetic(c) || Character.isDigit(c)) {
                stringBuilder.append(c);
            } else {
                break;
            }
        }
        return stringBuilder.toString();
    }

    public static String whitespaceSubstring(String string, int startIndex) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = startIndex; i < string.length(); i++) {
            char c = string.charAt(i);
            if (Character.isWhitespace(c)) {
                stringBuilder.append(c);
            } else {
                break;
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Checks if the string at a given index is exactly some other string.
     *   substringMatch("div span", "span", 4) -> true
     *   substringMatch("div span", "span", 0) -> false
     * @param string        The base string.
     * @param match     The substring that should be present at index.
     * @param startIndex        The index to start at.
     * @return      True if match is present at startIndex.
     */
    public static boolean substringMatch(String string, String match, int startIndex) {
        if (startIndex >= string.length() || startIndex + match.length() > string.length()) {
            return false;
        }
        for (int i = startIndex; i < Math.min(string.length(), startIndex + match.length()); i++) {
            char c = string.charAt(i);
            char c2 = match.charAt(i - startIndex);
            if (string.charAt(i) != match.charAt(i - startIndex)) {
                return false;
            }
        }
        return true;
    }

}
