package css;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DefaultColors {
	
	// Map between color names and hex strings
	private static Map<String, String> defaultColors = new HashMap<String, String>();
	
	/**
	 * Read the data files to load all the default color values
	 */
	public static void init() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader("./src/css/defaultColors.txt"));
			String line;
			while ((line = reader.readLine()) != null) {
				String name = line.substring(0, line.indexOf(' '));
				String hex = line.substring(line.indexOf(' ') + 2);
				defaultColors.put(name.toLowerCase(), hex);
			}
			reader.close();
		} catch (IOException e) {
			System.err.println("DefaultColors: error reading file defaultColors.txt");
			e.printStackTrace();
		}
	}
	
	public static String getHex(String name) {
		return defaultColors.get(name.toLowerCase());
	}

}
