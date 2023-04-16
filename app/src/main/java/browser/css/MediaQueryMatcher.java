package browser.css;

import static browser.constants.CSSConstants.CSS_LENGTH_PATTERN;
import static browser.constants.MathConstants.DELTA;

import java.util.List;
import java.util.regex.Matcher;

import browser.constants.CSSConstants;
import browser.model.CSSMediaExpression;

public class MediaQueryMatcher {

    private static final List<CSSConstants.MediaType> acceptedMediaTypes = List.of(CSSConstants.MediaType.SCREEN, CSSConstants.MediaType.ALL);
    private static final String acceptedOrientation = "landscape";

    public static boolean matches(CSSMediaExpression expression, float screenWidth, float screenHeight) {
        if (expression == null) {
            return true;
        } else if (expression.mediaType != null) {
            return acceptedMediaTypes.contains(expression.mediaType);
        } else if (expression.feature != null) {
            return matchFeature(expression.feature, expression.featureValue, screenWidth, screenHeight);
        } else {
            boolean left = matches(expression.leftHandExpression, screenWidth, screenHeight);
            boolean right = matches(expression.rightHandExpression, screenWidth, screenHeight);
            if (expression.operator == null) {
                return left && right;
            } else {
                switch (expression.operator) {
                    case AND, ONLY -> { return left && right; }
                    case OR -> { return left || right; }
                    case NOT -> { return !right; }
                }
            }
        }

        return false;
    }

    private static boolean matchFeature(CSSConstants.MediaFeature feature, String value, float screenWidth, float screenHeight) {
        switch (feature) {
            case MAX_WIDTH -> {
                Float maxWidth = parsePixelSize(value);
                return maxWidth != null && screenWidth <= maxWidth;
            }
            case MIN_WIDTH -> {
                Float minWidth = parsePixelSize(value);
                return minWidth != null && screenWidth >= minWidth;
            }
            case WIDTH -> {
                Float width = parsePixelSize(value);
                return width != null && Math.abs(screenWidth - width) <= DELTA;
            }
            case HEIGHT -> {
                Float height = parsePixelSize(value);
                return height != null && Math.abs(screenHeight - height) <= DELTA;
            }
            case ORIENTATION -> {
                return value.equalsIgnoreCase(acceptedOrientation);
            }
        }
        return false;
    }

    private static Float parsePixelSize(String text) {
        Matcher matcher = CSS_LENGTH_PATTERN.matcher(text);
        if (matcher.find()) {
            float value = Float.parseFloat(matcher.group(1));
            String unitString = matcher.group(2);
            try {
                CSSConstants.LengthUnit unit = CSSConstants.LengthUnit.valueOf(unitString.toUpperCase());
                if (unit == CSSConstants.LengthUnit.PX) {
                    return value;
                } else {
                    System.err.printf("Unsupported length unit \"%s\".\n", unit);
                }
            } catch (IllegalArgumentException ignored) {}
        }

        return null;
    }

}
