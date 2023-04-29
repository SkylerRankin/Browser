package browser.css;

import java.util.HashMap;
import java.util.Map;

import browser.model.RenderNode;

public class LegacyCSSLoader {

    private static Map<String, String> styleAttributes = Map.ofEntries(
            Map.entry("bgcolor", "background-color"),
            Map.entry("width", "width")
    );

    /**
     * In HTML4 and earlier, some styling was done through element attributes. A small set of these attributes are
     * supported by this engine and are converted into normal CSS strings here.
     * @param node      The render node to extract styling from.
     * @return      A CSS string.
     */
    public static String getCSSFromAttributes(RenderNode node) {
        Map<String, String> properties = new HashMap<>();
        for (Map.Entry<String, String> e : node.attributes.entrySet()) {
            if (styleAttributes.containsKey(e.getKey())) {
                properties.put(styleAttributes.get(e.getKey()), e.getValue());
            }
        }

        if (properties.size() == 0) {
            return null;
        } else {
            StringBuilder css = new StringBuilder();
            for (Map.Entry<String, String> e : properties.entrySet()) {
                css.append(String.format("%s: %s;", e.getKey(), e.getValue()));
            }
            return String.format("%s { %s }", node.type, css);
        }
    }

}
