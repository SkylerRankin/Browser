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
	
	public void applyAllCSS(RenderNode root) {
		loadDefaults(root);
		applyInline(root);
		finalizeCSS(root);
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
		
		parser.printRules();
		
		applyRules(root, rules);
		propagateCSS(root);
	}
	
	public void applyInline(RenderNode root) {
		
		if (root.attributes.containsKey("style")) {
			CSSParser parser = new CSSParser();
			String style = root.attributes.get("style");
			parser.parse(String.format("%s { %s }", root.type, style));
			parser.printRules();
			applyRules(root, parser.getRules());
		}
		
		for (RenderNode child : root.children) {
			applyInline(child);
		}
	}

	//TODO handle selectors for nested elements; this is just 1 level
	private static void applyRules(RenderNode node, Map<Selector, Map<String, String>> rules) {
		// Create some representative selectors for this node
		CSSParser.Selector allSelector = (new CSSParser()).new Selector(CSSParser.SelectorType.ALL);
		CSSParser.Selector elementSelector = (new CSSParser()).new Selector(CSSParser.SelectorType.ELEMENT);
		elementSelector.values.add(node.type);
		
		Map<String, String> allRule = rules.get(allSelector);
		Map<String, String> elementRule = rules.get(elementSelector);
		
		if (allRule != null) node.style.apply(allRule);
		if (elementRule != null) node.style.apply(elementRule);
		
		for (RenderNode child : node.children) {
			applyRules(child, rules);
		}
	}
	
	/**
	 * Apply most of the rules of the parent to the child. In some cases, the CSS is not supposed 
	 * to be inherited, such as with dimensions. Also, if the child has already set this property,
	 * then the parent should not override it.
	 * @param root
	 */
	public void propagateCSS(RenderNode root) {
		RenderNode parent = parentRenderNodeMap.get(root.id);
		
		if (parent != null) {
			for (Entry<String, String> e : parent.style.getAllProperties().entrySet()) {
//				if (root.type.equals("tt")) System.out.printf("tt %s:%s, %b\n", e.getKey(), e.getValue(), CSSStyle.propagateAttribute(e.getKey()));
				if (!root.style.hasPropertySet(e.getKey()) && CSSStyle.propagateAttribute(e.getKey())) {
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
