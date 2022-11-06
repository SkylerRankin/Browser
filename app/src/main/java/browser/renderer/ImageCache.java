package browser.renderer;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javafx.scene.image.Image;

import browser.network.HTTPClient;

import static browser.constants.ResourceConstants.FILE_PREFIX;

public class ImageCache {

    private static Map<String, Image> images = new HashMap<String, Image>();
    private static Set<String> brokenImageLinks = new HashSet<String>();

    public static void loadDefaultImages() {
        File file = new File("./src/main/resources//images//defaultImage.png");
        if (!file.exists()) {
            System.err.println("ImageCache: defaultImage.png doesn't exist");
        }
        try {
            Image defaultImage = new Image(file.toURI().toString());
            images.put("default", defaultImage);
        } catch (RuntimeException e) {
            System.err.println("ImageCache.loadDefaultImages: failed to load default image, internal graphics not initialized yet.");
        }
    }

    public static void loadLocalImage(String imagePath, String url) {
        String originalImagePath = imagePath;
        url = url.startsWith(FILE_PREFIX) ? url.substring(FILE_PREFIX.length()) : url;
        imagePath = imagePath.startsWith("./") ? imagePath.substring(2) : imagePath;

        String urlPreviousLevel = url.substring(0, url.lastIndexOf("/") + 1);

        if (!url.endsWith("/")) {
            url = url + "/";
        }

        String[] filePaths = {
                url + imagePath,
                urlPreviousLevel + imagePath,
                imagePath
        };

        for (String filePath : filePaths) {
            File imageFile = new File(filePath);
            if (imageFile.exists()) {
                Image image = new Image(imageFile.toURI().toString());
                images.put(originalImagePath, image);
                break;
            }
        }
    }

    public static void loadImage(String url) {
        Image image = HTTPClient.downloadImage(url);
        if (image != null) {
            images.put(url, image);
        } else {
            brokenImageLinks.add(url);
        }
    }

    public static Image getImage(String url) {
        Image image = images.get(url);
        if (image == null) image = images.get("default");
        return image;
    }

}
