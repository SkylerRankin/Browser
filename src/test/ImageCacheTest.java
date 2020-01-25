package test;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

import javafx.embed.swing.JFXPanel;
import renderer.ImageCache;

public class ImageCacheTest {
	
	@Before
	public void before() {
		JFXPanel jfxPanel = new JFXPanel();
	}
	
	@Test
	public void loadDefaultImagesTest() {
		ImageCache.loadDefaultImages();
		System.out.println(ImageCache.getImage("default"));
	}

}
