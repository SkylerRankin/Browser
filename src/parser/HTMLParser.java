package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import css.CSSStyle;
import model.DOMNode;
import model.RenderNode;
import network.ResourceLoader;

public class HTMLParser {
    
    private ResourceLoader loader;
    private final boolean debug = false;
    
    public HTMLParser(ResourceLoader loader) {
        this.loader = loader;
    }
    
    public DOMNode generateDOMTree(String html) {
        
        // Trim white space and remove empty spaces between tags;
        html = html.trim();
        html = html.replaceAll(">\\s+<", "><");
        html = removeComments(html);
        html = removeXML(html);
        html = removeDoctype(html);
        
        int index = 0;
        DOMNode root = new DOMNode("root");
        DOMNode current = root;
        
        while (index < html.length()) {
            
            if (html.substring(index, index+1).equals("<")) {
                // If the current index is the start of an HTML tag.
                
                int end = html.indexOf(">", index+1);
                String fullTag = html.substring(index, end+1);
                if (fullTag.startsWith("</")) {
                    // If this is an ending tag, then move up to the parent.
                    current = current.parent;
                } else {
                    DOMNode n;
                    // If this is another new tag, set it to be the current tag to explore.
                    String content = html.substring(end-1, end).equals("/") ? html.substring(index+1, end-1) : html.substring(index+1, end);
                    
                    int spaceIndex = content.indexOf(' ');
                    if (spaceIndex == -1) {
                        n = new DOMNode(content);
                    } else {
                        n = new DOMNode(content.substring(0, spaceIndex));
                        n.attributes = getAttributes(content.substring(spaceIndex));
                        if (loader != null) {
                            loader.checkAttributes(content.substring(0, spaceIndex), n.attributes);
                        }
                    }
                    
                    n.parent = current;
                    current.children.add(n);
                    if (!HTMLElements.isEmptyElement(n.type)) {
                        current = n;
                    }
//                    System.out.printf("node = %s, current = %s, isEmpty = %b\n", n.type, current.type, HTMLElements.isEmptyElement(n.type));
//                    if (!isSingular(fullTag)) {
//                        current = n;
//                    }
                }
                index+=(end - index+1);
            } else {
                // If the current index is the start of the content between two tags.
                
                int end = html.indexOf("<", index+1);
                String content = html.substring(index, end);
                DOMNode n = new DOMNode(HTMLElements.TEXT);
                n.content = content;
                n.parent = current;
                current.children.add(n);
                index+=(end - index);
            }
        }
        
        return root;
    }
    
    public DOMNode getBodyNode(DOMNode dom) {
        if (dom.type.equals(HTMLElements.BODY)) return dom;
        DOMNode bodyCandidate = null;
        for (DOMNode child : dom.children) {
            DOMNode d = getBodyNode(child);
            if (d != null) bodyCandidate = d;
        }
        return bodyCandidate;
    }
    
    public DOMNode getTitleNode(DOMNode dom) {
        if (dom.type.equals(HTMLElements.TITLE)) return dom;
        DOMNode titleCandidate = null;
        for (DOMNode child : dom.children) {
            DOMNode d = getTitleNode(child);
            if (d != null) titleCandidate = d;
        }
        return titleCandidate;
    }
    
    public void removeUnknownElements(DOMNode dom) {
        List<DOMNode> newChildren = new ArrayList<DOMNode>();
        for (DOMNode child : dom.children) {
            if (HTMLElements.isValidElement(child.type)) {
                newChildren.add(child);
                removeUnknownElements(child);
            } else {
                if (debug) {
                    System.out.printf("HTMLParser: ignoring unknown element %s, %d children\n", child.type, child.children.size());
                }
            }
        }
        dom.children = newChildren;
    }
    
    public boolean isSingular(String content) {
        return content.endsWith("/>") || content.equals("<!doctype html>");
    }
    
    /**
     * Make a map of attributes in the contents of <...>. This function splits the contents on
     * spaces, but since there could be attributes such as class="one two", it must keep track of 
     * if one segment between spaces is actually the same attribute as the next.
     * @param content       The string with in the angle brackets of an HTML tag.
     * @return      A map from attribute name to value.
     */
    public Map<String, String> getAttributes(String content) {
        Map<String, String> attributes = new HashMap<String, String>();
        String[] rawAttributes = splitOnAttributes(removeUselessSpaces(content));

        for (String attribute : rawAttributes) {
            if (attribute.contains("=")) {
                int equalsIndex = attribute.indexOf("=");
                attributes.put(
                        attribute.substring(0, equalsIndex), 
                        attribute.substring(equalsIndex+2, attribute.length()-1));
            } else {
                attributes.put(attribute, null);
            }
        }
        
        return attributes;
    }
    
    /**
     * Remove all stretches of 2 or more spaces that are not between quotation marks. Also remove
     * spaces around any equals sign. Core functionality is run twice as a simple way to remove
     * weird spaces.
     * For example, the string 'value  =  " 1 "  ' would be changed to 'value=" 1 "'
     * @param s     String to process.
     * @return      Processed string.
     */
    public String removeUselessSpaces(String s) {
        s = s.trim();
        String result = "";
        for (int iter = 0; iter < 2; ++iter) {
            boolean inQuotes = false;
            String prev = "a";
            for (int i = 0; i < s.length(); ++i) {
                String letter = s.substring(i, i+1);
                String nextLetter = i+2 <= s.length() ? s.substring(i+1, i+2) : "x";
                String prevLetter = i > 0 ? s.substring(i-1, i) : "x";
                if (letter.equals("\"")) inQuotes = !inQuotes;
                if (inQuotes ||
                   (!(letter.matches("\\s") && prevLetter.matches("\\s")) &&
                   !(letter.matches("\\s") && nextLetter.matches("=")) &&
                   !(letter.matches("\\s") && prevLetter.matches("=")))) {
                    result += letter;
                }
                prev = letter;
            }
            if (iter == 0) {
                s = result;
                result = "";
            }
        }
        
        return result;
    }
    
    /**
     * TODO regex has some issues
     * Split a string of attributes on only the spaces that are not within quotes. Assumes that
     * input is from 'removeUselessSpaces' such that no multiple space blocks are present outside
     * of quotation marks.
     * (?=\\s) is a positive lookahead, it matches an upcoming whitespace but does not include it
     * in the match.
     * @param s     String of attributes to split.
     * @return      Array of strings where each is an attribute.
     */
    public String[] splitOnAttributes(String s) {
        // Pattern Examples: "x", ".. x", ".. x ..", "x="some stuff""
        Pattern pattern = Pattern.compile("(^[\\w-]+$)|([\\w-]+$)|([\\w-]+?(?=\\s))|([\\w-]+=\"[\\s\\w\\.\\-\\:\\;%#]+\")");
        Matcher matcher = pattern.matcher(s);
        List<String> attributes = new ArrayList<String>();
        while (matcher.find()) {
            attributes.add(matcher.group());
        }
        return attributes.toArray(new String[0]);
    }
    
    /**
     * Remove the comments from some HTML. Does not accept nested comments. Should replace with a 
     * better regular expression.
     * @param s
     * @return
     */
    public String removeComments(String s) {
        return removeTag(s, "<!--", "-->", true);
    }
    
    public String removeXML(String s) {
        return removeTag(s, "<\\?", "\\?>", true);
    }
    
    public String removeDoctype(String s) {
        return removeTag(s, "<\\!DOCTYPE", ">", false);
    }
    
    /**
     * Removes some arbitrary tag with a start and end, including all the content between.
     * For comments, set exact to true, since each comment needs an ending tag and ending tags
     * are only used for comments. For other patterns, where the ending tag might appear elsewhere
     * without the start tag, exact is false so that these other ending tags are left for later.
     * @param s
     * @param startPattern
     * @param endPattern
     * @param exact         If starting and ending patterns must pair up exactly.
     * @return
     */
    public String removeTag(String s, String startPattern, String endPattern, boolean exact) {
        Matcher startMatcher = Pattern.compile(startPattern).matcher(s);
        Matcher endMatcher = Pattern.compile(endPattern).matcher(s);
        List<Integer> starts = new ArrayList<Integer>();
        List<Integer> ends = new ArrayList<Integer>();
        while (startMatcher.find()) starts.add(startMatcher.start());
        while (endMatcher.find()) ends.add(endMatcher.end());
        
        StringBuilder html = new StringBuilder();

        if ((exact && starts.size() != ends.size()) || starts.size() > ends.size()) {
            System.err.printf("HTMLParser: unmatched comments, %d started, %d ended\n", starts.size(), ends.size());
        } else {
            int current_start = 0;
            for (int i = 0; i < starts.size(); i++) {
                html.append(s.substring(current_start, starts.get(i)));
                current_start = ends.get(i);
            }
            html.append(s.substring(current_start));
        }
        
        return html.toString();
    }
    
    public String getTitle(DOMNode dom) {
        DOMNode title = getTitleNode(dom);
        if (title != null && title.children.size() == 1) {
            return title.children.get(0).content;
        } else {
            return null;
        }
    }

}
