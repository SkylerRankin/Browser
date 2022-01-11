package browser.renderer;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javafx.scene.image.Image;
import browser.network.HTTPClient;

public class ImageCache {
	
	private static Map<String, Image> images = new HashMap<String, Image>();
	private static Set<String> brokenImageLinks = new HashSet<String>();
	
	public static void loadDefaultImages() {
		File file = new File("./src/main/resources//images//defaultImage.png");
		if (!file.exists()) {
			System.err.println("ImageCache: defaultImage.png doesn't exist");
		}
		Image defaultImage = new Image(file.toURI().toString());
		images.put("default", defaultImage);
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
