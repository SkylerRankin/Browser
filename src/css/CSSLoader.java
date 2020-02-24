package css;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import model.DOMNode;
import model.RenderNode;
import parser.CSSParser;
import parser.CSSParser.Selector;
import parser.HTMLElements;

public class CSSLoader {
	
	private Map<Integer, RenderNode> parentRenderNodeMap;
	private List<String> externalCSS;
	private List<String> styleTagCSS;
	private final boolean debug = true;
	
	public CSSLoader(DOMNode dom, Map<Integer, RenderNode> parentRenderNodeMap, List<String> externalCSS) {
		this.parentRenderNodeMap = parentRenderNodeMap;
		this.externalCSS = externalCSS;
		styleTagCSS = new ArrayList<String>();
		findStyleTagCSS(dom);
	}
	
	/**
	 * Searches through the DOM tree for all style elements and adds their contents to the 
	 * styleTagCSS list. Assumes that the first and only child of a style element is a text
	 * element containing the CSS.
	 * @param root
	 */
	private void findStyleTagCSS(DOMNode root) {
	    if (root == null) return;
	    if (root.type.equals(HTMLElements.STYLE)) {
	        DOMNode text = root.children.get(0);
	        if (text != null) {
	            styleTagCSS.add(text.content);
	            if (debug) System.out.printf("");
	        } else {
	            System.err.println("CSSLoader: findStyleTagCSS: style node had no child text");
	        }
	    } else {
	        for (DOMNode child : root.children) {
	            findStyleTagCSS(child);
	        }
	    }
	}
	
	public void applyAllCSS(RenderNode root) {
		loadDefaults(root);
		loadExternalCSS(root);
		loadStyleTags(root);
		applyInline(root);
		propagateCSS(root);
		finalizeCSS(root);
	}
	
	public void loadDefaults(RenderNode root) {
		CSSParser parser = new CSSParser();
		String cssString = "";
		try {
			cssString = new String(Files.readAllBytes(Paths.get("./src/css/default.css")));
		} catch (IOException e) {
		    System.err.println("CSSLoader: failed to load default.css");
			e.printStackTrace();
		}
		parser.parse(cssString);
		Map<Selector, Map<String, String>> rules = parser.getRules();
		
		resetSetProperties(root);
		applyRules(root, rules);
	}
	
	public void loadExternalCSS(RenderNode root) {
	    for (String cssString : externalCSS) {
	        CSSParser parser = new CSSParser();
	        parser.parse(cssString);
	        Map<Selector, Map<String, String>> rules = parser.getRules();
	        
	        parser.printRules();
	        resetSetProperties(root);
	        applyRules(root, rules);
	    }
	    
	}
	
	public void loadStyleTags(RenderNode root) {
	    System.out.println("loadStyleTags");
	    for (String cssString : styleTagCSS) {
	        CSSParser parser = new CSSParser();
	        parser.parse(cssString);
	        Map<Selector, Map<String, String>> rules = parser.getRules();
	        resetSetProperties(root);
	        applyRules(root, rules);
	    }
	}
	
	/**
	 * Parse the inline style attribute and apple that style.
	 * TODO should not apply to every node of this type, just this node specifically
	 * @param root
	 */
	public void applyInline(RenderNode root) {
		if (root.attributes.containsKey("style")) {
			CSSParser parser = new CSSParser();
			String style = root.attributes.get("style");
			parser.parse(String.format("%s { %s }", root.type, style));
			
			resetSetProperties(root);
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
		CSSParser.Selector classSelector = (new CSSParser()).new Selector(CSSParser.SelectorType.CLASS);
		classSelector.values.add(node.attributes.get("class"));
		CSSParser.Selector idSelector = (new CSSParser()).new Selector(CSSParser.SelectorType.ID);
		idSelector.values.add(node.attributes.get("id"));
				
		Map<String, String> allRule = rules.get(allSelector);
		Map<String, String> elementRule = rules.get(elementSelector);
	    Map<String, String> classRule = rules.get(classSelector);
	    Map<String, String> idRule = rules.get(idSelector);
	    
//	    System.out.printf("%s, class=%s, %s\n", node.type, node.attributes.get("class"), classRule == null ? "none" : classRule.toString());
		
		if (allRule != null) node.style.apply(allRule);
		if (elementRule != null) node.style.apply(elementRule);
	    if (classRule != null) node.style.apply(classRule);
        if (idRule != null) node.style.apply(idRule);
		
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
		root.style.resetSetProperties();
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
