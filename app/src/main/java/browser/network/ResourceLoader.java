package browser.network;

import static browser.constants.ResourceConstants.FILE_PREFIX;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import browser.app.ErrorPageHandler;
import browser.constants.ErrorConstants;
import browser.constants.ErrorConstants.ErrorType;
import browser.exception.PageLoadException;
import browser.model.DOMNode;
import browser.parser.HTMLElements;
import browser.parser.HTMLParser;
import browser.renderer.ImageCache;

import lombok.Getter;

public class ResourceLoader {

    private enum resourceType {IMG, CSS}

    @Getter
    private DOMNode dom;
    private final Map<resourceType, Set<String>> resources;
    @Getter
    private final List<String> externalCSS;

    public ResourceLoader() {
        resources = new HashMap<>();
        resources.put(resourceType.IMG, new HashSet<>());
        resources.put(resourceType.CSS, new HashSet<>());
        externalCSS = new ArrayList<>();
    }
    
    /**
     * Load the HTML for a given URL, and load all other resources linked in that file
     */
    public void loadWebpage(String url) throws PageLoadException {
        String html = url.startsWith(FILE_PREFIX) ?
                loadLocalHTMLFile(url) :
                HTTPClient.requestPage(url);
        
        if (url.equals(ErrorConstants.ErrorPagePath)) {
            html = ErrorPageHandler.populateHTML(html);
        }

        HTMLParser parser = new HTMLParser();
        dom = parser.generateDOMTree(html);

        // TODO combine the extraction with the image/css loading code. No need to store resources map right?
        resources.get(resourceType.IMG).clear();
        resources.get(resourceType.CSS).clear();
        extractResourceAttributes(dom);

        for (String imgURL : resources.get(resourceType.IMG)) {
            if (url.startsWith(FILE_PREFIX)) {
                ImageCache.loadLocalImage(imgURL, url);
            } else {
                ImageCache.loadImage(imgURL);
            }
        }

        externalCSS.clear();
        for (String cssURL : resources.get(resourceType.CSS)) {
            String css = url.startsWith(FILE_PREFIX) ?
                    loadLocalFileAsText(cssURL, url) :
                    HTTPClient.requestResource(cssURL);
            if (css != null && !css.isBlank()) {
                externalCSS.add(css);
                System.out.printf("Loaded %d characters of css from %s.\n", css.length(), cssURL);
            }
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

    private String loadLocalHTMLFile(String filePath) throws PageLoadException {
        if (filePath.endsWith(".html")) {
            try {
                Path path = Paths.get(filePath.substring(FILE_PREFIX.length()));
                if (!Files.exists(path)) {
                    throw new PageLoadException(ErrorType.LOCAL_FILE_DOES_NOT_EXIST, Map.of(ErrorConstants.PATH, filePath));
                } else if (Files.isDirectory(path)) {
                    throw new PageLoadException(ErrorType.LOCAL_FILE_IS_DIRECTORY, Map.of(ErrorConstants.PATH, filePath));
                } else {
                    return new String(Files.readAllBytes(path));
                }
            } catch (IOException e) {
                System.err.printf("ResourceLoader: failed to load %s, %s\n", filePath, e.getLocalizedMessage());
                throw new PageLoadException(ErrorType.LOCAL_FILE_FAILED_TO_LOAD, Map.of(ErrorConstants.EXCEPTION, e));
            }
        } else {
            throw new PageLoadException(ErrorType.LOCAL_FILE_IS_NOT_HTML, Map.of(ErrorConstants.PATH, filePath));
        }
    }

    private String loadLocalFileAsText(String filePath, String pageURL) {
        String separator = pageURL.contains("/") ? "/" : pageURL.contains("\\") ? "\\" : null;
        List<String> paths = new ArrayList<>(List.of(
                filePath,
                Path.of(pageURL.substring(FILE_PREFIX.length()), filePath).toString()
        ));
        if (separator != null) {
            paths.add(Path.of(pageURL.substring(FILE_PREFIX.length(), pageURL.lastIndexOf(separator)), filePath).toString());
        }

        for (String pathString : paths) {
            Path path = Paths.get(pathString);
            if (Files.exists(path) && !Files.isDirectory(path)) {
                try {
                    return new String(Files.readAllBytes(path));
                } catch (IOException ignored) {}
            }
        }
        return null;
    }

}
