package browser.renderer;

import javafx.embed.swing.JFXPanel;

import org.junit.Before;
import org.junit.Test;

public class ImageCacheTest {

    @Before
    public void before() {
        new JFXPanel();
    }

    @Test
    public void loadDefaultImagesTest() {
        ImageCache.loadDefaultImages();
        System.out.println(ImageCache.getImage("default"));
    }

}
