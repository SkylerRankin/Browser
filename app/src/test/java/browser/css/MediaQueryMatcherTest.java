package browser.css;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import browser.constants.CSSConstants;
import browser.model.CSSMediaExpression;

import org.junit.Test;

public class MediaQueryMatcherTest {

    @Test
    public void simpleMinAndMaxWidth() {
        // (min-width: 100px) and (max-width: 200px)

        // (min-width: 100px)
        CSSMediaExpression expression1 = new CSSMediaExpression();
        expression1.feature = CSSConstants.MediaFeature.MIN_WIDTH;
        expression1.featureValue = "100px";

        // (max-width: 200px)
        CSSMediaExpression expression2 = new CSSMediaExpression();
        expression2.feature = CSSConstants.MediaFeature.MAX_WIDTH;
        expression2.featureValue = "200px";

        // (min-width: 100px) and (max-width: 200px)
        CSSMediaExpression expression3 = new CSSMediaExpression();
        expression3.leftHandExpression = expression1;
        expression3.binaryOperator = CSSConstants.MediaQueryOperator.AND;
        expression3.rightHandExpression = expression2;

        assertFalse(MediaQueryMatcher.matches(expression3, 10, 0));
        assertTrue(MediaQueryMatcher.matches(expression3, 100, 0));
        assertTrue(MediaQueryMatcher.matches(expression3, 150, 0));
        assertFalse(MediaQueryMatcher.matches(expression3, 201, 0));
    }

    @Test
    public void simpleNegatedProperty() {
        // not (min-width: 100px) and (max-width: 200px)

        // (min-width: 100px)
        CSSMediaExpression expression1 = new CSSMediaExpression();
        expression1.feature = CSSConstants.MediaFeature.MIN_WIDTH;
        expression1.featureValue = "100px";

        // (max-width: 200px)
        CSSMediaExpression expression2 = new CSSMediaExpression();
        expression2.feature = CSSConstants.MediaFeature.MAX_WIDTH;
        expression2.featureValue = "200px";

        // not (min-width: 100px) and (max-width: 200px)
        CSSMediaExpression expression3 = new CSSMediaExpression();
        expression3.unaryOperator = CSSConstants.MediaQueryOperator.NOT;
        expression3.leftHandExpression = expression1;
        expression3.binaryOperator = CSSConstants.MediaQueryOperator.AND;
        expression3.rightHandExpression = expression2;

        assertTrue(MediaQueryMatcher.matches(expression3, 10, 0));
        assertFalse(MediaQueryMatcher.matches(expression3, 100, 0));
        assertFalse(MediaQueryMatcher.matches(expression3, 150, 0));
        assertTrue(MediaQueryMatcher.matches(expression3, 201, 0));
    }

    @Test
    public void simpleMixedQueries() {
        // only screen and ((min-width: 100px) or (orientation: landscape))

        // screen
        CSSMediaExpression expression1 = new CSSMediaExpression();
        expression1.mediaType = CSSConstants.MediaType.SCREEN;

        // (min-width: 100px)
        CSSMediaExpression expression2 = new CSSMediaExpression();
        expression2.feature = CSSConstants.MediaFeature.MIN_WIDTH;
        expression2.featureValue = "100px";

        // (orientation: landscape)
        CSSMediaExpression expression3 = new CSSMediaExpression();
        expression3.feature = CSSConstants.MediaFeature.ORIENTATION;
        expression3.featureValue = "landscape";

        // (min-width: 100px) or (orientation: landscape)
        CSSMediaExpression expression4 = new CSSMediaExpression();
        expression4.leftHandExpression = expression2;
        expression4.binaryOperator = CSSConstants.MediaQueryOperator.OR;
        expression4.rightHandExpression = expression3;

        // only screen and ((min-width: 100px) or (orientation: landscape))
        CSSMediaExpression expression5 = new CSSMediaExpression();
        expression5.unaryOperator = CSSConstants.MediaQueryOperator.ONLY;
        expression5.leftHandExpression = expression1;
        expression5.binaryOperator = CSSConstants.MediaQueryOperator.AND;
        expression5.rightHandExpression = expression4;

        assertTrue(MediaQueryMatcher.matches(expression5, 0, 0));
        assertTrue(MediaQueryMatcher.matches(expression5, 100, 0));
        assertTrue(MediaQueryMatcher.matches(expression5, 1000, 0));
    }

}
