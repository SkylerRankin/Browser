package browser.layout;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import browser.css.CSSStyle;
import browser.model.Vector2;

import lombok.Data;

public class TextDimensionCalculator {

    private final Map<Integer, Vector2> cache = new HashMap<>();

    /**
     * Determine the width and height of the box containing some text based on its CSS styling.
     * @param string      The text to determine the size of.
     * @param style     The CSS rules applying to the text.
     * @return      A vector of the text's width and height;
     */
    public Vector2 getDimension(String string, CSSStyle style) {
        int key = new TextCacheKey(style.fontFamily, style.fontSize, style.fontWeight.ordinal()).hashCode();
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        Text text = new Text(string);
        FontWeight fontWeight = FontWeight.NORMAL;
        if (style.fontWeight == CSSStyle.fontWeightType.BOLD) {
            fontWeight = FontWeight.BOLD;
        }
        text.setFont(Font.font(style.fontFamily, fontWeight, style.fontSize));
        float width = (float) text.getBoundsInLocal().getWidth();
        float height = (float) text.getBoundsInLocal().getHeight();
        Vector2 dimensions = new Vector2(width, height);
        cache.put(key, dimensions);
        // TODO remove less used entries from cache. Is a cache even helping here anyways?
        return dimensions;
    }

    @Data
    private static class TextCacheKey {
        private final String fontFamily;
        private final int fontSize;
        private final int fontWeight;
    }
}
