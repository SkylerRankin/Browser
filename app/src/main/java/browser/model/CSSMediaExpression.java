package browser.model;

import browser.constants.CSSConstants;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class CSSMediaExpression {

    // Compound expression data
    public CSSMediaExpression leftHandExpression = null;
    public CSSMediaExpression rightHandExpression = null;
    public CSSConstants.MediaQueryOperator operator;

    // Terminal data
    public CSSConstants.MediaType mediaType = null;
    public CSSConstants.MediaFeature feature = null;
    public String featureValue = null;

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        if (mediaType != null) {
            stringBuilder.append(mediaType.name().toLowerCase());
        } else if (feature != null) {
            stringBuilder.append(String.format("%s: %s", feature.name().toLowerCase(), featureValue));
        } else if (leftHandExpression == null && operator == null && rightHandExpression == null) {
            stringBuilder.append("Empty");
        } else {
            stringBuilder.append(String.format("(%s) %s (%s)",
                    leftHandExpression == null ? "" : leftHandExpression.toString(),
                    operator == null ? "NONE" : operator.name(),
                    rightHandExpression == null ? "" : rightHandExpression.toString()));
        }

        return stringBuilder.toString();
    }

}
