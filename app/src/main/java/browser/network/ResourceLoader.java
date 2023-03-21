package browser.network;

import static browser.constants.ResourceConstants.FILE_PREFIX;

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
import browser.parser.HTMLElements;
import browser.parser.HTMLParser;
import browser.renderer.ImageCache;

public class ResourceLoader {

    private enum resourceType {IMG, CSS}
    
    private DOMNode dom;
    private final Map<resourceType, Set<String>> resources;
    private final List<String> externalCSS;

    public ResourceLoader() {
        dom = null;
        resources = new HashMap<>();
        resources.put(resourceType.IMG, new HashSet<>());
        resources.put(resourceType.CSS, new HashSet<>());
        externalCSS = new ArrayList<>();
    }
    
    public DOMNode getDOM() { return dom; }
    public List<String> getExternalCSS() { return externalCSS; }
    
    /**
     * Load the HTML for a given URL, and load all other resources linked in that file
     */
    public void loadWebpage(String url) {
        String html = null;
        if (url.startsWith(FILE_PREFIX)) {
            try {
                String filePath = url.substring(FILE_PREFIX.length());
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
        
        HTMLParser parser = new HTMLParser();
        dom = parser.generateDOMTree(html);

        // TODO combine the extraction with the image/css loading code. No need to store resources map right?
        extractResourceAttributes(dom);

        for (String imgURL : resources.get(resourceType.IMG)) {
            if (url.startsWith(FILE_PREFIX)) {
                ImageCache.loadLocalImage(imgURL, url);
            } else {
                ImageCache.loadImage(imgURL);
            }
        }
        
        for (String cssURL : resources.get(resourceType.CSS)) {
            String css = HTTPClient.requestResource(cssURL);
            externalCSS.add(css);
            System.out.printf("Loaded %d characters of css from %s.\n", (css == null ? 0 : css.length()), cssURL);
        }
        
    }

    private void extractResourceAttributes(DOMNode domNode) {
        if (domNode.type.equals(HTMLElements.IMG)) {
            String src = domNode.attributes.get("src");
            if (src != null) {
                resources.get(resourceType.IMG).add(src);
            }
        } else if (domNode.type.equals(HTMLElements.LINK)) {
            String href = domNode.attributes.get("href");
            if (href != null && href.endsWith("css")) {
                resources.get(resourceType.CSS).add(href);
            }
        }

        for (DOMNode child : domNode.children) {
            extractResourceAttributes(child);
        }
    }

}
