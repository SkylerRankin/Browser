package css;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;

import model.RenderNode;
import parser.CSSParser;
import parser.CSSParser.Selector;

public class CSSLoader {
	
	private Map<Integer, RenderNode> parentRenderNodeMap;
	
	public CSSLoader(Map<Integer, RenderNode> parentRenderNodeMap) {
		this.parentRenderNodeMap = parentRenderNodeMap;
	}
	
	public void loadDefaults(RenderNode root) {
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
		propagateCSS(root);
		finalizeCSS(root);
	}

	//TODO handle selectors for nested elements; this is just 1 level
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
	
	public void propagateCSS(RenderNode root) {
		RenderNode parent = parentRenderNodeMap.get(root.id);
		
		if (parent != null) {
			for (Entry<String, String> e : parent.style.getAllProperties().entrySet()) {
				if (!root.style.hasPropertySet(e.getKey())) {
					root.style.setProperty(e.getKey(), e.getValue());
				}
			}
		}
		
		for (RenderNode child : root.children) {
			propagateCSS(child);
		}
	}
	
	
	/**
	 * Clears the set that stores which properties have been set. Does not actually remove
	 * the values themselves.
	 * @param root
	 */
	public void resetSetProperties(RenderNode root) {
		root.style.resetSetProperties();;
		for (RenderNode child : root.children) {
			resetSetProperties(child);
		}
	}
	
	public void finalizeCSS(RenderNode root) {
		root.style.finalizeCSS();
		for (RenderNode child : root.children) {
			finalizeCSS(child);
		}
	}

}
