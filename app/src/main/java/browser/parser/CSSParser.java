package browser.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import browser.model.CSSNode;

public class CSSParser {
    
    private final boolean debug = false;
    
    public enum SelectorType {
        CLASS,
        NESTED_CLASS,
        ID,
        ALL,
        ELEMENT,
        NESTED_ELEMENT,
        ELEMENT_CLASS
    }
    
    // Map of CSS selectors to a mapping of rules.
    private Map<Selector, Map<String, String>> rules;
    // Root node of the CSS Object Model.
    private CSSNode cssOM;
    // Map from ID name to rules.
    private Map<String, Map<String, String>> idMap;
    // Map of class name/names to rules. If nested classes, key is space deliminated.
    // Ex. subtitle 
    private Map<String, Map<String, String>> classMap;
    
    /**
     * Parse a string of CSS, overwriting current rules only when conflicts exist.
     * After calling, this object will expose the parsed information through GET functions.
     */
    public void parse(String css) {
        css = css.replaceAll("[\r\n]", " ");
        css = removeComments(css);
        rules = parseRules(css);
        cssOM = generateCSSOM();
        idMap = generateIDMap();
    }
    
    public CSSNode generateCSSOM() {
        
        CSSNode root = new CSSNode("default");
        // Add default CSS styles.
        
        // Create the body node and children of body nodes.
        CSSNode body = new CSSNode(HTMLElements.BODY);
        body.parent = root;
        for (Entry<Selector, Map<String, String>> e : rules.entrySet()) {
            if (e.getKey().type.equals(SelectorType.ALL)) {
                for (Entry<String, String> declaration : ((Map<String, String>)e.getValue()).entrySet()) {
                    body.declarations.put(declaration.getKey(), declaration.getValue());
                }
            } else if (e.getKey().type.equals(SelectorType.ELEMENT)) {
                for (String element : e.getKey().values) {
                    // add node to body if not present
                    if (!body.hasChild(element)) {
                        CSSNode newChild = new CSSNode(element);
                        newChild.parent = body;
                        body.children.add(newChild);
                    }
                    CSSNode child = body.getChild(element);
                    // add all declarations to that node
                    for (Entry<String, String> declaration : ((Map<String, String>)e.getValue()).entrySet()) {
                        child.declarations.put(declaration.getKey(), declaration.getValue());
                    }
                }
            }
        }
        
        // Create nodes for nested CSS elements.
        for (Entry<Selector, Map<String, String>> e : rules.entrySet()) {
            if (e.getKey().type.equals(SelectorType.NESTED_ELEMENT)) {
                CSSNode currentNode = body;
                List<String> elementPath = e.getKey().values;
                for (String element : elementPath) {
                    boolean nodeAlreadyExisted = false;
                    for (CSSNode child : currentNode.children) {
                        if (child.type.equals(element)) {
                            currentNode = child;
                            nodeAlreadyExisted = true;
                            break;
                        }
                    }
                    if (!nodeAlreadyExisted) {
                        CSSNode newNode = new CSSNode(element);
                        currentNode.children.add(newNode);
                        newNode.parent = currentNode;
                        currentNode = newNode;
                    }
                }
                for (Entry<String, String> declaration : ((Map<String, String>)e.getValue()).entrySet()) {
                    currentNode.declarations.put(declaration.getKey(), declaration.getValue());
                }
            }
        }
        
        root.children.add(body);
        
        return root;
    }
    
    public Map<String, Map<String, String>> generateIDMap() {
        Map<String, Map<String, String>> idMap = new HashMap<String, Map<String, String>>();
        for (Entry<Selector, Map<String, String>> e : rules.entrySet()) {
            if (e.getKey().type.equals(SelectorType.ID)) {
                if (!idMap.containsKey(e.getKey().values.get(0))) {
                    idMap.put(e.getKey().values.get(0), new HashMap<String, String>());
                }
                for (Entry<String, String> declaration : ((Map<String, String>)e.getValue()).entrySet()) {
                    idMap.get(e.getKey().values.get(0)).put(declaration.getKey(), declaration.getValue());
                }
            }
        }
        return idMap;
    }
    
    public CSSNode getCSSOM() { return cssOM; }
    public Map<Selector, Map<String, String>> getRules() { return rules; }
    public Map<String, Map<String, String>> getIDMap() { return idMap; }
    public Map<String, Map<String, String>> getClassMap() { return classMap; }
    
    public String removeComments(String css) {
        final String commentsRegex = "/\\*.*?\\*/";
        return css.replaceAll(commentsRegex, "");
    }
    
    /**
     * Parses some string of CSS into a map from selectors to sets of declarations.
     * @param css
     * @return
     */
    public Map<Selector, Map<String, String>> parseRules(String css) {
        final String ruleRegex = "[\\_\\-(\\s?>\\s?)\\:@\\.#a-zA-Z0-9,\\*(\\s+)?]+\\{(.|\r\n)*?\\}";
        final String declarationRegex = "[^:]+:\\s*[^;]+;";
        final String lastDeclarationRegex = "[^:]+:\\s*[^;]+;?";
        
        Pattern declarationPattern = Pattern.compile(declarationRegex);
        Pattern lastDeclarationPattern = Pattern.compile(lastDeclarationRegex);
        
        Map<Selector, Map<String, String>> rules = new HashMap<Selector, Map<String, String>>();
        Matcher ruleMatcher = Pattern.compile(ruleRegex).matcher(css);
        
        while (ruleMatcher.find()) {
            String match = ruleMatcher.group();
            Map<String, String> declarations = new HashMap<String, String>();
            String declarationText = match.substring(match.indexOf("{")+1, match.indexOf("}"));
            Matcher declarationMatcher = declarationPattern.matcher(declarationText);
            
            String[] selectorTexts = match.substring(0, match.indexOf("{")).trim().split(",");
            List<Selector> selectors = new ArrayList<Selector>();
            
            for (String selectorText : selectorTexts) {
                selectors.add(parseSelector(selectorText.trim()));
            }
                        
            int lastIndex = 0;
            
            // Match each declaration. Requires a semicolon at the end
            while (declarationMatcher.find()) {
               String declarationsText = declarationMatcher.group().trim();
               lastIndex = declarationMatcher.end();
               for (String declaration : declarationsText.split(";")) {
                   declarations.put(
                           declaration.substring(0, declaration.indexOf(":")).trim(),
                           declaration.substring(declaration.indexOf(":") + 1).trim());
               }
            }
            
            // Try to match for a last declaration that has an optional semicolon.
            Matcher lastDeclarationMatcher = lastDeclarationPattern.matcher(declarationText.substring(lastIndex));
            if (lastDeclarationMatcher.find()) {
                String lastDeclaration = lastDeclarationMatcher.group().trim();
                declarations.put(
                        lastDeclaration.substring(0, lastDeclaration.indexOf(":")).trim(),
                        lastDeclaration.substring(lastDeclaration.indexOf(":") + 1).trim());
            }
            
            for (Selector selector : selectors) {
                // If the selector was already parsed, then merge the declarations.
                if (rules.containsKey(selector)) {
                    Map<String, String> prevDeclarations = rules.get(selector);
                    for (Entry<String, String> e : declarations.entrySet()) {
                        prevDeclarations.put(e.getKey(), e.getValue());
                    }
                    rules.put(selector, prevDeclarations);
                } else if (selector != null) {
                    // Create a fresh set of declarations so future merges for other selectors don't change it
                    Map<String, String> newDeclarations = new HashMap<String, String>();
                    newDeclarations.putAll(declarations);
                    rules.put(selector, newDeclarations);
                } else if (debug) {
                    System.out.printf("CSSParser: selector was null. was not parsed correctly. %s\n", declarations);
                }
            }
            
        }
        
        return rules;
    }
    
    public Selector parseSelector(String text) {
        Selector selector = null;
        if (text.matches("^(\\.[\\w-]+)+$")) {
            selector = new Selector(SelectorType.CLASS);
            for (String s : text.split("\\.")) {
                if (s.length() > 0) selector.values.add(s);
            }
        } else if (text.matches("^(\\.[\\w-]+\\s+)+\\.[\\w-]+$")) {
            selector = new Selector(SelectorType.NESTED_CLASS);
            for (String s : text.split("\\.")) {
                if (s.length() > 0) selector.values.add(s.trim());
            }
        } else if (text.matches("^#[\\w-]+$")) {
            selector = new Selector(SelectorType.ID);
            selector.values.add(text.substring(1));
        } else if (text.matches("^\\*$")) {
            selector = new Selector(SelectorType.ALL);
        } else if (text.matches("^[\\w-]+$")) {
            selector = new Selector(SelectorType.ELEMENT);
            selector.values.add(text);
        } else if (text.matches("^([\\w-]+,\\s*)+[\\w-]+$")) {
            selector = new Selector(SelectorType.ELEMENT);
            for (String s : text.split(",")) {
                if (s.length() > 0) selector.values.add(s.trim());
            }
        } else if (text.matches("^([\\w-]+\\s+)+[\\w-]+$")) {
            selector = new Selector(SelectorType.NESTED_ELEMENT);
            for (String s : text.split("\\s")) {
                if (s.length() > 0) selector.values.add(s.trim());
            }
        } else if (text.matches("^[\\w-]+\\.[\\w-]+$")) {
            selector = new Selector(SelectorType.ELEMENT_CLASS);
            selector.values.add(text.substring(0, text.indexOf(".")));
            selector.values.add(text.substring(text.indexOf(".")+1));
        }
        return selector;
    }
    
    public void printRules() {
        System.out.printf("--- CSSParser: %d Rules ---\n", rules.size());
        for (Entry<Selector, Map<String, String>> entry : rules.entrySet()) {

            System.out.printf("%s:\n", entry.getKey().toString());
            for (Entry<String, String> rule : entry.getValue().entrySet()) {
                System.out.printf("\t%s: %s\n", rule.getKey(), rule.getValue());
            }
        }
        System.out.println("---------------------------\n");
    }
    
    public static class Selector {
        public SelectorType type;
        public List<String> values;
        public Selector(SelectorType type) {
            this.type = type;
            values = new ArrayList<String>();
        }
        
        @Override
        public String toString() {
            String string = this.type.toString();
            for (String s : values) {
                string += ", "+s;
            }
            return string;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Selector)) return false;
            Selector s = (Selector) obj;
            if (!s.type.equals(this.type)) return false;
            if (s.values.size() != this.values.size()) return false;
            for (int i = 0; i < this.values.size(); i++) {
                if (!this.values.get(i).equals(s.values.get(i))) return false;
            }
            return true;
        }
        
        @Override
        public int hashCode() {
            int hash = type.toString().hashCode();
            for (String s : values) hash += s == null ? 0 : s.hashCode();
            return hash;
        }
    }

}
