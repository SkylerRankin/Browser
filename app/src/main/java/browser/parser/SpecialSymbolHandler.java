package browser.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SpecialSymbolHandler {
	
	private static Map<String, String> symbols = new HashMap<String, String>();
	
	public static void init() {
	    try {
            BufferedReader reader = new BufferedReader(new FileReader("./src/main/resources/data/specialSymbols.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split("\\t");
                if (values.length > 1) {
                    symbols.put(values[1], values[0]);
                    if (values.length == 3) {
                        symbols.put(values[2], values[0]);
                    }
                }
            }
            reader.close();
            System.out.printf("SpecialSymbolHandler: loaded %d symbols\n", symbols.size());
        } catch (IOException e) {
            System.err.println("SpecialSymbolHandler: error reading file specialSymbols.txt");
            e.printStackTrace();
        }
	    
	}
	
	public static String insertSymbols(String s) {
	    if (s == null) return s;
	    for (Entry<String, String> e : symbols.entrySet()) {
	        s = s.replace(e.getKey(), e.getValue());
	    }
	    s = s.replace("\\\\t", " ")
	            .replace("\\\\s", " ")
	            .replace("\\\\r", " ");
		return s;
	}

}
