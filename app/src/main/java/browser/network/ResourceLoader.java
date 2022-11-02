package browser.network;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import browser.app.ErrorPageHandler;
import browser.app.StartupPageHandler;
import browser.model.DOMNode;
import browser.parser.HTMLParser;
import browser.renderer.ImageCache;

public class ResourceLoader {

    private enum resourceType {IMG, CSS}
    
    private DOMNode dom;
    private Map<resourceType, Set<String>> resources;
    private List<String> externalCSS;
    private final String filePrefix = "file://";
    private final boolean debug = true;
    
    public ResourceLoader() {
        dom = null;
        resources = new HashMap<resourceType, Set<String>>();
        resources.put(resourceType.IMG, new HashSet<String>());
        resources.put(resourceType.CSS, new HashSet<String>());
        externalCSS = new ArrayList<String>();
    }
    
    public DOMNode getDOM() { return dom; }
    public List<String> getExternalCSS() { return externalCSS; }
    
    /**
     * Load the HTML for a given URL, and load all other resources linked in that file
     * @param url
     */
    public void loadWebpage(String url) {
        
        String html = null;
        if (url.startsWith(filePrefix)) {
            try {
                String filePath = url.substring(filePrefix.length());
                html = new String(Files.readAllBytes(Paths.get(filePath)));
            } catch (IOException e) {
                System.err.printf("ResourceLoader: failed to load %s, %s\n", url, e.getLocalizedMessage());
//                e.printStackTrace();
            }
        } else {
            html = HTTPClient.requestPage(url);
        }
        
        if (url.equals(ErrorPageHandler.errorPagePath)) html = ErrorPageHandler.populateHTML(html);
        if (url.equals(StartupPageHandler.startupPagePath)) html = StartupPageHandler.populateHTML(html);
        
        HTMLParser parser = new HTMLParser(this);
        dom = parser.generateDOMTree(html);
        parser.removeUnknownElements(dom);
        
        for (String imgURL : resources.get(resourceType.IMG)) {
            ImageCache.loadImage(imgURL);
        }
        
        for (String cssURL : resources.get(resourceType.CSS)) {
            String css = HTTPClient.requestResource(cssURL);
            externalCSS.add(css);
            System.out.printf("Loaded %d characters of css from %s.\n", (css == null ? 0 : css.length()), cssURL);
        }
        
    }
    
    /**
     * Check the attributes of an HTML element to find resources to load. Only loads src from an
     * img tag, and href from a link tag that links to a CSS file.
     * @param tag
     * @param attributes
     */
    public void checkAttributes(String tag, Map<String, String> attributes) {
        
        if (tag.equals("img")) {
            String src = attributes.get("src");
            if (src != null) {
                resources.get(resourceType.IMG).add(src);
                if (debug) {
                    System.out.printf("ResourceLoader: found img src [%s]\n", src);
                }
            }
        } else if (tag.equals("link")) {
            String href = attributes.get("href");
            if (href != null && href.endsWith("css")) {
                resources.get(resourceType.CSS).add(href);
                if (debug) {
                    System.out.printf("ResourceLoader: found link href [%s]\n", href);
                }
            }
        }
        
    }

}
