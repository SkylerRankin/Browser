package browser.renderer;

import org.junit.Before;
import org.junit.Test;
import org.junit.Before;

//import javafx.embed.swing.JFXPanel;
import browser.renderer.ImageCache;

public class ImageCacheTest {

//	@Before
//	public void before() {
//		JFXPanel jfxPanel = new JFXPanel();
//	}

    @Test
    public void loadDefaultImagesTest() {
        ImageCache.loadDefaultImages();
        System.out.println(ImageCache.getImage("default"));
    }

}
