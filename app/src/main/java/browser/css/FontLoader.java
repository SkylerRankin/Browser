package browser.css;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javafx.scene.text.Font;

public class FontLoader {
    
    private static Set<String> fonts;
    private static Map<String, String> genericFontMap;
    private static boolean debug = true;
    
    private final static String defaultFont = "Times New Roman";
    
    public static void init() {
        fonts = new HashSet<String>();
        for (String font : Font.getFontNames()) {
            fonts.add(font.toLowerCase());
        }
        
        genericFontMap = new HashMap<String, String>();
        genericFontMap.put("serif", "Times New Roman");
        genericFontMap.put("sans-serif", "Verdana");
        genericFontMap.put("cursive", "Lucida Calligraphy");
        genericFontMap.put("fantasy", "Papyrus");
        genericFontMap.put("monospace", "Lucida Console");
        
        if (debug) System.out.printf("DefaultFontLoader: loaded %d system fonts\n", fonts.size());
    }
    
    public static String getValidFont(String[] fonts) {
        for (String font : fonts) {
            if (font.toLowerCase().equals(defaultFont.toLowerCase())) return font;
            String validFont = getValidFont(font.toLowerCase());
            if (!validFont.toLowerCase().equals(defaultFont.toLowerCase())) return validFont;
        }
        return defaultFont;
    }
    
    public static String getValidFont(String font) {
        if (genericFontMap.containsKey(font)) {
            return genericFontMap.get(font);
        } else if (fonts.contains(font)) {
            return font;
        } else {
            return defaultFont;
        }
    }

}
