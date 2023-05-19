package browser.parser;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {

    public static String substringUntil(String string, int startIndex, String endString) {
        return substringUntil(string, startIndex, List.of(endString));
    }

    public static String substringUntil(String string, int startIndex, List<String> endStrings) {
        return substringUntil(string, startIndex, endStrings, false);
    }

    public static String substringUntil(String string, int startIndex, List<String> endStrings, boolean endInclusive) {
        int endIndex = -1;
        String matchingEndString = null;
        for (String endString : endStrings) {
            int newIndex = string.indexOf(endString, startIndex);
            if (endIndex == -1 || newIndex < endIndex) {
                endIndex = newIndex;
                matchingEndString = endString;
            }
        }
        if (endIndex == -1) {
            return string.substring(startIndex);
        } else if (endIndex == startIndex) {
            return endInclusive ? matchingEndString : "";
        } else {
            return endInclusive ?
                    string.substring(startIndex, endIndex + matchingEndString.length()) :
                    string.substring(startIndex, endIndex);
        }
    }

    public static String substringUntilSpace(String string, int startIndex) {
        return substringUntilSpaceOrString(string, startIndex, List.of());
    }

    public static String substringUntilSpaceOrString(String string, int startIndex, List<String> endStrings) {
        return substringUntilSpaceOrString(string, startIndex, endStrings, false);
    }

    public static String substringUntilSpaceOrString(String string, int startIndex, List<String> endStrings, boolean endInclusive) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = startIndex; i < string.length(); i++) {
            if (Character.isWhitespace(string.charAt(i))) {
                break;
            }

            boolean endStringMatched = false;
            for (String endString : endStrings) {
                if (substringMatch(string, endString, i)) {
                    endStringMatched = true;
                    if (endInclusive) {
                        stringBuilder.append(endString);
                    }
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
            if (string.charAt(i) != match.charAt(i - startIndex)) {
                return false;
            }
        }
        return true;
    }

    public static <T extends Enum<T>> T toEnum(Class<T> enumClass, String string) {
        if (enumClass != null && string != null) {
            try {
                return Enum.valueOf(enumClass, string);
            } catch (IllegalArgumentException ignored) {}
        }
        return null;
    }

    public static int prevIndexOf(String base, String search, int start) {
        if (start < 0 || start >= base.length()) {
            return -1;
        }
        for (int i = start; i >= 0; i--) {
            if (substringMatch(base, search, i)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Splits a string on a set of delimiters, but includes any resulting empty strings between delimiters as well as
     * at the start/end of the string. Delimiters are checked in the provided order, which might have implications if
     * some are prefixes of others.
     * @param text      The string to split.
     * @param delimiters        The delimiters to split on.
     * @return      A list of strings.
     */
    public static List<String> splitStringIncludeEmpty(String text, List<String> delimiters) {
        List<String> lines = new ArrayList<>();

        int start = 0;
        int end = 0;

        while (end < text.length()) {
            boolean foundDelimiter = false;
            for (String delimiter : delimiters) {
                if (substringMatch(text, delimiter, end)) {
                    lines.add(text.substring(start, end));
                    start = end + delimiter.length();
                    end = start;
                    foundDelimiter = true;
                    break;
                }
            }
            if (!foundDelimiter) {
                end++;
            }
        }
        lines.add(text.substring(start, end));

        return lines;
    }

}
