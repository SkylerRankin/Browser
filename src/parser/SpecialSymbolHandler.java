package parser;

import java.util.HashMap;
import java.util.Map;

public class SpecialSymbolHandler {
	
	/*
	 * put all these symbols in a text file and load them
	 * https://www.rapidtables.com/web/html/html-codes.html
	 * and have a function to replace node text with the special symbols
	 */
	
	private static Map<String, String> characters = new HashMap<String, String>();
	
	public static void loadSpecialSymbols() {
		
	}
	
	public String insertSymbols(String s) {
		return s;
	}

}
