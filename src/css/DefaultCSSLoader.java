package css;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;

import model.RenderNode;
import parser.CSSParser;
import parser.CSSParser.Selector;

public class DefaultCSSLoader {
	
	public static void loadDefaults(RenderNode root) {
		
		CSSParser parser = new CSSParser();
		String cssString = "";
		try {
			cssString = new String(Files.readAllBytes(Paths.get("./src/css/default.css")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		parser.parse(cssString);
		Map<Selector, Map<String, String>> rules = parser.getRules();
		
		System.out.printf("DefaultCSSLoader : loaded %d rules\n", rules.size());
		for (Entry<Selector, Map<String, String>> e : rules.entrySet()) {
			System.out.printf("\t%s\n", e.getKey());
			for (Entry<String, String> e2 : e.getValue().entrySet()) {
				System.out.printf("\t\t%s: %s\n", e2.getKey(), e2.getValue());
			}
		}
		
		applyRules(root, rules);
	}
	
	// TODO handle selectors for nested elements; this is just 1 level
	private static void applyRules(RenderNode node, Map<Selector, Map<String, String>> rules) {
		// Create some representative selectors for this node
		CSSParser.Selector elementSelector = (new CSSParser()).new Selector(CSSParser.SelectorType.ELEMENT);
		elementSelector.values.add(node.type);
		
		Map<String, String> elementRule = rules.get(elementSelector);
		
		if (elementRule != null) {
			node.style.apply(elementRule);
		}
		
		for (RenderNode child : node.children) {
			applyRules(child, rules);
		}
	}

}
